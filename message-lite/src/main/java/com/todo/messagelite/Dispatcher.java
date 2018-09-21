package com.todo.messagelite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by TCG on 2018/9/14.
 */

class Dispatcher {

    private List<Package> packages = new ArrayList<>(10);

    private int state = 0;

    private final int FLAG_INITIAL = 0;
    private final int FLAG_WORKING = 1;
    private final int FLAG_FREE = 2;

    private final int MAX_POOL_SIZE = 10;

    private boolean cancel = false;
    private Dispatcher next;

    private static volatile int poolSize = 0;
    private static final Object POOL_LOCK = new Object();
    private static Dispatcher cachePool;

    private void change2State(int newS) {
        state = newS;
    }

    public static Dispatcher obtain() {
        synchronized (POOL_LOCK) {
            if (cachePool != null) {
                Dispatcher d = cachePool;
                cachePool = d.next;
                d.next = null;
                poolSize--;
                return d;
            }
        }
        return new Dispatcher();
    }

    void recycle() {
        clearForRecycle();
        synchronized (POOL_LOCK) {
            if (poolSize < MAX_POOL_SIZE) {
                change2State(FLAG_FREE);
                next = cachePool;
                cachePool = this;
                poolSize++;
            }
        }
    }

    void clearForRecycle() {
        packages.clear();
        cancel = false;
        change2State(FLAG_INITIAL);
    }

    public Dispatcher updatePackage(Collection<Package> packages) {
        if (Utils.invalidCollection(packages)) {
            return this;
        }
        this.packages.clear();
        this.packages.addAll(packages);
        return this;
    }

    public void work() {
        change2State(FLAG_WORKING);
        for (Package p : packages) {
            if (cancel) {
                break;
            }
            if (p == null) {
                continue;
            }
            p.send2Target();
            p.recycle();
        }
        change2State(FLAG_FREE);
        recycle();
    }

    public void cancel() {
        cancel = true;
    }

    public boolean working() {
        return state == FLAG_WORKING;
    }

}
