package com.firestack.kit;

import com.firestack.laksaj.blockchain.TxBlock;
import com.firestack.laksaj.jsonrpc.HttpProvider;
import com.firestack.laksaj.jsonrpc.Rep;
import com.firestack.laksaj.transaction.Transaction;

import java.io.IOException;
import java.util.List;

public class PollingDeposits {
    public static void main(String[] args) throws IOException, InterruptedException {
        Service service = new Service();
        HttpProvider provider = new HttpProvider("https://dev-api.zilliqa.com/");
        service.setHttpProvider(provider);
        service.setLastFetchedTxBlock(362614);
        service.setInterval(1000);
        service.setAddress("4baf5fada8e5db92c3d3242618c5b47133ae003c");
        service.poll();
    }

    public static class Service {
        private String address;
        private HttpProvider httpProvider;
        private Integer lastFetchedTxBlock;
        private long interval;


        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public HttpProvider getHttpProvider() {
            return httpProvider;
        }

        public void setHttpProvider(HttpProvider httpProvider) {
            this.httpProvider = httpProvider;
        }

        public Integer getLastFetchedTxBlock() {
            return lastFetchedTxBlock;
        }

        public void setLastFetchedTxBlock(Integer lastFetchedTxBlock) {
            this.lastFetchedTxBlock = lastFetchedTxBlock;
        }


        public void poll() throws IOException, InterruptedException {
            while (true) {
                task();
                System.out.println("---------------------------------------------------------");
                Thread.sleep(interval);
            }
        }

        public long getInterval() {
            return interval;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }

        private void task() throws IOException {
            TxBlock currentBlock = httpProvider.getLatestTxBlock().getResult();
            String currentBlockNumber = currentBlock.getHeader().getBlockNum();
            System.out.println("last fetched block number = " + lastFetchedTxBlock);
            System.out.println("current block number = " + currentBlockNumber);
            if (Integer.valueOf(currentBlockNumber) > lastFetchedTxBlock) {
                Rep<List<List<String>>> rep = this.httpProvider.getTransactionsForTxBlock(lastFetchedTxBlock.toString());
                if (null == rep.getResult()) {
                    lastFetchedTxBlock = lastFetchedTxBlock + 1;
                    return;
                }

                for (List<String> list : rep.getResult()) {
                    if (list.isEmpty()) {
                        continue;
                    }
                    for (String hash : list) {
                        Transaction transaction = httpProvider.getTransaction(hash).getResult();
                        if (transaction.getToAddr().equalsIgnoreCase(this.address)) {
                            System.out.printf("Found deposit for %s, amount = %s\n", this.address, transaction.getAmount());
                        }
                    }
                }
                lastFetchedTxBlock = lastFetchedTxBlock + 1;
            }

        }
    }
}




