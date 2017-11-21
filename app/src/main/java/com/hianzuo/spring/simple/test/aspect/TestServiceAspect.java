package com.hianzuo.spring.simple.test.aspect;

import com.hianzuo.spring.aop.After;
import com.hianzuo.spring.aop.Around;
import com.hianzuo.spring.aop.Aspect;
import com.hianzuo.spring.aop.Before;
import com.hianzuo.spring.aop.JointPoint;
import com.hianzuo.spring.aop.Pointcut;

/**
 * @author Ryan
 * On 2017/10/5.
 */
@Aspect
public class TestServiceAspect {

    @Pointcut("^.*?handle\\(\\).*+$")
    public void handle() {
        System.out.println("AAA TestServiceAspect handle");
    }

    @Before("handle")
    public void before(JointPoint point) {
        System.out.println("AAA TestServiceAspect before");
    }

    @Around(value = "handle")
    public Object around(JointPoint point) {
        System.out.println("AAA TestServiceAspect around start");
        Object result = point.invokeResult();
        System.out.println("AAA TestServiceAspect around end");
        return result;
    }

    @After(value = "handle")
    public void after(JointPoint point) {
        System.out.println("AAA TestServiceAspect after");
    }
}
