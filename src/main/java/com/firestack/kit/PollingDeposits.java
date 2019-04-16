package com.firestack.kit;

import com.firestack.laksaj.jsonrpc.HttpProvider;
import com.firestack.laksaj.jsonrpc.Rep;
import com.firestack.laksaj.transaction.Transaction;

import java.io.IOException;
import java.util.List;

public class PollingDeposits {
    public static void main(String[] args) throws IOException {
        HttpProvider provider = new HttpProvider("https://dev-api.zilliqa.com/");
        Rep<List<List<String>>> rep =  provider.getTransactionsForTxBlock("362616");

        System.out.println(rep);
        for (List<String> list :rep.getResult()){
            if (list.isEmpty()){
                continue;
            }

            for (String hash: list) {
                System.out.println(hash);
                Transaction transaction = provider.getTransaction(hash).getResult();
                System.out.println(transaction);
            }
        }
    }


    //todo
    public static class Service {
        private String address;
        private HttpProvider httpProvider;



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
    }
}




