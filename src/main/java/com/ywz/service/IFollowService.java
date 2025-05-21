package com.ywz.service;

import com.ywz.dto.Result;
import com.ywz.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IFollowService extends IService<Follow> {

    Result follow(Long id, Boolean isFollow);

    Result isFollow(Long id);

    Result commonFollows(Long id);
}
