package com.hianzuo.spring.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.hianzuo.spring.utils.AndroidSpringLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.DexFile;

/**
 * @author Ryan
 * @date 2017/11/11.
 */

public class MultiDexUtils {
    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";

    private static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator +
            "secondary-dexes";

    private static final String PREFS_FILE = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";

    private static SharedPreferences getMultiDexPreferences(Context context) {
        return context.getSharedPreferences(PREFS_FILE, Context.MODE_MULTI_PROCESS);
    }

    /**
     * get all the dex path
     *
     * @param context the application context
     * @return all the dex path
     */
    public static List<String> getSourcePaths(Context context) throws IOException, PackageManager.NameNotFoundException {
        ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        File sourceApk = new File(ai.sourceDir);
        File dexDir = new File(ai.dataDir, SECONDARY_FOLDER_NAME);

        List<String> sourcePaths = new ArrayList<String>();
        sourcePaths.add(ai.sourceDir); //add the default apk path

        //the prefix of extracted file, ie: test.classes
        String extractedFilePrefix = sourceApk.getName() + EXTRACTED_NAME_EXT;
        //the total dex numbers
        int totalDexNumber = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1);

        for (int secondaryNumber = 2; secondaryNumber <= totalDexNumber; secondaryNumber++) {
            //for each dex file, ie: test.classes2.zip, test.classes3.zip...
            String fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX;
            File extractedFile = new File(dexDir, fileName);
            if (extractedFile.isFile()) {
                sourcePaths.add(extractedFile.getAbsolutePath());
                //we ignore the verify zip part
            } else {
                throw new IOException("Missing extracted secondary dex file '" +
                        extractedFile.getPath() + "'");
            }
        }

        return sourcePaths;
    }

    /**
     * get all the classes name in "classes.dex", "classes2.dex", ....
     *
     * @param context the application context
     * @return all the classes name
     */
    public static List<String> getAllClasses(Context context) {
        try {
            List<String> sourcePaths = getSourcePaths(context);
            return getAllClasses(context, sourcePaths);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
        static List<String> getAllClasses(Context context, List<String> sourcePaths) {
            List<String> classNames = new ArrayList<>();
            for (String path : sourcePaths) {
                try {
                    DexBackedDexFile dexFile = DexFileFactory.loadDexFile(path, Opcodes.forApi(Build.VERSION.SDK_INT));
                    Set<? extends DexBackedClassDef> classes = dexFile.getClasses();
                    for (DexBackedClassDef aClass : classes) {
                        String className = aClass.getType();
                        int len = className.length();
                        if (className.charAt(0) == 'L' && className.charAt(len - 1) == ';') {
                            classNames.add(className.substring(1, len - 1).replace('/', '.'));
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error at loading dex file '" + path + "'");
                }
            }
            return classNames;
        }
        */
    static List<String> getAllClasses(Context context, List<String> sourcePaths) throws IOException {
        List<String> classNames = new ArrayList<>();
        for (String path : sourcePaths) {
            DexFile dexfile = null;
            try {
                if (path.endsWith(EXTRACTED_SUFFIX)) {
                    //NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                    dexfile = DexFile.loadDex(path, path + ".tmp", 0);
                } else {
                    dexfile = new DexFile(path);
                }
                Enumeration<String> dexEntries = dexfile.entries();
                while (dexEntries.hasMoreElements()) {
                    classNames.add(dexEntries.nextElement());
                }
            } catch (IOException e) {
                throw new IOException("Error at loading dex file '" + path + "'");
            } finally {
                if (null != dexfile) {
                    dexfile.close();
                }
            }
        }
        return classNames;
    }

    static final boolean IS_VM_MULTI_DEX_CAPABLE;

    static {
        IS_VM_MULTI_DEX_CAPABLE = isVmMultiDexCapable(System.getProperty("java.vm.version"));
    }


    private static boolean isVmMultiDexCapable(String versionString) {
        boolean isVmMultiDexCapable = false;
        if (versionString != null) {
            Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
            if (matcher.matches()) {
                try {
                    int major = Integer.parseInt(matcher.group(1));
                    int minor = Integer.parseInt(matcher.group(2));
                    isVmMultiDexCapable = major > 2 || major == 2 && minor >= 1;
                } catch (NumberFormatException ignored) {
                    ;
                }
            }
        }

        AndroidSpringLog.w("MultiDex VM with version " + versionString + (isVmMultiDexCapable ? " has multidex support" : " does not have multidex support"));
        return isVmMultiDexCapable;
    }
}
