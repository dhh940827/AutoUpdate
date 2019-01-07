package com.dhh.mylibrary;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by 79393 on 2018/12/15.
 */

public class DownLoadService extends Service {

    private DownLoadRunable runable;

    public class MsgBinder extends Binder {

        public DownLoadService getService(){
            return DownLoadService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MsgBinder();
    }

    public void startDownLoad(String url, TaskInfo info, CusServiceConnection.OnServiceCall call){
        Log.e("服务开始","yes！！！");
        runable = new DownLoadRunable(url,info,call);
        new Thread(runable).start();
    }

    public void goAgain(){
        if(runable != null)
            new Thread(runable).start();
    }

}
