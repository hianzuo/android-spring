package com.hianzuo.spring.simple.impl;


import com.hianzuo.spring.annotation.Resource;
import com.hianzuo.spring.annotation.Service;
import com.hianzuo.spring.simple.BeanTest;
import com.hianzuo.spring.simple.PrintService;
import com.hianzuo.spring.simple.TestService;

/**
 * @author Ryan
 *         On 2017/10/5.
 */
@Service
public class TestServiceImpl implements TestService {
    @Resource
    private PrintService printService;

    @Resource(beanName = "testBean")
    private BeanTest testBean;

    @Resource(beanName = "methodIsBeanName")
    private BeanTest testBean1;

    @Override
    public void handle() {
        printService.print();
        System.out.println("AAA BeanTest :" + testBean.getText());
        System.out.println("AAA BeanTest1 :" + testBean1.getText());
        System.out.println("AAA TestService.handle.");
    }

    @Override
    public void execute() {
        System.out.println("AAA TestService.execute.");
    }
}
