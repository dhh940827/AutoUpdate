package com.dhh.mylibrary;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;

import java.io.File;
import java.nio.channels.Channel;

/**
 * Created by 79393 on 2018/12/15.
 */

public class CusServiceConnection implements ServiceConnection {

    private Context mContext;
    private String url;
    private TaskInfo info;
    private ProgressBarDialog dialog;
    private DownLoadService downLoadService;
    private SharedPreferences preferences;
    private RemoteViews views;
    private NotificationManager nm;
    private static final String CHANNELID = "update";
    private static final int ID = 1;

    public interface OnClickListener {
        void stop();
        void goAgain();
        void gottoback();
        void cancel();
    }

    public interface OnServiceCall{
        void onDownLoadStart();
        void onDownLoadFinish();
        void setProgress(int progress);
    }

    public CusServiceConnection(Context context, String url,TaskInfo info){
        mContext = context;
        preferences = mContext.getSharedPreferences("congif",Context.MODE_PRIVATE);
        nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        views = new RemoteViews(mContext.getPackageName(),R.layout.remoteview_layout);
        this.url = url;
        this.info = info;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        downLoadService = ((DownLoadService.MsgBinder)service).getService();
        info.setComletedLength(getCompletedLen());
        info.setContentLen(0l);
        info.setStop(false);
        showProgressBar();
        downLoadService.startDownLoad(url,info,new OnServiceCall(){

            @Override
            public void onDownLoadStart() {
                // 显示通知在导航栏
                showNotification();
            }

            @Override
            public void onDownLoadFinish() {
                dimissDialog();
                // 自动安装
                autoInstallApk();
            }

            @Override
            public void setProgress(final int progress) {
                // 必须确保在主线程运行
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.setProgressBarProgress(progress);
                    }
                });
            }
        });
    }

    /**
     * 展示通知
     */
    private void showNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(mContext,CHANNELID);
        else
            builder = new Notification.Builder(mContext);
        views.setTextViewText(R.id.tv_appname,info.getApp_name());
        views.setTextViewText(R.id.tv_isfinished,mContext.getResources().getString(R.string.is_loading));
        views.setTextViewText(R.id.tv_pencent,mContext.getResources().getString(R.string.zero_percent));
        views.setTextViewText(R.id.tv_cancelorinstall,mContext.getResources().getString(R.string.cancel));
        views.setProgressBar(R.id.pb_noti_loaded,100,0,true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setCustomContentView(views);
        else
            builder.setContent(views);
        builder.setAutoCancel(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            nm.notify(ID,builder.build());
        }
        else
            nm.notify(ID,builder.getNotification());
    }


    /**
     * auto install the apk
     */
    private void autoInstallApk() {
        //apk文件的本地路径
        File apkfile = new File(info.getFilePath() + File.separator + info.getFileName() );
        //会根据用户的数据类型打开android系统相应的Activity。
        Uri uri = FileProvider.getUriForFile(mContext,mContext.getPackageName() + ".auto.provider",apkfile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //设置intent的数据类型是应用程序application
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        //为这个新apk开启一个新的activity栈
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // !!!!这句话是重点
        //开始安装
        mContext.startActivity(intent);
        //关闭旧版本的应用程序的进程
       // android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onBindingDied(ComponentName name) {

    }

    private void showProgressBar(){
        dialog = new ProgressBarDialog();
        dialog.setListen(new OnClickListener() {
            @Override
            public void stop() {
                info.setStop(true);
            }

            @Override
            public void goAgain() {
                info.setStop(false);
                downLoadService.goAgain();
            }

            @Override
            public void gottoback() {

            }

            @Override
            public void cancel() {
                // 记录info的completedLength
                info.setStop(true);
                wirteCompletedLen();
                dimissDialog();
            }
        });
        dialog.show(((Activity)mContext).getFragmentManager(),"progressbar");
    }

    private void dimissDialog(){
        if(dialog != null){
            dialog.dismiss();
        }
    }

    private void wirteCompletedLen(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("completedLength",info.getComletedLength());
        editor.commit();
    }

    private Long getCompletedLen(){
        return preferences.getLong("completedLength",0l);
    }

}
