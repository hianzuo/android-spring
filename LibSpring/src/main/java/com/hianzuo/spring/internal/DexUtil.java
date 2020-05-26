package com.hianzuo.spring.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.hianzuo.spring.utils.AndroidSpringLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
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

    public static List<String> allClasses(Context context, boolean devMode) {
        long st = System.currentTimeMillis();
        List<String> list = null;
        if (devMode) {
            AndroidSpringLog.w(TAG+" all classes on debug mode");
            //开发调试模式，需要每次读取
        } else if (isUpdateVersion(context)) {
            AndroidSpringLog.w(TAG+" all classes on update version");
            //更新版本了，需要读取一次
        } else {
            //否则从缓存读取
            list = allClassesFromCache(context);
            if (null != list && list.size() > 0) {
                AndroidSpringLog.w(TAG+" all classes from cache");
            } else {
                AndroidSpringLog.w(TAG+" all classes on first time");
            }
        }
        if (null == list || list.isEmpty()) {
            //重新获取
            list = allClassesInternal(context);
            AndroidSpringLog.w(TAG+" all classes count: " + list.size());
            saveAllClassesToCache(context, list);
        }
        AndroidSpringLog.w(TAG+" all classes speed time: " + (System.currentTimeMillis() - st));
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
        return SharedPreferencesUtils.get(context, "dex_util_version");
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
            List<String> dexPathList = getSourcePaths(context);
            List<String> allClasses = new ArrayList<>();
            if (!isDexChanged(context, dexPathList)) {
                AndroidSpringLog.w(TAG+" dex no changed , read from cache");
                allClasses = allClassesFromCache(context);
            }
            if (null == allClasses || allClasses.isEmpty()) {
                AndroidSpringLog.w(TAG+" read from get all class");
                allClasses = MultiDexUtils.getAllClasses(context, dexPathList);
            }
            return allClasses;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isDexChanged(Context context, List<String> dexPathList) {
        SharedPreferences preferences = SharedPreferencesUtils.get(context,"dex_util_list_md5");
        String oldDexListMd5 = preferences.getString("value", "");
        String dexListMd5 = dexListMd5(dexPathList);
        preferences.edit().putString("value", dexListMd5).apply();
        return !oldDexListMd5.equals(dexListMd5);
    }

    private static String dexListMd5(List<String> dexPathList) {
        StringBuilder sb = new StringBuilder();
        for (String dexPath : dexPathList) {
            try {
                sb.append(readMd5ByFile(dexPath));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sb.toString();
    }

    private static String readMd5ByFile(String filePath) throws Exception {
        FileInputStream in = null;
        try {
            File file = new File(filePath);
            in = new FileInputStream(file);
            FileChannel.MapMode readOnly = FileChannel.MapMode.READ_ONLY;
            MappedByteBuffer byteBuffer = in.getChannel().map(readOnly, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            return new BigInteger(1, md5.digest()).toString(16);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
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
