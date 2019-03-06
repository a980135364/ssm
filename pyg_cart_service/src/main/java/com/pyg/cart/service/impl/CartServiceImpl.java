package com.pyg.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.cart.service.CartService;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbOrderItem;
import groupEntity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbItemMapper itemMapper;
    @Override
    public List<Cart> findCartListFromRedisByUuid(String uuid) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundValueOps("cartList_"+uuid).get();
        if(cartList==null){
            cartList=new ArrayList<Cart>();
        }

        return cartList;
    }

    @Override
    public List<Cart> findCartListFromRedisByUserId(String userId) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(userId);
        if(cartList==null){
            cartList=new ArrayList<Cart>();
        }

        return cartList;
    }

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, int num) {
        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
        if(tbItem==null){
            throw new RuntimeException("无此商品");
        }
        String sellerId = tbItem.getSellerId();
//        判断即将添加的商品所属的商家是否有对应的购物车对象cart
          Cart cart = findCartFromCartListBySellerId(cartList,sellerId);
//                 如果有
        if(cart!=null){
//            还需判断即将添加的商品是否在 cart的orderItemList中
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            TbOrderItem orderItem = findOrderItemFromOrderItemListByItemId(orderItemList,itemId);
            if(orderItem!=null){
                //            如果存在 数量累加 小计金额重新计算
                orderItem.setNum(orderItem.getNum()+num);
//                单价*数量
                BigDecimal total_fee = orderItem.getPrice().multiply(new BigDecimal(orderItem.getNum()));
                orderItem.setTotalFee(total_fee);

                if(orderItem.getNum()<=0){ //没有此商品了
                    orderItemList.remove(orderItem);
//                    判断此商家下是否还有商品
                            if(orderItemList.size()==0){
//                               把此商家对应的cart从cartList中移除
                                cartList.remove(cart);
                            }

                }


            }else{
                //            如果不存在新建一个TbOrderItem 把此TBOrderItem放入到orderItemList
                orderItem = new TbOrderItem();
                orderItem = createTbOrderItem(orderItem,tbItem,num);
//                orderItem.setId(); // TODO 向mysql中插入数据时才赋值
//                orderItem.setOrderId(); // TODO 向mysql中插入数据时才赋值
                orderItemList.add(orderItem);


            }

        }else{
            //                如果没有
//                  新建一个Cart对象
             cart = new Cart();
             cart.setSellerId(sellerId);
             cart.setSellerName(tbItem.getSeller());
             List<TbOrderItem> orderItemList = new ArrayList<>();
//                                新建一个TbOrderItem
            TbOrderItem orderItem = new TbOrderItem();
            orderItem = createTbOrderItem(orderItem,tbItem,num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
//                         把新构建好的cart放入到cartList中
            cartList.add(cart);
        }
        return cartList;
    }

    private TbOrderItem createTbOrderItem(TbOrderItem orderItem,TbItem tbItem,int num) {
        orderItem.setPrice(tbItem.getPrice());
        orderItem.setNum(num);
        BigDecimal total_fee = orderItem.getPrice().multiply(new BigDecimal(orderItem.getNum()));
        orderItem.setTotalFee(total_fee);
        orderItem.setSellerId(tbItem.getSellerId());
        orderItem.setTitle(tbItem.getTitle());

        orderItem.setPicPath(tbItem.getImage());
        orderItem.setItemId(tbItem.getId());
        orderItem.setGoodsId(tbItem.getGoodsId());

        return orderItem;
    }

    private TbOrderItem findOrderItemFromOrderItemListByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
           if(orderItem.getItemId().longValue()==itemId.longValue()){
               return orderItem;
           }
        }
        return null;
    }

    private Cart findCartFromCartListBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
           if( cart.getSellerId().equals(sellerId)){
               return cart;
           }
        }
        return null;
    }

    @Override
    public void saveCartListToRedis(String uuid, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(uuid,cartList);
    }

    @Override
    public void saveCartListToRedisByUserId(String userId, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(userId,cartList);
    }

    @Override
    public void saveCartListToRedisByUuid(String uuid, List<Cart> cartList) {
//        redisTemplate.boundHashOps("cartList").put(uuid,cartList);
        redisTemplate.boundValueOps("cartList_"+uuid).set(cartList,7, TimeUnit.DAYS);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_uuid, List<Cart> cartList_userId) {
        for (Cart cart : cartList_uuid) {
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            for (TbOrderItem orderItem : orderItemList) {
                cartList_userId = addGoodsToCartList(cartList_userId,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList_userId;
    }

    @Override
    public void deleteCartListFromRedisByKey(String uuid) {
//        redisTemplate.boundHashOps("cartList").delete(uuid);
        redisTemplate.delete("cartList_"+uuid);
    }

}
