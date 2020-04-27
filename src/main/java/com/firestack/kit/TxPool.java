package com.firestack.kit;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.firestack.laksaj.blockchain.DsBlock;
import com.firestack.laksaj.blockchain.TxBlock;
import com.firestack.laksaj.exception.ZilliqaAPIException;
import com.firestack.laksaj.jsonrpc.HttpProvider;
import com.firestack.laksaj.transaction.Transaction;
import com.firestack.laksaj.transaction.TransactionFactory;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class TxPool {
    private HashMap<String, Long> txMap = new HashMap<>();
    private ReentrantReadWriteLock txMapLock = new ReentrantReadWriteLock();

    private ListeningExecutorService txExecutor;
    private ScheduledExecutorService epochExecutor;

    private HttpProvider provider;
    private long dsEpoch;
    private long txEpoch;

    public TxPool(String api) {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        this.txExecutor = MoreExecutors.listeningDecorator(cachedThreadPool);

        ScheduledThreadPoolExecutor scheduleThreadPool = new ScheduledThreadPoolExecutor(1);
        this.epochExecutor = MoreExecutors.getExitingScheduledExecutorService(scheduleThreadPool);

        this.provider = new HttpProvider(api);
        this.poll();
    }

    public ListenableFuture<String> createTransaction(Transaction tx) {
        // execute the transaction using raw CreateTransaction JSON-RPC message
        // add it to this.txPool and this.txMap
        return txExecutor.submit(() -> {
            try {
                HttpProvider.CreateTxResult res =  TransactionFactory.createTransaction(tx);
                String txHash = res.getTranID();
                tx.setID(txHash);

                txMapLock.writeLock().lock();
                this.txMap.put(txHash, this.dsEpoch);
                txMapLock.writeLock().unlock();

                return txHash;
            } catch (IOException e) {
                System.out.println("Failed tx");
                throw e;
            }
        });
    }

    public boolean isPending(String txHash) {
        try {
            txMapLock.readLock().lock();
            return txMap.containsKey(txHash);
        } finally {
            txMapLock.readLock().unlock();
        }
    }

    private void poll() {
        // this is thread-safe because there is only 1 thread on this executor.
        epochExecutor.schedule(() -> {
            try {
                boolean shouldReconcile = false;
                DsBlock dsBlock = this.provider.getLatestDsBlock().getResult();
                Long currentDsEpoch = Long.parseLong(dsBlock.getHeader().getBlockNumber());

                if (this.dsEpoch != currentDsEpoch) {
                    this.dsEpoch = currentDsEpoch;
                    shouldReconcile = true;
                }

                TxBlock txBlock = this.provider.getLatestTxBlock().getResult();
                Long currentTxEpoch = Long.parseLong(txBlock.getHeader().getBlockNum());
                if (this.txEpoch != currentTxEpoch) {
                    this.txEpoch = currentTxEpoch;
                    shouldReconcile = true;
                }

                if (shouldReconcile) {
                    this.reconcileTxPool();
                }
            } catch (IOException | ZilliqaAPIException e) {
                System.out.println("Failed");
            }
        }, 5, TimeUnit.SECONDS);
    }

    private void reconcileTxPool() {
        this.txMap.forEach((String id, Long birthday) -> this.txExecutor.submit(() -> {
            try {
                // we don't care if the tx was confirmed if the ds epoch has changed, as the mempool will be cleared.
                txMapLock.writeLock().lock();

                if (birthday < dsEpoch) {
                    txMap.remove(id);
                    return;
                }

                // otherwise, we should check if the transaction has been confirmed
                Transaction res = provider.getTransaction(id).getResult();

                if (res.isConfirmed() || res.isRejected()) {
                    txMap.remove(id);
                }
            } catch (IOException e) {
                System.out.println("Something happened");
            } finally {
                txMapLock.writeLock().unlock();
            }
        }));
    }
}
