package com.ywz.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywz.dto.LoginFormDTO;
import com.ywz.dto.Result;
import com.ywz.entity.User;
import com.ywz.mapper.UserMapper;
import com.ywz.service.IUserService;
import com.ywz.utils.RegexUtils;
import com.ywz.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.ywz.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    /**
     * 模拟发送手机验证码
     * @param phone
     * @param session
     * @return
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1.判断手机号格式是否正确
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }

        //2.生成验证码
        String code = RandomUtil.randomString(6);

        //3.保存验证码到session
        session.setAttribute("code", code);
        //保存手机号码到session
        session.setAttribute("phone", phone);
        //4.模拟发送验证码
        log.info("发送验证码成功，验证码为：{}", code);
        return Result.ok();
    }

    /**
     * 登录功能
     *
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1.判断手机号格式是否正确
        if (RegexUtils.isPhoneInvalid(loginForm.getPhone())) {
            return Result.fail("手机号格式错误");
        }
        String phone = (String)session.getAttribute("phone");
        //2.判断手机号是否一致
        if(phone == null || !phone.equals(loginForm.getPhone())){
            return Result.fail("手机号错误");
        }
        String code = (String) session.getAttribute("code");
        //3.判断验证码是否正确
        if (code == null || !code.equals(loginForm.getCode())) {
            return Result.fail("验证码错误");
        }
        //4.根据手机号查询用户
        User user = query().eq("phone", phone).one();
        //5.判断用户是否存在
        if (user == null) {
            //6.如果不存在，创建新用户
            user = createUser(phone);
        }
        //7.保存用户信息到session
        session.setAttribute("user", user);
        return Result.ok();
    }

    /**
     * 根据手机号创建新用户
     *
     * @param phone 手机号
     * @return User
     */
    private User createUser(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(4));
        return user;
    }

    @Override
    public Result logOut(HttpSession session) {
        try {
            User user =(User) session.getAttribute("user");
            if (user != null) {
                session.removeAttribute("user");
            }
            UserHolder.removeUser();
            session.removeAttribute("phone");
            return Result.ok();
        } catch (Exception e) {
            return Result.fail("登出失败");
        }
    }
}
