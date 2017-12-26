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
            clearPage: function(){//清除所有的遗留数据
                viewModel.listGridViewModel.gridData.clear();
                viewModel.dataTableGrid1.clear();
                viewModel.dataTableGrid2.clear();
                viewModel.dataTableGrid3.clear();
            }
        };

        var viewModel = {
            data: {//监控页面数据
                eventsNum: ko.observable('共3家'),
                radio: ko.observable('0')
            },
            btnEvents: function(data, events){//所有单击事件
                var $this = $(events.target),
                    name = $this[0].name;
                if(name == 'btn1'){//上一步
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
                    var flag = 0 ;//判断是否存在未填，若存在，则flag加1
                    if(num == 0){
                        viewModel.getViewData(4);
                        var sartTime = $('#START_DATE'  + '-' + dealData.getIdLast(viewModel.viewList4.viewid)).val();
                        var endTime = $('#END_DATE'  + '-' + dealData.getIdLast(viewModel.viewList4.viewid)).val();

                        var d1 = new Date(sartTime.replace(/\-/g, "\/"));
                        var d2 = new Date(endTime.replace(/\-/g, "\/"));


                        if(sartTime != "" && endTime != "" && d1 >= d2){
                            ip.ipInfoJump("开始日期不能大于结束日期", 'error');
                            flag++;
                        }
                        var aim2 = viewModel.vailData;
                        var noVerifyRid =["REMARK","SUP_NO","UNION_ORGS"];
                        if(!verifyInputView(aim2,noVerifyRid)){
                            flag++;
                        }
                    }
                    if(num == 1){
                        var data = viewModel.dataTable7.getSimpleData();
                        $('#btn2').hide();
                        $('#btn3').show();
                    }
                    if(flag > 0){
                        return;
                    }else{
                        num++;
                        $('#btn1').show();
                        eventsDeal.addStep();
                        eventsDeal.modalTab();
                    }
                }
                if(name == 'btn3'){//完成
                    var listData1 = viewModel.dataTableGrid1.getSimpleData();
                    var listData = [];
                    if(listData1){
                    	 $.each(listData1, function(index, item){
                             var obj = {};
                             obj.BILLTYPE_CODE = '101';
                             obj.SID = postData.CHR_ID;
                             obj.REPORT_ID = item.CHR_ID;
                             obj.DISPLAY_TITLE = item.CHR_NAME;
                             obj.DISPLAY_ORDER = index + 1;
                             listData.push(obj);

                         });
                    }
                    var listData2 = viewModel.dataTableGrid2.getSimpleData();
                    if(listData2){
                        $.each(listData2, function(index, item){
                            var obj = {};
                            obj.BILLTYPE_CODE = '102';
                            obj.SID = postData.CHR_ID;
                            obj.REPORT_ID = item.CHR_ID;
                            obj.DISPLAY_TITLE = item.CHR_NAME;
                            obj.DISPLAY_ORDER = index + 1;
                            listData.push(obj);

                        });
                    }
                    var listData3 = viewModel.dataTableGrid3.getSimpleData();
                    if(listData3){
                        $.each(listData3, function(index, item){
                            var obj = {};
                            obj.BILLTYPE_CODE = '103';
                            obj.SID = postData.CHR_ID;
                            obj.REPORT_ID = item.CHR_ID;
                            obj.DISPLAY_TITLE = item.CHR_NAME;
                            obj.DISPLAY_ORDER = index + 1;
                            listData.push(obj);

                        });
                    }
                    viewModel.getViewData(4);
                    if(postAction == "update"){
                        var url = '/df/sup/updateSup.do';
                        var successMsg ='修改成功';
                    }else if(postAction == "input"){
                        var url = '/df/sup/saveSup.do';
                        var successMsg ='保存成功';
                    }
                    var data = {
                        "eSuplist":JSON.stringify(postData),
                        "billtype_code":billTypeCode,
                        "EReportlist":JSON.stringify(listData),
                        "id":"CHR_ID"
                    };
                    $.post(url + '?tokenid='+ tokenid + '&ajax=' + 'noCache', data, function(map){//数据存进后台
                        if(map.errorCode == '0'){
                            ip.ipInfoJump(successMsg, 'success');
                            dealData.clearPage();
                            viewModel.initTree();
                            viewModel.gridRefresh();
                        }else{
                            ip.ipInfoJump(map.errorMsg, 'error');
                        }
                        //重新加载树
                    });
                    num = 0;
                    eventsDeal.modalTab();
                    eventsDeal.reduceStep();
                    $('#addModal').modal('toggle');
                    $('#btn2').show();
                    $('#btn4').show();
                }
                if(name == 'btn4'){//返回

                }
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
                        pageData.tree1Node = node;
                        treeState.isLastNode = node.isLastNode;
                        treeState.isParent = node.isParent;
                        var listId = node.chr_id;//字段id不确定,根据后台传来的字段确定
                        var listCode = node.type_code;
                        treeState.treeId = listId;
                        treeState.treeCode = listCode;
                        if(node.name == '全部'){
                            viewModel.gridRefresh("","","");
                        }else{
                            viewModel.gridRefresh(listId,listCode,"");
                        }
                        pageData.listData = viewModel.listGridViewModel.gridData.getSimpleData();
                        viewModel.dataTableGrid1.clear();
                        viewModel.dataTableGrid2.clear();
                        viewModel.dataTableGrid3.clear();
                    }
                }
            },
            treeSetting5:{//左树
                view:{
                    showLine:false,
                    selectedMulti:false
                },
                check : {//设置checkbox
                    enable: true,
                    autoCheckTrigger: true
                },
                callback:{
                    onClick:function(e,id,node){
                    }

                }

            },
            dataTable5: new u.DataTable({
                meta: {
                    'CHR_ID': {},
                    'PARENT_ID': {},
                    'CHR_NAME':{}
                }
            }),
            treeSetting7:{//左树
                view:{
                    showLine:false,
                    selectedMulti:false
                },
                callback:{
                    onClick:function(e,id,node){
                    }

                }

            },
            dataTable7: new u.DataTable({
                meta: {
                    'CHR_ID': {},
                    'PARENT_ID': {},
                    'CHR_NAME':{}
                }
            }),
            dataTableGrid1 : new u.DataTable({
                meta: {
                    'REPORT': {},
                    'CHR_NAME': {},
                    'REMAKE': {}
                }
            }),
            dataTableGrid2 : new u.DataTable({
                meta: {
                    'REPORT': {},
                    'CHR_NAME': {},
                    'REMAKE': {}
                }
            }),
            dataTableGrid3 : new u.DataTable({
                meta: {
                    'REPORT': {},
                    'CHR_NAME': {},
                    'REMAKE': {}
                }
            }),
            getViewData: function(number){//获取视图数据
                var arr = viewModel.viewList[number].viewDetail;
                $.each(arr,function(key, item){
                    if(item.disp_mode == 'treeassist'){
                        postData[item.id] = $('#' + item.id + '-' + dealData.getIdLast(item.viewid) + '-h').val();
                    }else{
                        postData[item.id] = $('#' + item.id + '-' + dealData.getIdLast(item.viewid)).val();
                    }
                });
            },
        };

        var pageData = {//通过交互获取数据
            getModalTree: function(){//获取modal的树数据
                $.get('/ItemSupervision/selectObjectByDep.do?tokenid='+ tokenid + '&ajax=' + 'noCache',function(map){
                    var obj = {
                        "CHR_ID": "51",
                        "PARENT_ID": "root",
                        "CHR_NAME": options.svOfficeName
                    };
                    pageData.allModalData = map;
                    map.deplist.push(obj);
                    var eventNum = 0;
                    viewModel.dataTable7.setSimpleData(map.EReportlist);
                });
            },
            getUUID: function(){
                $.post('/df/task/getUUID.do?tokenid='+ tokenid + '&ajax=' + 'noCache',function(map){
                    pageData.SID = map;
                    postData.CHR_ID = map;
                    publicFileman();
                });
            }
        };

        var eventsDeal = {
            addStep: function () {//点击下一步切换step
                $('.u-step:eq(' +num+ ')').addClass('current').siblings().removeClass('current');
                $('.u-step:lt(' +num+ ')').addClass('done');
                $('.step-doing').text(num+1);
                $('.step-name').text($('.u-step:eq(' +num+ ') .u-step-title').text());
            },
            reduceStep: function () {//点击上一步切换step
                $('.u-step:gt(' +num+ ')').removeClass('done');
                $('.u-step:eq(' +num+ ')').removeClass('done');
                $('.u-step:eq(' +num+ ')').addClass('current').siblings().removeClass('current');
                $('.step-doing').text(num+1);
                $('.step-name').text($('.u-step:eq(' +num+ ') .u-step-title').text());

            },
            modalTab: function () {//点击上一步、下一步切换modal页签
                var arr = ['infoAdd','contentAdd', 'partAdd'];
                $('#' + arr[num]).addClass('active').siblings().removeClass('active');
            }
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
                    viewModel.initGridData(areaType,areaId); //调用初始化表格
                    viewModel.viewList4 = datas.viewlist[4];
                    viewModel.vailData = ip.initArea(viewModel.viewList[4].viewDetail,areaType,dealData.getIdLast(viewModel.viewList[4].viewid), areaId);
                }
            });
        };

        viewModel.initGridData = function(areaType,areaId) {
            var queryViewId;
            var tableViewDetail;
            var queryViewDetail;
            viewModel.listGridViewModel = ip.initGridWithCallBack(gridCallback,viewModel.viewList[2],'grid1', viewModel.viewList[2].remark + '?', options, 1, false,true,false,false);
            if(viewModel.listGridViewModel.gridData.getSimpleData()){
                $("#sumGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);
            }
//            var allRows = viewModel.listGridViewModel.gridData.getAllRows();
//            for(var i=0;i<allRows.length;i++){
//            	var row = viewModel.listGridViewModel.gridData.getRowByRowId(allRows[i].rowId);
//                if(allRows[i].data.status_code == "10" || allRows[i].data.STATUS_CODE == "10"){
//                	row.setValue('STATUS_NAME','未生成') ;
//                }else{
//                	row.setValue('STATUS_NAME','已生成') ;
//                }
//            }
            viewModel.changeBtnStatusBysetGrid(viewModel.listGridViewModel);
//	            viewModel.detailGridViewModel = ip.initGrid(viewModel.viewList[3],'grid2','/df/task/getTaskDep.do?', options, 1,false, false, false, false);
//	            if(viewModel.detailGridViewModel.gridData.getSimpleData()){
//	                $("#detailGridSumNum").html(viewModel.detailGridViewModel.gridData.getSimpleData().length);
//	            }
        };

        //grid查询
        viewModel.gridSearch = function () {
//	        	$.each(viewModel.curGridData[0], function(index, item){
//	    				$.each(item, function(index, item){
//		    				item = item.toUpperCase();
//		    		});
//	    		});
            $.each(viewModel.curGridData.dataDetail, function(index, item){
                $.each(item, function(index2, item2){
                    index2 = index2.toUpperCase();
                });
            });
            ip.fuzzyQuery(viewModel.curGridData, "gridSearchInput", viewModel.listGridViewModel)
        };

        //回调函数，定义全局变量viewModel.curGridData并赋值
        gridCallback = function(data){
            viewModel.curGridData = data;
        };

        //刷新Grid表格数据
        viewModel.gridRefresh = function(chr_id,type_code,status) {
        	var conditionMap = {};
        	conditionMap[type_code] = chr_id;
        	var data = {
        			"conditionMap" :JSON.stringify(conditionMap)
        	}
            ip.setGridWithCallBack(gridCallback,viewModel.listGridViewModel, '/df/sup/getSup.do?tokenid='+ tokenid + '&ajax=' + 'noCache', data);
            $("#sumGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);
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
        };
        
        //新增按钮
        addClick = function(id){//新增
            //$('#addModal').modal('show');
            $('#addModal').modal({
                show : true,
                backdrop : 'static'
            });
            $('#infoAdd').html('');
            //如果存在对于主树的点击，那么将类型填入新增页面
            ip.createArea('edit', viewModel.viewList4.viewid, 'infoAdd');
            pageData.getUUID();
            if(postAction != "input"){
                num = 0;
                eventsDeal.modalTab();
                eventsDeal.reduceStep();
                $('#btn2').show();
                $('#btn4').show();
            }
            postAction = "input";
            if(pageData.tree1Node){
                if(pageData.tree1Node.PARENT_ID == 'type'){
                    $('#SUP_TYPE_NAME'  + '-' + dealData.getIdLast(viewModel.viewList4.viewid)).val(pageData.tree1Node.chr_name);
                    $('#SUP_TYPE_NAME' + '-' + dealData.getIdLast(viewModel.viewList4.viewid) + "-h").val(pageData.tree1Node.chr_id + "@" + encodeURI(pageData.tree1Node.chr_name, "utf-8") + "@" + pageData.tree1Node.chr_code + "@" + pageData.tree1Node.PARENT_ID);
                }
            }
            //publicFileman();
            //initData("edit", "infoAdd");
            //重置modal页面的一些状态
            viewModel.dataTableGrid1.clear();
            viewModel.dataTableGrid2.clear();
            viewModel.dataTableGrid3.clear();
            viewModel.data.radio('0');
            var eventNum = 0;
            var arr = [];
            //postData.SID = '';
            //postData.SID = pageData.SID;
            if(num == 0){
                $('#btn1').hide();
                $('#btn3').hide();
            }else{
                if(num == 2){
                    $('#btn2').hide();
                    $('#btn3').show();
                }else{
                    $('#btn2').show();
                    $('#btn3').hide();
                }
            }
        };

        //修改
        publicModify = function() {
            var indeces = viewModel.listGridViewModel.gridData.getSelectedIndices();
            var datas = viewModel.listGridViewModel.gridData.getSimpleData();
            if(indeces.length == 1){
                if(postAction != "update"){
                    num = 0;
                    eventsDeal.modalTab();
                    eventsDeal.reduceStep();
                    $('#btn2').show();
                    $('#btn4').show();
                }
                postAction = "update";
                var index = indeces[0];
                $('#infoAdd').html('');
                var focusData = datas[index];
                postData.CHR_ID = focusData.chr_id;
                pageData.SID = focusData.chr_id;
                if(num == 0){
                    $('#btn1').hide();
                    $('#btn3').hide();
                }else{
                    if(num == 3){
                        $('#btn2').hide();
                        $('#btn3').show();
                    }else{
                        $('#btn2').show();
                        $('#btn3').hide();
                    }
                }
                ip.createArea('edit', viewModel.viewList4.viewid, 'infoAdd');
                publicFileman();
                var arr = viewModel.viewList4.viewDetail;
                var conditionMap = {};
            	conditionMap["CHR_ID"] = postData.CHR_ID;
            	var data = {
            			"conditionMap" :JSON.stringify(conditionMap)
            	}
                $.get( '/df/sup/getSup.do?tokenid='+ tokenid + '&ajax=' + 'noCache', data, function(map){
                    pageData.treeDataUpdate = map.dataDetail[0];
                    $.each(arr,function(key, item){//这里修改保存的是       pageData.listData[0]，将此变量里修改的值保存即可
                        $.each(pageData.treeDataUpdate, function(key2, value2){
                            if(item.id == key2.toUpperCase()){
                                $('#' + item.id + '-' + dealData.getIdLast(item.viewid)).val(value2);
                                if(item.disp_mode == "treeassist"){
                                    var all_options = {
                                        "element": item.source,
                                        "tokenid": tokenid,
                                        "ele_value": "",
                                        "ajax": "noCache",
                                        "condition": ""
                                    };
                                    $.ajax({
                                        url: "/df/dic/dictree.do",
                                        type: "GET",
                                        async: false,
                                        data: ip.getCommonOptions(all_options),
                                        success: function(map) {
                                            for(var j=0;j<map.eleDetail.length;j++){
                                                if(map.eleDetail[j].chr_name == value2){
                                                    $('#' + item.id + '-' + dealData.getIdLast(item.viewid)+"-h").val(map.eleDetail[j].chr_id + "@" + encodeURI(map.eleDetail[j].chr_name, "utf-8") + "@" + map.eleDetail[j].chr_code + "@" + map.eleDetail[j].pId);
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    });
                });

                $.post('/df/task/getSupReport.do?tokenid='+ tokenid + '&ajax=' + 'noCache', {'CHR_ID': postData.CHR_ID,'TYPE_CODE':'SID'}, function(map){//获取agencyList数据
                    if(map.errorCode == '0'){
                        var list1 = [];
                        var list2 = [];
                        var list3 = [];
                        $.each(map.dataDetail, function(index, item){
                            if(item.billtype_code == '101'){
                                var newItem = {};
                                newItem.REPORT = '';
                                $.each(item, function(key, value){
                                    if(key == 'billtype_code'){
                                        newItem.REPORT += value;
                                    }
                                    if(key == 'display_title'){
                                        newItem.REPORT += value;
                                        newItem.CHR_NAME = value;
                                    }
                                    if(key == 'report_id'){
                                        newItem.CHR_ID = value;
                                    }
                                });
                                list1.push(newItem);
                            }
                            if(item.billtype_code == '102'){
                                var newItem = {};
                                newItem.REPORT = '';
                                $.each(item, function(key, value){
                                    if(key == 'billtype_code'){
                                        newItem.REPORT += value;
                                    }
                                    if(key == 'display_title'){
                                        newItem.REPORT += value;
                                        newItem.CHR_NAME = value;
                                    }
                                    if(key == 'report_id'){
                                        newItem.CHR_ID = value;
                                    }
                                });
                                list2.push(newItem);
                            }
                            if(item.billtype_code == '103'){
                                var newItem = {};
                                newItem.REPORT = '';
                                $.each(item, function(key, value){
                                    if(key == 'billtype_code'){
                                        newItem.REPORT += value;
                                    }
                                    if(key == 'display_title'){
                                        newItem.REPORT += value;
                                        newItem.CHR_NAME = value;
                                    }
                                    if(key == 'report_id'){
                                        newItem.CHR_ID = value;
                                    }
                                });
                                list3.push(newItem);
                            }
                        });
                        viewModel.dataTableGrid1.setSimpleData(list1);
                        viewModel.dataTableGrid2.setSimpleData(list2);
                        viewModel.dataTableGrid3.setSimpleData(list3);
                    }
                });
                $('#addModal').modal({
                    show : true,
                    backdrop : 'static'
                });
            }else{
                ip.ipInfoJump('请选择一个要修改的事项,勿多选或不选！', 'error');
            }
        };

        //删除
        publicDelete = function() {
            var indeces = viewModel.listGridViewModel.gridData.getSelectedIndices();
            var datas = viewModel.listGridViewModel.gridData.getSimpleData();
            if(indeces.length > 0){
            	var flag = 0;
            	for(var d=0;d<indeces.length;d++){
            		if(datas[indeces[0]].status_code != "10"){
            			flag++;
            			break;
            		}
            	}
            	if(flag == 0){
            		ip.warnJumpMsg('是否删除所选择事项',"delConfirmSureId","delConfirmCancelCla");
                    $('#delConfirmSureId').on('click',function(){
                        $('#config-modal').remove();
                        var delIds = datas[indeces[0]].chr_id;
                        for(var i = 1 ; i<indeces.length ; i++){
                            var index = indeces[i];
                            var focusData = datas[index];
                            var delId = focusData.chr_id;
                            delIds = delIds + "," + delId ;
                        }
                        $.post('/df/sup/deleteSup.do?tokenid='+ tokenid + '&ajax=' + 'noCache', {'ids':delIds}, function(map){//删除事项
                            if(map.errorCode == '0'){
                                viewModel.gridRefresh("","","");
                                viewModel.initTree();
                            }

                        });
                    });
                    $('.delConfirmCancelCla').on('click',function(){
                        $('#config-modal').remove();
                    });
            	}else{
                    ip.ipInfoJump('未生成下达单的数据才可删除！','info');
            	}
            }else{
                ip.ipInfoJump('请选择你要删除的事项！','info');
            }
        };
        
        //作废 
        taskDel = function(){
        	var indeces = viewModel.listGridViewModel.gridData.getSelectedIndices();
            var datas = viewModel.listGridViewModel.gridData.getSimpleData();
            if(indeces.length > 0){
            	var flag = 0;
            	for(var d=0;d<indeces.length;d++){
            		if(datas[indeces[0]].status_code != "11"){
            			flag++;
            			break;
            		}
            	}
            	if(flag == 0){
            		ip.warnJumpMsg('是否作废所选择事项',"taskDelConfirmSureId","taskDelConfirmCancelCla");
                    $('#taskDelConfirmSureId').on('click',function(){
                        $('#config-modal').remove();
                        var delIds = datas[indeces[0]].chr_id;
                        for(var i = 1 ; i<indeces.length ; i++){
                            var index = indeces[i];
                            var focusData = datas[index];
                            var delId = focusData.chr_id;
                            delIds = delIds + "," + delId ;
                        }
                        var data = {
                      		   "ids":delIds
                      	};
                        $.post('/df/sup/validSup.do?tokenid='+ tokenid + '&ajax=' + 'noCache', data, function(map){//作废事项
                            if(map.errorCode == '0'){
                                viewModel.gridRefresh("","","");
                                viewModel.initTree();
                            }
                        });
                    });
                    $('.taskDelConfirmCancelCla').on('click',function(){
                        $('#config-modal').remove();
                    });
            	}else{
                    ip.ipInfoJump('已生成下达单的数据才可作废！','info');
            	}
            }else{
                ip.ipInfoJump('请选择你要作废的事项！','info');
            }
        };


        var publicFileman = function(obj) {
            $("#filePage")[0].src = "";
            var fileData = {
                "entity_id":pageData.SID,
                "oid":options.svOfficeId,
                "dep_id":options.svDepId,
                "dep_code":options.svDepCode,
                "edit":'Y',
                "modular":'TASK',
            };
            //if(pageData.SID)
            $("#filePage")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
                "&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&entityName=csof_sup_bill&oid="+fileData.oid+
                "&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit+"&modular="+fileData.modular;
        };

        $('#reportAddSure').click(function(){//报表保存
            var nodes = $("#tree5")[0]['u-meta'].tree.getCheckedNodes(true);
            $.each(nodes, function(index, item){
                item.REPORT = item.CHR_CODE + '' + item.CHR_NAME;
            });
            if(pageData.reportFlag == '1'){
                viewModel.dataTableGrid1.setSimpleData(nodes);
            }

            if(pageData.reportFlag == '2'){
                viewModel.dataTableGrid2.setSimpleData(nodes);
            }

            if(pageData.reportFlag == '3'){
                viewModel.dataTableGrid3.setSimpleData(nodes);
            }
            $('#reportModel').modal('toggle');
        });
        //监管明细
        $('#addReport1').click(function(){//新增报表
            $('#reportModel').modal('show');
            pageData.reportFlag = '1';
            var reportCycle = $('#SUP_CYCLE-' + dealData.getIdLast(viewModel.viewList4.viewDetail[0].viewid)).val();
            if(reportCycle != ''){
                var arr = [];
                $.each(pageData.allModalData.EReportlist, function(index, item){
                    if(item.REPORT_CYCLE == reportCycle){
                        arr.push(item);
                    }
                });
                viewModel.dataTable5.setSimpleData(arr);
            }else{
                ip.ipInfoJump('您需要选择周期！', 'info');
                viewModel.dataTable5.setSimpleData(pageData.allModalData.EReportlist);
            }

            var data = viewModel.dataTableGrid1.getSimpleData();
            var nodes = $("#tree5")[0]['u-meta'].tree.getNodes();
            $.each(nodes, function(index, item){
                $.each(data, function(index2, item2){
                    if(item.CHR_ID == item2.CHR_ID){
                        $("#tree5")[0]['u-meta'].tree.checkNode(item, true, false);
                        //$("#tree5")[0]['u-meta'].tree.checkAllNodes(true);
                    }
                });
            });
        });
        $('#deleteReport1').click(function(){
            var rowsIndex = viewModel.dataTableGrid1.getSelectedIndex();
            var data = viewModel.dataTableGrid1.getSimpleData();
            data.splice(rowsIndex,1);
            viewModel.dataTableGrid1.setSimpleData(data);
        });
        $('#upReport1').click(function(){
            var rowsIndex = viewModel.dataTableGrid1.getSelectedIndex();
            var data = viewModel.dataTableGrid1.getSimpleData();
            if(rowsIndex == '0'){
                ip.ipInfoJump('您的选择无法上移！', 'info');
            }else{
                var value = data[rowsIndex - 1];
                data[rowsIndex - 1] = data[rowsIndex];
                data[rowsIndex] = value;
                viewModel.dataTableGrid1.setSimpleData(data);
                viewModel.dataTableGrid1.setRowSelect(rowsIndex - 1);
            }

        });
        $('#downReport1').click(function(){
            var rowsIndex = viewModel.dataTableGrid1.getSelectedIndex();
            var data = viewModel.dataTableGrid1.getSimpleData();
            if(rowsIndex >= (data.length -1)){
                ip.ipInfoJump('您的选择无法下移！', 'info');
            }else{
                var value = data[rowsIndex + 1];
                data[rowsIndex + 1] = data[rowsIndex];
                data[rowsIndex] = value;
                viewModel.dataTableGrid1.setSimpleData(data);
                viewModel.dataTableGrid1.setRowSelect(rowsIndex + 1);
            }

        });
        //司局汇总
        $('#addReport2').click(function(){//新增报表
            $('#reportModel').modal('show');
            pageData.reportFlag = '2';
            var reportCycle = $('#SUP_CYCLE-' + dealData.getIdLast(viewModel.viewList4.viewDetail[0].viewid)).val();
            if(reportCycle != ''){
                var arr = [];
                $.each(pageData.allModalData.EReportlist, function(index, item){
                    if(item.REPORT_CYCLE == reportCycle){
                        arr.push(item);
                    }
                });
                viewModel.dataTable5.setSimpleData(arr);
            }else{
                ip.ipInfoJump('您需要选择周期！', 'info');
                viewModel.dataTable5.setSimpleData(pageData.allModalData.EReportlist);
            }
            var data = viewModel.dataTableGrid2.getSimpleData();
            var nodes = $("#tree5")[0]['u-meta'].tree.getNodes();
            $.each(nodes, function(index, item){
                $.each(data, function(index2, item2){
                    if(item.CHR_ID == item2.CHR_ID){
                        $("#tree5")[0]['u-meta'].tree.checkNode(item, true, false);
                        //$("#tree5")[0]['u-meta'].tree.checkAllNodes(true);
                    }
                });
            });
        });
        $('#deleteReport2').click(function(){
            var rowsIndex = viewModel.dataTableGrid2.getSelectedIndex();
            var data = viewModel.dataTableGrid2.getSimpleData();
            data.splice(rowsIndex,1);
            viewModel.dataTableGrid2.setSimpleData(data);
        });
        $('#upReport2').click(function(){
            var rowsIndex = viewModel.dataTableGrid2.getSelectedIndex();
            var data = viewModel.dataTableGrid2.getSimpleData();
            if(rowsIndex == '0'){
                ip.ipInfoJump('您的选择无法上移！', 'info')
            }else{
                var value = data[rowsIndex - 1];
                data[rowsIndex - 1] = data[rowsIndex];
                data[rowsIndex] = value;
                viewModel.dataTableGrid2.setSimpleData(data);
                viewModel.dataTableGrid2.setRowSelect(rowsIndex - 1);
            }

        });
        $('#downReport2').click(function(){
            var rowsIndex = viewModel.dataTableGrid2.getSelectedIndex();
            var data = viewModel.dataTableGrid2.getSimpleData();
            if(rowsIndex >= (data.length - 1)){
                ip.ipInfoJump('您的选择无法下移！', 'info')
            }else{
                var value = data[rowsIndex + 1];
                data[rowsIndex + 1] = data[rowsIndex];
                data[rowsIndex] = value;
                viewModel.dataTableGrid2.setSimpleData(data);
                viewModel.dataTableGrid2.setRowSelect(rowsIndex + 1);
            }

        });
        //办汇总
        $('#addReport3').click(function(){//新增报表
            $('#reportModel').modal('show');
            pageData.reportFlag = '3';
            var reportCycle = $('#SUP_CYCLE-' + dealData.getIdLast(viewModel.viewList4.viewDetail[0].viewid)).val();
            if(reportCycle != ''){
                var arr = [];
                $.each(pageData.allModalData.EReportlist, function(index, item){
                    if(item.REPORT_CYCLE == reportCycle){
                        arr.push(item);
                    }
                });
                viewModel.dataTable5.setSimpleData(arr);
            }else{
                ip.ipInfoJump('您需要选择周期！', 'info');
                viewModel.dataTable5.setSimpleData(pageData.allModalData.EReportlist);
            }
            var data = viewModel.dataTableGrid3.getSimpleData();
            var nodes = $("#tree5")[0]['u-meta'].tree.getNodes();
            $.each(nodes, function(index, item){
                $.each(data, function(index2, item2){
                    if(item.CHR_ID == item2.CHR_ID){
                        $("#tree5")[0]['u-meta'].tree.checkNode(item, true, false);
                        //$("#tree5")[0]['u-meta'].tree.checkAllNodes(true);
                    }
                });
            });
        });
        $('#deleteReport3').click(function(){
            var rowsIndex = viewModel.dataTableGrid3.getSelectedIndex();
            var data = viewModel.dataTableGrid3.getSimpleData();
            data.splice(rowsIndex,1);
            viewModel.dataTableGrid3.setSimpleData(data);
        });
        $('#upReport1').click(function(){
            var rowsIndex = viewModel.dataTableGrid3.getSelectedIndex();
            var data = viewModel.dataTableGrid3.getSimpleData();
            if(rowsIndex == '0'){
                ip.ipInfoJump('您的选择无法上移！', 'info');
            }else{
                var value = data[rowsIndex - 1];
                data[rowsIndex - 1] = data[rowsIndex];
                data[rowsIndex] = value;
                viewModel.dataTableGrid3.setSimpleData(data);
                viewModel.dataTableGrid3.setRowSelect(rowsIndex - 1);
            }

        });
        $('#downReport3').click(function(){
            var rowsIndex = viewModel.dataTableGrid3.getSelectedIndex();
            var data = viewModel.dataTableGrid3.getSimpleData();
            if(rowsIndex >= (data.length - 1)){
                ip.ipInfoJump('您的选择无法下移！', 'info');
            }else{
                var value = data[rowsIndex + 1];
                data[rowsIndex + 1] = data[rowsIndex];
                data[rowsIndex] = value;
                viewModel.dataTableGrid3.setSimpleData(data);
                viewModel.dataTableGrid3.setRowSelect(rowsIndex + 1);
            }
        });
        
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
            elecode = url.split("elecode=")[1].split("&")[0];
            menuId = url.split("menuid=")[1].split("&")[0];
            menuName = decodeURI(url.split("menuname=")[1].split("&")[0]);
            billTypeCode =  url.split("billTypeCode=")[1].split("&")[0];
            //初始化按钮
            var data = initBtns(menuId);
            if(data == false){
                ip.ipInfoJump("加载按钮出错", 'error');
            }else{
                viewModel.btnDataTable.setSimpleData(data);
            }
            //初始化树
            viewModel.initTree();
            //初始化模态框树
            pageData.getModalTree();
            pageData.eleCode = 'CSOF_SUPTYPE';
            pageData.eleCode2 = 'CSOF_SUP';
            pageData.status = '0';
            //初始化表格
            viewModel.initData("edit","testGrid", 4);
            csscsof.windowHeightNoNav();
        };
        init();
    }
);