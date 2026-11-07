package com.bookmgt.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 日期工具类
 */
public class DateUtil {

    private DateUtil() {}

    /** 默认新书判断天数 */
    public static final int DEFAULT_NEW_BOOK_DAYS = 30;

    /**
     * 判断日期是否在指定天数内（用于判断"新书"等场景）
     * @param date    待判断的日期
     * @param days    天数范围
     * @return true=在 days 天内
     */
    public static boolean isWithinDays(LocalDateTime date, int days) {
        if (date == null) return false;
        return date.toLocalDate().isAfter(LocalDate.now().minusDays(days));
    }

    /**
     * 判断日期是否在默认 30 天内
     */
    public static boolean isWithinDefaultDays(LocalDateTime date) {
        return isWithinDays(date, DEFAULT_NEW_BOOK_DAYS);
    }

    /**
     * 计算两个日期之间的天数差
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的天数差（向下取整）
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }
}
