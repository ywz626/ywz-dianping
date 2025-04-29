package com.ywz.utils;

import cn.hutool.core.bean.BeanUtil;
import com.ywz.dto.UserDTO;
import com.ywz.entity.User;
import org.aopalliance.intercept.Interceptor;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/4/29 15:17
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取session中的用户信息
        HttpSession session = request.getSession();
        User user =(User) session.getAttribute("user");
        UserDTO userDTO = new UserDTO();
        BeanUtil.copyProperties(user,userDTO);
        //判断用户是否登录
        if(user == null){
            //如果没有登录，返回401状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        //如果存在，保存用户信息到ThreadLocal
        UserHolder.saveUser(userDTO);

        return true;
    }
}
