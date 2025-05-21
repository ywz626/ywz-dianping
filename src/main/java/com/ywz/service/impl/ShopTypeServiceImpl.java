package com.ywz.service.impl;

import com.google.gson.Gson;
import com.ywz.entity.ShopType;
import com.ywz.mapper.ShopTypeMapper;
import com.ywz.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywz.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Gson gson;

    @Override
    public List<ShopType> queryByRedisByString() {
        List<ShopType> typeList = new ArrayList<>();
        for (int i = 1; i <= SystemConstants.MAX_SHOPTYPE_SIZE; i++) {
            String s = stringRedisTemplate.opsForValue().get("shopType:" + i);
            ShopType shopType = gson.fromJson(s, ShopType.class);
            if (shopType != null) {
                typeList.add(shopType);
            }
        }
        if (typeList.isEmpty()) {
            // 如果没有数据，则从数据库中查询
            typeList = query().orderByAsc("sort").list();
            // 将数据存入Redis
            for (ShopType shopType : typeList) {
                String s = gson.toJson(shopType);
                stringRedisTemplate.opsForValue().set("shopType:" + shopType.getId(), s);
            }
            log.info("从数据库中查询商铺类型数据: {}", typeList);
        } else {
            log.info("从Redis中查询商铺类型数据: {}", typeList);
        }
        return typeList;
    }

    @Override
    public List<ShopType> queryByRedisByList() {
        List<String> typeListTemp = stringRedisTemplate.opsForList().range("shopType:", 0, -1);
        List<ShopType> typeList = new ArrayList<>();
        for (String s : typeListTemp) {
            ShopType shopType = gson.fromJson(s, ShopType.class);
            if (shopType != null) {
                typeList.add(shopType);
            }
        }
        if (typeList.isEmpty()) {
            // 如果没有数据，则从数据库中查询
            typeList = query().orderByAsc("sort").list();
            // 将数据存入Redis
            for (ShopType shopType : typeList) {
                String s = gson.toJson(shopType);
                stringRedisTemplate.opsForList().rightPush("shopType:", s);
            }
            log.info("从数据库中查询商铺类型数据: {}", typeList);
        }else {
            log.info("从Redis中查询商铺类型数据: {}", typeList);
        }
        return typeList;
    }
}