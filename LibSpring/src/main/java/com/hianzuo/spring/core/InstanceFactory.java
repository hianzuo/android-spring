package com.hianzuo.spring.core;


import android.content.Context;
import android.content.SharedPreferences;

import com.hianzuo.spring.annotation.Component;
import com.hianzuo.spring.annotation.Handler;
import com.hianzuo.spring.http.BaseHandler;
import com.hianzuo.spring.internal.AnnotationUtil;
import com.hianzuo.spring.internal.ApplicationUtil;
import com.hianzuo.spring.internal.DexUtil;
import com.hianzuo.spring.internal.SharedPreferencesUtils;
import com.hianzuo.spring.internal.StringUtil;
import com.hianzuo.spring.internal.ThreadFactoryUtil;
import com.hianzuo.spring.utils.AndroidSpringLog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;


/**
 * @author Ryan
 * @date 2017/10/5.
 */
public class InstanceFactory {
    private static final HashMap<Class<?>, Object> CLASS_BEAN_MAP = new HashMap<>();
    private static final HashMap<String, Object> BEAN_NAME_MAP = new HashMap<>();

    public static <T> T get(Class<T> clazz) {
        //noinspection unchecked
        return (T) CLASS_BEAN_MAP.get(clazz);
    }

    public static <T> T get(String beanName) {
        //noinspection unchecked
        return (T) BEAN_NAME_MAP.get(beanName);
    }

    public static <Bean> List<Bean> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        List<Bean> results = new ArrayList<>();
        HashSet<Object> set = new HashSet<>();
        for (Class<?> c : CLASS_BEAN_MAP.keySet()) {
            Object o = CLASS_BEAN_MAP.get(c);
            if (set.contains(o)) {
                continue;
            }
            set.add(o);
            if (AnnotationUtil.has(c, annotationType)) {
                //noinspection unchecked
                results.add((Bean) o);
            }
        }
        return results;
    }

    private static List<BaseFactoryWorker> mFactoryWorkerAdapters;

    private static android.os.Handler uiHandler;
    private static boolean devMode = false;

    public static void devMode() {
        devMode = true;
    }

    public static void init(Context context, String... pnScan) {
        if (null == uiHandler) {
            uiHandler = new android.os.Handler();
        }
        AndroidSpringLog.w("SpringInitializer scan class start . ");
        long st = System.currentTimeMillis();
        List<Class<?>> scanClassList = scanAllClasses(context, devMode, pnScan);
        AndroidSpringLog.w("SpringInitializer scan class end , speed:" + (System.currentTimeMillis() - st) + " count:" + scanClassList.size());
        try {
            List<Class<?>> factoryClazzList = new ArrayList<>();
            for (Class<?> javaClass : scanClassList) {
                if (isFactoryWorkerClazz(javaClass)) {
                    factoryClazzList.add(javaClass);
                }
            }
            List<BaseFactoryWorker> factoryWorkerAdapters = new ArrayList<>();
            for (Class<?> javaClass : factoryClazzList) {
                Constructor<?> constructor = javaClass.getConstructor();
                constructor.setAccessible(true);
                factoryWorkerAdapters.add((BaseFactoryWorker) constructor.newInstance());
            }
            sortFactoryWorkerList(factoryWorkerAdapters);
            mFactoryWorkerAdapters = factoryWorkerAdapters;
            List<InternalBean> beanList = buildInternalBeanList(scanClassList);
            for (InternalBean bean : beanList) {
                Object targetObj = bean.getTargetObj();
                if (!(targetObj instanceof Class)) {
                    CLASS_BEAN_MAP.put(bean.getTargetObj().getClass(), bean.getObj());
                }
                if (null != bean.getBeanInterface()) {
                    CLASS_BEAN_MAP.put(bean.getBeanInterface(), bean.getObj());
                }
                if (null != bean.getName() && bean.getName().length() > 0) {
                    BEAN_NAME_MAP.put(bean.getName(), bean.getObj());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<InternalBean> buildInternalBeanList(List<Class<?>> scanClassList) {
        List<InternalBean> beanList = new ArrayList<>();
        return buildInternalBeanList(scanClassList, beanList, null);
    }

    private static List<InternalBean> buildInternalBeanList(List<Class<?>> scanClassList, List<InternalBean> beanList, BeanGetter beanGetter) {
        for (BaseFactoryWorker adapter : mFactoryWorkerAdapters) {
            beanList = adapter.onLoad(scanClassList, beanList, beanGetter);
        }
        for (BaseFactoryWorker adapter : mFactoryWorkerAdapters) {
            beanList = adapter.onBeanCreatedDone(scanClassList, beanList, beanGetter);
        }
        return beanList;
    }

    private static List<Class<?>> scanAllClasses(Context context, boolean devMode, String[] pnScan) {
        Set<String> classNameSet;
        if (devMode) {
            AndroidSpringLog.w("SpringInitializer scanAllClassNameList on Debug Mode");
            classNameSet = scanAllClassNameList(context, pnScan);
        } else if (isUpdateVersion(context)) {
            AndroidSpringLog.w("SpringInitializer scanAllClassNameList on Update Version");
            classNameSet = scanAllClassNameList(context, pnScan);
            saveScanAllClassNameList(context, classNameSet);
        } else {
            classNameSet = getScanAllClassNameList(context);
            if (null == classNameSet) {
                AndroidSpringLog.w("SpringInitializer scanAllClassNameList on FirstTime");
                classNameSet = scanAllClassNameList(context, pnScan);
                saveScanAllClassNameList(context, classNameSet);
            } else {
                AndroidSpringLog.w("SpringInitializer scanAllClassNameList from cache");
                refreshScanAllClassNameList(context, pnScan);
            }
        }
        List<Class<?>> scanClassList = new ArrayList<>();
        ClassLoader classLoader = context.getClassLoader();
        for (String className : classNameSet) {
            try {
                scanClassList.add(classLoader.loadClass(className));
            } catch (Exception ignored) {
            }
        }
        return scanClassList;
    }

    private static void updateAppVersionCode(Context context) {
        int appVersionCode = DexUtil.getPackageVersionCode(context);
        getVersionSharedPref(context).edit().putInt("version_code",appVersionCode).apply();
    }

    private static boolean isUpdateVersion(Context context) {
        SharedPreferences preferences = getVersionSharedPref(context);
        int versionCode = preferences.getInt("version_code", 0);
        int appVersionCode = DexUtil.getPackageVersionCode(context);
        return appVersionCode != versionCode;
    }

    private static SharedPreferences getVersionSharedPref(Context context) {
        return SharedPreferencesUtils.get(context,
                    "android_spring_" + ApplicationUtil.progressName(context) + "_version");
    }

    private static void refreshScanAllClassNameList(final Context context, final String[] pnScan) {
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ExecutorService executor = ThreadFactoryUtil.createSingle(InstanceFactory.class);
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        Set<String> classNameSet = scanAllClassNameList(context, pnScan);
                        saveScanAllClassNameList(context, classNameSet);
                    }
                });
                executor.shutdown();
            }
        }, 3000);
    }

    private static void saveScanAllClassNameList(final Context context, final Set<String> classNameSet) {
        ExecutorService executor = ThreadFactoryUtil.createSingle(InstanceFactory.class);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                sharedPreferences(context).edit().putStringSet("classes_set", classNameSet).apply();
                updateAppVersionCode(context);
            }
        });
        executor.shutdown();
    }

    private static SharedPreferences sharedPreferences(Context context) {
        return SharedPreferencesUtils.get(context,
                "android_spring_"+ApplicationUtil.progressName(context) + "_classes");
    }

    private static Set<String> getScanAllClassNameList(Context context) {
        Set<String> set = sharedPreferences(context)
                .getStringSet("classes_set", null);
        if (null != set && set.size() > 10) {
            return set;
        }
        return null;
    }

    private static Set<String> scanAllClassNameList(Context context, String[] pnScan) {
        if (null == pnScan || pnScan.length == 0) {
            return new HashSet<>();
        }
        List<String> classNameList = DexUtil.allClasses(context, devMode);
        Set<String> list = new HashSet<>();
        String springBasePackage = BaseFactoryWorker.class.getPackage().getName();
        springBasePackage = springBasePackage.substring(0, springBasePackage.lastIndexOf("."));
        for (String className : classNameList) {
            if (matchScan(className, pnScan) || matchScan(className, springBasePackage)) {
                list.add(className);
            }
        }
        return list;
    }


    private static boolean matchScan(String name, String... scanPnArray) {
        for (String scanPn : scanPnArray) {
            if (name.startsWith(scanPn)) {
                return true;
            }
        }
        return false;
    }

    private static void sortFactoryWorkerList(List<? extends BaseFactoryWorker> list) {
        Collections.sort(list, new Comparator<BaseFactoryWorker>() {
            @Override
            public int compare(BaseFactoryWorker o1, BaseFactoryWorker o2) {
                Integer order1 = o1.getClass().getAnnotation(FactoryWorker.class).order();
                Integer order2 = o2.getClass().getAnnotation(FactoryWorker.class).order();
                return order1.compareTo(order2);
            }
        });
    }

    private static boolean isFactoryWorkerClazz(Class<?> aClass) {
        return aClass.isAnnotationPresent(FactoryWorker.class) && !aClass.isAnnotation();
    }

    private static final BeanGetter BEAN_GETTER = new BeanGetter() {
        @Override
        public Object get(String beanName, Class<?> beanInterface) {
            Object obj = null;
            if (!StringUtil.isBlank(beanName)) {
                obj = InstanceFactory.get(beanName);
            }
            if (null == obj) {
                obj = InstanceFactory.get(beanInterface);
            }
            return obj;
        }
    };

    public static <T> T newObj(Class<T> c) {
        try {
            Constructor<T> defCons = null;
            try {
                defCons = c.getDeclaredConstructor();
            } catch (Exception ignored) {
            }
            if (null == defCons) {
                defCons = c.getConstructor();
            }
            defCons.setAccessible(true);
            T t = defCons.newInstance();
            if (!AnnotationUtil.has(c, Component.class, Handler.class)) {
                return t;
            }
            //noinspection unchecked
            return (T) renderInternal(t).getObj();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T render(T t) {
        //noinspection unchecked
        return (T) renderInternal(t).getTargetObj();
    }

    private static InternalBean renderInternal(Object obj) {
        List<InternalBean> beans = Collections.singletonList(
                new InternalBean("tempBean", obj));
        buildInternalBeanList(new ArrayList<Class<?>>(), beans, BEAN_GETTER);
        return beans.get(0);
    }

    private static HashMap<String, Class<? extends BaseHandler>> mHandlerClassMap;

    public static <T extends BaseHandler> T newHandler(String handlerName) {
        initHandlerClassMap();
        Class<? extends BaseHandler> handlerClazz = mHandlerClassMap.get(handlerName);
        if (null == handlerClazz) {
            throw new RuntimeException("handler name [" + handlerName + "] is not existed.");
        }
        //noinspection unchecked
        return (T) newObj(handlerClazz);
    }

    private synchronized static void initHandlerClassMap() {
        if (null == mHandlerClassMap) {
            mHandlerClassMap = new HashMap<>(32);
            List<Class<? extends BaseHandler>> controllerClassList =
                    InstanceFactory.getBeansWithAnnotation(Handler.class);
            for (Class<? extends BaseHandler> controllerClass : controllerClassList) {
                List<Annotation> annotations = AnnotationUtil.get(controllerClass, Handler.class);
                String uri = AnnotationUtil.joinValue(annotations);
                mHandlerClassMap.put(uri, controllerClass);
            }
        }
    }
}
