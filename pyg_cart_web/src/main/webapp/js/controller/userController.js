app.controller("userController",function ($scope,userService) {
    var reg = /^1[3|4|5|6|7|8|9][0-9]{9}$/;
    $scope.sendSms=function () {
        // 正则表达式 判断手机号码的格式
        if(!reg.test($scope.entity.phone)){
            alert("手机格式错误");
            return;
        }

        userService.sendSms($scope.entity.phone).success(function (response) {
            alert(response.message);
        })
    }
    $scope.register=function () {

        // 判断两个密码是否一致
       if( $scope.entity.password!=$scope.password2){
           alert("两次密码输入不一致");
           return;
       }
       // 判断手机号码的格式
        if(!reg.test($scope.entity.phone)){
            alert("手机格式错误");
            return;
        }

        userService.register($scope.entity,$scope.code).success(function (response) {
            if(response.success){
               // 跳转到单点登录
                location.href="/home-index.html";
            }else{
                alert(response.message);
            }
        })
    }
    
    $scope.showName=function () {
        
        userService.showName().success(function (response) {
            $scope.username=JSON.parse(response)  ;
        })


    }
})