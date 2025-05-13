package com.ywz.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/5/12 19:14
 */
@Component
public class RedisIdWorker {

    private final static long BEGIN_TIMESTAMP = 1735689600L; // 2025-01-01 00:00:00 UTC

    private final StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String key) {
        // 1. 生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long second = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = second - BEGIN_TIMESTAMP;

        //2. 生成序列号
        String nowTime = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = stringRedisTemplate.opsForValue().increment("icr" + key + ":" + nowTime);
        //3. 拼接
        return timestamp << 32 | count;
    }

}
