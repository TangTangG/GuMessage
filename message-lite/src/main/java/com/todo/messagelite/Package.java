package com.todo.messagelite;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by TCG on 2018/9/10.
 */

class Package {

    private int tag;
    private Object data;
    private Class<?> paramCls;
    private CopyOnWriteArrayList<Receiver> target;

    private Package next;

    private final static Object DEFAULT_DATA = new Object();

    private static volatile int poolSize = 0;
    private static final Object POOL_LOCK = new Object();
    private static Package cachePool;
    private final int MAX_POOL_SIZE = 10;

    public static Package obtain() {
        synchronized (POOL_LOCK) {
            if (cachePool != null) {
                Package d = cachePool;
                cachePool = d.next;
                d.next = null;
                poolSize--;
                return d;
            }
        }
        return new Package();
    }

    void clearForRecycle() {
        target = null;
        setData(null);
        tag = 0;
    }

    void recycle() {
        clearForRecycle();
        synchronized (POOL_LOCK) {
            if (poolSize < MAX_POOL_SIZE) {
                next = cachePool;
                cachePool = this;
                poolSize++;
            }
        }
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public void setData(Object data) {
        if (data == null) {
            data = DEFAULT_DATA;
        }
        this.data = data;
        this.paramCls = data.getClass();
    }

    public void setTarget(CopyOnWriteArrayList<Receiver> target) {
        this.target = target;
    }

    public void send2Target() {
        if (Utils.invalidCollection(target)) {
            return;
        }
        for (Receiver r : target) {
            r.handlerPackage(data);
        }
    }

    public static Package copy(Package src) {
        if (src == null) {
            return null;
        }
        Package p = obtain();
        p.tag = src.tag;
        p.data = src.data;
        return p;
    }

    public void updateParamCls(Class<?> paramCls) {
        this.paramCls = paramCls;
    }

    public Class<?> getParamCls() {
        return paramCls;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((paramCls == null) ? 0 : paramCls.hashCode());
        result = prime * result + tag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Package other = (Package) obj;
        if (tag != other.tag) {
            return false;
        }

        if (paramCls == null) {
            return other.paramCls == null;
        } else {
            return paramCls.equals(other.paramCls);
        }

    }


}
