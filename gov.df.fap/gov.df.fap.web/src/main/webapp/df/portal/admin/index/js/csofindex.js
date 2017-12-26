var menuThreeLevel;
var tokenId = "";
var roleList = roleList || {};
var supButtonData = new Array();
var accButtonData = [];

//即时通讯聊天工具 add by yanyga 
var setRuleApp;

//获取tokenId方法
function getTokenId(){
	//return Portal.tokenid.getTokenId();
	return dfp.tokenid.getTokenId();
}

// 生成tab页
function addTabToParent(title, url){
	if(title==""||url==""){
		alert("data-title属性不能为空或data-href属性不能为空");
		return false;
	}
	var tokenId = getTokenId();
	
	var bStop = false,
		bStopIndex = 0,
		href = url,
		title = title,
		topWindow = $(window.parent.document),
		show_navLi = topWindow.find("#min_title_list li"),
		iframe_box = topWindow.find("#iframe_box");
	show_navLi.each(function() {
		if($(this).find('span').attr("data-href")==href){
			bStop=true;
			bStopIndex=show_navLi.index($(this));
			return false;
		}
	});
	if(!bStop){
		creatIframe(href,title);
		min_titleList();
	}else{
		show_navLi.removeClass("active").eq(bStopIndex).addClass("active");			
		iframe_box.find(".show_iframe").hide().eq(bStopIndex).show().find("iframe").attr("src",href);
	}
	scroll(0,0);
}

var pt_index = {
	bf : function(){
		//top栏事件
		$(".nav .dropdown a").click(function(){
			var i = $(this).parent("li").index();
			$(".toggle").each(function(){
				$(this).hide();
			});
			if(!$(".toggle").eq(i).hasClass("null")){
				$(".toggle").eq(i).show();
			}			
		});
		$(".toggle").mouseleave(function(){
			$(".toggle").each(function(){
				$(this).hide();
			});
		});

		// 右上三图标
		$("#topDanju").click(function() {
			//$(this).find("span#topDanjuCount").hide();
			addTabToParent("我的单据", "/df/sd/pay/order/order.html?billtype=366&busbilltype=322&menuid=2E8B00AE30A562200CC558307069B4D9&menuname=%u6211%u7684%u5355%u636E&wfStatus=201&tokenid="+getTokenId());
		});
		$("#topXiaoxi").click(function() {
			$(this).find("span#topXiaoxiMore").hide();
			//addTabToParent("消息提醒", "http://www.baidu.com");
		});
		$("#topChat").click(function() {
			var im_username=Base64.decode($("#svUserId").val());
			loginIm(im_username,'', false);
		});
		
	},
	init : function(){
		this.bf();
		var caroleguid = localStorage.select_role_guid==undefined?"":localStorage.select_role_guid;
		$.ajax({
			url : "/df/portal/csofInitIndex.do",
			type : "GET",
			data : {"tokenid":getTokenId(), "caroleguid":Base64.encode(caroleguid)},
			dataType : "json",
			success : function(data){
				//portal.co.tokenid.isValid(data.msg);
				portal.pp.hiddenLabel(data.publicParam);
				portal.pp.commonData(data.publicParam);
            	$("#usernameTop").html(data.publicParam.svUserName);
            	$("#showOfficeName").html(data.publicParam.svOfficeName);
            	$("#showDepName").html(data.publicParam.svDepName);
            	if(data.publicParam.svAgencyName){
            		$("#agencyameTop").text(data.publicParam.svAgencyName);
            	}else{
            		$("#agencyameTop").parent("a").css("display", "none");
            	}
            	portal.co.time.current();
            	portal.role.show(data.roleList);
            	//portal.ag.tree();
            	portal.menu.show();
            	
//            	var _url_dashboard = dfp.Object.isNull(data.dashboardUrl)?"/df/portal/admin/index/dashboardJB.html":data.dashboardUrl;
//            	$("#iframeDashboard").prop("src", _url_dashboard+"?tokenid="+getTokenId());
            	// -----------------------
            	//$("#iframeDashboard").prop("src", "/df/portal/admin/index/dashboardJB.html?tokenid="+getTokenId());
            	//$("#iframeDashboard").prop("src", "/df/portal/admin/index/dashboardJZZF.html?tokenid="+getTokenId());
            	//$("#iframeDashboard").prop("src", "/df/portal/admin/index/dashboardGKLeader.html?tokenid="+getTokenId());
            	//$("#iframeDashboard").prop("src", "/df/portal/admin/index/dashboardGKJB.html?tokenid="+getTokenId());
            	//$("#iframeDashboard").prop("src", "/df/portal/admin/index/dashboardZGBM.html?tokenid="+getTokenId());
            	
            	//add BY yanyga
            	//模板加载函数
							require(['jquery'], function($) {
                var container = $('#imchat');
                var url = "/df/fap/messageplatform/messageTemplete.js";
                requirejs.undef(url);
                require([url], function (module) {
                  ko.cleanNode(container[0]);
                  container.html('');
                  container.html(module.template);
                  // module.init(container[0]);
                  setRuleApp = module;
                });
							})
            	// setTimeout(function(){
            	//     var container = $('#imchat');
            	//     var url = "/df/fap/messageplatform/messageTemplete.js";
            	//     requirejs.undef(url);
            	//     require([url], function (module) {
            	//         ko.cleanNode(container[0]);
            	//         container.html('');
            	//         container.html(module.template);
            	//        // module.init(container[0]);
            	//         setRuleApp = module;
            	//     });
            	// },5000);


			}
		});
	}
};

/**
 * common: tokenid time
 */
var pt_common = {
	tokenid : {
		isValid : function(msg){
			//if(msg==dfp.key.tokenidPassed){
			if(msg=="tokenid_passed"){
				alert("tokenId 已失效，请重新登录");
				portal.user.logout();
				window.location.href = "/";
				return;
			}
		}
	},
	time : {
		current : function(){
			//portalTopTime();
			var _xx = setInterval(function() {
				portalTopTime();
			}, 1000);
		}
	},
	roleUrl : { // role to index
		"800801001" : "/df/portal/admin/index/indexJB.html",
		"800801004" : "/df/portal/admin/index/indexSH.html",
		"name" : "经办|审核",
		"0" : "/df/portal/admin/index/indexJB.html",
		"1" : "/df/portal/admin/index/indexSH.html"
	}
}

/**
 * public params
 */
var pt_pubparams = {
	commonData : function(data){
		// TODO 是否有效
		localStorage.select_role_guid = Base64.decode($("#svRoleId").val());
		var _data = JSON.stringify(data);
		if(!localStorage.commonData){
			localStorage.commonData = _data;
		} else {
			localStorage.commonData = _data;
		}
	},
	hiddenLabel : function(data){
        //$("#loginReport").attr("src", "/ReportForm/selectEReportByUserType.do" + "?tokenid=" +  getTokenId() + "&ajax=noCache" + "&userType="+"sys"+"&dep_code="+data.svDepCode+"&user_code="+data.svUserCode);
		$("#svFiscalPeriod").val(dfp.encrypt.base64.encode(data.svFiscalPeriod));	// 会计期间
		$("#svMenuId").val(dfp.encrypt.base64.encode(data.svMenuId));	// 菜单ID
		$("#svRgCode").val(dfp.encrypt.base64.encode(data.svRgCode));	// 区划CODE
		$("#svRgName").val(dfp.encrypt.base64.encode(data.svRgName));	// 区划ID
		$("#svRoleCode").val(dfp.encrypt.base64.encode(data.svRoleCode));	// 角色CODE
		$("#svRoleId").val(dfp.encrypt.base64.encode(data.svRoleId));	// 角色ID
		$("#svRoleName").val(dfp.encrypt.base64.encode(data.svRoleName));	// 角色名称
		$("#svSetYear").val(dfp.encrypt.base64.encode(data.svSetYear));	// 年度
		$("#svTransDate").val(dfp.encrypt.base64.encode(data.svTransDate));	// 业务日期
		$("#svUserCode").val(dfp.encrypt.base64.encode(data.svUserCode));	// 用户CODE
		$("#svUserId").val(dfp.encrypt.base64.encode(data.svUserId));	// 用户ID
		$("#svUserName").val(dfp.encrypt.base64.encode(data.svUserName));	// 用户名称
		$("#svAgencyId").val(dfp.encrypt.base64.encode(data.svAgencyId));	// 单位Id
		$("#svAgencyCode").val(dfp.encrypt.base64.encode(data.svAgencyCode));	// 单位Code
		$("#svAgencyName").val(dfp.encrypt.base64.encode(data.svAgencyName));	// 单位名称
		$("#svAgencyId").val(dfp.encrypt.base64.encode(data.svAgencyId));	// 单位Id		
	}
};

/**
 * agency
 */
var pt_agency = {
	bf : function(){
		// 单位选择
		$('body').off('click').on('click','#department',function(e){
			var displayCss = $("#userDanweiTreeDiv").css("display");
			if(displayCss=='none'){
				portal.ag.tree();
				$("#userDanweiTreeDiv").show();
				e = e || event;
				e.stopPropagation ? e.stopPropagation() : e.cancelBubble = true;
				window.onclick = function() {
					$("#department").unbind("click");
					$("#userDanweiTreeDiv").hide();
				}
			}else{
				$("#userDanweiTreeDiv").hide();
			}
		});
		$("#userDanweiTreeDiv").bind("click", function(e) {
			e = e || event;
			e.stopPropagation ? e.stopPropagation() : e.cancelBubble = true;
		});
		// 单位选择确认
		$("#userDanweiSelectSubmit").click(function(){
			var treeObj = $.fn.zTree.getZTreeObj("userDanweiTree");  
            var selectedNode = treeObj.getSelectedNodes()[0];
            
            if(!selectedNode){
            	return;
            }
            
            //alert(selectedNode.name);
            //$("#userDanweiSelectInput").val();
            
            $("#userDanweiTreeDiv").hide();
		});
		$("#userDanweiSelectClose").click(function(){
			$("#userDanweiTreeDiv").hide();
		});
	},	
	tree : function(){
		this.bf();
		var setting = {
			view: {
				dblClickExpand: false,
				showLine: false,
				selectedMulti: false,
				showIcon: false
			},
			data: {
				simpleData: {
					enable:true,
					idKey: "id",
					pIdKey: "pId",
					rootPId: ""
				}
			},
			callback: {  
				onDblClick: function(event, treeId, treeNode){
                    $("#userDanweiSelectSubmit").click();
                }
            }
		};
		$.ajax({
			url:"/df/portal/getAllCompony.do",	// 获取用户全部单位信息
			type:"GET",
			data:{"tokenid":getTokenId(), "userid":$("#svUserId").val()},
			dataType: "json",
			success:function(data){
				var componyList = data.componyList;
				var zNodes = [];
				for(var i in componyList){
					if(!componyList.hasOwnProperty(i)){
						continue;
					}
					zNodes.push({id:componyList[i].chr_id, pId:componyList[i].parent_id, name:(componyList[i].chr_code+" "+componyList[i].chr_name)});
				}
				var treeObj = $.fn.zTree.init($("#userDanweiTree"), setting, zNodes);
				treeObj.expandAll(true);
			}
		});
	}
};

/**
 * role
 */
var pt_role = {
	bf : function(){
		$("#switchRoleLi").click(function(e){
			var $div = $(this).find("div#roleSwitchDiv");
				display = $div.css("display");
			if(display == "block"){
				$div.hide();
			}else{
				$div.show();
			}
			e = e || event;
			e.stopPropagation ? e.stopPropagation() : e.cancelBubble = true;
		});
//		$(document).click(function(e){
//			$(this).find("div#roleSwitchDiv").hide();
//			e = e || event;
//			e.stopPropagation ? e.stopPropagation() : e.cancelBubble = true;
//		});
		$("#switchRoleLi, #roleSwitchDiv").mouseleave(function(e){
			$("#roleSwitchDiv").hide();
			e = e || event;
			e.stopPropagation ? e.stopPropagation() : e.cancelBubble = true;
		});
	},
	show : function(roleList){
		this.bf();
//		if(dfp.Object.isNull(roleList)){
//			$.ajax({
//				url : "/df/portal/switchRole.do",
//				type : "GET",
//				dataType : "json",
//				data : {"tokenid" : getTokenId()},
//				async: false,
//				success : function(data) {
//					roleList = data.rlist;
//				}
//			});
//		}
		
		$("#roleSwitch").html("");
		var html = "";
		for(var i in roleList){
			if(!roleList.hasOwnProperty(i)){
				continue;
			}
			html += '<li><a href="javascript:switchRoleConfirm(&quot;'+(roleList[i]).role_ID+'&quot;);">' + (roleList[i]).role_NAME + '</a></li>';
		}
		$("#roleSwitch").html(html);
	},
	_switch : function(roleid){
		if(roleid == Base64.decode($("#svRoleId").val())){
			return;
		}
		$.ajax({
			url : "/df/portal/switchRoleConfirm.do",
			type : "GET",
			data : {"tokenid":getTokenId(), "roleid":roleid},
			dataType : "json",
			success : function(data){
				localStorage.select_role_guid = roleid;
				window.location.href="/df/portal/admin/index/index.html?tokenid="+data.tokenid;
			}
		});
	}
};

/**
 * menu
 */
var pt_menu = {
	level_menu3 : function(menuListnow,menuList){
		if(menuListnow.screentype == "SUP"){
			var data = {
				"user_id" : JSON.parse(localStorage.commonData).svUserId,
				"oid" : JSON.parse(localStorage.commonData).svOfficeId,
				"menu_id" : menuListnow.guid,
			}
			$.ajax({
	            type: 'GET',
	            url: "/df/user/getMenu.do" + "?tokenid=" + getTokenId() + "&ajax=noCache",
	            data: data,
	            dataType: 'JSON',
	            async: false,
	            success: function (result) {
	            	var data = result.data;
	                if(result.data != null){
	                	var taskNum = "";
	                	var taskNo = 1;
	                	var flag = 0;
	                	supButtonData = new Array();
	                	for(var j=0;j<data.length;j++) {
	                		if(data[j].TASK_ID == taskNum) {
	                			$("#"+data[j].TASK_ID+"_span").append("<button id='"+data[j].MENU_ID+"_"+taskNum+"_button' class='btn csof-btn' style='margin: 2px;width: 53px;text-align: center;padding: 0;'>"+data[j].SHORT_NAME+"</button>");
	                			flag++;
	                			taskNo++;
	                		}else{
	                			taskNum = data[j].TASK_ID;
	                			$("#"+menuListnow.screentype+"_menu").append("<span class='pull-left' id='"+data[j].TASK_ID+
	                					"_span' style='margin: 5px;'><div class='menu-item-block'>"+data[j].TASK_NAME+"</div><button class='btn csof-btn' id='"+data[j].MENU_ID+"_"+taskNum+"_button' style='margin: 2px;width: 53px;text-align: center;padding: 0;'>"+data[j].SHORT_NAME+"</button></span>");
	                			flag = 1;
	                		}
	                		if(flag == 3){
	            				$("#"+data[j].TASK_ID+"_span").append("<br>");
	            				flag =0;
	            			}
	                		var menuId = data[j].MENU_ID;
	                		var menuName = data[j].MENU_NAME;
	                		var sid = data[j].SID;
	                		var screenType = menuListnow.screentype;
	                		var menuCode = data[j].MENU_CODE;
	                		var bbq_date = data[j].SUP_DATE;
	                		var sup_name = data[j].SUP_NAME;
	                		var sup_no = data[j].SUP_NO;
	                		var task_id = data[j].TASK_ID;
	                		var task_name = data[j].TASK_NAME;
	                		var obj_type_id = data[j].OBJ_TYPE_ID;
	                		var url = data[j].URL;
	                		var data1 = {
	                				"menuCode":menuCode,
	                				"menuId":menuId,
	                				"menuName":menuName,
	                				"sid":sid,
	                				"screenType":screenType,
	                				"bbq_date" :bbq_date,
	                				"task_id" :task_id,
	                				"task_name" :task_name,
	                				"url":url,
	                				"SHORT_NAME":data[j].SHORT_NAME,
	                				"IS_ALLOBJ" :data[j].IS_ALLOBJ,
	                				"OBJ_TYPE_ID" :data[j].OBJ_TYPE_ID,
	                		};
	                		supButtonData.push(data1);
	                		$("#"+data[j].MENU_ID+"_"+data[j].TASK_ID+"_button").click(supButtonData,function(event){		
	                			var menuNo = event.currentTarget.id.split("_")[0];
	                			var taskId = event.currentTarget.id.split("_")[1];
	                			for(var bd = 0;bd<event.data.length;bd++){
	                				if(event.data[bd].menuId == menuNo && event.data[bd].task_id == taskId){
	                					addTabToParent(event.data[bd].task_name+event.data[bd].SHORT_NAME,event.data[bd].url+"tokenid=" 
					                			+ getTokenId() +"&menuid=" 
					                			+ event.data[bd].menuId +"&menuname=" 
					                			+ event.data[bd].menuName+"&pageId="
					                			+event.data[bd].menuId+"&SID="+event.data[bd].sid+"&IS_ALLOBJ="+
					                			event.data[bd].IS_ALLOBJ+"&OBJ_TYPE_ID="+
					                			event.data[bd].OBJ_TYPE_ID+"&BBQ_DATE="
					                			+event.data[bd].bbq_date+"&TASK_ID="+event.data[bd].task_id +"&TASK_NAME=" +event.data[bd].task_name);
										$("#"+event.data[bd].screenType+"_menu")[0].style.display="none";
	                				}
	                			}
							});
	                	}
	                }
	            }, 
				error: function () {
	            }
	        });	
		}else if(menuListnow.screentype == "ACC"){ 
			$.ajax({  
		          type : "get",  
		          url : '/df/csofacc/getAccmenu.do?tokenid='+ getTokenId() + '&ajax=' + 'noCache',  
		          data : {"user_id":JSON.parse(localStorage.commonData).svUserId,"oid":JSON.parse(localStorage.commonData).svOfficeId},  
		          async : false,  
		          success : function(result){
		        	  if(result.data.length && result.data.length > 0){
			        		var data = result.data;
			        		var BOOK_TYPE_ID = data[0].BOOK_TYPE_ID;
			                var oldbookTypeId = data[0].BOOK_TYPE_ID;
			                var bookTypeFlag = 0;
			                var SUM_FLAG = data[0].SUM_FLAG;
			                var sumFlag = 0;
			                var bookId = data[0].BOOK_ID;
			                var bookIdFlag = 0;
			                 $("#ACC_menu").append('<div class="tabbable tabs-left" style="height: 100%;"><ul id="acc_group_menu_ul" class="nav nav-tabs" style="height: 100%;background-color: rgba(242, 242, 242, 1);"></ul>'+
			                    '<div class="tab-content" id="acc_group_menu_div" style="overflow-y: scroll;height: 800px;"></div>');
			                 accButtonData = [];
			                 for(var ac=0;ac<data.length;ac++) {
			                	 if(data[ac].OID == JSON.parse(localStorage.commonData).svOfficeId){
			                		 if(data[ac].BOOK_TYPE_ID != BOOK_TYPE_ID){
			                             bookTypeFlag = 0;
			                             BOOK_TYPE_ID = data[ac].BOOK_TYPE_ID;
			                         }
			                         if(bookTypeFlag == 0 && oldbookTypeId == data[ac].BOOK_TYPE_ID){
			                             bookTypeFlag ++;
			                             $("#acc_group_menu_ul").append('<li class="active" id="'+data[ac].BOOK_TYPE_ID+'-li"><a href="#panel-'+data[ac].BOOK_TYPE_ID+'" data-toggle="tab">'+data[ac].BOOK_TYPE_NAME+'</a></li>');
			                             $("#acc_group_menu_div").append('<div class="tab-pane active" id="panel-'+data[ac].BOOK_TYPE_ID+'"></div>');
			                         }else if(bookTypeFlag == 0 && oldbookTypeId != data[ac].BOOK_TYPE_ID){
			                             bookTypeFlag ++;
			                             sumFlag = 0;
			                             bookIdFlag = 0;
			                             $("#acc_group_menu_ul").append('<li id="'+data[ac].BOOK_TYPE_ID+'-li"><a href="#panel-'+data[ac].BOOK_TYPE_ID+'" data-toggle="tab">'+data[ac].BOOK_TYPE_NAME+'</a></li>');
			                             $("#acc_group_menu_div").append('<div class="tab-pane" id="panel-'+data[ac].BOOK_TYPE_ID+'"></div>');
			                         }
			                         if(data[ac].SUM_FLAG != SUM_FLAG){
			                             sumFlag = 0;
			                             SUM_FLAG = data[ac].SUM_FLAG;
			                         }
			                         if(sumFlag == 0){
			                             sumFlag++;
			                             if(SUM_FLAG == 1){
			                                var SUM_NAME = "明细账";
			                             }else if(SUM_FLAG == 2){
			                                 var SUM_NAME = "处汇总账";
			                             }else if(SUM_FLAG == 3){
			                                 var SUM_NAME = "办汇总账";
			                             }else{
			                                 var SUM_NAME = "其他";
			                             }
			                             $("#panel-"+data[ac].BOOK_TYPE_ID).append('<div id="book-type-'+data[ac].BOOK_TYPE_ID+'-'+data[ac].SUM_FLAG+'"><div style="border-bottom: 1px solid blue;padding: 10px;">'+
			                                 '<span class=""></span><span style="margin-left: 5px">'+SUM_NAME+'</span></div>'+
			                                 '<div id="books-'+data[ac].BOOK_TYPE_ID+'-'+data[ac].SUM_FLAG+'" style="height: 120px;"></div>');
			                         }
				                     if(data[ac].BOOK_ID != bookId){
				                         bookIdFlag = 0;
				                         bookId = data[ac].BOOK_ID;
				                     }
				                     if(bookIdFlag == 0){
				                         bookIdFlag++;
				                         if(data[ac].ACC_NAME){
				                             var bookName = data[ac].BOOK_NAME+data[ac].ACC_NAME;
				                         }else{
				                             var bookName = data[ac].BOOK_NAME;
				                         }
				                         $("#books-"+data[ac].BOOK_TYPE_ID+"-"+data[ac].SUM_FLAG).append('<span class="pull-left" id="'+data[ac].BOOK_TYPE_ID+'-'+data[ac].BOOK_ID+'_span" style="margin: 5px;">'
				                                 +'<div class="menu-item-block">'+bookName+'</div>');						                       
				                     }
				                     $('#'+data[ac].BOOK_TYPE_ID+'-'+data[ac].BOOK_ID+'_span').append('<button class="btn csof-btn" id="'+data[ac].BOOK_TYPE_ID+'_'+data[ac].MENU_ID+'_'+data[ac].BOOK_ID+'_button" style="margin: 2px;width: 53px;text-align: center;padding: 0;">'+data[ac].SHORT_NAME+'</button>');
				                     var data1 = {
				                    		    "ACC_ID":data[ac].ACC_ID,
				                        		"GROUP_ID":data[ac].BOOK_TYPE_ID,
				                				"menuCode":data[ac].MENU_CODE,
				                				"menuId":data[ac].MENU_ID,
				                				"menuName":data[ac].MENU_NAME,
				                				"BOOK_ID":data[ac].BOOK_ID,
				                				"BOOK_NAME":data[ac].BOOK_NAME,
				                				"BOOK_GROUP":data[ac].BOOK_TYPE_NAME,
				                				"OID":data[ac].OID,
				                				"DEP_ID":data[ac].DEP_ID,
				                				"DEP_CODE":data[ac].DEP_CODE,
				                				    "DEP_NAME":data[ac].DEP_NAME,
				                				"SET_YEAR":data[ac].SET_YEAR,
				                				"SET_MONTH":data[ac].SET_MONTH,
				                				"url":data[ac].URL,
				                				"screenType":menuListnow.screentype,
				                		};
					                    accButtonData.push(data1);
				                }
			                     
			                 }	
		        	  }
		        	}  
		     });
			for(var accbtn =0;accbtn<accButtonData.length;accbtn++){
				$("#"+accButtonData[accbtn].GROUP_ID+'_'+accButtonData[accbtn].menuId+"_"+accButtonData[accbtn].BOOK_ID+"_button").click(accButtonData,function(event){		
	    			var group_id = event.currentTarget.id.split("_")[0];
					var menuId = event.currentTarget.id.split("_")[1];
	    			var BOOK_ID = event.currentTarget.id.split("_")[2];
	    			for(var accbtn2 =0;accbtn2<accButtonData.length;accbtn2++){
	    				if(event.data[accbtn2].GROUP_ID == group_id && event.data[accbtn2].menuId == menuId && event.data[accbtn2].BOOK_ID == BOOK_ID){
	    					addTabToParent(event.data[accbtn2].BOOK_NAME.split("台账")[0]+event.data[accbtn2].menuName,event.data[accbtn2].url+"tokenid=" 
		                			+ getTokenId() +"&menuid=" 
		                			+ event.data[accbtn2].menuId +"&menuname=" 
		                			+ event.data[accbtn2].menuName+"&ACC_ID=" 
		                			+ event.data[accbtn2].ACC_ID+"&BOOK_ID="
		                			+event.data[accbtn2].BOOK_ID+"&BOOK_NAME="
		                			+event.data[accbtn2].BOOK_NAME+"&BOOK_GROUP="
		                			+event.data[accbtn2].BOOK_GROUP+"&OID="
		                			+event.data[accbtn2].OID+"&DEP_ID="
		                			+event.data[accbtn2].DEP_ID+"&DEP_CODE="
		                			+event.data[accbtn2].DEP_CODE+"&DEP_NAME="
		                			+event.data[accbtn2].DEP_NAME+"&SET_YEAR="+event.data[accbtn2].SET_YEAR+"&SET_MONTH="+event.data[accbtn2].SET_MONTH);
							$("#"+event.data[accbtn2].screenType+"_menu")[0].style.display="none";
	    				}
	    			}
				});
			}
//		}else if(menuListnow.screentype == "TASK"){
		}else{
			for(var m = 0;m<menuList.length;m++){
				if(menuListnow.guid == menuList[m].parentid){
					$("#"+menuListnow.screentype+"_menu").append('<div id="'+menuList[m].guid+'_button" style="cursor:pointer;display: inline-block;margin: 5px;border: 1px solid #ccc;width: 120px;height: 60px;padding-top: 17px;margin:auto;text-align:center;margin-bottom: 2px;border-radius: 4px;background-color: #679CE7;color: #fff;">'+menuList[m].name+'</div><div style="display:inline-block;width:5px;"></div>');
					$("#"+menuList[m].guid+"_button").click(function(event){	
						var id = this.id.split("_")[0];
						for(var cl = 0;cl<menuList.length;cl++){
							if(menuList[cl].guid == id){
				    			addTabToParent(menuList[cl].name,menuList[cl].url+"tokenid=" 
			                			+ getTokenId() +"&menuid=" 
			                			+ menuList[cl].guid +"&menuname=" 
			                			+ menuList[cl].name);
				    		}else{
				    			if($("#"+menuList[cl].screentype+"_menu")[0]){
				    				$("#"+menuList[cl].screentype+"_menu")[0].style.display="none";
				    			}
				    		}
						}
					});
				}
			}
		}
//		else if(menuListnow.screentype == "BI"){
//			for(var m = 0;m<menuList.length;m++){
//				if(menuListnow.guid == menuList[m].parentid){
//					$("#BI_menu").append('<div id="'+menuList[m].guid+'_button" style="display: inline-block;cursor:pointer;margin: 5px;border: 1px solid #ccc;width: 120px;height: 60px;padding-top: 17px;margin:auto;text-align:center;margin-bottom: 2px;border-radius: 4px;background-color: #679CE7;color: #fff;">'+menuList[m].name+'</div><div style="display:inline-block;width:5px;"></div>');
//					$("#"+menuList[m].guid+"_button").click(function(event){	
//						var id = this.id.split("_")[0];
//						for(var cl = 0;cl<menuList.length;cl++){
//							if(menuList[cl].guid == id){
//				    			addTabToParent(menuList[cl].name,menuList[cl].url+"tokenid=" 
//			                			+ getTokenId() +"&menuid=" 
//			                			+ menuList[cl].guid +"&menuname=" 
//			                			+ menuList[cl].name);
//				    		}else{
//				    			if($("#"+menuList[cl].screentype+"_menu")[0]){
//				    				$("#"+menuList[cl].screentype+"_menu")[0].style.display="none";
//				    			}
//				    		}
//						}
//					});
//				}
//			}
//		}
	
	},
	level3 : function(menuList){
		for(var i=0;i<menuList.length;i++){
			if(menuList[i].levelno == 2){
				$("#left-menu-content").append("<li id='"+ menuList[i].guid +"' style='height:68px;padding-top:7px;'><div class='menu-list' style='width:50px;margin:0 18px;height:50px;background:  url(" +menuList[i].icon+ ") no-repeat;background-size: 30px 29px;'></div><div style='margin:-15px 5%;font-size:13px;text-align:center;'>"+ menuList[i].name +"</div></li>");
				if(menuList[i].isleaf == 0){
						$("#"+menuList[i].guid).click(function(){
							for(var cl = 0;cl<menuList.length;cl++){
					    		if(menuList[cl].guid == this.id){
					    			if($("#"+menuList[cl].screentype+"_menu")[0].style.display=="block"){
					    				$("#"+menuList[cl].screentype+"_menu")[0].style.display="none";
									}else {
										  $("#"+menuList[cl].screentype+"_menu").css("height",$(document).height());     
							    	        $("#"+menuList[cl].screentype+"_menu").css("width",$(document).width());   
							    	        $("#"+menuList[cl].screentype+"_menu")[0].style.display="block";
							    	        $("#"+menuList[cl].screentype+"_menu").html("");
							    	        pt_menu.level_menu3(menuList[cl],menuList);
									}	
					    		}else{
					    			if($("#"+menuList[cl].screentype+"_menu")[0]){
					    				$("#"+menuList[cl].screentype+"_menu")[0].style.display="none";
					    			}
					    		}
					    	}
						});
						$("#iframe_box").append("<div id='"+menuList[i].screentype+"_menu' class='mask' style='display: none;margin-left = -10px;padding: 10px;'></div>");
					if(menuList[i].screentype == "SUP"){
						var data = {
							"user_id" : JSON.parse(localStorage.commonData).svUserId,
							"oid" : JSON.parse(localStorage.commonData).svOfficeId,
							"menu_id" : menuList[i].guid,
						}
						$.ajax({
				            type: 'GET',
				            url: "/df/user/getMenu.do" + "?tokenid=" + getTokenId() + "&ajax=noCache",
				            data: data,
				            dataType: 'JSON',
				            async: false,
				            success: function (result) {
				            	var data = result.data;
				                if(result.data != null){
				                	var taskNum = "";
				                	var taskNo = 1;
				                	var flag = 0;
				                	supButtonData = new Array();
				                	for(var j=0;j<data.length;j++) {
				                		if(data[j].TASK_ID == taskNum) {
				                			$("#"+data[j].TASK_ID+"_span").append("<button id='"+data[j].MENU_ID+"_"+taskNum+"_button' class='btn csof-btn' style='margin: 2px;width: 53px;text-align: center;padding: 0;'>"+data[j].SHORT_NAME+"</button>");
				                			flag++;
				                			taskNo++;
				                		}else{
				                			taskNum = data[j].TASK_ID;
				                			$("#"+menuList[i].screentype+"_menu").append("<span class='pull-left' id='"+data[j].TASK_ID+
				                					"_span' style='margin: 5px;'><div class='menu-item-block'>"+data[j].TASK_NAME+"</div><button class='btn csof-btn' id='"+data[j].MENU_ID+"_"+taskNum+"_button' style='margin: 2px;width: 53px;text-align: center;padding: 0;'>"+data[j].SHORT_NAME+"</button></span>");
				                			flag = 1;
				                		}
				                		if(flag == 3){
			                				$("#"+data[j].TASK_ID+"_span").append("<br>");
			                				flag =0;
			                			}
				                		var menuId = data[j].MENU_ID;
				                		var menuName = data[j].MENU_NAME;
				                		var sid = data[j].SID;
				                		var mof_bill_id = data [j].MOF_BILL_ID;
				                		var screenType = menuList[i].screentype;
				                		var menuCode = data[j].MENU_CODE;
				                		var bbq_date = data[j].SUP_DATE;
				                		var sup_name = data[j].SUP_NAME;
				                		var sup_no = data[j].SUP_NO;
				                		var task_id = data[j].TASK_ID;
				                		var task_name = data[j].TASK_NAME;
				                		var url = data[j].URL;
				                		var data1 = {
				                				"menuCode":menuCode,
				                				"menuId":menuId,
				                				"menuName":menuName,
				                				"sid":sid,
				                				"mof_bill_id":mof_bill_id,
				                				"screenType":screenType,
				                				"bbq_date" :bbq_date,
				                				"task_id" :task_id,
				                				"task_name" :task_name,
				                				"url":url,
				                				"SHORT_NAME":data[j].SHORT_NAME,
				                				"IS_ALLOBJ" :data[j].IS_ALLOBJ,
				                				"OBJ_TYPE_ID" :data[j].OBJ_TYPE_ID,
				                		};
				                		supButtonData.push(data1);
				                		$("#"+data[j].MENU_ID+"_"+data[j].TASK_ID+"_button").click(supButtonData,function(event){		
				                			var menuNo = event.currentTarget.id.split("_")[0];
				                			var taskId = event.currentTarget.id.split("_")[1];
				                			for(var bd = 0;bd<event.data.length;bd++){
				                				if(event.data[bd].menuId == menuNo && event.data[bd].task_id == taskId){
				                					addTabToParent(event.data[bd].task_name+event.data[bd].SHORT_NAME,event.data[bd].url+
				                							"tokenid="+ getTokenId() +
				                							"&menuid="+ event.data[bd].menuId +
				                							"&menuname="+ event.data[bd].menuName+
				                							"&pageId="+event.data[bd].menuId+
				                							"&SID="+event.data[bd].sid+
				                							"&IS_ALLOBJ="+event.data[bd].IS_ALLOBJ+
				                							"&OBJ_TYPE_ID="+event.data[bd].OBJ_TYPE_ID+
				                							"&BBQ_DATE="+event.data[bd].bbq_date+
				                							"&TASK_ID="+event.data[bd].task_id +
				                							"&TASK_NAME=" +event.data[bd].task_name);
													$("#"+event.data[bd].screenType+"_menu")[0].style.display="none";
				                				}
				                			}
										});
				                	}
				                }
				            }, 
							error: function () {
				            }
				        });	
					}else if(menuList[i].screentype == "ACC"){ 
						$.ajax({  
					          type : "get",  
					          url : '/df/csofacc/getAccmenu.do?tokenid='+ getTokenId() + '&ajax=' + 'noCache',  
					          data : {"user_id":JSON.parse(localStorage.commonData).svUserId,"oid":JSON.parse(localStorage.commonData).svOfficeId},  
					          async : false,  
					          success : function(result){
					        		if(result.data.length && result.data.length > 0){
					        			var data = result.data;
						        		var BOOK_TYPE_ID = data[0].BOOK_TYPE_ID;
						                var oldbookTypeId = data[0].BOOK_TYPE_ID;
						                var bookTypeFlag = 0;
						                var SUM_FLAG = data[0].SUM_FLAG;
						                var sumFlag = 0;
						                var bookId = data[0].BOOK_ID;
						                var bookIdFlag = 0;
						                 $("#ACC_menu").append('<div class="tabbable tabs-left" style="height: 100%;"><ul id="acc_group_menu_ul" class="nav nav-tabs" style="height: 100%;background-color: rgba(242, 242, 242, 1);"></ul>'+
						                    '<div class="tab-content" id="acc_group_menu_div" style="overflow-y: scroll;height: 800px;"></div>');
						                 accButtonData = [];
						                 for(var ac=0;ac<data.length;ac++) {
						                	 if(data[ac].OID == JSON.parse(localStorage.commonData).svOfficeId){
						                		 if(data[ac].BOOK_TYPE_ID != BOOK_TYPE_ID){
						                             bookTypeFlag = 0;
						                             BOOK_TYPE_ID = data[ac].BOOK_TYPE_ID;
						                         }
						                         if(bookTypeFlag == 0 && oldbookTypeId == data[ac].BOOK_TYPE_ID){
						                             bookTypeFlag ++;
						                             $("#acc_group_menu_ul").append('<li class="active" id="'+data[ac].BOOK_TYPE_ID+'-li"><a href="#panel-'+data[ac].BOOK_TYPE_ID+'" data-toggle="tab">'+data[ac].BOOK_TYPE_NAME+'</a></li>');
						                             $("#acc_group_menu_div").append('<div class="tab-pane active" id="panel-'+data[ac].BOOK_TYPE_ID+'"></div>');
						                         }else if(bookTypeFlag == 0 && oldbookTypeId != data[ac].BOOK_TYPE_ID){
						                             bookTypeFlag ++;
						                             sumFlag = 0;
						                             bookIdFlag = 0;
						                             $("#acc_group_menu_ul").append('<li id="'+data[ac].BOOK_TYPE_ID+'-li"><a href="#panel-'+data[ac].BOOK_TYPE_ID+'" data-toggle="tab">'+data[ac].BOOK_TYPE_NAME+'</a></li>');
						                             $("#acc_group_menu_div").append('<div class="tab-pane" id="panel-'+data[ac].BOOK_TYPE_ID+'"></div>');
						                         }
						                         if(data[ac].SUM_FLAG != SUM_FLAG){
						                             sumFlag = 0;
						                             SUM_FLAG = data[ac].SUM_FLAG;
						                         }
						                         if(sumFlag == 0){
						                             sumFlag++;
						                             if(SUM_FLAG == 1){
						                                var SUM_NAME = "明细账";
						                             }else if(SUM_FLAG == 2){
						                                 var SUM_NAME = "处汇总账";
						                             }else if(SUM_FLAG == 3){
						                                 var SUM_NAME = "办汇总账";
						                             }else{
						                                 var SUM_NAME = "其他";
						                             }
						                             $("#panel-"+data[ac].BOOK_TYPE_ID).append('<div id="book-type-'+data[ac].BOOK_TYPE_ID+'-'+data[ac].SUM_FLAG+'"><div style="border-bottom: 1px solid blue;padding: 10px;">'+
						                                 '<span class=""></span><span style="margin-left: 5px">'+SUM_NAME+'</span></div>'+
						                                 '<div id="books-'+data[ac].BOOK_TYPE_ID+'-'+data[ac].SUM_FLAG+'" style="height: 120px;"></div>');
						                         }
							                     if(data[ac].BOOK_ID != bookId){
							                         bookIdFlag = 0;
							                         bookId = data[ac].BOOK_ID;
							                     }
							                     if(bookIdFlag == 0){
							                         bookIdFlag++;
							                         if(data[ac].ACC_NAME){
							                             var bookName = data[ac].BOOK_NAME+data[ac].ACC_NAME;
							                         }else{
							                             var bookName = data[ac].BOOK_NAME;
							                         }
							                         $("#books-"+data[ac].BOOK_TYPE_ID+"-"+data[ac].SUM_FLAG).append('<span class="pull-left" id="'+data[ac].BOOK_TYPE_ID+'-'+data[ac].BOOK_ID+'_span" style="margin: 5px;">'
							                                 +'<div class="menu-item-block">'+bookName+'</div>');						                       
							                     }
							                     $('#'+data[ac].BOOK_TYPE_ID+'-'+data[ac].BOOK_ID+'_span').append('<button class="btn csof-btn" id="'+data[ac].BOOK_TYPE_ID+'_'+data[ac].MENU_ID+'_'+data[ac].BOOK_ID+'_button" style="margin: 2px;width: 53px;text-align: center;padding: 0;">'+data[ac].SHORT_NAME+'</button>');
							                     var data1 = {
							                    		    "ACC_ID":data[ac].ACC_ID,
							                        		"GROUP_ID":data[ac].BOOK_TYPE_ID,
							                				"menuCode":data[ac].MENU_CODE,
							                				"menuId":data[ac].MENU_ID,
							                				"menuName":data[ac].MENU_NAME,
							                				"BOOK_ID":data[ac].BOOK_ID,
							                				"BOOK_NAME":data[ac].BOOK_NAME,
							                				"BOOK_GROUP":data[ac].BOOK_TYPE_NAME,
							                				"OID":data[ac].OID,
							                				"DEP_ID":data[ac].DEP_ID,
							                				"DEP_CODE":data[ac].DEP_CODE,
		 				                				    "DEP_NAME":data[ac].DEP_NAME,
							                				"SET_YEAR":data[ac].SET_YEAR,
							                				"SET_MONTH":data[ac].SET_MONTH,
							                				"url":data[ac].URL,
							                				"screenType":menuList[i].screentype,
							                		};
								                    accButtonData.push(data1);
							                }
						                     
						                 }	
					        		}
					        	}  
					     });
						for(var accbtn =0;accbtn<accButtonData.length;accbtn++){
							$("#"+accButtonData[accbtn].GROUP_ID+'_'+accButtonData[accbtn].menuId+"_"+accButtonData[accbtn].BOOK_ID+"_button").click(accButtonData,function(event){		
	                			var group_id = event.currentTarget.id.split("_")[0];
								var menuId = event.currentTarget.id.split("_")[1];
	                			var BOOK_ID = event.currentTarget.id.split("_")[2];
	                			for(var accbtn2 =0;accbtn2<accButtonData.length;accbtn2++){
	                				if(event.data[accbtn2].GROUP_ID == group_id && event.data[accbtn2].menuId == menuId && event.data[accbtn2].BOOK_ID == BOOK_ID){
	                					addTabToParent(event.data[accbtn2].BOOK_NAME.split("台账")[0]+event.data[accbtn2].menuName,event.data[accbtn2].url+"tokenid=" 
					                			+ getTokenId() +"&menuid=" 
					                			+ event.data[accbtn2].menuId +"&menuname=" 
					                			+ event.data[accbtn2].menuName+"&ACC_ID=" 
					                			+ event.data[accbtn2].ACC_ID+"&BOOK_ID="
					                			+event.data[accbtn2].BOOK_ID+"&BOOK_NAME="
					                			+event.data[accbtn2].BOOK_NAME+"&BOOK_GROUP="
					                			+event.data[accbtn2].BOOK_GROUP+"&OID="
					                			+event.data[accbtn2].OID+"&DEP_ID="
					                			+event.data[accbtn2].DEP_ID+"&DEP_CODE="
					                			+event.data[accbtn2].DEP_CODE+"&DEP_NAME="
					                			+event.data[accbtn2].DEP_NAME+"&SET_YEAR="+event.data[accbtn2].SET_YEAR+"&SET_MONTH="+event.data[accbtn2].SET_MONTH);
										$("#"+event.data[accbtn2].screenType+"_menu")[0].style.display="none";
	                				}
	                			}
							});
						}
//					}else if(menuList[i].screentype == "TASK"){
					}else{
						for(var m = 0;m<menuList.length;m++){
							if(menuList[i].guid == menuList[m].parentid){
								$("#"+menuList[i].screentype+"_menu").append('<div id="'+menuList[m].guid+'_button" style="cursor:pointer;display: inline-block;margin: 5px;border: 1px solid #ccc;width: 120px;height: 60px;padding-top: 17px;margin:auto;text-align:center;margin-bottom: 2px;border-radius: 4px;background-color: #679CE7;color: #fff;">'+menuList[m].name+'</div><div style="display:inline-block;width:5px;"></div>');
								$("#"+menuList[m].guid+"_button").click(function(event){	
									var id = this.id.split("_")[0];
									for(var cl = 0;cl<menuList.length;cl++){
										if(menuList[cl].guid == id){
							    			addTabToParent(menuList[cl].name,menuList[cl].url+"tokenid=" 
						                			+ getTokenId() +"&menuid=" 
						                			+ menuList[cl].guid +"&menuname=" 
						                			+ menuList[cl].name);
							    		}else{
							    			if($("#"+menuList[cl].screentype+"_menu")[0]){
							    				$("#"+menuList[cl].screentype+"_menu")[0].style.display="none";
							    			}
							    		}
									}
								});
							}
						}
					}
//					else if(menuList[i].screentype == "BI"){
//						for(var m = 0;m<menuList.length;m++){
//							if(menuList[i].guid == menuList[m].parentid){
//								$("#BI_menu").append('<div id="'+menuList[m].guid+'_button" style="display: inline-block;cursor:pointer;margin: 5px;border: 1px solid #ccc;width: 120px;height: 60px;padding-top: 17px;margin:auto;text-align:center;margin-bottom: 2px;border-radius: 4px;background-color: #679CE7;color: #fff;">'+menuList[m].name+'</div><div style="display:inline-block;width:5px;"></div>');
//								$("#"+menuList[m].guid+"_button").click(function(event){	
//									var id = this.id.split("_")[0];
//									for(var cl = 0;cl<menuList.length;cl++){
//										if(menuList[cl].guid == id){
//							    			addTabToParent(menuList[cl].name,menuList[cl].url+"tokenid=" 
//						                			+ getTokenId() +"&menuid=" 
//						                			+ menuList[cl].guid +"&menuname=" 
//						                			+ menuList[cl].name);
//							    		}else{
//							    			if($("#"+menuList[cl].screentype+"_menu")[0]){
//							    				$("#"+menuList[cl].screentype+"_menu")[0].style.display="none";
//							    			}
//							    		}
//									}
//								});
//							}
//						}
//					}
				} else{
					if(menuList[i].screentype == "MYHOME"){
						addTabToParent(menuList[i].name,menuList[i].url+"tokenid=" 
	                			+ getTokenId() +"&menuid=" 
	                			+ menuList[i].guid +"&menuname=" 
	                			+ menuList[i].name);
					}
					$("#"+menuList[i].guid).click(function(){
						for(var cl = 0;cl<menuList.length;cl++){
							if(menuList[cl].guid == this.id){
				    			addTabToParent(menuList[cl].name,menuList[cl].url+"tokenid=" 
			                			+ getTokenId() +"&menuid=" 
			                			+ menuList[cl].guid +"&menuname=" 
			                			+ menuList[cl].name);
				    		}else{
				    			if($("#"+menuList[cl].screentype+"_menu")[0]){
				    				$("#"+menuList[cl].screentype+"_menu")[0].style.display="none";
				    			}
				    		}
				    	}
						
						
					});
				}
			}
		}	
		
		menuThreeLevel = {
			menuFirstLevelList : new Array(),
			menuSecondLevelList : new Array(),
			menuThirdLevelList : new Array(),
			menuFourthLevelList : new Array(),
			menuFivthLevelList : new Array()
		};
		var n1 = 0;
		var n2 = 0;
		var n3 = 0;
		var n4 = 0;
		var n5 = 0;
		var baseNo = 0;
		for (var i = 0; i < menuList.length; i++){
			if(menuList[i].levelno==baseNo){
				menuThreeLevel.menuFirstLevelList[n1] = menuList[i];
				n1++;
			}else if(menuList[i].levelno==baseNo+1){
				menuThreeLevel.menuSecondLevelList[n2] = menuList[i];
				n2++;
			}else if(menuList[i].levelno==baseNo+2){
				menuThreeLevel.menuThirdLevelList[n3] = menuList[i];
				n3++;
			}else if(menuList[i].levelno==baseNo+3){
				menuThreeLevel.menuFourthLevelList[n4] = menuList[i];
				n4++;
			}else if(menuList[i].levelno==baseNo+4){
				menuThreeLevel.menuFivthLevelList[n5] = menuList[i];
				n5++;
			}
		}
		if(menuThreeLevel.menuFirstLevelList!=null && menuThreeLevel.menuFirstLevelList.length>0){
			// 0/1/2
		}else if(menuThreeLevel.menuSecondLevelList!=null && menuThreeLevel.menuSecondLevelList.length>0){
			menuThreeLevel.menuFirstLevelList = menuThreeLevel.menuSecondLevelList;
			menuThreeLevel.menuSecondLevelList = menuThreeLevel.menuThirdLevelList;
			menuThreeLevel.menuThirdLevelList = menuThreeLevel.menuFourthLevelList;
		}else if(menuThreeLevel.menuThirdLevelList!=null && menuThreeLevel.menuThirdLevelList.length>0){
			menuThreeLevel.menuFirstLevelList = menuThreeLevel.menuThirdLevelList;
			menuThreeLevel.menuSecondLevelList = menuThreeLevel.menuFourthLevelList;
			menuThreeLevel.menuThirdLevelList = menuThreeLevel.menuFivthLevelList;
		}
		return menuThreeLevel;
	},
	html : function(){
		var menuFirstLevelList = menuThreeLevel.menuFirstLevelList;
    	var menuSecondLevelList = menuThreeLevel.menuSecondLevelList;
    	var menuThirdLevelList = menuThreeLevel.menuThirdLevelList;
    	
    	var menuListHtml = '<li class="treeview">';
    	menuListHtml += '<a href="javascript:void(0);">';
    	menuListHtml += '<i class="fa fa-desktop"></i>';
    	menuListHtml += '<span>我的工作台</span>';
    	menuListHtml += '</a></li>';
    	var _tokenid = "";
    	var menuFirstLevelListLength = dfp.Object.isNull(menuFirstLevelList)?0:menuFirstLevelList.length;
    	// 一级菜单
    	for (var i=0; i<menuFirstLevelListLength; i++){
    		menuListHtml += '<li class="treeview">';
    		menuListHtml += '<a href="javascript:void(0);">';
    		menuListHtml += '<i class="fa fa-leaf"></i>';
    		menuListHtml += '<span>' + menuFirstLevelList[i].name + '</span><span class="pull-right-container"><i class="fa fa-angle-right pull-right"></i></span></a>';
    		menuListHtml += '<div class="two-level treeview-menu">';
    		
    		// 二级菜单
    		for (var j in menuSecondLevelList){
    			if(menuSecondLevelList[j].parentid == null || menuSecondLevelList[j].parentid == ""
    					|| menuSecondLevelList[j].parentid != menuFirstLevelList[i].guid){
	    			continue;
	    		}
    			
    			menuListHtml += '<dl class="two-level-item">';
    			menuListHtml += '<dt class="two-level-tit">';
    			menuListHtml += menuSecondLevelList[j].name + '&nbsp;&nbsp;';
    			menuListHtml += '</dt>';
    			menuListHtml += '<dd class="two-level-detail">';
    			
    			// 三级菜单
    			for (var k in menuThirdLevelList){
    				if(menuThirdLevelList[k].parentid == null || menuThirdLevelList[k].parentid == ""
    					|| menuThirdLevelList[k].parentid != menuSecondLevelList[j].guid){
    	    			continue;
    	    		}
    				var _url = menuThirdLevelList[k].url;
    				if(_url == null || _url == "" || _url == undefined){
    					_url = "";
    				}else{
    					_url = (menuThirdLevelList[k].url).replace(/\s/g, "");
    				}
    				_url += (_url.indexOf("?")!=-1 ? "&tokenid=" : "?tokenid=")+getTokenId();
    				_url += '&menuid='+menuThirdLevelList[k].guid+'&menuname='+encodeURI(menuThirdLevelList[k].name);
    				menuListHtml += '<a class="_portal_recent_menu_add_a" href="javascript:void(0);" data-title="'+menuThirdLevelList[k].name+'" data-href="'+_url+'" >'+menuThirdLevelList[k].name+'</a>';
    			}
    			menuListHtml += '</dd></dl>';
    		}
    		menuListHtml += '</div>';
    	}
    	menuListHtml += '</li>';
    	$("#_sidebar_menu").append(menuListHtml);
	},
	event : function(){
		//页签
		$(".two-level-detail a").click(function(){
			var href = $(this).attr('data-href'),
			title = $(this).attr("data-title");
			if(title==""||href==""){
				alert("data-title属性不能为空或data-href属性不能为空");
				return false;
			}
			Hui_admin_tab($(this));
			$("#min_title_list li i").addClass('fa fa-times');
			$(this).parents('.two-level').hide();
		});
		$("li.treeview >a").on("click", function(e){
			e.preventDefault();
		});
	    $("li.treeview >a").on("mouseover", function(e) {
	    	if($(this).parent("li").index() == 0){
	    		$(".two-level").each(function() {
	    			$(this).hide();
	    		});
	    		return;
	    	}
			var s = e.fromElement || e.relatedTarget;
			if (document.all) {
				if (this.contains(s))  // 判断调用onmouseover的对象(this)是否包含自身或子级，如果包含，则不执行
					return;
			} else {
				var reg = this.compareDocumentPosition(s);
				if ((reg == 20 || reg == 0))
					return;
			}
			$(".two-level").each(function() {
				$(this).hide();
			});
			var i = $(this).parent("li").index();
			if(!$(".two-level").eq(i-1).hasClass("null")) {
				$(".two-level").eq(i-1).slideDown();
			}
		});
		$("li.treeview >a").on("mouseout", function(e) {
			e = window.event || e;
			var s = e.toElement || e.relatedTarget;
			if(document.all) {
				if(this.contains(s)) {
					return;
				}
			} else {
				var reg = this.compareDocumentPosition(s);
				if((reg == 20 || reg == 0)) {
					return;
				}
			}
		});
		$(".two-level").mouseleave(function() {
			$(".two-level").each(function() {
				$(this).slideUp();
			})
		});		
	},
	show : function(){
		var caroleguid = localStorage.select_role_guid==undefined?"":localStorage.select_role_guid;
		$.ajax({
			url : "/df/portal/getMenuByRole.do",
			type : "GET",
			data : {"tokenid":getTokenId(), "caroleguid":Base64.encode(caroleguid)},
			dataType : "json",
			success : function(data){
				pt_menu.level3(data.mapMenu);
				pt_menu.html();
				pt_menu.event();
			}
		});
	}
//	getMenuById : function(menu_id) {
//		var data = {
//				"user_id" : common_data.svUserId,
//				"oid" : common_data.svOfficeId,
//				"menu_id" : menu_id,
//		}
//		$.ajax({
//            type: 'GET',
//            url: "/df/user/getMenu.do" + "?tokenid=" + getTokenId() + "&ajax=noCache",
//            data: data,
//            dataType: 'JSON',
//            async: false,
//            success: function (result) {
//            	var data = result.data;
//            	return data;
//            }, 
//			error: function () {
//            }
//        });			
//	}
}

/**
 * user management
 */
var pt_user = {
	logout : function(){
		var tokenId = getTokenId();
		// 清空当次访问产生的页面临时数据
		var lsName = [
			"commonData",
			"select_role_guid",
			"commonBtnValue",
			"common_operation_select_url",
			"commonOperationSelectMenuHtml",
			"normalMenuByUserRoleSimple",
			dfp.key.tokenid
		];
		for(var i in lsName){
			if(!lsName.hasOwnProperty(i)){
				continue;
			}
			localStorage.removeItem(lsName[i]);
		}
		$.ajax({
			url : "/df/login/loginout.do",
			type : "GET",
			data : {"tokenid":tokenId},
			dataType : "json",
			success : function(data){
				var flag = data.flag;
				if(flag == 1){
					window.location.href = "/df/portal/login/csoflogin.html";
				}else{
					alert("wrong");
				}
			}
		});
	}
};

function Portal() {
	this.index = pt_index;
	this.co = pt_common;
	this.pp = pt_pubparams;
	this.ag = pt_agency;
	this.role = pt_role;
	this.menu = pt_menu;
	this.user = pt_user;
}
var portal = new Portal();

var centerDate;
$(function(){
	// 服务器时间
	centerDate = $.ajax({async: false}).getResponseHeader("Date");
	portal.index.init();
});

function logout() {
	layer.confirm('是否确认退出？', {
		title : '退出提示',
		btn : [ '退出', '取消' ] //按钮
	}, function() {
		portal.user.logout();
	}, function() {
		layer.closeAll();
	});
}

/**
 * 切换角色
 */
function switchRoleConfirm(roleid){
	portal.role._switch(roleid);
}

/**
 * 刷新工作台待办条目数量
 * @params menuid 待办menuid
 */
function refreshDealingDashboard(menuid){
	//iframeDashboard.window.refreshDealingDashboardPart(menuid);
}

/**
 * 页面顶部时间显示
 */
function portalTopTime() {
	_timestamp = Date.parse(centerDate);
    _timestamp = _timestamp.toString().match(/^\d$/)?_timestamp:new Date().getTime();
	curDate = new Date(_timestamp);
	_hour = curDate.getHours();
	_minutes = curDate.getMinutes();
	_seconds = curDate.getSeconds();
	if (_hour < 10) {
		_hour = "0" + _hour;
	}
	if (_minutes < 10) {
		_minutes = "0" + _minutes;
	}
	if (_seconds < 10) {
		_seconds = "0" + _seconds;
	}
	$("#_currenttime_index_top").html(curDate.getFullYear()+'年'+(curDate.getMonth() + 1)+'月'+curDate.getDate()+'日'+'&nbsp;&nbsp;'+ curDate.getHours() + ":" + _minutes + ":" + _seconds);
	centerDate += 1000;
}

