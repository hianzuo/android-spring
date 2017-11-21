package com.hianzuo.spring.internal;

/**
 * @author ryan
 * @date 2017/11/21.
 */

public class StringUtil {
    public static boolean isBlank(Object o) {
        if (null == o) {
            return true;
        }
        if (!(o instanceof String)) {
            return false;
        }
        String str = (String) o;
        int strLen;
        if ((strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
