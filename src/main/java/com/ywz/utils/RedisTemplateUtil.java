package com.ywz.utils;

import com.google.gson.Gson;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author 于汶泽
 * @Description: stringRedisTemplate工具类
 * @DateTime: 2025/5/1 20:48
 */
@Component
public class RedisTemplateUtil {

    private final StringRedisTemplate stringRedisTemplate;

    @Resource
    private Gson gson;

    public RedisTemplateUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit timeUnit) {
       stringRedisTemplate.opsForValue().set(key, gson.toJson(value), time, timeUnit);
    }

    public void setWithLogical(String key, Object value, Long time, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, gson.toJson(value), time, timeUnit);
    }
}
