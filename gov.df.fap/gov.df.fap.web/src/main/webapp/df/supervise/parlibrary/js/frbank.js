require(['jquery','knockout', '/df/supervise/ncrd.js','csscsof','/df/supervise/fileUpload/js/jquery.media.js','bootstrap','dateZH', 'uui','tree', 'grid', 'ip','csof'], function($,ko, ncrd,csscsof,media) {
        window.ko = ko;
        var GET_DOCUMENTTREE_URL = '/df/csofdocument/getdocumentTree.do';//查询法规类型
        var GET_DOCUMENTBYID_URL = '/df/csofdocument/getdocumentById.do'; //通过法规类型查询政策法规
        var GET_DOCUMENT_URL = '/df/csofdocument/getdocument.do'; //查询政策法规
        var SAVE_DOCUMENT_URL = '/df/csofdocument/savedocument.do';//新增政策法规
        var DELETE_DOCUMENT_URL = '/df/csofdocument/deleteDocument.do';//删除政策法规	
        var UPDATE_DOCUMENT_URL = '/df/csofdocument/updatedocument.do';//编辑政策法规	
        /* 请求链接 */
        var requireUrl = {
            OPEN_ATTACH : '/df/cs/previewFile.do?tokenid='+ ip.getTokenId() + '&ajax=noCache',  //附件预览
            CANCEL_OPEN_ATTACH: '/df/cs/deletepreviewFile.do?tokenid='+ ip.getTokenId() + '&ajax=noCache',  //取消预览
        };

        var nodeSelected;//左侧树被选中节点
        var uuid;
        var tokenid,
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
            	"modular": "INFO",
                "entity_id":pageData.ID,
                "oid":options.svOfficeId,
                "dep_id":options.svDepId,
                "dep_code":options.svDepCode,
                "edit":'Y',
            };
            $("#filePage")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
                "&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&modular="+fileData.modular+"&entityName=csof_info_document&oid="+fileData.oid+
                "&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit;
        };

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

        var viewModel = {
        		//政策法规库列表
//        		gridDataTable: new u.DataTable({
//                    meta: {
//                    	
//                    }
//                }),
                //左树（法规分类树）
                treeDataTable: new u.DataTable({
                    meta: {
                    	'CHR_ID': {},
                        'PARENT_ID': {},
                        'CHR_NAME':{}
                    }
                }),
                treeSetting:{
                    view:{
                        showLine: false,
                        selectedMulti: false,
                        showIcon: false,
                        showTitle: true
                    },
                    callback:{
                    	 onClick: zTreeOnClick
                    }
                }
        };


        //以下为政策法规库(新)的方法
        /**
         * @method addFr 添加
         * @method releaseFr 确定 @param reflag: 0为新增 1为修改
         * @method cancelAddFr 取消添加操作 
         */
        var reflag;
        addFr = function() {
        	var treeObj = $.fn.zTree.getZTreeObj("tree1");
			var node = treeObj.getSelectedNodes()[0];
        	if( node ) {
        		if(node.CHR_ID == 'type' || node.IS_LEAF == 0) {
        			ip.ipInfoJump("请选择具体分类！！", 'info');
        		}
        		else {
        			$("#addFrModal").modal({
        				show : true,
        				backdrop:"static"
        			});
        			$("#frTitle").val('');
           		 	$("#kWord").val('');
            		$("#groupName").val(node.CHR_NAME);
            		pageData.getUUID();
            		reflag = 0;
        		}
        	}
        	else {
        		ip.ipInfoJump("请先选择一个分类！！", 'info');
        	}
        }
        
        viewModel.releaseFr = function () {
        	if(reflag == 0) {
        		var title = $("#frTitle").val();
            	var summary = $("#kWord").val();
            	if(title) {
	            	var treeObj = $.fn.zTree.getZTreeObj("tree1");
	    			var node = treeObj.getSelectedNodes()[0];
	            	var save_data = {
	            			"id":pageData.ID,
	            			"doctype_id": node.CHR_ID,
	            			"title": title,
	            			"summary":summary 
	            	}
	            	$.ajax({
	                    type: 'GET',
	                    url: SAVE_DOCUMENT_URL + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
	                    data: save_data,
	                    dataType: 'JSON',
	                    async: false,
	                    success: function (result) {
	                    	if(result.errorCode == 0) {
	                    		$("#frTitle").val('');
	                    		$("#kWord").val('');
	                        	$("#addFrModal").modal('hide');
	                    		ip.ipInfoJump("新增成功！！", 'success');
	                    		zTreeOnClick(event,node.CHR_ID,node);
	                    	}
	                    	else {
	                    		ip.ipInfoJump("请上传文件！", 'info');
	                    	}
	                    },
	            	 	error: function (result) {
	            	 		ip.ipInfoJump("新增失败！！！", 'error');
	            	 		$("#addFrModal").modal('hide');
	            	 	}
	                });
            	}
            	else {
            		ip.ipInfoJump("请将信息填写完整！", 'error');
            	}
        	}
        	if(reflag == 1) {
        		var treeObj = $.fn.zTree.getZTreeObj("tree1");
    			var node = treeObj.getSelectedNodes()[0];
        		var title = $("#frTitle").val();
            	var summary = $("#kWord").val();
            	if( title ) {
            		var modify_list =  viewModel.listGridViewModel.gridData.getSelectedRows()[0];
                	var modify_data = {
                			"id":modify_list.data.ID.value,
                			"doctype_id": modify_list.data.DOCTYPE_ID.value,
                			"summary":summary,
                			"title": title
                	}
                	$.ajax({
                        type: 'GET',
                        url: UPDATE_DOCUMENT_URL + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
                        data: modify_data,
                        dataType: 'JSON',
                        async: false,
                        success: function (result) {
                        	if(result.errorCode == 0) {
                        		$("#frTitle").val('');
                        		$("#kWord").val('');
                        		$("#addFrModal").modal('hide');
                        		ip.ipInfoJump("修改成功！！", 'success');
                        		zTreeOnClick(event,node.CHR_ID,node);
                        	}
                        	else {
                        		ip.ipInfoJump("请上传文件！", 'info');
                        	}
                        },
                        error: function (result) {
                	 		ip.ipInfoJump("修改失败！！！", 'error');
                	 		$("#addFrModal").modal('hide');
                	 	}
                    });
            	}
            	else {
            		ip.ipInfoJump("请将信息填写完整！", 'error');
            	}
        	}
        }
        viewModel.cancelAddFr = function() {
        	$("#frTitle").val('');
   		 	$("#kWord").val('');
        	$("#addFrModal").modal('hide');
        }
        
       
        /**
         * 修改
         * @method modifyFr 点击修改按钮
         */
        modifyFr = function() {
        	var modify_list = viewModel.listGridViewModel.gridData.getSelectedRows();
        	if( modify_list.length > 0 ) {
        		if( modify_list.length > 1 ) {
        			ip.ipInfoJump("只能选择一条数据进行修改！！", 'info');
        		}
        		else {
        			$("#addFrModal").modal({
        				show : true,
        				backdrop:"static"
        			});
            		$("#groupName").val(modify_list[0].data.DOCTYPE_NAME.value);
            		$("#frTitle").val(modify_list[0].data.TITLE.value);
           		 	$("#kWord").val(modify_list[0].data.SUMMARY.value);
           		 	pageData.ID = modify_list[0].data.ID.value;
           		 	publicFileman();
           		 	reflag = 1;
        		}
        	}
        	else {
        		ip.ipInfoJump("请先选择一条法规！！", 'info');
        	}
        }
        
        
        /**
         * @method delFr 点击删除按钮 
         * 可多条或者单条删除
         * @method delConfirmSureId 确认删除所选中的法规
         * @method delConfirmCancelCla 取消删除所选中的法规
         * 首先需要获取要删除的法规的信息
         */
        delFr = function() {
        	var del_list = viewModel.listGridViewModel.gridData.getSelectedRows();
        	var treeObj = $.fn.zTree.getZTreeObj("tree1");
			var node = treeObj.getSelectedNodes()[0];
			var send_del_list = [];
        	if( del_list.length > 0 ){
		        		for(var i = 0; i < del_list.length; i++) {
		        			var send_del_list_arr = {
		        					"file_id" :  del_list[i].data.FILE_ID.value ,
		            				"id" : del_list[i].data.ID.value
		        			}
		        			send_del_list.push(send_del_list_arr);
		        		}
		        		ip.warnJumpMsg('是否删除所选法规!',"delConfirmSureId","delConfirmCancelCla");
		        		 $('#delConfirmSureId').on('click',function(){
		                     $('#config-modal').remove();
		                     $.ajax({
		                         type: 'GET',
		                         url: DELETE_DOCUMENT_URL + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
		                         data:{ "dataStr" : JSON.stringify(send_del_list) },
		                         dataType: 'JSON',
		                         async: false,
		                         success: function (result) {
		                         	if(result.errorCode == 0) {
		                         		ip.ipInfoJump("删除成功！", "success");
		                         		zTreeOnClick(event,node.CHR_ID,node);
		                         	 }
		                         	else{
		                         		ip.ipInfoJump("删除失败！", "error");
		                         	}	
		                         },
		                         error: function (result) {
		                        	 ip.ipInfoJump("删除失败！", "error");
		                         }
		                     });
		                 });
		                 $('.delConfirmCancelCla').on('click',function(){
		                     $('#config-modal').remove();
		                 });
        	}
        	else{
        		ip.ipInfoJump("您没有选择任何需要删除的数据！", 'info');
        	}
        }
        /*
         * @method previewFr 预览
         * 选择一条法规进行预览
         * 
         * */
        var fileName;
        previewFr = function () {
        	var pre_list =  viewModel.listGridViewModel.gridData.getSelectedRows();
        	if( pre_list.length > 0 ) {
        		if(pre_list.length > 1) {
        			ip.ipInfoJump("只能选择一条法规进行预览！", 'info');
        		}
        		else {
                	$('#preview').html('');
        			$("#previewModal").modal({
        				show : true,
        				backdrop:"static"
        			});
        			$('#preview').html('文件加载中。。。');
        			$.get(requireUrl.OPEN_ATTACH, {attach_id : pre_list[0].data.FILE_ID.value}, function (map) {
                      	if(map.errorCode == '0'){
                      		if(map.data.typeFlag == '0') {
                      			$('#preview').media({width:870,height:430,src:map.data.htmlString});
                      			$('#preview').css('overflow','none');
                      		} else if(map.data.typeFlag == '1') {
                      			var imgHtml = '<img src="' + map.data.htmlString + '" alt="" />';
            					$('#preview').html(imgHtml);
                      		} else {
                      			$('#preview').html(map.data.htmlString);
                      		}
                      		fileName = map.data.fileName;
                      	}
                      	else {
                      		$('#preview').html('文件加载失败！');
                      	}
                      });
        		}
        	}
        	else {
        		ip.ipInfoJump("请选择一条法规预览！", 'info');
        	}
        }
        
        //取消预览
        viewModel.cancelPre = function() {
        	$("#previewModal").hide();
        	$.get(requireUrl.CANCEL_OPEN_ATTACH, {fileName : fileName} ,function(map) {
        		if(map.errorCode == '0') {
        			
        		}
        	});
        }
        
        //获取左侧要素树
        viewModel.getDocumentTree = function (treeElecode) {
        	$.ajax({
                type: 'GET',
                url: GET_DOCUMENTTREE_URL + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
                data: {"ele_code": treeElecode},
                dataType: 'JSON',
                async: false,
                success: function (result) {
                	if(result.errorCode == 0) {
                		var data = result.data;
                        var obj = {
                            CHR_ID : 'type',
                            PARENT_ID : 'root',
                            CHR_NAME : '全部'
                        };
                        $.each(data, function(index, item){
                            if(item.LEVEL_NUM == '1'){
                                item.PARENT_ID = 'type';
                            }
                        });
                        data.push(obj);
   						viewModel.treeDataTable.setSimpleData(data,{unSelect:true});
   						var treeObj = $.fn.zTree.getZTreeObj("tree1");
   						treeObj.expandAll(true);
   					 }
   					 else{
   						 ip.ipInfoJump("左侧数据加载失败！", "info");
   					 }
                }
            });
        }
        
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
                	viewModel.listGridViewModel = ip.initGridWithCallBack(gridCallback,viewModel.listViewList[n],'frbankGrid', '/df/csofdocument/getdocument.do?', options, 0, false,true,true,false);
                    if(viewModel.listGridViewModel.gridData.getSimpleData()){
                        $("#mainGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);
                    }
                }    
            }
        };
        
        //获取options
        getOptions = function() {
        	var pageInfo = options["pageInfo"];
        	options = ip.getCommonOptions({});
            options['tokenid'] = tokenid;
            options['ajax'] = "noCache";
            options['pageInfo'] = pageInfo;
        }
        
        //回调函数，定义全局变量viewModel.curGridData并赋值
        gridCallback = function(data){
            viewModel.curGridData = data;
        };

        //查询全部
        viewModel.gridRefresh = function() {
        	getOptions();
            ip.setGridWithCallBack(gridCallback,viewModel.listGridViewModel, '/df/csofdocument/getdocument.do?tokenid='+ tokenid + '&ajax=' + 'noCache', options);
            $("#mainGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);
        };
        
        //通过类型id查询列表
        viewModel.gridRefreshById = function(id) {
        	getOptions();
        	options['id'] = id;
            ip.setGridWithCallBack(gridCallback,viewModel.listGridViewModel, '/df/csofdocument/getdocumentById.do?tokenid='+ tokenid + '&ajax=' + 'noCache', options);
            if(viewModel.listGridViewModel.gridData.getSimpleData()){
                $("#mainGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);
            }else{
            	$("#mainGridSumNum").html("0");
            }
        };
        
        //左侧树点击事件
        function zTreeOnClick(event, treeId, treeNode){
        	nodeSelected = treeNode;
            var treeObj = $.fn.zTree.getZTreeObj("tree1");
            var node = treeObj.getNodes();
            var nodes = treeObj.transformToArray(node);
        	if(nodeSelected.CHR_ID == "type") {
        		viewModel.gridRefresh();
        	}
        	else {
        		viewModel.gridRefreshById(nodeSelected.CHR_ID);
        	}
        } 
        
        function windowHeight() {
            var h = document.body.clientHeight; //获取当前窗口可视操作区域高度
            var bodyHeight = document.getElementById("main-content"); //寻找ID为content的对象
            $('#main-content').jqxSplitter({ width: '98.3%', height: (h - 65) + "px", panels: [{ size: '23.2%', min: 200 }, { min: 800, size: '76.8%' }] });
            $('.treeDiv').height($("#left-main-content").height() - 10 + "px");
            $('.rightTableGrid').height($("#right-main-content").height() - 26 + "px");
        }
        setInterval(windowHeight(), 500)//每半秒执行一次windowHeight函数

        //初始化
        function init() {
            app = u.createApp({
                el: document.body,
                model: viewModel
            });
            tokenid = ip.getTokenId();
            options = ip.getCommonOptions({});
            options['tokenid'] = tokenid;
            var url=window.location.href;
            elecode = url.split("elecode=")[1].split("&")[0];
            menuId = url.split("menuid=")[1].split("&")[0];
            viewModel.getDocumentTree(elecode);
            windowHeight();
            viewModel.initData();
        };
        init();
    }
);