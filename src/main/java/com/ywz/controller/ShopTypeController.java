package com.ywz.controller;


import com.ywz.dto.Result;
import com.ywz.entity.ShopType;
import com.ywz.service.IShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
        //使用字符串的方法进行redis缓存
//        List<ShopType> typeList = typeService.queryByRedisByString();
        //使用List的方法进行redis缓存
        List<ShopType> typeList = typeService.queryByRedisByList();
        return Result.ok(typeList);
    }
}
