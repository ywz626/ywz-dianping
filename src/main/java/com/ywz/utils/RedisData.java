package com.ywz.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RedisData {
    // 使用逻辑过期 解决缓存击穿问题
    private LocalDateTime expireTime;
    private Object data;
}
