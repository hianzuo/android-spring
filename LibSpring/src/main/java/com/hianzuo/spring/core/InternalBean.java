package com.hianzuo.spring.core;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * @author Ryan
 * @date 2017/10/7.
 */
public class InternalBean {
    private String name;
    private Class<?> beanInterface;
    private Object obj;

    public InternalBean(String name, Class<?> beanInterface, Object obj) {
        this.name = null != name ? (String.valueOf(name.charAt(0)).toLowerCase() + name.substring(1)) : null;
        this.beanInterface = beanInterface;
        this.obj = obj;
    }

    public InternalBean(String name, Object obj) {
        this(name, null, obj);
    }

    public InternalBean(Class<?> beanInterface, Object obj) {
        this(null, beanInterface, obj);
    }

    public String getName() {
        return name;
    }

    public Class<?> getBeanInterface() {
        return beanInterface;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }


    public Object getTargetObj() {
        return getTargetObj(getObj());
    }

    private Object getTargetObj(Object obj) {
        Class<?> aClass = obj.getClass();
        if (Proxy.isProxyClass(aClass)) {
            Proxy proxy = ((Proxy) obj);
            ProxyHandler handler = getProxyHandler(proxy);
            return getTargetObj(handler.getTargetObj());
        } else {
            return obj;
        }
    }

    private static Field mProxyHField;

    private ProxyHandler getProxyHandler(Proxy proxy) {
        if (null == mProxyHField) {
            try {
                mProxyHField = Proxy.class.getDeclaredField("h");
                mProxyHField.setAccessible(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return (ProxyHandler) mProxyHField.get(proxy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
