package com.dhh.autoupdateframe;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import com.dhh.mylibrary.ProgressBarDialog;
import com.dhh.mylibrary.UpdateFlame;

import java.io.File;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    private UpdateFlame mFlame;
    private RemoteViews views;
    private Context mContext;
    private NotificationManager nm;
    private static final int ID = 1;
    private String CHANNEL_ID = "";
    private Notification notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button)findViewById(R.id.bt_start);
        Button button1 = (Button)findViewById(R.id.bt_install);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoInstallApk();
            }
        });
        mFlame = new UpdateFlame(this,"https://https-serve.b0.upaiyun.com/uploads/apps/345/yimentong_30_v2.9.30_181102110714.apk");
        mFlame.setFileName("yimentong.apk");
        mFlame.setAppName("测试demo");
        mFlame.setAppIcon(R.mipmap.ic_launcher);
        mFlame.setFilePath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AutoUpdate");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},123);

                }
                else {
                    startService();
                }

            }
        });
        Button button2 = findViewById(R.id.bt_showprogress);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressBarDialog dialog = new ProgressBarDialog();
                dialog.show(MainActivity.this.getFragmentManager(),"progress");
            }
        });
        mContext = this;
        views = new RemoteViews(mContext.getPackageName(), com.dhh.mylibrary.R.layout.remoteview_layout);
        nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        CHANNEL_ID = getPackageName() + ".update";
        createNotificationChannel();
        readyNotification();
        Button button3 = findViewById(R.id.bt_showNotification);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotification();
                new Thread(runnable).start();
            }
        });

    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            showNotification(msg.what);
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int rate = 0;
            while (rate < 100){
                handler.sendEmptyMessage(rate);
                rate = rate + 2;
            }
        }
    };

    /**
     * 更新通知状态
     * @param progress
     */
    private void showNotification(int progress) {
        if(progress != 100){
            views.setTextViewText(com.dhh.mylibrary.R.id.tv_isfinished,mContext.getResources().getString(com.dhh.mylibrary.R.string.is_loading));
            views.setTextViewText(com.dhh.mylibrary.R.id.tv_pencent,progress + "%");
            views.setTextViewText(com.dhh.mylibrary.R.id.tv_cancelorinstall,mContext.getResources().getString(com.dhh.mylibrary.R.string.cancel));
        }
        else {
            views.setTextViewText(com.dhh.mylibrary.R.id.tv_isfinished,mContext.getResources().getString(com.dhh.mylibrary.R.string.is_finished));
            views.setTextViewText(com.dhh.mylibrary.R.id.tv_pencent,progress + "%");
            views.setTextViewText(com.dhh.mylibrary.R.id.tv_cancelorinstall,mContext.getResources().getString(com.dhh.mylibrary.R.string.install));
        }
        views.setProgressBar(com.dhh.mylibrary.R.id.pb_noti_loaded,100,progress,false);
        nm.notify(ID, notification);
    }

    /**
     * 构造notification
     * @return
     */
    private void readyNotification(){
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(mContext,CHANNEL_ID);
        else
            builder = new Notification.Builder(mContext);
        builder.setSmallIcon(com.dhh.mylibrary.R.drawable.ic_launcher);
        builder.setAutoCancel(true);
        views.setImageViewResource(com.dhh.mylibrary.R.id.iv_appicon, com.dhh.mylibrary.R.drawable.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setCustomContentView(views);
        else
            builder.setContent(views);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification =  builder.build();
        }
        else
            notification = builder.getNotification();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "update";
            String description = "update app";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 展示通知
     */
    private void showNotification() {
        views.setTextViewText(com.dhh.mylibrary.R.id.tv_appname,"appdemo");
        views.setTextViewText(com.dhh.mylibrary.R.id.tv_isfinished,mContext.getResources().getString(com.dhh.mylibrary.R.string.is_loading));
        views.setTextViewText(com.dhh.mylibrary.R.id.tv_pencent,mContext.getResources().getString(com.dhh.mylibrary.R.string.zero_percent));
        views.setTextViewText(com.dhh.mylibrary.R.id.tv_cancelorinstall,mContext.getResources().getString(com.dhh.mylibrary.R.string.cancel));
        views.setProgressBar(com.dhh.mylibrary.R.id.pb_noti_loaded,100,0,false);
        nm.notify(ID, notification);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 123 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startService();
        }
        if(requestCode == 234 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            installApk();
        }
    }

    private void startService(){
        Log.e("start service","yes");
        try {
            mFlame.BindService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * auto install the apk
     */
    private void autoInstallApk() {
        installApk();
    }

    private void installApk(){
        //apk文件的本地路径
        File apkfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AutoUpdate" + File.separator + "ymtdc.apk" );
        //会根据用户的数据类型打开android系统相应的Activity。
        Log.e("apkfile",apkfile.getAbsolutePath());
        Uri uri = FileProvider.getUriForFile(this,getPackageName() + ".auto.provider",apkfile);
        Log.e("uri",uri.toString());
        Log.e("uri",uri.getEncodedPath());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //设置intent的数据类型是应用程序application
        intent.setDataAndType(uri,"application/vnd.android.package-archive");
        //为这个新apk开启一个新的activity栈
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // !!!!这句话是重点
        //开始安装
        startActivity(intent);
        //关闭旧版本的应用程序的进程
        //  android.os.Process.killProcess(android.os.Process.myPid());
    }
}
