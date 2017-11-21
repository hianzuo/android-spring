package com.hianzuo.spring.simple.test.impl;


import com.hianzuo.spring.annotation.Service;
import com.hianzuo.spring.simple.test.PrintService;

/**
 * @author Ryan
 *         On 2017/10/6.
 */
@Service
public class PrintServiceImpl implements PrintService {
    @Override
    public String print() {
        return "Print Line str";
    }
}
