package com.iqianjin.client.hotfix.robust.file.processSafeFile;

import android.util.Log;

import com.iqianjin.client.hotfix.robust.PatchManipulateImp;

import java.io.File;

/**
 * Created by iqianjin-liujiawei on 18/9/3.
 */

public class ProcessSafeDeleteFile extends ProcessSafeOperateAbstract<Boolean> {
    private File file;

    public ProcessSafeDeleteFile(File file) {
        this.file = file;
    }

    @Override
    protected Boolean operate() {
        if (file.exists()) {
            boolean deleted = file.delete();
            Log.d(PatchManipulateImp.TAG, "删除文件  文件名:" + file.getName() + " 是否删除 : " + deleted);
            return Boolean.valueOf(deleted);
        }
        return Boolean.valueOf(true);
    }

    @Override
    protected String getLockPath() {
        Log.d(PatchManipulateImp.TAG, ProcessSafeDeleteFile.class.getSimpleName() + "   getLockPath = " + file.getAbsolutePath());
        return file.getAbsolutePath();
    }
}
