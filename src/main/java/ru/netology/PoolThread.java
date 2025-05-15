package ru.netology;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoolThread {
    private static final int NUMBER_THREADS_IN_POOL = 64;
    private final ExecutorService pool = Executors.newFixedThreadPool(NUMBER_THREADS_IN_POOL);

    private static class PoolThreadHead{
        private static final PoolThread poolThread = new PoolThread();
    }
    private PoolThread(){}
    public static PoolThread getInstance(){
        return PoolThreadHead.poolThread;
    }
    public ExecutorService getPool(){
        return pool;
    }
    public void setInterrupt(){
        pool.shutdown();
        while(!pool.isTerminated()){}
    }
}
