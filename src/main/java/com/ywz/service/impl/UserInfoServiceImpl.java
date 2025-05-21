package com.ywz.service.impl;

import com.ywz.entity.UserInfo;
import com.ywz.mapper.UserInfoMapper;
import com.ywz.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
