package com.todo.messagelite;

import java.lang.reflect.Method;

/**
 * Created by TCG on 2018/9/20.
 */

class InvokeRunnable implements Runnable {

    private Receiver r;
    private Object data;

    InvokeRunnable(Receiver r, Object data) {
        this.r = r;
        this.data = data;
    }

    @Override
    public void run() {
        if (r == null) {
            data = null;
            return;
        }
        Method method = r.getTargetMethod();
        Object target = r.getTarget();
        if (method == null || target == null) {
            r.markDirty();
            r = null;
            data = null;
            return;
        }
        try {
            method.setAccessible(true);
            method.invoke(target, data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            r = null;
            data = null;
        }
    }
}
