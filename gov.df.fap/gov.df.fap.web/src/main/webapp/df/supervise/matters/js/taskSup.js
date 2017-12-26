/**
 * Created by yanqiong on 2017/11/6.
 */
require(['jquery','knockout', '/df/supervise/ncrd.js','csscsof', 'bootstrap','dateZH', 'uui','tree', 'grid', 'ip','csof'], function($,ko, ncrd,csscsof) {
        window.ko = ko;
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

        var dealData = {//处理数据
            showTreeColor: function (treeId, treeNode) {//显示非最终子节点的树节点为蓝色
                return treeNode.level == 0 ? {color:"blue"} : {};
            },
            getIdLast: function(data){//将{***}的***提取出来
                var par = /[{}]/g;
                var text = data.replace(par,'');
                return text;
            },
            delQuotation : function(data){//将"***"的***提取出来
                var par = /\"/g;
                var text = data.replace(par,'');
                return text;
            },
        };

        var viewModel = {
            SearchTreeKEY: ko.observable(""),
            //模糊查询单位树
            searchTree : function () {
                ncrd.findTreeNode($.fn.zTree.getZTreeObj("tree1"), viewModel.SearchTreeKEY());
            },
            //按钮
            btnDataTable: new u.DataTable({
                meta: {

                }
            }),
            treeDataTable: new u.DataTable({
                meta: {
                    'chr_id': {},
                    'PARENT_ID': {},
                    'chr_name':{}
                }
            }),//监管类型事项树
            treeSetting:{
                view:{
                    showLine: false,
                    selectedMulti: false,
                    showIcon: false,
                    showTitle: true
                },
                callback:{
                    onClick:function(e,id,node){
                    	var btnData = viewModel.btnDataTable.getSimpleData();
                    	treeState = node;
                    	//pageData.tree1Node = node;
                        treeState.isLastNode = node.isLastNode;
                        treeState.isParent = node.isParent;
                        var listId = node.chr_id || node.CHR_ID;//字段id不确定,根据后台传来的字段确定
                        if(elecode == "CSOF_OFFICE"){
                        	var listCode = "OID";
                        }else if(elecode == "CSOF_SUPTYPE"){
                            var listCode = "SUP_TYPE_ID";
                        }
                        treeState.treeId = listId;
                        treeState.treeCode = listCode;
                        var radio =document.getElementsByName("mainStatus");
                		var submit_status = null;
                    	for(var i =0;i < radio.length;i++){
            	        	 if(radio[i].checked){
            	        	     submit_status = radio[i].value;
            	        	 }
                    	}
                    	if(node.CHR_NAME == '全部' || node.chr_name == '全部'){
                            viewModel.gridRefresh("",submit_status);
                        }else{
                            var conditionMap  = {};
                        	conditionMap[listCode] = node.CHR_ID;
                            viewModel.gridRefresh(conditionMap,submit_status);
                        }
                    }
                }
            },
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
            getOptions();
            for ( var n = 0; n < viewModel.listViewList.length; n++) {
                var view = viewModel.listViewList[n];
                if(view.orders == '0'){
                	options["menu_id"] = menuId;
                	options["status"] = "9";
                	options["billtype_code"] = billtype_code;
                	viewModel.listGridViewModel = ip.initGridWithCallBack(gridCallback,viewModel.listViewList[n],'mainGrid', viewModel.listViewList[n].remark + '?', options, 1, false,true,false,false);
                    if(viewModel.listGridViewModel.gridData.getSimpleData()){
                        $("#mainGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);
                    }
                    viewModel.changeBtnStatusBysetGrid(viewModel.listGridViewModel);
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
        viewModel.gridRefresh = function(conditionMap,status) {
        	getOptions();
        	options["menu_id"] = menuId;
        	if(!status || status == ""){
        		status = "9";
        	}
        	options["status"] = status;
        	options["billtype_code"] = billtype_code;
        	if(conditionMap || conditionMap != ""){     		
       	        options["conditionMap"] = JSON.stringify(conditionMap);
       	    }
            ip.setGridWithCallBack(gridCallback,viewModel.listGridViewModel, viewModel.listViewList[0].remark + '?',options);
            $("#mainGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);        
        };
        
        viewModel.changeBtnStatusBysetGrid = function(gridViewModel) {
        	gridViewModel.gridData.on('select', function(e) {
    			var btnData = viewModel.btnDataTable.getSimpleData();
        		for(var i=0;i<e.rowIds.length;i++) {
        			var status = gridViewModel.gridData.getRowByRowId(e.rowIds[i]).getValue('status_code');
		        	for(var j=0;j<btnData.length;j++) {
		        		var btnId = btnData[j].BUTTON_ID;
		        		if(btnData[j].DISPLAY_STATUS == status || (btnData[j].DISPLAY_STATUS == "9" && btnData[j].ENABLED == "1")) {
		        			$("#"+btnId).removeAttr("disabled","disabled");
		        		}else {
		        			$("#"+btnId).attr("disabled","disabled");
		        		}
		        	}
        		}
            });
        }
        
      //初始化状态框
        viewModel.initStatus = function() {
        	//主单状态
        	$("#mainGridStatus").html('<label><input name="mainStatus" type="radio" value="9" onchange ="submitStatusChange(this.name)"/>全部 </label> '+
    	        	'<label><input name="mainStatus" type="radio" value="0" onchange ="submitStatusChange(this.name)"/>待办理</label> '+
    	        	'<label><input name="mainStatus" type="radio" value="1" onchange ="submitStatusChange(this.name)"/>已办理</label>');
   		    var radio =document.getElementsByName("mainStatus");
     	    for(var i =0;i < radio.length;i++){
	        	   if(radio[i].value == "9"){
	        		  radio[i].checked = true;
	        	   }else{
	        		  radio[i].checked = false;
	        	   }
     	    }
//     	    //副单状态
//     	   $("#detailGridStatus").html('<label><input name="detailStatus" type="radio" value="9" onchange ="submitStatusChange(this.name)"/>全部 </label> '+
//   	        	'<label><input name="detailStatus" type="radio" value="10" onchange ="submitStatusChange(this.name)"/>待生成</label> '+
//   	        	'<label><input name="detailStatus" type="radio" value="11" onchange ="submitStatusChange(this.name)"/>已生成 </label>');
//  		    var radio =document.getElementsByName("detailStatus");
//    	    for(var i =0;i < radio.length;i++){
//	        	   if(radio[i].value == "9"){
//	        		  radio[i].checked = true;
//	        	   }else{
//	        		  radio[i].checked = false;
//	        	   }
//    	    }
        } 
        
        //select状态改变
        submitStatusChange = function(type){
        	var radio =document.getElementsByName(type);
    		var submit_status = null;
    		var conditionMap  = {};
        	for(var i =0;i < radio.length;i++){
	        	 if(radio[i].checked){
	        	     submit_status = radio[i].value;
	        	 }
        	}
        	if(type == "mainStatus"){
            	var treeObj = $.fn.zTree.getZTreeObj("tree1");
            	var nodes = treeObj.getSelectedNodes();
            	if(nodes.length > 0) {
            		var selectNodeId = nodes[0].CHR_ID;
            		if(nodes[0].CHR_NAME != '全部' && nodes[0].chr_name != '全部'){
            			if(elecode == "CSOF_OFFICE"){
                        	var type_code = "OID";
                        }else if(elecode == "CSOF_SUPTYPE"){
                            var type_code = "SUP_TYPE_ID";
                        }
                  	    conditionMap[type_code] = selectNodeId;
                    }
                }
                viewModel.gridRefresh(conditionMap,submit_status);
        	}
//        	else if(type="detailStatus"){
//        		if(submit_status != "9"){
//                	conditionMap["status_code"] = submit_status;
//        		}
//        		viewModel.detailgridRefresh(conditionMap);
//        	}
        	var btnData = viewModel.btnDataTable.getSimpleData();
            if(submit_status == "9") {
            	for(var i=0;i<btnData.length;i++) {
            		if(btnData[i].ENABLED == "1" && btnData[i].DISPLAY_STATUS != "2") {
            			var btnId = btnData[i].BUTTON_ID;
                		$("#"+btnId).removeAttr("disabled","disabled");
            		}
            	}
            }else {
            	for(var i=0;i<btnData.length;i++) {
            		var btnId = btnData[i].BUTTON_ID;
            		if(btnData[i].DISPLAY_STATUS == submit_status || (btnData[i].DISPLAY_STATUS == "9" && btnData[i].ENABLED == "1")) {
            			$("#"+btnId).removeAttr("disabled","disabled");
            		}else {
            			$("#"+btnId).attr("disabled","disabled");
            		}
            	}
            }           
        };     
        
        //工作流提交
        workFlowSubmit = function(id) {
        	workFlow(id,'0');
        }
        //工作流审核
        workFlowSup = function(id) {
        	workFlow(id,'0');
        }
      //工作流撤销
        workFlowUndo = function(id) {
        	workFlow(id,'1');
        }
      //工作流退回
        workFlowReturn = function(id) {
        	workFlow(id,'0');
        }
        
        //工作流结束
        workFlowEnd = function(id) {
        	workFlow(id,'1');
        }

        //工作流/df/workflow/work.do
        workFlow = function(id,status) {
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	var btn = getBtnMsgById(btnData,id);
        	var selectRow = viewModel.listGridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
        		var flag = 0;
        		for(var i=0;i<selectRow.length;i++){
        			if(selectRow[i].data.status_code.value == status){
        				var data = {
                    			"menu_id":menuId,
                    			"entity_id":selectRow[i].data.id.value,
                    			"billtype_code":billtype_code,
                    			"op_type":btn.ACTION_TYPE,
                    			"op_name":btn.DISPLAY_TITLE,
                    	};
                		$.ajax({
                            type: 'get',
                            url: "/df/workflow/work.do?tokenid=" + tokenid + "&ajax=noCache",
                            data: data,
                            dataType: 'JSON',
                            async: false,
                            success: function (result) {
                            	if(result.data == false) {
                            		ip.ipInfoJump("第"+ i+1 + "条数据"+btn.DISPLAY_TITLE+"失败", 'info');
                            		flag = -1;
                            	}else {

                            	}
                            }, error: function () {
                            	ip.ipInfoJump("第"+ i+1 + "条数据"+btn.DISPLAY_TITLE+"失败", 'info');
                        		flag = -1;
                            }
                        });  
                		if(flag == -1) {
                			break;
                		}
        			}else{
        				ip.ipInfoJump("第"+ i+1 + "条数据状态与操作不符", 'info');
        			}
        		}
        		if(flag == 0){
                	ip.ipInfoJump(btn.DISPLAY_TITLE+'成功！', 'success');
        		}
        		initTreeByElecode(elecode,viewModel.treeDataTable,"tree1");//初始化要素树
    			var radio =document.getElementsByName("mainStatus");
        		var submit_status = null;
            	for(var i =0;i < radio.length;i++){
    	        	 if(radio[i].checked){
    	        	     submit_status = radio[i].value;
    	        	 }
            	}
            	viewModel.gridRefresh("",submit_status);
            	for(var i=0;i<btnData.length;i++) {
            		if(btnData[i].ENABLED == "1" && btnData[i].DISPLAY_STATUS != "2") {
            			var btnId = btnData[i].BUTTON_ID;
                		$("#"+btnId).removeAttr("disabled","disabled");
            		}
            	}
        	}else {
        		ip.ipInfoJump("请勾选需要操作的事项内容", 'info');
        	}
        }
        
        //下达
        approveClick = function(id) {
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	var btn = getBtnMsgById(btnData,id);
        	var selectRow = viewModel.listGridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
        		var flag = 0;
        		for(var i=0;i<selectRow.length;i++){
        			if(selectRow[i].data.status_code.value == "0"){
        				var workFlowMap = {
        						"menu_id":menuId,
                    			"entity_id":selectRow[i].data.id.value,
                    			"billtype_code":billtype_code,
                    			"op_type":btn.ACTION_TYPE,
                    			"op_name":btn.DISPLAY_TITLE,
        				};
        				var data = {
                    			"workFlowMap":JSON.stringify(workFlowMap),
                    			"id":selectRow[i].data.id.value,
                    	};
                		$.ajax({
                            type: 'post',
                            url: "/df/bill/approveTask.do?tokenid=" + tokenid + "&ajax=noCache",
                            data: data,
                            dataType: 'JSON',
                            async: false,
                            success: function (result) {
                            	if(result.errorCode == "1") {
                            		ip.ipInfoJump("第"+ i+1 + "条数据"+btn.DISPLAY_TITLE+"失败", 'info');
                            		flag = -1;
                            	}else {

                            	}
                            }, error: function () {
                            	ip.ipInfoJump("第"+ i+1 + "条数据"+btn.DISPLAY_TITLE+"失败", 'info');
                        		flag = -1;
                            }
                        });  
                		if(flag == -1) {
                			break;
                		}
        			}else{
        				ip.ipInfoJump("第"+ i+1 + "条数据已下达，不可再次下达", 'info');
        			}
        		}
        		if(flag == 0){
                	ip.ipInfoJump(btn.DISPLAY_TITLE+'成功！', 'success');
        		}
        		initTreeByElecode(elecode,viewModel.treeDataTable,"tree1");//初始化要素树
    			var radio =document.getElementsByName("mainStatus");
        		var submit_status = null;
            	for(var i =0;i < radio.length;i++){
    	        	 if(radio[i].checked){
    	        	     submit_status = radio[i].value;
    	        	 }
            	}
            	viewModel.gridRefresh("",submit_status);
            	for(var i=0;i<btnData.length;i++) {
            		if(btnData[i].ENABLED == "1" && btnData[i].DISPLAY_STATUS != "2") {
            			var btnId = btnData[i].BUTTON_ID;
                		$("#"+btnId).removeAttr("disabled","disabled");
            		}
            	}
        	}else {
        		ip.ipInfoJump("请勾选需要操作的事项内容", 'info');
        	}
        }
        
        //登记
        registerFun = function(id){
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	var btn = getBtnMsgById(btnData,id);
        	var selectRow = viewModel.listGridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
        		var flag = 0;
        		for(var i=0;i<selectRow.length;i++){
        			if(selectRow[i].data.status_code.value == "0"){
        				var workFlowMap = {
        						"menu_id":menuId,
                    			"entity_id":dealData.delQuotation(selectRow[i].data.id.value),
                    			"billtype_code":billtype_code,
                    			"op_type":btn.ACTION_TYPE,
                    			"op_name":btn.DISPLAY_TITLE,
        				};
        				var data = {
                    			"workFlowMap":JSON.stringify(workFlowMap),
                    			"id":selectRow[i].data.id.value,
                    	};
                		$.ajax({
                            type: 'post',
                            url: "/df/bill/receiveTask.do?tokenid=" + tokenid + "&ajax=noCache",
                            data: data,
                            dataType: 'JSON',
                            async: false,
                            success: function (result) {
                            	if(result.errorCode == "1") {
                            		ip.ipInfoJump("第"+ i+1 + "条数据"+btn.DISPLAY_TITLE+"失败", 'info');
                            		flag = -1;
                            	}else {

                            	}
                            }, error: function () {
                            	ip.ipInfoJump("第"+ i+1 + "条数据"+btn.DISPLAY_TITLE+"失败", 'info');
                        		flag = -1;
                            }
                        });  
                		if(flag == -1) {
                			break;
                		}
        			}else{
        				ip.ipInfoJump("第"+ i+1 + "条数据已下达，不可再次下达", 'info');
        			}
        		}
        		if(flag == 0){
                	ip.ipInfoJump(btn.DISPLAY_TITLE+'成功！', 'success');
        		}
        		initTreeByElecode(elecode,viewModel.treeDataTable,"tree1");//初始化要素树
    			var radio =document.getElementsByName("mainStatus");
        		var submit_status = null;
            	for(var i =0;i < radio.length;i++){
    	        	 if(radio[i].checked){
    	        	     submit_status = radio[i].value;
    	        	 }
            	}
            	viewModel.gridRefresh("",submit_status);
            	for(var i=0;i<btnData.length;i++) {
            		if(btnData[i].ENABLED == "1" && btnData[i].DISPLAY_STATUS != "2") {
            			var btnId = btnData[i].BUTTON_ID;
                		$("#"+btnId).removeAttr("disabled","disabled");
            		}
            	}
        	}else {
        		ip.ipInfoJump("请勾选需要操作的事项内容", 'info');
        	}
        }
        
        //牵头处办任务发布（生成执行任务sup_bill）明细input，原单next
        publishClick = function(id) {
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	var btn = getBtnMsgById(btnData,id);
        	var selectRow = viewModel.listGridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
        		var flag = 0;
        		for(var i=0;i<selectRow.length;i++){
        			if(selectRow[i].data.status_code.value == "0"){
        				var workFlowMap = {
        						"menu_id":menuId,
                    			"entity_id":selectRow[i].data.id.value,
                    			"billtype_code":billtype_code,
                    			"op_type":btn.ACTION_TYPE,
                    			"op_name":btn.DISPLAY_TITLE,
        				};
        				var data = {
                    			"workFlowMap":JSON.stringify(workFlowMap),
                    			"id":selectRow[i].data.id.value,
                    	};
                		$.ajax({
                            type: 'post',
                            url: "/df/bill/publishTask.do?tokenid=" + tokenid + "&ajax=noCache",
                            data: data,
                            dataType: 'JSON',
                            async: false,
                            success: function (result) {
                            	if(result.errorCode == "1") {
                            		ip.ipInfoJump("第"+ i+1 + "条数据"+btn.DISPLAY_TITLE+"失败", 'info');
                            		flag = -1;
                            	}else {

                            	}
                            }, error: function () {
                            	ip.ipInfoJump("第"+ i+1 + "条数据"+btn.DISPLAY_TITLE+"失败", 'info');
                        		flag = -1;
                            }
                        });  
                		if(flag == -1) {
                			break;
                		}
        			}else{
        				ip.ipInfoJump("第"+ i+1 + "条数据已发布，不可再次发布", 'info');
        			}
        		}
        		if(flag == 0){
                	ip.ipInfoJump(btn.DISPLAY_TITLE+'成功！', 'success');
        		}
        		initTreeByElecode(elecode,viewModel.treeDataTable,"tree1");//初始化要素树
    			var radio =document.getElementsByName("mainStatus");
        		var submit_status = null;
            	for(var i =0;i < radio.length;i++){
    	        	 if(radio[i].checked){
    	        	     submit_status = radio[i].value;
    	        	 }
            	}
            	viewModel.gridRefresh("",submit_status);
            	for(var i=0;i<btnData.length;i++) {
            		if(btnData[i].ENABLED == "1" && btnData[i].DISPLAY_STATUS != "2") {
            			var btnId = btnData[i].BUTTON_ID;
                		$("#"+btnId).removeAttr("disabled","disabled");
            		}
            	}
        	}else {
        		ip.ipInfoJump("请勾选需要操作的任务内容", 'info');
        	}
        }
        
      //牵头处办任务撤销发布
        undoPublish = function(id) {
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	var btn = getBtnMsgById(btnData,id);
        	var selectRow = viewModel.listGridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
        		var flag = 0;
        		for(var i=0;i<selectRow.length;i++){
        			if(selectRow[i].data.status_code.value == "1"){
        				var workFlowMap = {
        						"menu_id":menuId,
                    			"entity_id":dealData.delQuotation(selectRow[i].data.id.value),
                    			"billtype_code":billtype_code,
                    			"op_type":btn.ACTION_TYPE,
                    			"op_name":btn.DISPLAY_TITLE,
        				};
        				var data = {
                    			"workFlowMap":JSON.stringify(workFlowMap),
                    			"id":selectRow[i].data.id.value,
                    	};
                		$.ajax({
                            type: 'post',
                            url: "/df/bill/undoPublishTask.do?tokenid=" + tokenid + "&ajax=noCache",
                            data: data,
                            dataType: 'JSON',
                            async: false,
                            success: function (result) {
                            	if(result.errorCode == "1") {
                            		ip.ipInfoJump("第"+ i+1 + "条数据"+btn.DISPLAY_TITLE+"失败", 'info');
                            		flag = -1;
                            	}else {

                            	}
                            }, error: function () {
                            	ip.ipInfoJump("第"+ i+1 + "条数据"+btn.DISPLAY_TITLE+"失败", 'info');
                        		flag = -1;
                            }
                        });  
                		if(flag == -1) {
                			break;
                		}
        			}else{
        				ip.ipInfoJump("第"+ i+1 + "条数据已发布，不可再次发布", 'info');
        			}
        		}
        		if(flag == 0){
                	ip.ipInfoJump(btn.DISPLAY_TITLE+'成功！', 'success');
        		}
        		initTreeByElecode(elecode,viewModel.treeDataTable,"tree1");//初始化要素树
    			var radio =document.getElementsByName("mainStatus");
        		var submit_status = null;
            	for(var i =0;i < radio.length;i++){
    	        	 if(radio[i].checked){
    	        	     submit_status = radio[i].value;
    	        	 }
            	}
            	viewModel.gridRefresh("",submit_status);
            	for(var i=0;i<btnData.length;i++) {
            		if(btnData[i].ENABLED == "1" && btnData[i].DISPLAY_STATUS != "2") {
            			var btnId = btnData[i].BUTTON_ID;
                		$("#"+btnId).removeAttr("disabled","disabled");
            		}
            	}
        	}else {
        		ip.ipInfoJump("请勾选需要操作的任务内容", 'info');
        	}
        }
        
      //获取options
        getOptions = function() {
        	options = ip.getCommonOptions({});
            options['tokenid'] = tokenid;
            options['ajax'] = "noCache";
        }
        setInterval(csscsof.windowHeightNoNav(), 500)//每半秒执行一次windowHeight函数
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
            menuId = url.split("menuid=")[1].split("&")[0];
            menuName = decodeURI(url.split("menuname=")[1].split("&")[0]);
            elecode = url.split("elecode=")[1].split("&")[0];
            billtype_code = url.split("billTypeCode=")[1].split("&")[0];
            if(billtype_code == "131") {
            	$(".rightTable-title").html("下达单列表");
            }else if(billtype_code == "132"){
            	$(".rightTable-title").html("处室任务单列表");
            }
            //初始化按钮
            var data = initBtns(menuId);
            if(data == false){
                ip.ipInfoJump("加载按钮出错", 'error');
            }else{
                viewModel.btnDataTable.setSimpleData(data);
            }
            initTreeByElecode(elecode,viewModel.treeDataTable,"tree1");//初始化要素树
            viewModel.initData();//初始化Grid
            viewModel.initStatus(); //初始化状态栏
            csscsof.windowHeightNoNav();
        };
        init();
    }
);