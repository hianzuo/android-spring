package com.hianzuo.spring.simple.impl;

import android.util.Log;

import com.hianzuo.spring.annotation.Service;
import com.hianzuo.spring.simple.LoginService;

/**
 * @author Ryan
 * @date 2018/4/26.
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Override
    public void login(String username, String password) {
        Log.d("LoginService", "user [" + username + "] is logined. ");
    }
}
