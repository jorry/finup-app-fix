package com.iqianjin.client.hotfix.robust;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iqianjin.client.hotfix.robust.file.ProcessSafeOperateUtils;
import com.meituan.robust.Patch;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 补丁文件的维护
 * 1. 补丁列表    JSON
 * 2. 补丁文件
 */
public class PatchHelper {
    private static String LOCAL_PATCH_LIST = "patch.list";
    private static final Type TYPE;
    private static Gson gson = new Gson();
    private static PatchHelper instance;
    private final Context context;

    static {
        TYPE = new TypeToken<ArrayList<Patch>>() {
        }.getType();
    }

    private PatchHelper(Context context) {
        this.context = context.getApplicationContext();
        initLock(context);
    }

    private void initLock(Context paramContext) {
        String str = PatchManipulateImp.getPatchDirPath(paramContext);
        LOCAL_PATCH_LIST = str + LOCAL_PATCH_LIST;
        try {
            ensureFileExist(LOCAL_PATCH_LIST);
        } catch (IOException e) {
            Log.d(PatchManipulateImp.TAG, e.getMessage());
        }
    }


    /**
     * 删除补丁文件
     *
     * @param path
     */
    private static void deletePatchSafe(Patch path) {
        if (path == null) {
            return;
        }
        try {
            File file = new File(path.getLocalPath());
            if (file.exists()) {
                ProcessSafeOperateUtils.deletePatchSafe(new File(path.getLocalPath()));
            }
        } catch (IOException localIOException) {
            Log.e(PatchManipulateImp.TAG, localIOException.getMessage());
        }
    }

    private boolean ensureFileExist(String files)
            throws IOException {
        File file = new File(files);
        if (!file.exists()) {
            return file.createNewFile();
        }
        return true;
    }

    static PatchHelper getInstance(Context context) {
        try {
            if (instance == null) {
                instance = new PatchHelper(context);
            }
            return instance;
        } finally {
        }
    }

    /**
     * 获取本地配置补丁信息
     * 1. 读取文件
     * 2. 解析成JSON
     * 3. 验证补丁信息，如果不是当前版本的补丁，删除之
     *
     * @param robustApkHash app 唯一
     * @return
     */
    public static ArrayList<IqianjinPathModel> getLocalValidPatchList(String robustApkHash) {
        String str = readPatchListFromLocal();
        List<IqianjinPathModel> patches = null;
        if (!TextUtils.isEmpty(str)) {
            try {
                patches = parseJsonToPatches(str);
            } catch (Throwable ex) {
                Log.d(PatchManipulateImp.TAG, ex.getMessage());
            }
            if (patches != null) {
                Iterator iterator = patches.iterator();
                while (iterator.hasNext()) {
                    Patch localPatch = (Patch) iterator.next();
                    if (!TextUtils.equals(localPatch.getAppHash(), robustApkHash)) {
                        iterator.remove();
                        deletePatchSafe(localPatch);
                    }
                }
            }
        }
        return (ArrayList<IqianjinPathModel>) patches;
    }

    /**
     * 清空补丁
     */
    public static void deleteLocalPatchList() {
        String str = readPatchListFromLocal();
        List localList = null;
        if (!TextUtils.isEmpty(str)) {
        }
        try {
            localList = parseJsonToPatches(str);
        } catch (Throwable ex) {
            Log.d(PatchManipulateImp.TAG, ex.getMessage());
        }
        if (localList != null) {
            Iterator iterator = localList.iterator();
            while (iterator.hasNext()) {
                Patch localPatch = (Patch) iterator.next();
                iterator.remove();
                deletePatchSafe(localPatch);
            }
        }
        writePatchListLocal("");
    }

    private static List<IqianjinPathModel> parseJsonToPatches(String localJson) {
        if (!TextUtils.isEmpty(localJson)) {
            try {
                return gson.fromJson(localJson, TYPE);
            } catch (Throwable ex) {
                Log.d(PatchManipulateImp.TAG, ex.getMessage());
            }
        }
        return null;
    }


    private static String readPatchListFromLocal() {
        try {
            File localFile = new File(LOCAL_PATCH_LIST);
            String str = ProcessSafeOperateUtils.readPatchListLocal(localFile);
            return str;
        } catch (IOException e) {
            Log.d(PatchManipulateImp.TAG, "本地补丁维护的列表 异常：" + e);
        }
        return null;
    }

    /**
     * @param serverPath    服务器补丁和本地补丁集合
     * @param robustApkHash
     */
    public void updateLocalPatchListDelay(final List<IqianjinPathModel> serverPath, final String robustApkHash) {
        updatePatchListJsonToLocal(serverPath, robustApkHash);
    }

    /**
     * 内容来自官网wiki:
     * 补丁如何加载才能保证尽可能修复所有问题，并且在后台做到补丁可控，可控的意思就是可以随时不使用补丁，补丁的加载与否完全由后台来决定。
     * 首先来说为了提高补丁的加载率，有必要在应用启动的时候加载缓存的补丁，也就是上一次App启动加载过得补丁，但是这会导致一个问题，那就是我如何在后台控制不使用这个补丁，
     * 这个问题可以在本地保留一个和服务器同步的补丁列表，当发现补丁被在补丁后台不存在的时候，就删除本地缓存的补丁。
     *
     * @param serverPath    服务器补丁
     * @param robustApkHash
     * @return
     */
    private boolean updatePatchListJsonToLocal(List<IqianjinPathModel> serverPath, String robustApkHash) {

        if (serverPath == null) {
            return false;
        }
        String json = null;
        try {
            json = gson.toJson(serverPath, TYPE);
        } catch (Throwable ex) {
            Log.d(PatchManipulateImp.TAG, ex.getMessage());
        }

        if (TextUtils.isEmpty(json)) {
            return false;
        }
//      reset 服务器中没有包含本地列表的补丁，删除本地补丁
        List<IqianjinPathModel> localList = getLocalValidPatchList(robustApkHash);
        if (localList != null) {
            for (Patch localPath : localList) {
                if ((!TextUtils.isEmpty(localPath.getMd5())) && (!json.contains(localPath.getMd5()))) {
                    deletePatchSafe(localPath);
                }
            }
        }

        return writePatchListLocal(json);
    }

    private static boolean writePatchListLocal(String pathJson) {
        try {
            return ProcessSafeOperateUtils.writePatchListLocal(new File(LOCAL_PATCH_LIST), pathJson);
        } catch (IOException ex) {
            Log.d(PatchManipulateImp.TAG, ex.getMessage());
        }
        return false;
    }

}