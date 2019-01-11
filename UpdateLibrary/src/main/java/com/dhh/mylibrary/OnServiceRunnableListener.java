package com.dhh.mylibrary;

public interface OnServiceRunnableListener {
    void onDownLoadStart();

    void onDownLoadFinish();

    void setProgress(int progress);
}
