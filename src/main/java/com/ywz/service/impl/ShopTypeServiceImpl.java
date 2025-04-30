package com.ywz.service.impl;

import com.google.gson.Gson;
import com.ywz.entity.ShopType;
import com.ywz.mapper.ShopTypeMapper;
import com.ywz.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Gson gson;

    @Override
    public List<ShopType> queryByRedis() {
        List<ShopType> typeList = new ArrayList<>();
        for(int i = 1;i <= 10;i ++){
            String s = stringRedisTemplate.opsForValue().get("shopType:" + i);
            ShopType shopType = gson.fromJson(s, ShopType.class);
            if(shopType != null){
                typeList.add(shopType);
            }
        }
        if(typeList.isEmpty()){
            // 如果没有数据，则从数据库中查询
            typeList = query().orderByAsc("sort").list();
            // 将数据存入Redis
            for(ShopType shopType : typeList){
                String s = gson.toJson(shopType);
                stringRedisTemplate.opsForValue().set("shopType:" + shopType.getId(), s);
            }
            log.info("从数据库中查询商铺类型数据: {}", typeList);
        }else {
            log.info("从Redis中查询商铺类型数据: {}", typeList);
        }
        return typeList;
    }
}
