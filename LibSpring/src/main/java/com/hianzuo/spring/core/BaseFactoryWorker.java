package com.hianzuo.spring.core;

import com.hianzuo.spring.utils.AndroidSpringLog;

import java.util.HashMap;
import java.util.List;

/**
 * @author Ryan
 * @date 2017/10/7.
 */
public abstract class BaseFactoryWorker {

    private HashMap<String, InternalBean> beanNameMap;
    private HashMap<Class<?>, InternalBean> beanClassMap;
    private BeanGetter beanGetter;

    /**
     * 加载功能forBean
     *
     * @param classList classList
     * @param oldList   oldList
     * @return List<InternalBean>
     */
    List<InternalBean> onLoad(List<Class<?>> classList, List<InternalBean> oldList, BeanGetter beanGetter) {
        try {
            this.beanGetter = beanGetter;
            this.initFactoryWorker(oldList);
            return onLoad(classList, oldList);
        } finally {
            this.beanGetter = null;
        }
    }

    List<InternalBean> onBeanCreatedDone(List<Class<?>> classList, List<InternalBean> oldList, BeanGetter beanGetter) {
        try {
            this.beanGetter = beanGetter;
            return onBeanCreatedDone(classList, oldList);
        } finally {
            this.beanGetter = null;
        }
    }

    protected List<InternalBean> onBeanCreatedDone(List<Class<?>> classList, List<InternalBean> beanList) {
        return beanList;
    }


    protected abstract List<InternalBean> onLoad(List<Class<?>> classList, List<InternalBean> oldList);

    protected void initFactoryWorker(List<InternalBean> oldList) {
        beanNameMap = new HashMap<>(128);
        beanClassMap = new HashMap<>(128);
        for (InternalBean bean : oldList) {
            beanClassMap.put(bean.getObj().getClass(), bean);
            if (null != bean.getBeanInterface()) {
                beanClassMap.put(bean.getBeanInterface(), bean);
            }
            if (null != bean.getName() && bean.getName().length() > 0) {
                beanNameMap.put(bean.getName(), bean);
            }
        }
    }

    protected Object getBeanObj(String beanName, Class<?> beanInterface) {
        if (null != beanGetter) {
            return beanGetter.get(beanName, beanInterface);
        }
        InternalBean bean = getInternalBean(beanName, beanInterface);
        if (null != bean) {
            return bean.getObj();
        }
        return null;
    }


    Object getOrNewTargetObj(String beanName, Class<?> anInterface, Class<?> beanClazz) {
        Object beanObj = getBeanObj(beanName, anInterface);
        if (null != beanObj) {
            return beanObj;
        }
        try {
            AndroidSpringLog.d("SpringInitializer getOrNewTargetObj beanName:" +
                    beanName + ", class:" + beanClazz.getName());
            return beanClazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private InternalBean getInternalBean(String beanName, Class<?> beanInterface) {
        if (null != beanName && beanName.length() > 0) {
            InternalBean bean = beanNameMap.get(beanName);
            if (null != bean) {
                return bean;
            }
        }
        if (null != beanInterface) {
            return beanClassMap.get(beanInterface);
        }
        return null;
    }

    protected List<InternalBean> joinLoadList(List<InternalBean> oldList, List<InternalBean> loadList) {
        for (InternalBean bean : loadList) {
            InternalBean oldBean = getInternalBean(bean.getName(), bean.getBeanInterface());
            if (null == oldBean) {
                oldList.add(bean);
            } else {
                oldBean.setObj(bean.getObj());
            }
        }
        return oldList;
    }


}
