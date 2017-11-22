package com.hianzuo.spring.simple.test.cache;

/**
 * @author ryan
 * @date 2017/11/22.
 */

public class Demo {
    private Integer id;
    private String value;

    public Demo(Integer id, String value) {
        this.id = id;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    private String getValue(){
        return value;
    }
}
