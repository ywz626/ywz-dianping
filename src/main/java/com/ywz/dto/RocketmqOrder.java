package com.ywz.dto;

import com.ywz.entity.VoucherOrder;
import com.ywz.service.IVoucherOrderService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/5/30 14:30
 */
@Data
@Builder
@AllArgsConstructor
public class RocketmqOrder implements Serializable {
    private VoucherOrder voucherOrder;
    private IVoucherOrderService proxy;
}
