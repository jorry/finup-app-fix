package com.iqianjin.client.hotfix.robust.file;

import android.content.Context;
import android.util.Log;


import com.iqianjin.client.hotfix.robust.PatchManipulateImp;
import com.iqianjin.client.hotfix.robust.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpUtils {

    /**
     * 下载补丁列表
     *
     * @param client
     * @param url
     * @return
     * @throws IOException
     */
    public static String simpleGet(OkHttpClient client, String url)
            throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        Log.d(PatchManipulateImp.TAG, "url = " + url);
        Log.d(PatchManipulateImp.TAG, "http CallBack response.isSuccessful = " + response.isSuccessful());

        if (response.isSuccessful()) {
            String res = response.body().toString();
            Log.d(PatchManipulateImp.TAG, "http CallBack message" + res);
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    /**
     * 下载 补丁
     *
     * @param okHttpClient
     * @param url
     * @param file
     * @return
     * @throws Exception
     */
    public static boolean simpleDownload(OkHttpClient okHttpClient, final String url, final File file) throws Exception {
        if (file.exists()) {
            return Boolean.valueOf(true);
        }
        Log.d(PatchManipulateImp.TAG, "补丁包存储地址：" + file + " 下载URL = " + url);
        final Request request = new Request.Builder().url(url).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            InputStream is = null;
            byte[] buf = new byte[2048];
            int len = 0;
            FileOutputStream fos = null;
            try {
                long total = response.body().contentLength();
                Log.d(PatchManipulateImp.TAG, "total------>" + total);
                long current = 0;
                is = response.body().byteStream();
                fos = new FileOutputStream(file);
                while ((len = is.read(buf)) != -1) {
                    current += len;
                    fos.write(buf, 0, len);
                    Log.d(PatchManipulateImp.TAG, "current------>" + current);
                }
                fos.flush();
                return true;
            } catch (IOException e) {
                Log.d(PatchManipulateImp.TAG, e.toString());
                Log.d(PatchManipulateImp.TAG, url + "下载失败------>文件地址：" + file.getAbsolutePath());
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    Log.d(PatchManipulateImp.TAG, e.toString());
                }
            }
        }
        return false;
    }


    public static boolean isNetworkConnected(Context context) {
        return Util.isAvailable(context);
    }
}
