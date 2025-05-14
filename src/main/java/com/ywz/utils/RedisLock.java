package com.ywz.utils;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/5/14 15:58
 */

public class RedisLock implements ILock {

    private final String name;
    private final StringRedisTemplate stringRedisTemplate;

    private final static String KEY_PREFIX = "lock:";

    public RedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(Long timeOut) {
        String threadName = Thread.currentThread().getName();

        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + name, threadName, timeOut, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public boolean unLock() {
        Boolean delete = stringRedisTemplate.delete(KEY_PREFIX + name);
        return Boolean.TRUE.equals(delete);
    }
}
