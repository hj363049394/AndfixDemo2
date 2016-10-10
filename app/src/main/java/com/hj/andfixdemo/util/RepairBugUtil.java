package com.hj.andfixdemo.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.hj.andfixdemo.application.MyApplication;
import com.hj.andfixdemo.model.PatchInfo;
import com.hj.andfixdemo.model.SPConst;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.io.IOException;

/**
 * Created by zhonghw on 2016/3/11.
 */
public class RepairBugUtil {

    private static final String TAG = "AndFix";
    private static final int THREAD_COUNT = 3;  //下载的线程数
    private ThinDownloadManager mDownloadManager;
    private LocalPreferencesHelper mLocalPreferencesHelper;

    public static RepairBugUtil instance = null;


    public static RepairBugUtil getInstance() {
       if (instance == null){
           instance = new RepairBugUtil();
       }
        return instance;
    }

    public void downloadAndLoad(Context context, final PatchInfo bean, String downloadUrl) {
        if (mLocalPreferencesHelper == null) {
            mLocalPreferencesHelper = new LocalPreferencesHelper(context, SPConst.SP_NAME);
        }
        Uri downloadUri = Uri.parse(downloadUrl);
        Uri destinationUri = Uri.parse(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + bean.url);
        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                .setDestinationURI(destinationUri)
                .setPriority(DownloadRequest.Priority.HIGH)
                .setDownloadListener(new DownloadStatusListener() {
                    @Override
                    public void onDownloadComplete(int id) {
                        // add patch at runtime
                        try {
                            // .apatch file path
                            String patchFileString = Environment.getExternalStorageDirectory()
                                    .getAbsolutePath() + bean.url;
                            MyApplication.mPatchManager.addPatch(patchFileString);
                            Log.d(TAG, "apatch:" + patchFileString + " added.");

                            //复制且加载补丁成功后，删除下载的补丁
                            File f = new File(patchFileString);
                            if (f.exists()) {
                                boolean result = new File(patchFileString).delete();
                                if (!result)
                                    Log.e(TAG, patchFileString + " delete fail");
                            }
//                            mLocalPreferencesHelper.saveOrUpdate(SPConst.IsHavePathDownLoad, false);
                        } catch (IOException e) {
                            Log.e(TAG, "", e);
                        } catch (Throwable throwable) {

                        }
                    }

                    @Override
                    public void onDownloadFailed(int id, int errorCode, String errorMessage) {
                        //下载失败的时候，标注标记位，等下次重新打开应用的时候重新下载
//                        mLocalPreferencesHelper.saveOrUpdate(SPConst.IsHavePathDownLoad, true);
                        Log.e(TAG, "onDownloadFailed");

                    }

                    @Override
                    public void onProgress(int id, long totalBytes, int progress) {
                        Log.e(TAG, "progress:" + progress);
                    }
                });
        mDownloadManager = new ThinDownloadManager(THREAD_COUNT);
        mDownloadManager.add(downloadRequest);
    }


    public void comparePath(Context context, PatchInfo RemoteBean) throws Exception {
        String pathInfo = mLocalPreferencesHelper.getString(SPConst.PATH_INFO);
        final PatchInfo localBean = GsonUtils.getInstance().parseIfNull(PatchInfo.class, pathInfo);
        //远程的应用版本跟当前应用的版本比较
        if (MyApplication.VERSION_NAME.equals(RemoteBean.app_v)) {
            //远程的应用版本跟本地保存的应用版本一样，但补丁不一样，则需要下载重新
            /**
             *第一种情况：当本地记录的Bean为空的时候（刚安装的时候可能为空）并且远程的Bean的path_v不为空的时候需要下载补丁。
             * 第二种情况：当本地记录的path_v和远程Bean的path_v不一样的时候需要下载补丁。
             */
            if (localBean == null && !TextUtils.isEmpty(RemoteBean.path_v)
                    || localBean.app_v.equals(RemoteBean.app_v) &&
                    !localBean.path_v.equals(RemoteBean.path_v)) {
                downloadAndLoad(context, RemoteBean,
                        SPConst.URL_PREFIX + RemoteBean.url);
                String json = GsonUtils.getInstance().parse(RemoteBean);
                mLocalPreferencesHelper.saveOrUpdate(SPConst.PATH_INFO, json);
            } /*else {
                mLocalPreferencesHelper.saveOrUpdate(SPConst.IsHavePathDownLoad, false);
            }*/
        }
    }

    public void release() {
        if (mDownloadManager != null) {
            mDownloadManager.release();
        }
    }

}
