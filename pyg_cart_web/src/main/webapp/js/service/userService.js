app.service("userService",function ($http) {
    
    this.sendSms=function (phone) {
      return  $http.get("./user/sendSms?phone="+phone);
    }

    this.register=function (entity,code) {
      return  $http.post("./user/add?code="+code,entity);
    }
    this.showName=function () {
      return  $http.get("./user/showName");
    }
})