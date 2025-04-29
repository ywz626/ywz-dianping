package com.ywz.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywz.dto.Result;
import com.ywz.entity.User;
import com.ywz.mapper.UserMapper;
import com.ywz.service.IUserService;
import com.ywz.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

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

        //4.模拟发送验证码
        log.info("发送验证码成功，验证码为：{}", code);
        return Result.ok();
    }
}
