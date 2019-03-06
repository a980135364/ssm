package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.order.service.OrderService;
import com.pyg.pojo.TbOrder;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference
    private OrderService orderService;

    @RequestMapping("/saveOrder")
    public Result saveOrder(@RequestBody TbOrder order){
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            order.setUserId(userId);
            String out_trade_no = orderService.saveOrder(order);
//            保存成功后把保存后的订单号返回给页面 支付时需要
            return new Result(true,out_trade_no);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"提交订单失败");
        }
    }
}
