package com.ywz.service;

import com.ywz.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface IShopTypeService extends IService<ShopType> {

    List<ShopType> queryByRedisByString();

    List<ShopType> queryByRedisByList();
}
