package com.dhh.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

/**
 * Created by 79393 on 2018/12/15.
 */

public class UpdateFlame {

    private Context mContext;
    private TaskInfo info;
    private TaskInfoSetting setting;
    private SharedPreferences preferences;
    private int progress;
    private CusServiceConnection conn;
    private boolean bindService;
    private static UpdateFlame flame;

    public UpdateFlame(){
    }

    public void bind(Context context){
        mContext = context;
        preferences = mContext.getSharedPreferences("congif", Context.MODE_PRIVATE);
        BindService();
    }

    public UpdateFlame setTaskInfo(TaskInfoSetting setting){
        this.setting = setting;
        return flame;
    }

    public static UpdateFlame getInstance(){
        if(flame == null){
            synchronized (UpdateFlame.class){
                if(flame == null){
                    flame = new UpdateFlame();
                }
            }
        }
        return flame;
    }


    /**
     * 开启服务
     */
    private void  BindService(){
        info = getInfo();
        conn = new CusServiceConnection(mContext,info,updateListener);
        Intent intent = new Intent(mContext, DownLoadService.class);
        Log.e("bind","start");
//        if(isFirst){
            // 这里应该弄一个标志位判断
        bindService = mContext.bindService(intent,conn,Context.BIND_AUTO_CREATE);
//            isFirst = false;
//        }else {
//            Log.e("notice","please do not bind the service more than one");
//           // conn.startDownLoad();
//        }
    }

    public void startDownLoad(){
        conn.startDownLoad();
    }

    public void unBindService(){
        if(conn != null)
            mContext.unbindService(conn);
    }

    /**
     * 设置TaskInfo的一些内容
     */
    public static class TaskInfoSetting{
        private  String appName;
        private  String url;
        private  String filePath;
        private  String fileName;
        static final String defaultFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AutoUpdate";
        static final String defaultFileName = "update.apk";

        String getAppName() {
            return appName;
        }

        public TaskInfoSetting setAppName(String appName) {
            this.appName = appName;
            return this;
        }

        String getUrl() {
            return url;
        }

        public TaskInfoSetting setUrl(String url) {
            this.url = url;
            return this;
        }

        String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getFileName() {
            return fileName;
        }

        public TaskInfoSetting setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
    }

    public interface OnUpdateControllerListener{
        void getProgress(int progress);
    }

    /**
     * 监控sevice的进度，并且进行一些必要的操作
     */
     OnUpdateControllerListener updateListener = new OnUpdateControllerListener() {
        @Override
        public void getProgress(int progress) {
            //setProgress(progress);
        }
    };


    /**
     * @return 获取记录中的已完成长度
     */
    private Long getCompletedLen() {
        return preferences.getLong("completedLength", 0L);
    }

    /**
     * @return 获取记录中的文件整体长度
     */
    private Long getContentLen() {
        return preferences.getLong("contentLength", 0L);
    }

    /**
     * 中途暂停或者取消下载需要用到
     */
    private void wirteLen() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("completedLength", info.getComletedLength());
        editor.putLong("contentLength", info.getContentLen());
        editor.apply();
    }

    /**
     * 下载完毕清空记录的下载长度
     */
    private void writeLenAfterFinish() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("completedLength", 0L);
        editor.putLong("contentLength", 0L);
        editor.apply();
    }

    /**
     * 显示进度progressbar时有用
     * @return
     */
    public int getProgress() {
        return progress;
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
         android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * @return ServiceCallListener控制下载进度，可以暂停，可以继续
     */
    public OnServiceCallListener getController(){
        return conn.getServiceCallListener();
    }

    /**
     * @param setting 设置taskinfo
     */
    public void setSetting(TaskInfoSetting setting) {
        this.setting = setting;
    }

    /**
     * 根据
     * @return 用于下载
     */
    private TaskInfo getInfo() {
        TaskInfo info = new TaskInfo();
        if(setting == null)
            throw new NullPointerException("please set th setting");
        info.setApp_name(setting.getAppName() == null ?TaskInfoSetting.defaultFileName:setting.getAppName());
        info.setFileName(setting.getAppName() == null ?TaskInfoSetting.defaultFileName:setting.getAppName());
        info.setFilePath(setting.getAppName() == null ?TaskInfoSetting.defaultFilePath:setting.getFilePath());
        if(setting.getUrl() == null)
            throw new NullPointerException("please add th url");
        info.setDownLoadUrl(setting.getUrl());
        info.setComletedLength(getCompletedLen());
        info.setContentLen(getContentLen());
        info.setStop(false);
        return info;
    }

}
