package com.hianzuo.spring.utils;

import android.util.Log;

/**
 * @author Ryan
 * @date 2020/5/26
 */
public class AndroidSpringLog {
    private static AndroidSpringLogListener mListener;

    public static void listen(AndroidSpringLogListener listener) {
        AndroidSpringLog.mListener = listener;
    }

    public static void w(String msg) {
        if (null != mListener && mListener.w(msg)) {
            return;
        }
        Log.w("AndroidSpringLog", msg);
    }
}
