package com.hianzuo.spring.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.hianzuo.spring.utils.AndroidSpringLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author Ryan
 * @date 2017/11/11.
 */

public class DexUtil {
    private static final String TAG = "DexUtil";

    public static List<String> allClasses(Context context, boolean devMode) {
        long st = System.currentTimeMillis();
        List<String> list = null;
        if (devMode) {
            AndroidSpringLog.w(TAG + " all classes on debug mode");
            //开发调试模式，需要每次读取
        } else if (isUpdateVersion(context)) {
            AndroidSpringLog.w(TAG + " all classes on update version");
            //更新版本了，需要读取一次
        } else {
            //否则从缓存读取
            list = allClassesFromCache(context);
            if (null != list && list.size() > 0) {
                AndroidSpringLog.w(TAG + " all classes from cache");
            } else {
                AndroidSpringLog.w(TAG + " all classes on first time");
            }
        }
        if (null == list || list.isEmpty()) {
            //重新获取
            list = allClassesInternal(context);
            AndroidSpringLog.w(TAG + " all classes on read,size:" + list.size(), list);
            saveAllClassesToCache(context, list);
        }
        AndroidSpringLog.w(TAG + " all classes speed time: " + (System.currentTimeMillis() - st));
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
                Set<String> set = new HashSet<>(list);
                preferences.edit().putStringSet("set", set).apply();
                updateDexUtilAppVersion(context);
            }
        });
        executor.shutdown();
    }

    private static void updateDexUtilAppVersion(Context context) {
        int appVersionCode = getPackageVersionCode(context);
        getDexUtilPref(context).edit().putInt("version_code", appVersionCode).apply();
    }

    private static boolean isUpdateVersion(Context context) {
        SharedPreferences preferences = getDexUtilPref(context);
        int versionCode = preferences.getInt("version_code", 0);
        int appVersionCode = getPackageVersionCode(context);
        return appVersionCode != versionCode;
    }

    private static SharedPreferences getDexUtilPref(Context context) {
        return SharedPreferencesUtils.get(context, "android_spring_dex_util_version");
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
        return SharedPreferencesUtils.get(context, "android_spring_all_classes");
    }

    private static List<String> allClassesInternal(Context context) {
        try {
            Set<String> dexPathList = getSourcePaths(context);
            AndroidSpringLog.w(TAG + " read from get all class");
            return MultiDexUtils.getAllClasses(dexPathList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<String> getSourcePaths(Context context)
            throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        String publicSourceDir = ai.publicSourceDir;
        String[] splitPublicSourceDirs = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            splitPublicSourceDirs = ai.splitPublicSourceDirs;
        }
        Set<String> sets = new LinkedHashSet<>();
        sets.add(publicSourceDir);
        if (null != splitPublicSourceDirs) {
            sets.addAll(Arrays.asList(splitPublicSourceDirs));
        }
        sets.addAll(MultiDexUtils.getSourcePaths(context));
        return sets;
    }
}
