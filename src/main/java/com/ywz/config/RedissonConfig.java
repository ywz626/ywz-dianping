package com.ywz.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/5/14 21:59
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://47.94.214.84:6379").setPassword("123456");
        return Redisson.create(config);
    }
}
