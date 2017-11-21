package com.hianzuo.spring.internal;

import android.app.ActivityManager;
import android.content.Context;

/**
 * @author ryan
 * @date 2017/11/21.
 */

public class ApplicationUtil {
    private static String mProgressName;

    public static String progressName(Context context) {
        if (null != mProgressName) {
            return mProgressName;
        } else {
            int pid = android.os.Process.myPid();
            ActivityManager activityManager = (ActivityManager)
                    context.getSystemService(Context.ACTIVITY_SERVICE);
            assert null != activityManager;
            for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                    .getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    mProgressName = appProcess.processName;
                    break;
                }
            }
        }
        return mProgressName;
    }
}
