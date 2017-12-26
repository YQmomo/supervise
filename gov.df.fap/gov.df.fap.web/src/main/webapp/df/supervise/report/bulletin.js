/**
 * Created by yanqiong on 2017/8/8.
 */
require(
		[ 'jquery', 'knockout', '/df/supervise/ncrd.js', 'bootstrap', 'dateZH',
				'uui', 'tree', 'grid', 'ip' ],
		function($, ko, ncrd, _) {
			window.ko = ko;
			var tokenid,options;
            var isPublic,groupId,canManage,userType,DEP_CODE,USER_CODE,menuId,menuName;

			
			//URL
			var getloginbulletinURL = '/ReportForm/selectEReportByUserType.do';//登录报表url			
			var getbulletinURL = '/ReportForm/selectEReportByIsPublic.do';;//公告url
			
			var viewModel = {};
	        
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
	            menuId = url.split("menuid=")[1].split("&")[0];
	            menuName = decodeURI(url.split("menuname=")[1].split("&")[0]);
	            DEP_CODE = options.svDepCode;
	            USER_CODE = options.svUserCode;
	            isPublic = url.split("isPublic=")[1].split("&")[0];
	            groupId = url.split("groupId=")[1].split("&")[0];
	            canManage = url.split("canManage=")[1].split("&")[0];
	            userType = url.split("userType=")[1].split("&")[0];
	            //登录报表url
	            //$("#loginbulletin").attr("src", getloginbulletinURL + "?tokenid=" +  ip.getTokenId() + "&ajax=noCache" + "&userType="+userType+"&dep_code="+DEP_CODE+"&user_code="+USER_CODE);
//	            $.ajax({
//			        type: 'post',
//			        url: getloginbulletinURL + "?tokenid=" + tokenid + "&ajax=noCache",
//			        data: {"userType" : userType,"dep_code":DEP_CODE,"user_code":USER_CODE},
//			        dataType: 'JSON',
//			        async: false,
//			        success: function (result) {
//			            if (result.errorCode == 0) {
//			            	$("#loginbulletin")[0].src = result.data;
//			            } else {
//			                ip.ipInfoJump(result.message, 'error');
//			            }
//			        }, error: function () {
//			            ip.ipInfoJump("错误", 'error');
//			        }
//			    });
	            $("#bulletin").attr("src", getbulletinURL + "?tokenid=" +  ip.getTokenId() + "&ajax=noCache" + "&isPublic="+isPublic+"&groupId="+groupId+"&canManage="+canManage+"&dep_code="+DEP_CODE+"&user_code="+USER_CODE);
//	            $.ajax({
//			        type: 'post',
//			        url: getbulletinURL + "?tokenid=" + tokenid + "&ajax=noCache",
//			        data: {"isPublic" : isPublic,"groupId" : groupId,"canManage" : canManage,"dep_code":DEP_CODE,"user_code":USER_CODE},
//			        dataType: 'JSON',
//			        async: false,
//			        success: function (result) {
//			            if (result.errorCode == 0) {
//			            	$("#bulletin")[0].src = result.data;
//			            } else {
//			                ip.ipInfoJump(result.message, 'error');
//			            }
//			        }, error: function () {
//			            ip.ipInfoJump("错误", 'error');
//			        }
//			    });
			}
			init();
		})