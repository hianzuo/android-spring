package com.hianzuo.spring.simple.test.model;

/**
 * @author ryan
 * @date 2017/11/22.
 */

public class LoginData {
    private String username;
    private String password;

    public LoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
