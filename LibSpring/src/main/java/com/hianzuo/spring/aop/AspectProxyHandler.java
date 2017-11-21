package com.hianzuo.spring.aop;


import com.hianzuo.spring.core.ProxyHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * @author Ryan
 *         On 2017/10/5.
 */
class AspectProxyHandler implements ProxyHandler {
    private Object targetObj;
    private HashMap<Class<? extends Annotation>, List<AspectAdviceMethod>> mAspectPointcutMethodListMap;
    private HashMap<Method, List<AspectAdviceMethod>> mBeforeMethodMap = new HashMap<>();
    private HashMap<Method, List<AspectAdviceMethod>> mAfterMethodMap = new HashMap<>();
    private HashMap<Method, AspectAdviceAroundMethod> mAroundMethodMap = new HashMap<>();

    public AspectProxyHandler(Object targetObj, HashMap<Class<? extends Annotation>, List<AspectAdviceMethod>> map) {
        this.targetObj = targetObj;
        this.mAspectPointcutMethodListMap = map;
    }

    @Override
    public Object getTargetObj() {
        return targetObj;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (null == args) {
            args = new Object[0];
        }
        List<AspectAdviceMethod> beforeMethods = getBeforeMethodList(method);
        JointPoint beforePoint = new JointPoint().setTargetObj(targetObj).setProxy(proxy).setMethod(method).setArgs(args);
        for (AspectAdviceMethod adviceMethod : beforeMethods) {
            adviceMethod.invoke(beforePoint);
        }
        AspectAdviceAroundMethod aroundMethod = getAroundMethodList(method);
        Object obj;
        if (null != aroundMethod) {
            obj = aroundMethod.invoke(beforePoint);
        } else {
            obj = method.invoke(targetObj, args);
        }
        List<AspectAdviceMethod> afterMethods = getAfterMethodList(method);
        JointPoint afterPoint = new JointPoint().setTargetObj(targetObj).setProxy(proxy).setMethod(method).setArgs(args).setResult(obj);
        for (AspectAdviceMethod adviceMethod : afterMethods) {
            adviceMethod.invoke(afterPoint);
        }
        return obj;
    }

    private AspectAdviceAroundMethod getAroundMethodList(Method method) {
        if (mAroundMethodMap.containsKey(method)) {
            return mAroundMethodMap.get(method);
        }
        List<AspectAdviceMethod> adviceMethods = mAspectPointcutMethodListMap.get(Around.class);
        if (null == adviceMethods) {
            return null;
        }
        List<AspectAdviceAroundMethod> list = new ArrayList<>();
        for (AspectAdviceMethod adviceMethod : adviceMethods) {
            AspectAdviceAroundMethod adviceAroundMethod = (AspectAdviceAroundMethod) adviceMethod;
            if (adviceAroundMethod.match(method)) {
                list.add(adviceAroundMethod);
            }
        }
        AspectAdviceAroundMethod aroundMethod = null;
        if (!list.isEmpty()) {
            sortAdviceList(list);
            AspectAdviceAroundMethod upMethod = null;
            for (AspectAdviceAroundMethod adviceAroundMethod : list) {
                if (null == aroundMethod) {
                    aroundMethod = adviceAroundMethod;
                }
                if (null != upMethod) {
                    upMethod.setNextMethod(adviceAroundMethod);
                }
                upMethod = adviceAroundMethod;
            }
        }
        mAroundMethodMap.put(method, aroundMethod);
        return aroundMethod;
    }

    private static void sortAdviceList(List<? extends AspectAdviceMethod> list) {
        Collections.sort(list, new Comparator<AspectAdviceMethod>() {
            @Override
            public int compare(AspectAdviceMethod o1, AspectAdviceMethod o2) {
                Integer order1 = o1.getPointMethodOrder();
                Integer order2 = o2.getPointMethodOrder();
                return order1.compareTo(order2);
            }
        });
    }

    private List<AspectAdviceMethod> getAfterMethodList(Method method) {
        return getAspectAdviceMethods(After.class, mAspectPointcutMethodListMap, mAfterMethodMap, method);
    }

    private List<AspectAdviceMethod> getBeforeMethodList(Method method) {
        return getAspectAdviceMethods(Before.class, mAspectPointcutMethodListMap, mBeforeMethodMap, method);
    }

    private static List<AspectAdviceMethod> getAspectAdviceMethods(Class<? extends Annotation> adviceClass, HashMap<Class<? extends Annotation>, List<AspectAdviceMethod>> dataMap, HashMap<Method, List<AspectAdviceMethod>> methodMap, Method method) {
        List<AspectAdviceMethod> aspectAdviceMethods = methodMap.get(method);
        if (null != aspectAdviceMethods) {
            return aspectAdviceMethods;
        }
        aspectAdviceMethods = new ArrayList<>();
        methodMap.put(method, aspectAdviceMethods);
        List<AspectAdviceMethod> methods = dataMap.get(adviceClass);
        if (null == method) {
            return aspectAdviceMethods;
        }
        for (AspectAdviceMethod adviceMethod : methods) {
            if (adviceMethod.match(method)) {
                aspectAdviceMethods.add(adviceMethod);
            }
        }
        sortAdviceList(aspectAdviceMethods);
        return aspectAdviceMethods;
    }
}
