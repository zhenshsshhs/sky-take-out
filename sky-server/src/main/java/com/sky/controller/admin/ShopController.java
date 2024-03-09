package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺营业状态")
public class ShopController {

    private static final String  KEY = "shop_status";
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/status")
    @ApiOperation("查询营业状态")
    private Result<Integer> getStatus(){
        log.info("查询营业状态：");
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Integer shopStatus = (Integer)valueOperations.get(KEY);
        return Result.success(shopStatus);
    }

    @PutMapping("/{status}")
    @ApiOperation("设置营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置营业状态为：{}",status==1?"营业中":"打样中");
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(KEY,status);
        return Result.success();
    }
}
