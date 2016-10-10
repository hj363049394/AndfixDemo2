package com.hj.andfixdemo.application;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.alipay.euler.andfix.patch.PatchManager;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by haojian12583 on 2016/9/29.
 */

public class MyApplication extends Application {

    private static final String TAG = "AndfixDemo";
    public static String VERSION_NAME = "";
    public static PatchManager mPatchManager;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            PackageInfo mPackageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);     		// 初始化 JPush
        initAndfix();
    }

    private void initAndfix(){
        mPatchManager = new PatchManager(this);
        mPatchManager.init(VERSION_NAME);
        Log.d(TAG,"Andfix inited!");
        // load patch
        mPatchManager.loadPatch();
        Log.d(TAG,"Andfix loaded!");
    }

}
