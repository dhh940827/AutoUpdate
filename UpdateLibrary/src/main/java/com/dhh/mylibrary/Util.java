package com.dhh.mylibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by 79393 on 2018/12/15.
 */

public class Util {
    /**
     * auto install the apk
     */
    public static void autoInstallApk(Context context,String file) {
        //apk文件的本地路径
        File apkfile = new File(file);
        //会根据用户的数据类型打开android系统相应的Activity。
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".auto.provider", apkfile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //设置intent的数据类型是应用程序application
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        //为这个新apk开启一个新的activity栈
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // !!!!这句话是重点
        //开始安装
        context.startActivity(intent);
        //关闭旧版本的应用程序的进程
        //android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static void collapseStatusBar(Context context) {
        @SuppressLint("WrongConstant")
        Object service = context.getSystemService("statusbar");
        if (null == service)
            return;
        try {
            Class<?> clazz = Class.forName("android.app.StatusBarManager");
            int sdkVersion = android.os.Build.VERSION.SDK_INT;
            Method collapse = null;
            if (sdkVersion <= 16) {
                collapse = clazz.getMethod("collapse");
            } else {
                collapse = clazz.getMethod("collapsePanels");
            }
            collapse.setAccessible(true);
            collapse.invoke(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
