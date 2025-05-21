package com.ywz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ywz.entity.Voucher;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface VoucherMapper extends BaseMapper<Voucher> {

    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}
