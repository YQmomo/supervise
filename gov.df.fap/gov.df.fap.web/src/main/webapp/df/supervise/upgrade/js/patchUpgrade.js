require(['jquery','knockout', '/df/supervise/ncrd.js', 'bootstrap','dateZH', 'uui','tree', 'grid', 'ip','csof'], function($,ko, ncrd) {
        window.ko = ko;
        var SAVE_UPGRADE_URL= '/df/csofupgrade/saveUpgrade.do';//上传
        var DO_UPGRADE_URL= '/df/csofupgrade/doUpgrade.do';//执行
        var GET_UPGRADE_URL='/df/csofupgrade/getUpgrade.do';//查询列表
        var DELETE_UPGRADE_URL='/df/csofupgrade/deleteUpgrade.do';//删除待升级的补丁

        var tokenid,
        	usercode,
            options,
            elecode,
            menuId,
            menuName,
            elecode,
            billTypeCode,
            num = 0;//根据页签的长度定值

        var pViewType = {
            VIEWTYPE_INPUT : "001",// 录入视图
            VIEWTYPE_LIST : "002",// 列表视图
            VIEWTYPE_QUERY : "003"// 查询视图
        };
        var viewData = {};//视图的数据
        var postData = {};//通过post传到后台的数据
        var treeState = {};//点击主树节点的状态
        var postAction;

        var dealData = {//处理数据
            showTreeColor: function (treeId, treeNode) {//显示非最终子节点的树节点为蓝色
                return treeNode.level == 0 ? {color:"blue"} : {};
            },
            getIdLast: function(data){//将{***}的***提取出来
                var par = /[{}]/g;
                var text = data.replace(par,'');
                return text;
            },
        };
        /* 请求链接 */
        var requireUrl = {
            OPEN_ATTACH : '/df/cs/previewFile.do?tokenid='+ ip.getTokenId() + '&ajax=noCache',  //附件预览
        };
        var viewModel = {
        		//升级列表
                updataTableGrid1 : new u.DataTable({
                    meta: {
                        
                    }
                }),
                //按钮
                btnDataTable: new u.DataTable({
                	meta: {

                	}
                }),
        };
        
      //生成表头
        viewModel.initData = function(areaType,areaId) {
            var current_url = location.search;
            $.ajax({
                url : "/df/init/initMsg.do",
                type : "GET",
                dataType : "json",
                async : true,
                data : options,
                success : function(datas) {
                    viewModel.viewList = datas.viewlist;// 视图信息
                    viewModel.resList = datas.reslist;// 资源信息
                    viewModel.coaId = datas.coaId;// coaid
                    viewModel.coaDetails = datas.coaDetail;// coa明细
                    viewModel.relations = datas.relationlist;// 关联关系
                    viewModel.inputViewList = []; 
                    viewModel.listViewList = []; 
                    for(var i=0;i<datas.viewlist.length;i++) {
                    	if(datas.viewlist[i].viewtype == pViewType.VIEWTYPE_INPUT){
                    		viewModel.inputViewList.push(datas.viewlist[i]);
                    	}else if(datas.viewlist[i].viewtype == pViewType.VIEWTYPE_LIST){
                    		viewModel.listViewList.push(datas.viewlist[i]);
                    	}
                    }
                    viewModel.initGridData(); //调用初始化查询表格
                    //初始化录入视图
                    if(viewModel.inputViewList.length>0){
                        viewModel.vailData = ip.initArea(viewModel.inputViewList[0].viewDetail,areaType,dealData.getIdLast(viewModel.inputViewList[0].viewid), areaId);
                    }
                }
            });
        };
        
        viewModel.initGridData = function() {
            var queryViewId;
            var tableViewDetail;
            var queryViewDetail;
            for ( var n = 0; n < viewModel.listViewList.length; n++) {
                var view = viewModel.listViewList[n];
                if(view.orders == '0'){
                	viewModel.listGridViewModel = ip.initGridWithCallBack(gridCallback,viewModel.listViewList[n],'upgradeGrid', '/df/csofupgrade/getUpgrade.do?', options, 1, false,true,false,false);
                    if(viewModel.listGridViewModel.gridData.getSimpleData()){
                        $("#mainGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);
                    }
                }    
            }
        };
        
        //grid查询
        viewModel.gridSearch = function () {
            ip.fuzzyQuery(viewModel.curGridData, "gridSearchInput", viewModel.listGridViewModel);
        };

        //回调函数，定义全局变量viewModel.curGridData并赋值
        gridCallback = function(data){
            viewModel.curGridData = data;
        };

        //刷新Grid表格数据
        viewModel.gridRefresh = function() {
            ip.setGridWithCallBack(gridCallback,viewModel.listGridViewModel, '/df/csofupgrade/getUpgrade.do?tokenid='+ tokenid + '&ajax=' + 'noCache'," ");
            $("#mainGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);
        };
        
        //刷新Grid表格数据
        viewModel.gridRefresh1 = function(iscommit) {
            ip.setGridWithCallBack(gridCallback,viewModel.listGridViewModel, '/df/csofupgrade/getUpgradeByIscommit.do?tokenid='+ tokenid + '&ajax=' + 'noCache', {'iscommit': iscommit});
            $("#mainGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);
        };
        
        //初始化状态框
        viewModel.initStatus = function() {
        	//主单状态
        	$("#mainGridStatus").html('<label><input name="mainStatus" type="radio" value="9" onchange ="submitStatusChange(this.value)"/>全部 </label> '+
    	        	'<label><input name="mainStatus" type="radio" value="0" onchange ="submitStatusChange(this.value)"/>待升级</label> '+
    	        	'<label><input name="mainStatus" type="radio" value="1" onchange ="submitStatusChange(this.value)"/>已升级</label>');
   		    var radio =document.getElementsByName("mainStatus");
     	    for(var i = 0; i < radio.length; i++){
	        	   if(radio[i].value == "9") {
	        		  radio[i].checked = true;
	        	   }else{
	        		  radio[i].checked = false;
	        	   }
     	    }
        } 
        
        //select状态改变
        submitStatusChange = function(val){
        	if(val == 9) {
        		viewModel.gridRefresh();
        	}
        	else {
        		viewModel.gridRefresh1(val);
        	}
        };     
        
        var pageData  = {
        		getUUID: function(){
                    $.post('/df/task/getUUID.do?tokenid='+ tokenid + '&ajax=' + 'noCache',function(map){
                        pageData.ID = map;
                        publicFileman();
                    });
                }
        }
        
        var publicFileman = function() {
            $("#filePage")[0].src = "";
            var fileData = {
            	"modular":"SYS",
                "entity_id":pageData.ID,
                "oid":options.svOfficeId,
                "dep_id":options.svDepId,
                "dep_code":options.svDepCode,
                "edit":'Y',
            };
            $("#filePage")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
                "&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&modular="+fileData.modular+"&entityName=csof_bug_sql&oid="+fileData.oid+
                "&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit;
        };
        /*
         * @method 上传方法
         * 
         * */
        upgradeUpload = function(id) {
        	$('#uploadModal').modal({
                show : true,
                backdrop : 'static'
            });
        	pageData.getUUID();
        }
        //确认上传
        viewModel.confirmUpload = function() {
        	var remark = $("#remark").val();
        	$.ajax({
                type: 'GET',
                url: SAVE_UPGRADE_URL + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
                data: {
                	"id":pageData.ID,
                	"remark":remark
                },
                dataType: 'JSON',
                async: false,
                success: function (result) {
                	if(result.errorCode == 0) {
                		$('#uploadModal').modal('hide');
                		ip.ipInfoJump("上传成功！！", 'success');
                		var radioVal = $("input[type=radio]:checked").val()
                		if(radioVal == 9) {
                			viewModel.gridRefresh();
                		}
                		if(radioVal == 0){
                			viewModel.gridRefresh1(radioVal);
                		}
                		if(radioVal == 1) {
                			viewModel.gridRefresh1(radioVal);
                		}
                	}
                	else{
                		$('#uploadModal').modal('hide');
                		ip.ipInfoJump("上传失败！请正确上传文件", 'error');
                	}
                },
                error: function (result) {
                	$('#uploadModal').modal('hide');
            		ip.ipInfoJump("上传失败！", 'error');
                }
            });
        }
        //取消上传
        viewModel.cancelUpload = function() {
        	$("#remark").val('');
        	$('#uploadModal').modal('hide');
        }
        
        //表格单选之前事件
        upgradeGrid_onBeforeRowSelected = function(obj) {
        	var list = viewModel.listGridViewModel.gridData.getSelectedRows();
        	if(list.length > 0) {
        		ip.ipInfoJump("只能选择一条数据进行操作！", 'info');
        		return false;
        	}
        	else {
        		return true;
        	}
        }
        //表格行单选事件
        upgradeGrid_onRowSelected = function(obj) {
    		$('#myTab a[href="#home"]').tab('show');
    		$("#home").html('');
    		//选中状态才能进行查看
    		if(obj.rowObj.checked == true) {
    			$.get(requireUrl.OPEN_ATTACH, {attach_id : obj.rowObj.value.SQL_ID}, function (map) {
                 	if(map.errorCode == '0'){
                 		$("#home").html(map.data.htmlString);
                 	} else {
                 		$("#home").html('文件加载失败！');
                 	}
                 });
    		}
        }
        /*
         * @method 执行方法
         * 
         * 
         * */
        upgradeExe = function(id) {
        	var do_list = viewModel.listGridViewModel.gridData.getSelectedRows()[0];
        	if(do_list) {
        		if(do_list.data.ISCOMMIT.value == "待升级") {
        			$("#uppwd").val('');
             		$('#exeModal').modal({
                        show : true,
                        backdrop : 'static'
                    });
        		}
        		else {
        			ip.ipInfoJump("已升级！", 'info');
        		}
        	}
        	else {
        		ip.ipInfoJump("请选择需要执行的数据", 'info');
        	}
        }
        var myDate = new Date();
        /*
         * @method 确认执行方法
         * 确认之后弹框取消，执行日志tab active,显示执行日志
         * 
         * */
        viewModel.confirmExe = function() {
        	var pwd = $("#uppwd").val();
        	var password = hex_md5(pwd);
        	$('#exeModal').modal('hide');
        	var do_list = viewModel.listGridViewModel.gridData.getSelectedRows()[0];
        	var html = "------"+myDate.toLocaleString( )+" "+ do_list.data.APP_VERSION.value + " "+"补丁开始执行"+"------";
        	$('#myTab a[href="#profile"]').tab('show');
        	$("#profile").html(html);
        	var radioVal = $("input[type=radio]:checked").val();
        	$.ajax({
                    type: 'GET',
                    url: DO_UPGRADE_URL + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
                    data: {
                    	"id" : do_list.data.ID.value,
                    	"file_id" : do_list.data.SQL_ID.value ,
                    	"pwd" : password,
                    	"usercode": usercode
                    },
                    dataType: 'JSON',
                    async: false,
                    success: function (result) {
                    	if(result.errorCode == 0) {
                    		ip.ipInfoJump("执行成功", 'success');
                    		$('#exeModal').modal('hide');
                    		if(radioVal == 9) {
                    			viewModel.gridRefresh();
                    		}
                    		if(radioVal == 0){
                    			viewModel.gridRefresh1(radioVal);
                    		}
                    		if(radioVal == 1) {
                    			viewModel.gridRefresh1(radioVal);
                    		}
                    		html += "<br/>------"+result.message1 +"------ "+"<br/>------"+myDate.toLocaleString( )+ " "+do_list.data.APP_VERSION.value +" "+"补丁执行完成"+"------";
                    		$("#profile").html(html);
                    	}
                    	if(result.errorCode == "-1") {
                    		html += "<br/>------"+result.errorMsg +"------ ";
                    		$("#profile").html(html);
                    		ip.ipInfoJump(result.errorMsg+"执行失败。", 'error');
                    	}
                    },
                    error: function(result) {
                    	html += "<br/>------"+result.errorMsg +"------ ";
                		$("#profile").html(html);
                    	ip.ipInfoJump("执行失败", 'errorCode');
                    }
                });
        }
        /*
         * @method 取消执行方法
         * 
         * 
         * */
        viewModel.cancelExe = function() {
        	$("#uppwd").val('');
        	 $('#exeModal').modal('hide');
        }
        /*
         * @method 删除方法
         * 
         * 
         * */
        upgradeDel = function(id) {
        	var del_list = viewModel.listGridViewModel.gridData.getSelectedRows()[0];
        	var radio =document.getElementsByName("mainStatus");
        	var radioVal = $("input[type=radio]:checked").val()
        	//在未升级的情况下，允许删除补丁
        		if(del_list.data.ISCOMMIT.value == "待升级"){
            		$.ajax({
                        type: 'GET',
                        url: DELETE_UPGRADE_URL + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
                        data: {
                        	"id" : del_list.data.ID.value,
                        	"file_id" :del_list.data.SQL_ID.value
                        },
                        dataType: 'JSON',
                        async: false,
                        success: function (result) {
                        	if(result.errorCode == 0) {
                        		ip.ipInfoJump("删除成功", 'success');
                        		if(radioVal == 9) {
                        			viewModel.gridRefresh();
                        		}
                        		if(radioVal == 0){
                        			viewModel.gridRefresh1(radioVal);
                        		}
                        		if(radioVal == 1) {
                        			viewModel.gridRefresh1(radioVal);
                        		}
                        	}
                        	else {
                        		ip.ipInfoJump("删除失败", 'error');
                        	}
                        }
                    });
            	}
        		else {
            		ip.ipInfoJump("文件已升级，不能删除！", 'info');
            	}
        	$('#myTab a[href="#home"]').tab('show');
    		$("#home").html('');
        }
        
        
        //初始化
        function init() {
            app = u.createApp({
                el: document.body,
                model: viewModel
            });
            tokenid = ip.getTokenId();
            options = ip.getCommonOptions({});
            usercode = options['svUserCode'];
            options['tokenid'] = tokenid;
            var url=window.location.href;
            menuId = url.split("menuid=")[1].split("&")[0];
           // 初始化按钮
            var data = initBtns(menuId);
            if(data == false){
                ip.ipInfoJump("加载按钮出错", 'error');
            }else{
                viewModel.btnDataTable.setSimpleData(data);
            }
            viewModel.initData();
            viewModel.initStatus();
        };
        init();
    }
);