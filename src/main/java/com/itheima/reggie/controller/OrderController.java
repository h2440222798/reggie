package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    // 用户下单操作
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据:{}",orders);

        orderService.submit(orders);
        return R.success("下单成功");
    }
    // 展示订单操作
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String number){

        log.info("page = {},pageSize = {},number = {}",page,pageSize,number);
//        log.info("page = {},pageSize = {}, status = {}, id = {}",page,pageSize,status,id);

        Page pageInfo = new Page(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper();
        if(number != null){
            queryWrapper.like(Orders::getNumber,number);
        }
        queryWrapper.orderByAsc(Orders::getOrderTime);

//
//        //添加一个排序条件
//        queryWrapper.orderByDesc(Orders::getOrderTime);
//
//        //执行查询
        orderService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }
    @PutMapping
    public R<String> update(@RequestBody Orders orders){
        log.info("修改分类信息:{}",orders);
        orderService.updateById(orders);
        return R.success("修改派送信息成功");
    }

}
