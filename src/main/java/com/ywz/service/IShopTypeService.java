package com.ywz.service;

import com.ywz.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopTypeService extends IService<ShopType> {

    List<ShopType> queryByRedisByString();

    List<ShopType> queryByRedisByList();
}
