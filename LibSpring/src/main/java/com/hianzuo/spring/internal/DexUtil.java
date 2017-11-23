package com.hianzuo.spring.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author Ryan
 * @date 2017/11/11.
 */

public class DexUtil {
    private static final String TAG = "DexUtil";

    public static List<String> allClasses(Context context,boolean devMode) {
        long st = System.currentTimeMillis();
        List<String> list = null;
        if (devMode) {
            Log.w(TAG, "all classes on debug mode");
            //开发调试模式，需要每次读取
        } else if (isUpdateVersion(context)) {
            Log.w(TAG, "all classes on update version");
            //更新版本了，需要读取一次
        } else {
            //否则从缓存读取
            list = allClassesFromCache(context);
            if (null != list && list.size() > 0) {
                Log.w(TAG, "all classes from cache");
            } else {
                Log.w(TAG, "all classes on first time");
            }
        }
        if (null == list || list.isEmpty()) {
            //重新获取
            list = allClassesInternal(context);
            Log.w(TAG, "all classes count: " + list.size());
            saveAllClassesToCache(context, list);
        }
        Log.w(TAG, "all classes speed time: " + (System.currentTimeMillis() - st));
        return list;
    }

    public static int getPackageVersionCode(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    private static void saveAllClassesToCache(final Context context, final List<String> list) {
        ExecutorService executor = ThreadFactoryUtil.createSingle(DexUtil.class);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = sharedPreferences(context);
                Set<String> set = new HashSet<>();
                set.addAll(list);
                preferences.edit().putStringSet("set", set).apply();
            }
        });
        executor.shutdown();
    }

    private static boolean isUpdateVersion(Context context) {
        SharedPreferences preferences = SharedPreferencesUtils.get(context,
                "dex_util_version");
        int versionCode = preferences.getInt("version_code", 0);
        int appVersionCode = getPackageVersionCode(context);
        if (appVersionCode != versionCode) {
            preferences.edit().putInt("version_code", appVersionCode).apply();
            return true;
        }
        return false;
    }

    private static List<String> allClassesFromCache(Context context) {
        SharedPreferences preferences = sharedPreferences(context);
        List<String> list = new ArrayList<>();
        Set<String> set = preferences.getStringSet("set", null);
        if (null != set) {
            list.addAll(set);
        }
        return list;
    }

    private static SharedPreferences sharedPreferences(Context context) {
        return SharedPreferencesUtils.get(context, "all_classes");
    }

    private static List<String> allClassesInternal(Context context) {
        try {
            return MultiDexUtils.getAllClasses(context,getSourcePaths(context));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        if (MultiDexUtils.IS_VM_MULTI_DEX_CAPABLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String publicSourceDir = ai.publicSourceDir;
                String[] splitPublicSourceDirs = ai.splitPublicSourceDirs;
                List<String> list = new ArrayList<>();
                list.add(publicSourceDir);
                if (null != splitPublicSourceDirs) {
                    list.addAll(Arrays.asList(splitPublicSourceDirs));
                }
                return list;
            }
        }
        return MultiDexUtils.getSourcePaths(context);
    }
}
