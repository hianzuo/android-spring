package com.hianzuo.spring.di;

import com.hianzuo.spring.annotation.Resource;
import com.hianzuo.spring.core.BaseFactoryWorker;
import com.hianzuo.spring.core.FactoryWorker;
import com.hianzuo.spring.core.InternalBean;
import com.hianzuo.spring.internal.ReflectUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Ryan
 * @date 2017/10/7.
 */
@FactoryWorker(order = 2)
public class DiFactoryWorker extends BaseFactoryWorker {
    @Override
    public List<InternalBean> onBeanCreatedDone(List<Class<?>> classList, List<InternalBean> beanList) {
        this.initFactoryWorker(beanList);
        return onLoad(classList, beanList);
    }

    @Override
    public List<InternalBean> onLoad(List<Class<?>> classList, List<InternalBean> oldList) {
        for (InternalBean bean : oldList) {
            Object targetObj = bean.getTargetObj();
            Class<?> objClass = targetObj.getClass();
            List<Field> listFields = ReflectUtils.getFields(objClass);
            for (Field field : listFields) {
                Resource resource = field.getAnnotation(Resource.class);
                if (null != resource) {
                    Object beanObj = getBeanObj(resource.beanName(), field.getType());
                    if (null != beanObj) {
                        try {
                            field.setAccessible(true);
                            field.set(targetObj, beanObj);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

        }
        return oldList;
    }

}
