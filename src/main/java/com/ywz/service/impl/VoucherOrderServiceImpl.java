package com.ywz.service.impl;

import com.ywz.dto.Result;
import com.ywz.entity.VoucherOrder;
import com.ywz.mapper.VoucherOrderMapper;
import com.ywz.service.ISeckillVoucherService;
import com.ywz.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywz.utils.RedisIdWorker;
import com.ywz.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redisson;

    private IVoucherOrderService proxy;

    private final static DefaultRedisScript<Long> redisScript;

    static {
        redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("seckill.lua"));
        redisScript.setResultType(Long.class);
    }

    // 阻塞队列
    private final BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<VoucherOrder>(1024 * 1024);
    // 线程池
    private static final ExecutorService SECKILL_EXECUTOR = Executors.newSingleThreadExecutor();

    // 初始化
    @PostConstruct
    public void init() {
        SECKILL_EXECUTOR.submit(new VoucherOrderTask());
    }

    // 阻塞队列实现异步秒杀
    private class VoucherOrderTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    VoucherOrder order = orderTasks.take();
                    VoucherHandler(order);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void VoucherHandler(VoucherOrder order) {
        Long userId = order.getUserId();
        RLock redisLock = redisson.getLock("lock:order:" + userId);
        boolean lockStatus = redisLock.tryLock();
        if (!lockStatus) {
            // 这里的锁争抢失败代表同一时间段该用户已经购买该商品，并且购买过程还未结束
            log.error("只能买一件");
            return;
        }
        try {
            proxy = (IVoucherOrderService) AopContext.currentProxy();
            proxy.createOrder(order);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } finally {
            redisLock.unlock();
        }
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        // 查询lua脚本
        Long execute = stringRedisTemplate.execute(redisScript, Collections.emptyList(), voucherId.toString(), UserHolder.getUser().getId().toString());
        // 判断返回值是否为0       不为0 没有购买资格
        int r = execute.intValue();
        if (r != 0) {
            return r == 1 ? Result.fail("库存不足") : Result.fail("禁止购买多个");
        }
        long orderId = redisIdWorker.nextId("order");
        // 为0 有购买资格 加入阻塞队列
        // 保存到阻塞队列
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setUserId(UserHolder.getUser().getId());
        orderTasks.add(voucherOrder);
        proxy = (IVoucherOrderService) AopContext.currentProxy();
        return Result.ok(orderId);
    }

//    @Override
//    public Result seckillVoucher(Long voucherId) {
//        //1.查询优惠卷信息
//        SeckillVoucher secVoucher = seckillVoucherService.getById(voucherId);
//
//        //2.判断秒杀是否开始
//        if (secVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
//            return Result.fail("秒杀尚未开始！");
//        }
//        //3.判断秒杀是否结束
//        if (secVoucher.getEndTime().isBefore(LocalDateTime.now())) {
//            return Result.fail("秒杀已结束！");
//        }
//        //4.判断库存是否充足
//        if (secVoucher.getStock() <= 0) {
//            return Result.fail("库存不足！");
//        }
//        Long userId = UserHolder.getUser().getId();
//        //尝试获取锁

    /// /        RedisLock redisLock = new RedisLock("order:" + userId, stringRedisTemplate);
//        RLock redisLock = redisson.getLock("lock:order:" + userId);
//        boolean lockStatus = redisLock.tryLock();
//        if(!lockStatus){
//            // 这里的锁争抢失败代表同一时间段该用户已经购买该商品，并且购买过程还未结束
//            return Result.fail("同一用户只能买一件");
//        }
//        try {
//            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
//            return proxy.createOrder(voucherId,secVoucher);
//        } catch (IllegalStateException e) {
//            throw new RuntimeException(e);
//        } finally {
//            redisLock.unlock();
//        }
//    }

    // 这个方法枷锁是防止超卖，但外面已经有一个锁，同一个用户不可能有两个线程执行该方法，所以我把锁去掉了
    // 测试是正确的
    @Transactional
    public void createOrder(VoucherOrder order) {
        Long userId = order.getUserId();
        Long voucherId = order.getVoucherId();
        Integer count = query().eq("voucher_id", voucherId)
                .eq("user_id", userId).count();
        if (count > 0) {
            log.error("禁止超卖");
            return;
        }
        //5.扣减库存
        seckillVoucherService.update().setSql("stock = stock -1")
                .gt("stock", 0)
                .eq("voucher_id", voucherId).update();
        save(order);
    }
}
