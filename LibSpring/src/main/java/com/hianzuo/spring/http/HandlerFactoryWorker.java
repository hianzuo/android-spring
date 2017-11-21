package com.hianzuo.spring.http;

import com.hianzuo.spring.annotation.Handler;
import com.hianzuo.spring.core.BaseFactoryWorker;
import com.hianzuo.spring.core.FactoryWorker;
import com.hianzuo.spring.core.InternalBean;
import com.hianzuo.spring.internal.AnnotationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan
 *         On 2017/10/7.
 * @author Ryan
 */
@FactoryWorker(order = 4)
public class HandlerFactoryWorker extends BaseFactoryWorker {

    @Override
    public List<InternalBean> onLoad(List<Class<?>> classList, List<InternalBean> oldList) {
        List<InternalBean> loadList = new ArrayList<>();
        for (Class<?> c : classList) {
            if (AnnotationUtil.has(c, Handler.class) && !c.isAnnotation()) {
                try {
                    List<InternalBean> beanList = initComponentBean(c);
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

    private List<InternalBean> initComponentBean(Class<?> c) {
        List<InternalBean> list = new ArrayList<>();
        list.add(new InternalBean(c, c));
        return list;
    }

}
