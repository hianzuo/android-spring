package com.hianzuo.spring.aop;

import java.lang.reflect.Method;

/**
 * @author Ryan
 * On 2017/10/6.
 */
class AspectAdviceMethod {
    private String pointMethodName;
    private int pointMethodOrder;
    protected Object aspectObj;
    protected Method pointcutMethod;
    private Pointcut pointcut;

    public AspectAdviceMethod(String pointMethodName, int pointMethodOrder,Object aspectObj, Method pointcutMethod) {
        this.pointMethodName = pointMethodName;
        this.pointMethodOrder = pointMethodOrder;
        this.aspectObj = aspectObj;
        this.pointcutMethod = pointcutMethod;
        try {
            pointcut = aspectObj.getClass().getMethod(pointMethodName).getAnnotation(Pointcut.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Object invoke(JointPoint point) {
        try {
            return pointcutMethod.invoke(aspectObj, point);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean match(Method method) {
        String methodName = method.toString();
        if (methodName.matches(pointcut.value())) {
            return true;
        }
        return false;
    }

    public int getPointMethodOrder() {
        return pointMethodOrder;
    }
}
