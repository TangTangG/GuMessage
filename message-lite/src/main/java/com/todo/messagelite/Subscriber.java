package com.todo.messagelite;

import android.util.SparseArray;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * Created by TCG on 2018/9/17.
 */

class Subscriber {

    private SparseArray<Receiver> receivers;

    private WeakReference<Object> reference;

    Subscriber(Object o, ReferenceQueue<Object> referenceQueue) {
        this.reference = new WeakReference<Object>(o, referenceQueue);
    }

    boolean methodHunter(ReferenceQueue<Object> referenceQueue) {
        if (reference == null || reference.get() == null) {
            return false;
        }
        receivers = MethodHunter.findByCls(reference.get(), referenceQueue);
        return receivers.size() != 0;
    }

    Receiver matchPackage(int p) {
        if (receivers == null) {
            return null;
        }
        return receivers.get(p);
    }

    Object getReference() {
        Object o = null;
        if (reference != null) {
            o = reference.get();
        }
        return o;
    }

    void release() {
        if (receivers != null) {
            int size = receivers.size();
            for (int i = 0; i < size; i++) {
                Receiver receiver = receivers.valueAt(i);
                if (receiver != null) {
                    receiver.markDirty();
                }
            }
            receivers.clear();
        }
        if (reference != null) {
            reference.clear();
        }
    }
}
