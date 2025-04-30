package com.ywz.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
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
    public Shop getByRedisById(Long id) {
        try {
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(RedisConstants.CACHE_SHOP_KEY + id);
            log.info("从redis中获取商铺信息成功");
            Shop shop = BeanUtil.fillBeanWithMap(entries, new Shop(), false);
            log.info("商户信息为：{}", shop);
            return shop;
        } catch (Exception e) {
            log.error("{}从redis中获取商铺信息时出错", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveByRedis(Shop shop) {
        try {
            // 将商铺信息存入Redis
            Map<String, Object> shopMap = BeanUtil.beanToMap(shop, new HashMap<>(), CopyOptions.create().
                    setIgnoreNullValue(true).
                    setFieldValueEditor((field, value) -> {
                        if(value == null){
                            return null;
                        }
                        return value.toString();
                    }));
            log.info("商户信息为：{}", shopMap);
            stringRedisTemplate.opsForHash().putAll(RedisConstants.CACHE_SHOP_KEY + shop.getId(), shopMap);
            log.info("商铺信息缓存到redis成功");
        } catch (Exception e) {
            log.error("{}缓存商铺信息到redis时出错", e.getMessage());
        }
    }

}
