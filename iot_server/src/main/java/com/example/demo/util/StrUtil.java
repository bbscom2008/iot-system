package com.example.demo.util;

public class StrUtil {

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    public static boolean isBlank(String str) {
        return str != null && str.trim().isEmpty();
    }
    
}
