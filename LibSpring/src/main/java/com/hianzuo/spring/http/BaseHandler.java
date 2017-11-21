package com.hianzuo.spring.http;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * @author Ryan
 * @date 2017/11/14.
 */

public abstract class BaseHandler {
    private static HashMap<Class<?>, Method> handleMethodMap = new HashMap<>();

    public <T> T execute() {
        Object handlerObject = getHandlerObject();
        Method method = handleMethodMap.get(handlerObject.getClass());
        if (null == method) {
            method = getHandleMethod(handlerObject);
            if (null == method) {
                throw new RuntimeException("handle method not exist in controller.");
            }
            handleMethodMap.put(getClass(), method);
        }
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
        //noinspection unchecked
        return (T) handleMethod.invoke(handlerObject, parameterObjects);
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

    protected abstract Object getMethodParamObject(String value);

    private Method getHandleMethod(Object handlerObject) {
        Method[] methods = handlerObject.getClass().getMethods();
        if (null == methods) {
            return null;
        }
        Method handleNameMethod = null;
        for (Method method : methods) {
            if (method.isAnnotationPresent(HandleMethod.class)) {
                return method;
            }
            if ("handle".equals(method.getName())) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                handleNameMethod = method;
            }
        }
        return handleNameMethod;
    }
}
