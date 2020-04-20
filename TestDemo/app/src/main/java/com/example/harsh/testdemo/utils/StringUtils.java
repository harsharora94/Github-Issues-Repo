package com.example.harsh.testdemo.utils;

/**
 * Created by harsh on 1/21/2017.
 */

public class StringUtils {

    private static final String EMPTY = "";
    private static final String NULL = "null";

    public static boolean isEmpty(String s) {
        return null == s || EMPTY.equals(s.trim());
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }
}
