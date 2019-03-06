app.controller("orderController",function ($scope,orderService,addressService,cartService) {

    $scope.selectedAddress=null; //用来接收选中的地址对象 显示到页面的底部

    $scope.findAddressList=function () {
        addressService.findAddressList().success(function (response) {
            $scope.addressList = response;
            for (var i = 0; i < response.length; i++) {
               if( response[i].isDefault=='1'){
                   $scope.selectedAddress=response[i];
                   break;// 跳出循环
               }
            }
            if($scope.selectedAddress==null&&response.length>0){ //意味着没有默认值
                $scope.selectedAddress = response[0]
            }

        })

    }

    // 点击地址 更新页面下方的显示
    $scope.updateSelectedAddress=function (pojo) {
        $scope.selectedAddress = pojo;
    }
    // 判断pojo是否和selectedAddress一样
    $scope.ifSelectedAddress=function (pojo) {
      return  $scope.selectedAddress == pojo;
    }



    $scope.findCartList=function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList=response;

            $scope.totalNum=0; //商品数量
            $scope.totalMoney=0; //商品总金额
            for (var i = 0; i < response.length; i++) {
                var cart = response[i];
                var orderItemList = cart.orderItemList;
                for (var j = 0; j < orderItemList.length; j++) {
                    $scope.totalNum+= orderItemList[j].num;
                    $scope.totalMoney+= orderItemList[j].totalFee;

                }
            }
        })
    }
    // 提交订单
    //     `source_type` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端',
    //     `payment_type` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '支付类型，1、微信支付，2、货到付款',
    //     `receiver_area_name` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人地区名称(省，市，县)街道',
    //     `receiver_mobile` varchar(12) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人手机',
    //     `receiver` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人',

        $scope.entity={sourceType:'2',paymentType:'1'};
        $scope.saveOrder=function () {
            // $scope.selectedAddress
//          向entity中追加三个属性并且赋值
            $scope.entity.receiverAreaName=$scope.selectedAddress.address;
            $scope.entity['receiverMobile']=$scope.selectedAddress.mobile;
            $scope.entity['receiver']=$scope.selectedAddress.contact;
            orderService.saveOrder($scope.entity).success(function (response) {
                if(response.success){
                    // response={success:true,message:'XXXXXXX'}
                    location.href="http://pay.pinyougou.com/pay.html#?out_tarde_no="+response.message;
                }else{
                    alert(response.message);
                }
            })
        }

})