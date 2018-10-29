package com.iqianjin.client.hotfix.robust;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iqianjin.client.hotfix.robust.file.OkHttpUtils;
import com.meituan.robust.Patch;
import com.meituan.robust.PatchManipulate;
import com.meituan.robust.RobustApkHashUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.OkHttpClient;

/**
 * We recommend you rewrite your own PatchManipulate class ,adding      your special patch Strategy，in the demo we just load the patch directly
 * Pay attention to the difference of patch's LocalPath and patch's    TempPath
 * We recommend LocalPath store the origin patch.jar which may be encrypted,while TempPath is the true runnable jar
 * 我们推荐继承PatchManipulate实现你们App独特的A补丁加载策略，其中setLocalPath设置补丁的原始路径，这个路径存储的补丁是加密过得，setTempPath存储解密之后的补丁，是可以执行的jar文件
 * setTempPath设置的补丁加载完毕即刻删除，如果不需要加密和解密补丁，两者没有啥区别
 */

public class PatchManipulateImp extends PatchManipulate {
    public static String TAG = "robust";
    static RobustCallBackImp realCallBack;
    //补丁策略
    private ArrayList<Patch> patches;
    private static final String processName = "com.iqiajin.client";
    private String robustApkHash;
    private OkHttpClient okHttpClient;
    private String appVersion;
    private String us;
    public PatchManipulateImp() {

    }

    public PatchManipulateImp(Context context, RobustCallBackImp realCallBack, String appVersion, String us) {
        this.realCallBack = realCallBack;
//        appHash = robustApkHash = "6864aba2dd5972e5dba9d031e6bbb307";
        robustApkHash = RobustApkHashUtils.readRobustApkHash(context);
        this.okHttpClient = new OkHttpClient();
        this.appVersion = appVersion;
        this.us = us;
        initCrashHandler(context);
        Log.i(TAG, "robustApkHash = " + robustApkHash);
    }

    /**
     * debug 模式 没有appHash.
     * 如果要调试需要手动设置一个 appHash
     * @param appHash
     */
    public void setAppHash(String appHash) {
        if (!BuildConfig.isBuildTypeRealse) {
            this.robustApkHash = appHash;
        }
    }

    private void initCrashHandler(Context paramContext) {
        new RobustCrashHandler(paramContext, realCallBack).setDefaultUncaughtExceptionHandlerSelf();
    }

    private String getUrl() {
        StringBuffer stringBuffer = new StringBuffer(getIp());
        stringBuffer.append("appHash=").append(robustApkHash).append("&");
        stringBuffer.append("appVersion=").append(appVersion).append("&");
        stringBuffer.append("us=").append(us);
        Log.d(TAG, "补丁列表接口 " + stringBuffer.toString());
        return stringBuffer.toString();
    }

    /***
     * 联网获取最新的补丁或则列表
     * @param context
     * @return 可使用的补丁列表
     */
    @Override
    protected ArrayList<Patch> fetchPatchList(Context context) {
        if (patches == null) {
            patches = new ArrayList<>();
        }
        if (TextUtils.isEmpty(this.robustApkHash)) {
            throw new NullPointerException("当前模式是debug模式，请先初始化一个appHash. 调用方法setAppHash");
        }
        //规则：读取本地文件并解析；如果robustApkHash没有存储在本地列表中，将删除补丁，只加载用户版本对应的补丁（因为可能有多个）
        ArrayList<IqianjinPathModel> tempPaths = PatchHelper.getInstance(context).getLocalValidPatchList(this.robustApkHash);
        if (tempPaths == null) {
            tempPaths = new ArrayList<>();
        }
        Log.d(TAG, "localCacheSize.size() = " + tempPaths.size());
        onPatchListFetched(true, true);
        ArrayList<IqianjinPathModel> serverList = null;
        // 加载网络补丁文件
        try {
            String str = OkHttpUtils.simpleGet(this.okHttpClient, getUrl());
            if (!TextUtils.isEmpty(str)) {
                serverList = new Gson().fromJson(str, new TypeToken<ArrayList<IqianjinPathModel>>() {
                }.getType());
                if (serverList != null) {
                    Log.d(TAG, "serverList.size() = " + serverList.size());
                    for (Patch serverPath : serverList) {
                        serverPath.setLocalPath(getPatchDirPath(context) + serverPath.getName() + "_" + this.robustApkHash);
                    }
                    verificationPaths(context, tempPaths, serverList);
                } else {
                    Log.d(TAG, "serverList = is null");
                }
            }
        } catch (IOException e) {
            exceptionNotify(e, "class:PatchManipulateImpl method:fetchPatchList");
            onPatchListFetched(false, OkHttpUtils.isNetworkConnected(context));
        }

        setInfoAndMergePatches(context, tempPaths);
        Log.d(TAG, "可使用的补丁个数" + patches.size());
        return patches;
    }

    /**
     * 本地存储与服务器存储进行比较，如果有补丁有变更（新增或者删除）重新生成补丁列表
     *
     * @param context
     * @param tempPaths  本地存储的array
     * @param serverList 服务端的array
     */
    private void verificationPaths(Context context, ArrayList<IqianjinPathModel> tempPaths, ArrayList<IqianjinPathModel> serverList) {
        if (!(tempPaths.containsAll(serverList) && serverList.containsAll(tempPaths))) {
            tempPaths.clear();
            tempPaths.addAll(serverList);
            PatchHelper.getInstance(context).updateLocalPatchListDelay(serverList, this.robustApkHash);
        }
    }

    /**
     * 初始化数据
     *
     * @param context
     * @param localPathList
     */
    private void setInfoAndMergePatches(Context context, ArrayList<IqianjinPathModel> localPathList) {
        if (localPathList == null || localPathList.isEmpty()) {
            return;
        }
        Iterator iterator = localPathList.iterator();
        while (iterator.hasNext()) {
            Patch localPatch = (Patch) iterator.next();
            localPatch.setLocalPath(getPatchDirPath(context) + localPatch.getName() + "_" + this.robustApkHash);
            localPatch.setTempPath(getPatchTempDirPath(context) + localPatch.getName() + "_" + this.robustApkHash);
            localPatch.setUrl(getPatchIp() + localPatch.getUrl());
            localPatch.setAppHash(robustApkHash);
            if ((localPatch.getPatchesInfoImplClassFullName() == null) || ("" == localPatch.getPatchesInfoImplClassFullName())) {
                localPatch.setPatchesInfoImplClassFullName("com.iqianjin.client.robust.PatchesInfoImpl");
            }
            if (!isPatchesContains(localPatch)) {
                patches.add(localPatch);
            }
        }
    }

    /**
     * 补丁验证
     *
     * @param context
     * @param patch
     * @return you can verify your patches here
     */
    @Override
    protected boolean verifyPatch(Context context, Patch patch) {
        boolean isVerifyPath = VerifyUtils.verifyPatch(patch, context);
        Log.i("PatchManipulateImp", "verifyPatch :" + context.getCacheDir() + File.separator + "robust" + File.separator + "patch");
        return isVerifyPath;
    }

    /**
     * 检查是否有可用的补丁
     *
     * @param patch
     * @return you may download your patches here, you can check whether patch is in the phone
     */
    @Override
    protected boolean ensurePatchExist(Patch patch) {
        File localFile = new File(patch.getLocalPath());
        String string1 = new StringBuilder("md5 is ").append(patch.getMd5()).append(" VerifyUtils.fileMd5  ").append(VerifyUtils.fileMd5(localFile)).toString();
        String string2 = new StringBuilder("补丁是否存在 in  ensurePatchExist").append(localFile.exists()).toString();
        Log.d(TAG, string1 + "/" + string2);
        if ((localFile.exists()) && (patch.getMd5().equals(VerifyUtils.fileMd5(localFile)))) {
            onPatchFetched(true, false, patch);
            return true;
        }
        boolean isDownloaded = false;
        try {
            isDownloaded = OkHttpUtils.simpleDownload(this.okHttpClient, patch.getUrl(), localFile);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        onPatchFetched(isDownloaded, true, patch);
        return isDownloaded;
    }

    /**
     * 获取补丁文件
     *
     * @param paramContext
     * @return
     */
    protected static String getPatchDirPath(Context paramContext) {
        String str = paramContext.getFilesDir() + File.separator + "patch" + File.separator;
        ensureDirExist(str);
        return str;
    }

    /**
     * 检查改补丁是否已存在补丁列表中，如果已存在，则不进行加载
     *
     * @param paramPatch
     * @return
     */
    private boolean isPatchesContains(Patch paramPatch) {
        Iterator localIterator = patches.iterator();
        while (localIterator.hasNext()) {
            Patch localPatch = (Patch) localIterator.next();
            if (TextUtils.equals(paramPatch.getLocalPath(), localPatch.getLocalPath())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 创建临时补丁文件名
     *
     * @param paramContext
     * @return
     */
    public static String getPatchTempDirPath(Context paramContext) {
        String str = paramContext.getCacheDir() + File.separator + "patch" + File.separator;
        ensureDirExist(str);
        return str + "_" + processName;
    }

    /**
     * 创建补丁文件夹
     *
     * @param paramString
     */
    private static void ensureDirExist(String paramString) {
        File localFile = new File(paramString);
        if (!localFile.exists()) {
            localFile.mkdir();
        }
    }

    private void onPatchListFetched(boolean result, boolean isNet) {
        if (this.realCallBack != null) {
            try {
                this.realCallBack.onPatchListFetched(result, isNet, null);
                return;
            } catch (Throwable localThrowable) {
                Log.e(TAG,localThrowable.getMessage());
            }
        }
    }

    private void exceptionNotify(Throwable paramThrowable, String paramString) {
        if (this.realCallBack != null) {
            try {
                this.realCallBack.exceptionNotify(paramThrowable, paramString);
                return;
            } catch (Throwable localThrowable) {
                Log.e(TAG,localThrowable.getMessage());
            }
        }
    }

    private void onPatchFetched(boolean paramBoolean1, boolean paramBoolean2, Patch paramPatch) {
        if (this.realCallBack != null) {
            this.realCallBack.onPatchFetched(paramBoolean1, paramBoolean2, paramPatch);
        }
    }

    private String getIp() {
        if (!BuildConfig.isBuildTypeRealse) {
            return "http://10.10.223.41:3001/patchlist_v1?";
        } else {
            return "http://p1.iqianjin.com/patchlist_v1?";

        }
    }

    private String getPatchIp() {
        if (!BuildConfig.isBuildTypeRealse) {
            return "http://10.10.223.41:3333/data/apkstore/";
        } else {
            return "http://p1.iqianjin.com/apkstore/";

        }
    }
}