app.service("indexService",function ($http) {
    
    this.findByCategotyId=function (cid) {
      return  $http.get("./content/findByCategotyId?cid="+cid);
    }
})