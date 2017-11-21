package com.hianzuo.spring.activity;

import android.app.Application;

/**
 * @author ryan
 * @date 2017/11/21.
 */

public class ActivitySupport {
    private static SpringActivityLifecycleCallbacks mCallbacks = new SpringActivityLifecycleCallbacks();

    public static void init(Application application) {
        application.registerActivityLifecycleCallbacks(mCallbacks);
    }
}
