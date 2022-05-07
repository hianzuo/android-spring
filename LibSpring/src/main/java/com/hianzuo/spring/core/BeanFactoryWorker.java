package com.hianzuo.spring.core;

import com.hianzuo.spring.annotation.Component;
import com.hianzuo.spring.internal.AnnotationUtil;
import com.hianzuo.spring.internal.StringUtil;
import com.hianzuo.spring.utils.AndroidSpringLog;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan
 *         On 2017/10/7.
 * @author Ryan
 */
@FactoryWorker(order = -1)
public class BeanFactoryWorker extends BaseFactoryWorker {

    @Override
    public List<InternalBean> onLoad(List<Class<?>> classList, List<InternalBean> oldList) {
        List<InternalBean> loadList = new ArrayList<>();
        for (Class<?> javaClass : classList) {
            if (isComponentClazz(javaClass)) {
                try {
                    List<InternalBean> beanList = initComponentBean(javaClass);
                    if (null != beanList) {
                        loadList.addAll(beanList);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return joinLoadList(oldList, loadList);
    }


    private List<InternalBean> initComponentBean(Class<?> beanClazz) {
        Class<?>[] interfaces = beanClazz.getInterfaces();
        List<InternalBean> list = new ArrayList<>();
        if (interfaces.length > 0) {
            for (Class<?> anInterface : interfaces) {
                String beanName = getComponentBeanName(anInterface, beanClazz);
                Object obj = newInstanceProxyClass(beanName, anInterface, beanClazz);
                list.add(new InternalBean(beanName, anInterface, obj));
            }
        } else {
            String beanName = getComponentBeanName(beanClazz, beanClazz);
            Object obj;
            try {
                obj = beanClazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            AndroidSpringLog.d("SpringInitializer initComponentBean beanName:" + beanName + ", class:" + beanClazz.getName());
            list.add(new InternalBean(beanName, null, obj));
        }
        return list;
    }

    private Object newInstanceProxyClass(String beanName, Class<?> anInterface, Class<?> beanClazz) {
        try {
            Object targetObj = getOrNewTargetObj(beanName, anInterface, beanClazz);
            return Proxy.newProxyInstance(targetObj.getClass().getClassLoader(), new Class<?>[]{anInterface}, new BeanProxyHandler(targetObj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getComponentBeanName(Class<?> anInterface, Class<?> beanClazz) {
        String beanName = AnnotationUtil.getLastValue(beanClazz, Component.class);
        if (StringUtil.isBlank(beanName)) {
            return anInterface.getSimpleName();
        } else {
            return beanName;
        }
    }

    private static boolean isComponentClazz(Class<?> c) {
        return AnnotationUtil.has(c, Component.class) && !c.isAnnotation();
    }

}
