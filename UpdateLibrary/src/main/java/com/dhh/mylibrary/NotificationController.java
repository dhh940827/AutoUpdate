package com.dhh.mylibrary;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class NotificationController {

    private static Context mContext;
    private static int layoutId;
    private static NotificationController controller;
    private static int notificationId = 0;
    private Long lastTime = 0l;
    private NotificationManager mManager;
    private RemoteViews mViews;
    private Notification notification;
    private String CHANNELID = "update";
    public static final int NOTI_INSTALL = 123;
    public static final int NOTI_CANCEL = 234;

    private NotificationController() {
        mViews = new RemoteViews(mContext.getPackageName(), layoutId);
        mManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        readyNotification();
    }

    /**
     * 构造notification
     *
     */
    private void readyNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(mContext, CHANNELID);
        else
            builder = new Notification.Builder(mContext);
//        Intent intent = new Intent(mContext,CusReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,NOTI_REC,intent,FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setAutoCancel(true);
        mViews.setImageViewResource(R.id.iv_appicon, R.drawable.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setCustomContentView(mViews);
        else
            builder.setContent(mViews);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        } else
            notification = builder.getNotification();
    }

    public NotificationController setViewOnClick(int viewId,PendingIntent intent){
        mViews.setOnClickPendingIntent(viewId,intent);
        return controller;
    }

    public NotificationController setText(int textId,CharSequence charSequence){
        mViews.setTextViewText(textId,charSequence);
        return controller;
    }

    public NotificationController setCancelListener(int cancel_id){
        Intent intent = new Intent(mContext,CusReceiver.class);
        intent.putExtra("type","cancel");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,NOTI_CANCEL,intent,FLAG_UPDATE_CURRENT);
        mViews.setOnClickPendingIntent(cancel_id,pendingIntent);
        return controller;
    }

    public NotificationController setInstallListener(int install_id){
        Intent intent = new Intent(mContext,CusReceiver.class);
        intent.putExtra("type","install");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,NOTI_INSTALL,intent,FLAG_UPDATE_CURRENT);
        mViews.setOnClickPendingIntent(install_id,pendingIntent);
        return controller;
    }

    public NotificationController setImageViewResource(int viewId,int resourceId){
        mViews.setImageViewResource(viewId,resourceId);
        return controller;
    }

    public NotificationController setProgress(int progressId,int progress){
        mViews.setProgressBar(progressId,100,progress,false);
        return controller;
    }

    public void notifyNotification(){

        mManager.notify(notificationId,notification);
    }

    public void cancelNotification(int id){
        mManager.cancel(id);
    }

    /**
     * 8.0以上系统需要构建channel
     */
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
            mManager.createNotificationChannel(channel);
        }
    }

    public static class Builder {

        public Builder setContext(Context context) {
            mContext = context;
            return this;
        }

        public Builder setLayout(int layot) {
            layoutId = layot;
            return this;
        }

        public Builder setNotificationId(int id){
            notificationId = id;
            return this;
        }

        public NotificationController build() {
            if(controller == null){
                controller = new NotificationController();
            }
            return controller;
        }
    }
}
