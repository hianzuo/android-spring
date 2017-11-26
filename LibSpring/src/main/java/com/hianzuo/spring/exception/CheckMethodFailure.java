package com.hianzuo.spring.exception;

/**
 * @author Ryan
 * @date 2017/11/26.
 */

public class CheckMethodFailure extends RuntimeException {
    private Object obj;

    public CheckMethodFailure(Object obj) {
        this.obj = obj;
    }

    public <T> T getFailure(){
        //noinspection unchecked
        return (T) obj;
    }

}
