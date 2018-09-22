package com.todo.gumessage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.todo.messagelite.ExecuteMode;
import com.todo.messagelite.PackageStation;
import com.todo.messagelite.annotation.AsReceiver;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PackageStation.getDefault().register(this);
    }
    long thread = Looper.getMainLooper().getThread().getId();
    final String TAG = getClass().getSimpleName();


    @AsReceiver(target = MessageFlag.ASYNC_MSG_BASE,mode = ExecuteMode.ASYNC)
    private void async(Object o){
        Log.d("tang", "async: "+TAG+" "+(thread ==Thread.currentThread().getId()));
    }

    @AsReceiver(target = MessageFlag.BACKGROUND_MSG_BASE,mode = ExecuteMode.BACKGROUND)
    private void back(Object o){
        Log.d("tang", "back: "+TAG+" "+(thread ==Thread.currentThread().getId()));
    }

    @AsReceiver(target = MessageFlag.MAIN_MSG_BASE)
    private void mainMode(Object o){
        Log.d("tang", "mainMode: "+TAG+" "+(thread ==Thread.currentThread().getId()));
    }

    public void simpleTest(View view) {
        startActivity(new Intent(this,SendMessageActivity.class));
    }

    public void multiThreadTest(View view) {
        startActivity(new Intent(this,MultiThreadActivity.class));
    }
}
