package com.hianzuo.spring.simple.http;

import android.util.Log;

import com.hianzuo.spring.annotation.Handler;
import com.hianzuo.spring.http.HandleMethod;
import com.hianzuo.spring.http.MethodParam;

/**
 * @author ryan
 * @date 2017/11/22.
 */

@Handler("/api/reg")
public class HttpRegHandler extends BaseTestHandler {

    @HandleMethod
    public void handle(@MethodParam("username") String name, @MethodParam("password") String pwd) {
        Log.d("HttpRegHandler", "username: " + name + ", password: " + pwd);
    }
}
