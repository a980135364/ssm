app.controller("cartController",function ($scope,cartService) {
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

    $scope.addGoodsToCartList=function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(function (response) {
            if(response.success){
                $scope.findCartList(); //重新查询购物车
            }else{
                alert(response.message);
            }
        })
    }
})