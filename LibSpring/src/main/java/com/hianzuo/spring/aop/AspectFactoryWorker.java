package com.hianzuo.spring.aop;

import com.hianzuo.spring.core.BaseFactoryWorker;
import com.hianzuo.spring.core.FactoryWorker;
import com.hianzuo.spring.core.InternalBean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Ryan
 * @date 2017/10/6.
 */
@FactoryWorker(order = 1)
public class AspectFactoryWorker extends BaseFactoryWorker {
    private final HashMap<Class<? extends Annotation>, List<AspectAdviceMethod>> mAspectPointcutMethodListMap = new HashMap<>();

    @Override
    public List<InternalBean> onLoad(List<Class<?>> clazz, List<InternalBean> oldList) {
        for (Class<?> javaClass : clazz) {
            if (isAspectClazz(javaClass)) {
                try {
                    initAspect(javaClass);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        initBeanForAspect(oldList);
        return oldList;
    }

    private void initBeanForAspect(List<InternalBean> oldList) {
        for (InternalBean bean : oldList) {
            Object targetObj = bean.getObj();
            Class<?> beanInterface = bean.getBeanInterface();
            if (null != beanInterface) {
                AspectProxyHandler proxyHandler = new AspectProxyHandler(targetObj, mAspectPointcutMethodListMap);
                bean.setObj(Proxy.newProxyInstance(targetObj.getClass().getClassLoader(), new Class<?>[]{beanInterface}, proxyHandler));
            }
        }
    }


    private void initAspect(Class<?> aspectClazz) {
        Method[] methods = aspectClazz.getMethods();
        Object obj;
        try {
            obj = aspectClazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (Method method : methods) {
            if (method.isAnnotationPresent(Before.class)) {
                Before adviceBefore = method.getAnnotation(Before.class);
                List<AspectAdviceMethod> adviceList = getAspectAdviceList(Before.class);
                adviceList.add(new AspectAdviceMethod(adviceBefore.value(), adviceBefore.order(), obj, method));
            }
            if (method.isAnnotationPresent(After.class)) {
                After adviceAfter = method.getAnnotation(After.class);
                List<AspectAdviceMethod> adviceList = getAspectAdviceList(After.class);
                adviceList.add(new AspectAdviceMethod(adviceAfter.value(), adviceAfter.order(), obj, method));
            }
            if (method.isAnnotationPresent(Around.class)) {
                Around adviceAround = method.getAnnotation(Around.class);
                List<AspectAdviceMethod> adviceList = getAspectAdviceList(Around.class);
                adviceList.add(new AspectAdviceAroundMethod(adviceAround.value(), adviceAround.order(), obj, method));
            }
        }

    }

    private List<AspectAdviceMethod> getAspectAdviceList(Class<? extends Annotation> adviceClazz) {
        List<AspectAdviceMethod> methodList = mAspectPointcutMethodListMap.get(adviceClazz);
        if (null == methodList) {
            methodList = new ArrayList<>();
            mAspectPointcutMethodListMap.put(adviceClazz, methodList);
        }
        return methodList;
    }

    private static boolean isAspectClazz(Class<?> aClass) {
        return aClass.isAnnotationPresent(Aspect.class) && !aClass.isAnnotation();
    }

}
