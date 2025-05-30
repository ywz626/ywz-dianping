package com.ywz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywz.dto.Result;
import com.ywz.entity.Voucher;
import com.ywz.mapper.VoucherMapper;
import com.ywz.entity.SeckillVoucher;
import com.ywz.service.ISeckillVoucherService;
import com.ywz.service.IVoucherService;
import com.ywz.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        // 将秒杀信息存到redis中
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY +voucher.getId(), voucher.getStock()+"");
    }
}
