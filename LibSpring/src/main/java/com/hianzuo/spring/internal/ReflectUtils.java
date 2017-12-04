package com.hianzuo.spring.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectUtils {

    public static List<Field> getFields(Class<?> c) {
        List<Field> fields = new ArrayList<>();
        getFields(c, fields);
        return fields;
    }

    private static List<Field> getFields(Class<?> c, List<Field> list) {
        if (c == Object.class) {
            return list;
        } else {
            Field[] fs = c.getDeclaredFields();
            list.addAll(Arrays.asList(fs));
            return getFields(c.getSuperclass(), list);
        }
    }

    public static List<Method> getMethods(Class<?> c) {
        List<Method> methods = new ArrayList<>();
        getMethods(c, methods);
        return methods;
    }

    private static List<Method> getMethods(Class<?> c, List<Method> list) {
        if (c == Object.class) {
            return list;
        } else {
            Method[] ms = c.getDeclaredMethods();
            list.addAll(Arrays.asList(ms));
            return getMethods(c.getSuperclass(), list);
        }
    }

    public static void printFields(List<Field> fields) {
        for (Field field : fields) {
            System.out.println(field.toString());
        }
    }

    public static void printMethods(List<Method> methods) {
        for (Method method : methods) {
            System.out.println(method.toString());
        }
    }
}
