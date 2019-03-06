package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.order.service.AddressService;
import com.pyg.pojo.TbAddress;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Reference
    private AddressService addressService;
//    select * from tb_address where user_id=?
    @RequestMapping("/findAddressList")
    public List<TbAddress> findAddressList(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return addressService.findAddressListByUserId(userId);
    }
}
