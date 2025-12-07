package com.example.demo.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间处理工具类
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_HH_MM = "HH:mm";

    public static final String TIME_ZONE = "GMT+8";
    /**
     * 日期时间格式
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取今天的开始时间 （00:00:00）
     *
     * @return 今天的开始时间
     */
    public static LocalDateTime getStartOfDay() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    }

    /**
     * 获取昨天日期
     *
     * @return 今天的开始时间
     */
    public static String getYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return yesterday.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    /**
     * 获取昨天日期
     *
     * @return 今天的开始时间
     */
    public static LocalDate getYesterdayLocalDate() {
        LocalDate today = LocalDate.now();
        // 获取昨日日期 （减去 1 天）
        return today.minusDays(1);

    }


    /**
     * 获取当前时间
     *
     * @return 当前时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取当天时间的分钟 返回字符串
     */
    public static String getCurrentTime() {
        // 1. 获取当前时间（含分钟）
        LocalTime currentTime = LocalTime.now();
        // 2. 直接格式化为"HH:mm"（自动处理分钟补零）
        return currentTime.format(DateTimeFormatter.ofPattern(DATE_FORMAT_HH_MM));
    }

    /**
     * 获取偏移天数后的结束时间（如近7天的结束时间）
     *
     * @param days 偏移天数
     * @return 偏移后的日期结束时间
     */
    public static LocalDateTime getEndOfDayWithOffset(int days) {
        LocalDate targetDate = LocalDate.now().minusDays(days);
        return LocalDateTime.of(targetDate, LocalTime.of(23, 59, 59));
    }


    /**
     * 验证时间格式是否为 HH:mm 格式
     *
     * @param time 时间字符串
     * @return 是否为有效的时间格式
     */
    public  static boolean isValidTimeFormat(String time) {
        if (StrUtil.isEmpty(time)) {
            return false;
        }

        String[] parts = time.split(":");
        if (parts.length != 2) {
            return false;
        }

        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            return hour >= 0 && hour <= 24 && minute >= 0 && minute <= 59;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
