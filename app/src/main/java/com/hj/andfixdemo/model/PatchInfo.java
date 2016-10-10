package com.hj.andfixdemo.model;

/**
 * Created by haojian12583 on 2016/9/30.
 */

public class PatchInfo {
    public String app_v;    //应用的版本
    public String path_v;   //补丁的版本
    public String url;  //补丁的下载地址

    public String getApp_v() {
        return app_v;
    }

    public String getPath_v() {
        return path_v;
    }

    public String getUrl() {
        return url;
    }

    public void setApp_v(String app_v) {
        this.app_v = app_v;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPath_v(String path_v) {
        this.path_v = path_v;
    }
}
