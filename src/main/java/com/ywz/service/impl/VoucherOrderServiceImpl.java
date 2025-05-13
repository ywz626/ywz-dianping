package com.ywz.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.ywz.controller.VoucherOrderController;
import com.ywz.dto.Result;
import com.ywz.entity.SeckillVoucher;
import com.ywz.entity.Voucher;
import com.ywz.entity.VoucherOrder;
import com.ywz.mapper.VoucherOrderMapper;
import com.ywz.service.ISeckillVoucherService;
import com.ywz.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywz.utils.RedisIdWorker;
import com.ywz.utils.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
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

    @Override
    public Result seckillVoucher(Long voucherId) {
        //1.查询优惠卷信息
        SeckillVoucher secVoucher = seckillVoucherService.getById(voucherId);

        //2.判断秒杀是否开始
        if(secVoucher.getBeginTime().isAfter(LocalDateTime.now())){
            return Result.fail("秒杀尚未开始！");
        }
        //3.判断秒杀是否结束
        if(secVoucher.getEndTime().isBefore(LocalDateTime.now())){
            return Result.fail("秒杀已结束！");
        }
        //4.判断库存是否充足
        if (secVoucher.getStock()<=0){
            return Result.fail("库存不足！");
        }
        //5.扣减库存
        seckillVoucherService.update().setSql("stock = stock -1")
                .eq("voucher_id", voucherId).update();
        //6.创建订单
        VoucherOrder order = new VoucherOrder();
        order.setVoucherId(secVoucher.getVoucherId());
        order.setId(redisIdWorker.nextId("voucher"));
        order.setUserId(UserHolder.getUser().getId());
        save(order);
        //7.返回订单id
        return Result.ok(order.getId());
    }
}
