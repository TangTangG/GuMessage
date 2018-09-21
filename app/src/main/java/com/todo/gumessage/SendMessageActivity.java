package com.todo.gumessage;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.todo.messagelite.PackageStation;

public class SendMessageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
    }

    public void mainMsg(View view) {
        PackageStation.getDefault().buildPackage()
                .setTag(MessageFlag.MAIN_MSG_BASE)
                .post();

    }

    public void backMsg(View view) {
        PackageStation.getDefault().buildPackage()
                .setTag(MessageFlag.BACKGROUND_MSG_BASE)
                .post();
    }

    public void asyncMsg(View view) {
        PackageStation.getDefault().buildPackage()
                .setTag(MessageFlag.ASYNC_MSG_BASE)
                .post();

    }

    public void mainMsg2(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PackageStation.getDefault().buildPackage()
                        .setTag(MessageFlag.MAIN_MSG_BASE)
                        .post();
            }
        }).start();
    }
}
