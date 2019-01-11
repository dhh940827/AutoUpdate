package com.dhh.mylibrary;

import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

/**
 * Created by 79393 on 2018/12/15.
 */

public class CusServiceConnection implements ServiceConnection, OnServiceRunnableListener {

    private Context mContext;
    private TaskInfo info;
    private DownLoadService downLoadService;
    private OnServiceCallListener serviceCallListener;
    private static final int ID = 1;
    private NotificationController controller;
    private UpdateFlame.OnUpdateControllerListener mUpdateConntrollerListener;

    public CusServiceConnection(Context context, TaskInfo info, UpdateFlame.OnUpdateControllerListener updateConntrollerListener) {
        mContext = context;
        this.info = info;
        mUpdateConntrollerListener = updateConntrollerListener;
        controller = new NotificationController.Builder()
                .setContext(mContext)
                .setLayout(R.layout.remoteview_layout)
                .setNotificationId(ID)
                .build();
    }

    @Override
    public void onDownLoadStart() {
        showNotification(0);
    }

    @Override
    public void onDownLoadFinish() {
        // 停止下载线程
        serviceCallListener.stop();
        // 通知Flame下载完毕
        mUpdateConntrollerListener.downLoadFinish();
    }

    @Override
    public void setProgress(final int progress) {
        showNotification(progress);
        mUpdateConntrollerListener.getProgress(progress);
    }

    @Override
    public void onServiceConnected(final ComponentName name, IBinder service) {
        downLoadService = ((DownLoadService.MsgBinder) service).getService();
        serviceCallListener = (OnServiceCallListener) downLoadService;
        mUpdateConntrollerListener.downLoadStart();
        downLoadService.startDownLoad(info, this);
    }


    /**
     * 更新通知状态
     *
     * @param progress 通知栏进度
     */
    private void showNotification(int progress) {
        controller.setText(R.id.tv_isfinished, progress != 100 ? mContext.getResources().getString(R.string.is_loading)
                : mContext.getResources().getString(R.string.is_finished))
                .setText(R.id.tv_pencent, progress + "%")
                .setText(R.id.tv_cancelorinstall, progress != 100 ? mContext.getResources().getString(R.string.cancel)
                        : mContext.getResources().getString(R.string.install))
                .setText(R.id.tv_pencent, progress + "%")
                .setProgress(R.id.pb_noti_loaded, progress)
                .notifyNotification();
    }

    public void startDownLoad() {
        downLoadService.startDownLoad(info, this);
    }

    public void cancelNotificaiton(){
        controller.cancelNotification(ID);
    }


    public OnServiceCallListener getServiceCallListener() {
        return serviceCallListener;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

    @Override
    public void onBindingDied(ComponentName name) {
    }

}
