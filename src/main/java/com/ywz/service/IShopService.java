package com.ywz.service;

import com.ywz.dto.Result;
import com.ywz.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IShopService extends IService<Shop> {

    Result getByRedisById(Long id);


    Result updateAndRedis(Shop shop);

    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
