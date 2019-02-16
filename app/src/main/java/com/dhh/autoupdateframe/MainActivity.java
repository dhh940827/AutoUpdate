package com.dhh.autoupdateframe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.dhh.mylibrary.UpdateFlame;

public class MainActivity extends AppCompatActivity {

    private UpdateFlame mFlame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // UpdateFlame.getInstance.start();
        Button button = (Button)findViewById(R.id.bt_start);
        UpdateFlame.getInstance()
                .setTaskInfo(new UpdateFlame.TaskInfoSetting()
                        .setFileName("yimentong.apk")
                        .setUrl("https://https-serve.b0.upaiyun.com/uploads/apps/345/yimentong_30_v2.9.30_181102110714.apk"))
                .bind(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},123);

                }
                else {
                    startDownLoad();
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 123 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startDownLoad();
        }
    }

    private void startDownLoad(){
        try {
            UpdateFlame.getInstance().startDownLoad();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        UpdateFlame.getInstance().unBindService();
    }
}
