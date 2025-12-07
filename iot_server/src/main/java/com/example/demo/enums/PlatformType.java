package com.example.demo.enums;

/**
 * 平台类型枚举：统一管理 web/mobile 字符串常量
 */
public enum PlatformType {
    WEB("web"),
    MOBILE("mobile");

    private final String value;

    PlatformType(String value) {
        this.value = value;
    }

    /**
     * 获取字符串值（与数据库、JWT中存储保持一致）
     */
    public String getValue() {
        return value;
    }

    /**
     * 由字符串安全转换为枚举，容错处理未知/空值
     */
    public static PlatformType from(String value) {
        if (value == null) {
            return MOBILE; // 默认按手机端
        }
        String v = value.trim().toLowerCase();
        switch (v) {
            case "web":
                return WEB;
            case "mobile":
                return MOBILE;
            default:
                return MOBILE;
        }
    }
}
