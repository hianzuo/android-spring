package com.hianzuo.spring.core;

import java.lang.reflect.Method;

/**
 * @author Ryan
 *         On 2017/10/5.
 */
class BeanProxyHandler implements ProxyHandler {
    private Object targetObj;

    public BeanProxyHandler(Object targetObj) {
        this.targetObj = targetObj;
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
        return method.invoke(targetObj, args);
    }
}
