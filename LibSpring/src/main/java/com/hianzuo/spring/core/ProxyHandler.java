package com.hianzuo.spring.core;

import java.lang.reflect.InvocationHandler;

/**
 * @author Ryan
 * @date 2017/10/7.
 */
public interface ProxyHandler extends InvocationHandler {
    /**
     * 获取目标对象
     *
     * @return 目标对象
     */
    Object getTargetObj();
}
