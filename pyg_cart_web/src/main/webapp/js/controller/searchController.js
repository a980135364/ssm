app.controller("searchController",function ($scope,$location,searchService) {

    $scope.keyword=""; //搜索框中的内容

    $scope.paramMap={keyword:"小米",category:'',brand:'',price:'',order:'asc',spec:{},pageNo:1};
    // $scope.paramMap={keyword:"小米",category:'',brand:'',price:'',order:'asc',spec:{网络：移动3G,机身内存：16G,手机屏幕尺寸:5寸 }};

    // 点击分类或品牌向paramMap添加参数
    $scope.addParamToParamMap=function (key,value) {
        $scope.paramMap[key]=value;
        $scope.search();
    }

    $scope.removeParamFromParamMap=function (key) {
        $scope.paramMap[key]='';
        $scope.search();
    }
    // 点击规格向paramMap.spec添加参数
    $scope.addParamToParamMapSpec=function (key,value) {
        $scope.paramMap.spec[key]=value;
        $scope.search();
    }
    $scope.removeParamFromParamMapSpec=function (key) {
        // {"网络":"移动3G","机身内存":"32G","手机屏幕尺寸":"5寸"}---key:手机屏幕尺寸-->{"网络":"移动3G","机身内存":"32G"}
        // 从map中移除 一对 数据
        delete $scope.paramMap.spec[key];
        $scope.search();
    }

    // 一打开页面时
    $scope.initSearch=function () {
        // 从url中获取参数
        // $scope.paramMap.keyword=location.search.split("=")[1];
        if($location.search()['keyword']==undefined||$location.search()['keyword']==''||$location.search()['keyword']=="undefined"){
            $scope.paramMap.keyword='小米';
        }else{
            $scope.paramMap.keyword=$location.search()['keyword'];
        }



        $scope.keyword = $scope.paramMap.keyword;
        $scope.search();
    }

    // 点击页面上的搜索按钮
    $scope.searchByKeyword=function () {
        $scope.paramMap={keyword:"小米",category:'',brand:'',price:'',order:'asc',spec:{},pageNo:1};
        $scope.paramMap.keyword=$scope.keyword;
        $scope.search();
    }

    $scope.search=function () {
        searchService.searchByParamMap($scope.paramMap).success(function (response) {
            $scope.resultMap=response;

            // $scope.pageLabel=[];
            // for (var i = 1; i <= response.totalPages; i++) {
            //     $scope.pageLabel.push(i);
            // }
            buildPageLabel(); //构建页码

        })
    }


    function buildPageLabel() {
        $scope.pageLabel = [];//新增分页栏属性
        var maxPageNo = $scope.resultMap.totalPages;//得到最后页码
        var firstPage = 1;//开始页码
        var lastPage = maxPageNo;//截止页码
        $scope.firstDot = true;//前面有点
        $scope.lastDot = true;//后边有点
        if ($scope.resultMap.totalPages > 5) { //如果总页数大于 5 页,显示部分页码
            if ($scope.paramMap.pageNo <= 3) {//如果当前页小于等于 3
                lastPage = 5; //前 5 页
                $scope.firstDot = false;//前面没点
            } else if ($scope.paramMap.pageNo >= lastPage - 2) {//如果当前页大于等于最大页码-2
                firstPage = maxPageNo - 4;  //后 5 页
                $scope.lastDot = false;//后边没点
            } else { //显示当前页为中心的 5 页
                firstPage = $scope.paramMap.pageNo - 2;
                lastPage = $scope.paramMap.pageNo + 2;
            }
        } else {
            $scope.firstDot = false;//前面无点
            $scope.lastDot = false;//后边无点
        }
        //循环产生页码标签
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }

})