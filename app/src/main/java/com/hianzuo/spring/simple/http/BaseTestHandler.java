package com.hianzuo.spring.simple.http;

import com.hianzuo.spring.http.BaseHandler;
import com.hianzuo.spring.simple.model.LoginData;

/**
 * @author Ryan
 * @date 2018/4/26.
 */
public class BaseTestHandler extends BaseHandler {
    @Override
    protected Object getMethodParamObjectByType(Class<?> type) {
        if (type == LoginData.class) {
            String username = getMethodParamObject("username");
            String password = getMethodParamObject("password");
            return new LoginData(username, password);
        }
        return super.getMethodParamObjectByType(type);
    }

    @Override
    protected <T> T getMethodParamObject(String value) {
        // get value from request.
        // demo request.getParameter(value);

        //noinspection unchecked
        return super.getMethodParamObject(value);
    }

    @Override
    protected Object caseValueToType(Class<?> type, CharSequence value) {
        // you can custom your type here.
        return super.caseValueToType(type, value);
    }
}
