package com.ywz.utils;

import com.google.gson.Gson;
import com.ywz.entity.VoucherOrder;
import com.ywz.service.ISeckillVoucherService;
import com.ywz.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/5/30 14:10
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = "voucher-order-topic",consumerGroup = "voucher-order-consumer-group")
public class RocketMQConsumer implements RocketMQListener<String> {

    @Resource
    private Gson gson;
    @Resource
    private RedissonClient redisson;
    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private IVoucherOrderService voucherOrderService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void onMessage(String s) {
        VoucherOrder order = gson.fromJson(s, VoucherOrder.class);
        log.info("接收到订单消息: {}", order);
        // 处理订单逻辑
        voucherHandler(order);
    }

    private void voucherHandler(VoucherOrder order) {
        Long userId = order.getUserId();
        RLock redisLock = redisson.getLock("lock:order:" + userId);
        boolean lockStatus = redisLock.tryLock();
        if (!lockStatus) {
            // 这里的锁争抢失败代表同一时间段该用户已经购买该商品，并且购买过程还未结束
            log.error("只能买一件");
            return;
        }
        try {
            RocketMQConsumer proxy = (RocketMQConsumer) AopContext.currentProxy();
            proxy.createOrder(order);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } finally {
            redisLock.unlock();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void createOrder(VoucherOrder order) {
        log.info("创建订单，订单信息为：{}", order);
        Long userId = order.getUserId();
        Long voucherId = order.getVoucherId();
        Integer count = voucherOrderService.query().eq("voucher_id", voucherId)
                .eq("user_id", userId).count();
        if (count > 0) {
            log.error("禁止超卖");
            return;
        }
        //5.扣减库存
        seckillVoucherService.update().setSql("stock = stock -1")
                .gt("stock", 0)
                .eq("voucher_id", voucherId).update();
        stringRedisTemplate.opsForValue().increment(RedisConstants.SECKILL_STOCK_KEY + voucherId.toString(), -1);
        voucherOrderService.save(order);
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_ORDER_KEY + voucherId.toString(),userId.toString());
        log.info("创建订单成功，订单id为：{}", order.getId());
    }
}
