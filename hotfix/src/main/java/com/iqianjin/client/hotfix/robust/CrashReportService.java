package com.iqianjin.client.hotfix.robust;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * author : iqianjin-liujiawei
 * e-mail : xxx@xx
 * time   : 2017/05/02
 * desc   : xxxx描述
 * version: 1.0
 */
public class CrashReportService extends IntentService {
    private String TAG = CrashReportService.class.getSimpleName();
    public static String paramMessage = "message";
    public static String paramUrl = "url";

    public CrashReportService() {
        super("crashReport");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public CrashReportService(String name) {
        super("crashReport");
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        try {
            if (intent != null && intent.getExtras() != null) {
                OkHttpClient client = new OkHttpClient();
                String json = intent.getExtras().getString(paramMessage);
                String url = intent.getExtras().getString(paramUrl);
                Log.i(TAG, "-------CrashReportService-------onHandleIntent" + paramUrl);
                Log.i(TAG, "提交的参数：" + json);
                if (json == null) {
                    return;
                }

                RequestBody body = RequestBody.create(JSON, json.replaceAll("'", "&"));
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (null != responseBody) {
                        Log.i(TAG, "服务器返回" + responseBody.string());
                    }
                    stopSelf();
                } else {
                    stopSelf();
                    Log.e(TAG, "提交crash 异常 .服务器错误" + response.code());
                    throw new IOException("Unexpected code " + response);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "提交crash 异常" + e);
        }
    }
}

