/**
 * Created by yizhang on 2017/8/11.
 */
require(['jquery', 'knockout','/df/supervise/ncrd.js','csscsof','bootstrap','dateZH', 'uui', 'tree', 'grid', 'ip','csof'],
    function ($, ko,ncrd,csscsof) {
        window.ko = ko;
        var tokenid;
        var options;
        var eleCode;
        var entity_id;
        var pageId;
        var sid;
        var bbq_date;
        var task_id;
        var task_name;
        var IS_ALLOBJ;
        var OBJ_TYPE_ID;
        var sumBillTypeNo;
        var sumBillTypeDep;
        var sumType;
        var billTypeCode;
        var fileData;
		var pViewType = {
        		VIEWTYPE_INPUT : "001",// 录入视图
        		VIEWTYPE_LIST : "002",// 列表视图
        		VIEWTYPE_QUERY : "003"// 查询视图
        };
        //URL
        var testURL = '/df/cs/test.do';
        var viewListURL= '/df/cs/selectAll.do';
        var doPayWorkFlowURL= '/df/workflow/work.do';
        var depTreeURL ='/df/tree/initTree.do';
        var summaryURL ='/df/cs/doCollect.do';
        var undoSummaryURL ='/df/cs/undoSummary.do';
        var sumListURL = '/df/cs/getbill.do';
        var getBillNoURL = '/df/cs/inputbill.do';//获取单号
        var getNavURL = '/ReportForm/selectSupReportBySidBillcode.do';//获取详情的列表页签
        var getReportURL = '/ReportForm/selectEReportByChrId.do'//获取报表
        var viewModel = {
        		SearchdepTreeKEY: ko.observable(""),
        		SearchdepTreeKEY2: ko.observable(""),
        		SearchdepTreeKEY3: ko.observable(""),
        		//模糊查询单位树
                searchDepTree : function () {
                	 ncrd.findTreeNode($.fn.zTree.getZTreeObj("depTree"), viewModel.SearchdepTreeKEY());    
                },  
              //模糊查询单位树
                searchDepTree2 : function () {
                	 ncrd.findTreeNode($.fn.zTree.getZTreeObj("depTree2"), viewModel.SearchdepTreeKEY2());    
                }, 
              //模糊查询单位树
                searchDepTree3 : function () {
                	 ncrd.findTreeNode($.fn.zTree.getZTreeObj("depTree3"), viewModel.SearchdepTreeKEY3());    
                },
        	
            exDataTable:new u.DataTable({
                meta: {

                }
            }),
            //汇总Data
            exDataTable2:new u.DataTable({
                meta: {

                }
            }),
            btnDataTable:new u.DataTable({
                meta: {

                }
            }),
            depTreeSetting: {
                view:{
                    showLine: false,
                    selectedMulti: false,
                    showIcon: false,
                    showTitle: true,
                    nameIsHTML: true
                },
                callback:{
                    onClick:function(e,id,node){
                    	var btnData = viewModel.btnDataTable.getSimpleData();
                        var selectRow = viewModel.gridViewModel.gridData.getSimpleData();
                        var selectNodeId = node.CHR_ID;
                        var type = node.TYPE;
               		    var code = node.CHR_CODE;
               		    var oid = node.OID;
                        if(selectNodeId == "000") {
                        	viewModel.refresh("","","","","9");
                        	$("#details-li").removeClass("active");
                     		$("#list-li").addClass("active");
                     		$("#panel-list").addClass("active");
                     		$("#panel-details").removeClass("active");
                     		$("#panel-details").hide();
                        }else {
                    		viewModel.refresh(selectNodeId,type,code,oid,"9");
                    		if(selectRow.length){
//                    			if($("#rightTable-ul li.active")[0].id == "details-li"){
//                            		var bill_no = selectRow[0].BILL_NO;
//                            		var dep_id = selectRow[0].AGENCY_ID;
//                            		var dep_code = selectRow[0].AGENCY_CODE;
//                                	if(type == "AGENCY"){
//                                		viewModel.navShow(billTypeCode,bill_no,"true",code); 
//                                		$("#panel-details").show();
//                           		    }else if(type == "MOFDEP"){
//                           		    	viewModel.navShow(sumBillTypeDep,bill_no,"true",code);
//                           		    	$("#panel-details").show();
//                           		    }else if(type == "OFFICE"){
//                           		    	viewModel.navShow(sumBillTypeNo,bill_no,"true",code);
//                           		    	$("#panel-details").show();
//                           		    }else{
//                           		    	$("#panel-details").hide();
//                           		    }
//                                	$("#details-li").addClass("active");
//                             		$("#list-li").removeClass("active");
//                             		$("#panel-list").removeClass("active");
//                             		$("#panel-details").addClass("active");
//                            	}
                    			if(selectRow[0].STATUS_CODE == "0") {
                                	for(var i=0;i<btnData.length;i++) {
                                		var btnId = btnData[i].BUTTON_ID;
                                		if(sumType == selectRow[0].SUM_TYPE){
                                			if(btnData[i].DISPLAY_STATUS == "0" || (btnData[i].DISPLAY_STATUS == "9" && btnData[i].ENABLED == "1")) {
                                    			$("#"+btnId).removeAttr("disabled","disabled");
                                    		}else {
                                    			$("#"+btnId).attr("disabled","disabled");
                                    		}
                                		}else {
                                			if(btnData[i].DISPLAY_STATUS == "1" || (btnData[i].DISPLAY_STATUS == "9" && btnData[i].ENABLED == "1")) {
                                    			$("#"+btnId).removeAttr("disabled","disabled");
                                    		}else {
                                    			$("#"+btnId).attr("disabled","disabled");
                                    		}
                                		}
                                		
                                	}
                                }else {
                                	for(var i=0;i<btnData.length;i++) {
                                		var btnId = btnData[i].BUTTON_ID;
                                		if(btnData[i].DISPLAY_STATUS == "9" && btnData[i].ENABLED == "1") {
                                			$("#"+btnId).removeAttr("disabled","disabled");
                                		}else {
                                			$("#"+btnId).attr("disabled","disabled");
                                		}
                                	}
                                }
                    			fileData = {
                     					"entity_id":bill_no,
                     					"oid":oid,
                     					"dep_id":node.CHR_ID,
                     					"dep_code":node.CHR_CODE,
                     					"edit":"N",
                     			}
                    		}
                        }
                    }
                }
            },
            depTreeDataTable: new u.DataTable({
                meta: {
                    'CHR_ID': {
                        'value':""
                    },
                    'PARENT_ID': {
                        'value':""
                    },
                    'CHR_NAME':{
                        'value':""
                    },
                    'CHR_CODE':{
                        'value':""
                    },
                    'DISP_CODE': {
                        'value':""
                    },
                    'LEVEL_NUM':{
                        'value':""
                    },
                    'IS_LEAF':{
                        'value':""
                    },
                    'TYPE':{
                        'value':""
                    },
                    'ENABLED':{
                        'value':""
                    },
                    'CREATE_DATE':{
                        'value':""
                    },
                    'CREATE_USER':{
                        'value':""
                    },
                    'LATEST_OP_DATE':{
                        'value':""
                    },
                    'IS_DELETED':{
                        'value':""
                    },
                    'LATEST_OP_USER':{
                        'value':""
                    },
                    'LAST_VER':{
                        'value':""
                    },
                    'RG_CODE':{
                        'value':""
                    },
                    'SET_YEAR':{
                        'value':""
                    },
                    'OID':{
                        'value':""
                    },               
                }
            })
        };

        //初始化事项下拉框
        viewModel.initDropDown = function () {
        	$("#eventName").html(task_name);
        };

        /**
         * 按钮事件
         */
        //汇总
        summaryClick = function (id){
        	var treeObj = $.fn.zTree.getZTreeObj("depTree");
        	var nodes = treeObj.getSelectedNodes();
        	if(nodes.length > 0) {
        		var selectNodeId = nodes[0].CHR_ID;
                var type = nodes[0].TYPE;
            	if(type != "MOFDEP" && sumType == 0){
            		ip.ipInfoJump("请选择司局进行汇总", 'info');
            	}else if(type != "OFFICE" && sumType == 1){
            		ip.ipInfoJump("请选择专员办进行汇总", 'info');
            	}else{
            		var code = nodes[0].CHR_CODE;
           		    var oid = nodes[0].OID;
           		    var bill_no = nodes[0].BILL_NO;
                    var btnData = viewModel.btnDataTable.getSimpleData();
                	for(var bt = 0; bt<btnData.length;bt++) {
                		if(btnData[bt].BUTTON_ID == id) {
                			var btn = btnData[bt];
                			break;
                		}
                	}
                	var selectAllRow = viewModel.gridViewModel.gridData.getSimpleData();
                	if(selectAllRow.length > 0) { 
                		var errorFlag = 0;
                		for(var j=0;j<selectAllRow.length;j++) {
                			if(errorFlag == 0){
                				var sumTypeRow = selectAllRow[j].SUM_TYPE;
                    			var status = selectAllRow[j].STATUS_CODE;
                    			if(sumTypeRow == sumType && status == "0") {
                    				var data = {
                                			"total" : selectAllRow.length,
                                			"data" : JSON.stringify(selectAllRow[j]),
                                			"billtype_code" : billTypeCode,//die
                                			"sumBilltype_code" : sumBillTypeNo,//die
                                			"type" : type,
                                			"menu_id" : pageId,
                            		        "op_type" : btn.ACTION_TYPE,
                            		        "op_name" : btn.DISPLAY_TITLE,
                                	}
                                	$.ajax({
                                        type: 'get',
                                        url: summaryURL + "?tokenid=" + tokenid + "&ajax=noCache",
                                        data: data,
                                        dataType: 'JSON',
                                        async: false,
                                        success: function (result) {   
                                            viewModel.refresh(selectNodeId,type,code,oid,"0");
                                            viewModel.initDepTree();
                                            treeObj.selectNode(nodes[0]);
                                        	if(result.errorCode == "0"){
                                                for(var n=0;n<btnData.length;n++) {
                                            		var btnId = btnData[n].BUTTON_ID;
                                            		var btnName = btnData[n].DISPLAY_TITLE;
                                            		if(btnData[n].DISPLAY_STATUS == "0" || btnData[n].ENABLED == "0") {
                                            			$("#"+btnId).attr("disabled","disabled");
                                            		}else {
                                            			$("#"+btnId).removeAttr("disabled","disabled");
                                            		}
                                            	} 
                                                if(j == (selectAllRow.length-1)){
                                                	if(type == "AGENCY"){
                                                		$("#panel-details").show();
                                                		viewModel.navShow(billTypeCode,bill_no,"true",code); 
                                           		    }else if(type == "MOFDEP"){
                                           		    	$("#panel-details").show();
                                           		    	viewModel.navShow(sumBillTypeDep,bill_no,"true",code);
                                           		    }else if(type == "OFFICE"){
                                           		    	$("#panel-details").show();
                                           		    	viewModel.navShow(sumBillTypeNo,bill_no,"true",code);
                                           		    }else{
                                           		    	$("#panel-details").hide();
                                           		    }
                                				}
                                        	}else {
                                        		errorFlag++;
                                        		ip.ipInfoJump("错误", 'error');
                                        	}
                                        }, error: function () {
                                        	errorFlag++;
                                            ip.ipInfoJump("错误", 'error');
                                        }
                                    });
                    			}else {
                    				ip.ipInfoJump("操作与数据状态不符，不能操作", 'info');
                    			}
                    		}else{
                    			ip.ipInfoJump("第"+(j-1)+"数据汇总失败", 'info');
                    			break;
                    		}
                		}    
                		var treeObj = $.fn.zTree.getZTreeObj("depTree");
                		var selectnodes2 = treeObj.getNodeByParam("CHR_ID", nodes[0].CHR_ID, null);
        				treeObj.selectNode(selectnodes2);
                	}else {
                		ip.ipInfoJump("没有数据，不能操作", 'info');
                	} 
            	}
            }else {
            	ip.ipInfoJump("请选择需要操作的机构", 'info');
            }          	
        };
        //退回
        workFlowReturn = function (id){
        	var treeObj = $.fn.zTree.getZTreeObj("depTree");
        	var nodes = treeObj.getSelectedNodes();
        	if(nodes.length > 0) {
        		var selectNodeId = nodes[0].CHR_ID;
                var depTreeData = viewModel.depTreeDataTable.getSimpleData();
                var type = nodes[0].TYPE;
                if(type != "MOFDEP" && sumType == 0){
            		ip.ipInfoJump("请选择司局进行汇总", 'info');
            	}else if(type != "OFFICE" && sumType == 1){
            		ip.ipInfoJump("请选择专员办进行汇总", 'info');
            	}else{
       		    var code = nodes[0].CHR_CODE;
       		    var oid = nodes[0].OID;
                var btnData = viewModel.btnDataTable.getSimpleData();
            	for(var bt = 0; bt<btnData.length;bt++) {
            		if(btnData[bt].BUTTON_ID == id) {
            			var btn = btnData[bt];
            			break;
            		}
            	}
            	var selectRow = viewModel.gridViewModel.gridData.getSimpleData();
            	if(selectRow.length > 0) { 
            		for(var j=0;j<selectRow.length;j++) {
            			var entity_id = selectRow[j].ID;
            			var sumTypeRow = selectRow[j].SUM_TYPE;
            			var status = selectRow[j].STATUS_CODE;
                		if(sumTypeRow == sumType && status == "0") {
                			var op_name = btn.DISPLAY_TITLE;
                    		var op_type = btn.ACTION_TYPE;
                			var data = {
                        			"menu_id":pageId,
                        			"entity_id":entity_id,
                        			"billtype_code":billTypeCode,//die
                        			"op_type":op_type,
                        			"op_name":op_name,
                        	};
                        	$.ajax({
                                type: 'get',
                                url: doPayWorkFlowURL + "?tokenid=" + tokenid + "&ajax=noCache",
                                data: data,
                                dataType: 'JSON',
                                async: false,
                                success: function (result) { 
                                    if(result.errorCode == 0){
                                    	 viewModel.refresh(selectNodeId,type,code,oid,"0");
                                         viewModel.initDepTree();
                                         treeObj.selectNode(nodes[0]);
                                         for(var n=0;n<btnData.length;n++) {
                                     		var btnId = btnData[n].BUTTON_ID;
                                     		if(btnData[n].DISPLAY_STATUS == "9" && btnData[n].ENABLED != "0") {
                                     			$("#"+btnId).removeAttr("disabled","disabled");
                                     		}else {
                                     			$("#"+btnId).attr("disabled","disabled");
                                     		}
                                     	} 
                                          ip.ipInfoJump(op_name+'成功！', 'success');
                                  	}else{
                                  		ip.ipInfoJump(result.message, 'error');
                                  	}
                                }, error: function () {
                                    ip.ipInfoJump("错误", 'error');
                                }
                            }); 
                		}else {
                			ip.ipInfoJump("操作与数据状态不符，不能操作", 'info');
                		}
                	}
            		var treeObj = $.fn.zTree.getZTreeObj("depTree");
            		var selectnodes2 = treeObj.getNodeByParam("CHR_ID", nodes[0].CHR_ID, null);
    				treeObj.selectNode(selectnodes2);
                }else {
            		ip.ipInfoJump("没有数据，不能操作", 'info');
            	} 
            	}
            }else {
            	ip.ipInfoJump("请选择需要操作的机构", 'info');
            }    
        };
        //撤销汇总
        Undosummary = function(id){
        	var treeObj = $.fn.zTree.getZTreeObj("depTree");
        	var nodes = treeObj.getSelectedNodes();
        	if(nodes.length > 0) {
        		var selectNodeId = nodes[0].CHR_ID;
        		var type = nodes[0].TYPE;
        		if(type != "MOFDEP" && sumType == 0){
            		ip.ipInfoJump("请选择司局进行汇总", 'info');
            	}else if(type != "OFFICE" && sumType == 1){
            		ip.ipInfoJump("请选择专员办进行汇总", 'info');
            	}else{
       		    var code = nodes[0].CHR_CODE;
       		    var oid = nodes[0].OID;
                var btnData = viewModel.btnDataTable.getSimpleData();
            	for(var bt = 0; bt<btnData.length;bt++) {
            		if(btnData[bt].BUTTON_ID == id) {
            			var btn = btnData[bt];
            			break;
            		}
            	}
            	var selectRow = viewModel.gridViewModel.gridData.getSimpleData();
            	if(selectRow.length > 0) { 
            		for(var i=0;i<selectRow.length;i++) {
            			var entity_id = selectRow[i].ID;
            			var status = selectRow[i].STATUS_CODE;
                		var sumTypeRow = selectRow[i].SUM_TYPE;
                		if(sumTypeRow != sumType && status == "0") {
                			var data = {
                    				"chr_id" : selectNodeId,	
                    				"oid" : selectRow[i].OID,
                    				"chr_code" : code,
                    				"id" : entity_id,
                    				"type" : type,
                    				"total" : selectRow.length,
                    				"menu_id" : pageId,
                    			    "billtype_code" : billTypeCode,
                    			    "sumBilltype_code" : sumBillTypeNo,
                    			    "op_type" : btn.ACTION_TYPE,
                    			    "op_name" : btn.DISPLAY_TITLE,
                    			};
                    			$.ajax({
                                    type: 'get',
                                    url: undoSummaryURL + "?tokenid=" + tokenid + "&ajax=noCache",
                                    data: data,
                                    dataType: 'JSON',
                                    async: false,
                                    success: function (result) {
                                        viewModel.refresh(selectNodeId,type,code,oid,"0");
                                        viewModel.initDepTree();
                                        treeObj.selectNode(nodes[0]);
                                    	if(result.errorCode == "0"){
                                            for(var n=0;n<btnData.length;n++) {
                                        		var btnId = btnData[n].BUTTON_ID;
                                        		var btnName = btnData[n].DISPLAY_TITLE;
                                        		if(btnData[n].DISPLAY_STATUS != "1" || btnData[n].ENABLED == "0") {
                                        			$("#"+btnId).attr("disabled","disabled");
                                        		}else {
                                        			$("#"+btnId).removeAttr("disabled","disabled");
                                        		}
                                        	}
                                    	}else {
                                    		ip.ipInfoJump("错误", 'error');
                                    	}
                                    }, error: function () {
                                        ip.ipInfoJump("错误", 'error');
                                    }
                                });
                    		}else{
                    			ip.ipInfoJump("操作与数据状态不符，不能操作", 'info');
                    		}
                	}
            		var treeObj = $.fn.zTree.getZTreeObj("depTree");
            		var selectnodes2 = treeObj.getNodeByParam("CHR_ID", nodes[0].CHR_ID, null);
    				treeObj.selectNode(selectnodes2);
            	}  else {
            		ip.ipInfoJump("没有数据，不能操作", 'info');
            	} 
            	}
            }else {
            	ip.ipInfoJump("请选择需要操作的机构", 'info');
            } 
        };
        //意见填写
        fillingClick = function(id){
        	var treeObj = $.fn.zTree.getZTreeObj("depTree");
        	var nodes = treeObj.getSelectedNodes();
        	if(nodes.length > 0) {
        		var selectNodeId = nodes[0].CHR_ID;
        		var type = nodes[0].TYPE;
        		if(type != "MOFDEP" && sumType == 0){
            		ip.ipInfoJump("请选择司局进行汇总", 'info');
            	}else if(type != "OFFICE" && sumType == 1){
            		ip.ipInfoJump("请选择专员办进行汇总", 'info');
            	}else{
       		    var code = nodes[0].CHR_CODE;
       		    var oid = nodes[0].OID;
       		    var bill_no = nodes[0].BILL_NO;
                var btnData = viewModel.btnDataTable.getSimpleData();
                for(var i=0;i<btnData.length;i++) {
            		var btnId = btnData[i].BUTTON_ID;
            		var btnName = btnData[i].DISPLAY_TITLE;
            		if(btnData[i].DISPLAY_STATUS != "0" || (btnData[i].ENABLED == "1" && btnData[i].DISPLAY_STATUS == "9")) {
            			$("#"+btnId).removeAttr("disabled","disabled");
            		}else {
            			$("#"+btnId).attr("disabled","disabled");
            		}
            	}
                var selectRow = viewModel.gridViewModel.gridData.getSimpleData();
    			if(selectRow.length > 0){
    				var dep_id = selectRow[0].AGENCY_ID;
    				if(type == "MOFDEP"){
           		    	viewModel.navShow(sumBillTypeDep,bill_no,"false",code);
           		    }else if(type == "OFFICE"){
           		    	viewModel.navShow(sumBillTypeNo,bill_no,"false",code);
           		    }else{
           		    	ip.ipInfoJump("请选择正确的机构", 'info');
           		    }
    			}
                
            	}
        	}else {
            	ip.ipInfoJump("请选择需要操作的机构", 'info');
            } 
        };
        //提交
        workFlowSup = function(id){
        	var treeObj = $.fn.zTree.getZTreeObj("depTree");
        	var nodes = treeObj.getSelectedNodes();
        	if(nodes.length > 0) {
        		var selectNodeId = nodes[0].CHR_ID;
        		var type = nodes[0].TYPE;
        		if(type != "MOFDEP" && sumType == 0){
            		ip.ipInfoJump("请选择司局进行汇总", 'info');
            	}else if(type != "OFFICE" && sumType == 1){
            		ip.ipInfoJump("请选择专员办进行汇总", 'info');
            	}else{
       		    var code = nodes[0].CHR_CODE;
       		    var oid = nodes[0].OID;
                var btnData = viewModel.btnDataTable.getSimpleData();
            	for(var bt = 0; bt<btnData.length;bt++) {
            		if(btnData[bt].BUTTON_ID == id) {
            			var btn = btnData[bt];
            			break;
            		}
            	}
            	var selectRow = viewModel.gridViewModel.gridData.getSimpleData();
            	if(selectRow.length > 0) { 
            		for(var j=0;j<selectRow.length;j++) {
            			var entity_id = selectRow[j].ID;
            			var status = selectRow[j].STATUS_CODE;
            			var sumTypeRow = selectRow[j].SUM_TYPE;
            			if(sumTypeRow != sumType && status == "0") {
            				var op_name = btn.DISPLAY_TITLE;
                    		var op_type = btn.ACTION_TYPE;
                			var data = {
                        			"menu_id":pageId,
                        			"entity_id":entity_id,
                        			"billtype_code":billTypeCode,
                        			"op_type":op_type,
                        			"op_name":op_name,
                        	};
                        	$.ajax({
                                type: 'get',
                                url: doPayWorkFlowURL + "?tokenid=" + tokenid + "&ajax=noCache",
                                data: data,
                                dataType: 'JSON',
                                async: false,
                                success: function (result) { 
                                	
                                    if(result.errorCode == 0){
                                    	for(var n=0;n<btnData.length;n++) {
                                    		var btnId = btnData[n].BUTTON_ID;
                                    		if(btnData[n].DISPLAY_STATUS == "9" && btnData[n].ENABLED == "1") {
                                    			$("#"+btnId).removeAttr("disabled","disabled");
                                    		}else {
                                    			$("#"+btnId).attr("disabled","disabled");
                                    		}
                                    	}                       	
                                        viewModel.refresh(selectNodeId,type,code,oid,"1");
                                        viewModel.initDepTree();
                                        treeObj.selectNode(nodes[0]);
                                         ip.ipInfoJump(op_name+'成功！', 'success');
                                 	}else{
                                 		ip.ipInfoJump(result.message, 'error');
                                 	}
                                }, error: function () {
                                    ip.ipInfoJump("错误", 'error');
                                }
                            }); 
            			}else {
            				ip.ipInfoJump("操作与数据状态不符，不能操作", 'info');
            			}
                	} 
            		var treeObj = $.fn.zTree.getZTreeObj("depTree");
            		var selectnodes2 = treeObj.getNodeByParam("CHR_ID", nodes[0].CHR_ID, null);
    				treeObj.selectNode(selectnodes2);
                }else {
                	ip.ipInfoJump("没有数据，不能操作", 'info');
            	}  
            	}
            }else {
            	ip.ipInfoJump("请选择需要操作的机构", 'info');
            }          	  	
        };     
        //获取汇总列表
        viewModel.sumData = function(chr_id,chr_code,billtype_code){
        	var data = {
        			"chr_code": chr_code,
        			"chr_id": chr_id,	
        			"billtype_code": billtype_code,
        	}
        	$.ajax({
                type: 'get',
                url: sumListURL + "?tokenid=" + tokenid + "&ajax=noCache",
                data: data,
                dataType: 'JSON',
                async: false,
                success: function (result) {
                    if (result.errorCode == "0") {
                        viewModel.exDataTable2.setSimpleData(result.data);                     
                    } else {
                        ip.ipInfoJump("错误", 'error');
                    }
                }, error: function () {
                    ip.ipInfoJump(result.message, 'error');
                }
            });
        }
        
    	//获取视图列表
        viewModel.supData = function(chr_id, type,chr_code,oid, status, billtypecode, menu_id) {
        	var data = {
        			"chr_code": chr_code,
        			"chr_id": chr_id,
        			"type": type,
        			"oid": oid,
        			"status": status,
        			"billtypecode": billtypecode,
        			"menu_id": pageId,
        			"task_id": task_id,
        	}
        	$.ajax({
                type: 'get',
                url: viewListURL + "?tokenid=" + tokenid + "&ajax=noCache",
                data: data,
                dataType: 'JSON',
                async: false,
                success: function (result) {
                    if (result.dataDetail != null) {
                        viewModel.exDataTable.setSimpleData(result.dataDetail);                     
                    } else {
                        ip.ipInfoJump("错误", 'error');
                    }
                }, error: function () {
                    ip.ipInfoJump("错误", 'error');
                }
            });
        };
        
      //初始化部门树
        viewModel.initDepTree = function () {
        	$.ajax({
	            type: 'get',
	            url: depTreeURL + "?tokenid=" + tokenid + "&ajax=noCache",
	            data: {"ele_code":eleCode,"sid":sid,"task_id":task_id,"is_allobj":IS_ALLOBJ,"obj_type_id":OBJ_TYPE_ID,},
	            dataType: 'JSON',
	            async: false,
	            success: function (result) {
	            	if(result.data.length>0) {
	            		var dep_array = [];
	            		viewModel.supData("", "", "", "", "9", billTypeCode, pageId);
	            		var exData = viewModel.exDataTable.getSimpleData();	            		
	            		for(var i=0;i<result.data.length;i++) {
	            			var flag = 0;
	            			var chrName = result.data[i].CHR_CODE + " " +result.data[i].CHR_NAME;
	            			if(result.data[i].TYPE == "AGENCY"){
	            				for(var j = 0;j<exData.length;j++) {
		            				if(exData[j].AGENCY_ID == result.data[i].CHR_ID){
		            					var chrName = result.data[i].CHR_CODE + " " +result.data[i].CHR_NAME + "<font color='green'>(已复核)</font>";
		            				}
		            			}
	            			}
	            			var showTreeLevels = showTreeLevel.split("+");
	            			var sumBillTypes = sumBillTypeCode.split("+");
	            			for(var level=0;level<showTreeLevels.length;level++) {
	            				if(result.data[i].LEVEL_NUM == showTreeLevels[level]){
		            				viewModel.sumData(result.data[i].CHR_ID, result.data[i].CHR_CODE,sumBillTypes[level]);
		    	            		var exData2 = viewModel.exDataTable2.getSimpleData();
		            				viewModel.supData(result.data[i].CHR_ID, result.data[i].TYPE, result.data[i].CHR_CODE, result.data[i].OID, "9", billTypeCode, pageId);
		    	            		var exData3 = viewModel.exDataTable.getSimpleData();
		    	            		if(exData3.length>0) {
		    	            			if(exData3[0].STATUS_CODE == "1") {
		    	            				var chrName = result.data[i].CHR_CODE + " " +result.data[i].CHR_NAME + "<font color='green'>(已提交)</font>";
			            					flag = 1;
		    	            			}else{
		    	            				if(exData2.length){
		    	            					for(var j = 0;j<exData2.length;j++) {
				    	            				if(showTreeLevels[level] == "1"){
				    	            					if(exData2[j].OID == result.data[i].CHR_CODE){
							            					var chrName = result.data[i].CHR_CODE + " " +result.data[i].CHR_NAME + "<font color='green'>(已汇总)</font>";
							            					flag = 1;
							            				}
				    	            				}else{
				    	            					if(exData2[j].MOFDEP_CODE == result.data[i].CHR_CODE){
							            					var chrName = result.data[i].CHR_CODE + " " +result.data[i].CHR_NAME + "<font color='green'>(已汇总)</font>";
							            					flag = 1;
							            				}
				    	            				}
						            				
						            			}
		    	            				}
		    	            			}
			            				if(flag == 0) {
			            					var chrName = result.data[i].CHR_CODE + " " +result.data[i].CHR_NAME + "<font color='red'>(待汇总)</font>";
			            				}
		    	            		}
		            			}
	            			}
	            			if(result.data[i].PARENT_ID == "" || result.data[i].PARENT_ID == null) {
	            				result.data[i].PARENT_ID = "000";
	            			}
	            			var depArray = {
	                                "CHR_ID": result.data[i].CHR_ID,
	                                "CHR_CODE": result.data[i].CHR_CODE,
	                                "DISP_CODE": result.data[i].DISP_CODE,
	                                "CHR_NAME": chrName,
	                                "LEVEL_NUM": result.data[i].LEVEL_NUM,
	                                "IS_LEAF": result.data[i].IS_LEAF,
	                                "TYPE": result.data[i].TYPE,
	                                "ENABLED": result.data[i].ENABLED,
	                                "CREATE_DATE": result.data[i].CREATE_DATE,
	                                "CREATE_USER": result.data[i].CREATE_USER,
	                                "LATEST_OP_DATE": result.data[i].LATEST_OP_DATE,
	                                "IS_DELETED": result.data[i].IS_DELETED,
	                                "LATEST_OP_USER": result.data[i].LATEST_OP_USER,
	                                "LAST_VER": result.data[i].LAST_VER,
	                                "RG_CODE": result.data[i].RG_CODE,
	                                "SET_YEAR": result.data[i].SET_YEAR,
	                                "PARENT_ID": result.data[i].PARENT_ID,
	                                "OID": result.data[i].OID
	                            }
	            			dep_array.push(depArray);
	            		}
	            		var depArray = {
                                "CHR_ID": "000",
                                "CHR_CODE": "",
                                "DISP_CODE": "",
                                "CHR_NAME": "全部",
                                "LEVEL_NUM": "",
                                "IS_LEAF": "",
                                "TYPE": "",
                                "ENABLED": "",
                                "CREATE_DATE": "",
                                "CREATE_USER": "",
                                "LATEST_OP_DATE": "",
                                "IS_DELETED": "",
                                "LATEST_OP_USER": "",
                                "LAST_VER": "",
                                "RG_CODE": "",
                                "SET_YEAR": "",
                                "PARENT_ID": "",
                                "OID": ""
                            }
            			dep_array.push(depArray);
	            		viewModel.depTreeDataTable.setSimpleData(dep_array);
	            		var treeObj = $.fn.zTree.getZTreeObj("depTree");
	                    treeObj.expandAll(true);
	            	}
	            }, error: function () {
	                ip.ipInfoJump("错误", 'error');
	            }
            });
        };          

        //单位树根据状态切换
        $('#depTreeTab a').click(function (e) {
            var exStatus = e.currentTarget.innerHTML;
            if(exStatus == "当前") {
                var depTreeData1 =  window.test.depTree;
                viewModel.depTreeDataTable.setSimpleData(depTreeData1);
                viewModel.initDepTree();
            }else if(exStatus == "未办结") {
                var depTreeData1 =  window.test.depTree;
                viewModel.depTreeDataTable.setSimpleData(depTreeData1);
                viewModel.initDepTree();
                var treeObj = $.fn.zTree.getZTreeObj("depTree2");
                treeObj.expandAll(true);
            }else{
                var depTreeData1 =  window.test.depTree;
                viewModel.depTreeDataTable.setSimpleData(depTreeData1);
                viewModel.initDepTree();
                var treeObj = $.fn.zTree.getZTreeObj("depTree3");
                treeObj.expandAll(true);
            }
        });                        
       
      //附件上传
        $("#fileUplode_li").click(fileData,function(event){	
        	$("#fileUploadPage")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
        	"&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&entityName=csof_sup_bill&oid="+fileData.oid+
        	"&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit;
        });
        
      //页签显示
		viewModel.navShow = function(billTypeCo,id,readonly,obj_id) {
			$.ajax({
		        type: 'post',
		        url: getNavURL + "?tokenid=" + tokenid + "&ajax=noCache",
		        data: {"billtypeCode":billTypeCo,"sid":sid},//die
		        dataType: 'JSON',
		        async: false,
		        success: function (result) {
		            if (result.errorCode == 0) {
		            	$("#details-li").addClass("active");
                 		$("#list-li").removeClass("active");
                 		$("#panel-list").removeClass("active");
                 		$("#panel-details").addClass("active");
		            	$("#panel-details").show();
		            	var data = result.data;
		            	$(".dynamic").remove();
		            	if(result.data.length == 0){
		            		$("#fileUplode_li").addClass("active");
		            		$("#panel-fileUpload").addClass("active");
		            		$("#fileUploadPage")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
		                	"&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&entityName=csof_sup_bill&oid="+fileData.oid+
		                	"&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit;
		            	}else{
		            		for(var i=0;i<data.length;i++) {
								if(i == 0) {
									$("#fileUplode_li").before("<li id='"+data[i].REPORT_ID+"_li' class='active dynamic'><a data-toggle='tab' href='#panel-"+data[i].REPORT_ID+"' id='"+data[i].REPORT_ID+"'>"+data[i].DISPLAY_TITLE+"</a></li>");
									$("#panel-fileUpload").before("<div class='tab-pane active dynamic' style='height:100%;' id='panel-"+data[i].REPORT_ID+"'><iframe class='irptShowIframe' id='"+data[i].REPORT_ID+"Page'></iframe></div>");
									$("#panel-"+data[i].REPORT_ID).addClass("active");
									viewModel.irptShow(data[i].REPORT_ID,readonly,obj_id);
								}else {
									$("#fileUplode_li").before("<li id='"+data[i].REPORT_ID+"_li' class='dynamic'><a data-toggle='tab' href='#panel-"+data[i].REPORT_ID+"' id='"+data[i].REPORT_ID+"'>"+data[i].DISPLAY_TITLE+"</a></li>");
									$("#panel-fileUpload").before("<div class='tab-pane dynamic' style='height:100%;' id='panel-"+data[i].REPORT_ID+"'><iframe class='irptShowIframe' id='"+data[i].REPORT_ID+"Page'></iframe></div>");
									$("#panel-"+data[i].REPORT_ID).removeClass("active");
								}
								$("#"+data[i].REPORT_ID+"_li").click(function(){
									var id = this.id;
									id=id.split("_")[0];
									viewModel.irptShow(id,readonly,obj_id);
								});
							}
							$("#panel-fileUpload").removeClass("active");
							$("#fileUplode_li").removeClass("active");
		            	}
		            } else {
		                ip.ipInfoJump("错误", 'error');
		            }
		        }, error: function () {
		            ip.ipInfoJump("错误", 'error');
		        }
		    });
		};
		
		//报表显示
		viewModel.irptShow = function(rid,readonly,obj_id) {
        	var data = {
					"chrId"	:rid,
					"obj_id" :obj_id,
					"param_add_str":"",
					"bbq_date":bbq_date,
					"readonly":readonly,
				};
        	$("#"+rid+"Page").attr("src", getReportURL + "?tokenid=" +  ip.getTokenId() + "&ajax=noCache" + "&chrId="+rid+"&obj_id="+obj_id+"&param_add_str=" +""+"&bbq_date="+bbq_date+"&readonly="+readonly);
		};
		
		//点击列表刷新视图
		$('#list-li').on('click' , function() {  
			$("#panel-details").hide();
			var treeObj = $.fn.zTree.getZTreeObj("depTree");
			var nodes = treeObj.getSelectedNodes();
        	if(nodes.length > 0) {
        		var selectNodeId = nodes[0].CHR_ID;
                var type = nodes[0].TYPE;
       		    var code = nodes[0].CHR_CODE;
       		    var oid = nodes[0].OID;
                var selectStatus = $("#submitStatus").val("9");                       
                viewModel.refresh(selectNodeId,type,code,oid,"9");
        	}
        	else{
        		$("#submitStatus").val("9"); 
        		viewModel.refresh("","","","","9");
        	}
		})
		
//		//详细显示报表
//		$('#details-li').on('click' , function() {        	
//			var selectRow = viewModel.gridViewModel.gridData.getSimpleData();
//			if(selectRow.length = 1){
//				$("#panel-details").show();
//				var bill_no = selectRow[0].BILL_NO;
//				var oid = selectRow[0].OID;
//				var dep_id = selectRow[0].AGENCY_ID;
//				var dep_code = selectRow[0].AGENCY_CODE;
//				var treeObj = $.fn.zTree.getZTreeObj("depTree");
//            	var nodes = treeObj.getSelectedNodes();
//            	if(nodes.length > 0) {
//            		fileData = {
//        					"entity_id":bill_no,
//        					"oid":oid,
//        					"dep_id":dep_id,
//        					"dep_code":dep_code,
//        					"edit" : "N",
//        			}
//            		var type = nodes[0].TYPE;
//            		if(type == "AGENCY"){
//            			viewModel.navShow(billTypeCode,bill_no,"true",nodes[0].CHR_CODE);  
//            		}else if(type == "MOFDEP"){
//            			viewModel.navShow(sumBillTypeDep,bill_no,"true",nodes[0].CHR_CODE);  
//            		}else if(type == "OFFICE"){
//           		    	viewModel.navShow(sumBillTypeNo,bill_no,"true",nodes[0].CHR_CODE);
//           		    }
//           		}
//			}else{
//				$("#panel-details").hide(); 
//        		ip.ipInfoJump("请选择一个需要操作的单位", 'info');
//			}
//		});
//		
//		//视图双击函数
//		testGrid_onDbClick = function(obj){
//		    	var index = obj.rowIndex;
//		    	$("#details-li").addClass("active");
//        		$("#list-li").removeClass("active");
//        		$("#panel-list").removeClass("active");
//        		$("#panel-details").addClass("active");
//        		$("#panel-details").show();
//        		var bill_no = obj.rowObj.value.BILL_NO;
//				var oid = obj.rowObj.value.OID;
//				var dep_id = obj.rowObj.value.AGENCY_ID;
//				var dep_code = obj.rowObj.value.AGENCY_CODE;
//        		fileData = {
//    					"entity_id":bill_no,
//    					"oid":oid,
//    					"dep_id":dep_id,
//    					"dep_code":dep_code,
//    					"edit":"N",
//    			}
//        		viewModel.navShow(billTypeCode,bill_no,"true",dep_code);   
//		 };
//        
      //生成表头
     	viewModel.initData = function(id,type,code,oid,status) {
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
    				viewModel.initGridData(id,type,code,oid,status); //调用初始化表格
    			}
    		});
     	};
        
        // 初始化表格
    	viewModel.initGridData = function(id,type,code,oid,status) {
            var queryViewId;
            var tableViewDetail;
            var queryViewDetail;
           
            for ( var n = 0; n < viewModel.viewList.length; n++) {
                var view = viewModel.viewList[n];
                if (view.viewtype == pViewType.VIEWTYPE_LIST) {// 列表视图
                    if (view.orders == '0') {
                    	viewModel.tableViewDetail = view;
                        options["chr_code"] = code;
                        options["type"] = type;
                        options["chr_id"] = id;
                        options["oid"] = oid;
                        options["status"] = status;
                        options["billtypecode"] = billTypeCode;
                        options["menu_id"] = pageId;
                        options["task_id"] = task_id;
                        payViewId = view.viewid;
                    }                    
                } 
            }
            viewModel.gridViewModel = ip.initGridWithCallBack(gridCallback,viewModel.tableViewDetail, 'testGrid', "/df/cs/selectAll.do",options, 1, false,false,false,false);
            if(viewModel.gridViewModel.gridData.getSimpleData()){
                $("#gridSumNum").html(viewModel.gridViewModel.gridData.getSimpleData().length);
            }
            //初始化Grid的select事件
//            viewModel.initGridSelectsFun();
            
          //初始化单位部门树
            viewModel.initDepTree();
            var treeObj = $.fn.zTree.getZTreeObj("depTree");
            treeObj.cancelSelectedNode();
    	};
    	
    	//grid查询
        viewModel.gridSearch = function () {
        	ip.fuzzyQuery(viewModel.curGridData, "gridSearchInput", viewModel.gridViewModel)
        }

      //回调函数，定义全局变量viewModel.curGridData并赋值
       gridCallback = function(data){
       	viewModel.curGridData = data;
       };    	    	
        
        //刷新视图
        viewModel.refresh = function(id,type,code,oid,status) {
        	var queryViewId;
            var tableViewDetail;
            var queryViewDetail;
           
            for ( var n = 0; n < viewModel.viewList.length; n++) {
                var view = viewModel.viewList[n];
                if (view.viewtype == pViewType.VIEWTYPE_LIST) {// 列表视图
                    if (view.orders == '0') {
                    	viewModel.tableViewDetail = view;
                        options["chr_code"] = code;
                        options["type"] = type;
                        options["chr_id"] = id;
                        options["oid"] = oid;
                        options["status"] = status;
                        options["billtypecode"] = billTypeCode;
                        options["menu_id"] = pageId;
                        options["task_id"] = task_id;
                        payViewId = view.viewid;
                    }
                } else if (view.viewtype == pViewType.VIEWTYPE_QUERY) {// 查询视图
                    if (view.orders == '1') {
                        queryViewId = view.viewid;
                        queryViewDetail = view;
                        options["chr_id"] = id;
                        options["type"] = type;
                        options["chr_code"] = code;
                        options["oid"] = oid;
                        options["status"] = status;
                        options["billtypecode"] = billTypeCode;
                        options["menu_id"] = pageId;
                        options["task_id"] = task_id;
                    }
                }
            }
            ip.setGridWithCallBack(gridCallback,viewModel.gridViewModel, "/df/cs/selectAll.do", options);
            $("#gridSumNum").html(viewModel.gridViewModel.gridData.getSimpleData().length);
        }       		
        
      //刷新
        publicRefresh = function() {
        	location.reload() 
        }
        
        //跳转流程界面
        viewModel.skipPage = function() {
        	location.href="flow.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +"&menuname=" + options.svMenuName + "&sid=" + sid +"&task_id=" + task_id +"&task_name=" +task_name;
        }
        setInterval(csscsof.windowHeightNoNav(), 500)//每半秒执行一次windowHeight函数
        //初始化
        function init() {
            u.createApp({
                el: document.body,
                model: viewModel
            });
            var url=window.location.href;
            pageId = url.split("pageId=")[1].split("&")[0];
            eleCode = url.split("elecode=")[1].split("&")[0];
            showTreeLevel = url.split("showTreeLevel=")[1].split("&")[0];
            billTypeCode =  url.split("billTypeCode=")[1].split("&")[0];
            sumType = url.split("sumType=")[1].split("&")[0];
            sumBillTypeCode = url.split("sumBillTypeCode=")[1].split("&")[0];
            if(sumBillTypeCode.split("+").length>1){
            	sumBillTypeNo = sumBillTypeCode.split("+")[0];
            	sumBillTypeDep = sumBillTypeCode.split("+")[1];
            }else {
            	sumBillTypeNo = sumBillTypeCode;
            	sumBillTypeDep = sumBillTypeCode;
            }
            sid = url.split("SID=")[1].split("&")[0];
            IS_ALLOBJ = url.split("IS_ALLOBJ=")[1].split("&")[0];
            OBJ_TYPE_ID = url.split("OBJ_TYPE_ID=")[1].split("&")[0];
            bbq_date = url.split("BBQ_DATE=")[1].split("&")[0];
            if(bbq_date == "" || bbq_date == null || bbq_date == "undefined"){
            	var nowDate = new Date();
            	 var year = nowDate.getFullYear();
            	 var month = nowDate.getMonth() + 1 < 10 ? "0" + (nowDate.getMonth() + 1) : nowDate.getMonth() + 1;
            	 var day = nowDate.getDate() < 10 ? "0" + nowDate.getDate() : nowDate.getDate();
            	 var dateStr = year + month + day;
            	 bbq_date = dateStr;
            }
            task_id = url.split("TASK_ID=")[1].split("&")[0];
            task_name = decodeURI(url.split("TASK_NAME=")[1].split("&")[0]);
            tokenid = ip.getTokenId();
            options = ip.getCommonOptions({});
            options['tokenid'] = tokenid;
			//初始化下拉框
            viewModel.initDropDown();
            //初始化按钮         
            var data = initBtns(pageId);
    		if(data == false){
    			ip.ipInfoJump("加载按钮出错", 'error');
    		}else{
    			viewModel.btnDataTable.setSimpleData(data);
    		}
			//初始化grid
            viewModel.initData("","","","","9");        
            csscsof.windowHeightNoNav();
        }

        init();
    }
)