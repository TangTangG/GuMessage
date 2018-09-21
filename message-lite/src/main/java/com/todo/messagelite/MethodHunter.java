package com.todo.messagelite;

import android.util.SparseArray;

import com.todo.messagelite.annotation.AsReceiver;

import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Method;

/**
 * Created by TCG on 2018/9/17.
 */

class MethodHunter {

    static SparseArray<Receiver> findByCls(Object o, ReferenceQueue<Object> referenceQueue) {
        Class<?> cls = o.getClass();
        SparseArray<Receiver> data = findByInject(cls);
        if (data != null && data.size() != 0) {
            return data;
        }
        data = new SparseArray<>(16);
        while (cls != null && !Utils.systemCls(cls)) {
            Method[] methods = cls.getDeclaredMethods();
            if (methods.length < 1) {
                cls = cls.getSuperclass();
                continue;
            }
            for (Method m : methods) {
                AsReceiver asReceiver = m.getAnnotation(AsReceiver.class);
                if (asReceiver == null) {
                    continue;
                }
                Class<?>[] types = m.getParameterTypes();
                if (types.length != 1) {
                    continue;
                }
                int tag = asReceiver.target();
                Class<?> paramCls = types[0];
                Receiver re = new Receiver();
                re.setMethodName(m.getName());
                re.setMsgTag(tag);
                re.setParamCls(paramCls);
                re.setTarget(o,referenceQueue);
                re.setMode(asReceiver.mode());
                data.put(Utils.generateMsgCode(tag, paramCls), re);
            }
            cls = cls.getSuperclass();
        }

        return data;
    }

    private static SparseArray<Receiver> findByInject(Class<?> cls) {
//        SparseArray<Receiver> data = new SparseArray<>(16);
        return null;
    }

}
