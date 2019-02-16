package com.dhh.mylibrary;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private  TaskInfo info;
    private DownLoadService downLoadService;
    private static OnServiceCallListener serviceCallListener;
    private static NotificationController controller;
    private UpdateFlame.OnUpdateControllerListener mUpdateConntrollerListener;
    private CusReceiver receiver;
    private static final int ID = 1;
    public static final int CODE = 100012;

    public CusServiceConnection(Context context, TaskInfo info, UpdateFlame.OnUpdateControllerListener updateConntrollerListener) {
        mContext = context;
        this.info = info;
        mUpdateConntrollerListener = updateConntrollerListener;
        prepareNotification();
    }

    /**
     * 不可以动态注册广播
     */
    private void setBroadcastReceiver() {
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("android.type.update");
//        receiver = new CusReceiver(this);
//        mContext.registerReceiver(receiver,intentFilter);
    }

    /**
     * 准备notification的controller
     */
    private void prepareNotification() {
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
        writeLenAfterFinish();
        // 延迟等到通知彻底更新
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Util.autoInstallApk(mContext,info.getFilePath() + File.separator + info.getFileName());
    }

    public  void onDownLoadCancel(){
        // 停止下载线程
        serviceCallListener.stop();
        cancelNotificaiton();
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
        //downLoadService.startDownLoad(info, this);
    }



    /**
     * 更新通知状态
     *
     * @param progress 通知栏进度
     */
    private void showNotification(int progress) {
        if(progress == 100)
            controller.setViewOnClick(R.id.tv_cancelorinstall,getIntent(true));
        controller.setText(R.id.tv_isfinished, progress != 100 ? mContext.getResources().getString(R.string.is_loading)
                : mContext.getResources().getString(R.string.is_finished))
                .setText(R.id.tv_pencent, progress + "%")
                .setText(R.id.tv_cancelorinstall, progress != 100 ? mContext.getResources().getString(R.string.cancel)
                        : mContext.getResources().getString(R.string.install))
                .setText(R.id.tv_pencent, progress + "%")
                .setProgress(R.id.pb_noti_loaded, progress)
                .notifyNotification();
    }

    /**
     * 安装还是取消
     * @return
     * @param b
     */
    private PendingIntent getIntent(boolean b) {
        Intent intent = new Intent(mContext,CusReceiver.class);
        if(b) {
            intent.putExtra(CusReceiver.REC_TYPE,CusReceiver.INSTALL_SINAL);
            intent.putExtra(CusReceiver.REC_FILE,info.getFilePath() + File.separator + info.getFileName());
        }
        else
            intent.putExtra(CusReceiver.REC_TYPE,CusReceiver.CANCEL_SINAL);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,CODE,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }

    public void startDownLoad() {
        downLoadService.startDownLoad(info, this);
    }

    public static void cancelNotificaiton(){
        controller.cancelNotification(ID);
    }


    public OnServiceCallListener getServiceCallListener() {
        return serviceCallListener;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mContext.unregisterReceiver(receiver);
    }

    @Override
    public void onBindingDied(ComponentName name) {
    }

    /**
     * 下载完毕清空记录的下载长度
     */
    private void writeLenAfterFinish() {
        SharedPreferences.Editor editor = mContext.getSharedPreferences("congif", Context.MODE_PRIVATE).edit();
        editor.putLong("completedLength", 0L);
        editor.putLong("contentLength", 0L);
        editor.apply();
    }


}
