package com.hianzuo.spring.http;


import com.hianzuo.spring.exception.CheckMethodFailure;
import com.hianzuo.spring.internal.ReflectUtils;
import com.hianzuo.spring.internal.StringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

/**
 * @author Ryan
 * @date 2017/11/14.
 */

public abstract class BaseHandler {
    private static HashMap<Class<?>, CallMethod> handleMethodMap = new HashMap<>();
    private static HashMap<Class<?>, CallMethod> checkMethodMap = new HashMap<>();

    public <T> T execute() {
        Object handlerObject = getHandlerObject();
        CallMethod checkMethod = getMethod(handlerObject, checkMethodMap,
                CheckMethod.class, "check");
        if (!checkMethod.isNull()) {
            Object obj = callMethod(handlerObject, checkMethod.target);
            if (!isEmptyObj(obj)) {
                throw new CheckMethodFailure(obj);
            }
        }
        CallMethod handleMethod = getMethod(handlerObject, handleMethodMap,
                HandleMethod.class, "handle");
        if (handleMethod.isNull()) {
            throw new RuntimeException("no handle method found in handler.");
        }
        return callMethod(handlerObject, handleMethod.target);
    }

    private boolean isEmptyObj(Object obj) {
        if (null == obj) {
            return true;
        }
        if (obj instanceof String) {
            return StringUtil.isBlank(obj);
        }
        return false;
    }

    private static CallMethod getMethod(Object handlerObject, HashMap<Class<?>, CallMethod> methodMap,
                                        Class<? extends Annotation> methodType, String defMethodName) {
        Class<?> handlerClass = handlerObject.getClass();
        CallMethod method = methodMap.get(handlerClass);
        if (null == method) {
            method = getMethod(handlerObject, methodType, defMethodName);
            methodMap.put(handlerClass, method);
        }
        return method;
    }

    private static CallMethod getMethod(Object handlerObject, Class<? extends Annotation> methodType, String defMethodName) {
        List<Method> methods = ReflectUtils.getMethods(handlerObject.getClass());
        if (null == methods || methods.isEmpty()) {
            return null;
        }
        Method handleNameMethod = null;
        for (Method method : methods) {
            if (method.isAnnotationPresent(methodType)) {
                return new CallMethod(method);
            }
            if (defMethodName.equals(method.getName())) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                handleNameMethod = method;
            }
        }
        return new CallMethod(handleNameMethod);
    }

    private <T> T callMethod(Object handlerObject, Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] parameterObjects = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            Annotation[] parameterAnnotation = parameterAnnotations[i];
            parameterObjects[i] = getParameterObject(parameterType, parameterAnnotation);
        }
        try {
            return invokeHandleMethod(handlerObject, method, parameterObjects);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Object getHandlerObject() {
        return this;
    }

    private <T> T invokeHandleMethod(Object handlerObject, Method handleMethod, Object[] parameterObjects) throws Exception {
        try {
            //noinspection unchecked
            return (T) handleMethod.invoke(handlerObject, parameterObjects);
        } catch (InvocationTargetException e) {
            Throwable throwable = e.getTargetException();
            if (null != throwable && throwable instanceof Exception) {
                throw (Exception) throwable;
            }
            throw e;
        }
    }

    private Object getParameterObject(Class<?> parameterType, Annotation[] parameterAnnotation) {
        MethodParam param = null;
        for (Annotation annotation : parameterAnnotation) {
            if (annotation.annotationType() == MethodParam.class) {
                param = (MethodParam) annotation;
            }
        }
        Object obj = null;
        boolean isMethodParam = false;
        if (null != param) {
            isMethodParam = true;
            obj = getMethodParamObject(param.value());
        }
        if (null == obj) {
            obj = getMethodParamObjectByType(parameterType);
            if (null == obj && !isMethodParam) {
                throw new RuntimeException("the type[" + parameterType.getName()
                        + "] cannot support or no @MethodParam for it");
            }
        }
        return obj;
    }

    protected Object getMethodParamObjectByType(Class<?> type) {
        return null;
    }

    protected Object getMethodParamObject(String value) {
        return null;
    }

    private static class CallMethod {
        private Method target;

        CallMethod(Method target) {
            this.target = target;
        }

        public boolean isNull() {
            return null == target;
        }
    }

}
