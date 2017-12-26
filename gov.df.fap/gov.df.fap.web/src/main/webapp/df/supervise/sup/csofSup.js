/**
 * Created by yanqiong on 2017/8/8.
 */
require(['jquery', 'knockout','/df/supervise/ncrd.js','../fileUpload/js/webuploader.js','csscsof','bootstrap','dateZH', 'uui', 'tree', 'grid', 'ip' ,'csof'],
    function ($, ko,ncrd, WebUploader,csscsof) {
        window.ko = ko;
        var tokenid;
        var options;
        var eleCode;
        var entity_id;
        var pageId;
        var sid;
        var mof_bill_id;
        var bbq_date;
        var task_id;
        var task_name;
        var IS_ALLOBJ;
        var OBJ_TYPE_ID;
        var winHeight;
        var winWidth;
		var pViewType = {
        		VIEWTYPE_INPUT : "001",// 录入视图
        		VIEWTYPE_LIST : "002",// 列表视图
        		VIEWTYPE_QUERY : "003"// 查询视图
        };
		var fileData;
		var num=0;
		
		var viewData = {};//视图的数据
        var postData = {};//通过post传到后台的数据
        var treeState = {};//点击主树节点的状态
        var postAction,IMPROWID;//确认导入的数据信息的行数rowID
        var nowBtnId;
        
        var pageData = {//通过交互获取数据
                getUUID: function(){
                    /*$.ajax({
    		            type: 'post',
    		            url: "/df/task/getUUID.do?tokenid=" + tokenid + "&ajax=noCache",
    		            data: {},
    		            dataType: 'text',
    		            success: function (map) {
                            pageData.ID = map;
                            publicFileman("execlFile");
                            return map;
    		            }	                
    	            });*/
                    $.post('/df/task/getUUID.do?tokenid='+ tokenid + '&ajax=' + 'noCache',function(map){
                    	pageData.ID = map;
                    	if(postAction == "dataInput"){
                            newExcelShow(pageData.ID);
                    	}else if(postAction == "batchImp"){
                    		publicFileman("execlFile");
                    	}
                        
                	});
                },
                //加载初始化页签
                ininReportNav : function(){
                	$.ajax({
        		        type: 'post',
        		        url: getNavURL + "?tokenid=" + tokenid + "&ajax=noCache",
        		        data: {"billtypeCode":billTypeCode,"sid":sid},//die
        		        dataType: 'JSON',
        		        async: false,
        		        success: function (result) {
        		            if (result.errorCode == 0) {
        		            	var data = result.data;
        		            	pageData.EReportNavData = data;
        		            	pageData.execlReportData = [];
        						for(var i=0;i<data.length;i++) {
        							if(data[i].REPORT_TYPE == 3 || data[i].REPORT_TYPE == '3'){
                		            	pageData.execlReportData.push(data[i]); 
        							}
        						}
        		            } else {
        		                ip.ipInfoJump("错误", 'error');
        		            }
        		        }, error: function () {
        		            ip.ipInfoJump("错误", 'error');
        		        }
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
                    var arr = ['viewReport','filePage','confirmImp', 'finishImp'];
                    $('#' + arr[num]).addClass('active').siblings().removeClass('active');
                }
            };

        
        //URL
        var testURL = '/df/cs/test.do';
        var viewListURL= '/df/cs/selectAll.do';
        var doPayWorkFlowURL= '/df/workflow/work.do';
        var depTreeURL ='/df/tree/initTree.do';
        var deleteListURL ='/df/cs/update.do';
        var getBillNoURL = '/df/cs/inputbill.do';//获取单号
        var saveBillURL = '/df/cs/savebill.do';
        var getNavURL = '/ReportForm/selectSupReportBySidBillcode.do';//获取详情的列表页签
        var getReportURL = '/ReportForm/selectEReportByChrId.do'//获取报表
        var getFileNameURL = '/df/cs/getfileName.do';//获取文件名
        var ifExistBillURL = '/df/cs/ifExistBill.do';//判断是否录入内容是否存在
        var viewModel = {
        		data: {//监控页面数据
        			hasOpNum: ko.observable('0'),
                    radio: ko.observable('0')
                },

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
            btnDataTable:new u.DataTable({
                meta: {

                }
            }),
            impFileDataTable:new u.DataTable({
                meta: {

                }
            }),
            confirmImpGridDataTable:new u.DataTable({
                meta: {

                }
            }),
            impTreeDataTable : new u.DataTable({
                meta: {
                }
            }),
            impTreeSetting:{
            	view:{
                    showLine: false,
                    selectedMulti: false,
                    showIcon: false,
                    showTitle: true
                },
                callback:{
                    onClick:function(e,id,node){
                      
                    }
                }
            },
            depTreeSetting: {
                view:{
                    showLine: false,
                    selectedMulti: false,
                    showIcon: false,
                    showTitle: true
                },
                callback:{
                    onClick:function(e,id,node){
                        var selectNodeId = node.CHR_ID;
                        var type = node.TYPE;
               		    var code = node.CHR_CODE;
               		    var oid = node.OID;
                        var radio =document.getElementsByName("submitStatus");
                 	    var submitStatus = null;
                 	    for(var i =0;i < radio.length;i++){
        	 	        	 if(radio[i].checked){
        	 	        	     submitStatus = radio[i].value;
        	 	        	 }
                 	    }
                        if(selectNodeId == "000") {
                        	viewModel.refresh("","","","",submitStatus);
                        }else {
                        	viewModel.refresh(selectNodeId,type,code,oid,submitStatus);
                        }
//                        $("#details-li").removeClass("active");
//                		$("#list-li").addClass("active");
//                		$("#panel-list").addClass("active");
//                		$("#panel-details").removeClass("active");
//                		$("#panel-details").hide();
                		var btnData = viewModel.btnDataTable.getSimpleData();
                		for(var i=0;i<btnData.length;i++){
                    		var btnId = btnData[i].BUTTON_ID;
                    		if(btnData[i].ENABLED == "0" || btnData[i].DISPLAY_STATUS == "2") {
	            				$("#"+btnId).attr("disabled","disabled");
	            			}else{
	            				$("#"+btnId).removeAttr("disabled","disabled");
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
                    'DEP_ID':{
                        'value':""
                    }, 
                    'MOFDEP_ID':{
                        'value':""
                    },
                }
            }),
            //确认导入的数据信息表
            confirmImpGridDataTable : new u.DataTable({
                meta: {
                    'file_name': {},
                    'dep': {},
                    'status': {}
                }
            }),
            btnEvents: function(data, events){//所有单击事件
                var $this = $(events.target),
                    name = $this[0].name;
                if(name == 'btn1'){//上一步
                    num--;
                    if(num == 0){
                        $('#btn1').hide();
                    }
                    if(num < 3){
                        $('#btn2').show();
                        $('#btn3').hide();
                        $('#impbtn').hide();
                    }
                    eventsDeal.reduceStep();
                    eventsDeal.modalTab();
                }
                if(name == 'btn2'){//下一步
                	if(num == 0){
                        var rediolist= $('input:radio[name="view"]:checked').val();
                        if(rediolist == null){
                            alert("请选中一个需要导入的表单!");
                            return false;
                        }
                	}
                    if(num == 1){
                    	$("#bbq_date").val(bbq_date);
                    	$("#task_id").val(task_id);
                    	$("#task_name").val(task_name);
                    	viewModel.getfileName(pageData.ID);
                    }
                    if(num == 2){
                    	var confirmImpGridData = viewModel.confirmImpGridDataTable.getSimpleData();
                    	var impData = [];
                    	for(var i=0;i<confirmImpGridData.length;i++){
                    		if(confirmImpGridData[i].statusCode == 1){
                        		impData.push(confirmImpGridData[i]);
                    		}
                    	}
                    	postData.impData = impData;
                    	$("#totleOpNum").html(impData.length);
                    	if(impData.length > 0){
                    		$("#log").append("-----导入日志-----");
                    	}else{
                    		$("#log").append("-----没有可导入的文件-----");
                    	}
                        $('#btn2').hide();
                        $('#btn3').show();
                        $('#impbtn').show();
                    }
                    num++;
                    $('#btn1').show();
                    eventsDeal.addStep();
                    eventsDeal.modalTab();
                }
                if(name == 'impbtn'){//开始执行
                	viewModel.data.hasOpNum(0);
                		if(postData.impData.length){
                        	for(var i = 0; i < postData.impData.length;i++){
                        		var attachId = postData.impData[i].file_id;
                        		var viewId = $('input:radio[name="view"]:checked').val();
                        		$("#op_dep_name").html(postData.impData[i].dep);
                        		$("#op_file_name").html(postData.impData[i].file_name);
                        		var map = {
                        			"attachId":attachId,
                        			"viewId":viewId,
                        		};
                        		$.ajax({
	            		            type: 'post',
	            		            url: "/df/task/getUUID.do?tokenid=" + tokenid + "&ajax=noCache",
	            		            data: {},
	            		            dataType: 'text',
	            		            async: false,
	            		            success: function (map) {
	                                    pageData.ID = map;
	            		            }	                
            	                });
                        		var chr_code = postData.impData[i].dep.split(" ")[0];
                            	var treeData = viewModel.depTreeDataTable.getSimpleData();
                            	for(var t = 0;t<treeData.length;t++){
                            		if(treeData[t].CHR_CODE == chr_code){
                                    	var selectNodeId = treeData[t].CHR_ID;
                            			var selectNodeCode = treeData[t].CHR_CODE;
                            			var selectNodeName = treeData[t].CHR_NAME;
                            			var selectNodeOid = treeData[t].OID;
                            			var selectNodeDepID = treeData[t].DEP_ID;
                            			var selectNodeMofdepId = treeData[t].MOFDEP_ID;
                            		}
                            	}
                        		var data = {
                        				"mapParam":map,
                        				"billtypeCode":billTypeCode,
                        				"billId":pageData.ID,
                        				"objtypeId":OBJ_TYPE_ID,
                        				"objId":selectNodeId,
                        				"supCycle":"-1",
                        				"supDate":bbq_date,
                        		};
                            	$("#log").append("<br>-----导入"+postData.impData[i].file_name+"开始-----");
                        		$.ajax({
                                    type: 'POST',
                                    url: '/CsofCView/saveExcelData.do?tokenid='+ tokenid + '&ajax=noCache',
                                    contentType: 'application/json',
                                    dataType: 'json',
                                    data: JSON.stringify(data),
                                    async: false,
                                    success: function (map) {
                                        if(map.errorCode == 0){
                                        	$("#log").append("<br>-----导入"+postData.impData[i].file_name+"解析成功-----");
                                        	$("#log").append("<br>-----导入"+postData.impData[i].file_name+"内容保存开始-----");
                                        	saveBusinessDataInput(selectNodeId,selectNodeCode,selectNodeName,selectNodeDepID,selectNodeMofdepId,pageData.ID,"batchImpModal",postData.impData[i]);
                                        }else{
                                        	$("#log").append("<br>-----导入"+postData.impData[i].file_name+"失败-----");
                                        }
                                    }, error: function () {
                                    	$("#log").append("<br>-----导入"+postData.impData[i].file_name+"失败-----");
                    	            }
                                });
                        	}
                    	}
                }
                if(name == 'btn3'){//完成
                	 num = 0;
                     eventsDeal.modalTab();
                     eventsDeal.reduceStep();
                     $('#batchImpModal').modal('toggle');
                     $('#btn2').show();
                     $('#btn4').show();
                }
                if(name == 'btn4'){//返回
                	num = 0;
                    eventsDeal.modalTab();
                    eventsDeal.reduceStep();
                    $('#batchImpModal').modal('toggle');
                    $('#btn2').show();
                    $('#btn4').show();
               }
            },
          //定义所属对象操作列
          optFun: function(obj) {
              var dataTableRowId = obj.row.value['$_#_@_id'];
              var showTreeFun = "data-bind=click:showImpTree.bind($data," + dataTableRowId + ")";
              var clearTreeFun = "data-bind=click:clearDepImp.bind($data," + dataTableRowId + ")";
              obj.element.innerHTML = '<div style="margin-top: 2px;"><span class="input-group-addon-span"><span class="glyphicon glyphicon-remove" ' + clearTreeFun + '></span></span>'+
                 '<span class="input-group-addon-span"><span class="glyphicon glyphicon-list" ' + showTreeFun + '></span></span></div>';
              //              obj.element.innerHTML = '<div><i class="op uf uf-pencil" title="修改"' + editfun + '></i>' + '<i class=" op icon uf uf-del title="删除" ' + delfun + '></i></div>';
              ko.applyBindings(viewModel, obj.element);
          },
          showImpTree: function(rowId){
        	  $('#impTreeModel').modal({
              	show : true,
              	backdrop : 'static'
              });
        	  IMPROWID = rowId;
        	  viewModel.impTreeDataTable.clear();
        	  viewModel.impTreeDataTable.setSimpleData(viewModel.depTreeDataTable.getSimpleData());
        	  var treeObj = $.fn.zTree.getZTreeObj("impDepTree");
              treeObj.expandAll(true);
              treeObj.cancelSelectedNode();
          },
          clearDepImp: function(rowId){
          	  IMPROWID = rowId;
          	  var row = viewModel.confirmImpGridDataTable.getRowByRowId(rowId);
          	  row.setValue("dep","");
              row.setValue("status","匹配失败");
              row.setValue("statusCode",0);
            },
        };

        //初始化事项下拉框
        viewModel.initDropDown = function () {
        	$("#eventName").html(task_name);
        };
        
        //初始化状态框
        viewModel.initStatus = function(pageId) {
        	if(pageId == "m1") {     
        		$(".rightTable-title-status").html('<label><input name="submitStatus" type="radio" value="9" onchange ="submitStatusChange()"/>全部 </label> '+
        	        	'<label><input name="submitStatus" type="radio" value="0" onchange ="submitStatusChange()"/>待提交</label> '+
        	        	'<label><input name="submitStatus" type="radio" value="1" onchange ="submitStatusChange()"/>已提交 </label>');
        	}else if(pageId == "m2"){
        		$(".rightTable-title-status").html('<label><input name="submitStatus" type="radio" value="9" onchange ="submitStatusChange()"/>全部 </label> '+
        	        	'<label><input name="submitStatus" type="radio" value="0" onchange ="submitStatusChange()"/>待初审</label> '+
        	        	'<label><input name="submitStatus" type="radio" value="1" onchange ="submitStatusChange()"/>已初审 </label>');
        	}else if(pageId == "m3") {
        		$(".rightTable-title-status").html('<label><input name="submitStatus" type="radio" value="9" onchange ="submitStatusChange()"/>全部 </label> '+
        	        	'<label><input name="submitStatus" type="radio" value="0" onchange ="submitStatusChange()"/>待复审</label> '+
        	        	'<label><input name="submitStatus" type="radio" value="1" onchange ="submitStatusChange()"/>已复审 </label>');
        	}else {
        		$(".rightTable-title-status").html('<label><input name="submitStatus" type="radio" value="9" onchange ="submitStatusChange()"/>全部 </label> '+
        	        	'<label><input name="submitStatus" type="radio" value="0" onchange ="submitStatusChange()"/>待办理</label> '+
        	        	'<label><input name="submitStatus" type="radio" value="1" onchange ="submitStatusChange()"/>已办理 </label>');
        	}
   		    var radio =document.getElementsByName("submitStatus");
     	    for(var i =0;i < radio.length;i++){
	        	   if(radio[i].value == "9"){
	        		  radio[i].checked = true;
	        	   }else{
	        		  radio[i].checked = false;
	        	   }
     	    }
        }       
        
      //初始化部门树
        viewModel.initDepTree = function () {
        	$.ajax({
	            type: 'get',
	            url: depTreeURL + "?tokenid=" + tokenid + "&ajax=noCache",
	            data: {"ele_code":eleCode,"task_id":task_id,"is_allobj":IS_ALLOBJ,"obj_type_id":OBJ_TYPE_ID,},
	            dataType: 'JSON',
	            async: true,
	            success: function (result) {
	            	if(result.data.length>0) {
	            		var dep_array = [];
	            		for(var i=0;i<result.data.length;i++) {
	            			if(result.data[i].PARENT_ID == "" || result.data[i].PARENT_ID == null) {
	            				result.data[i].PARENT_ID = "000";
	            			}
            				var chrName = result.data[i].CHR_CODE + " " +result.data[i].CHR_NAME;
	            			var depArray = {
	                                "CHR_ID": result.data[i].CHR_ID,
	                                "CHR_CODE": result.data[i].CHR_CODE,
	                                "DISP_CODE": result.data[i].DISP_CODE,
	                                "NAME": chrName,
	                                "CHR_NAME": result.data[i].CHR_NAME,
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
	                                "OID": result.data[i].OID,
	                                "DEP_ID" : result.data[i].DEP_ID,
	                                "MOFDEP_ID" : result.data[i].MOFDEP_ID
	                            }
	            			dep_array.push(depArray);
	            		}
	            		var depArray = {
                                "CHR_ID": "000",
                                "CHR_CODE": "",
                                "DISP_CODE": "",
                                "NAME":"全部",
                                "CHR_NAME": "",
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
                                "OID": "",
                                "DEP_ID" : "",
                                "MOFDEP_ID" : ""
                            }
            			dep_array.push(depArray);
	            		viewModel.depTreeDataTable.setSimpleData(dep_array);
	            		var treeObj = $.fn.zTree.getZTreeObj("depTree");
	                    treeObj.expandAll(true);
	                    treeObj.cancelSelectedNode();
	            	}
	            }, error: function () {
	                ip.ipInfoJump("错误", 'error');
	            }
            });
        };        
        
        $("#impTreeSure").click(function (e) {
        	var treeObj = $.fn.zTree.getZTreeObj("impDepTree");
        	var nodes = treeObj.getSelectedNodes();
        	if(nodes.length > 0) {
        		if(nodes[0].IS_LEAF == 1){
        			var getRow = viewModel.confirmImpGridDataTable.getRowByRowId(IMPROWID);
        			var confirmData = viewModel.confirmImpGridDataTable.getSimpleData();
    				getRow.setValue('dep',nodes[0].NAME);
    				getRow.setValue('status',"匹配成功");
    				getRow.setValue('statusCode',1);
    				$.ajax({
    		            type: 'GET',
    		            url: ifExistBillURL+"?tokenid=" + tokenid + "&ajax=noCache",
    		            data: {"chr_id":nodes[0].CHR_ID,"billtype_code":billTypeCode,"task_id":task_id},
    		            dataType: 'JSON',
    		            async: false,
    		            success: function (map) {
    		            	if(map.errorCode == 0){
    		            		if(map.data){
    		            		}else{
    		            			getRow.setValue('dep',"");
    		        				getRow.setValue('status',"该单位数据已录入");
    		        				getRow.setValue('statusCode',0);
    		            		}
    		            	}
    		            }	                
	                }); 
    				for(var i=0;i<confirmData.length;i++){
    					if(confirmData[i].dep.split(" ")[0] == nodes[0].CHR_CODE){
    						getRow.setValue('dep',"");
        					getRow.setValue('statusCode',0);
        					getRow.setValue('status',"单位重复");
        					break;
        				}
    				}
//        			if(getRow.data.file_name.value.indexOf(nodes[0].CHR_CODE)!=-1){
//        				
//    				}else{
//    					getRow.setValue('dep',"");
//    					getRow.setValue('statusCode',0);
//    					getRow.setValue('status',"匹配失败");
//    				}
        			$('#impTreeModel').modal('toggle');
        		}else{
        			ip.ipInfoJump("请选择底层单位！", 'error');
        		}
        	}
        });
       
        //单位树根据状态切换
        $('#depTreeTab a').click(function (e) {
            var exStatus = e.currentTarget.innerHTML;
            if(exStatus == "当前") {
                viewModel.initDepTree();
            }else if(exStatus == "未办结") {
                viewModel.initDepTree();
                var treeObj = $.fn.zTree.getZTreeObj("depTree2");
                treeObj.expandAll(true);
                treeObj.cancelSelectedNode();
            }else{
                viewModel.initDepTree();
                var treeObj = $.fn.zTree.getZTreeObj("depTree3");
                treeObj.expandAll(true);
                treeObj.cancelSelectedNode();
            }
        });

		//select状态改变
        
        submitStatusChange = function(){
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	var radio =document.getElementsByName("submitStatus");
        	var submit_status = null;
        	for(var i =0;i < radio.length;i++){
	        	 if(radio[i].checked){
	        	     submit_status = radio[i].value;
	        	 }
        	}
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
        	var treeObj = $.fn.zTree.getZTreeObj("depTree");
        	var nodes = treeObj.getSelectedNodes();
        	if(nodes.length > 0) {
        		var selectNodeId = nodes[0].CHR_ID;
        		var type = nodes[0].TYPE;
       		    var code = nodes[0].CHR_CODE;
       		    var oid = nodes[0].OID;
            }else {
            	var code = "";
            	var oid = "";  
            	var selectNodeId = "";
            	var type = "";
            }    
            viewModel.refresh(selectNodeId,type,code,oid,submit_status);
        };             
        
        //附件上传
        $("#fileUplode_li").click(fileData,function(event){	
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	for(var i=0;i<btnData.length;i++) {
        		var btnId = btnData[i].BUTTON_ID;
        		if(btnData[i].DISPLAY_STATUS == "2") {
        			$("#"+btnId).attr("disabled","disabled");
        		}
        	}
        	publicFileman("fileUploadPage");
        });
        
        //附件上传
        $("#dataInputfileUplode_li").click(fileData,function(event){	
        	publicFileman("dataInputfileUploadPage");
        });
        
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
                        options["billtypecode"] = "101";
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
                        options["billtypecode"] = "101";
                        options["menu_id"] = pageId;
                        options["task_id"] = task_id;
                    }
                }
            }
            viewModel.gridViewModel = ip.initGridWithCallBack(gridCallback,viewModel.tableViewDetail, 'testGrid', "/df/cs/selectAll.do",options, 1, false,true,false,false);
            if(viewModel.gridViewModel.gridData.getSimpleData()){
                $("#gridSumNum").html(viewModel.gridViewModel.gridData.getSimpleData().length);
            }
            //初始化Grid的select事件
            viewModel.initGridSelectsFun();
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
        
        viewModel.initGridSelectsFun = function() {
        	viewModel.gridViewModel.gridData.on('select', function(e) {
    			var btnData = viewModel.btnDataTable.getSimpleData();
        		for(var i=0;i<e.rowIds.length;i++) {
        			var status = viewModel.gridViewModel.gridData.getRowByRowId(e.rowIds[i]).getValue('STATUS_CODE');
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
                        options["billtypecode"] = "101";
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
                        options["billtypecode"] = "101";
                        options["menu_id"] = pageId;
                        options["task_id"] = task_id;
                    }
                }
            }
        	ip.setGridWithCallBack(gridCallback,viewModel.gridViewModel, "/df/cs/selectAll.do", options);
            $("#gridSumNum").html(viewModel.gridViewModel.gridData.getSimpleData().length);
        }
        
        //数据录入
        dataInput = function(id) {
        	nowBtnId = id;
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	for(var bt = 0; bt<btnData.length;bt++) {
        		if(btnData[bt].BUTTON_ID == id) {
        			var btn = btnData[bt];
        			break;
        		}
        	}
        	postAction = "dataInput";
        	var op_name = btn.DISPLAY_TITLE;
    		var op_type = btn.ACTION_TYPE;
    		var treeObj = $.fn.zTree.getZTreeObj("depTree");
        	var nodes = treeObj.getSelectedNodes();
        	if(nodes.length > 0) {
        		if(nodes[0].IS_LEAF == "1"){
        			var selectRow = viewModel.gridViewModel.gridData.getSimpleData();
        			if(selectRow.length > 0){
        				ip.ipInfoJump("该单位数据已录入", 'info');
        			}else{
        				$.ajax({
        		            type: 'GET',
        		            url: ifExistBillURL+"?tokenid=" + tokenid + "&ajax=noCache",
        		            data: {"chr_id":nodes[0].CHR_ID,"billtype_code":billTypeCode,"task_id":task_id,},
        		            dataType: 'JSON',
        		            async: false,
        		            success: function (map) {
        		            	if(map.errorCode == 0){
        		            		if(map.data){
        		            			$('#dataInputModal').modal({
        		                        	show : true,
        		                        	backdrop : 'static'
        		                        });
        		        				$.ajax({
        		        		            type: 'post',
        		        		            url: "/df/task/getUUID.do?tokenid=" + tokenid + "&ajax=noCache",
        		        		            data: {},
        		        		            dataType: 'text',
        		        		            async: false,
        		        		            success: function (map) {
        		                                pageData.ID = map;
        		        		            }	                
        		    	                }); 
        		                    	for(var i=0;i<btnData.length;i++) {
        		                    		var btnId = btnData[i].BUTTON_ID;
        		                    		if(btnData[i].DISPLAY_STATUS == "0" || btnData[i].DISPLAY_STATUS == "2" || (btnData[i].DISPLAY_STATUS == "9" && btnData[i].ENABLED == "1")){
        		                    			$("#"+btnId).removeAttr("disabled","disabled");
        		                    		}else {
        		                    			$("#"+btnId).attr("disabled","disabled");
        		                    		}
        		                    	}
        		                		viewModel.navShow(pageData.ID,"false",nodes[0].CHR_ID,nodes[0].CHR_CODE);
        		            		}else{
        		            			ip.ipInfoJump("该单位数据已录入", 'info');
        		            		}
        		            	}
        		            }	                
    	                }); 
        			}
        		}else{
            			ip.ipInfoJump("请选择底级单位", 'info');
            	}
        	}else{
            	ip.ipInfoJump("请选择需要操作的单位", 'info');
            }
        }
        
        //批量导入
        batchImp = function(id) {
        	if(pageData.execlReportData.length && pageData.execlReportData.length > 0){
        		nowBtnId = id;
            	$('#batchImpModal').modal({
                	show : true,
                	backdrop : 'static'
                });
            	postAction = "batchImp";
            	num = 0;
                eventsDeal.modalTab();
                eventsDeal.reduceStep();
                $('#btn2').show();
                $('#btn4').show();
                $('#btn1').hide();
                $('#btn3').hide();
                $('#impbtn').hide();
            	pageData.getUUID();
            	$("#viewShow").html("");
            	var viewHtml = ''; 
				for(var i=0;i<pageData.execlReportData.length;i++) {
					if(pageData.execlReportData[i].REPORT_TYPE == 3 || pageData.execlReportData[i].REPORT_TYPE == '3'){
						viewHtml = viewHtml + '<label><input name="view" type="radio" value="'+pageData.execlReportData[i].VIEW_ID+'" />&nbsp;&nbsp;'+pageData.execlReportData[i].DISPLAY_TITLE+' </label>';
					}
				}
				$("#viewShow").html(viewHtml);
        	}
        }
        
        //获取文件名
        viewModel.getfileName = function(id){
        	$.ajax({
                type: 'get',
                url: getFileNameURL + "?tokenid=" + tokenid + "&ajax=noCache",
                data: {"id":id},
                dataType: 'JSON',
                async: false,
                success: function (result) {
                	if(result.errorCode == "0") {
                		viewModel.impFileDataTable.setSimpleData(result.data);
                		var depTreeData = viewModel.depTreeDataTable.getSimpleData();
                		var dep_arr = [];
                		for(var i=0;i<result.data.length;i++){
                			var depArray = {};
                			depArray["file_name"] = result.data[i].file_name;
                			depArray["file_id"] = result.data[i].id;
                			for(var j = 0;j<depTreeData.length;j++){
                				if(depTreeData[j].IS_LEAF == 1){
                					if(result.data[i].file_name.indexOf(depTreeData[j].CHR_CODE)!=-1){
                    					depArray["status"] = "匹配成功";
                    					depArray["statusCode"] = 1;
                    					depArray["dep"] = depTreeData[j].NAME;
                    					$.ajax({
        		        		            type: 'GET',
        		        		            url: ifExistBillURL+"?tokenid=" + tokenid + "&ajax=noCache",
        		        		            data: {"chr_id":depTreeData[j].CHR_ID,"billtype_code":billTypeCode,"task_id":task_id,},
        		        		            dataType: 'JSON',
        		        		            async: false,
        		        		            success: function (map) {
        		        		            	if(map.errorCode == 0){
        		        		            		if(map.data){
        		        		            		}else{
        		        		            			depArray["status"] = "该单位数据已录入";
        		                    					depArray["statusCode"] = 0;
        		                    					depArray["dep"] = "";
        		        		            		}
        		        		            	}
        		        		            }	                
        		    	                }); 
                						for(var z=0;z<dep_arr.length;z++){
                							if(depTreeData[j].CHR_CODE == dep_arr[z].dep.split(" ")[0]){
                								depArray["status"] = "单位重复";
                            					depArray["statusCode"] = 0;
                            					depArray["dep"] = "";
                            					break;
                							}
                						}
                    					break;
                    				}else{
                    					depArray["status"] = "匹配失败";
                    					depArray["statusCode"] = 0;
                    					depArray["dep"] = "";
                    				}
                				}else{
                					depArray["status"] = "匹配失败";
                					depArray["statusCode"] = 0;
                					depArray["dep"] = "";
                				}
                			}
                			dep_arr.push(depArray);
                		}
                		viewModel.confirmImpGridDataTable.setSimpleData(dep_arr);
                	}else {
                		ip.ipInfoJump("错误", 'error');
                	}
                }, error: function () {
                    ip.ipInfoJump("错误", 'error');
                }
            }); 
        }
        
        //保存数据录入
        saveDataInput = function(){
        	if($("#dataInputModal").find('iframe').contents().find("#saveKeepVal").val() == "1"){
        		if(postAction == "dataUpdate"){
                  	$('#dataInputModal').modal('toggle');
            	}else if(postAction == "dataInput"){
            		var treeObj = $.fn.zTree.getZTreeObj("depTree");
                  	var nodes = treeObj.getSelectedNodes();
                  	var selectNodeId = nodes[0].CHR_ID;
          			var selectNodeCode = nodes[0].CHR_CODE;
          			var selectNodeName = nodes[0].CHR_NAME;
          			var selectNodeOid = nodes[0].OID;
          			var selectNodeDepID = nodes[0].DEP_ID;
          			var selectNodeMofdepId = nodes[0].MOFDEP_ID;
          			saveBusinessDataInput(selectNodeId,selectNodeCode,selectNodeName,selectNodeDepID,selectNodeMofdepId,pageData.ID,"dataInputModal");
            	}
        	}else{
        		ip.ipInfoJump("动态表单未保存成功", 'error');
        	}
        }
        
        //保存业务数据录入
        saveBusinessDataInput = function(selectNodeId,selectNodeCode,selectNodeName,selectNodeDepID,selectNodeMofdepId,id,modalId,impData){
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	for(var bt = 0; bt<btnData.length;bt++) {
        		if(btnData[bt].BUTTON_ID == nowBtnId) {
        			var btn = btnData[bt];
        			break;
        		}
        	}
        	var op_name = btn.DISPLAY_TITLE;
    		var op_type = btn.ACTION_TYPE;
    		var data = {
	        		"billtype_code":billTypeCode,
	        		"sid":sid,
	        		"op_type":op_type,
	        		"op_name":op_name,
	        		"menu_id":pageId,
	        		"chr_id":selectNodeId,
	        		"chr_code":selectNodeCode,
	        		"chr_name":selectNodeName,
	        		"task_id":task_id,
	        		"sup_date":bbq_date,
	        		"dep_id" :selectNodeDepID,
	        		"mofdep_id" :selectNodeMofdepId,
	        		"bill_id" :id,
	        	}
	        	$.ajax({
	                type: 'get',
	                url: saveBillURL + "?tokenid=" + tokenid + "&ajax=noCache",
	                data: data,
	                dataType: 'JSON',
	                async: false,
	                success: function (result) {
	                	if(result.errorCode == "0") {
	                		if(result.map == true){
	                			viewModel.refresh("","","","","9");
    	                    	for(var i=0;i<btnData.length;i++) {
    	                    		var btnId = btnData[i].BUTTON_ID;
    	                    		if(btnData[i].DISPLAY_STATUS == "0" || btnData[i].DISPLAY_STATUS == "2" || (btnData[i].DISPLAY_STATUS == "9" && btnData[i].ENABLED == "1")){
    	                    			$("#"+btnId).removeAttr("disabled","disabled");
    	                    		}else {
    	                    			$("#"+btnId).attr("disabled","disabled");
    	                    		}
    	                    	}
    	                    	if(modalId == "batchImpModal"){
    	                    		$("#log").append("<br>-----导入"+impData.file_name+"内容保存成功-----");
                        	    	var showNum = parseInt(viewModel.data.hasOpNum());
                        	    	viewModel.data.hasOpNum(++showNum);
    	                    	}else{
        	                      	$('#'+modalId).modal('toggle');
    	                    	}
	                		}else{
	                          	if(modalId == "batchImpModal"){
    	                    		$("#log").append("<br>-----导入"+impData.file_name+"文件的单位已存在数据，不允许录入-----");
    	                    		$("#log").append("<br>-----导入"+impData.file_name+"结束-----");
                        	    	var showNum = parseInt(viewModel.data.hasOpNum());
                        	    	viewModel.data.hasOpNum(++showNum);
    	                    	}else{
    	                          	$('#'+modalId).modal('toggle');
    	                    	}
	                			ip.ipInfoJump("数据已存在，不允许录入", 'info');
	                		}
	                	}else {
	                		if(modalId == "batchImpModal"){
	                			$("#log").append("<br>-----导入"+impData.file_name+"失败-----");
	                			$("#log").append("<br>-----导入"+impData.file_name+"结束-----");
                    	    	var showNum = parseInt(viewModel.data.hasOpNum());
                    	    	viewModel.data.hasOpNum(++showNum);
	                    	}
	                		ip.ipInfoJump("错误", 'error');
	                	}
	                }, error: function () {
	                	if(modalId == "batchImpModal"){
                			$("#log").append("<br>-----导入"+impData.file_name+"失败-----");
                			$("#log").append("<br>-----导入"+impData.file_name+"结束-----");
                	    	var showNum = parseInt(viewModel.data.hasOpNum());
                	    	viewModel.data.hasOpNum(++showNum);
                    	}
	                    ip.ipInfoJump("错误", 'error');
	                }
	            }); 
        }
        
        //附件
        var publicFileman = function(obj) {
            $("#"+obj)[0].src = "";
            fileData = {
                "entity_id":pageData.ID,
                "oid":options.svOfficeId,
                "dep_id":options.svDepId,
                "dep_code":options.svDepCode,
                "edit":'Y',
                "modular":'SUP',
            };
            //if(pageData.SID)
            $("#"+obj)[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
                "&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&entityName=csof_sup_bill&oid="+fileData.oid+
                "&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit+"&modular="+fileData.modular;
        };
        
//        //录入
//        supInput = function(id) {
//        	nowBtnId = id;
//        	var btnData = viewModel.btnDataTable.getSimpleData();
//        	for(var bt = 0; bt<btnData.length;bt++) {
//        		if(btnData[bt].BUTTON_ID == id) {
//        			var btn = btnData[bt];
//        			break;
//        		}
//        	}
//        	var op_name = btn.DISPLAY_TITLE;
//    		var op_type = btn.ACTION_TYPE;
//    		var treeObj = $.fn.zTree.getZTreeObj("depTree");
//        	var nodes = treeObj.getSelectedNodes();
//        	if(nodes.length > 0) {
//        		if(nodes[0].IS_LEAF == "1"){
//        			var selectRow = viewModel.gridViewModel.gridData.getSimpleData();
//        			if(selectRow.length > 0){
//        				ip.ipInfoJump("该单位数据已录入", 'info');
//        			}else{
//        				var selectNodeId = nodes[0].CHR_ID;
//            			var selectNodeCode = nodes[0].CHR_CODE;
//            			var selectNodeName = nodes[0].CHR_NAME;
//            			var selectNodeOid = nodes[0].OID;
//            			var selectNodeDepID = nodes[0].DEP_ID;
//            			var selectNodeMofdepId = nodes[0].MOFDEP_ID;
//            			var data = {
//            	        		"billtype_code":billTypeCode,
//            	        		"sid":sid,
//            	        		"op_type":op_type,
//            	        		"op_name":op_name,
//            	        		"menu_id":pageId,
//            	        		"chr_id":selectNodeId,
//            	        		"chr_code":selectNodeCode,
//            	        		"chr_name":selectNodeName,
//            	        		"task_id":task_id,
//            	        		"sup_date":bbq_date,
//            	        		"dep_id" :selectNodeDepID,
//            	        		"mofdep_id" :selectNodeMofdepId,
//            	        	}
//            	        	$.ajax({
//            	                type: 'get',
//            	                url: getBillNoURL + "?tokenid=" + tokenid + "&ajax=noCache",
//            	                data: data,
//            	                dataType: 'JSON',
//            	                async: false,
//            	                success: function (result) {
//            	                	if(result.errorCode == "0") {
//            	                		if(result.map == true){
//            	                			viewModel.refresh("","","","","9");
//                	                		$("#details-li").addClass("active");
//                	                		$("#list-li").removeClass("active");
//                	                		$("#panel-list").removeClass("active");
//                	                		$("#panel-details").addClass("active");  
//                	                		$("#panel-details").show();  
//                	                		fileData = {
//                	            					"entity_id":result.data,
//                	            					"oid":selectNodeOid,
//                	            					"dep_id":selectNodeId,
//                	            					"dep_code":selectNodeCode,
//                	            					"edit":'Y',
//                	            			}
//                	                    	for(var i=0;i<btnData.length;i++) {
//                	                    		var btnId = btnData[i].BUTTON_ID;
//                	                    		if(btnData[i].DISPLAY_STATUS == "0" || btnData[i].DISPLAY_STATUS == "2" || (btnData[i].DISPLAY_STATUS == "9" && btnData[i].ENABLED == "1")){
//                	                    			$("#"+btnId).removeAttr("disabled","disabled");
//                	                    		}else {
//                	                    			$("#"+btnId).attr("disabled","disabled");
//                	                    		}
//                	                    	}
//                	                		viewModel.navShow(result.data,"false",selectNodeCode);  
//            	                		}else{
//            	                			ip.ipInfoJump("数据已存在，不允许录入", 'info');
//            	                		}
//            	                	}else {
//            	                		ip.ipInfoJump("错误", 'error');
//            	                	}
//            	                }, error: function () {
//            	                    ip.ipInfoJump("错误", 'error');
//            	                }
//            	            }); 
//        			}
//        		}else{
//            			ip.ipInfoJump("请选择底级单位", 'info');
//            	}
//        	}else{
//            	ip.ipInfoJump("请选择需要操作的单位", 'info');
//            }
//        };
        
        newExcelShow = function(id,areaId,selectId,viewId) {
            $("#"+areaId)[0].src = "";
            var execlData = {
    				"entity_id":id,
    				"oid":options.svOfficeId,
    				"dep_id":options.svDepId,
    				"dep_code":options.svDepCode,
    				"edit":'Y',
    				"modular": 'csofSup',
    				"billtypeCode":billTypeCode,
    				"billId":id,
    				"objtypeId":OBJ_TYPE_ID,
    				"objId":selectId,
    				"supCycle":"-1",
    				"supDate":bbq_date,
    				"saveBtn":"Y",
    				"isImp":"N",
    				"viewId":viewId,
    		    };
            $("#"+areaId)[0].src = "/df/supervise/newExcel/excel.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
            "&menuname=" + options.svMenuName+"&entity_id="+execlData.entity_id+"&entityName=csof_sup_bill&oid="+execlData.oid+
            "&dep_id="+execlData.dep_id+"&dep_code="+execlData.dep_code+"&modelFlag=0&admin="+execlData.edit+"&modular="+execlData.modular+
            "&saveBtn="+execlData.saveBtn+"&billtypeCode="+execlData.billtypeCode+"&billId="+execlData.billId+"&objtypeId="+execlData.objtypeId+
            "&objId="+execlData.objId+"&supCycle="+execlData.supCycle+"&supDate="+execlData.supDate+"&isImp="+execlData.isImp+"&viewId="+execlData.viewId;
        };

		//页签显示
		viewModel.navShow = function(id,readonly,obj_id,obj_code) {
			if(readonly == "false"){
				var dataInputid = "dataInput";
			}else if(readonly == "true"){
				var dataInputid = "";
			}
			var data = pageData.EReportNavData;
        	if(data.length == 0){
        		$("#"+dataInputid+"fileUplode_li").addClass("active");
        		$("#panel-"+dataInputid+"fileUpload").addClass("active");
        		publicFileman(dataInputid+"fileUploadPage",readonly);
        	}else{
            	$(".dynamic").remove();
				for(var i=0;i<data.length;i++) {
					if(i == 0) {
						$("#"+dataInputid+"fileUplode_li").before("<li id='"+data[i].REPORT_ID+"_"+dataInputid+"li' class='active dynamic'><a data-toggle='tab' href='#panel"+dataInputid+"-"+data[i].REPORT_ID+"' id='"+data[i].REPORT_ID+"-"+dataInputid+"'>"+data[i].DISPLAY_TITLE+"</a></li>");
						$("#panel-"+dataInputid+"fileUpload").before("<div class='tab-pane active dynamic' style='height:100%;' id='panel"+dataInputid+"-"+data[i].REPORT_ID+"'><iframe src='' class='irptShowIframe' id='"+data[i].REPORT_ID+dataInputid+"Page'></iframe></div>");
						$("#panel"+dataInputid+"-"+data[i].REPORT_ID).addClass("active");
						viewModel.irptShow(data[i].REPORT_ID,readonly,obj_id,obj_code);
					}else {
						$("#"+dataInputid+"fileUplode_li").before("<li id='"+data[i].REPORT_ID+"_"+dataInputid+"li' class='dynamic'><a data-toggle='tab' href='#panel"+dataInputid+"-"+data[i].REPORT_ID+"' id='"+data[i].REPORT_ID+"-"+dataInputid+"'>"+data[i].DISPLAY_TITLE+"</a></li>");
						$("#panel-"+dataInputid+"fileUpload").before("<div class='tab-pane dynamic' style='height:100%;' id='panel"+dataInputid+"-"+data[i].REPORT_ID+"'><iframe src='' class='irptShowIframe' id='"+data[i].REPORT_ID+dataInputid+"Page'></iframe></div>");
						$("#panel"+dataInputid+"-"+data[i].REPORT_ID).removeClass("active");
					}
					$("#"+data[i].REPORT_ID+"_"+dataInputid+"li").click(function(){
						var id = this.id;
						id=id.split("_")[0];
						viewModel.irptShow(id,readonly,obj_id,obj_code);
						var btnData = viewModel.btnDataTable.getSimpleData();
			        	for(var i=0;i<btnData.length;i++) {
			        		var btnId = btnData[i].BUTTON_ID;
			        		if(btnData[i].DISPLAY_STATUS == "2") {
			        			$("#"+btnId).removeAttr("disabled","disabled");
			        		}
			        	}
					});
				}
				$("#panel-"+dataInputid+"fileUpload").removeClass("active");
				$("#"+dataInputid+"ileUplode_li").removeClass("active");
        	}
		};
		
		//报表显示
		viewModel.irptShow = function(rid,readonly,obj_id,obj_code) {
			if(readonly == "false"){
				var dataInputid = "dataInput";
			}else if(readonly == "true"){
				var dataInputid = "";
			}
			for(var i=0;i<pageData.EReportNavData.length;i++){
				if(rid == pageData.EReportNavData[i].REPORT_ID){
					var repoty_type = pageData.EReportNavData[i].REPORT_TYPE;
					break;
				}
			}
			if(repoty_type == 3){
				newExcelShow(pageData.ID,rid+""+dataInputid+"Page",obj_id,pageData.EReportNavData[i].VIEW_ID);
			}else{
	        	$("#"+rid+""+dataInputid+"Page").attr("src", getReportURL + "?tokenid=" +  ip.getTokenId() + "&ajax=noCache" + "&chrId="+rid+"&obj_id="+obj_code+"&param_add_str=" +""+"&bbq_date="+bbq_date+"&readonly="+readonly);
			}
		};
		
		//点击列表刷新视图
		$('#list-li').on('click' , function() {  
			var btnData = viewModel.btnDataTable.getSimpleData();
        	for(var i=0;i<btnData.length;i++) {
        		var btnId = btnData[i].BUTTON_ID;
        		if(btnData[i].DISPLAY_STATUS == "2") {
        			$("#"+btnId).attr("disabled","disabled");
        		}
        	}
			var treeObj = $.fn.zTree.getZTreeObj("depTree");
			var nodes = treeObj.getSelectedNodes();
			$("#panel-details").hide();
     	    var radio =document.getElementsByName("submitStatus");
    	    var submitStatus = null;
    	    for(var i =0;i < radio.length;i++){
	        	 if(radio[i].checked){
	        	     submitStatus = radio[i].value;
	        	 }
    	    }
        	if(nodes.length > 0) {
        		var selectNodeId = nodes[0].CHR_ID;
                var type = nodes[0].TYPE;
       		    var code = nodes[0].CHR_CODE;
       		    var oid = nodes[0].OID;
                viewModel.refresh(selectNodeId,type,code,oid,submitStatus);
        	}
        	else{
        		viewModel.refresh("","","","",submitStatus);
        	}
		})
		
//		//详细显示报表
//		$('#details-li').on('click' , function() {        	
//			var selectRow = viewModel.gridViewModel.gridData.getSelectedRows();
//			var btnData = viewModel.btnDataTable.getSimpleData();
//        	for(var i=0;i<btnData.length;i++) {
//        		var btnId = btnData[i].BUTTON_ID;
//        		if(btnData[i].DISPLAY_STATUS == "2") {
//        			$("#"+btnId).attr("disabled","disabled");
//        		}
//        	}
//			if(selectRow.length == 1){
//				$("#panel-details").show();
//				var bill_no = selectRow[0].data.BILL_NO.value;
//				var oid = selectRow[0].data.OID.value;
//				var dep_id = selectRow[0].data.AGENCY_ID.value;
//				var dep_code = selectRow[0].data.AGENCY_CODE.value;
//        		fileData = {
//    					"entity_id":bill_no,
//    					"oid":oid,
//    					"dep_id":dep_id,
//    					"dep_code":dep_code,
//    					"edit" : "N",
//    			}
//        		viewModel.navShow(bill_no,"true",dep_id,dep_code); 
//			}else{
//				$("#panel-details").hide();
//        		ip.ipInfoJump("请选择一个需要操作的事项", 'info');
//			}
//		});
		
//		//视图双击函数
//		testGrid_onDbClick = function(obj){
//		    	var index = obj.rowIndex;
//		    	$("#details-li").addClass("active");
//        		$("#list-li").removeClass("active");
//        		$("#panel-list").removeClass("active");
//        		$("#panel-details").addClass("active");
//        		$("#panel-details").show();
//        		var btnData = viewModel.btnDataTable.getSimpleData();
//            	for(var i=0;i<btnData.length;i++) {
//            		var btnId = btnData[i].BUTTON_ID;
//            		if(btnData[i].DISPLAY_STATUS == "2") {
//            			$("#"+btnId).attr("disabled","disabled");
//            		}
//            	}
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
//        		viewModel.navShow(bill_no,"true",dep_id,dep_code);   
//		 };


        //工作流函数
        viewModel.WorkFlow = function(id,op) {
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	for(var bt = 0; bt<btnData.length;bt++) {
        		if(btnData[bt].BUTTON_ID == id) {
        			var btn = btnData[bt];
        			break;
        		}
        	}
        	var selectRow = viewModel.gridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
        		for(var i=0;i<selectRow.length;i++) {
        			var flag = 0;
        			var entity_id = selectRow[i].data.ID.value;
            		var status = selectRow[i].data.STATUS_CODE.value;                 		
            		if(status != btn.DISPLAY_STATUS) {
            			ip.ipInfoJump("数据状态不符", 'info');
            		}else {
            			var op_name = btn.DISPLAY_TITLE;
                		var op_type = btn.ACTION_TYPE;
                		if(op){
                			var opinion = op;
                		}else{
                			var opinion = ""
                		}
                		var data = {
                    			"menu_id":pageId,
                    			"entity_id":entity_id,
                    			"billtype_code":101,//die
                    			"op_type":op_type,
                    			"op_name":op_name,
                    			"opinion":opinion,
                    	};
                		$.ajax({
                            type: 'get',
                            url: doPayWorkFlowURL + "?tokenid=" + tokenid + "&ajax=noCache",
                            data: data,
                            dataType: 'JSON',
                            async: false,
                            success: function (result) {
                            	if(result.data == false) {
                            		ip.ipInfoJump("第"+ i+1 + "条数据"+op_name+"失败", 'info');
                            		flag = -1;
                            		submitStatusChange();
                            	}else {
                            		submitStatusChange();
                                	viewModel.workFlowAfterBtn(op_type);
                                	ip.ipInfoJump(op_name+'成功！', 'success');
                            	}
                            }, error: function () {
                                ip.ipInfoJump("错误", 'error');
                            }
                        });                    	
            		}
            		if(flag == -1) {
            			break;
            		}
        		}
            }else {
        		ip.ipInfoJump("请勾选需要操作的监管内容", 'info');
        	}
        }
        
        //工作流成功后按钮变化
        viewModel.workFlowAfterBtn = function(op_type) {
        	var btnData = viewModel.btnDataTable.getSimpleData();
        	if(op_type == "INPUT" || op_type == "NEXT") {
    			for(var i=0;i<btnData.length;i++) {
            		var btnId = btnData[i].BUTTON_ID;
            		if(btnData[i].DISPLAY_STATUS == "1" || (btnData[i].DISPLAY_STATUS == "9" && btnData[i].ENABLED == "1")) {
            			$("#"+btnId).removeAttr("disabled","disabled");
            		}else {
            			$("#"+btnId).attr("disabled","disabled");
            		}
            	}        		
        	}else if(op_type == "UNDO") {
        		for(var i=0;i<btnData.length;i++) {
            		var btnId = btnData[i].BUTTON_ID;
            		if(btnData[i].DISPLAY_STATUS == "0" || (btnData[i].DISPLAY_STATUS == "9" && btnData[i].ENABLED == "1")) {
            			$("#"+btnId).removeAttr("disabled","disabled");
            		}else {
            			$("#"+btnId).attr("disabled","disabled");
            		}
            	}
        	}else if(op_type == "BACK"){
        		for(var i=0;i<btnData.length;i++) {
        			if(btnData[i].DISPLAY_STATUS == "9" && btnData[i].ENABLED == "1") {
        				var btnId = btnData[i].BUTTON_ID;
                		$("#"+btnId).removeAttr("disabled","disabled");
        			}
            	}     		
        	}
        }
        
		//提交
        workFlowSubmit = function(id) {
        	var selectRow = viewModel.gridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
            	viewModel.WorkFlow(id);
            }else {
        		ip.ipInfoJump("请勾选需要操作的监管内容", 'info');
        	}
        };
        
        //备案
        workFlowEnd = function(id) {
        	var selectRow = viewModel.gridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
            	viewModel.WorkFlow(id);
            }else {
        		ip.ipInfoJump("请勾选需要操作的监管内容", 'info');
        	}
        };
        
        
        //工作流撤销
        workFlowUndo = function(id) {
        	var selectRow = viewModel.gridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
            	viewModel.WorkFlow(id);
            }else {
        		ip.ipInfoJump("请勾选需要操作的监管内容", 'info');
        	}
        };
        
        //退回
        workFlowReturn = function(id) {
        	var selectRow = viewModel.gridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
            	var btnData = viewModel.btnDataTable.getSimpleData();
            	var btn = getBtnMsgById(btnData,id);
            	csof.opinionModal('退回意见',pageId+"-opinionConfirmSureId",pageId+"-opinionConfirmCancelCla",pageId);
            	$("#"+pageId+"-op_name").html(btn.DISPLAY_TITLE);
                $("#"+pageId+"-opinionConfirmSureId").on('click',function(){
                	if($("#"+pageId+"-opinion").val() != "" && $("#"+pageId+"-opinion").val() != null){
                    	viewModel.WorkFlow(id,$("#"+pageId+"-opinion").val());
                    	$('#'+pageId+'-opinion-modal').modal('hide');
                        $('#'+pageId+'-opinion-modal').remove();
                	}else{
                		ip.ipInfoJump("请输入意见！", 'info');
                	}
                });
                $('.'+pageId+'-opinionConfirmCancelCla').on('click',function(){
                	$('#'+pageId+'-opinion-modal').modal('hide');
                    $('#'+pageId+'-opinion-modal').remove();
                });
            }else {
        		ip.ipInfoJump("请勾选需要操作的监管内容", 'info');
        	}
        }
        
        //工作流审核
        workFlowSup = function(id) {
        	var selectRow = viewModel.gridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) { 
        		if(pageId == "m1"){
            		viewModel.WorkFlow(id);
            	}else{
                	var btnData = viewModel.btnDataTable.getSimpleData();
                	var btn = getBtnMsgById(btnData,id);
                	csof.opinionModal('审核意见',pageId+"-opinionConfirmSureId",pageId+"-opinionConfirmCancelCla",pageId);
                	$("#"+pageId+"-op_name").html(btn.DISPLAY_TITLE);
                	$("#"+pageId+"-opinion").val("同意！");
                    $("#"+pageId+"-opinionConfirmSureId").on('click',function(){
                    	if($("#"+pageId+"-opinion").val() != "" && $("#"+pageId+"-opinion").val() != null){
                    		viewModel.WorkFlow(id,$("#"+pageId+"-opinion").val());
                        	$('#'+pageId+'-opinion-modal').modal('hide');
                            $('#'+pageId+'-opinion-modal').remove();
                    	}else{
                    		ip.ipInfoJump("请输入意见！", 'info');
                    	}
                    });
                    $('.'+pageId+'-opinionConfirmCancelCla').on('click',function(){
                    	$('#'+pageId+'-opinion-modal').modal('hide');
                        $('#'+pageId+'-opinion-modal').remove();
                    });
            	}
        	}else {
        		ip.ipInfoJump("请勾选需要操作的监管内容", 'info');
        	}
        }
        
        //修改
        publicModify = function(id) {
        	var selectRow = viewModel.gridViewModel.gridData.getSelectedRows();
			if(selectRow.length == 1){
				$('#dataInputModal').modal({
                	show : true,
                	backdrop : 'static'
                });
				postAction = "dataUpdate";
				var btnData = viewModel.btnDataTable.getSimpleData();
	        	for(var i=0;i<btnData.length;i++) {
	        		var btnId = btnData[i].BUTTON_ID;
	        		if(btnData[i].DISPLAY_STATUS == "2") {
	        			$("#"+btnId).removeAttr("disabled","disabled");
	        		}
	        	}
				var ID = selectRow[0].data.ID.value;
				pageData.ID = ID;
				var oid = selectRow[0].data.OID.value;
				var dep_id = selectRow[0].data.AGENCY_ID.value;
				var dep_code = selectRow[0].data.AGENCY_CODE.value;
        		viewModel.navShow(ID,"false",dep_id,dep_code); 
			}else{
				$("#panel-details").hide();
        		ip.ipInfoJump("请选择一个需要操作的事项", 'info');
			}
        }
        
        //删除
        publicDelete = function(id) {
        	var selectRow = viewModel.gridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) {
        		for(var j=0;j<selectRow.length;j++) {
        			var entity_id = selectRow[j].data.ID.value;        	        	
                	var treeObj = $.fn.zTree.getZTreeObj("depTree");
                	var nodes = treeObj.getSelectedNodes();
                	if(nodes.length > 0) {
                		var selectNodeId = nodes.CHR_ID;
                		var type = nodes[0].TYPE;
               		    var code = nodes[0].CHR_CODE;
               		    var oid = nodes[0].OID;
                    }else {
                    	var code = selectRow[j].data.AGENCY_CODE.value;
                    	var oid = selectRow[j].data.OID.value;  
                    	var selectNodeId = "";
                    	var type = "";
                    }
                	$.ajax({
                        type: 'get',
                        url: deleteListURL + "?tokenid=" + tokenid + "&ajax=noCache",
                        data: {"id":entity_id},
                        dataType: 'JSON',
                        async: false,
                        success: function (result) {
                        	var btnData = viewModel.btnDataTable.getSimpleData();
                        	for(var i=0;i<btnData.length;i++) {
                        		var btnId = btnData[i].BUTTON_ID;
                        		if(btnData[i].DISPLAY_STATUS == "0" || (btnData[i].DISPLAY_STATUS == "9" && btnData[i].ENABLED == "1")) {
                        			$("#"+btnId).removeAttr("disabled","disabled");
                        		}else {
                        			$("#"+btnId).attr("disabled","disabled");
                        		}
                        	}
                            var status = $("input[name='submitStatus'][checked]").val(); 
                            var radio =document.getElementsByName("submitStatus");
                     	    var status = null;
                     	    for(var i =0;i < radio.length;i++){
            	 	        	 if(radio[i].checked){
            	 	        	     status = radio[i].value;
            	 	        	 }
                     	    }
                            viewModel.refresh(selectNodeId,type,code,oid,status);
                            ip.ipInfoJump('删除成功！', 'success');
                        }, error: function () {
                            ip.ipInfoJump("错误", 'error');
                        }
                    });
        		}
        	}else {
        		ip.ipInfoJump("请勾选需要操作的监管内容", 'info');
        	}
        }    
        
        //刷新
        publicRefresh = function() {
        	location.reload() ;
        }
        
        //报表导出导入
        repExport = function(id) {
//        	$("#details-li").after('<li id="rep-li" class="active"><a data-toggle="tab" style="height: 33px;line-height: 33px;padding-top: 0;padding-bottom: 0;" href="#panel-reps" id="reps-li-a">导入导出</a></li>');
//        	$("#panel-details").after('<div class="tab-pane active" id="panel-reps"> <iframe id="repPage2" style="width:100%;height:486px;border: 0;"></iframe></div>');
        	$("#repPage")[0].src = "";
//        	$("#details-li").removeClass("active");
//    		$("#list-li").removeClass("active");
//    		$("#panel-list").removeClass("active");
//    		$("#panel-details").removeClass("active");
//    		$("#panel-details").hide();
        	var treeObj = $.fn.zTree.getZTreeObj("depTree");
        	var nodes = treeObj.getSelectedNodes();
        	if(nodes.length > 0) {
        		var rId = $("#detailIrptNav .active").attr("id");
				var chrId = rId.split("_")[0];
				var data = {
						"chrId"	:chrId,
						"obj_id" :nodes[0].CHR_CODE,
						"param_add_str":"",
						"bbq_date":bbq_date,
						"readonly":"false",
					};
				$("#repEaIModal").modal({
                	show : true,
                	backdrop : 'static'
                });
            	$("#repPage").attr("src", getReportURL + "?tokenid=" +  ip.getTokenId() + "&ajax=noCache" + "&chrId="+chrId+"&obj_id="+nodes[0].CHR_CODE+"&param_add_str=" +""+"&bbq_date="+bbq_date+"&readonly="+"false");
//					$.ajax({
//				        type: 'post',
//				        url: getReportURL + "?tokenid=" + tokenid + "&ajax=noCache",
//				        data: data,
//				        dataType: 'JSON',
//				        async: false,
//				        success: function (result) {
//				            if (result.errorCode == 0) {
////				            	$("#"+rid+"Page")[0].src = result.data;
//				            	$("#repEaIModal").modal('show');
//				            	$("#repPage")[0].src = result.data.split("&userid")[0];
//				            	//$("#repPage2")[0].src = result.data.split("&userid")[0];
//				            } else {
//				                ip.ipInfoJump("错误", 'error');
//				            }
//				        }, error: function () {
//				            ip.ipInfoJump("错误", 'error');
//				        }
//				    });
        	}
        }
        
        //跳转流程界面
        viewModel.skipPage = function() {
        	location.href="flow.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +"&menuname=" + options.svMenuName + "&sid=" + sid +"&task_id=" + task_id +"&task_name=" +task_name;
        }
        
        //高级搜索栏
        hignSearch = function () {
        	$("#hignSearch").show();
        }
        findAll = function() {
        	$("#hignSearch").hide();
        }
        var repEaIModalmodalheight,repEaIModalmodalwidth,repEaIModaloffsetH,repEaIModaloffsetL;
        showBigRepEaIModal = function() {
        	if($("#repEaIModal .modal-content").offset().left == 0){
        		$("#repEaIModal .modal-content").css("height",repEaIModalmodalheight);     
    	        $("#repEaIModal .modal-content").css("width",repEaIModalmodalwidth);
    	        $("#repEaIModal .modal-content").offset({ top: repEaIModaloffsetH, left: repEaIModaloffsetL });
        	}else{
        		repEaIModalmodalheight = $("#repEaIModal .modal-content").height();
        		repEaIModalmodalwidth = $("#repEaIModal .modal-content").width();
        		repEaIModaloffsetH = $("#repEaIModal .modal-content").offset().top;
        		repEaIModaloffsetL = $("#repEaIModal .modal-content").offset().left
        		$("#repEaIModal .modal-content").css("height",winHeight);     
    	        $("#repEaIModal .modal-content").css("width",winWidth);
    	        $("#repEaIModal .modal-content").offset({ top: 0, left: 0 });
        	}
        }
        closeRepEaIModal = function(){
        	$("#repEaIModal .modal-content").css("height",repEaIModalmodalheight);     
	        $("#repEaIModal .modal-content").css("width",repEaIModalmodalwidth);
	        $("#repEaIModal .modal-content").offset({ top: repEaIModaloffsetH, left: repEaIModaloffsetL });
        }
        var dataInputModalmodalheight,dataInputModalmodalwidth,dataInputModaloffsetH,dataInputModaloffsetL;
        showBigdataInputModal = function() {
        	if($("#dataInputModal .modal-content").offset().left == 0){
        		$("#dataInputModal .modal-content").css("height",dataInputModalmodalheight);     
    	        $("#dataInputModal .modal-content").css("width",dataInputModalmodalwidth);
    	        $("#dataInputModal .modal-content").offset({ top: dataInputModaloffsetH, left: dataInputModaloffsetL });
        	}else{
        		dataInputModalmodalheight = $("#dataInputModal .modal-content").height();
        		dataInputModalmodalwidth = $("#dataInputModal .modal-content").width();
        		dataInputModaloffsetH = $("#dataInputModal .modal-content").offset().top;
        		dataInputModaloffsetL = $("#dataInputModal .modal-content").offset().left
        		$("#dataInputModal .modal-content").css("height",winHeight);     
    	        $("#dataInputModal .modal-content").css("width",winWidth);
    	        $("#dataInputModal .modal-content").offset({ top: 0, left: 0 });
        	}
        }
        closedataInputModal = function(){
        	$("#dataInputModal .modal-content").css("height",dataInputModalmodalheight);     
	        $("#dataInputModal .modal-content").css("width",dataInputModalmodalwidth);
	        $("#dataInputModal .modal-content").offset({ top: dataInputModaloffsetH, left: dataInputModaloffsetL });
        }
        setInterval(csscsof.windowHeightNoNav(), 500)//每半秒执行一次windowHeight函数

        //初始化
        function init() {
        	app = u.createApp({
                el: document.body,
                model: viewModel
            });
            var url=window.location.href;
            pageId = url.split("pageId=")[1].split("&")[0];
            eleCode = url.split("elecode=")[1].split("&")[0];
            billTypeCode =  url.split("billTypeCode=")[1].split("&")[0];
            sid = url.split("SID=")[1].split("&")[0];
            IS_ALLOBJ = url.split("IS_ALLOBJ=")[1].split("&")[0];
            OBJ_TYPE_ID = url.split("OBJ_TYPE_ID=")[1].split("&")[0];
            bbq_date = url.split("BBQ_DATE=")[1].split("&")[0];
            if(bbq_date == "" || bbq_date == null || bbq_date == "undefined"){
            	var nowDate = new Date();
            	 var year = nowDate.getFullYear();
            	 var month = nowDate.getMonth() + 1 < 10 ? "0" + (nowDate.getMonth() + 1) : nowDate.getMonth() + 1;
            	 var day = nowDate.getDate() < 10 ? "0" + nowDate.getDate() : nowDate.getDate();
            	 var dateStr = year.toString() + month.toString() + day;
            	 bbq_date = dateStr;
            }
            task_id = url.split("TASK_ID=")[1].split("&")[0];
            task_name = decodeURI(url.split("TASK_NAME=")[1].split("&")[0]);
            tokenid = ip.getTokenId();
            options = ip.getCommonOptions({});
            options['tokenid'] = tokenid;
            winHeight = $(window).height();
            winWidth = $(window).width();
            viewModel.initStatus(pageId); //初始化状态框
            var data = initBtns(pageId);//初始化按钮     
    		if(data == false){
    			ip.ipInfoJump("加载按钮出错", 'error');
    		}else{
    			viewModel.btnDataTable.setSimpleData(data);
    		}
    		for(var i=0;i<data.length;i++) {
    			if(data[i].ENABLED == "0" || data[i].DISPLAY_STATUS == "2") {
    				$("#"+data[i].BUTTON_ID).attr("disabled","disabled");
    			}
    		}
            viewModel.initDropDown();//初始化下拉框
            viewModel.initData("","","","","9");//初始化grid
            pageData.ininReportNav();//初始化页签
            csscsof.windowHeightNoNav();
        }
        init();
    }
)