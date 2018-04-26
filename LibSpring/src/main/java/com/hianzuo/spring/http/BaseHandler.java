package com.hianzuo.spring.http;


import com.hianzuo.spring.exception.CheckMethodFailure;
import com.hianzuo.spring.internal.ReflectUtils;
import com.hianzuo.spring.internal.StringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan
 * @date 2017/11/14.
 */

public abstract class BaseHandler {
    private static HashMap<Class<?>, CallMethod> handleMethodMap = new HashMap<>();
    private static HashMap<Class<?>, CallMethod> checkMethodMap = new HashMap<>();
    private Map<String, String> mQueryMap;

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
        for (Method method : methods) {
            if (method.isAnnotationPresent(methodType)) {
                return new CallMethod(method);
            }
            if (Modifier.isAbstract(method.getModifiers())) {
                continue;
            }
            if (Modifier.isNative(method.getModifiers())) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (defMethodName.equals(method.getName())) {
                return new CallMethod(method);
            }
        }
        return new CallMethod(null);
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
            obj = getMethodParamObject(parameterType, param.value());
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

    private Object tryToCaseToType(Class<?> type, CharSequence value) {
        String valueStr = value.toString();
        try {
            Method valueOfMethod = type.getMethod("valueOf", String.class);
            valueOfMethod.setAccessible(true);
            return valueOfMethod.invoke(null, valueStr);
        } catch (Exception e) {
            try {
                Constructor<?> constructor = type.getConstructor(String.class);
                return constructor.newInstance(valueStr);
            } catch (Exception e1) {
                return value;
            }
        }
    }


    private Class<?> getRefType(Class<?> type) {
        if (byte.class == type) {
            return Byte.class;
        } else if (int.class == type) {
            return Integer.class;
        } else if (char.class == type) {
            return Character.class;
        } else if (long.class == type) {
            return Long.class;
        } else if (boolean.class == type) {
            return Boolean.class;
        } else if (double.class == type) {
            return Double.class;
        } else if (short.class == type) {
            return Short.class;
        } else if (float.class == type) {
            return Float.class;
        }
        return type;
    }

    private Object getMethodParamObject(Class<?> type, String value) {
        Object obj = getMethodParamObject(value);
        if (null == obj) {
            return null;
        }
        if (obj.getClass().isAssignableFrom(type)) {
            return obj;
        }
        if (obj instanceof CharSequence) {
            caseValueToType(type, value);
        }
        if (!obj.getClass().isAssignableFrom(type)) {
            throw new RuntimeException("the @MethodParam[" + value + "] expect type[" + type.getName()
                    + "] but get [" + obj.getClass().getName() + "]");
        }
        return obj;
    }

    protected Object caseValueToType(Class<?> type, CharSequence value) {
        if (type.isPrimitive()) {
            type = getRefType(type);
        }
        return tryToCaseToType(type, value);
    }

    public BaseHandler setParameterMap(Map<String, String> queryMap) {
        this.mQueryMap = queryMap;
        return this;
    }

    protected <T> T getMethodParamObject(String value) {
        return (T) mQueryMap.get(value);
    }

    private static class CallMethod {
        private Method target;

        CallMethod(Method target) {
            this.target = target;
            if (null != target) {
                this.target.setAccessible(true);
            }
        }

        public boolean isNull() {
            return null == target;
        }
    }

}
