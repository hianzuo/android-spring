package com.hianzuo.spring.internal;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan
 * @date 2017/11/14.
 */
public class AnnotationUtil {

    public static List<Annotation> get(Class<?> c, Class<? extends Annotation> anC) {
        Annotation annotation = c.getAnnotation(anC);
        if (null != annotation) {
            List<Annotation> list = new ArrayList<>();
            list.add(annotation);
            return list;
        }
        return getInternal(c, anC);
    }

    private static List<Annotation> getInternal(Class<?> c, Class<? extends Annotation> anC) {
        Annotation[] annotations = c.getAnnotations();
        for (Annotation an : annotations) {
            Class<? extends Annotation> annotationType = an.annotationType();
            if (isBaseAnnotation(annotationType)) {
                continue;
            }
            if (anC == annotationType) {
                List<Annotation> list = new ArrayList<>();
                list.add(an);
                return list;
            } else {
                List<Annotation> list = getInternal(annotationType, anC);
                if (null != list) {
                    list.add(an);
                }
                return list;
            }
        }
        return null;
    }

    @SafeVarargs
    public static boolean has(Class<?> c, Class<? extends Annotation>... anCs) {
        for (Class<? extends Annotation> anC : anCs) {
            if(c.isAnnotationPresent(anC) || hasInternal(c, anC)){
                return true;
            }
        }
        return false;
    }

    private static boolean hasInternal(Class<?> c, Class<? extends Annotation> anC) {
        Annotation[] annotations = c.getAnnotations();
        for (Annotation an : annotations) {
            Class<? extends Annotation> annotationType = an.annotationType();
            if (isBaseAnnotation(annotationType)) {
                continue;
            }
            return anC == annotationType || hasInternal(annotationType, anC);
        }
        return false;
    }

    private static boolean isBaseAnnotation(Class<? extends Annotation> annotationType) {
        return
                Documented.class == annotationType ||
                        Inherited.class == annotationType ||
                        Retention.class == annotationType ||
                        Target.class == annotationType
                ;
    }

    public static String joinValue(List<Annotation> annotations) {
        return join(annotations, "value", false);
    }

    public static <T> T getLastValue(List<Annotation> annotations) {
        return getLast(annotations, "value");
    }

    public static <T> T getLastValue(Class<?> c, Class<? extends Annotation> annotationClass) {
        List<Annotation> annotations = get(c, annotationClass);
        if (null == annotations) {
            return null;
        }
        return getLast(annotations, "value");
    }


    public static <T> T getLast(List<Annotation> annotations, String method) {
        return get(annotations, method, true);
    }

    public static <T> T get(List<Annotation> annotations, String method, boolean last) {
        if (null == annotations || annotations.isEmpty()) {
            return null;
        }
        int index = 0;
        int step = 1;
        int size = annotations.size();
        if (last) {
            index = size - 1;
            step = -1;
        }
        Object retObj = null;
        for (; index >= 0 && index < size; index += step) {
            if (index < 0 || index >= size) {
                continue;
            }
            Annotation annotation = annotations.get(index);
            retObj = callMethod(annotation, method);
            if (null == retObj || StringUtil.isBlank(retObj)) {
                continue;
            }
            break;
        }
        //noinspection unchecked
        return (T) retObj;
    }

    public static String join(List<Annotation> annotations, String method, boolean last) {
        if (null == annotations || annotations.isEmpty()) {
            return null;
        }
        int index = 0;
        int step = 1;
        int size = annotations.size();
        if (last) {
            index = size - 1;
            step = -1;
        }
        StringBuilder sb = new StringBuilder();
        for (; index >= 0 && index < size; index += step) {
            if (index < 0 || index >= size) {
                continue;
            }
            Annotation annotation = annotations.get(index);
            sb.append(callMethod(annotation, method));
        }
        return sb.toString();
    }

    private static Object callMethod(Annotation annotation, String methodName) {
        try {
            Method method = annotation.annotationType().getMethod(methodName);
            return method.invoke(annotation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*public static void main(String[] args) {
        System.out.println("-------:");
        List<Annotation> annotations = new ArrayList<>();
        annotations.add(null);
        annotations.add(null);
        annotations.add(null);
        annotations.add(null);
        annotations.add(null);
        boolean last = true;
        int index = 0;
        int step = 1;
        int size = annotations.size();
        if (last) {
            index = size - 1;
            step = -1;
        }
        StringBuilder sb = new StringBuilder();
        for (; index >= 0 || index < size; index += step) {
            if (index == -1) {
                continue;
            }
            if (index == size) {
                continue;
            }
            Annotation annotation = annotations.get(index);
            System.out.println("-----");
        }
    }

    @Service("aaa")
    public static class Test {

    }*/
}
