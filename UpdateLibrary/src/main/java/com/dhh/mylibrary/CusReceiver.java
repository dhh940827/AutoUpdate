package com.dhh.mylibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

public class CusReceiver extends BroadcastReceiver {

    public static final String TAG = "CusReceiver";
    public static final String REC_TYPE = "type";
    public static final String REC_FILE = "file";
    public static final String CANCEL_SINAL = "cancel";
    public static final String INSTALL_SINAL = "install";
    private String file;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG,"have received the message");
        if(intent.getStringExtra(REC_TYPE) != null){
            String type = intent.getStringExtra(REC_TYPE);
            if(type.equals(CANCEL_SINAL)){
                //CusServiceConnection.onDownLoadCancel();
            }else if(type.equals(INSTALL_SINAL)) {
                Util.collapseStatusBar(context);
                file = intent.getStringExtra("file");
                Util.autoInstallApk(context,file);
            }
        }
    }

}
