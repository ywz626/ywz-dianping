package com.ywz.service;

import com.ywz.dto.Result;
import com.ywz.entity.SeckillVoucher;
import com.ywz.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);

    void createOrder(VoucherOrder order);
}
