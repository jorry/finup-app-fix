package com.iqianjin.client.hotfix.robust.file.processSafeFile;

import android.util.Log;

import com.iqianjin.client.hotfix.robust.PatchManipulateImp;
import com.iqianjin.client.hotfix.robust.file.OkHttpUtils;

import java.io.File;

import okhttp3.OkHttpClient;

public class ProcessSafeDownloadFile
        extends ProcessSafeOperateAbstract<Boolean>
{
    private OkHttpClient okHttpClient;
    private String url;
    private File file;
    public ProcessSafeDownloadFile(OkHttpClient okHttpClient,File file,String url){
        this.okHttpClient = okHttpClient;
        this.url = url;
        this.file = file;
    }

    @Override
    protected Boolean operate() {
        try {
            return OkHttpUtils.simpleDownload(this.okHttpClient,url,file);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected String getLockPath() {
        File localFiile = new File(file.getAbsolutePath());
        Log.d(PatchManipulateImp.TAG,"补丁文件的本地路径是："+localFiile);
        return localFiile.getAbsolutePath();
    }
}