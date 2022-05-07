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
        if (null != mListener && mListener.w(msg, null)) {
            return;
        }
        Log.w("AndroidSpringLog", msg);
    }

    public static void d(String msg) {
        if (null != mListener && mListener.d(msg, null)) {
            return;
        }
        Log.d("AndroidSpringLog", msg);
    }

    public static void w(String msg, Object attach) {
        if (null != mListener && mListener.w(msg, attach)) {
            return;
        }
        Log.w("AndroidSpringLog", msg);
    }
}
