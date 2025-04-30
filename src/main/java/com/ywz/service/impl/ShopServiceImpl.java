package com.ywz.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.ywz.dto.Result;
import com.ywz.entity.Shop;
import com.ywz.mapper.ShopMapper;
import com.ywz.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywz.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getByRedisById(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        try {
            Object o = stringRedisTemplate.opsForHash().get(key, "id");
            log.info("o: {}", o);
            if("null".equals(o)){
                return Result.fail("redis缓存穿透商铺不存在");
            }
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(RedisConstants.CACHE_SHOP_KEY + id);
            Shop shop = BeanUtil.fillBeanWithMap(entries, new Shop(), false);
            if (shop.getId() != null) {
                log.info("redis中有商铺信息，直接返回");
                return Result.ok(shop);
            }
            log.info("redis中没有商铺信息，查询数据库");
            shop = getById(id);
            if(shop == null) {
                //如果数据库中没有商铺信息，则将null存入redis
                stringRedisTemplate.opsForHash().put(key,"id","null");
                stringRedisTemplate.expire(key, RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return Result.fail("商铺不存在");
            }
            Map<String, Object> shopMap = BeanUtil.beanToMap(shop, new HashMap<>(), CopyOptions.create().
                    setIgnoreNullValue(true).
                    setFieldValueEditor((field, value) -> {
                        if(value == null){
                            return null;
                        }
                        return value.toString();
                    }));
            stringRedisTemplate.opsForHash().putAll(RedisConstants.CACHE_SHOP_KEY + shop.getId(), shopMap);
            stringRedisTemplate.expire(RedisConstants.CACHE_SHOP_KEY+ shop.getId(), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
            log.info("商铺信息缓存到redis成功");
            return Result.ok(shop);
        } catch (Exception e) {
            log.error("{}从redis中获取商铺信息时出错", e.getMessage());
            throw new RuntimeException(e);
        }
    }



    @Override
    public Result updateAndRedis(Shop shop) {
        if(shop.getId() == null){
            return Result.fail("商铺id不能为空");
        }
        updateById(shop);
        //删除缓存
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }

}
