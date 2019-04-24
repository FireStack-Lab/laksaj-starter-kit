package com.firestack.kit;

import com.firestack.laksaj.account.Wallet;
import com.firestack.laksaj.jsonrpc.HttpProvider;
import com.firestack.laksaj.transaction.Transaction;
import com.firestack.laksaj.transaction.TransactionFactory;
import com.firestack.laksaj.transaction.TxStatus;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.firestack.laksaj.account.Wallet.pack;
import static java.time.temporal.ChronoUnit.SECONDS;

public class TransactionOperationAsync {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        Wallet wallet = new Wallet();
        wallet.setProvider(new HttpProvider("https://dev-api.zilliqa.com/"));
        String address = wallet.addByPrivateKey("e19d05c5452598e24caad4a0d85a49146f7be089515c905ae6a19e8a578a6930");
        System.out.println(address);

        // Constructing the Transaction Object
        Transaction transaction = Transaction.builder()
                .version(String.valueOf(pack(333, 1)))
                .toAddr("4baf5fada8e5db92c3d3242618c5b47133ae003c".toLowerCase())
                .senderPubKey("0246e7178dc8253201101e18fd6f6eb9972451d121fc57aa2a06dd5c111e58dc6a")
                .amount("10000000")
                .gasPrice("1000000000")
                .gasLimit("1")
                .code("")
                .data("")
                .provider(new HttpProvider("https://dev-api.zilliqa.com/"))
                .build();

        TxPool txPool = new TxPool("https://dev-api.zilliqa.com");

        // Signing the Transaction
        transaction = wallet.sign(transaction);
        System.out.println("signature is: " + transaction.getSignature());


        // Send the Transaction through the txPool
        String txHash = txPool.createTransaction(transaction).get();

        // the tx has gone into the tx pool. now, the pool can be queried.
        boolean pending = txPool.isPending(txHash);
        System.out.println("Tx is pending: " + pending);

        // Confirm
        transaction.confirm(txHash, 150, 2);

        // now it won't be pending anymore.
        boolean notPending = txPool.isPending(txHash);
        System.out.println("Tx is pending: " + notPending);

        // Still wait 2 blocks
        if (TxStatus.Rejected.equals(transaction.getStatus())) {
            String lastBlockNumberString = transaction.getProvider().getLatestTxBlock().getResult().getHeader().getBlockNum();
            Integer lastBlockNumber = Integer.valueOf(lastBlockNumberString);
            Integer currentBlockNumber = lastBlockNumber;
            while (currentBlockNumber < lastBlockNumber + 2) {
                currentBlockNumber = Integer.valueOf(transaction.getProvider().getLatestTxBlock().getResult().getHeader().getBlockNum());
                Thread.sleep(Duration.of(2, SECONDS).toMillis());
            }
            boolean tracked = transaction.trackTx(txHash);
            if (tracked) {
                System.out.println("Transaction confirmed!");

            } else {
                System.out.println("Transaction rejected!");

            }

        }
    }
}

