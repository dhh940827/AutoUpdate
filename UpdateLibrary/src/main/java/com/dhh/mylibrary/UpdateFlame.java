package com.dhh.mylibrary;

import android.content.Context;
import android.content.Intent;

import java.net.BindException;

/**
 * Created by 79393 on 2018/12/15.
 */

public class UpdateFlame {

    private Context mContext;
    private String url;
    private TaskInfo info;

    public UpdateFlame(Context context,String url){
        mContext = context;
        this.url = url;
        info = new TaskInfo();
    }
    private  CusServiceConnection conn;

    public void  BindService() throws Exception {
        createConnection(mContext);
        Intent intent = new Intent(mContext, DownLoadService.class);
        mContext.bindService(intent,conn,Context.BIND_AUTO_CREATE);
    }

    private void createConnection(Context context) throws Exception {
        if(info.getFileName().equals("") || info.getFilePath().equals("")){
            throw new Exception("value is empty!");
        }
        conn = new CusServiceConnection(context,url,info);
    }

    public void unBindService(Context context){
        context.unbindService(conn);
    }

    public void setFileName(String name){
        info.setFileName(name);
    }

    public void setFilePath(String path){
        info.setFilePath(path);
    }

    public void setAppName(String appName){
        info.setApp_name(appName);
    }
}
