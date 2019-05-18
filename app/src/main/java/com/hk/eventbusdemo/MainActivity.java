package com.hk.eventbusdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getInstance().register(this);

        startActivity(new Intent(this, SecondActivity.class));
    }

    @Subscribe(ThreadMode.MAIN)
    public void getMessage(EventBean bean) {
        Log.e("EventBus", bean.toString());
    }
}
