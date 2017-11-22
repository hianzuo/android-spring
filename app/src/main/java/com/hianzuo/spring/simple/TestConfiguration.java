package com.hianzuo.spring.simple;


import com.hianzuo.spring.annotation.Bean;
import com.hianzuo.spring.annotation.Component;
import com.hianzuo.spring.annotation.Configuration;

/**
 * @author Ryan
 *         On 2017/10/6.
 */
@Component
@Configuration
public class TestConfiguration {

    @Bean("testBean")
    public BeanTest bean1() {
        return new BeanTest("bean name in annotation");
    }

    @Bean
    public BeanTest methodIsBeanName() {
        return new BeanTest("method is bean name");
    }
}
