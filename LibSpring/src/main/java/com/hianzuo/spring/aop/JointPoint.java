package com.hianzuo.spring.aop;

import java.lang.reflect.Method;

/**
 * @author Ryan
 * On 2017/10/6.
 */
public class JointPoint {
    private Object proxy;
    private Method method;
    private Object[] args;
    private Object targetObj;
    private Object result;

    public Object getProxy() {
        return proxy;
    }

    public JointPoint setProxy(Object proxy) {
        this.proxy = proxy;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public JointPoint setMethod(Method method) {
        this.method = method;
        return this;
    }

    public Object[] getArgs() {
        return args;
    }

    public JointPoint setArgs(Object[] args) {
        this.args = args;
        return this;
    }

    public Object getTargetObj() {
        return targetObj;
    }

    public JointPoint setTargetObj(Object targetObj) {
        this.targetObj = targetObj;
        return this;
    }

    public Object getResult() {
        return result;
    }

    public JointPoint setResult(Object result) {
        this.result = result;
        return this;
    }

    public Object invokeResult() {
        try {
            return method.invoke(targetObj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public JointPoint copy() {
        JointPoint jp = new JointPoint();
        jp.proxy = this.proxy;
        jp.method = this.method;
        jp.args = this.args;
        jp.targetObj = this.targetObj;
        jp.result = this.result;
        return jp;
    }
}
