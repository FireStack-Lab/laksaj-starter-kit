package com.firestack.kit;

import com.firestack.laksaj.account.Wallet;
import com.firestack.laksaj.jsonrpc.HttpProvider;
import com.firestack.laksaj.transaction.Transaction;
import com.firestack.laksaj.transaction.TransactionFactory;
import com.firestack.laksaj.transaction.TxStatus;

import java.io.IOException;

import static com.firestack.laksaj.account.Wallet.pack;

public class TransactionOperation {
    public static void main(String[] args) throws IOException, InterruptedException {
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

        // Signing the Transaction
        transaction = wallet.sign(transaction);
        System.out.println("signature is: " + transaction.getSignature());


        // Sending the Transaction
        HttpProvider.CreateTxResult result = TransactionFactory.createTransaction(transaction);
        System.out.println(result);

        // Confirm
        transaction.confirm(result.getTranID(), 150, 2);

        // Still wait 2 blocks
        if (TxStatus.Rejected.equals(transaction.getStatus())) {
            String lastBlockNumberString = transaction.getProvider().getLatestTxBlock().getResult().getHeader().getBlockNum();
            Integer lastBlockNumber = Integer.valueOf(lastBlockNumberString);
            Integer currentBlockNumber = lastBlockNumber;
            while (currentBlockNumber < lastBlockNumber + 2) {
                currentBlockNumber = Integer.valueOf(transaction.getProvider().getLatestTxBlock().getResult().getHeader().getBlockNum());
            }
            boolean tracked = transaction.trackTx(result.getTranID());
            if (tracked) {
                System.out.println("Transaction confirmed!");

            } else {
                System.out.println("Transaction rejected!");

            }

        }
    }
}