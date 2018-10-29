package com.iqianjin.client.hotfix.robust;

import android.content.Context;
import android.util.Log;

/**
 * 补丁的自我修复
 * 当加载补丁后20秒以内，删除补丁
 * Created by iqianjin-liujiawei on 18/9/4.
 */
class RobustCrashHandler implements Thread.UncaughtExceptionHandler {
    private static final long LIMTIT_TIME = 20000L;
    private static long closingTime;
    private static boolean startCheckCrash = false;
    private Context context;
    private Thread.UncaughtExceptionHandler oldHandler;
    private RobustCallBackImp robustCallBack;

    public RobustCrashHandler(Context paramContext, RobustCallBackImp robustCallBack) {
        this.context = paramContext;
        this.robustCallBack = robustCallBack;
        setClosingTime();
    }

    static void setClosingTime() {
        startCheckCrash = true;
        closingTime = LIMTIT_TIME + System.currentTimeMillis();
    }

    private boolean timeValid() {
        boolean timeValid = false;
        if (startCheckCrash) {
            if (closingTime - System.currentTimeMillis() < 0L) {
                timeValid = true;
            }
        }
        return timeValid;
    }

    void setDefaultUncaughtExceptionHandlerSelf() {
        if (this.oldHandler == null) {
            this.oldHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    /**
     * 当加载补丁一分钟内出现异常的时候清空补丁文件
     *
     * @param thread
     * @param throwable
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        sendErrorMessage(throwable);
        if (throwable != null) {
            try {
//                if (timeValid()) {
//                    PatchHelper.getInstance(this.context).deleteLocalPatchList();
//                }
                if (this.oldHandler != null) {
                    this.oldHandler.uncaughtException(thread, throwable);
                }
                return;
            } catch (Throwable ex) {
                Log.d(PatchManipulateImp.TAG,"uncaughtException");
            }
        }
    }

    private void sendErrorMessage(Throwable ex) {
        this.robustCallBack.exceptionNotify(ex, "appCrash");
    }
}
