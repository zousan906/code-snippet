package com.archly.mhh.oversea.core.framework.net.http;


import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NetExecutor {
    private static final String TAG = "NET.executor";

    /**
     * 创建一个可重用固定线程数的线程池
     */
    private ExecutorService executor = null;


    private final Deque<HttpCall.AsyncCall> readyAsyncCalls = new ArrayDeque<>();
    private final Deque<HttpCall.AsyncCall> runningAsyncCalls = new ArrayDeque<>();
    private final Deque<HttpCall.RealCall> runningSyncCalls = new ArrayDeque<>();


    private int maxRequest;

    public NetExecutor(int maxRequest) {
        if (maxRequest > 0) {
            this.maxRequest = maxRequest;
        }
        executor = new ThreadPoolExecutor(0, 256,
                60, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new NamedThreadFactory("async"));


    }


    public NetExecutor() {
        this(64);
    }

    // 同步队列 需要外部来确定 并发线程数量


    /**
     * 异步任务处理
     */
    void execute(HttpCall.AsyncCall call) {
        synchronized (this) {
            readyAsyncCalls.add(call);
        }
        promoteAndExecute();
    }

    void executed(HttpCall.RealCall call) {
        synchronized (this) {
            runningSyncCalls.add(call);
        }
    }


    private boolean promoteAndExecute() {
        List<HttpCall.AsyncCall> executableCalls = new ArrayList<>();
        boolean isRunning = false;
        synchronized (this) {
            Iterator<HttpCall.AsyncCall> ite = readyAsyncCalls.iterator();
            while (ite.hasNext()) {
                HttpCall.AsyncCall asyncCall = ite.next();

                if (runningAsyncCalls.size() >= maxRequest) {
                    break;
                }

                ite.remove();
                executableCalls.add(asyncCall);
                runningAsyncCalls.add(asyncCall);
            }
            isRunning = runningCallCount() > 0;
        }
        for (HttpCall.AsyncCall executableCall : executableCalls) {
            executableCall.executeWith(executor);
        }

        return isRunning;
    }


    private int runningCallCount() {
        return runningAsyncCalls.size() + runningSyncCalls.size();
    }


    void finished(HttpCall.AsyncCall call) {
        finished(runningAsyncCalls, call);
    }

    void finished(HttpCall.RealCall call) {
        finished(runningSyncCalls, call);
    }

    private <T> void finished(Deque<T> deque, T call) {
        synchronized (this) {
            if (!deque.remove(call)) {
                Log.e(TAG, "Http call wasn't in flight.");
            }
        }

        boolean isRunning = promoteAndExecute();

        // TODO 这里可以加一个空闲回调
    }


    static class ThreadFactoryWithName implements ThreadFactory {

        private final String name;

        ThreadFactoryWithName(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, name);
        }
    }
}
