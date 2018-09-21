package com.todo.messagelite;

import android.util.Log;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by TCG on 2018/9/17.
 */

class Utils {

    static final String MODULE_TAG  = "message-lite";

    /**
     * This is the key of the msg;
     */
    static int generateMsgCode(int tag, Class<?> paramCls) {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((paramCls == null) ? 0 : paramCls.hashCode());
        result = prime * result + tag;
        return result;
    }

    static boolean systemCls(Class<?> cls) {
        if (cls == null) {
            return true;
        }
        String clsName = cls.getName();
        return clsName.startsWith("java.") || clsName.startsWith("javax.") || clsName.startsWith("android.");
    }

    static <T> boolean invalidCollection(Collection<T> list) {
        return list == null || list.size() == 0;
    }

    static <T> void foreachCopyOnWrite(CopyOnWriteArrayList<T> list, Foreach<T> foreach) {
        if (list == null) {
            Log.e("for-each", "Can not for each null list. ");
            return;
        }
        Iterator<T> i = list.iterator();
        while (i.hasNext()) {
            T next = i.next();
            if (foreach != null) {
                foreach.act(next);
            }
        }
    }

    interface Foreach<T> {
        void act(T t);
    }

}
