package com.ywz.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ywz.dto.Result;
import com.ywz.entity.Shop;
import com.ywz.mapper.ShopMapper;
import com.ywz.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywz.utils.RedisConstants;
import com.ywz.utils.RedisData;
import com.ywz.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ywz.utils.RedisConstants.SHOP_GEO_KEY;

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
        Result result = new Result();

        // 用互斥锁的方法解决缓存击穿问题
        result = queryWithPassThrough(id);

        // 用逻辑过期的方法解决缓存击穿问题
        return result;
    }

    private Shop queryWithPassThroughByLogic(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        //TODO: 用逻辑过期的方法解决缓存击穿问题
        return null;
    }

    private Result queryWithPassThrough(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        try {
            Object o = stringRedisTemplate.opsForHash().get(key, "id");
            log.info("o: {}", o);
            if ("null".equals(o)) {
                return Result.fail("redis缓存穿透商铺不存在");
            }
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(RedisConstants.CACHE_SHOP_KEY + id);
            Shop shop = BeanUtil.fillBeanWithMap(entries, new Shop(), false);
            if (shop.getId() != null) {
                log.info("redis中有商铺信息，直接返回");
                return Result.ok(shop);
            }
            log.info("redis中没有商铺信息，查询数据库");
            if (!tryLock(RedisConstants.LOCK_SHOP_KEY + id)) {
                log.info("获取锁失败");
                Thread.sleep(50);
                getByRedisById(id);
            }
            log.info("获取锁成功");
            shop = getById(id);
            if (shop == null) {
                //如果数据库中没有商铺信息，则将null存入redis
                // 解决缓存穿透问题的方案
                stringRedisTemplate.opsForHash().put(key, "id", "null");
                stringRedisTemplate.expire(key, RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return Result.fail("商铺不存在");
            }
            Map<String, Object> shopMap = BeanUtil.beanToMap(shop, new HashMap<>(), CopyOptions.create().
                    setIgnoreNullValue(true).
                    setFieldValueEditor((field, value) -> {
                        if (value == null) {
                            return null;
                        }
                        return value.toString();
                    }));
            stringRedisTemplate.opsForHash().putAll(RedisConstants.CACHE_SHOP_KEY + shop.getId(), shopMap);
            stringRedisTemplate.expire(RedisConstants.CACHE_SHOP_KEY + shop.getId(), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
            log.info("商铺信息缓存到redis成功");
            return Result.ok(shop);
        } catch (Exception e) {
            log.error("{}从redis中获取商铺信息时出错", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            //释放锁
            boolean isSuccess = deleteLock(RedisConstants.LOCK_SHOP_KEY + id);
            if (isSuccess) {
                log.info("释放锁成功");
            }
        }
    }


    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private boolean deleteLock(String key) {
        return BooleanUtil.isTrue(stringRedisTemplate.delete(key));
    }


    @Override
    public Result updateAndRedis(Shop shop) {
        if (shop.getId() == null) {
            return Result.fail("商铺id不能为空");
        }
        updateById(shop);
        //删除缓存
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        if (x == null || y == null) {
            Page<Shop> shops = query().eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(shops.getRecords());
        }
        // 从redis中获取商铺信息
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
        String key = SHOP_GEO_KEY + typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> search = stringRedisTemplate.opsForGeo().search(
                key,
                GeoReference.fromCoordinate(x, y),
                new Distance(5000),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end));
        if (search == null) {
            return Result.ok(Collections.emptyList());
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = search.getContent();
        if (list.isEmpty()){
            log.error("list为空");
            return Result.ok(Collections.emptyList());
        }
        if(list.size() <= from){
            return Result.ok(Collections.emptyList());
        }
        ArrayList<Long> ids = new ArrayList<>(list.size());
        HashMap<String, Distance> distanceMap = new HashMap<>();
        list.stream().skip(from).forEach(result -> {
            String idStr = result.getContent().getName();
            ids.add(Long.valueOf(idStr));
            Distance distance = result.getDistance();
            distanceMap.put(idStr, distance);
        });
        String idStr = StrUtil.join(",", ids);
        List<Shop> shops = query().in("id", ids).last("order by field(id," + idStr + ")").list();
        for (Shop shop : shops) {
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        return Result.ok(shops);
    }

}
