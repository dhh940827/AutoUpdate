package com.dhh.mylibrary;

/**
 * Created by 79393 on 2018/12/15.
 */

public class TaskInfo {

    private volatile Long comletedLength; // 已经完成的文件长度
    private Long contentLen;
    private String fileName;
    private String filePath;
    private String downLoadUrl;
    private String app_name;
    private volatile Boolean isStop;

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public Boolean getStop() {
        return isStop;
    }

    public void setStop(Boolean stop) {
        isStop = stop;
    }

    public Long getContentLen() {
        return contentLen;
    }

    public void setContentLen(Long contentLen) {
        this.contentLen = contentLen;
    }

    public Long getComletedLength() {
        return comletedLength;
    }

    public void setComletedLength(Long comletedLength) {
        this.comletedLength = comletedLength;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDownLoadUrl() {
        return downLoadUrl;
    }

    public void setDownLoadUrl(String downLoadUrl) {
        this.downLoadUrl = downLoadUrl;
    }
}
