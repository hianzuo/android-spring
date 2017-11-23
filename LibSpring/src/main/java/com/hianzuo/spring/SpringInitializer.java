package com.hianzuo.spring;

import android.app.Application;
import android.util.Log;

import com.hianzuo.spring.activity.ActivitySupport;
import com.hianzuo.spring.core.InstanceFactory;

/**
 * @author Ryan
 * @date 2017/11/10.
 */

public class SpringInitializer {
    public static void init(Application application, String... pnScan) {
        long st = System.currentTimeMillis();
        Log.w("SpringInitializer", "start...");
        InstanceFactory.init(application, pnScan);
        ActivitySupport.init(application);
        Log.w("SpringInitializer", "end, speed:" + (System.currentTimeMillis() - st) + " ms");
    }

    public static void devMode(){
        InstanceFactory.devMode();
    }
}
