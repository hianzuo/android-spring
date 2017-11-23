package com.hianzuo.spring.internal;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan
 *         On 2017/10/5.
 */
public class ClassUtilDex {
    private static final String TAG = "ClassUtilDex";


    public static List<Class<?>> getClasses(Context context, String pkg) {
        List<Class<?>> list = new ArrayList<>();
        try {
            List<String> allClasses = MultiDexUtils.getAllClasses(context);
            ClassLoader classLoader = context.getClassLoader();
            for (String clazzPath : allClasses) {
                Log.w(TAG, clazzPath);
                if (clazzPath.startsWith(pkg)) {
                    Class<?> c;
                    try {
                        //noinspection unchecked
                        c = classLoader.loadClass(clazzPath);
                    } catch (Exception e) {
                        continue;
                    }
                    list.add(c);
                }
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
