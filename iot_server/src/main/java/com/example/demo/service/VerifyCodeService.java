package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务
 * 注意：这是模拟实现，生产环境应该使用真实的短信服务（如阿里云短信、腾讯云短信等）
 */
@Slf4j
@Service
public class VerifyCodeService {

    // 使用内存存储验证码（生产环境应该使用 Redis）
    private final Map<String, VerifyCodeInfo> verifyCodeMap = new ConcurrentHashMap<>();

    /**
     * 发送验证码
     */
    public void sendVerifyCode(String phone) {
        // 生成6位随机验证码
        String code = generateCode();
        
        // 保存验证码（5分钟有效期）
        VerifyCodeInfo info = new VerifyCodeInfo(code, System.currentTimeMillis() + 5 * 60 * 1000);
        verifyCodeMap.put(phone, info);
        
        // 模拟发送短信
        log.info("发送验证码到手机号 {}: {}", phone, code);
        System.out.println("==============================================");
        System.out.println("【模拟短信】手机号: " + phone);
        System.out.println("【验证码】: " + code);
        System.out.println("【有效期】: 5分钟");
        System.out.println("==============================================");
        
        // TODO: 在生产环境中，调用真实的短信API
        // 例如：smsService.send(phone, code);
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String phone, String code) {
        // 开发时先返回为 true
        return true;

//        VerifyCodeInfo info = verifyCodeMap.get(phone);
//
//        if (info == null) {
//            log.warn("验证码不存在，手机号: {}", phone);
//            return false;
//        }
//
//        // 检查是否过期
//        if (System.currentTimeMillis() > info.getExpireTime()) {
//            verifyCodeMap.remove(phone);
//            log.warn("验证码已过期，手机号: {}", phone);
//            return false;
//        }
//
//        // 验证码校验（忽略大小写）
//        boolean isValid = info.getCode().equalsIgnoreCase(code);
//
//        // 验证成功后删除验证码（一次性使用）
//        if (isValid) {
//            verifyCodeMap.remove(phone);
//            log.info("验证码验证成功，手机号: {}", phone);
//        } else {
//            log.warn("验证码错误，手机号: {}, 输入: {}, 正确: {}", phone, code, info.getCode());
//        }
//
//        return isValid;
    }

    /**
     * 生成6位随机数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * 验证码信息内部类
     */
    private static class VerifyCodeInfo {
        private final String code;
        private final long expireTime;

        public VerifyCodeInfo(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }

        public String getCode() {
            return code;
        }

        public long getExpireTime() {
            return expireTime;
        }
    }
}

