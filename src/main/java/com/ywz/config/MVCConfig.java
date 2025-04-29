package com.ywz.config;

import com.ywz.utils.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/4/29 15:23
 */
@Configuration
public class MVCConfig implements WebMvcConfigurer {

    public void addInterceptors(InterceptorRegistry registry) {
        //添加拦截器
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns("/user/code", "/user/login", "/shop/**", "/voucher/**","blog/hot",
                        "shop-type/**", "/upload/**")
                .order(1);
    }
}
