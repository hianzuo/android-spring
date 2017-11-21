package com.hianzuo.spring.core;

/**
 * @author ryan
 * @date 2017/11/21.
 */

public interface BeanGetter {
    Object get(String beanName, Class<?> beanInterface);
}