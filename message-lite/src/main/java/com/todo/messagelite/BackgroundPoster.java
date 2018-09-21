package com.todo.messagelite;

import android.os.Handler;
import android.os.HandlerThread;

class BackgroundPoster implements Poster {

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    BackgroundPoster() {
        mHandlerThread = new HandlerThread("message-lite");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public void enqueue(Receiver receiver, Object data) {
        mHandler.post(new InvokeRunnable(receiver, data));
    }

    @Override
    public void quit() {
        mHandlerThread.quit();
    }
}
