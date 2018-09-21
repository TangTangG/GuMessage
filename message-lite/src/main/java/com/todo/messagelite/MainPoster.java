package com.todo.messagelite;

import android.os.Handler;
import android.os.Looper;


class MainPoster extends Handler implements Poster{

    public MainPoster() {
        super(Looper.getMainLooper());
    }

    @Override
    public void enqueue(Receiver receiver, Object data) {
        post(new InvokeRunnable(receiver,data));
    }

    @Override
    public void quit() {
        removeCallbacksAndMessages(null);
    }
}
