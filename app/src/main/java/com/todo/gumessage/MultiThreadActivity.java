package com.todo.gumessage;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.todo.messagelite.PackageStation;
import com.todo.messagelite.annotation.AsReceiver;

public class MultiThreadActivity extends Activity {

    private final Object lock = new Object();
    private CmdThread[] pool;

    private volatile int command = -1;
    private static final int REGISTER_CMD = 1;
    private static final int UNREGISTER_CMD = 2;
    private static final int SEND_MESSAGE_CMD = 3;

    private static class CmdThread extends Thread {

        boolean interrupt = false;

        int cmd;

        CmdThread(String name) {
            super(name);
        }

        void setCmd(int cmd) {
            this.cmd = cmd;
        }

        void resetCmd() {
            cmd = -1;
        }

        Object o = new Object() {
            @AsReceiver(target = MessageFlag.MAIN_MSG_BASE)
            private void mainMode(Object o) {
                Log.e("tang", "mainMode: MultiThreadActivity "+getName());
            }
        };
    }

    final String TAG = getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_thread);
        pool = new CmdThread[20];
        for (int i = 0; i < 20; i++) {
            pool[i] = new CmdThread("multi-" + i) {
                @Override
                public void run() {
                    while (true) {
                        if (interrupt){
                            break;
                        }
                        if (cmd == -1 && !interrupt) {
                            try {
                                synchronized (lock){
                                    lock.wait();
                                }
                            } catch (InterruptedException e) {
//                                e.printStackTrace();
                                interrupt = true;
                                Log.i(TAG, "run: "+getName()+" mark interrupt");
                            }
                        } else {
                            doWork(o,getName());
                            resetCmd();
                        }
                    }
                }
            };
            pool[i].start();
        }
    }


    private void doWork(Object o, String name) {
        switch (command) {
            case REGISTER_CMD:
                multiThreadRegister(o,name);
                break;
            case UNREGISTER_CMD:
                multiThreadUnregisterMsg(o,name);
                break;
            case SEND_MESSAGE_CMD:
                multiThreadSendMessage(o,name);
                break;
        }
    }

    public void multiThreadRegister(Object o, String name) {
        Log.i(TAG, "multiThreadRegister: "+name);
        PackageStation.getDefault().register(o);
    }

    public void multiThreadSendMessage(Object o, String name) {
        Log.d(TAG, "multiThreadSendMessage:   send message"+name);
        PackageStation.getDefault().buildPackage()
                .setTag(MessageFlag.MAIN_MSG_BASE)
                .post();
    }

    public void multiThreadUnregisterMsg(Object o, String name) {
        Log.d(TAG, "multiThreadUnregisterMsg:        un register "+name);
        PackageStation.getDefault().unregister(o);
    }

    public void multiThreadRegister(View view) {
        command = REGISTER_CMD;
        notifyLock();
    }

    public void multiThreadSendMessage(View view) {
        command = SEND_MESSAGE_CMD;
        notifyLock();
    }

    public void multiThreadUnregisterMsg(View view) {
        command = UNREGISTER_CMD;
        notifyLock();
    }

    private synchronized void notifyLock() {
        for (CmdThread t : pool) {
            t.setCmd(command);
        }
        synchronized (lock){
            lock.notifyAll();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            for (Thread t : pool) {
                t.interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
