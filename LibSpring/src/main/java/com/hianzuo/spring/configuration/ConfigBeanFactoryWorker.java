package com.hianzuo.spring.configuration;


import com.hianzuo.spring.annotation.Bean;
import com.hianzuo.spring.annotation.Configuration;
import com.hianzuo.spring.core.BaseFactoryWorker;
import com.hianzuo.spring.core.FactoryWorker;
import com.hianzuo.spring.core.InternalBean;
import com.hianzuo.spring.internal.AnnotationUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Ryan
 * @date 2017/10/7.
 */

@FactoryWorker(order = 3)
public class ConfigBeanFactoryWorker extends BaseFactoryWorker {
    @Override
    public List<InternalBean> onLoad(List<Class<?>> classList, List<InternalBean> oldList) {
        List<InternalBean> loadList = new ArrayList<>();
        for (Class<?> javaClass : classList) {
            if (isConfigurationClazz(javaClass)) {
                try {
                    List<InternalBean> beanList = initConfigurationBean(javaClass);
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


    private static boolean isConfigurationClazz(Class<?> c) {
        return AnnotationUtil.has(c,Configuration.class) && !c.isAnnotation();
    }


    private List<InternalBean> initConfigurationBean(Class<?> javaClazz) throws Exception {
        Object configObj = getBeanObj("", javaClazz);
        if (null == configObj) {
            configObj = javaClazz.newInstance();
        }
        List<Method> configurationMethods = new ArrayList<>();
        Method[] declaredMethods = javaClazz.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            declaredMethod.setAccessible(true);
            configurationMethods.add(declaredMethod);
        }
        List<InternalBean> list = new ArrayList<>();
        for (Method method : configurationMethods) {
            Bean beanAn = method.getAnnotation(Bean.class);
            if (null == beanAn) {
                continue;
            }
            String beanName = beanAn.value();
            if ("".equals(beanName.trim())) {
                beanName = method.getName();
            }
            Object beanObj = method.invoke(configObj);
            list.add(new InternalBean(beanName, null, beanObj));
        }
        return list;
    }
}
