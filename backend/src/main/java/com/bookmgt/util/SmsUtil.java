package com.bookmgt.util;

import cn.hutool.core.util.RandomUtil;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 短信验证码工具类
 */
public class SmsUtil {

    private static final String SMS_PREFIX = "sms:";
    /** 验证码有效期（分钟） */
    private static final int CODE_EXPIRE_MINUTES = 5;

    private SmsUtil() {}

    /**
     * 校验手机号格式（1[3-9]开头 + 9位数字）
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 生成 6 位验证码并存入 Redis
     * @return 生成的验证码
     */
    public static String generateAndStore(StringRedisTemplate redis, String phone) {
        String code = RandomUtil.randomNumbers(6);
        redis.opsForValue().set(SMS_PREFIX + phone, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        System.out.println("【短信验证码】手机号: " + phone + " 验证码: " + code);
        return code;
    }

    /**
     * 校验验证码（正确则自动删除）
     * @return true=验证通过
     */
    public static boolean verify(StringRedisTemplate redis, String phone, String code) {
        String cacheCode = redis.opsForValue().get(SMS_PREFIX + phone);
        if (cacheCode == null || !cacheCode.equals(code)) {
            return false;
        }
        redis.delete(SMS_PREFIX + phone);
        return true;
    }

    /**
     * 仅校验但不删除（用于分步流程，第一步校验，第二步消费）
     * @return true=验证通过
     */
    public static boolean checkOnly(StringRedisTemplate redis, String phone, String code) {
        String cacheCode = redis.opsForValue().get(SMS_PREFIX + phone);
        return cacheCode != null && cacheCode.equals(code);
    }

    /**
     * 消费验证码（第二步删除）
     */
    public static void consume(StringRedisTemplate redis, String phone) {
        redis.delete(SMS_PREFIX + phone);
    }
}
