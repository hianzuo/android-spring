package com.hianzuo.spring.internal;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author ryan
 * @date 2017/11/21.
 */

public class SharedPreferencesUtils {
    public static SharedPreferences get(Context context, String name) {
        return context.getSharedPreferences(name + ".properties", Context.MODE_MULTI_PROCESS);
    }
}
