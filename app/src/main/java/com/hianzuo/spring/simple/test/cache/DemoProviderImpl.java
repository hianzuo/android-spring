package com.hianzuo.spring.simple.test.cache;


import com.hianzuo.spring.annotation.Component;
import com.hianzuo.spring.cache.AbstractCacheAble;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ryan
 * @date 2017/11/22.
 */
@Component
public class DemoProviderImpl extends AbstractCacheAble<Integer, Demo> {
    @Override
    protected Integer getKey(Demo demo) {
        return demo.getId();
    }

    @Override
    protected List<Demo> loadData() {
        //load Demo data from remote server or database
        ArrayList<Demo> list = new ArrayList<>();
        list.add(new Demo(1, "aaa"));
        list.add(new Demo(2, "bbb"));
        return list;
    }
}
