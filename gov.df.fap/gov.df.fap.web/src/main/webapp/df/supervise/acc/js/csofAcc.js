/**
 * Created by yanqiong on 2017/8/8.
 */
require(
		[ 'jquery', 'knockout', '/df/supervise/ncrd.js','csscsof', 'bootstrap', 'dateZH',
				'uui', 'tree', 'grid', 'ip' ,'csof'],
		function($, ko, ncrd, csscsof) {
			window.ko = ko;
			var tokenid,
			    options,
			    fileData,
			    elecode,
			    menuId,
			    menuName,
			    DEP_ID,
			    OID,
			    ACC_ID,
			    BOOK_ID,
			    BOOK_NAME,
			    SET_YEAR,
			    SET_MONTH,
			    comboData;
			
			var pViewType = {
					VIEWTYPE_INPUT : "001",// 录入视图
					VIEWTYPE_LIST : "002",// 列表视图
					VIEWTYPE_QUERY : "003"// 查询视图
				};
			var saveBtnclick = false;
			var num = 0;
			var comboDataHtml = $('#EXECUTOR-w').html();

			//URL
			var testURL = '/df/test/btns.do';//测试
			var depTreeURL = '/df/csofacc/getSupType.do';//左侧下拉框
			var leftList = '/df/csofacc/getsupname.do';//左侧列表
			var leftListById = '/df/csofacc/getsupnameById.do';//通过sup_type_id过滤左侧列表
			var addExURL = '/df/csofacc/save.do';//事项新增
			var getExURL = '/df/csofacc/get.do';//事项查询
			var updateExURL = '/df/csofacc/update.do';//事项更新
			var deleteExURL = '/df/csofacc/delete.do';//事项删除 
			var workAddURL = '/df/csofacc/worksave.do';//工作记录新增
			var workGetURL = '/df/csofacc/workget.do';//工作记录查询
			var workUpdateURL = '/df/csofacc/workupdate.do';//工作记录更新
			var workDeleteURL = '/df/csofacc/workdelete.do';//工作记录删除
			var problemAddURL = '/df/csofacc/problemsave.do';//问题记录新增
			var problemGetURL = '/df/csofacc/problemget.do';//问题记录查询
			var problemUpdateURL = '/df/csofacc/problemupdate.do';//问题记录更新
			var problemDeleteURL = '/df/csofacc/problemdelete.do';//问题记录删除
			var getTaskUserURL = '/df/csofacc/getTaskUser.do';//查询未过账数据
			var saveTaskUserURL = '/df/csofacc/saveTaskUser.do';//过账
			var getCsofAccURL = '/df/csofacc/getCsofAcc.do';//得到台账类型
			var getCsofAccByIdURL = '/df/csofacc/getSupTreeByAccId.do';//通过acc_id过滤左侧列表
			var getUserNameByDepIdURL = '/df/csofacc/getUserNameByDepId.do';//通过dep_id获取当前处室相关人员
			
			
			var viewModel = {
					SearchdepTreeKEY: ko.observable(""),
	        		//模糊查询单位树
	                searchDepTree : function () {
	                	 ncrd.findTreeNode($.fn.zTree.getZTreeObj("depTree"), viewModel.SearchdepTreeKEY());    
	                },  
				//问题GridDataTable
				questionDataTable: new u.DataTable({
			        meta: {
			            
			        }
			    }),
			    //修改问题GridDataTable
			    formDataTable: new u.DataTable({
			        meta: {
			           
			        }
			    }),
			    //工作GridDataTable
				workDataTable: new u.DataTable({
			        meta: {
			            
			        }
			    }),
			    //修改工作GridDataTable
			    workFormDataTable: new u.DataTable({
			        meta: {
			           
			        }
			    }),
			    //录入视图表格
			    inputViewDataTable: new u.DataTable({
			        meta: {
			           
			        }
			    }),
			    //事项新增
			    exDataTable: new u.DataTable({
			    	meta: {
			           
			        }
			    }),
			  //事项新增
			    exDataTable1: new u.DataTable({
			    	meta: {
			           
			        }
			    }),
			  //事项查询
			    exDataTable2: new u.DataTable({
			    	meta: {
			           
			        }
			    }),
			    //按钮
			    btnDataTable: new u.DataTable({
			    	meta: {
			           
			        }
			    }),
			    depTreeSetting: {
	                view:{
	                    showLine: false,
	                    selectedMulti: false,
	                    showIcon: false,
	                    showTitle: true
	                },
	                callback:{
	                    onClick:function(e,id,node){
	                    	var btnData = viewModel.btnDataTable.getSimpleData();
	                        var selectNodeId = node.CHR_ID;
	                        var type = node.TYPE_CODE;
	                        if(type == "SUP"){
	                        	for(var i=0;i<btnData.length;i++){
	                        		var btnId = btnData[i].BUTTON_ID;
	                        		$("#"+btnId).removeAttr("disabled","disabled");
	                        	}
	                        }else{
	                        	for(var i=0;i<btnData.length;i++){
	                        		var btnId = btnData[i].BUTTON_ID;
	                        		if(btnData[i].DISPLAY_STATUS == "9" || btnData[i].DISPLAY_STATUS == "0"){
	                        			$("#"+btnId).removeAttr("disabled","disabled");
	                        		}else{
	                        			$("#"+btnId).attr("disabled","disabled");
	                        		}	                        		
	                        	}
	                        }
	               		    var opretion = $('#righ-tab-ul li[class="active"] a')[0].innerText;
	               		    if(type == "SUP"){
	               		    	viewModel.getEx(selectNodeId,viewModel.exDataTable1);
	               		    }
	               		    if(opretion == "事项详情"){
	               		    	var exShow = viewModel.exDataTable1.getSimpleData();
			            		for(var i=0;i<exShow.length;i++){
			            			$.each(exShow, function(index, item){
			            				$("#"+item.id).val("");
		        					})
			            		}
	               		    }else if(opretion == "工作记录"){
	               		    	if(type == "SUP"){
	               		    		if($("#workRecodeGrid")[0].innerHTML==""){
	    		    					viewModel.initData("", "workRecodeGrid",pViewType.VIEWTYPE_LIST,"0");
	    		    				}else{
	    		    					viewModel.refresh("", "workRecodeGrid",pViewType.VIEWTYPE_LIST,"0");
	    		    				}
		               		    }
	               		    }else if(opretion == "问题记录"){
	               		    	if(type == "SUP"){
		               		    	if($("#questionRecodeGrid")[0].innerHTML==""){
		            					viewModel.initData("", "questionRecodeGrid",pViewType.VIEWTYPE_LIST,"1");
		            				}else{
		            					viewModel.refresh("", "questionRecodeGrid",pViewType.VIEWTYPE_LIST,"1");
		            				}
		               		    }
	               		    }else if(opretion == "附录"){
	               		    	if(type == "SUP"){
	               		    		$("#fileUploadPage")[0].src = "";
	               		    		fileData = {
            		    					"entity_id":selectNodeId,
            		    					"oid":options.svOfficeId,
            		    					"dep_id":options.svDepId,
            		    					"dep_code":options.svDepCode,
            		    					"edit":'N',
            		    			    }
            		    				$("#fileUploadPage")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
            		    		    	"&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&entityName=csof_sup_bill&oid="+fileData.oid+
            		    		    	"&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit;
		               		    }
	               		    }
	                    }
	                }
	            },
	            depTreeDataTable: new u.DataTable({
	                meta: {
	                	
	                }
	            }),
			    
			    //删除操作
			    del: function(flag,rowId) {
			    	if(flag == 0){
			    		var DataTable = viewModel.workAddGridViewModel;
			    		var opType = "work";
			    	}else if(flag == 1){
			    		var DataTable = viewModel.questionAddGridViewModel;
			    		var opType = "question";
			    	}else if(flag == 2){
			    		var DataTable = viewModel.workGridViewModel;
			    		var opType = "work";
			    	}else if(flag == 3){
			    		var DataTable = viewModel.questionGridViewModel;
			    		var opType = "question";
			    	}
			    	viewModel.rowId = rowId;
			    	var datatableRow = DataTable.gridData.getRowByRowId(rowId);
			        viewModel.delOpertion(opType,DataTable,datatableRow.data.ID.value);
			    },
				//
				beforeEdit : function(flag,rowId, s, e) {
					var treeObj = $.fn.zTree.getZTreeObj("depTree");
		        	var nodes = treeObj.getSelectedNodes();
		        	if(nodes.length > 0) {
		        		var selectNodeId = nodes[0].CHR_ID;
		        		$('#DISCOVER_PROBLEMS').css('border-color', '#ccc');
						$('#AUDIT_BASIS').css('border-color', '#ccc');
						$('#AUDIT_OPINION').css('border-color', '#ccc');
		        		if(flag == 3){
				    		var DataTable = viewModel.questionGridViewModel;
				    		var formDataTable = viewModel.formDataTable;
				    		var addModel = $("#questionAddModal");
				    		$("#updateQuestionBtn").hide();
				    		$("#saveQuestionBtn").show();
				    		$("#questionAddModalLabel").html("问题记录新增");
				    	}
		        		var self = this;
						viewModel.rowId = rowId;
						if (rowId && rowId != -1) {
							var datatableRow = DataTable.gridData.getRowByRowId(rowId);
							//修改操作
							if(flag == 3){
					    		$("#updateQuestionBtn").show();
					    		$("#saveQuestionBtn").hide();
					    		$("#questionAddModalLabel").html("问题记录修改");
					    		$('#DISCOVER_PROBLEMS').val(datatableRow.data.DISCOVER_PROBLEMS.value);
					    		$('#AUDIT_BASIS').val(datatableRow.data.AUDIT_BASIS.value);
					    		$('#AUDIT_OPINION').val(datatableRow.data.AUDIT_OPINION.value);
					    		$('#HANDLE').val(datatableRow.data.HANDLE.value);
					    		$('#REMARK-q').val(datatableRow.data.REMARK.value);
							}
						}
						//显示模态框 
						addModel.modal({
		                	show : true,
		                	backdrop : 'static'
		                });
						u.stopEvent(e);
		        	}else{
		        		ip.ipInfoJump("请选择需要登记的事项", 'info');
		        	}
				},
				saveQuestion : function(data, events) {
					viewModel.edit(3,viewModel.rowId);
					$('#questionAddModal').modal('toggle');
				},
				saveAddQuestion : function(data, events) {
					viewModel.edit(1,viewModel.rowId);
					$('#questionAddModal').modal('toggle');
				},
				modelClose : function(data, events) {
					$('#questionAddModal').modal('toggle');
				},
				saveWork : function(data, events) {
					viewModel.edit(2,viewModel.rowId);
					$('#workAddModal').modal('toggle');
				},
				saveAddWork : function(data, events) {
					viewModel.edit(0,viewModel.rowId);
					$('#workAddModal').modal('toggle');
				},
				workModelClose : function(data, events) {
					$('#workAddModal').modal('toggle');
				},
				//将操作后的数据进行保存
			    edit: function(flag,rowId) {
			    	if(flag == 0){
			    		var DataTable = viewModel.workAddGridViewModel;
			    		var formDataTable = viewModel.workFormDataTable;
			    		var addModel = $("#workAddModal");
			    		$("#saveWorkBtn").hide();
			    		$("#saveAddWorkBtn").show();
			    	}else if(flag == 1){
			    		var DataTable = viewModel.questionAddGridViewModel;
			    		var formDataTable = viewModel.formDataTable;
			    		var addModel = $("#questionAddModal");
			    		$("#saveQuestionBtn").hide();
			    		$("#saveAddQuestionBtn").show();
			    	}else if(flag == 2){
			    		var DataTable = viewModel.workGridViewModel;
			    		var formDataTable = viewModel.workFormDataTable;
			    		var addModel = $("#workAddModal");
			    		$("#saveWorkBtn").show();
			    		$("#saveAddWorkBtn").hide();
			    	}else if(flag == 3){
			    		var DataTable = viewModel.questionGridViewModel;
			    		var formDataTable = viewModel.formDataTable;
			    		var addModel = $("#questionAddModal");
			    		$("#saveQuestionBtn").show();
			    		$("#saveAddQuestionBtn").hide();
			    	}
			        //更改后台数据

			        var currentRow;
			        //如果index大于等于0说明是修改
			        if (rowId && rowId != -1) {
			            //获取需要修改的行
			            currentRow = DataTable.gridData.getRowByRowId(rowId);
			            //将用户填写的数据更新到listDataTable上
			            currentRow.setSimpleData(formDataTable.getSimpleData()[0]);
			        } else {
			            //添加数据
			        	DataTable.gridData.addSimpleData(formDataTable.getSimpleData()[0]);
			        }

			    },
			    getIdLast: function(data){//将{***}的***提取出来
		        	var par = /[{}]/g;
		        	var text = data.replace(par,'');
		        	return text;
		        },
			};
			
			//定义操作列的内容
			questionGrid = function(obj) {
		        var dataTableRowId = obj.row.value['$_#_@_id'];
		        var delfun = "data-bind=click:del.bind($data,1," + dataTableRowId + ")";
		        var editfun = "data-bind=click:beforeEdit.bind($data,1," + dataTableRowId + ")";
		        obj.element.innerHTML = '<div><i class="op uf uf-pencil" title="修改"' + editfun + '></i>' + '<i class=" op icon uf uf-del title="删除" ' + delfun + '></i></div>';
		        ko.applyBindings(viewModel, obj.element);
		    };
		    
		    workGrid = function(obj) {
		        var dataTableRowId = obj.row.value['$_#_@_id'];
		        var delfun = "data-bind=click:del.bind($data,0," + dataTableRowId + ")";
//		        var editfun = "data-bind=click:beforeEdit.bind($data,0," + dataTableRowId + ")";
//		        obj.element.innerHTML = '<div><i class="op uf uf-pencil" title="修改"' + editfun + '></i>' + '<i class=" op icon uf uf-del title="删除" ' + delfun + '></i></div>';
		        obj.element.innerHTML = '<div><i class=" op icon uf uf-del title="删除" ' + delfun + '></i></div>';
		        ko.applyBindings(viewModel, obj.element);
		    };
		    
		    workRecodeGrid = function(obj) {
		        var dataTableRowId = obj.row.value['$_#_@_id'];
		        var delfun = "data-bind=click:del.bind($data,2," + dataTableRowId + ")";
//		        var editfun = "data-bind=click:beforeEdit.bind($data,2," + dataTableRowId + ")";
//		        obj.element.innerHTML = '<div><i class="op uf uf-pencil" title="修改"' + editfun + '></i>' + '<i class=" op icon uf uf-del title="删除" ' + delfun + '></i></div>';
		        obj.element.innerHTML = '<div><i class=" op icon uf uf-del title="删除" ' + delfun + '></i></div>';
		        ko.applyBindings(viewModel, obj.element);
		    };
		    
		    questionRecodeGrid = function(obj) {
		        var dataTableRowId = obj.row.value['$_#_@_id'];
		        var delfun = "data-bind=click:del.bind($data,3," + dataTableRowId + ")";
		        var editfun = "data-bind=click:beforeEdit.bind($data,3," + dataTableRowId + ")";
		        obj.element.innerHTML = '<div><i class="op uf uf-pencil" title="修改"' + editfun + '></i>' + '<i class=" op icon uf uf-del title="删除" ' + delfun + '></i></div>';
		        ko.applyBindings(viewModel, obj.element);
		    };		    
			//生成表头
			viewModel.initData = function(areaType, areaId,viewType,order) {
				var current_url = location.search;
				$.ajax({
					url : "/df/init/initMsg.do",
					type : "GET",
					dataType : "json",
					async : false,
					data : options,
					success : function(datas) {
						viewModel.viewList = datas.viewlist;// 视图信息
						viewModel.resList = datas.reslist;// 资源信息
						viewModel.coaId = datas.coaId;// coaid
						viewModel.coaDetails = datas.coaDetail;// coa明细
						viewModel.relations = datas.relationlist;// 关联关系
						viewModel.initGridData(areaType, areaId,viewType,order); //调用初始化表格
					}
				});
			};
			
			 // 初始化表格
	    	viewModel.initGridData = function(areaType, areaId,viewType,order) {
	            var queryViewId;
	            var tableViewDetail;
	            var queryViewDetail;
	            for ( var n = 0; n < viewModel.viewList.length; n++) {
	                var view = viewModel.viewList[n];
	                if (view.viewtype == viewType) {
	                	if(viewType == pViewType.VIEWTYPE_INPUT && view.orders == order){
	                		var viewid = viewModel.getIdLast(view.viewid);
	                		if(order == '0'){
	                			var aims = ip.initArea(view.viewDetail,areaType,viewid, areaId);
	                			var dep_array = [];
	                			for(var i=0;i<aims.length;i++){
	                				var rid = aims[i].id.split("-")[0];
	                				var depArray = {
	    	                                "id": aims[i].id,
	    	                                "rid": rid,
	    	                                "type": aims[i].type,
	    	                                "source":aims[i].source,
	    	                            }
	    	            			dep_array.push(depArray);
	                			}
	                			viewModel.exDataTable1.setSimpleData(dep_array);
	                        }else if(order == '1'){
	                        	var aims2 = ip.initArea(view.viewDetail,areaType,viewid, areaId);
	                        	viewModel.inputViewDataTable.setSimpleData(aims2);
	                        	var dep_array = [];
	                			for(var i=0;i<aims2.length;i++){
	                				var rid = aims2[i].id.split("-")[0];
	                				var depArray = {
	    	                                "id": aims2[i].id,
	    	                                "rid": rid,
	    	                                "type": aims2[i].type,
	    	                                "source":aims2[i].source,
	    	                            }
	    	            			dep_array.push(depArray);
	                			}
	                			viewModel.exDataTable2.setSimpleData(dep_array);
	                        }
	     	           }else if(viewType == pViewType.VIEWTYPE_LIST && view.orders == order){
	     	        	    viewModel.tableViewDetail = view;
	     	        	    options["menu_id"] = menuId;
	     	        	    var treeObj = $.fn.zTree.getZTreeObj("depTree");
	     		        	var nodes = treeObj.getSelectedNodes();
	     		        	if(nodes.length > 0) {
	     		        		var selectNodeId = nodes[0].CHR_ID;
	     		        	}else{
	     		        		var selectNodeId = "";
	     		        	}
	     	        	    options["entity_id"] = selectNodeId;
	                        payViewId = view.viewid;
	                        if(order == '0'){
		                        viewModel.workGridViewModel = ip.initGridWithCallBack(workgridCallback,viewModel.tableViewDetail, areaId, "/df/csofacc/workget.do",options, 1, true,false,false,false);
		                        if(viewModel.workGridViewModel.gridData.getSimpleData()){
			                        $("#workGridSumNum").html(viewModel.workGridViewModel.gridData.getSimpleData().length);
		                        }
	                        }else if(order == '1'){
		                        viewModel.questionGridViewModel = ip.initGridWithCallBack(questiongridCallback,viewModel.tableViewDetail, areaId, "/df/csofacc/problemget.do",options, 1, true,false,false,false);
		                        if(viewModel.questionGridViewModel.gridData.getSimpleData()){
			                        $("#questionGridSumNum").html(viewModel.questionGridViewModel.gridData.getSimpleData().length);
		                        }
	                        }
//	                        else if(order == '2'){
//	                        	viewModel.exGridViewModel = ip.initGrid(viewModel.tableViewDetail, areaId, testURL, options, 1, false, false, false, true);
//	                        }
	                        else if(order == '3'){
	                        	    options["user_id"] = options.svUserId;
	                        	    options["oid"] = options.svOfficeId;
	                        	viewModel.postGridViewModel = ip.initGrid(viewModel.tableViewDetail, areaId, getTaskUserURL, options, 1, false, true, false, true);
	                        }
	     	           }else if(viewType == pViewType.VIEWTYPE_QUERY) {
	                    	
	     	           }
	                }
	            }
	        };
	        
	        //workgrid查询
	        viewModel.workGridSearch = function () {
	        	ip.fuzzyQuery(viewModel.curGridData, "workGridSearchInput", viewModel.workGridViewModel)
	        };
	      //questiongrid查询
	        viewModel.questionGridSearch = function () {
	        	ip.fuzzyQuery(viewModel.curGridData, "questionGridSearchInput", viewModel.questionGridViewModel)
	        };
	        
	        //回调函数，定义全局变量viewModel.curGridData并赋值
	        workgridCallback = function(data){
	        	viewModel.curGridData = data;
	        };
	        //回调函数，定义全局变量viewModel.curGridData并赋值
	        questiongridCallback = function(data){
	        	viewModel.curGridData = data;
	        };
	        
	        //刷新视图
	        viewModel.refresh = function(areaType, areaId,viewType,order) {
	        	var queryViewId;
	            var tableViewDetail;
	            var queryViewDetail;
	            for ( var n = 0; n < viewModel.viewList.length; n++) {
	                var view = viewModel.viewList[n];
	                if(viewType == pViewType.VIEWTYPE_LIST && view.orders == order){
     	        	    viewModel.tableViewDetail = view;
     	        	    options["menu_id"] = menuId;
     	        	    var treeObj = $.fn.zTree.getZTreeObj("depTree");
     		        	var nodes = treeObj.getSelectedNodes();
     		        	if(nodes.length > 0) {
     		        		var selectNodeId = nodes[0].CHR_ID;
     		        	}else{
     		        		var selectNodeId = "";
     		        	}
     	        	    options["entity_id"] = selectNodeId;
                        payViewId = view.viewid;
                        if(order == '0'){
	                        ip.setGridWithCallBack(workgridCallback,viewModel.workGridViewModel,"/df/csofacc/workget.do",options);
	                        $("#workGridSumNum").html(viewModel.workGridViewModel.gridData.getSimpleData().length);
                        }else if(order == '1'){
	                        ip.setGridWithCallBack(questiongridCallback,viewModel.questionGridViewModel,"/df/csofacc/problemget.do",options);
	                        $("#questionGridSumNum").html(viewModel.questionGridViewModel.gridData.getSimpleData().length);
                        }
//                        else if(order == '2'){
//                        	ip.setGrid(viewModel.exGridViewModel, "exGrid", testURL, options);
//                        }
                        else if(order == '3'){
                        	ip.setGrid(viewModel.postGridViewModel, "postExGrid", testURL, options);
                        }
     	           }
	            }
	        };
	        
	        //点击工作记录exDetails-li workRecode-li questionRecode-li feedback-li file-li
			$('#exDetails-li').on('click' , function() {   
				$("#workGridSearchDiv").hide();
				$("#questionGridSearchDiv").hide();
				$("#testGrid").html("");
				viewModel.initData("edit", "testGrid",pViewType.VIEWTYPE_INPUT,"0");
				var treeObj = $.fn.zTree.getZTreeObj("depTree");
				var nodes = treeObj.getSelectedNodes();
	        	if(nodes.length > 0) {
	        		var selectNodeId = nodes[0].CHR_ID;
	        		viewModel.getEx(selectNodeId,viewModel.exDataTable1);
	   		    	if($("#questionRecodeGrid")[0].innerHTML==""){
						viewModel.initData("", "questionRecodeGrid",pViewType.VIEWTYPE_LIST,"1");
					}else{
						viewModel.refresh("", "questionRecodeGrid",pViewType.VIEWTYPE_LIST,"1");
					}
	        	}	
			});
			$('#workRecode-li').on('click' , function() {    
				$("#workGridSearchDiv").show();
				$("#workGridSearchInput").val("");
				$("#questionGridSearchDiv").hide();
				if($("#workRecodeGrid")[0].innerHTML==""){
					viewModel.initData("", "workRecodeGrid",pViewType.VIEWTYPE_LIST,"0");
				}else{
					viewModel.refresh("", "workRecodeGrid",pViewType.VIEWTYPE_LIST,"0");
				}
			});
			$('#questionRecode-li').on('click' , function() {      
				$("#workGridSearchDiv").hide();
				$("#questionGridSearchDiv").show();
				$("#questionGridSearchInput").val("");
				if($("#questionRecodeGrid")[0].innerHTML==""){
					viewModel.initData("", "questionRecodeGrid",pViewType.VIEWTYPE_LIST,"1");
				}else{
					viewModel.refresh("", "questionRecodeGrid",pViewType.VIEWTYPE_LIST,"1");
				}
			});
			$('#file-li').on('click' , function() {    
				$("#workGridSearchDiv").hide();
				$("#questionGridSearchDiv").hide();
				$("#fileUploadPage")[0].src = "";
				var treeObj = $.fn.zTree.getZTreeObj("depTree");
	        	var nodes = treeObj.getSelectedNodes();
	        	if(nodes.length > 0) {
	        		if(nodes[0].TYPE_CODE == "SUP"){
	        			var selectNodeId = nodes[0].CHR_ID;
		        		fileData = {
		    					"entity_id":selectNodeId,
		    					"oid":options.svOfficeId,
		    					"dep_id":options.svDepId,
		    					"dep_code":options.svDepCode,
		    					"edit":'N',
		    			    }
		    				$("#fileUploadPage")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
		    		    	"&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&entityName=csof_sup_bill&oid="+fileData.oid+
		    		    	"&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit;
	        		}else{
	        			fileData = {
		    					"entity_id":"",
		    					"oid":options.svOfficeId,
		    					"dep_id":options.svDepId,
		    					"dep_code":options.svDepCode,
		    					"edit":'N',
		    			    }
		    				$("#fileUploadPage")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
		    		    	"&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&entityName=csof_sup_bill&oid="+fileData.oid+
		    		    	"&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit;
	        			ip.ipInfoJump("请选择需要登记的事项", 'info');
	        		}
	        	}else{
	        		ip.ipInfoJump("请选择需要登记的事项", 'info');
	        	}
			});
			
			//附件上传完成
			viewModel.closeFile = function(){
				$("#fileUploadPage")[0].src = "";
				var treeObj = $.fn.zTree.getZTreeObj("depTree");
	        	var nodes = treeObj.getSelectedNodes();
	        	if(nodes.length > 0) {
	        		if(nodes[0].TYPE_CODE == "SUP"){
	        			var selectNodeId = nodes[0].CHR_ID;
		        		fileData = {
		    					"entity_id":selectNodeId,
		    					"oid":options.svOfficeId,
		    					"dep_id":options.svDepId,
		    					"dep_code":options.svDepCode,
		    					"edit":'N',
		    			    }
		    				$("#fileUploadPage")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
		    		    	"&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&entityName=csof_sup_bill&oid="+fileData.oid+
		    		    	"&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit;
	        		}else{
	        			ip.ipInfoJump("请选择需要登记的事项", 'info');
	        		}
	        	}else{
	        		ip.ipInfoJump("请选择需要登记的事项", 'info');
	        	}
			}
			
			//初始化下拉框
			viewModel.initSupTypeSelect = function(){
				$("#sup_type-select").html("");
				var objSelect = document.getElementById("sup_type-select");//获得select标签对象
				var new_opt = new Option("全部", "-1"); //直接new一个option对象，然后把text和value值都赋上
                objSelect.options.add(new_opt);
				$.ajax({
		            type: 'get',
		            url: depTreeURL + "?tokenid=" + tokenid + "&ajax=noCache",
		            data: {"ele_code":elecode,"acc_id":ACC_ID},
		            dataType: 'JSON',
		            async: true,
		            success: function (result) {
		            	if(result.errorCode == 0) {
		            		var data = result.data;
		            		for(var i = 0;i<data.length;i++) {
		    	                var new_opt = new Option(data[i].CHR_NAME, data[i].CHR_ID); //直接new一个option对象，然后把text和value值都赋上
		    	                objSelect.options.add(new_opt);
		    	            }
		            	}else {
		            		ip.ipInfoJump(result.errorMsg, 'error');
		            	}
		            }, error: function () {
		            	ip.ipInfoJump("错误", 'error');
		            }		                
	            });
			};
			//初始化左侧树leftList
			viewModel.leftTree = function(){
				$.ajax({
		            type: 'get',
		            url: leftList + "?tokenid=" + tokenid + "&ajax=noCache",
		            data: {"ele_code":elecode,"acc_id":ACC_ID},
		            dataType: 'JSON',
		            async: true,
		            success: function (result) {
		            	if(result.errorCode == 0) {
		            		var data = result.data;
		            		console.log(data);
		            		var dep_array = [];
		            		for(var i=0;i<data.length;i++) {
		            			if(data[i].IS_LEAF == "0") {
		            				data[i].PARENT_ID = "";
		            			}
		            		}
		            		viewModel.depTreeDataTable.setSimpleData(data);
		            		var treeObj = $.fn.zTree.getZTreeObj("depTree");
		                    treeObj.expandAll(true);
		                    treeObj.cancelSelectedNode();
		                    var exShow = viewModel.exDataTable1.getSimpleData();
		                    if(exShow){
		                    	for(var i=0;i<exShow.length;i++){
			            			$.each(exShow, function(index, item){
			            				$("#"+item.id).val("");
		        					})
			            		}
		                    }
		            	}else {
		            		ip.ipInfoJump(result.errorMsg, 'error');
		            	}
		            }, error: function () {
		            	ip.ipInfoJump("错误", 'error');
		            }		                
	            });
			};
			
			//监管类型过滤左侧树
			supTypeChange = function() {
				var chrId = $("#sup_type-select").val();
				if(chrId == "-1"){
					viewModel.leftTree();
				}else{
					$.ajax({
			            type: 'get',
			            url: leftListById + "?tokenid=" + tokenid + "&ajax=noCache",
			            data: {"ele_code":elecode,"chr_id":chrId,"acc_id":ACC_ID},
			            dataType: 'JSON',
			            async: true,
			            success: function (result) {
			            	if(result.errorCode == 0) {
			            		var data = result.data;
			            		console.log(data);
			            		var dep_array = [];
			            		for(var i=0;i<data.length;i++) {
			            			if(data[i].IS_LEAF == "0") {
			            				data[i].PARENT_ID = "";
			            			}
			            		}
			            		viewModel.depTreeDataTable.setSimpleData(data);
			            		var treeObj = $.fn.zTree.getZTreeObj("depTree");
			                    treeObj.expandAll(true);
			                    treeObj.cancelSelectedNode();
			                    var exShow = viewModel.exDataTable1.getSimpleData();
			                    if(exShow){
			                    	for(var i=0;i<exShow.length;i++){
				            			$.each(exShow, function(index, item){
				            				$("#"+item.id).val("");
			        					})
				            		}
			                    }
			            	}else {
			            		ip.ipInfoJump(result.errorMsg, 'error');
			            	}
			            }, error: function () {
			            	ip.ipInfoJump("错误", 'error');
			            }		                
		            });
				}
			};
			
			//新增事项addExURL
			viewModel.addEx = function() {
				if(saveBtnclick){
					ip.ipInfoJump("保存按钮已被点击，请稍等。", 'error');
				}else{
					saveBtnclick = true;
					var aim2 = viewModel.inputViewDataTable.getSimpleData();
					var noVerifyRid =["REMARK","SUP_NO"];
					if(verifyInputView(aim2,noVerifyRid)) {
						var accSup = viewModel.exDataTable2.getSimpleData();
						var sup_array = [];
						sup_array[0] = {};
						for(var i=0;i<accSup.length;i++) {
							if(accSup[i].type == "treeassist") {
								var value = $('#'+accSup[i].id+"-h").val();
							}else{
								var value = $('#'+accSup[i].id).val();
							}
							sup_array[0][accSup[i].rid] = value;
						}
						var data = {
							"data" : JSON.stringify(sup_array[0]),
							"billtype_code" : "201",
							"book_id" : BOOK_ID,
							"acc_id" : ACC_ID,
						}
						$.ajax({
				            type: 'get',
				            url: addExURL + "?tokenid=" + tokenid + "&ajax=noCache",
				            data: data,
				            dataType: 'JSON',
				            async: true,
				            success: function (result) {
				            	if(result.errorCode == 0) {
				            		$('#addExModal').modal('toggle');
				    				saveBtnclick = false;
				            		$('#infoAdd').html("");
				            		viewModel.leftTree();
				            		viewModel.clearData();
				            	}else {
				            		ip.ipInfoJump(result.message, 'error');
				            	}
				            }, error: function () {
				            	ip.ipInfoJump("错误", 'error');
				            }		                
			            });
					}else{
						ip.ipInfoJump("请完善红框数据", 'error');
					}
				}
			};
			
			//get事项getExURL
			viewModel.getEx = function(sid,taskData) {
				$.ajax({
		            type: 'get',
		            url: getExURL + "?tokenid=" + tokenid + "&ajax=noCache",
		            data: {"id":sid},
		            dataType: 'JSON',
		            async: true,
		            success: function (result) {
		            	if(result.errorCode == 0) {
		            		console.log(result.data);
		            		var data = result.data;
		            		var exShow = taskData.getSimpleData();
		            		if(exShow){
		            			for(var i=0;i<exShow.length;i++){
			            			$.each(data, function(key, value){
		        						if(key === exShow[i].rid){
		        							$("#"+exShow[i].id).val(value);
		        							if(exShow[i].type == "treeassist"){
				            					var all_options = {
				            							"element": exShow[i].source,
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
				            								console.log(map.eleDetail);
				            								for(var j=0;j<map.eleDetail.length;j++){
				            									if(map.eleDetail[j].chr_name == value){
				            										$("#"+exShow[i].id+"-h").val(map.eleDetail[j].chr_id + "@" + encodeURI(map.eleDetail[j].chr_name, "utf-8") + "@" + map.eleDetail[j].chr_code + "@" + map.eleDetail[j].pId);
				            									}
				            								}
				            							}
				            						});
				            				}
		        						}
		        					})
			            		}
	            			}
		            		viewModel.exDataTable.setSimpleData(data);
		            	}else {
		            		ip.ipInfoJump(result.message, 'error');
		            	}
		            }, error: function () {
		            	ip.ipInfoJump("错误", 'error');
		            }		                
	            });
			};
			
			//事项修改btn
			viewModel.taskUpdata = function() {
				var treeObj = $.fn.zTree.getZTreeObj("depTree");
	        	var nodes = treeObj.getSelectedNodes();
	        	if(nodes.length > 0 && nodes[0].TYPE_CODE == "SUP") {
	        		var selectNodeId = nodes[0].CHR_ID;
	        		var exData = viewModel.exDataTable.getSimpleData();
	        		if(exData[0].IS_INPUT == 0){
	        			ip.ipInfoJump("过账事项不可修改", 'info');
	        		}else{
	        			$('#addExModal').modal({
		                	show : true,
		                	backdrop : 'static'
		                });
	    				saveBtnclick = false;
						$('#infoAdd').html("");
						viewModel.initData("edit", "infoAdd",pViewType.VIEWTYPE_INPUT,"1");
						$('#savebtn').hide();
						$('#updatabtn').show();
						viewModel.getEx(selectNodeId,viewModel.exDataTable2);
						var accSup = viewModel.exDataTable2.getSimpleData();
						for(var i=0;i<accSup.length;i++) {
							if(accSup[i].rid == "START_DATE" || accSup[i].rid == "END_DATE"){
								$('#'+accSup[i].id).change( function(event) {
									for(var i=0;i<accSup.length;i++) {
										if(accSup[i].rid == "START_DATE"){
											var startTime = $('#'+accSup[i].id).val();
										}
										if(accSup[i].rid == "END_DATE"){
											var endTime = $('#'+accSup[i].id).val();
										}
									}
									if(!checkData(startTime,endTime)){
										$('#'+event.target.id).val("");
										 ip.ipInfoJump("开始日期不能大于结束日期", 'error');
									}
								});
							}
						}
	        		}
	        	}else{
	        		ip.ipInfoJump("请选择需要修改的事项", 'info');
	        	}
			};
			
			//事项修改
			viewModel.updataEx = function() {	
				if(saveBtnclick){
					ip.ipInfoJump("保存按钮已被点击，请稍等。", 'error');
				}else{
					saveBtnclick = true;
					var aim2 = viewModel.inputViewDataTable.getSimpleData();
					var noVerifyRid =["REMARK","SUP_NO"];
					if(verifyInputView(aim2,noVerifyRid)) {
						var accSup = viewModel.exDataTable2.getSimpleData();
						var exData = viewModel.exDataTable.getSimpleData();
						for(var i=0;i<accSup.length;i++) {
							if(accSup[i].type == "treeassist") {
								var value = $('#'+accSup[i].id+"-h").val();
							}else{
								var value = $('#'+accSup[i].id).val();
							}
							exData[0][accSup[i].rid] = value;
						}
						var data = {
							"data" : JSON.stringify(exData[0]),
							"billtype_code" : "201",
						}
						$.ajax({
				            type: 'get',
				            url: updateExURL + "?tokenid=" + tokenid + "&ajax=noCache",
				            data: data,
				            dataType: 'JSON',
				            async: true,
				            success: function (result) {
				            	if(result.errorCode == 0) {
				            		$('#addExModal').modal('toggle');
				            		saveBtnclick = false;
				            		$('#infoAdd').html("");
				            		viewModel.leftTree();
				            		viewModel.clearData();
				            	}else {
				            		ip.ipInfoJump(result.message, 'error');
				            	}
				            }, error: function () {
				            	ip.ipInfoJump("错误", 'error');
				            }		                
			            });
					}else{
						ip.ipInfoJump("请完善红框数据", 'error');
					}
				}
			};
			
			//事项删除btn
			viewModel.taskdel = function() {
				var treeObj = $.fn.zTree.getZTreeObj("depTree");
	        	var nodes = treeObj.getSelectedNodes();
	        	if(nodes.length > 0 && nodes[0].TYPE_CODE == "SUP") {
	        		var selectNodeId = nodes[0].CHR_ID;
	        		$.ajax({
			            type: 'get',
			            url: deleteExURL + "?tokenid=" + tokenid + "&ajax=noCache",
			            data: {"id":selectNodeId},
			            dataType: 'JSON',
			            async: true,
			            success: function (result) {
			            	if(result.errorCode == 0) {
			            		viewModel.leftTree();
			            		viewModel.clearData();
			            	}else {
			            		ip.ipInfoJump(result.message, 'error');
			            	}
			            }, error: function () {
			            	ip.ipInfoJump("错误", 'error');
			            }		                
		            });
	        	}else{
	        		ip.ipInfoJump("请选择需要删除的事项", 'info');
	        	}
			};

			viewModel.initAddWorkBase = function(selectNodeId) {
				$("#workBaseExMas").html("");
				$.get(getExURL, {"id":selectNodeId}, function(result){
        			var data = result.data;
        			viewModel.exDataTable.setSimpleData(data);
        			if(data.WORK_TYPE == "0"){
        				var workType = "财政部布置";
        			} else if(data.WORK_TYPE == "1"){
        				var workType = "自行安排";
        			} else{
        				var workType = "";
        			} 
        			$("#workBaseExMas").append('<div class="col-md-7 p5">监管类型：'+data.SUP_TYPE_NAME+' </div><div class="col-md-5 p5">工作性质： '+workType+'</div>'+
        					'<div class="col-md-7 p5">监管任务：'+data.SUP_NAME+' </div><div class="col-md-5 p5">负责司局： '+data.MOFDEP_NAME+'</div>'+
        					'<div class="col-md-7 p5">开始时间：'+data.START_DATE+'</div>'+
        					'<div class="col-md-5 p5">截止时间： '+data.END_DATE+'</div>');
        			//<div class="col-md-7 p5">任务内容： '+data.SUP_CONTENT+'</div>'<div class="col-md-5 p5">牵头处室： '+data.DEP_NAME+'</div>
    	    	})
			};
			
			//工作进度
			workProgressChange = function() {
				if($("#WORK_PROGRESS-w").val() == "100") {
					$('#WORK_PROGRESS-wp').val("100");
					$('#WORK_PROGRESS-wp').attr('disabled',true);
				}else {
					$('#WORK_PROGRESS-wp').attr('disabled',false);
					$('#WORK_PROGRESS-wp').val("");
				}
			};
			
			//工作新增进行校验
			verifyWork = function(){
				var nullFlag = 0;
				if($('#RECONDERDate-w').val() == "" || $('#RECONDERDate-w').val() == null || $('#RECONDERDate-w').val() == "-1"){
					nullFlag++;
					$('#RECONDERDate-w').css('border-color', 'red');
				}else{
					$('#RECONDERDate-w').css('border-color', '#ccc');
				}
				if($('.u-combo-name-par > div').length == 0){
					nullFlag++;
					$('#EXECUTOR-w .u-form-control').css('border-color', 'red');
				}else{
					$('#EXECUTOR-w .u-form-control').css('border-color', '#ccc');
				}
				if($('#FUND_SCALE-w').val() == "" || $('#FUND_SCALE-w').val() == null || $('#FUND_SCALE-w').val() == "-1"){
					nullFlag++;
					$('#FUND_SCALE-w').css('border-color', 'red');
				}else{
					$('#FUND_SCALE-w').css('border-color', '#ccc');
				}
				if($('#WORK_PROGRESS-wp').val() == "" || $('#WORK_PROGRESS-wp').val() == null || $('#WORK_PROGRESS-wp').val() == "-1"){
					nullFlag++;
					$('#WORK_PROGRESS-wp').css('border-color', 'red');
				}else{
					$('#WORK_PROGRESS-wp').css('border-color', '#ccc');
				}
				if($('#WORKLOAD-w').val() == "" || $('#WORKLOAD-w').val() == null || $('#WORKLOAD-w').val() == "-1"){
					nullFlag++;
					$('#WORKLOAD-w').css('border-color', 'red');
				}else{
					$('#WORKLOAD-w').css('border-color', '#ccc');
				}
				if(nullFlag == 0){
					return true;
				}else{
					return false;
				}
			}
			
			//工作记录新增workAddURL
			viewModel.addWork = function() {
				if(verifyWork()){
					var treeObj = $.fn.zTree.getZTreeObj("depTree");
		        	var nodes = treeObj.getSelectedNodes();
		        	if(nodes.length > 0) {
		        		var selectNodeId = nodes[0].CHR_ID;
		        		$.get(getExURL, {"id":selectNodeId}, function(result){
		        			var data = result.data;
		        			viewModel.exDataTable.setSimpleData(data);
		        			var accWork = {};
		        			$.each(data, function(key, value){
		        				accWork[key] = value;
		    				})
		    				accWork["REGISTRATION_TIME"] = $('#RECONDERDate-w').val();
		        			var arr = $('.u-combo-name-par > div');
	    					var comboD = arr[0].textContent;
	    					var comboKey = [];
	    					for(var cd = 1;cd<arr.length;cd++){
	    						comboD = comboD+","+arr[cd].textContent;
	    					}
	    					$.each(arr, function(index, item){
	    						comboKey.push(item.attributes[1].value);
	    					})
	    					accWork["EXECUTOR"] = comboD;
		        			accWork["FUND_SCALE"] = parseFloat($('#FUND_SCALE-w').val());
		        			accWork["WORK_PROGRESS"] = $('#WORK_PROGRESS-wp').val()+"%";
		        			accWork["WORKLOAD"] = $('#WORKLOAD-w').val();
		        			accWork["WORK_CONTENT"] = $('#WORKLOADParm-w').val();
		        			accWork["REMARK"] = $('#REMARK-w').val();
		    				var data = {
		    					"data" : JSON.stringify(accWork),
		    					"billtype_code" : "202",
		    					"set_month" : SET_MONTH,
		    					"set_year" : SET_YEAR,
		    					"book_id" : BOOK_ID,
		    					"entity_id" : selectNodeId,
		    				}
		    				$.ajax({
		    		            type: 'get',
		    		            url: workAddURL + "?tokenid=" + tokenid + "&ajax=noCache",
		    		            data: data,
		    		            dataType: 'JSON',
		    		            async: true,
		    		            success: function (result) {
		    		            	if(result.errorCode == 0) {
		    		            		viewModel.clearData();
		    		            		$('#workAddModal').modal('toggle');
		    		            		$(".workAddInput").each(function () {
		    		                        $(this).val("");
		    		                    });
		    		            		$('#WORK_PROGRESS-w').val("0");
		    		            		if($("#workRecodeGrid")[0].innerHTML==""){
		    		    					viewModel.initData("", "workRecodeGrid",pViewType.VIEWTYPE_LIST,"0");
		    		    				}else{
		    		    					viewModel.refresh("", "workRecodeGrid",pViewType.VIEWTYPE_LIST,"0");
		    		    				}
		    		            	}else {
		    		            		ip.ipInfoJump(result.message, 'error');
		    		            		$('#workAddModal').modal('toggle');
		    		            		$(".workAddInput").each(function () {
		    		                        $(this).val("");
		    		                    });
		    		            		$('#WORK_PROGRESS-w').val("0");
		    		            	}
		    		            }, error: function () {
		    		            	ip.ipInfoJump("错误", 'error');
		    		            }		                
		    	            });
		    	    	})
		        	}
				}else{
					ip.ipInfoJump("请完善红框数据", 'error');
				}
			};
			
			//问题新增校验
			verifyProblem = function(){
				var nullFlag = 0;  			
				if($('#DISCOVER_PROBLEMS').val() == "" || $('#DISCOVER_PROBLEMS').val() == null || $('#DISCOVER_PROBLEMS').val() == "-1"){
					nullFlag++;
					$('#DISCOVER_PROBLEMS').css('border-color', 'red');
				}else{
					$('#DISCOVER_PROBLEMS').css('border-color', '#ccc');
				}
				if($('#AUDIT_BASIS').val() == "" || $('#AUDIT_BASIS').val() == null || $('#AUDIT_BASIS').val() == "-1"){
					nullFlag++;
					$('#AUDIT_BASIS').css('border-color', 'red');
				}else{
					$('#AUDIT_BASIS').css('border-color', '#ccc');
				}
				if($('#AUDIT_OPINION').val() == "" || $('#AUDIT_OPINION').val() == null || $('#AUDIT_OPINION').val() == "-1"){
					nullFlag++;
					$('#AUDIT_OPINION').css('border-color', 'red');
				}else{
					$('#AUDIT_OPINION').css('border-color', '#ccc');
				}
				if(nullFlag == 0){
					return true;
				}else{
					return false;
				}
			}
			
			//问题记录新增problemAddURL
			viewModel.addProblem = function() {
				if(verifyProblem()) {
					var treeObj = $.fn.zTree.getZTreeObj("depTree");
		        	var nodes = treeObj.getSelectedNodes();
		        	if(nodes.length > 0) {
		        		var selectNodeId = nodes[0].CHR_ID;
		        		$.get(getExURL, {"id":selectNodeId}, function(result){
		        			var data = result.data;
		        			viewModel.exDataTable.setSimpleData(data);
		        			var accProblem = {};
		        			$.each(data, function(key, value){
		        				accProblem[key] = value;
		    				})
		    				accProblem["DISCOVER_PROBLEMS"] = $('#DISCOVER_PROBLEMS').val();
		        			accProblem["AUDIT_BASIS"] = $('#AUDIT_BASIS').val();
		        			accProblem["AUDIT_OPINION"] = $('#AUDIT_OPINION').val();
		        			accProblem["HANDLE"] = $('#HANDLE').val();
		        			accProblem["REMARK"] = $('#REMARK-q').val();
		        			accProblem["GROUPING_NAME"] = options.svDepName;
		        			accProblem["RECORDING_USER"] = options.svUserName;
		        			
		    				var data = {
		    					"data" : JSON.stringify(accProblem),
		    					"entity_id" : selectNodeId,
		    				}
		    				$.ajax({
		    		            type: 'get',
		    		            url: problemAddURL + "?tokenid=" + tokenid + "&ajax=noCache",
		    		            data: data,
		    		            dataType: 'JSON',
		    		            async: true,
		    		            success: function (result) {
		    		            	if(result.errorCode == 0) {
		    		            		$('#questionAddModal').modal('toggle');
		    		            		$(".questionAddInput").each(function () {
		    		                        $(this).val("");
		    		                    });
		    		            		$('#HANDLE').val("0");
		    		            		if($("#questionRecodeGrid")[0].innerHTML==""){
		    		    					viewModel.initData("", "questionRecodeGrid",pViewType.VIEWTYPE_LIST,"1");
		    		    				}else{
		    		    					viewModel.refresh("", "questionRecodeGrid",pViewType.VIEWTYPE_LIST,"1");
		    		    				}
		    		            	}else {
		    		            		ip.ipInfoJump(result.message, 'error');
		    		            		$('#questionAddModal').modal('toggle');
		    		            		$(".questionAddInput").each(function () {
		    		                        $(this).val("");
		    		                    });
		    		            		$('#HANDLE').val("0");
		    		            	}
		    		            }, error: function () {
		    		            	ip.ipInfoJump("错误", 'error');
		    		            }		                
		    	            });
		    	    	})
		        	}
				}else{
					ip.ipInfoJump("请完善红框数据", 'error');
				}
			};
		    
		    //编辑问题视图保存
			viewModel.updateProblem = function(obj){
				if(verifyProblem()) {
					var treeObj = $.fn.zTree.getZTreeObj("depTree");
		        	var nodes = treeObj.getSelectedNodes();
		        	if(nodes.length > 0) {
		        		var selectNodeId = nodes[0].CHR_ID;
		        	}
					var datatableRow = viewModel.questionGridViewModel.gridData.getRowByRowId(viewModel.rowId);
			    	$.get(problemGetURL, {"entity_id":selectNodeId}, function(result){
	        			var dataDetail = result.dataDetail;
	        			for(var i=0;i<dataDetail.length;i++){
	        				if(dataDetail[i].ID == datatableRow.data.ID.value){
	        					dataDetail[i].DISCOVER_PROBLEMS = $('#DISCOVER_PROBLEMS').val();
	        					dataDetail[i].AUDIT_BASIS = $('#AUDIT_BASIS').val();
	        					dataDetail[i].AUDIT_OPINION = $('#AUDIT_OPINION').val();
	        					dataDetail[i].HANDLE = $('#HANDLE').val();
	        					dataDetail[i].REMARK = $('#REMARK-q').val();
	        					dataDetail[i].RECORDING_USER = options.svUserName;
	        					var data = {
	                					"data" : JSON.stringify(dataDetail[i]),
	                				}
	                				$.ajax({
	                		            type: 'get',
	                		            url: problemUpdateURL + "?tokenid=" + tokenid + "&ajax=noCache",
	                		            data: data,
	                		            dataType: 'JSON',
	                		            async: true,
	                		            success: function (result) {
	                		            	if(result.errorCode == 0) {
	                		            		viewModel.refresh("", "questionRecodeGrid",pViewType.VIEWTYPE_LIST,"1");
	                		            		$('#questionAddModal').modal('toggle');
	                		            	}else {
	                		            		ip.ipInfoJump(result.message, 'error');
	                		            	}
	                		            }, error: function () {
	                		            	ip.ipInfoJump("错误", 'error');
	                		            }		                
	                	            });
	        					break;
	        				}
	        			}
	        			
	    	    	})
				}else{
					ip.ipInfoJump("请完善红框数据", 'error');
				}
		    };
			
			//删除操作deleteExURLworkDeleteURLproblemDeleteURL
			viewModel.delOpertion = function(opType,delData,id){
				if(opType == "work"){
					var URL = workDeleteURL;
				}else if(opType == "question"){
					var URL = problemDeleteURL;
				}else if(opType == "ex") {
					var URL = deleteExURL;
				}
				var treeObj = $.fn.zTree.getZTreeObj("depTree");
	        	var nodes = treeObj.getSelectedNodes();
	        	if(nodes.length > 0) {
	        		var selectNodeId = nodes[0].CHR_ID;
	        	}
				$.ajax({
					type: 'get',
		            url: URL + "?tokenid=" + tokenid + "&ajax=noCache",
		            data: {"id":id,"entity_id":selectNodeId},
		            dataType: 'JSON',
		            async: false,
		            success: function (result) {
		            	if(result.errorCode == 0) {
		            		var datatableRow = delData.gridData.getRowByRowId(viewModel.rowId);
		            		delData.gridData.removeRow(datatableRow); 
		            	}else {
		            		ip.ipInfoJump(result.message, 'error');
		            	}
		            }, error: function () {
		            	ip.ipInfoJump("错误", 'error');
		            	return false;
		            }
				});
			};			
	        
	        //初始化期间
	        viewModel.initYearMonth = function() {
	        	var objSelect = document.getElementById("year");
	        	objSelect.options.length=0;
	        	objSelect.options.add(new Option(SET_YEAR , SET_YEAR));
	        	var objSelect2 = document.getElementById("month");
	        	objSelect2.options.length=0;
	        	objSelect2.options.add(new Option(SET_MONTH , SET_MONTH));
	        };
			
	        
	       //初始化台账类型getCsofAccURL getCsofAccByIdURL
	        viewModel.initAccType = function(){
	        	$("#acc_type-select").html("");
				var objSelect = document.getElementById("acc_type-select");//获得select标签对象
				var new_opt = new Option("全部", "-1"); //直接new一个option对象，然后把text和value值都赋上
                objSelect.options.add(new_opt);
	        	$.ajax({
		            type: 'get',
		            url: getCsofAccURL + "?tokenid=" + tokenid + "&ajax=noCache",
		            data: {"ele_code":"CSOF_ACC","book_id":BOOK_ID},
		            dataType: 'JSON',
		            async: true,
		            success: function (result) {
		            	if(result.errorCode == 0) {
		            		var data = result.data;
		            		for(var i = 0;i<data.length;i++) {
		    	                var new_opt = new Option(data[i].CHR_NAME, data[i].CHR_ID); //直接new一个option对象，然后把text和value值都赋上
		    	                objSelect.options.add(new_opt);
		    	            }
		            	}else {
		            		ip.ipInfoJump(result.errorMsg, 'error');
		            	}
		            }, error: function () {
		            	ip.ipInfoJump("错误", 'error');
		            }		                
	            });
	        }
	        
	        AccTypeById = function(){
	        	var chrId = $("#acc_type-select").val();
				if(chrId == "-1"){
					viewModel.leftTree();
				}else{
					$.ajax({
			            type: 'get',
			            url: getCsofAccByIdURL + "?tokenid=" + tokenid + "&ajax=noCache",
			            data: {"ele_code":elecode,"chr_id":chrId},
			            dataType: 'JSON',
			            async: true,
			            success: function (result) {
			            	if(result.errorCode == 0) {
			            		var data = result.data;
			            		console.log(data);
			            		var dep_array = [];
			            		for(var i=0;i<data.length;i++) {
			            			if(data[i].IS_LEAF == "0") {
			            				data[i].PARENT_ID = "";
			            			}
			            		}
			            		viewModel.depTreeDataTable.setSimpleData(data);
			            		var treeObj = $.fn.zTree.getZTreeObj("depTree");
			                    treeObj.expandAll(true);
			                    treeObj.cancelSelectedNode();
			                    var exShow = viewModel.exDataTable1.getSimpleData();
			                    if(exShow){
			                    	for(var i=0;i<exShow.length;i++){
				            			$.each(exShow, function(index, item){
				            				$("#"+item.id).val("");
			        					})
				            		}
			                    }
			            	}else {
			            		ip.ipInfoJump(result.errorMsg, 'error');
			            	}
			            }, error: function () {
			            	ip.ipInfoJump("错误", 'error');
			            }		                
		            });
				}
	        }
	        
	        /*按钮事件
	         * <button class="btn csof-btn" onclick="taskAdd()">事项新增</button>
					<button class="btn csof-btn" data-bind="click: taskUpdata.bind($data)">事项修改</button>
					<button class="btn csof-btn" data-bind="click: taskdel.bind($data)">事项删除</button>
					<button class="btn csof-btn">事项办结</button>
					<button class="btn csof-btn"  data-bind="click: beforeEdit.bind($data,2,-1)">工作登记</button>
					<button class="btn csof-btn" data-bind="click: beforeEdit.bind($data,3,-1)">问题登记</button>*/
	      //事项新增btn
			taskAdd = function(id) {
				var treeObj = $.fn.zTree.getZTreeObj("depTree");
	        	var nodes = treeObj.getSelectedNodes();
	        	if(nodes.length > 0) {
					//$('#addExModal').modal('show');
					$('#addExModal').modal({
	                	show : true,
	                	backdrop : 'static'
	                });
					saveBtnclick = false;
					$('#infoAdd').html("");
					viewModel.initData("edit", "infoAdd",pViewType.VIEWTYPE_INPUT,"1");
					var accSup = viewModel.exDataTable2.getSimpleData();
					for(var i=0;i<accSup.length;i++) {
						if(accSup[i].rid == "SUP_NO") {
							var value = $('#'+accSup[i].id).val("");
							$('#'+accSup[i].id).attr('placeholder','自动序列')
							break;
						}
					}
					for(var i=0;i<accSup.length;i++) {
						if(accSup[i].rid == "START_DATE" || accSup[i].rid == "END_DATE"){
							$('#'+accSup[i].id).change( function(event) {
								for(var i=0;i<accSup.length;i++) {
									if(accSup[i].rid == "START_DATE"){
										var startTime = $('#'+accSup[i].id).val();
									}
									if(accSup[i].rid == "END_DATE"){
										var endTime = $('#'+accSup[i].id).val();
									}
								}
								if(!checkData(startTime,endTime)){
									$('#'+event.target.id).val("");
									 ip.ipInfoJump("开始日期不能大于结束日期", 'error');
								}
							});
						}
					}
					var leftTreeData = viewModel.depTreeDataTable.getSimpleData();
	        		var type = nodes[0].TYPE_CODE;
	        		if(type == "SUP"){
	        			for(var i=0;i<leftTreeData.length;i++){
	        				if(nodes[0].PARENT_ID == leftTreeData[i].CHR_ID){
	        					for(var j=0;j<accSup.length;j++) {
	        						if(accSup[j].rid == "SUP_TYPE_NAME") {
	        							var value = $('#'+accSup[j].id+"-h").val(leftTreeData[i].CHR_ID + "@" + encodeURI(leftTreeData[i].CHR_NAME, "utf-8") + "@" + leftTreeData[i].CHR_CODE + "@" + leftTreeData[i].PARENT_ID);
	        							var value = $('#'+accSup[j].id).val(leftTreeData[i].CHR_NAME);
	        							break;
	        						}
	        					}
	        					break;
	        				}
	        			}
	        		}else{
	        			var accSup = viewModel.exDataTable2.getSimpleData();
    					for(var i=0;i<accSup.length;i++) {
    						if(accSup[i].rid == "SUP_TYPE_NAME") {
    							var value = $('#'+accSup[i].id+"-h").val(nodes[0].CHR_ID + "@" + encodeURI(nodes[0].CHR_NAME, "utf-8") + "@" + nodes[0].CHR_CODE + "@" + leftTreeData[i].PARENT_ID);
    							var value = $('#'+accSup[i].id).val(nodes[0].CHR_NAME);
    							break;
    						}
    					}
	        		}
	        		$('#savebtn').show();
					$('#updatabtn').hide();
	        	}else{
	        		 ip.ipInfoJump("请选择需要新增的事项类型树节点！", 'info');
	        	}
			};
			
			//事项过账
			taskPost = function(){
				$("#postExGrid").html("");
				viewModel.initData("", "postExGrid",pViewType.VIEWTYPE_LIST,"3");
				$("#postExModal").modal({
                	show : true,
                	backdrop : 'static'
                });
				
			}
			//过账完成
			viewModel.postEx = function(){
				var selectRow = viewModel.postGridViewModel.gridData.getSelectedRows();
				var allRows = viewModel.postGridViewModel.gridData.getSimpleData();
				var row = [];
				for(var i=0;i<allRows.length;i++){
					for(var j=0;j<selectRow.length;j++){
						if(selectRow[j].data.ID.value == allRows[i].ID){
							row.push(allRows[i]);
						}
					}
				}
				var postErrorFlag = 0;
				for(var r = 0;r<row.length;r++){
					if(postErrorFlag == 0){
						var data = {
								"dep_id":DEP_ID,
								"oid":OID,
								"book_id":BOOK_ID,
								"data": JSON.stringify(row[r]),
								"acc_id":ACC_ID,
						}
						$.ajax({
				            type: 'get',
				            url: saveTaskUserURL + "?tokenid=" + tokenid + "&ajax=noCache",
				            data: data,
				            dataType: 'JSON',
				            async: false,
				            success: function (result) {
				            	if(result.errorCode == "0") {
				            		
				            	}else{
				            		postErrorFlag++;
						            ip.ipInfoJump(result.message, 'error');
					            }
				            }, error: function () {
				            	postErrorFlag++;
				                ip.ipInfoJump("错误", 'error');
				            }
			            });
					}
				}
				if(postErrorFlag == 0){
					$('#postExModal').modal('toggle');   
		            ip.ipInfoJump("事项过账成功", 'success');
		            viewModel.clearData();
		            viewModel.leftTree();
				}
			}
			
			//事项修改
			publicModify = function(id) {
				viewModel.taskUpdata();
			};
			//事项删除
			publicDelete = function(id) {
				viewModel.taskdel();
			};
			//事项办结
			taskFinish = function(id) {
				var treeObj = $.fn.zTree.getZTreeObj("depTree");
	        	var nodes = treeObj.getSelectedNodes();
	        	$("#workBaseExMas").html("");
	        	$(".workAddInput").each(function () {
                    $(this).val("");
                });
	        	var today=new Date();
        	    var h=today.getFullYear();
        	    var m=today.getMonth()+1;
        	    var d=today.getDate();
        	    if(m<10){
        	    	m = "0" + m;
        	    }
        		$('#RECONDERDate-w').val(h+"-"+m+"-"+d);
	        	if(nodes.length > 0) {
	        		if(nodes[0].TYPE_CODE == "SUP"){
	        			var selectNodeId = nodes[0].CHR_ID;
		        		var DataTable = viewModel.workGridViewModel;
			    		var formDataTable = viewModel.workFormDataTable;
			    		var addModel = $("#workAddModal");
			    		viewModel.initAddWorkBase(selectNodeId);
			    		$("#WORK_PROGRESS-w").val("100");
			    		$('#WORK_PROGRESS-wp').val("100");
			    		$('#WORK_PROGRESS-wp').attr('disabled',true);
			    		$("#WORK_PROGRESS-w").attr("disabled","disabled").css("background-color","#EEEEEE;");
			    		$('#RECONDERDate-w').css('border-color', '#ccc');
			    		$('#EXECUTOR-w .u-form-control').css('border-color', '#ccc');
						$('#FUND_SCALE-w').css('border-color', '#ccc');
						$('#WORK_PROGRESS-wp').css('border-color', '#ccc');
						$('#WORKLOAD-w').css('border-color', '#ccc');
						$("#workAddModalLabel").html("事项办结");
						//显示模态框 
			    		addModel.modal({
		                	show : true,
		                	backdrop : 'static'
		                });
	        		}else{
	        			ip.ipInfoJump("请选择事项进行办结", 'info');
	        		}
	        	}else{
	        		ip.ipInfoJump("请选择需要办结的事项", 'info');
	        	}
			};
			//工作登记
			workAdd = function(id) {
				var treeObj = $.fn.zTree.getZTreeObj("depTree");
	        	var nodes = treeObj.getSelectedNodes();
	        	$("#workBaseExMas").html("");
	        	$('#WORK_PROGRESS-wp').removeAttr('disabled',true);
	        	$("#WORK_PROGRESS-w").removeAttr("disabled","disabled").css("background-color","#fff;");
	        	$(".workAddInput").each(function () {
                    $(this).val("");
                });
	        	var today=new Date();
        	    var h=today.getFullYear();
        	    var m=today.getMonth()+1;
        	    var d=today.getDate();
        	    if(m<10){
        	    	m = "0" + m;
        	    }
        		$('#WORK_PROGRESS-w').val("1");
        		$('#RECONDERDate-w').val(h+"-"+m+"-"+d);
	        	if(nodes.length > 0) {
	        		if(nodes[0].TYPE_CODE == "SUP"){
	        			var selectNodeId = nodes[0].CHR_ID
	        			$.ajax({
	    		            type: 'get',
	    		            url: getExURL + "?tokenid=" + tokenid + "&ajax=noCache",
	    		            data: {"id":selectNodeId},
	    		            dataType: 'JSON',
	    		            async: true,
	    		            success: function (result) {
	    		            	var data = result.data;
	    		            	viewModel.exDataTable.setSimpleData(data);
			        			if(data.STATUS == "100%"){
				    				ip.ipInfoJump("该事项工作进展已完成，不可再登记", 'info');
				    			}else{
				    	    		var addModel = $("#workAddModal");
				    	    		viewModel.initAddWorkBase(selectNodeId);
									//显示模态框 
				    	    		$("#workAddModalLabel").html("工作记录新增");
									addModel.modal({
					                	show : true,
					                	backdrop : 'static'
					                });
									$('#RECONDERDate-w').css('border-color', '#ccc');
									$('#EXECUTOR-w .u-form-control').css('border-color', '#ccc');
									$('#FUND_SCALE-w').css('border-color', '#ccc');
									$('#WORK_PROGRESS-wp').css('border-color', '#ccc');
									$('#WORKLOAD-w').css('border-color', '#ccc');
									document.getElementById('EXECUTOR-w')['u.Combo'].setComboData("");
									$.ajax({
				    		            type: 'get',
				    		            url: getUserNameByDepIdURL + "?tokenid=" + tokenid + "&ajax=noCache",
				    		            data: {"dep_id":options.svDepId},
				    		            dataType: 'JSON',
				    		            async: false,
				    		            success: function (map) {
				    		            	if(map.errorCode == 0){
				    		            	    comboData = map.data;
				    		            	    var newArr = [];
				    		            	    var num = 1;
				    		            		for(var cd = 0;cd<comboData.length;cd++){
				    		            			if(comboData[cd].USER_ID == options.svUserId){
				    		            				newArr[0] = comboData[cd];
				    		            			}else{
				    		            				newArr[num] = comboData[cd];
					    		            			num ++ ;
				    		            			}
				    		            		}
				    		            		comboData = newArr;
				    		            		var comboData1 = [];
												num = 1;
				    		            		for(var cd = 0;cd<comboData.length;cd++){
				    		            			if(comboData[cd].USER_ID == options.svUserId){
				    		            				comboData1[0] = {
					    		            					value:comboData[cd].USER_ID,
					    		            					name:comboData[cd].USER_NAME.split("（")[0],
					    		            			}
				    		            			}else{
					    		            			comboData1[num] = {
					    		            					value:comboData[num].USER_ID,
					    		            					name:comboData[num].USER_NAME.split("（")[0],
					    		            			}
					    		            			num ++ ;
				    		            			}
				    		            		}
												document.getElementById('EXECUTOR-w')['u.Combo'].setComboData(comboData1);
												
												$('.u-has-feedback').find('.myde').remove();
												if($('.u-has-feedback > div>div')){
													$('.u-has-feedback > div > div').html(comboData[0].USER_NAME.split("（")[0]); 
												}
												var html = '<div class="u-combo-name-par myde" style="position:absolute;max-width:184px;" title="'+ comboData[0].USER_NAME.split("（")[0] +'">'+
											    '<div class="u-combo-name" key="'+ comboData[0].USER_ID +'"> '+ comboData[0].USER_NAME.split("（")[0] +' <div>'+
											    '<div>';
												$('.u-has-feedback').append(html);
				    		            	}else{
				    		            		 ip.ipInfoJump(map.message, 'error');
				    		            	}
				    		            }, error: function () {
				    		                ip.ipInfoJump("错误", 'error');
				    		            }
				    	            });
				    			}            	
	    		            }, error: function () {
	    		                ip.ipInfoJump("错误", 'error');
	    		            }
	    	            });
	        		}else{
	        			ip.ipInfoJump("请选择事项进行工作登记", 'info');
	        		}
	        	}else{
	        		ip.ipInfoJump("请选择需要登记的事项", 'info');
	        	}	    		
			};
			//问题登记
			questionAdd = function(id) {
				viewModel.beforeEdit("3","-1", id)
			};
			//附件上传
			publicFileman = function(id) {
				$("#filePage")[0].src = "";
				var treeObj = $.fn.zTree.getZTreeObj("depTree");
	        	var nodes = treeObj.getSelectedNodes();
	        	if(nodes.length > 0) {
	        		if(nodes[0].TYPE_CODE == "SUP"){
	        			var selectNodeId = nodes[0].CHR_ID;
		        		fileData = {
		    					"entity_id":selectNodeId,
		    					"oid":options.svOfficeId,
		    					"dep_id":options.svDepId,
		    					"dep_code":options.svDepCode,
		    					"edit":'Y',
		    			    }
		    				$("#filePage")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
		    		    	"&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&entityName=csof_sup_bill&oid="+fileData.oid+
		    		    	"&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit;
						    $("#addFileModal").modal({
			                	show : true,
			                	backdrop : 'static'
			                });
	        		}else{
	        			ip.ipInfoJump("请选择需要登记的事项", 'info');
	        		}
	        	}else{
	        		ip.ipInfoJump("请选择需要登记的事项", 'info');
	        	}
			};       
			
			//清除
			viewModel.clearData = function(){
       		    var opretion = $('#righ-tab-ul li[class="active"] a')[0].innerText;
       		    if(opretion == "事项详情"){
       		    	var exShow = viewModel.exDataTable1.getSimpleData();
            		for(var i=0;i<exShow.length;i++){
            			$.each(exShow, function(index, item){
            				$("#"+item.id).val("");
    					})
            		}
       		    }else if(opretion == "工作记录"){
       		    	if($("#workRecodeGrid")[0].innerHTML==""){
    					viewModel.initData("", "workRecodeGrid",pViewType.VIEWTYPE_LIST,"0");
    				}else{
    					viewModel.refresh("", "workRecodeGrid",pViewType.VIEWTYPE_LIST,"0");
    				}
       		    }else if(opretion == "问题记录"){
       		    	if($("#questionRecodeGrid")[0].innerHTML==""){
    					viewModel.initData("", "questionRecodeGrid",pViewType.VIEWTYPE_LIST,"1");
    				}else{
    					viewModel.refresh("", "questionRecodeGrid",pViewType.VIEWTYPE_LIST,"1");
    				}
       		    }else if(opretion == "附录"){
       		    	$("#fileUploadPage")[0].src = "";
   		    		fileData = {
	    					"entity_id":"",
	    					"oid":options.svOfficeId,
	    					"dep_id":options.svDepId,
	    					"dep_code":options.svDepCode,
	    					"edit":'N',
	    			    }
	    				$("#fileUploadPage")[0].src = "/df/supervise/fileUpload/upload.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
	    		    	"&menuname=" + options.svMenuName+"&entityId="+fileData.entity_id+"&entityName=csof_sup_bill&oid="+fileData.oid+
	    		    	"&dep_id="+fileData.dep_id+"&dep_code="+fileData.dep_code+"&modelFlag=0&admin="+fileData.edit;
       		    }
			}
			
			//资金规模格式化和数字控制
			fomateFund = function(){
				var money = $("#FUND_SCALE-w").val();
				if(money != "" && money != null){
					if(csof.isNum(money) == false){
				         ip.ipInfoJump("请输入数字!", 'info');
				         $("#FUND_SCALE-w").val("");
				    }else{
						var fomoney = csof.fmoney(money,2);
						$("#FUND_SCALE-w").val(fomoney);
					}
				}
			}
			
			 //校验为数字
		     validate = function(obj){
		    	 if(obj.value != "" && obj.value != null){
		    		 if(csof.isNum(obj.value) == false){
				    	  ip.ipInfoJump("请输入数字!", 'info');
				    	  obj.value = ""; 
				      }
		    	 }
		    }
		     
		     //验证百分数，填的值小于100
		     validateP = function(obj){
		    	 if(obj.value != "" && obj.value != null){
		    		 if(csof.isNum(obj.value) == false){
			    		 ip.ipInfoJump("请输入数字!", 'info');
				    	  obj.value = ""; 
				      } else{
				    	  if(parseFloat(obj.value) > 100){
				    		  ip.ipInfoJump("请输入小于等于100的数字!", 'info');
				    		  obj.value = ""; 
				    	  }
				      }
		    	 }
		     }
		     
		     function windowHeight() {
		            var h = document.body.clientHeight; //获取当前窗口可视操作区域高度
		            var bodyHeight = document.getElementById("main-content"); //寻找ID为content的对象
		            $('#main-content').jqxSplitter({ width: '98.3%', height: (h - 65) + "px", panels: [{ size: '23.2%', min: 200 }, { min: 800, size: '76.8%' }] });
		            $('.treeDiv').height($("#left-main-content").height() - 120 + "px");
		            $('.tab-panel-height').height($("#right-main-content").height() - 32 + "px");
		            $('.rightTableGrid').height($(".tab-panel-height").height() - 26 + "px");
		        }
		     
		        setInterval(windowHeight(), 500)//每半秒执行一次windowHeight函数

			//初始化
			function init() {
				app = u.createApp({
					el : document.body,
					model : viewModel
				});
				tokenid = ip.getTokenId();
				options = ip.getCommonOptions({});
				options['tokenid'] = tokenid;
				var url=window.location.href;
				elecode = url.split("elecode=")[1].split("&")[0];
	            menuId = url.split("menuid=")[1].split("&")[0];
	            menuName = decodeURI(url.split("menuname=")[1].split("&")[0]);
	            DEP_ID = options.svDepId;
	            OID = options.svOfficeId;
	            ACC_ID = url.split("ACC_ID=")[1].split("&")[0];
	            BOOK_ID = url.split("BOOK_ID=")[1].split("&")[0];
	            BOOK_NAME = decodeURI(url.split("BOOK_NAME=")[1].split("&")[0]);
	            SET_YEAR = url.split("SET_YEAR=")[1].split("&")[0];
	            SET_MONTH = url.split("SET_MONTH=")[1].split("&")[0];
				viewModel.initData("edit", "testGrid",pViewType.VIEWTYPE_INPUT,"0");//初始化grid
				viewModel.initSupTypeSelect();//初始化下拉框
				viewModel.leftTree();//初始化左侧树leftList
				viewModel.initYearMonth();//初始化期间
				viewModel.initAccType();//初始化台账类型
				var data = initBtns(menuId);
        		if(data == false){
        			ip.ipInfoJump("加载按钮出错", 'error');
        		}else{
        			viewModel.btnDataTable.setSimpleData(data);
        		}
        		windowHeight();
			}
			init();
		})