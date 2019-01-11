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

public class DownLoadService extends Service implements OnServiceCallListener {

    private DownLoadRunable runable;
    private TaskInfo info;

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

    public void startDownLoad(TaskInfo info, OnServiceRunnableListener call){
        runable = new DownLoadRunable(info,call);
        this.info = info;
        new Thread(runable).start();
    }


    @Override
    public void goAgain() {
        if(info != null)
            info.setStop(false);
        else
            return;
        if(runable != null)
            new Thread(runable).start();
    }

    @Override
    public void stop(){
        info.setStop(false);
    }

}
