require(['jquery', 'knockout','/df/supervise/ncrd.js','csscsof', 'bootstrap', 'uui', 'tree', 'grid', 'ip'],
function ($, ko,ncrd,csscsof) {
	window.ko = ko;
	//与后台交互的路径
	var GET_TREE_URL = '/df/csofinfo/getTree.do';
	var GET_TAB_URL ='/df/csofinfo/getdisplaytitle.do';
	var GET_YEAR_URL = '/df/csofinfo/getyear.do';
	var GET_DATA_URL = '/df/csofinfo/getData.do';
	var testURL = '/df/cs/test.do';
	var getReportURL = '/ReportForm/selectEReportByChrId.do';//报表显示
	var selInfotreeNode,selPer,selMonth,reportFile,reportid,viewid;//定义获取报表url的参数
	var libData;//用于切换库获取预算单位的参数
	var infoDate = new Date();//获取当前年度
	var nowyear = infoDate.getFullYear();//默认显示当前年度
	var nowmonth = infoDate.getMonth()+1;//当前月份
	if(nowmonth == "1" || nowmonth == "2" || nowmonth == "3"){
		var nowque = "01";
	}else if(nowmonth == "4" || nowmonth == "5" || nowmonth == "6"){
		var nowque = "02";
	}else if(nowmonth == "7" || nowmonth == "8" || nowmonth == "9"){
		var nowque = "03";
	}else if(nowmonth == "10" || nowmonth == "11" || nowmonth == "12"){
		var nowque = "04";
	}
	if(nowmonth <= 6){
		var nowhyear = "01";
	}else if(nowmonth > 6){
		var nowhyear = "02";
	}
	if(nowmonth <10 ){
			nowmonth = "0"+nowmonth;
	}
	var reportData;//用于获取reportFile
	var str;
	
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
	var viewModel={
			treeKeyword: ko.observable(""),
			findTree: function () {
		            ncrd.findTreeNode($.fn.zTree.getZTreeObj("infotree1"), viewModel.treeKeyword());
		     },
		     infoTree1Setting: {
	                view:{
	                    showLine: false,
	                    selectedMulti: false,
	                    showIcon: false,
	                    showTitle: true
	                },
	                callback:{
	                    onClick:function(e,id,node){
	                    	selInfotreeNode = node.CHR_CODE;
	                    	if(!node.isParent){
	                    		selPer = $("#dataPeriod").val();
		                    	var monval = $('.showcolor').val();
		                    	if(monval < 10){
		                    		monval = "0" + monval;
		                    	}
		                    	selMonth = monval;
		                    	if($(".active").attr("isleaf") == "0"){
		                    		getreportFile($(".dropdown-menu .active").attr("value")); 
		                    	}else{
		                    		getreportFile($(".active").attr("value"));
		                    		//getreportFile(dom[0].id.split('a')[0]);
		                    	}
	                    	}else{
	                    		 ip.ipInfoJump("该节点报表不存在！", 'info');
	                    	}
	                    }
	                }
	            },
	            infoTree1DataTable: new u.DataTable({
	                meta: {
	                	'SET_YEAR':{
		                        'value':""
		                 },    
	                    'CHR_ID': {
	                        'value':""
	                    },
	                    'CHR_CODE':{
	                        'value':""
	                    },
	                    'DISP_CODE': {
	                        'value':""
	                    },
	                    'CHR_NAME':{
	                        'value':""
	                    },
	                    'LEVEL_NUM':{
	                        'value':""
	                    },
	                    'IS_LEAF':{
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
	                    'PARENT_ID': {
	                        'value':""
	                    },
	                    'MOFDEP_ID':{
	                        'value':""
	                    }
	                }
	            })
	};
	//获取树
	viewModel.getInfoTree = function(treeElecode){
		$.ajax({
            type: 'GET',
            url: GET_TREE_URL + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
            data: {"ele_code": treeElecode},
            dataType: 'JSON',
            async: false,
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
            		viewModel.infoTree1DataTable.setSimpleData(dep_array);
            		viewModel.infoTree1DataTable.setSimpleData(dep_array,{unSelect:true});
            		var treeObj = $.fn.zTree.getZTreeObj("infotree1");
                    treeObj.expandAll(true);
            	}
            }, error: function () {
                ip.ipInfoJump("错误", 'error');
            }
        });
	}
	//库列表
	viewModel.getAllLibrary = function(eleCode) {
		$.ajax({
          type: 'POST',
          url: GET_DATA_URL + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
          data: {"ele_code":eleCode},
          dataType: 'JSON',
          async: false,
          success: function (result) {
        	  if(result.data.length > 0) {
        		  var libraryData = result.data;
        		  libData = result.data;
				  for(var i = 0 ; i < libraryData.length; i++) {
  						$("#library").append("<option>"+libraryData[i].CHR_NAME+"</option>");
					}
        		  if(libraryData[0].CHR_ID) {
						var index = libraryData[0].ELE_CODE.lastIndexOf("\_");  
        				str  = libraryData[0].ELE_CODE.substring(index + 1, libraryData[0].ELE_CODE.length);
        				viewModel.getInfoTree(libraryData[0].ELE_CODE);
        				viewModel.getTab(libraryData[0].CHR_ID);
        		  }
        	  }
        	  else{
        		  
        	  }
          },
          error: function(){
        	  
          }
		});
	};
	//切换库
	viewModel.method = function() {
		var val=$("#library").val();
		if(libData) {
			for(var i = 0; i < libData.length; i++) {
				if(libData[i].CHR_NAME == val) {
					var index = libData[i].ELE_CODE.lastIndexOf("\_");  
					str  = libData[i].ELE_CODE.substring(index + 1, libData[i].ELE_CODE.length);
					viewModel.getInfoTree(libData[i].ELE_CODE);
					viewModel.getTab(libData[i].CHR_ID);
				}
			}
		}
		else{
			
		}
	};

	//获取期间
	viewModel.getALLPeriod = function(){
		$.ajax({
	          type: 'GET',
	          url: GET_YEAR_URL + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
	          data: {"ele_code":"CSOF_YEAR"},//ele_code对应业务年度
	          dataType: 'JSON',
	          async: false,
	          success: function (result) {
			    if(result.data.length>0){
					var periodData = result.data;
					//$("#dataPeriod").append('<option selected>' + 2016 + '</option>');
					for(var i=0;i<periodData.length;i++){
						$("#dataPeriod").append("<option value='"+ periodData[i].SET_YEAR +"'>"+periodData[i].YEAR_NAME+"</option>");
					}
				}
				else{
					ip.ipInfoJump("没有查到数据！", 'error');
				}
			  },
			  error: function(){
				
			  }
		});
	};
	

	/*
	 * 根据库的CHR_ID去获取页签
	 * 参数chrId
	 */
	viewModel.getTab = function (chrId){
		$("#myTab").html("");
		$("#myTabContent").html("");
		$.ajax({
	          type: 'GET',
	          url: GET_TAB_URL + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
	          data: {"chr_id":chrId},
	          dataType: 'JSON',
	          async: false,
	          success: function (result) {
				var tabData = result.data;
				reportData = tabData;
				if(tabData.length>0) {
					var secLevel = [];//显示为第二级的页签
					var firLevel = [];//显示为第一级的页签
					for(var j = 0 ; j < tabData.length; j++){
						if(tabData[j].PARENT_ID) {
							secLevel.push(tabData[j]);
						}
						else{
							firLevel.push(tabData[j]);
						}
					}
					//通过IS_LEAF去判断能不能点击页签，有子节点则不能点击,去掉href属性
					//先判断默认的第一个
					if(firLevel[0].IS_LEAF == 1){
						//firLevel[0].CHR_ID == 1
						$("#myTab").append("<li class='active' id='"+firLevel[0].CHR_ID+"a"+"' isLeaf='1' value='"+firLevel[0].CHR_ID+"'><a href='#"+firLevel[0].CHR_ID+"' onclick=\"getreportFile('"+firLevel[0].CHR_ID+"')\" data-toggle='tab'>"+firLevel[0].DISPLAY_TITLE+"</a></li>");
						$("#myTabContent").append("<div class='tab-pane fade in active' id='"+firLevel[0].CHR_ID +"'></div>");
						reportid = firLevel[0].CHR_ID;
						var selMonth2 = initBbqDate(reportid);
//						getreportFile(reportid);
						if(firLevel[0].REPORT_TYPE == 3){
							$("#gridTabContent").show();
							$("#myTabContent").hide();
							$("#excelTabContent").hide();
							viewid = firLevel[0].REPORT_FILE;
						}else{
							$("#gridTabContent").hide();
							$("#myTabContent").show();
							$("#excelTabContent").hide();
						}
					}
					else{
						$("#myTab").append("<li id='"+firLevel[0].CHR_ID+"a"+"' isLeaf='0' value='"+firLevel[0].CHR_ID+"' class='active'><a data-toggle='tab'>"+firLevel[0].DISPLAY_TITLE+"</a></li>");						
					}
					//再判断其他的
					for(var i = 1 ; i < firLevel.length; i++){
						if(firLevel[i].IS_LEAF == 1){
							$("#myTab").append("<li id='"+firLevel[i].CHR_ID+"a"+"' isLeaf='1' value='"+firLevel[i].CHR_ID+"'><a href='#"+firLevel[i].CHR_ID+"' data-toggle='tab' onclick=\"getreportFile('"+firLevel[i].CHR_ID+"')\" >"+firLevel[i].DISPLAY_TITLE+"</a></li>");
							$("#myTabContent").append("<div class='tab-pane fade ' id='"+firLevel[i].CHR_ID +"'></div>");
						}
						else{
							$("#myTab").append("<li id='"+firLevel[i].CHR_ID+"a"+"' isLeaf='0' value='"+firLevel[i].CHR_ID+"'><a  data-toggle='tab'>"+firLevel[i].DISPLAY_TITLE+"</a></li>");
						}
					}
					//将第二级页签放在第一级下面				
					 for (var m = 0;m < secLevel.length; m++) {
			                for (n = 0; n < firLevel.length; n++) {
			                    if (firLevel[n].CHR_ID == secLevel[m].PARENT_ID) {
			                    	$("#myTabContent").append("<div class='tab-pane fade' id='"+secLevel[m].CHR_ID +"'></div>");
			                    	var list = $('#'+firLevel[n].CHR_ID+"a").find("ul");
			                    	//判断对应li下是否已经存在ul
			                    	if(list.length > 0){
			                    		$('#'+firLevel[n].CHR_ID+"a").find("ul").append("<li isLeaf='1' value='"+secLevel[m].CHR_ID+"'><a href='#"+secLevel[m].CHR_ID+"' onclick=\"getreportFile('"+secLevel[m].CHR_ID+"')\" tabindex='-1' data-toggle='tab'>"+secLevel[m].DISPLAY_TITLE+"</a></li>");
			                    	}
			                    	else{
			                    		$('#'+firLevel[n].CHR_ID+"a").addClass('dropdown');
			                    		$('#'+firLevel[n].CHR_ID+"a").removeAttr("data-toggle","tab");
			                    		$('#'+firLevel[n].CHR_ID+"a").find("a").attr("id",firLevel[n].CHR_ID);
			                    		$('#'+firLevel[n].CHR_ID+"a").find("a").attr("data-toggle","dropdown");
			                    		$('#'+firLevel[n].CHR_ID+"a").find("a").addClass("dropdown-toggle");
			                    		$('#'+firLevel[n].CHR_ID+"a").find("a").append("<b class='caret'></b>");
			                    		$('#'+firLevel[n].CHR_ID+"a").append(
													"<ul class='dropdown-menu' role='menu' aria-labelledby='"+firLevel[n].CHR_ID+ "'>" +
														"<li isLeaf='1' value='"+secLevel[m].CHR_ID+"'><a href='#"+secLevel[m].CHR_ID+"' onclick=\"getreportFile('"+secLevel[m].CHR_ID+"')\" tabindex='-1' data-toggle='tab'>"+secLevel[m].DISPLAY_TITLE+"</a></li>" +
													"</ul>"
										);
			                    	}
			                    }
			                }
			            }	
				}
				else{
					ip.ipInfoJump("没有查到数据！", 'error');
					$("#myTab").html('');
					$('#myTabContent').html('');
				}
			},
			error: function(){
				
			}
		});
	}
	//先突出显示月份
	viewModel.monthshownow = function(){
		$("#a"+nowmonth).addClass('showcolor');
	}
	
	//初始化bbq-date显示
	initBbqDate = function(chrid){
		var id = chrid;
		$(".month").each(function () {
            $(this).removeClass('showcolor');
        });
		$(".que").each(function () {
            $(this).removeClass('showcolor');
        });
		$(".hyear").each(function () {
            $(this).removeClass('showcolor');
        });
		var selMonth2 = selMonth;
		for(var r = 0;r<reportData.length;r++){
			if(id == reportData[r].CHR_ID){
				if(reportData[r].REPORT_CYCLE == "1"){
					$("#month-ul").hide();
					$("dataPeriod").show();
					$("#que-ul").hide();
					$("#hyear-ul").hide();
					selMonth2 = "";
				}else if(reportData[r].REPORT_CYCLE == "2"){
					$("#month-ul").hide();
					$("dataPeriod").show();
					$("#que-ul").show();
					$("#hyear-ul").hide();
					$("#q"+nowque).addClass('showcolor');
					selMonth2 = $('.showcolor').val();
				}else if(reportData[r].REPORT_CYCLE == "3"){
					$("#month-ul").show();
					$("dataPeriod").show();
					$("#que-ul").hide();
					$("#hyear-ul").hide();
					$("#a"+nowmonth).addClass('showcolor');
					selMonth2 = $('.showcolor').val();
				}else if(reportData[r].REPORT_CYCLE == "7"){
					$("#month-ul").hide();
					$("dataPeriod").show();
					$("#que-ul").hide();
					$("#hyear-ul").show();
					$("#h"+nowmhyear).addClass('showcolor');
					selMonth2 = $('.showcolor').val();
				}else{
					$("#month-ul").show();
					$("dataPeriod").show();
					$("#que-ul").hide();
					$("#hyear-ul").hide();
					$("#a"+nowmonth).addClass('showcolor');
					selMonth2 = $('.showcolor').val();
				}
			}
		}
		return selMonth2;
	}
	//拿到report_file
	getreportFile = function(chrid){
			var selMonth2 = initBbqDate(chrid);
			if(selMonth2 < 10) {
				selMonth2 = "0"+selMonth2;
			}
			reportid = chrid;
			if(selInfotreeNode) {
				if(selMonth2 == "" || selMonth2 == "undefined" || selMonth2 == null || selMonth2 == "0"){
	        		viewModel.showReport(reportid,selPer,"----",true,selInfotreeNode);
	        	}else{
	        		viewModel.showReport(reportid,selPer,selMonth2,true,selInfotreeNode);
	        	}
			}
			else{
				ip.ipInfoJump("请选择需要操作的单位", 'info');
			}
	} ;
	//切换年度
	periodChange = function(){
		 var periodVal = $("#dataPeriod").val();
		 selPer = periodVal;
		 if(selInfotreeNode){
				if(selMonth == "" || selMonth == "undefined" || selMonth == null){
            		viewModel.showReport(reportid,selPer,"----",true,selInfotreeNode);
            	}else{
            		viewModel.showReport(reportid,selPer,selMonth,true,selInfotreeNode);
            	}
		}
		else{
			ip.ipInfoJump("请选择需要操作的单位", 'info');
		}
	};
	//点击月份
	changeMonth = function(id){
		var id= id;
		$('#'+id).siblings().removeClass('showcolor');
		$('#'+id).addClass('showcolor');
		var mval=$("#"+id).val();
		if(mval < 10){
			mval = "0"+mval;
		}
		if(selInfotreeNode){
			if(mval == "" || mval == "undefined" || mval == null){
        		viewModel.showReport(reportid,selPer,"----",true,selInfotreeNode);
        	}else{
        		viewModel.showReport(reportid,selPer,mval,true,selInfotreeNode);
        	}
	    }
		else{
			ip.ipInfoJump("请选择需要操作的单位", 'info');
		}
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
            	viewModel.listGridViewModel = ip.initGridWithCallBack(gridCallback,viewModel.listViewList[n],'gridTabContent', '/df/common/commonAction.do?', options, 1, false,false,false,false);
                if(viewModel.listGridViewModel.gridData.getSimpleData()){
//                    $("#mainGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);
                }
            }    
        }
    };
    gridTabContent_onDbClick = function(obj) {
    	console.log(obj);
    	$("#gridTabContent").hide();
		$("#myTabContent").hide();
		$("#excelTabContent").show();
    	$("#rowName").html(obj.rowObj.value.agency_name);
    	newExcelShow(obj);
    };
    
    newExcelShow = function(obj) {
    	$("#comeIfr")[0].src = "";
        var execlData = {
				"entity_id":obj.rowObj.value.bill_id,
				"oid":options.svOfficeId,
				"dep_id":options.svDepId,
				"dep_code":options.svDepCode,
				"edit":'N',
				"modular": 'INFO',
				"billtypeCode":obj.rowObj.value.billtype_code,
				"billId":obj.rowObj.value.bill_id,
				"objtypeId":obj.rowObj.value.objtype_id,
				"objId":obj.rowObj.value.obj_id,
				"supCycle":obj.rowObj.value.sup_cycle,
				"supDate":obj.rowObj.value.sup_date,
				"saveBtn":"N",
				"isImp":"N",
				"viewId":obj.rowObj.value.view_id,
		    };
        $("#comeIfr")[0].src = "/df/supervise/newExcel/excel.html?tokenid=" + tokenid +"&menuid=" + options.svMenuId +
        "&menuname=" + options.svMenuName+"&entity_id="+execlData.entity_id+"&entityName=csof_sup_bill&oid="+execlData.oid+
        "&dep_id="+execlData.dep_id+"&dep_code="+execlData.dep_code+"&modelFlag=0&admin="+execlData.edit+"&modular="+execlData.modular+
        "&saveBtn="+execlData.saveBtn+"&billtypeCode="+execlData.billtypeCode+"&billId="+execlData.billId+"&objtypeId="+execlData.objtypeId+
        "&objId="+execlData.objId+"&supCycle="+execlData.supCycle+"&supDate="+execlData.supDate+"&isImp="+execlData.isImp+"&viewId="+execlData.viewId;
    };
    
    back = function() {
    	$("#gridTabContent").show();
		$("#myTabContent").hide();
		$("#execlTabContent").hide();
    	viewModel.gridRefresh(selInfotreeNode,viewid);
    };
    
//    //grid查询
//    viewModel.gridSearch = function () {
//        ip.fuzzyQuery(viewModel.curGridData, "gridSearchInput", viewModel.listGridViewModel);
//    };

    //回调函数，定义全局变量viewModel.curGridData并赋值
    gridCallback = function(data){
        viewModel.curGridData = data;
    };

    //刷新Grid表格数据
    viewModel.gridRefresh = function(selInfotreeNode,viewid) {
    	var conditionMap = {"AGENCY_CODE": selInfotreeNode,"VIEW_ID":viewid,"OID":options.svOfficeId};
    	var conditionRela = {"AGENCY_CODE": "0","VIEW_ID":"0","OID":"0"};

    	var data ={
    			"action" : "Q",
    			"billtype_code" : "901002",
    			"condition_data": JSON.stringify(conditionMap),
    			"condition_rela":JSON.stringify(conditionRela)
    	}
        ip.setGridWithCallBack(gridCallback,viewModel.listGridViewModel, '/df/common/commonAction.do?tokenid='+ tokenid + '&ajax=' + 'noCache',data);
       // $("#mainGridSumNum").html(viewModel.listGridViewModel.gridData.getSimpleData().length);
    };
	
	//报表显示
	viewModel.showReport = function(rid,year,month,readonly,obj_id) {
    	/*var data = {
				"chrId"	:rid,
				"obj_id" :obj_id,
				"param_add_str":"",
				"bbq_date":year+month,
				"readonly":readonly,
			};
			$.ajax({
		        type: 'post',
		        url: getReportURL + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
		        data: data,
		        dataType: 'JSON',
		        async: false,
		        success: function (result) {
		            if (result.errorCode == 0) {
		            	$("#"+rid+"").html(result.data);
//		            	$("#"+rid+"").html("");
//		            	$("#"+rid+"").append("<iframe style='width:100%;height:470px;overflow:auto;border: 0;' id='url_iframe_"+rid+"'></iframe>");
//             			$("#url_iframe_"+rid)[0].src = result.data;
		            } else {
		                ip.ipInfoJump("错误", 'error');
		            }
		        }, error: function () {
		            ip.ipInfoJump("错误", 'error');
		        }
		    });*/
		
		//10.17修改
		for(var i = 0 ; i < reportData.length; i++){
			if(rid == reportData[i].CHR_ID){
				var report_type = reportData[i].REPORT_TYPE;
				var rData = reportData[i];
				break;
			}
		}
		if(report_type == "3") {
			$("#gridTabContent").show();
			$("#myTabContent").hide();
			$("#execlTabContent").hide();
			viewid = rData.REPORT_FILE;
			viewModel.gridRefresh(selInfotreeNode,rData.REPORT_FILE);
			console.log(rid,year);
			console.log(selInfotreeNode);
			
//			var data = {
//					  action : "QUERY",
//					  billtype_code : "901002",
//					  condition_data : {"AGENCY_CODE": selInfotreeNode }
//			}
//			console.log(data);
//			$.ajax({
//		        type: 'GET',
//		        url: "/df/common/commonAction.do" + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
//		        data: data,
//		        dataType : "json",
//                async : true,
//		        success: function (result) {
//		        	console.log(result);
//		            if (result.errorCode == 0) {
//                        
//		            } else {
//		                ip.ipInfoJump("错误", 'error');
//		            }
//		        }, error: function () {
//		            ip.ipInfoJump("错误", 'error');
//		        }
//			});
		}else{
			$("#gridTabContent").hide();
			$("#myTabContent").show();
			$("#execlTabContent").hide();
			var data = {
					"chrId"	:rid,
					"obj_id" :obj_id,
					"param_add_str":"",
					"bbq_date":year+month,
					"readonly":readonly,
				};
			$("#"+rid+"").html("");
	    	$("#"+rid+"").append("<iframe style='width:100%;height:470px;overflow:auto;border: 0;' id='url_iframe_"+rid+"'></iframe>");
			$("#url_iframe_"+rid).attr("src", getReportURL + "?tokenid=" +  ip.getTokenId() + "&ajax=noCache" + "&chrId="+rid+"&obj_id="+obj_id+"&param_add_str=" +""+"&bbq_date="+(year+month)+"&readonly="+readonly);
		}
	};
	
//	/*
//	 * 显示报表
//	 * 参数，必须有4个，左边预算单位树节点，期间，月份,reportFile(报表)
//	 * 分别定义为selInfotreeNode,selPer,selMonth,reportFile;
//	 *
//	 */
//	viewModel.showReport = function(treenode,year,month,file){
//		console.log(treenode+year+month+file);
//		$.ajax({
//			            type: 'GET',
//			            url: testURL + "?tokenid=" + ip.getTokenId() + "&ajax=noCache",
//			            data: {"rid":file,"bbq":year+month},
//			            dataType: 'JSON',
//			            async: false,
//			            success: function (result) {
//			                if (result.data != null) {
//			                    var report_url = result.data;
//								if(treenode){
//									report_url +=  "&"+ str+"_ID=" +treenode;
//								}
//								else{
//									report_url +=  "&"+ str+"_ID=" +null;
//								}
//			                    console.log(report_url);
//			                    for(var i=0;i<reportData.length;i++){
//			            			if(file == reportData[i].REPORT_FILE){
//			            				 $("div #"+reportData[i].CHR_ID+"").html('');
//			            				//$("div #"+reportData[i].CHR_ID+"").append("<iframe style='margin-top:20px;width:90%;height:400px;margin-left:5%;overflow-y:auto;' id='url_iframe'></iframe>");
//				            			// $("#url_iframe")[0].src = report_url;
//			            				 $("div #"+reportData[i].CHR_ID+"").append("<p style='border:1px solid #ccc;height:200px;'>" +report_url+"</p>");
//			            			}
//			            		}
//			                } else {
//			                    ip.ipInfoJump("错误", 'error');
//			                }
//			            }, 
//						error: function () {
//			                ip.ipInfoJump("错误", 'error');
//			            }
//			        });		
//	};
	
	 function init(){
			var app = u.createApp(
					{
						el: document.body,
						model: viewModel
					}
			);
			var url=window.location.href;
			console.log(url);
			var eleCode = url.split("elecode=")[1].split("&")[0];//获取登录路径参数
			viewModel.getAllLibrary(eleCode);//获取库
			viewModel.getALLPeriod();//获取期间
			//viewModel.monthshownow();
			tokenid = ip.getTokenId();
            options = ip.getCommonOptions({});
            options['tokenid'] = tokenid;
            viewModel.initData();
            var h = document.body.clientHeight; //获取当前窗口可视操作区域高度
            $('#main-content').jqxSplitter({ width: '98.3%', height: (h - 25) + "px", panels: [{ size: '23.2%', min: 200 }, { min: 800, size: '76.8%' }] });
            $('.treeDiv').height($("#left-main-content").height() - 79 + "px");
            $("#infoShowReport").height($("#info-showDetail").height() - 47 + "px")
            var height = $("#infoShowReport").height() - $("#myTab").height();
            $("#myTabContent").css("height", height);
            $("#excelTabContent").css("height", height);
            $("#gridTabContent").css("height", height);
            height = $("#excelTabContent").height() - $("#rowinfo").height();
            $("#comeIfr").css("height", height);
	    };

	    init();
});