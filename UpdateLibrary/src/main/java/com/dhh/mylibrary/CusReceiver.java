package com.dhh.mylibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getStringExtra("type") != null){
            String type = intent.getStringExtra("type");
            if(type.equals("cancel")){

            }else if(type.equals("install")) {

            }
        }
    }
}
