package com.hianzuo.spring;

import android.app.Application;

import com.hianzuo.spring.activity.ActivitySupport;
import com.hianzuo.spring.core.InstanceFactory;
import com.hianzuo.spring.utils.AndroidSpringLog;

/**
 * @author Ryan
 * @date 2017/11/10.
 */

public class SpringInitializer {
    private static volatile boolean isActivitySupportInit = false;

    public static void init(Application application, String... pnScan) {
        long st = System.currentTimeMillis();
        AndroidSpringLog.w("SpringInitializer start...");
        InstanceFactory.init(application, pnScan);
        if (!isActivitySupportInit) {
            isActivitySupportInit = true;
            AndroidSpringLog.w("SpringInitializer activity support init...");
            ActivitySupport.init(application);
        }
        AndroidSpringLog.w("SpringInitializer end, speed:" + (System.currentTimeMillis() - st) + " ms");
    }

    public static void devMode() {
        InstanceFactory.devMode();
    }
}
