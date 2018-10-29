package com.iqianjin.client.hotfix.robust.file;

import com.iqianjin.client.hotfix.robust.file.processSafeFile.ProcessSafeDeleteFile;
import com.iqianjin.client.hotfix.robust.file.processSafeFile.ProcessSafeDownloadFile;
import com.iqianjin.client.hotfix.robust.file.processSafeFile.ProcessSafeReadFile;
import com.iqianjin.client.hotfix.robust.file.processSafeFile.ProcessSafeWriteFile;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;

/**
 * Created by iqianjin-liujiawei on 18/9/3.
 */

public class ProcessSafeOperateUtils {

    public static boolean deletePatchSafe(File file)
            throws IOException {
        return ((Boolean) new ProcessSafeDeleteFile(file).perform()).booleanValue();
    }

    private static String readFileSafe(File paramFile)
            throws IOException {
        return (String) new ProcessSafeReadFile(paramFile).perform();
    }

    public static String readPatchListLocal(File paramFile)
            throws IOException {
        return readFileSafe(paramFile);
    }

    private static boolean writeFileSafe(File file, String strData)
            throws IOException {
        return ((Boolean) new ProcessSafeWriteFile(file, strData).perform()).booleanValue();
    }

    public static boolean writePatchListLocal(File paramFile,  String strData)
            throws IOException {
        return writeFileSafe(paramFile, strData);
    }

    public static boolean downLoadPatchSafe(OkHttpClient okHttpClient, String url, File file) {
        return new ProcessSafeDownloadFile(okHttpClient,file,url).perform().booleanValue();
    }
}
