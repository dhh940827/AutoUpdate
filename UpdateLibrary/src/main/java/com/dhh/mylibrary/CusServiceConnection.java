package com.dhh.mylibrary;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private String CHANNELID = "update";
    private static final int ID = 1;
    private OnCancelListener mListener;
    private Boolean isShowNoti = false;
    private Notification notification;
    private volatile int pro = 0;
    Lock lock = new ReentrantLock();

    public interface OnCancelListener {
        void oncancal();
    }

    public interface OnClickListener {
        void stop();

        void goAgain();

        void gottoback();

        void cancel();
    }

    public interface OnServiceCall {
        void onDownLoadStart();

        void onDownLoadFinish();

        void setProgress(int progress);
    }

    public CusServiceConnection(Context context, String url, TaskInfo info, OnCancelListener listener) {
        mContext = context;
        preferences = mContext.getSharedPreferences("congif", Context.MODE_PRIVATE);
        nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        views = new RemoteViews(mContext.getPackageName(), R.layout.remoteview_layout);
        readyNotification();
        createNotificationChannel();
        this.url = url;
        this.info = info;
        this.mListener = listener;
    }

    @Override
    public void onServiceConnected(final ComponentName name, IBinder service) {
        downLoadService = ((DownLoadService.MsgBinder) service).getService();
        info.setComletedLength(getCompletedLen());
        info.setContentLen(getContentLen());
        info.setStop(false);
      //  showProgressBar();
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
        downLoadService.startDownLoad(url, info, new OnServiceCall() {

            @Override
            public void onDownLoadStart() {
                // 显示通知在导航栏
                isShowNoti = true;
                showNotification();
            }

            @Override
            public void onDownLoadFinish() {
               // dimissDialog();
                // 自动安装
                info.setStop(true);
                writeLenAfterFinish();
                autoInstallApk();
            }

            @Override
            public void setProgress(int progress) {
//                ((Activity) mContext).runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        dialog.setProgressBarProgress(progress);
//                    }
//                });
                Log.e("回调progress",progress + "");
                pro = progress;
                handler.sendEmptyMessage(1);
            }
        });
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1){
                if(!info.getStop())
                    showNotification(pro);
                else
                    showNotification(100);
            }
        }
    };

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "update";
            String description = "update app";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNELID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(false);
            channel.setSound(null, null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            nm.createNotificationChannel(channel);
        }
    }

    /**
     * 更新通知状态
     *
     * @param progress
     */
    private  void showNotification(int progress) {
        Log.e("progress",progress + "");
        if (progress != 100) {
            views.setTextViewText(R.id.tv_isfinished, mContext.getResources().getString(R.string.is_loading));
            views.setTextViewText(R.id.tv_pencent, progress + "%");
            views.setTextViewText(R.id.tv_cancelorinstall, mContext.getResources().getString(R.string.cancel));
        } else {
            views.setTextViewText(R.id.tv_isfinished, mContext.getResources().getString(R.string.is_finished));
            views.setTextViewText(R.id.tv_pencent, progress + "%");
            views.setTextViewText(R.id.tv_cancelorinstall, mContext.getResources().getString(R.string.install));
        }
        views.setProgressBar(R.id.pb_noti_loaded, 100, progress, false);
        nm.notify(ID, notification);
    }

    /**
     * 构造notification
     *
     * @return
     */
    private void readyNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(mContext, CHANNELID);
        else
            builder = new Notification.Builder(mContext);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setAutoCancel(false);
        views.setImageViewResource(R.id.iv_appicon, R.drawable.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setCustomContentView(views);
        else
            builder.setContent(views);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        } else
            notification = builder.getNotification();
    }

    /**
     * 展示通知
     */
    private void showNotification() {
        views.setTextViewText(R.id.tv_appname, info.getApp_name());
        views.setTextViewText(R.id.tv_isfinished, mContext.getResources().getString(R.string.is_loading));
        views.setTextViewText(R.id.tv_pencent, mContext.getResources().getString(R.string.zero_percent));
        views.setTextViewText(R.id.tv_cancelorinstall, mContext.getResources().getString(R.string.cancel));
        views.setProgressBar(R.id.pb_noti_loaded, 100, 0, false);
        nm.notify(ID, notification);
    }


    /**
     * auto install the apk
     */
    private void autoInstallApk() {
        //apk文件的本地路径
        File apkfile = new File(info.getFilePath() + File.separator + info.getFileName());
        //会根据用户的数据类型打开android系统相应的Activity。
        Uri uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".auto.provider", apkfile);
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

    /**
     * 展示进度dialog
     */
    private void showProgressBar() {
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
                wirteLen();
                if (isShowNoti) {
                    isShowNoti = false;
                    nm.cancel(ID);
                }
            }
        });
        dialog.show(((Activity) mContext).getFragmentManager(), "progressbar");
    }

    private void dimissDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void wirteLen() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("completedLength", info.getComletedLength());
        editor.putLong("contentLength", info.getContentLen());
        editor.commit();
    }

    private void writeLenAfterFinish() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("completedLength", 0l);
        editor.putLong("contentLength", 0l);
        editor.commit();
    }

    private Long getCompletedLen() {
        return preferences.getLong("completedLength", 0l);
    }

    private Long getContentLen() {
        return preferences.getLong("contentLength", 0l);
    }

}
