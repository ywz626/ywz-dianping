package com.ywz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ywz.dto.LoginFormDTO;
import com.ywz.dto.Result;
import com.ywz.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result logOut(HttpServletRequest request);

    User createUser(String phone);

    Result sign();

    Result signDays();
}
