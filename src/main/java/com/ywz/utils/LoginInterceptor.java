package com.ywz.utils;

import cn.hutool.core.bean.BeanUtil;
import com.ywz.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 于汶泽
 * @Description: 用户登录拦截器
 * @DateTime: 2025/4/29 15:17
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取session中的用户信息
//        HttpSession session = request.getSession();
//        User user =(User) session.getAttribute("user");
//        UserDTO userDTO = new UserDTO();
//        BeanUtil.copyProperties(user,userDTO);
        //获取redis中的用户信息
        String authorization = request.getHeader("authorization");
        if(authorization == null) {
            log.error("用户token为空");
            //如果没有登录，返回401状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        Map<Object, Object> user = stringRedisTemplate.opsForHash().entries(RedisConstants.LOGIN_USER_KEY + authorization);
        //判断用户是否登录
        if(user.isEmpty() ){
            log.error("该用户未登录");
            //如果没有登录，返回401状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        UserDTO userDTO = BeanUtil.fillBeanWithMap(user, new UserDTO(), false);
        //如果存在，保存用户信息到ThreadLocal
        UserHolder.saveUser(userDTO);

        stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY+authorization,RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        return true;
    }
}
