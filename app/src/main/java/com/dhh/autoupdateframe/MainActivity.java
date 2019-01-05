package com.dhh.autoupdateframe;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dhh.mylibrary.ProgressBarDialog;
import com.dhh.mylibrary.UpdateFlame;

import java.io.File;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    private UpdateFlame mFlame;

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
        mFlame = new UpdateFlame(this,"https://https-serve.b0.upaiyun.com/uploads/apps/380/ymtdc_1_v1.0.0_181219115850.apk");
        mFlame.setFileName("ymtdc.apk");
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
//        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.REQUEST_INSTALL_PACKAGES) !=
//                PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES},234);
//
//        }
//        else {
            installApk();
     //   }

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
