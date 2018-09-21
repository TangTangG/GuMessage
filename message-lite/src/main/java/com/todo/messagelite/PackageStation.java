package com.todo.messagelite;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by TCG on 2018/9/10.
 * <p>
 * dispatch
 * add package
 * auto clean up
 */

public class PackageStation {

    private static final Map<Class<?>, List<Class<?>>> PACKAGE_PARAM_CLASS_CACHE
            = new ConcurrentHashMap<>(32);

    private static final SparseArray<CopyOnWriteArrayList<Receiver>> PACKAGE_RECEIVER_CACHE
            = new SparseArray<>(16);

    private final Poster poster;

    private CopyOnWriteArrayList<Subscriber> subscribers = new CopyOnWriteArrayList<Subscriber>();
    /**
     * This for auto clean up.
     */
    private final ReferenceQueue<Object> subscriberReferenceQueue = new ReferenceQueue<>();
    private final ScheduledExecutorService autoCleanService
            = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r,Utils.MODULE_TAG + "-auto-clean");
        }
    });

    private PackageStation() {
        this.poster = new PosterPolicy();
        startAutoClean();
    }

    private void startAutoClean() {
        autoCleanService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                while (subscriberReferenceQueue.poll() != null) {
                    //clear gc queue
                }
                releaseSubscriber(null);
            }
        },1,1, TimeUnit.MINUTES);
    }

    public void register(Object o) {
        if (o == null) {
            return;
        }
        Subscriber subscriber = new Subscriber(o, subscriberReferenceQueue);
        if (subscriber.methodHunter(subscriberReferenceQueue)) {
            subscribers.add(subscriber);
            tryUpdateReceiverCache(subscriber);
        }
    }

    private void tryUpdateReceiverCache(Subscriber subscriber) {
        int size = PACKAGE_RECEIVER_CACHE.size();
        for (int i = 0; i < size; i++) {
            int p = PACKAGE_RECEIVER_CACHE.keyAt(i);
            CopyOnWriteArrayList<Receiver> receivers = PACKAGE_RECEIVER_CACHE.get(p);
            Receiver r;
            if ((r = subscriber.matchPackage(p)) != null) {
                receivers.add(r);
            }
        }
    }

    public void unregister(final Object o) {
        if (o == null) {
            return;
        }
        releaseSubscriber(o);
        poster.quit();
    }

    private void releaseSubscriber(final Object o) {
        Utils.foreachCopyOnWrite(subscribers, new Utils.Foreach<Subscriber>() {
            @Override
            public void act(Subscriber subscriber) {
                Object reference = subscriber.getReference();
                if (reference == null || reference.equals(o)) {
                    subscriber.release();
                    subscribers.remove(subscriber);
                }
            }
        });
    }

    void post(Package pkg) {
        if (pkg == null) {
            return;
        }
        Class<?> paramCls = pkg.getParamCls();
        if (paramCls == null) {
            return;
        }
        List<Class<?>> list = getMatchClassType(paramCls);
        List<Package> packages = new ArrayList<>(16);
        for (Class<?> cls : list) {
            Package p = Package.copy(pkg);
            p.updateParamCls(cls);
            p.setTarget(getMatchedReceivers(p));
            packages.add(p);
        }
        Dispatcher.obtain()
                .updatePackage(packages)
                .work();
    }

    @NonNull
    private CopyOnWriteArrayList<Receiver> getMatchedReceivers(final Package p) {
        final int key = p.hashCode();
        final CopyOnWriteArrayList<Receiver> receivers = new CopyOnWriteArrayList<Receiver>();
        final CopyOnWriteArrayList<Receiver> cache = PACKAGE_RECEIVER_CACHE.get(key);
        if (Utils.invalidCollection(cache)) {
            Utils.foreachCopyOnWrite(subscribers, new Utils.Foreach<Subscriber>() {
                @Override
                public void act(Subscriber subscriber) {
                    Receiver r;
                    if ((r = subscriber.matchPackage(key)) != null) {
                        r.updatePoster(poster);
                        receivers.add(r);
                    }
                }
            });
            PACKAGE_RECEIVER_CACHE.put(key, receivers);
        } else {
            //try filter dirty receiver.
            Utils.foreachCopyOnWrite(cache, new Utils.Foreach<Receiver>() {
                @Override
                public void act(Receiver receiver) {
                    if (receiver.dirty()) {
                        cache.remove(receiver);
                    } else {
                        Receiver c = receiver.copy();
                        c.updatePoster(poster);
                        receivers.add(c);
                    }
                }
            });
        }
        return receivers;
    }

    @NonNull
    private List<Class<?>> getMatchClassType(Class<?> paramCls) {
        List<Class<?>> list = PACKAGE_PARAM_CLASS_CACHE.get(paramCls);
        if (Utils.invalidCollection(list)) {
            Class<?> cls = paramCls;
            list = new ArrayList<>(16);
            list.add(cls);
            cls = cls.getSuperclass();
            while (cls != null && !Utils.systemCls(cls)) {
                list.add(cls);
                findTypeClsInterface(cls, list);
                cls = cls.getSuperclass();
            }
            PACKAGE_PARAM_CLASS_CACHE.put(paramCls, list);
        }
        return list;
    }

    private void findTypeClsInterface(Class<?> cls, List<Class<?>> list) {
        Class<?>[] interfaces = cls.getInterfaces();
        for (Class<?> i : interfaces) {
            if (!list.contains(i)) {
                list.add(i);
                findTypeClsInterface(i, list);
            }
        }
    }

    public static void sendEmptyPackage(int tag) {
        new PackageBuilder(getDefault())
                .setTag(tag)
                .setData(null)
                .post();
    }

    public PackageBuilder buildPackage() {
        return new PackageBuilder(this);
    }

    public static class PackageBuilder {
        PackageStation station;
        int tag;
        Object data;

        public PackageBuilder(PackageStation station) {
            this.station = station;
        }

        public PackageBuilder setTag(int tag) {
            this.tag = tag;
            return this;
        }

        public PackageBuilder setData(Object data) {
            this.data = data;
            return this;
        }

        public void post() {
            Package p = Package.obtain();
            p.setTag(tag);
            p.setData(data);
            Log.d("tang", "post: " + tag);
            station.post(p);
        }

    }

    //*********************************
    //This for default package station.
    //*********************************

    private static class DefaultInstance {
        static final PackageStation STATION = new PackageStation();
    }

    public static PackageStation getDefault() {
        return DefaultInstance.STATION;
    }

}
