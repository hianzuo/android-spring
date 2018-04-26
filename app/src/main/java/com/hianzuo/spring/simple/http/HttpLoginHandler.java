package com.hianzuo.spring.simple.http;

import com.hianzuo.spring.annotation.Handler;
import com.hianzuo.spring.annotation.Resource;
import com.hianzuo.spring.http.HandleMethod;
import com.hianzuo.spring.simple.LoginService;
import com.hianzuo.spring.simple.model.LoginData;

/**
 * @author ryan
 * @date 2017/11/22.
 */

@Handler("/api/login")
public class HttpLoginHandler extends BaseTestHandler {

    @Resource
    private LoginService loginService;

    //you can use @MethodParam Annotation to get parameter
    /*@HandleMethod
    public void handle(@MethodParam("username") String username, @MethodParam("password") String password) {
        loginService.login(username, password);
    }*/

    /**
     * you can get DataModel in Method Param , register in (Object getMethodParamObjectByType(Class<?> type))
     * @param data
     */
    @HandleMethod
    public void handle(LoginData data) {
        loginService.login(data.getUsername(), data.getPassword());
    }
}
