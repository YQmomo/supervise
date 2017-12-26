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
        };
        
        var pageData = {
        		getUUID: function(){
                    //postData.SID = '-1';
                    $.post('/df/task/getUUID.do?tokenid='+ tokenid + '&ajax=' + 'noCache',function(map){
                        pageData.ID = map;
                        publicFileman(map);
                    });
                }
        }

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
                        var listCode = "SUP_TYPE_ID";
                        treeState.treeId = listId;
                        treeState.treeCode = listCode;
                        var radio =document.getElementsByName("mainStatus");
                		var submit_status = null;
                		var status = null;
                    	for(var i =0;i < radio.length;i++){
            	        	 if(radio[i].checked){
            	        	     submit_status = radio[i].value;
            	        	     status = submit_status;
            	        	 }
                    	}
                    	var conditionMap = {};
                    	if(submit_status == "10"){
                			conditionMap["IS_SUM"] = 0;
                			status = 0;
                    	}else if(submit_status == "0"){
                    		conditionMap["IS_SUM"] = 1;
                    	}
                    	if(node.CHR_NAME == '全部' || node.chr_name == '全部'){
                            viewModel.gridRefresh(0,conditionMap,status);
                            viewModel.officeSumGridViewModel.gridData.clear();
                            viewModel.depSumGridViewModel.gridData.clear();
                            viewModel.detailGridViewModel.gridData.clear();
                        }else{
                        	conditionMap[listCode] = listId;
                            viewModel.gridRefresh(0,conditionMap,status);
                            viewModel.officeSumGridViewModel.gridData.clear();
                            viewModel.depSumGridViewModel.gridData.clear();
                            viewModel.detailGridViewModel.gridData.clear();
                        }
                    }
                }
            },
            btnEvents: function(data, events){//所有单击事件
                var $this = $(events.target),
                    name = $this[0].name;
                if(name == 'btn1'){//上一步b
                    num--;
                    if(num == 0){
                        $('#btn1').hide();
                    }
                    if(num < 1){
                        $('#btn2').show();
                        $('#btn3').hide();
                    }
                    eventsDeal.reduceStep();
                    eventsDeal.modalTab();
                }
                if(name == 'btn2'){//下一步
                	var flag = 0;
                	if(num == 0){
//                		 var aim2 = viewModel.vailData;
//                         var noVerifyRid =["REMARK"];
//                         if(!verifyInputView(aim2,noVerifyRid)){
//                             flag++;
//                         }
                    }
                	 if(flag > 0){
                         return;
                     }else{
                         num++;
                         $('#btn1').show();
                         eventsDeal.addStep();
                         eventsDeal.modalTab();
                     }
                    if(num == 1){
                        $('#btn2').hide();
                        $('#btn3').show();
                    }
                }
                if(name == 'btn3'){//完成
                	num = 0;
                    eventsDeal.modalTab();
                    eventsDeal.reduceStep();
                    $('#sumModal').modal('toggle');
                    $('#btn2').show();
                    $('#btn4').show();
                    var summary_type = "";
                	if(postData == "ADD"){
                        var indeces = viewModel.listGridViewModel.gridData.getSelectedIndices();
            			var datas = viewModel.listGridViewModel.gridData.getSimpleData();
                        var url = "/df/summary/saveSummary.do";
                        var op_name = "总结录入";
                        var summary_id = pageData.ID;
                        var bill_id = datas[indeces[0]].ID ||　datas[indeces[0]].id;
                	}else if(postData == "UPDATE"){
                    	var indeces = viewModel.officeSumGridViewModel.gridData.getSelectedIndices();
            			var datas = viewModel.officeSumGridViewModel.gridData.getSimpleData();
                        var url = "/df/summary/updateSummary.do";
                        var op_name = "总结修改";
                        var summary_id = datas[indeces[0]].ID ||　datas[indeces[0]].id;
                        var bill_id = datas[indeces[0]].BILL_ID ||　datas[indeces[0]].bill_id;
                	}
                	var index = indeces[0];
                    var focusData = datas[index];
                    focusData["SUMMARY"] = $("#summary").val();
                    focusData["SUP_RESULTS"] = $("#sup_results").val();
                    focusData["BILLTYPE_CODE"] = billtype_code;
                    focusData["SUMMARY_ID"] = summary_id; //    要新生成的ID     TO ADD 
                    var data = {
                    	"summaryData":JSON.stringify(focusData),
                    };
                    $.post(url + '?tokenid='+ tokenid + '&ajax=' + 'noCache', data, function(map){
                 		if(map.errorCode == '0'){
                 			ip.ipInfoJump(op_name + '成功！', 'success');
                 			initTreeByElecode(elecode,viewModel.treeDataTable,"tree1");
                			var radio =document.getElementsByName("mainStatus");
                    		var submit_status = null;
                    		var status = null;
                        	for(var i =0;i < radio.length;i++){
                	        	 if(radio[i].checked){
                	        	     submit_status = radio[i].value;
                	        	     status = submit_status;
                	        	 }
                        	}
                        	var conditionMap = {};
                        	if(submit_status == "10"){
                    			conditionMap["IS_SUM"] = 0;
                    			status = 0;
                        	}else if(submit_status == "0"){
                        		conditionMap["IS_SUM"] = 1;
                        	}
                        	viewModel.gridRefresh(0,conditionMap,status);
                        	var mof_bill_id = bill_id;
                        	conditionMap = {};
                            conditionMap["mof_bill_id"] = mof_bill_id;
                        	viewModel.gridRefresh(1,conditionMap);
                        	//viewModel.gridRefresh(3,conditionMap);
                            if(billtype_code == "131"){
                            	viewModel.gridRefresh(2,conditionMap);
                            	conditionMap = {};
                                conditionMap["mof_bill_id"] = obj.rowObj.value.id || obj.rowObj.value.ID;
                            }else{
                            	conditionMap = {};
                                conditionMap["task_id"] = obj.rowObj.value.id || obj.rowObj.value.ID;
                            }
                            viewModel.gridRefresh(3,conditionMap);
                 		}else{
                 			ip.ipInfoJump(op_name+'失败！', 'info');
                 		}
                 	});
                }
                if(name == 'btn4'){//返回
                	num = 0;
                	eventsDeal.reduceStep();
                    eventsDeal.modalTab();
                	$('#btn1').show();
                    $('#btn3').show();
                    $('#btn2').hide();
                    $('#btn4').show();
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
                }else if(view.orders == '1'){
                	options["bill_id"] = "";
                	if(billtype_code == "131"){
                		var areaGridId = "officeSumGrid"; 
                	}else if(billtype_code == "132"){
                		var areaGridId = "depSumGrid"; 
                	}
                	viewModel.officeSumGridViewModel = ip.initGrid(viewModel.listViewList[n],areaGridId, viewModel.listViewList[n].remark + '?', options, 0, false,true,false,false);
                    if(viewModel.officeSumGridViewModel.gridData.getSimpleData()){
                        $("#officeSumGridSumNum").html(viewModel.officeSumGridViewModel.gridData.getSimpleData().length);
                    }
                }else if(view.orders == '3'){
                   options["action"] = "Q";
                   options["billtype_code"] = "101";
               	   viewModel.detailGridViewModel = ip.initGrid(viewModel.listViewList[n],'detailGrid', "/df/common/commonAction.do?", options, 0, false,false,false,false);
                   if(viewModel.detailGridViewModel.gridData.getSimpleData()){
                       $("#detailGridSumNum").html(viewModel.detailGridViewModel.gridData.getSimpleData().length);
                   }
               } 
                if(billtype_code == "131"){
                	if(view.orders == '2'){
                   	 options["bill_id"] = "";
                   	 if(billtype_code == "131"){
                   		 var operateFlag = false;
                   	 }else if(billtype_code == "132"){
                   		 var operateFlag = true;
                   	 }
                   	viewModel.depSumGridViewModel = ip.initGrid(viewModel.listViewList[n],'depSumGrid', viewModel.listViewList[n].remark + '?', options, 0, false,operateFlag,false,false);
                       if(viewModel.depSumGridViewModel.gridData.getSimpleData()){
                           $("#depSumGridSumNum").html(viewModel.depSumGridViewModel.gridData.getSimpleData().length);
                       }
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
        viewModel.gridRefresh = function(orders,conditionMap,status) {
        	getOptions();
        	if(orders == '0'){
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
        	}else if(orders == "1"){
        		if(conditionMap || conditionMap != ""){     		
           	        options["bill_id"] = conditionMap.mof_bill_id;
           	    }
        		 ip.setGrid(viewModel.officeSumGridViewModel, viewModel.listViewList[1].remark + '?',options);
                 $("#officeSumGridSumNum").html(viewModel.officeSumGridViewModel.gridData.getSimpleData().length);  
        	}else if(orders == "2"){
	        	 if(billtype_code == "131"){
	        		 options["all"] = "1";
	           	 }
        		 options["bill_id"] = conditionMap.mof_bill_id;
        		 ip.setGrid(viewModel.depSumGridViewModel, viewModel.listViewList[2].remark + '?',options);
                 $("#depSumGridSumNum").html(viewModel.depSumGridViewModel.gridData.getSimpleData().length);  
        	}else if(orders == "3"){
        		options["action"] = "Q";
        		if(billtype_code == "131"){
        			options["billtype_code"] = "132";
                    var conditionRela = {
                        	"mof_bill_id":"0"
                        }
                }else{
                    options["billtype_code"] = "101";
                    var conditionRela = {
                        	"task_id":"0"
                        }
                }           
                options["condition_rela"] = JSON.stringify(conditionRela);
        		if(conditionMap || conditionMap != ""){     		
           	        options["condition_data"] = JSON.stringify(conditionMap);
           	    }
        		 ip.setGrid(viewModel.detailGridViewModel, '/df/common/commonAction.do?',options);
                 $("#detailGridSumNum").html(viewModel.detailGridViewModel.gridData.getSimpleData().length);  
        	}
        };
        
        mainGrid_onRowSelected = function(obj) {
        	var index = obj.rowIndex;
        	var data = viewModel.listGridViewModel.gridData.getSimpleData();
        	//pageData.resolveData =  data[index];
        	var mof_bill_id = obj.rowObj.value.id || obj.rowObj.value.ID;
        	var conditionMap = {};
            conditionMap["mof_bill_id"] = mof_bill_id;
        	viewModel.gridRefresh(1,conditionMap);
        	//viewModel.gridRefresh(3,conditionMap);
        	if(billtype_code == "131"){
            	viewModel.gridRefresh(2,conditionMap);
            	conditionMap = {};
                conditionMap["mof_bill_id"] = obj.rowObj.value.id || obj.rowObj.value.ID;
            }else{
            	conditionMap = {};
                conditionMap["task_id"] = obj.rowObj.value.id || obj.rowObj.value.ID;
            }
            viewModel.gridRefresh(3,conditionMap);
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
        	if(menuName.indexOf("审核") >= 0){
        		$("#mainGridStatus").html('<label><input name="mainStatus" type="radio" value="9" onchange ="submitStatusChange(this.name)"/>全部 </label> '+
        	        	'<label><input name="mainStatus" type="radio" value="0" onchange ="submitStatusChange(this.name)"/>待审核</label> '+
        	        	'<label><input name="mainStatus" type="radio" value="1" onchange ="submitStatusChange(this.name)"/>已审核</label>');
        	}else if(menuName.indexOf("上报") >= 0){
        		$("#mainGridStatus").html('<label><input name="mainStatus" type="radio" value="9" onchange ="submitStatusChange(this.name)"/>全部 </label> '+
        	        	'<label><input name="mainStatus" type="radio" value="0" onchange ="submitStatusChange(this.name)"/>待上报</label> '+
        	        	'<label><input name="mainStatus" type="radio" value="1" onchange ="submitStatusChange(this.name)"/>已上报</label>');
        	}else{
            	$("#mainGridStatus").html('<label><input name="mainStatus" type="radio" value="9" onchange ="submitStatusChange(this.name)"/>全部 </label> '+
            			'<label><input name="mainStatus" type="radio" value="10" onchange ="submitStatusChange(this.name)"/>执行中</label> '+
        	        	'<label><input name="mainStatus" type="radio" value="0" onchange ="submitStatusChange(this.name)"/>待总结</label> '+
        	        	'<label><input name="mainStatus" type="radio" value="1" onchange ="submitStatusChange(this.name)"/>已总结</label>');
        	}
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
    		var status = null;
    		var conditionMap  = {};
        	for(var i =0;i < radio.length;i++){
	        	 if(radio[i].checked){
	        	     submit_status = radio[i].value;
	        	     status = submit_status;
	        	 }
        	}
        	if(type == "mainStatus"){
            	var treeObj = $.fn.zTree.getZTreeObj("tree1");
            	var nodes = treeObj.getSelectedNodes();
            	if(nodes.length > 0) {
            		var selectNodeId = nodes[0].CHR_ID;
            		if(nodes[0].CHR_NAME != '全部' && nodes[0].chr_name != '全部'){
            			 var type_code = "SUP_TYPE_ID";
                  	    conditionMap[type_code] = selectNodeId;
                    }
                }
            	if(menuName.indexOf("审核") >= 0){
            		
            	}else{
            		if(submit_status == "10"){
            			conditionMap["IS_SUM"] = 0;
            			status = 0;
                	}else if(submit_status == "0"){
                		conditionMap["IS_SUM"] = 1;
                	}
            	}
                viewModel.gridRefresh(0,conditionMap,status);
                if(viewModel.officeSumGridViewModel){
                    viewModel.officeSumGridViewModel.gridData.clear();
                }
                if(viewModel.depSumGridViewModel){
                    viewModel.depSumGridViewModel.gridData.clear();
                }
                if(viewModel.detailGridViewModel){
                    viewModel.detailGridViewModel.gridData.clear();
                }
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
        
        var eventsDeal = {
                addStep: function () {
                    $('.u-step:eq(' +num+ ')').addClass('current').siblings().removeClass('current');
                    $('.u-step:lt(' +num+ ')').addClass('done');
                    $('.step-doing').text(num+1);
                    $('.step-name').text($('.u-step:eq(' +num+ ') .u-step-title').text());
                },
                reduceStep: function () {
                    $('.u-step:gt(' +num+ ')').removeClass('done');
                    $('.u-step:eq(' +num+ ')').removeClass('done');
                    $('.u-step:eq(' +num+ ')').addClass('current').siblings().removeClass('current');
                    $('.step-doing').text(num+1);
                    $('.step-name').text($('.u-step:eq(' +num+ ') .u-step-title').text());

                },
                modalTab: function () {//点击上一步、下一步切换modal页签
                    var arr = ['sumAdd', 'partAdd'];
                    $('#' + arr[num]).addClass('active').siblings().removeClass('active');
                }
            };
        
        //工作流提交
        workFlowSubmit = function(id) {
        	workFlow(id,'0');
        }
        //工作流审核
        workFlowSup = function(id) {
        	var selectRow = viewModel.listGridViewModel.gridData.getSelectedRows();
        	var flag = 0;
        	if(selectRow.length > 0) { 
        		for(var i=0;i<selectRow.length;i++){
            		if(selectRow[i].data.is_sum.value == "0"){
                		flag++;
                	}
            	}
            	if(flag == 0){
            		if(menuName.indexOf("审核") >= 0){
            			var btnData = viewModel.btnDataTable.getSimpleData();
                    	var btn = getBtnMsgById(btnData,id);
                    	csof.opinionModal('退回意见',menuId+"-opinionConfirmSureId",menuId+"-opinionConfirmCancelCla",menuId);
                    	$("#"+menuId+"-op_name").html(btn.DISPLAY_TITLE);
                    	$("#"+menuId+"-opinion").val("同意！");
                        $("#"+menuId+"-opinionConfirmSureId").on('click',function(){
                        	if($("#"+menuId+"-opinion").val() != "" && $("#"+menuId+"-opinion").val() != null){
                            	workFlow(id,'0',$("#"+menuId+"-opinion").val());
                            	$('#'+menuId+'-opinion-modal').modal('hide');
                                $('#'+menuId+'-opinion-modal').remove();
                        	}else{
                        		ip.ipInfoJump("请输入意见！", 'info');
                        	}
                        });
                        $('.'+menuId+'-opinionConfirmCancelCla').on('click',function(){
                        	$('#'+menuId+'-opinion-modal').modal('hide');
                            $('#'+menuId+'-opinion-modal').remove();
                        });
                	}else{
                    	workFlow(id,'0');
                	}
            	}else{
            		ip.ipInfoJump("总结的事项还在执行中，无法提交！", 'info');
            	}
            }else {
        		ip.ipInfoJump("请勾选需要操作的监管内容", 'info');
        	}
        }
      //工作流撤销
        workFlowUndo = function(id) {
        	workFlow(id,'1');
        }
      //工作流退回
        workFlowReturn = function(id) {
        	var selectRow = viewModel.listGridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
        		if(menuName.indexOf("审核") >= 0){
        			var btnData = viewModel.btnDataTable.getSimpleData();
                	var btn = getBtnMsgById(btnData,id);
                	csof.opinionModal('退回意见',menuId+"-opinionConfirmSureId",menuId+"-opinionConfirmCancelCla",menuId);
                	$("#"+menuId+"-op_name").html(btn.DISPLAY_TITLE);
                    $("#"+menuId+"-opinionConfirmSureId").on('click',function(){
                    	if($("#"+menuId+"-opinion").val() != "" && $("#"+menuId+"-opinion").val() != null){
                        	workFlow(id,'0',$("#"+menuId+"-opinion").val());
                        	$('#'+menuId+'-opinion-modal').modal('hide');
                            $('#'+menuId+'-opinion-modal').remove();
                    	}else{
                    		ip.ipInfoJump("请输入意见！", 'info');
                    	}
                    });
                    $('.'+menuId+'-opinionConfirmCancelCla').on('click',function(){
                    	$('#'+menuId+'-opinion-modal').modal('hide');
                        $('#'+menuId+'-opinion-modal').remove();
                    });
            	}else{
                	workFlow(id,'0');
            	}
            }else {
        		ip.ipInfoJump("请勾选需要操作的监管内容", 'info');
        	}
        }
        
        //工作流结束
        workFlowEnd = function(id) {
        	workFlow(id,'1');
        }

        //工作流/df/workflow/work.do
        workFlow = function(id,status,op) {
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	var btn = getBtnMsgById(btnData,id);
        	var selectRow = viewModel.listGridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
        		var flag = 0;
        		for(var i=0;i<selectRow.length;i++){
        			if(selectRow[i].data.status_code.value == status){
        				if(op){
                			var opinion = op;
                		}else{
                			var opinion = ""
                		}
        				var data = {
                    			"menu_id":menuId,
                    			"entity_id":selectRow[i].data.id.value,
                    			"billtype_code":billtype_code,
                    			"op_type":btn.ACTION_TYPE,
                    			"op_name":btn.DISPLAY_TITLE,
                    			"opinion":opinion
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
        		var status = null;
            	for(var i =0;i < radio.length;i++){
    	        	 if(radio[i].checked){
    	        	     submit_status = radio[i].value;
    	        	     status = submit_status;
    	        	 }
            	}
            	var conditionMap = {};
            	if(submit_status == "10"){
        			conditionMap["IS_SUM"] = 0;
        			status = 0;
            	}else if(submit_status == "0"){
            		conditionMap["IS_SUM"] = 1;
            	}
            	viewModel.gridRefresh(0,conditionMap,status);
            	if(viewModel.officeSumGridViewModel){
                    viewModel.officeSumGridViewModel.gridData.clear();
            	}
            	if(viewModel.depSumGridViewModel){
                    viewModel.depSumGridViewModel.gridData.clear();
            	}
            	if(viewModel.detailGridViewModel){
                    viewModel.detailGridViewModel.gridData.clear();
            	}
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
        
        //总结录入
        summaryClick = function(id) {
        	var indeces = viewModel.listGridViewModel.gridData.getSelectedIndices();
			var datas = viewModel.listGridViewModel.gridData.getSimpleData();
			if(indeces.length == 1){
				$('#sumModal').modal({
	            	show : true,
	            	backdrop : 'static'
	            });
				num = 0;
				postData = "ADD";
                var index = indeces[0];
                var focusData = datas[index];
                $("#sup_task").val(focusData.sup_name);
                $("#summary").val("");
                $("#sup_results").val("");
	        	pageData.getUUID();
                $('#btn2').show();
                $('#btn1').hide();
                $('#btn3').hide();
			}else{
				ip.ipInfoJump('请选择一个要总结的任务,勿多选或不选！', 'info');
			}
        }
        
        //总结修改
        publicModify = function(id) {
        	var indeces = viewModel.officeSumGridViewModel.gridData.getSelectedIndices();
			var datas = viewModel.officeSumGridViewModel.gridData.getSimpleData();
			if(indeces.length == 1){
				$('#sumModal').modal({
	            	show : true,
	            	backdrop : 'static'
	            });
				num = 0;
	        	postData = "UPDATE";
                var index = indeces[0];
                var focusData = datas[index];
                $("#sup_task").val(focusData.sup_name);
                $("#summary").val(focusData.summary);
                $("#sup_results").val(focusData.sup_results);
                var id = datas[indeces[0]].ID ||　datas[indeces[0]].id;
                publicFileman(id);
                $('#btn2').show();
                $('#btn1').hide();
                $('#btn3').hide();
			}else{
				ip.ipInfoJump('请选择一个要修改的总结,勿多选或不选！', 'info');
			}
        }
        
        //总结删除
        publicDelete = function(id) {
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	var btn = getBtnMsgById(btnData,id);
        	var indeces = viewModel.officeSumGridViewModel.gridData.getSelectedIndices();
			var datas = viewModel.officeSumGridViewModel.gridData.getSimpleData();
			var focusData;
			if(indeces.length > 0){
				var delIds =  datas[indeces[0]].id;
                for(var i = 1 ; i<indeces.length ; i++){
                    var index = indeces[i];
                    focusData = datas[index];
                    var delId = focusData.id;
                    delIds = delIds + ","+  delId ;
                }
                $.ajax({
                    type: 'post',
                    url: "/df/summary/deleteSummary.do?tokenid=" + tokenid + "&ajax=noCache",
                    data: {'ids':delIds},
                    dataType: 'JSON',
                    async: false,
                    success: function (result) {
                    	if(result.data == false) {
                    		ip.ipInfoJump("第"+ i+1 + "条数据"+btn.DISPLAY_TITLE+"失败", 'info');
                    		flag = -1;
                    	}else {
                    		var mof_bill_id = datas[indeces[0]].bill_id;
                        	var conditionMap = {};
                            conditionMap["mof_bill_id"] = mof_bill_id;
                        	viewModel.gridRefresh(1,conditionMap);
                        	//viewModel.gridRefresh(3,conditionMap);
                        	if(billtype_code == "131"){
                            	viewModel.gridRefresh(2,conditionMap);
                            	conditionMap = {};
                                conditionMap["mof_bill_id"] = obj.rowObj.value.id || obj.rowObj.value.ID;
                            }else{
                            	conditionMap = {};
                                conditionMap["task_id"] = obj.rowObj.value.id || obj.rowObj.value.ID;
                            }
                            viewModel.gridRefresh(3,conditionMap);
                    	}
                    }, error: function () {
                    	ip.ipInfoJump("第"+ i+1 + "条数据"+btn.DISPLAY_TITLE+"失败", 'info');
                		flag = -1;
                    }
                });  
			}else{
				ip.ipInfoJump('请选择要删除的总结！', 'info');
			}
        }
        
        //附件
        var publicFileman = function(sId) {
            $("#filePage")[0].src = "";
            var fileData = {
                "entity_id":sId,
                "oid":options.svOfficeId,
                "dep_id":options.svDepId,
                "dep_code":options.svDepCode,
                "edit":'Y',
                "modular":'TASK',
            };
            //if(pageData.SID)
            $("#filePage")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
                "&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&entityName=tasksum&oid="+fileData.oid+
                "&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit+"&modular="+fileData.modular;
        };
              
      //获取options
        getOptions = function() {
        	options = ip.getCommonOptions({});
            options['tokenid'] = tokenid;
            options['ajax'] = "noCache";
        }
        setInterval(csscsof.rightTwoHeight(), 500)//每半秒执行一次windowHeight函数
        //初始化
        function init() {
            app = u.createApp({
                el: document.body,
                model: viewModel
            });
            tokenid = ip.getTokenId();
            getOptions();
            var url=window.location.href;
            menuId = url.split("menuid=")[1].split("&")[0];
            menuName = decodeURI(url.split("menuname=")[1].split("&")[0]);
            elecode = url.split("elecode=")[1].split("&")[0];
            billtype_code = url.split("billTypeCode=")[1].split("&")[0];
            if(billtype_code == "131"){
                $("#officeSum").show();
                $("#officeSumLi").show();
            }else if(billtype_code == "132"){
            	 $("#depSumLi").addClass("active");
            	 $("#depSum").addClass("active");
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
            csscsof.rightTwoHeight();
        };
        init();
    }
);