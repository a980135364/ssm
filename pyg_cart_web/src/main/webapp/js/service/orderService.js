app.service("orderService",function ($http) {
    
    this.saveOrder=function (entity) {
      return  $http.post("./order/saveOrder",entity);
    }
})