package com.hianzuo.spring.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.hianzuo.spring.core.InstanceFactory;

/**
 * @author ryan
 * @date 2017/11/21.
 */

public class SpringActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        InstanceFactory.render(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
