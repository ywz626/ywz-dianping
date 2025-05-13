package com.ywz.config;

import com.ywz.utils.LoginInterceptor;
import com.ywz.utils.LoginRefreshInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author 于汶泽
 * @Description: 用户登录拦截器的配置类
 * @DateTime: 2025/4/29 15:23
 */
@Configuration
public class MVCConfig implements WebMvcConfigurer {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void addInterceptors(InterceptorRegistry registry) {
        //添加拦截器
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns("/user/code", "/user/login", "/shop/**", "/voucher/**","blog/hot",
                        "shop-type/**", "/upload/**","voucher/**")
                .order(2);

        registry.addInterceptor(new LoginRefreshInterceptor(stringRedisTemplate))
                .order(1);
    }
}
