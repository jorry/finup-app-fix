package com.iqianjin.client.hotfix.robust;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.meituan.robust.Patch;
import com.meituan.robust.RobustApkHashUtils;
import com.meituan.robust.RobustCallBack;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RobustCallBackImp implements RobustCallBack {
    private ConfigBinder configBinder;

    public static class ConfigBinder {
        private String pid;
        private String channel;
        private String appVersion;

        public String getPid() {
            return pid;
        }

        public ConfigBinder setPid(String pid) {
            this.pid = pid;
            return this;
        }

        public String getChannel() {
            return channel;
        }

        public ConfigBinder setChannel(String channel) {
            this.channel = channel;
            return this;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public ConfigBinder setAppVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }
    }

    private Map<String, Object> commonInfoMap = new HashMap(10);
    private Context context;

    public RobustCallBackImp(Context context) {
        this.context = context;
        initCommonInfoMap(context);
    }

    private void initCommonInfoMap(Context context) {
        commonInfoMap.put("pid", configBinder != null ? configBinder.getPid() : "");
        commonInfoMap.put("appHash", RobustApkHashUtils.readRobustApkHash(context));
        commonInfoMap.put("us", configBinder != null ? configBinder.getChannel() : "");
        commonInfoMap.put("osVersion", Build.VERSION.RELEASE);
        commonInfoMap.put("appVersion", configBinder != null ? configBinder.getAppVersion() : "");
        commonInfoMap.put("phoneModel", Build.MODEL);
    }

    /**
     * * 获取补丁列表后，回调此方法
     *
     * @param result
     * @param isNet
     * @param patches
     */
    @Override
    public void onPatchListFetched(boolean result, boolean isNet, List<Patch> patches) {
        Log.i("RobustCallBack", "onPatchListFetched result: " + result);
    }

    /**
     * 在获取补丁后，回调此方法
     *
     * @param result
     * @param isNet
     * @param patch
     */

    @Override
    public void onPatchFetched(boolean result, boolean isNet, Patch patch) {
        Log.i("RobustCallBack", "onPatchFetched result: " + result);
        Log.i("RobustCallBack", "onPatchFetched isNet: " + isNet);
        Log.i("RobustCallBack", "onPatchFetched patch: " + patch.getName());
    }

    /**
     * 在补丁应用后，回调此方法
     *
     * @param result
     * @param patch
     */
    @Override
    public void onPatchApplied(boolean result, Patch patch) {
        RobustCrashHandler.setClosingTime();
        if (result) {
            Log.d(PatchManipulateImp.TAG, "修复成功");
        } else {
            Log.d(PatchManipulateImp.TAG, "修复失败");
        }

        String message = result ? String.valueOf(1) : String.valueOf(0);
        HashMap localHashMap = new HashMap();
        localHashMap.put("key", "patch_apply");
        localHashMap.put("value", message);
        localHashMap.put("where", "onPatchApplied");
        localHashMap.put("patchId", patch.getName());
        localHashMap.put("patchMd5", patch.getMd5());
        localHashMap.put("currentTimeMillis", System.currentTimeMillis());
        localHashMap.put("appHash", patch.getAppHash());

        report(localHashMap);
        Log.i("RobustCallBack", "onPatchApplied result: " + result);
        Log.i("RobustCallBack", "onPatchApplied patch: " + patch.getName());


    }

    @Override
    public void logNotify(String log, String where) {
        Log.i("RobustCallBack", "logNotify log: " + log);
        Log.i("RobustCallBack", "logNotify where: " + where);
    }

    @Override
    public void exceptionNotify(Throwable throwable, String where) {
        Log.e("RobustCallBack", "exceptionNotify where: " + where, throwable);
        String message = getStackTraceString(throwable);
        HashMap localHashMap = new HashMap();
        localHashMap.put("key", "exception");
        localHashMap.put("value", message);
        localHashMap.put("where", where);
        localHashMap.put("patchId", "");
        localHashMap.put("patchMd5", "");
        report(localHashMap);
    }

    private void report(Map<String, Object> messageMap) {
        this.commonInfoMap.putAll(messageMap);
        String json = mapToJson(this.commonInfoMap);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(CrashReportService.paramUrl, getReportLogServer());
        bundle.putString(CrashReportService.paramMessage, json);
        intent.putExtras(bundle);
        intent.setClass(context, CrashReportService.class);
        context.startService(intent);
    }

    private String getReportLogServer() {
        if (!BuildConfig.isBuildTypeRealse) {
            return "http://10.10.223.41:3001/patchLog_v1";
        } else {
            return "http://p1.iqianjin.com/patchLog_v1/";

        }
    }

    protected String mapToJson(Map<String, Object> map) {
        String jsonObject = com.alibaba.fastjson.JSON.toJSONString(map);
        return jsonObject;
    }

    String getStackTraceString(Throwable paramThrowable) {
        if (paramThrowable == null) {
            return "";
        }
        for (Throwable localThrowable = paramThrowable; localThrowable != null; localThrowable = localThrowable.getCause()) {
            if ((localThrowable instanceof UnknownHostException)) {
                return "";
            }
        }
        StringWriter localStringWriter = new StringWriter();
        PrintWriter localPrintWriter = new PrintWriter(localStringWriter);
        paramThrowable.printStackTrace(localPrintWriter);
        localPrintWriter.flush();
        return localStringWriter.toString();
    }

    public void setConfigBinder(ConfigBinder configBinder) {
        this.configBinder = configBinder;
    }
}