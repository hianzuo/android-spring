package com.hianzuo.spring.simple.app;

import android.app.Application;

import com.hianzuo.spring.SpringInitializer;

/**
 * @author ryan
 * @date 2017/11/21.
 */

public class SimpleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //if in dev mode ,please line blow code
        //SpringInitializer.devMode();
        //spring init， you can add more package to scan spring component.
        SpringInitializer.init(this,
                "com.hianzuo.spring.simple.",
                "other package to scan spring component");
    }
}
