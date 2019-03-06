package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.cart.service.CartService;
import entity.Result;
import groupEntity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
//    user-key
    @Reference
    private CartService cartService;

    private String getUuid(){
//        1、先从cookie中获取uuid
        String user_key = CookieUtil.getCookieValue(request, "user-key");
        if(user_key==null){ //如果没有重新获取一个uuid放入到cookie中
            user_key = UUID.randomUUID().toString();
            CookieUtil.setCookie(request,response,"user-key",user_key,60*60*24*7);
        }
        return user_key;
    }
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        String uuid = getUuid();
        List<Cart> cartList_uuid = cartService.findCartListFromRedisByUuid(uuid); //通过未登录时的uuid获取的那一份购物车数据

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!userId.equals("anonymousUser")) { //表示登录
            List<Cart> cartList_userId= cartService.findCartListFromRedisByUserId(userId); ////通过登录时的userId获取的那一份购物车数据
            if(cartList_uuid.size()>0){ //判断是否有必要合并
                cartList_userId = cartService.mergeCartList(cartList_uuid,cartList_userId);
                //合并后清除cartList_uuid
                cartService.deleteCartListFromRedisByKey(uuid);
//                把合并后的购物车数据重新放入redis
                cartService.saveCartListToRedis(userId,cartList_userId);
            }
            return cartList_userId;
        }else{
//            表示未登录
            return cartList_uuid;
        }

//        return cartService.findCartListFromRedisByKey(uuid);
    }

    @RequestMapping("/addGoodsToCartList")
//    @CrossOrigin(origins="http://item.pinyougou.com")  // 信任item.pinyougou.com过来的请求
////    @CrossOrigin(origins={"http://item.pinyougou.com","http://search.pinyougou.com"})  // 信任item.pinyougou.com和search.pinyougou.com过来的请求
    @CrossOrigin(origins="*")  // 信任所有的网站过来的请求
    public Result addGoodsToCartList(Long itemId, int num){
        try {
            List<Cart> cartList = findCartList(); //添加之前的数据
            cartList = cartService.addGoodsToCartList(cartList,itemId,num); //向之前的购物车列表中追加新的商品数据
//        重新获取的购物车列表存储到redis中

            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            if(!userId.equals("anonymousUser")){ //表示登录
                cartService.saveCartListToRedisByUserId(userId, cartList);
            }else{
//                没有登录
                String uuid = getUuid();
                cartService.saveCartListToRedisByUuid(uuid, cartList);
            }
            return new Result(true,"");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }
}
