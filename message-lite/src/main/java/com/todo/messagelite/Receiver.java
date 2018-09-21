package com.todo.messagelite;

import android.util.Log;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * Created by TCG on 2018/9/10.
 */

class Receiver {

    private ExecuteMode mode = ExecuteMode.MAIN;
    private Poster poster;
    private int msgTag;
    private String methodName;
    private Class<?> paramCls;
    private boolean dirty = false;

    private WeakReference<Object> target;
    private Method targetMethod;

    void handlerPackage(Object data) {
        if (poster == null) {
            return;
        }
        if (paramCls == null && data != null) {
            return;
        }
        if (paramCls != null && data == null) {
            return;
        }
        if (paramCls != null && (data.getClass() != paramCls)) {
            return;
        }
        if (targetMethod == null) {
            findTargetMethod();
        }
        if (targetMethod != null){
            poster.enqueue(this, data);
        }
    }

    private void findTargetMethod() {
        if (target == null){
            return;
        }
        Object o = target.get();
        if (o == null){
            return;
        }
        try {
            targetMethod = o.getClass().getDeclaredMethod(methodName,paramCls);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    void updatePoster(Poster poster) {
        this.poster = poster;
    }

    void setMode(ExecuteMode mode) {
        this.mode = mode;
    }

    ExecuteMode getMode() {
        return mode;
    }

    void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    void setParamCls(Class<?> paramCls) {
        this.paramCls = paramCls;
    }

    void setMsgTag(int msgTag) {
        this.msgTag = msgTag;
    }

    void setTarget(Object target, ReferenceQueue<Object> referenceQueue) {
        this.target = new WeakReference<>(target, referenceQueue);
    }

    Object getTarget() {
        Object o = null;
        if (target != null) {
            o = target.get();
        }
        return o;
    }

    Method getTargetMethod() {
        return targetMethod;
    }

    void markDirty() {
        dirty = true;
        if (target != null) {
            target.clear();
            target = null;
            targetMethod = null;
        }
    }

    boolean dirty() {
        return dirty;
    }

    Receiver copy() {
        Receiver re = new Receiver();
        re.setMethodName(methodName);
        re.setMsgTag(msgTag);
        re.setParamCls(paramCls);
        re.target = target;
        re.setMode(mode);
        re.targetMethod = targetMethod;
        dirty = false;
        return re;
    }
}
