package com.ywz.service;

import com.ywz.dto.Result;
import com.ywz.entity.SeckillVoucher;
import com.ywz.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);

    Result createOrder(Long voucherId, SeckillVoucher secVoucher);
}
