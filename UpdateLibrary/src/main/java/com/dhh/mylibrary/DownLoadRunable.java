package com.dhh.mylibrary;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by 79393 on 2018/12/15.
 */

public class DownLoadRunable implements Runnable {

    private String mURL; //"http://app.sj33333.com/ymtdc"
    private TaskInfo mInfo;
    private OnServiceRunnableListener mCall;

    DownLoadRunable(TaskInfo info, OnServiceRunnableListener call){
        mURL = info.getDownLoadUrl();
        mInfo = info;
        mCall = call;
    }

    @Override
    public void run() {
        URL url;
        BufferedInputStream bIS;
        RandomAccessFile raf;
        byte[] buff = new byte[1024 * 8];
        int length = 0;
        File fileCat = new File(mInfo.getFilePath());
        if(!fileCat.exists()){
            fileCat.mkdir();
        }
        File file = new File(mInfo.getFilePath() + File.separator + mInfo.getFileName());
        int lastProgress = 0;
        mCall.onDownLoadStart();
        try {
            raf = new RandomAccessFile(file , "rwd");
            url = new URL(mURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(240000);//连接超时时间
            urlConnection.setReadTimeout(240000);//读取超时时间
            urlConnection.setRequestMethod("GET");//请求类型为GET
            // volatile 修饰后 不能用getComletedLength()作为条件
            if(mInfo.getContentLen() != 0){
                // 寻找位置
                urlConnection.setRequestProperty("Range", "bytes=" + mInfo.getComletedLength() + "-" + mInfo.getContentLen());
            }
            else {
                // 一开始记录文件长度
                mInfo.setContentLen(Long.valueOf(urlConnection.getHeaderField("content-length")));
                mInfo.setComletedLength(0l);
            }
            lastProgress = (int) ((mInfo.getComletedLength()  * 100) / mInfo.getContentLen());
            mCall.setProgress(lastProgress);
            urlConnection.connect();
            bIS = new BufferedInputStream(urlConnection.getInputStream());
            // 移动 RandomAccessFile
            raf.seek(mInfo.getComletedLength());
            while (!mInfo.getStop() && (length = bIS.read(buff)) != -1){
                raf.write(buff,0,length);
                mInfo.setComletedLength(mInfo.getComletedLength() + length);
                int progress = (int) ((mInfo.getComletedLength()  * 100) / mInfo.getContentLen());
                if(progress > lastProgress){
                    mCall.setProgress(progress);
                    lastProgress = progress;
                }
            }
            if(length == -1){
                mCall.onDownLoadFinish();
            }
            bIS.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
