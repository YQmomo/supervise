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
        var pageData = {//通过交互获取数据
        		 getModalTree: function(){//获取modal的树数据
        	        	$.get('/ItemSupervision/selectObjectByDep.do?tokenid='+ tokenid + '&ajax=' + 'noCache',function(map){
        	        		pageData.allModalData = map;
        	        		var eventNum = 0;
        	        		var arr = [];
        	        		if(viewModel.data.radio() == '0'){
        	             	   $.each(pageData.allModalData.Objectlist, function(index, item){
        	                    	if(item.IS_LEAF == '1'){
        	                    		eventNum++;
        	                    		arr.push(item);
        	                    	}
        	                    });
        	                    viewModel.data.eventsNum('共' +eventNum+ '家');
        	                }
        	                 postData.Objectlist = JSON.stringify(arr);
        	        	});
        	        }
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
        		 data: {//监控页面数据
        	    	    eventsNum: ko.observable('共3家'),
        	    	    radio: ko.observable('0'),
        	    	    totelManHour: ko.observable(0),
        	         },
        		
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
                        var conditionMap = {};
                        var conditionRela = {};
                        var radio =document.getElementsByName("mainStatus");
                		var submit_status = null;
                    	for(var i =0;i < radio.length;i++){
            	        	 if(radio[i].checked){
            	        	     submit_status = radio[i].value;
            	        	 }
                    	}
                    	if(node.CHR_NAME == '全部' || node.chr_name == '全部'){
                            viewModel.gridRefresh(0,"",submit_status);
                            viewModel.detailGridViewModel.gridData.clear();
                        }else{
                            conditionMap[listCode] = listId;
                            conditionRela[listCode] = "=";
                            viewModel.gridRefresh(0,conditionMap,conditionRela,submit_status);
                            viewModel.detailGridViewModel.gridData.clear();
                        }
                    }
                }
            },
            treeSetting3:{//左树
                view:{
                    showLine:false,
                    selectedMulti:false
                },
                check : {//设置checkbox
                	enable: true,
            		autoCheckTrigger: false,
            		chkboxType: { "Y": "p", "N": "p" }
                },
                callback:{
                    onClick:function(e,id,node){
                    },
                    onCheck: function(event, treeId, treeNode){
                    	var nodes = $("#tree3")[0]['u-meta'].tree.getCheckedNodes(true);
                    	var arr = [];
                    	var sumOfChecked;
                    	var pNode = treeNode;//.getParentNode();//获取父节点

                    	function filter(node){
                    		return (node.checked == true);
                    	}
                    	function filter2(node){
                    		return (node.checked == false);
                    	}
                		var childrenNodes = treeNode.children;

                		if(treeNode.isParent == true){
                			if(treeNode.checked){
                    		    var nodesFilter = $("#tree3")[0]['u-meta'].tree.getNodesByFilter(filter2,false,treeNode); // 查找节点集合
                    		    $.each(nodesFilter, function(index, item){
                        			$("#tree3")[0]['u-meta'].tree.checkNode(item, treeNode.checked, false);
                        		});
                    		}else{
                    			var nodesFilter = $("#tree3")[0]['u-meta'].tree.getNodesByFilter(filter,false,treeNode); // 查找节点集合
                    		    $.each(nodesFilter, function(index, item){
                        			$("#tree3")[0]['u-meta'].tree.checkNode(item, treeNode.checked, false);
                        		});
                    		}
                		}
                    	var showNum = 0;

                    	function filter3(node){
                    		return (1 == 1);
                    	}
                    	var allCheckNodes = $("#tree3")[0]['u-meta'].tree.getNodesByFilter(filter3,false);
                    	$.each(allCheckNodes, function(index, item){
                    		//var nowNode = treeNode;
                    		treeNode = item;
                    		var size = 0;
                    		var nodesFilter = $("#tree3")[0]['u-meta'].tree.getNodesByFilter(filter,false,treeNode);
                    		$.each(nodesFilter, function(index, item){
	                			if(item.IS_LEAF == '1'){
	                				size++;
	                			}

                		    });
                    		
                    		var value = treeNode.CHR_NAME;
                    		var html = '<span>'+ value +'</span><div class="treeNode">'+ size +'</div>';
                    		if(size == 0){
                    			html = '<span>'+ value +'</span>';
                    		}
                    		
                    		$("#" + treeNode.tId + "_span").html('');
                    		$("#" + treeNode.tId + "_span").html(html);//创建后续的数量显示
                    	});
                    	var checkNodes = $("#tree3")[0]['u-meta'].tree.getCheckedNodes(true);
                    	$.each(checkNodes, function(index, item){
                    		if(item.IS_LEAF == '1'){
                    			showNum++;
                    		}
                    	});
                    	viewModel.data.eventsNum('共' +showNum+ '家');
                    }

                }

            },
            dataTable3: new u.DataTable({
                meta: {
                    'CHR_ID': {},
                    'PARENT_ID': {},
                    'CHR_NAME':{}
                }
            }),
            btnEvents: function(data, events){//所有单击事件
                var $this = $(events.target),
                    name = $this[0].name;
                if(name == 'btn1'){//上一步b
                    num--;
                    if(num == 0){
                        $('#btn1').hide();
                    }
                    if(num < 2){
                        $('#btn2').show();
                        $('#btn3').hide();
                    }
                    eventsDeal.reduceStep();
                    eventsDeal.modalTab();
                }
                if(name == 'btn2'){//下一步
                	var flag = 0;
                	if(num == 0){
                		 var aim2 = viewModel.vailData;
                         var noVerifyRid =["REMARK"];
                         if(!verifyInputView(aim2,noVerifyRid)){
                             flag++;
                         }
                    }
                	if(num == 1 && viewModel.data.radio() == '1'){
                    	var nodes = $("#tree3")[0]['u-meta'].tree.getCheckedNodes(true);
                    	var arr = [];
                    	var arr2 = [];
                    	$.each(nodes, function(index, item){
                    		$.each(pageData.allModalData.Objectlist, function(index2, item2){
                    			if(item2.CHR_ID == item.CHR_ID){
                    				if(item2.TYPE == 'DEP'){
                    					arr.push(item2);
                    				}
                    			}
                    		});
                    	});
                    	/*$.each(nodes, function(index, item){
                    		$.each(pageData.allModalData.Objectlist, function(index2, item2){
                    			if(item2.CHR_ID == item.CHR_ID){
                    				if(item2.TYPE == 'DEP'||item2.TYPE == 'AGENCY'){
                    					arr2.push(item2);
                    				}
                    			}
                    		});
                    	});*/
                    	
                    	
                    	postData.Objectlist = JSON.stringify(arr);
                    	postData.agencyList = JSON.stringify(nodes);
                    }
                	 if(flag > 0){
                         return;
                     }else{
                         num++;
                         $('#btn1').show();
                         eventsDeal.addStep();
                         eventsDeal.modalTab();
                     }
                    if(num == 2){
                    	var indeces = viewModel.listGridViewModel.gridData.getSelectedIndices();
            			var datas = viewModel.listGridViewModel.gridData.getSimpleData();
            			var index = indeces[0];
                        var focusData = datas[index];
            	    	workResoveImp(focusData);
                        $('#btn2').hide();
                        $('#btn3').show();
                    }
                }
                if(name == 'btn3'){//完成
                	if(parseFloat(viewModel.data.totelManHour()) != parseFloat($("#PLAN_COST-"+dealData.getIdLast(viewModel.inputViewList[0].viewid)).val())){
                		ip.warnJumpMsg('当前工时合计（'+parseFloat(viewModel.data.totelManHour())+'）与计划总工时（'+parseFloat($("#PLAN_COST-"+dealData.getIdLast(viewModel.inputViewList[0].viewid)).val())+'）不一致，是否继续保存',"delConfirmSureId","delConfirmCancelCla");
                        $('#delConfirmSureId').on('click',function(){
                            $('#config-modal').remove();
                            saveResove();
                            num = 0;
                            eventsDeal.modalTab();
                            eventsDeal.reduceStep();
                            $('#addModal').modal('toggle');
                            $('#btn2').show();
                            $('#btn4').show();
                        });
                        $('.delConfirmCancelCla').on('click',function(){
                            $('#config-modal').remove();
                        });
                	}else{
                		saveResove();
                		num = 0;
                        eventsDeal.modalTab();
                        eventsDeal.reduceStep();
                        $('#addModal').modal('toggle');
                        $('#btn2').show();
                        $('#btn4').show();
                	}
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
//          //删除操作
//		    del: function(rowId) {
//		        var datatableRow = viewModel.workResoveGridViewModel.gridData.getRowByRowId(rowId);
//		        //请求后端删除对应的数据；
//		        // index为数据下标
//		        viewModel.workResoveGridViewModel.gridData.removeRow(datatableRow);
//		    },
            //任务分解
		    workResoveDataTable: new u.DataTable({
		    	meta: {
		           
		        }
		    }),
		    getViewData: function(number){//获取视图数据
                var arr = viewModel.inputViewList[number].viewDetail;
                $.each(arr,function(key, item){
                    if(item.disp_mode == 'treeassist'){
                        postData[item.id] = $('#' + item.id + '-' + dealData.getIdLast(item.viewid) + '-h').val();
                    }else{
                        postData[item.id] = $('#' + item.id + '-' + dealData.getIdLast(item.viewid)).val();
                    }
                });
            },
        };
        
        viewModel.initTree = function() {
            $.get('/df/tree/getElementData.do?tokenid='+ tokenid + '&ajax=' + 'noCache', {'ele_code': elecode}, function(map){
                if(map.errorCode == '0'){
                    var data = map.dataDetail || map.data;
                    var obj = {
                        CHR_ID : 'type',
                        PARENT_ID : 'root',
                        CHR_NAME : '全部',
                        chr_id : 'type',
                        parent_id : 'root',
                        chr_name : '全部'
                    };
                    $.each(data, function(index, item){
                        if(item.level_num == '1' || item.LEVEL_NUM == '1'){
                            item.PARENT_ID = 'type';
                            item.parent_id = 'type';
                        }
                    });
                    data.push(obj);
                    viewModel.treeDataTable.setSimpleData(data);
                    var treeObj = $.fn.zTree.getZTreeObj("tree1");
                    treeObj.expandAll(true);
                    treeObj.cancelSelectedNode();
                }else{
                    ip.ipInfoJump(map.errorMsg, 'error');
                }
            });
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
                    var arr = ['infoAdd', 'objectAdd', 'workResoveAdd'];
                    $('#' + arr[num]).addClass('active').siblings().removeClass('active');
                }
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
                if (view.orders == '1') {
                	getOptions();
                	options["menu_id"] = menuId;
                	options["status"] = "9";
                	options["billtype_code"] = billtype_code;
                	viewModel.detailGridViewModel = ip.initGrid(viewModel.listViewList[n],'detailGrid', viewModel.listViewList[n].remark + '?', options, 0, false,false,false,false);
                    if(viewModel.detailGridViewModel.gridData.getSimpleData()){
                        $("#detailGridSumNum").html(viewModel.detailGridViewModel.gridData.getSimpleData().length);
                    }
                    viewModel.changeBtnStatusBysetGrid(viewModel.detailGridViewModel);
                }else if(view.orders == '0'){
                	getOptions();
                	options["menu_id"] = menuId;
                	options["status"] = "9";
                	options["billtype_code"] = mof_billtype_code;
                	viewModel.listGridViewModel = ip.initGridWithCallBack(gridCallback,viewModel.listViewList[n],'mainGrid', viewModel.listViewList[n].remark + '?', options,1, false,true,false,false);
//                	viewModel.listGridViewModel = ip.initGridWithCallBack(gridCallback,viewModel.listViewList[n],'mainGrid', '/df/sup/getSup.do?', options, 1, false,true,false,false);
                	if(viewModel.listGridViewModel.gridData.getSimpleData()){
                        $("#mainGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);
                    }
                    viewModel.changeBtnStatusBysetGrid(viewModel.listGridViewModel);
                }else if(view.orders == '2'){
                	getOptions();
                	viewModel.workResoveGridViewModel = ip.initGrid(viewModel.listViewList[n],'workResoveGrid', viewModel.listViewList[n].remark + '?', {}, 0, false,true,false,false,true);                	
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
//        
//      //定义操作列的内容
//        workResoveGrid = function(obj) {
//	        var dataTableRowId = obj.row.value['$_#_@_id'];
//	        var delfun = "data-bind=click:del.bind($data," + dataTableRowId + ")";
//	        obj.element.innerHTML = '<div><i class=" op icon uf uf-del title="删除" ' + delfun + '></i></div>';
//	        ko.applyBindings(viewModel, obj.element);
//	    };

        //刷新Grid表格数据
        viewModel.gridRefresh = function(orders,conditionMap,conditionRela,status) {
        	getOptions();
        	if(orders == '0'){
            	options["menu_id"] = menuId;
            	if(!status || status == ""){
            		status = "9";
            	}
            	options["status"] = status;
            	options["billtype_code"] = mof_billtype_code;
            	if(conditionMap || conditionMap != ""){     		
           	        options["condition_data"] = JSON.stringify(conditionMap);
           	    }
            	
            	if(conditionRela || conditionRela != ""){     		
           	        options["condition_rela"] = JSON.stringify(conditionRela);
           	    }
                ip.setGridWithCallBack(gridCallback,viewModel.listGridViewModel, viewModel.listViewList[0].remark + '?',options);
//            	ip.setGridWithCallBack(gridCallback,viewModel.listGridViewModel, '/df/sup/getSup.do?' ,options);
            	$("#mainGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);
//                viewModel.changeBtnStatusBysetGrid(viewModel.listGridViewModel);
            }else if (orders == '1') {
            	options["menu_id"] = menuId;
//            	if(!status || status == ""){
//            		status = "9";
//            	}
//            	options["status"] = status;
//            	options["billtype_code"] = billtype_code;
//            	if(conditionMap || conditionMap != ""){     		
//           	        options["conditionMap"] = JSON.stringify(conditionMap);
//           	    }
            	options["action"] = "Q";
            	options["billtype_code"] = billtype_code;
            	if(conditionMap || conditionMap != ""){     		
           	        options["condition_data"] = JSON.stringify(conditionMap);
           	    }
            	if(conditionRela || conditionRela != ""){     		
           	        options["condition_rela"] = JSON.stringify(conditionRela);
           	    }
            	ip.setGrid(viewModel.detailGridViewModel,'/df/common/commonAction.do?', options);
	            //ip.setGrid(viewModel.detailGridViewModel,viewModel.listViewList[1].remark + '?', options);
	            if(viewModel.detailGridViewModel.gridData.getSimpleData()){
	                $("#detailGridSumNum").html(viewModel.detailGridViewModel.gridData.getSimpleData().length);
	            }else{
	            	$("#detailGridSumNum").html("");
	            }
	//            viewModel.changeBtnStatusBysetGrid(viewModel.detailGridViewModel);
	        }else if(orders == '2'){
	            ip.setGrid(viewModel.workResoveGridViewModel,viewModel.listViewList[2].remark + '?', options);
	            viewModel.workResoveGridViewModel.gridData.clear();
	        }
        };
        
        mainGrid_onRowSelected = function(obj) {
        	var index = obj.rowIndex;
        	var data = viewModel.listGridViewModel.gridData.getSimpleData();
        	pageData.resolveData =  data[index];
        	var mof_bill_id = obj.rowObj.value.ID;
        	var conditionMap = {};
            conditionMap["mof_bill_id"] = mof_bill_id;
            var conditionRela = {};
            conditionRela["mof_bill_id"] = "=";
        	viewModel.gridRefresh(1,conditionMap,conditionRela);
        }
        
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
    	        	'<label><input name="mainStatus" type="radio" value="0" onchange ="submitStatusChange(this.name)"/>待分解</label> '+
    	        	'<label><input name="mainStatus" type="radio" value="1" onchange ="submitStatusChange(this.name)"/>已分解</label>');
   		    var radio =document.getElementsByName("mainStatus");
     	    for(var i =0;i < radio.length;i++){
	        	   if(radio[i].value == "9"){
	        		  radio[i].checked = true;
	        	   }else{
	        		  radio[i].checked = false;
	        	   }
     	    }
     	    //副单状态
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
    		var conditionRela  = {};
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
            			 var type_code = "SUP_TYPE_ID";
                  	    conditionMap[type_code] = selectNodeId;
                  	    conditionRela[type_code] = "=";
                    }
                }
                viewModel.gridRefresh(0,conditionMap,conditionRela,submit_status);
                if(viewModel.detailGridViewModel){
                    viewModel.detailGridViewModel.gridData.clear();
                }
        	}
//        	else if(type="detailStatus"){
////        		if(submit_status != "9"){
////                	conditionMap["status_code"] = submit_status;
////        		}
//        		viewModel.gridRefresh(0,conditionMap,submit_status);
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
        
      //工作流退回
        workFlowReturn = function(id) {
        	workFlow(id,mof_billtype_code);
        }

        //工作流/df/workflow/work.do
        workFlow = function(id,billtypecode) {
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	var btn = getBtnMsgById(btnData,id);
        	var selectRow = viewModel.listGridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
        		var flag = 0;
        		for(var i=0;i<selectRow.length;i++){
        			if(selectRow[i].data.status_code.value == "0"){
        				var data = {
                    			"menu_id":menuId,
                    			"entity_id":selectRow[i].data.id.value,
                    			"billtype_code":billtypecode,
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
    			viewModel.initTree();
    			var radio =document.getElementsByName("mainStatus");
        		var submit_status = null;
            	for(var i =0;i < radio.length;i++){
    	        	 if(radio[i].checked){
    	        	     submit_status = radio[i].value;
    	        	 }
            	}
            	viewModel.gridRefresh(0,"","",submit_status);
            	viewModel.detailGridViewModel.gridData.clear();
        	}else {
        		ip.ipInfoJump("请勾选需要操作的事项内容", 'info');
        	}
        }
        
        //分解
        resolveClick = function(id) {//分解
			var indeces = viewModel.listGridViewModel.gridData.getSelectedIndices();
			var datas = viewModel.listGridViewModel.gridData.getSimpleData();
			if(indeces.length == 1){
                var index = indeces[0];
                var focusData = datas[index];
				if(focusData.status_code == "0"){
					$('#addModal').modal({
	                	show : true,
	                	backdrop : 'static'
	                });
					num = 0;
	                var eventNum = 0;
	                var arr = [];
	                viewModel.data.radio('0');
	                postData.IS_ALLOBJ = '1';//默认选择全部
	                $('#btn2').show();
	                $('#btn1').hide();
	                $('#btn3').hide();
	                if(num == 0){
	              	   $.each(pageData.allModalData.Objectlist, function(index, item){
	                     	if(item.TYPE == 'AGENCY'){
	                     		arr.push(item);        // TO DO    arr用途待确认
	                     	}
	                     	
	                     	if(item.IS_LEAF == '1'){          //  eventNum++放到 is_leaf判断，非 type=agency
	                     		eventNum++;
	                     	}
	                     });
	                     viewModel.data.eventsNum('共' +eventNum+ '家');
	                 }
	                $("#baseInfo").html("");
	                viewModel.vailData = ip.initArea(viewModel.inputViewList[0].viewDetail,'edit',dealData.getIdLast(viewModel.inputViewList[0].viewid), 'baseInfo');
	                var arr = viewModel.inputViewList[0].viewDetail;
	                var viewListViewId;
	            	$.each(arr,function(key, item){
	    	    		viewListViewId = item.viewid;
	    	    		$.each(focusData, function(key2, value2){
	    	    			if(item.id == key2.toUpperCase()||item.id==key2.toLowerCase()){
	    	    				$('#' + item.id + '-' + dealData.getIdLast(item.viewid)).val(value2);
	    	    			}
	    	    		});
	    	    	});
	               	var deleteArr = ['REMARK', 'PLAN_COST'];
	    	    	$.each(deleteArr, function(index, item){
	    	    		$('#' + item + '-' + dealData.getIdLast(viewListViewId)).val('');
	    	    	});
				}else{
					ip.ipInfoJump('选择的任务已分解,不可再次分解！', 'info');
				}
			}else{
				ip.ipInfoJump('请选择一个要分解的任务,勿多选或不选！', 'info');
			}
		};
		
		var workResoveDate = {
			year : [new Date().getFullYear()],
		    year_name : [""],
		    year_startDate : [new Date().getFullYear()+"-1-1"],
		    year_endDate : [new Date().getFullYear()+"-12-31"],
			halfYear_name : ["上半年","下半年"],
			halfYear : [ "01", "07" ],
			halfYear_startDate : [new Date().getFullYear()+"-1-1",new Date().getFullYear()+"-7-1"],
			halfYear_endDate : [new Date().getFullYear()+"-6-30",new Date().getFullYear()+"-12-31"],
			quarter_name : ["第一季","第二季","第三季","第四季"],
			quarter:["01", "04", "07", "10" ],
			quarter_startDate : [new Date().getFullYear()+"-1-1",new Date().getFullYear()+"-4-1",new Date().getFullYear()+"-7-1",new Date().getFullYear()+"-10-1"],
			quarter_endDate : [new Date().getFullYear()+"-3-31",new Date().getFullYear()+"-6-30",new Date().getFullYear()+"-9-30",new Date().getFullYear()+"-12-31"],
			month_name : [ "01月", "02月", "03月", "04月", "05月", "06月", "07月", "08月", "09月", "10月","11月", "12月" ],
			month:[ "01--", "02--", "03--", "04--", "05--", "06--", "07--", "08--", "09--","10--", "11--", "12--" ],
			month_startDate : [new Date().getFullYear()+"-1-1",new Date().getFullYear()+"-2-1",new Date().getFullYear()+"-3-1",new Date().getFullYear()+"-4-1",new Date().getFullYear()+"-5-1",new Date().getFullYear()+"-6-1",new Date().getFullYear()+"-7-1",new Date().getFullYear()+"-8-1",new Date().getFullYear()+"-9-1",new Date().getFullYear()+"-10-1",new Date().getFullYear()+"-11-1",new Date().getFullYear()+"-12-1",],
			month_endDate : [new Date().getFullYear()+"-1-31",new Date().getFullYear()+"-2-28",new Date().getFullYear()+"-3-31",new Date().getFullYear()+"-4-30",new Date().getFullYear()+"-5-31",new Date().getFullYear()+"-6-30",new Date().getFullYear()+"-7-31",new Date().getFullYear()+"-8-31",new Date().getFullYear()+"-9-30",new Date().getFullYear()+"-10-31",new Date().getFullYear()+"-11-30",new Date().getFullYear()+"-12-31"],
			oneOff:[ "--------" ],
			oneOff_name:[""],
			oneOff_startDate:[""],
			oneOff_endDate:[""],
			nonScheduled:[ "--------" ],
			nonScheduled_name:[""],
			nonScheduled_startDate:[""],
			nonScheduled_endDate:[""],
		}
		var workResoveDateMap = {
				cycleMap : { 
					"1":workResoveDate.year,
					"2":workResoveDate.halfYear,
					"3":workResoveDate.quarter,
					"4":workResoveDate.month,
					"5":workResoveDate.oneOff,
					"0":workResoveDate.nonScheduled,
				},
			    cycleMapName:{
			    	"1":workResoveDate.year_name,
					"2":workResoveDate.halfYear_name,
					"3":workResoveDate.quarter_name,
					"4":workResoveDate.month_name,
					"5":workResoveDate.oneOff_name,
					"0":workResoveDate.nonScheduled_name,
			    },
			    cycleMapStartDate:{
			    	"1":workResoveDate.year_startDate,
					"2":workResoveDate.halfYear_startDate,
					"3":workResoveDate.quarter_startDate,
					"4":workResoveDate.month_startDate,
					"5":workResoveDate.oneOff_startDate,
					"0":workResoveDate.nonScheduled_startDate,
			    },
			    cycleMapEndDate:{
			    	"1":workResoveDate.year_endDate,
					"2":workResoveDate.halfYear_endDate,
					"3":workResoveDate.quarter_endDate,
					"4":workResoveDate.month_endDate,
					"5":workResoveDate.oneOff_endDate,
					"0":workResoveDate.nonScheduled_endDate,
			    },
		}
		
		//任务分解
		workResoveImp = function(focusData) {
	        $("#playHour").html("");
			viewModel.gridRefresh("2");
			var cycleArray = workResoveDateMap.cycleMap[focusData.sup_cycle];
			var cycleNameArray = workResoveDateMap.cycleMapName[focusData.sup_cycle];
			var cycleStartDateArray = workResoveDateMap.cycleMapStartDate[focusData.sup_cycle];
			var cycleEndDateArray = workResoveDateMap.cycleMapEndDate[focusData.sup_cycle];
			var str = workResoveDate.year[0];
            if(focusData.sup_cycle == "4"){
            	var cond1 = str % 4 == 0;  //条件1：年份必须要能被4整除  
                var cond2 = str % 100 != 0;  //条件2：年份不能是整百数  
                var cond3 = str % 400 == 0;  //条件3：年份是400的倍数  
                //当条件1和条件2同时成立时，就肯定是闰年，所以条件1和条件2之间为“与”的关系。  
                //如果条件1和条件2不能同时成立，但如果条件3能成立，则仍然是闰年。所以条件3与前2项为“或”的关系。  
                //所以得出判断闰年的表达式：  
                var cond = cond1 && cond2 || cond3;  
                if (cond) {  
                	cycleEndDateArray[1] = str + "-2-29";  
                }
			}
	        if (focusData.sup_cycle == "0" || focusData.sup_cycle == "5" || focusData.sup_cycle == "1") {
	          str = "";
	        }
	        viewModel.data.totelManHour(0);
	        if(cycleArray.length > 0){
				var plan_cost = parseFloat($("#PLAN_COST-"+dealData.getIdLast(viewModel.inputViewList[0].viewid)).val()) / cycleArray.length;
			}
	        $("#playHour").html($("#PLAN_COST-"+dealData.getIdLast(viewModel.inputViewList[0].viewid)).val());
	        var remark = $("#REMARK-"+dealData.getIdLast(viewModel.inputViewList[0].viewid)).val();
			for(var i=0;i<cycleArray.length;i++){
				if(cycleArray.length > 1){
					var task_name = focusData.sup_name + "（" + cycleNameArray[i] + "）";
				}else{
					var task_name = focusData.sup_name;
				}
				var sup_date = str  + cycleArray[i];
				var no = i < 9 ? "0" : "";
				var start_time = cycleStartDateArray[i];
				var end_time = cycleEndDateArray[i];
				var data = {
                      'TASK_NAME' : task_name,
                      'SUP_DATE' : sup_date,
                      'PLAN_COST' : plan_cost,
                      'PLAN_BEGIN_DATE' : start_time,
                      'PLAN_END_DATE' : end_time,
                      'REMARK' : remark,
//                      'TASK_NO' : focusData.bill_no + no + i,
                };
				viewModel.workResoveGridViewModel.gridData.addSimpleData(data,Row.STATUS.NEW);
			}
			viewModel.workResoveGridViewModel.gridData.setEnable(true);
			viewModel.workResoveGridViewModel.gridData.setAllRowsUnSelect();
		};
		
		//任务分解总工时计算
		workResoveGrid_onValueChange = function(obj){
			var oldValue = obj.oldValue;
			if(obj.field == "PLAN_COST"){
				if(oldValue == null || oldValue == ""){
					oldValue = 0;
				}
				var totleManHour  = viewModel.data.totelManHour();
				var currentTotleManHour = parseFloat(totleManHour) - parseFloat(oldValue) + parseFloat(obj.newValue);
				viewModel.data.totelManHour(currentTotleManHour);
			}
		};
		
		//任务分解新增
		workResoveAdd = function() {
			var createRow = viewModel.workResoveGridViewModel.gridData.createEmptyRow();
			createRow.setValue("PLAN_COST",0);
			createRow.setValue("REMARK",$("#REMARK-"+dealData.getIdLast(viewModel.inputViewList[0].viewid)).val());
//			var indeces = viewModel.listGridViewModel.gridData.getSelectedIndices();
//			var datas = viewModel.listGridViewModel.gridData.getSimpleData();
//			var index = indeces[0];
//            var focusData = datas[index];
//			var num = viewModel.workResoveGridViewModel.gridData.getSimpleData().length + 13;
//			var no = num < 9 ? "0" : "";
//			//createRow.setValue('TASK_NO',focusData.bill_no + no + num) ;
//			viewModel.workResoveGridViewModel.gridData.setValue('TASK_NO',focusData.bill_no + no + num,createRow) //设置在指定行字段值
		}
		
		//任务分解删除
		workResoveDel = function(){
			var rowId = viewModel.workResoveGridViewModel.gridData.getSelectedRows()[0].rowId;
			var datatableRow = viewModel.workResoveGridViewModel.gridData.getRowByRowId(rowId);
			var totleManHour = viewModel.data.totelManHour();
		    var delHour = datatableRow.data.PLAN_COST.value;
		    var currentTotleManHour = parseFloat(totleManHour) - parseFloat(delHour);
		    viewModel.data.totelManHour(currentTotleManHour);
	        viewModel.workResoveGridViewModel.gridData.removeRow(datatableRow);
	       
		}
        
        $('input:radio[name="inlineRadioOptions"]').on('click', function(){
        	viewModel.data.radio($('input:radio[name="inlineRadioOptions"]:checked').val());
        	if(viewModel.data.radio() == '0'){
        		var eventNum = 0;
        		postData.IS_ALLOBJ = '1';
        		viewModel.dataTable3.setSimpleData([]);
        		$.each(pageData.allModalData.Objectlist, function(index, item){
                	if(item.IS_LEAF == '1'){
                		eventNum++;
                	}
                });
                viewModel.data.eventsNum('共' +eventNum+ '家');
        	}else{
        		postData.IS_ALLOBJ = '0';
        		viewModel.data.eventsNum('共0家');
        		viewModel.dataTable3.setSimpleData(pageData.allModalData.Objectlist);
        	}
        });
        
        //分解保存
        saveResove = function() {
        	 var objectlist = [];
        	 var agencylist = [];
//             postData = {};
             viewModel.getViewData(0);
             postData["PLAN_COST"] = viewModel.data.totelManHour();
             if( viewModel.data.radio() == '1'){
             	var nodes = $("#tree3")[0]['u-meta'].tree.getCheckedNodes(true);
             	$.each(nodes, function(index, item){
             		$.each(pageData.allModalData.Objectlist, function(index2, item2){
             			if(item2.CHR_ID == item.CHR_ID){
             				if(item2.TYPE == "DEP"){
             					var  depValue = {};
             					depValue["CHR_ID"] = item2.CHR_ID;
             					depValue["CHR_CODE"] = item2.CHR_CODE;
             					depValue["CHR_NAME"] = item2.CHR_NAME;
             					depValue["OID"] = item2.OID;
             					objectlist.push(depValue);
             				}
             				agencylist.push(item2);
             			}
             		});
             	});
             }else{
             	$.each(pageData.allModalData.Objectlist, function(index2, item2){
         				if(item2.TYPE == 'DEP'){
         					var  depValue = {};
         					depValue["CHR_ID"] = item2.CHR_ID;
         					depValue["CHR_CODE"] = item2.CHR_CODE;
         					depValue["CHR_NAME"] = item2.CHR_NAME;
         					depValue["OID"] = item2.OID;
         					objectlist.push(depValue);
         				}
         				//agencylist.push(item2);
         		});
             }
             var newPostData = {};
             $.each(postData, function(key, value){
             	key1 = key.toUpperCase();
             	newPostData[key1] = value;
             });
             postData = newPostData;
         	var indeces = viewModel.listGridViewModel.gridData.getSelectedIndices();
			var datas = viewModel.listGridViewModel.gridData.getSimpleData();
			var index = indeces[0];
            var focusData = datas[index];
            focusData["PLAN_COST"] = viewModel.data.totelManHour();
            focusData["REMARK"] = postData.REMARK;
            focusData["IS_ALLOBJ"] = postData.IS_ALLOBJ;
         	var mofList = JSON.stringify(focusData);
         	var objectList = JSON.stringify(objectlist);
         	var agencyList = JSON.stringify(agencylist);
         	var taskList = viewModel.workResoveGridViewModel.gridData.getSimpleData();
         	$.each(taskList, function(index, item){
         		var no = (index+1) < 9 ? "0" : "";
         		item["TASK_NO"] = focusData.bill_no;
         		item["DETAIL_NO"] = no + (index+1);
             });
         	taskList = JSON.stringify(taskList);
         	var task_workFlowMap = {
					"menu_id":menuId,
					"billtype_code":billtype_code,
					"op_type":"INPUT",
					"op_name":"牵头处办任务分解",
			};
         	var mof_workFlowMap = {
					"menu_id":menuId,
					"billtype_code":mof_billtype_code,
					"op_type":"NEXT",
					"op_name":"牵头处办任务分解",
			};
         	var data = {
         			"mofData":mofList,
         			"agencyList":agencyList,
         			"objectList":objectList,
         			"taskList":taskList,
         			"task_workFlowMap":JSON.stringify(task_workFlowMap),
         			"mof_workFlowMap":JSON.stringify(mof_workFlowMap),
         	}
         	$.post('/df/bill/saveTaskBill.do?tokenid='+ tokenid + '&ajax=' + 'noCache', data, function(map){
         		if(map.errorCode == '0'){
         			ip.ipInfoJump('分解成功！', 'success');
         			viewModel.initTree();
        			var radio =document.getElementsByName("mainStatus");
            		var submit_status = null;
                	for(var i =0;i < radio.length;i++){
        	        	 if(radio[i].checked){
        	        	     submit_status = radio[i].value;
        	        	 }
                	}
                	viewModel.gridRefresh(0,"",submit_status);
                	var mof_bill_id = focusData.ID || focusData.id;
                	var conditionMap = {};
                    conditionMap["mof_bill_id"] = mof_bill_id;
                	viewModel.gridRefresh(1,conditionMap);
         		}else{
         			ip.ipInfoJump('分解失败！', 'info');
         		}
         	});
        };
        
        //撤销分解
        undoResove = function(id){
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	var btn = getBtnMsgById(btnData,id);
        	var selectRow = viewModel.listGridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
        		var flag = 0;
        		for(var i=0;i<selectRow.length;i++){
        			if(selectRow[i].data.status_code.value == "1"){
        				var indeces = viewModel.listGridViewModel.gridData.getSelectedIndices();
        				var datas = viewModel.listGridViewModel.gridData.getSimpleData();
        				var index = indeces[0];
        	            var focusData = datas[index];
        	         	var mofList = JSON.stringify(focusData);
        	         	var id = selectRow[i].data.id.value;
        				var task_workFlowMap = {
        						"menu_id":menuId,
        						"billtype_code":billtype_code,
        						"op_type":"DEL",
        						"op_name":"牵头处办任务撤销分解",
        				};
        	         	var mof_workFlowMap = {
        						"menu_id":menuId,
        						"billtype_code":mof_billtype_code,
        						"entity_id":id,
        						"op_type":"UNDO",
        						"op_name":"牵头处办任务撤销分解",
        				};
        	         	var data = {
        	         			"mofData":mofList,
        	         			"task_workFlowMap":JSON.stringify(task_workFlowMap),
        	         			"mof_workFlowMap":JSON.stringify(mof_workFlowMap),
        	         	}
                		$.ajax({
                            type: 'post',
                            url: "/df/bill/deleteTaskBill.do?tokenid=" + tokenid + "&ajax=noCache",
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
        				ip.ipInfoJump("第"+ i+1 + "条数据未分解，不可撤销分解", 'info');
        			}
        		}
        		if(flag == 0){
                	ip.ipInfoJump(btn.DISPLAY_TITLE+'成功！', 'success');
        		}
    			viewModel.initTree();
    			var radio =document.getElementsByName("mainStatus");
        		var submit_status = null;
            	for(var i =0;i < radio.length;i++){
    	        	 if(radio[i].checked){
    	        	     submit_status = radio[i].value;
    	        	 }
            	}
            	viewModel.gridRefresh(0,"","",submit_status);
            	viewModel.detailGridViewModel.gridData.clear();
        	}else {
        		ip.ipInfoJump("请勾选需要操作的事项内容", 'info');
        	}
        };
        
      //获取options
        getOptions = function() {
        	options = ip.getCommonOptions({});
            options['tokenid'] = tokenid;
            options['ajax'] = "noCache";
        };
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
            mof_billtype_code = url.split("mof_billTypeCode=")[1].split("&")[0];
            //初始化按钮
            var data = initBtns(menuId);
            if(data == false){
                ip.ipInfoJump("加载按钮出错", 'error');
            }else{
                viewModel.btnDataTable.setSimpleData(data);
            }
            pageData.getModalTree();
            viewModel.initTree();
            viewModel.initData();
            viewModel.initStatus();
            csscsof.rightTwoHeight();
        };
        init();
    }
);