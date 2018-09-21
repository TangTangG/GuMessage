package com.todo.messagelite;

import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

class AsyncPoster implements Poster {

    private ExecutorService asyncPool;

    public AsyncPoster() {
        asyncPool = Executors.newCachedThreadPool(new SimpleThreadFactory());
    }

    @Override
    public void enqueue(Receiver receiver, Object data) {
        asyncPool.execute(new InvokeRunnable(receiver,data));
    }

    @Override
    public void quit() {
        asyncPool.shutdown();
    }

    private static class SimpleThreadFactory implements ThreadFactory{

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r,"message-lite-async");
        }
    }

}
