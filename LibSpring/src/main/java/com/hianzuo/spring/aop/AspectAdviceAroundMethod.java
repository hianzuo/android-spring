package com.hianzuo.spring.aop;

import java.lang.reflect.Method;

/**
 * @author Ryan
 * On 2017/10/6.
 */
class AspectAdviceAroundMethod extends AspectAdviceMethod {
    private AspectAdviceAroundMethod method;

    public AspectAdviceAroundMethod(String pointMethodName, int pointMethodOrder, Object aspectObj, Method pointcutMethod) {
        super(pointMethodName, pointMethodOrder, aspectObj, pointcutMethod);
    }

    @Override
    public Object invoke(JointPoint point) {
        if (null != method) {
            return super.invoke(point.copy().setMethod(method.pointcutMethod).setTargetObj(method.aspectObj).setArgs(new Object[]{point}));
        } else {
            return super.invoke(point);
        }
    }

    public AspectAdviceAroundMethod setNextMethod(AspectAdviceAroundMethod method) {
        this.method = method;
        return this;
    }
}
