package com.ywz.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
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
    private final static String ID_PREFIX = UUID.randomUUID().toString(true) + "-";

    private final static DefaultRedisScript<Long> redisScript;
    static {
        redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("unlock.lua"));
    }

    public RedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(Long timeOut) {
        String threadName = ID_PREFIX + Thread.currentThread().getName();
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + name, threadName, timeOut, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unLock() {
        // 用redis的lua脚本实现原子性
        stringRedisTemplate.execute(redisScript,
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getName());
    }

    // 原始版本
//    @Override
//    public boolean unLock() {
//        String threadName = ID_PREFIX + Thread.currentThread().getName();
//        String s = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
//        if (s != null && s.equals(Thread.currentThread().getName())) {
//            Boolean delete = stringRedisTemplate.delete(KEY_PREFIX + name);
//            return Boolean.TRUE.equals(delete);
//        }
//        return false;
//    }
}
