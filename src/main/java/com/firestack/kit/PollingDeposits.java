package com.firestack.kit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firestack.laksaj.blockchain.TxBlock;
import com.firestack.laksaj.exception.ZilliqaAPIException;
import com.firestack.laksaj.jsonrpc.HttpProvider;
import com.firestack.laksaj.jsonrpc.Rep;
import com.firestack.laksaj.transaction.Transaction;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public class PollingDeposits {
    public static void main(String[] args) throws IOException, InterruptedException, ZilliqaAPIException {
        Service service = new Service();
        HttpProvider provider = new HttpProvider("https://api.zilliqa.com/");
        service.setHttpProvider(provider);
        service.setLastFetchedTxBlock(513805);
        service.setInterval(1000);
        service.setAddress("b30fe431b52e3be050ee5f11d9ff80a091f8f5d9");
        service.poll();
    }

    public static class Service {
        private String address;
        private HttpProvider httpProvider;
        private Integer lastFetchedTxBlock;
        private long interval;
        private ObjectMapper mapper = new ObjectMapper();


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


        public void poll() throws IOException, InterruptedException, ZilliqaAPIException {
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

        public static class Msg {
            public String _amount;
            public String _recipient;
            public String _tag;
            public List<Object> params;
        }

        public static class Transition {
            public String addr;
            public Integer depth;
            public Msg msg;
        }

        private void task() throws IOException, ZilliqaAPIException {
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
                        if (transaction.getReceipt().isSuccess()) {
                            if (transaction.getReceipt().getTransitions() != null && transaction.getReceipt().getTransitions().size() != 0) {
                                // indicate it is a smart contract transaction
                                List<Object> transitions = transaction.getReceipt().getTransitions();
                                for (Object transitionObj : transitions) {
                                    String transitionStr = mapper.writeValueAsString(transitionObj);
                                    Transition ts = mapper.readerFor(Transition.class).readValue(transitionStr);
                                    if (ts.msg._recipient.equalsIgnoreCase("0x" + this.address)) {
                                        System.out.printf("Found deposit for %s, amount = %s\n", this.address, ts.msg._amount);
                                    }
                                }
                            } else {
                                // indicate it is a payment transaction
                                if (transaction.getToAddr().equalsIgnoreCase(this.address)) {
                                    System.out.printf("Found deposit for %s, amount = %s\n", this.address, transaction.getAmount());
                                }
                            }

                        }
                    }
                }
                lastFetchedTxBlock = lastFetchedTxBlock + 1;
            }

        }
    }
}




