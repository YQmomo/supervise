/**
 * 附件管理JS处理
 * 2017-08-17 whd
 */
require(['jquery', 'knockout','bootstrap', 'uui','tree', 'jquery.file.upload','grid', 'ip'],
    function ($, ko, _) {
        window.ko = ko;
        var tokenid;
        var options;
        var entityId;
        var entityName;
        var oid;
        var dep_id;
        var dep_code;
        var modelFlag="1";
        var admin;
        tokenid = ip.getTokenId();
        options = ip.getCommonOptions({});
        options['tokenid'] = tokenid;
        
        var uploadType={".zip":"zip",
        		        ".gif":"gif",
        		        ".png":"png",
        		        ".jpg":"jpg",
        		        ".jpeg":"jpeg",
        		        ".htm":"htm",
        		        ".html":"html",
        		        ".doc":"doc",
        		        ".docx":"docx",
        		        ".xls":"xls",
        		        ".xlsx":"xlsx",
        		        ".ppt":"ppt",
        		        ".pptx":"pptx",
        		        ".pdf":"pdf"
        };
        
		var pViewType = {
        		VIEWTYPE_INPUT : "001",	// 录入视图
        		VIEWTYPE_LIST  : "002",	// 列表视图
        		VIEWTYPE_QUERY : "003"	// 查询视图
        };
		var deleteListURL ='/df/cs/delAttach.do';
		var viewModel = {
	            exDataTable:new u.DataTable({
	                meta: {

	                }
	            })
		 };
		
        //生成表头
     	viewModel.initData = function() {
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
    				viewModel.initGridData(); //调用初始化表格
    			}
    		});
     	};
        
        // 初始化表格
    	viewModel.initGridData = function() {
            var queryViewId;
            var tableViewDetail;
            var queryViewDetail;        
            for ( var n = 0; n < viewModel.viewList.length; n++) {
                var view = viewModel.viewList[n];
                if (view.viewtype == pViewType.VIEWTYPE_LIST) {// 列表视图
                    if (view.orders == '9') {
                    	viewModel.tableViewDetail = view;                          
                        payViewId = view.viewid;
                    }
                  }
                else if (view.viewtype == pViewType.VIEWTYPE_QUERY) {// 查询视图
                    if (view.orders == '1') {
                        queryViewId = view.viewid;
                        queryViewDetail = view;
                    }
                }
            }
            if(modelFlag=="0"){
            	options["condition"]="and entity_id='"+entityId+"' and entity_name='"+entityName+"'";
            }
            viewModel.gridViewModel = ip.initGrid(viewModel.tableViewDetail, 'testGrid', "/df/cs/getAttachList.do",options, 1, false,true,false,false);

    	};
    	
    	//刷新视图
        viewModel.refresh = function() {
        	var queryViewId;
            var tableViewDetail;
            var queryViewDetail;
            for ( var n = 0; n < viewModel.viewList.length; n++) {
                var view = viewModel.viewList[n];
                if (view.viewtype == pViewType.VIEWTYPE_LIST) {// 列表视图
                    if (view.orders == '9') {
                    	viewModel.tableViewDetail = view;
                        payViewId = view.viewid;
                    }
                } else if (view.viewtype == pViewType.VIEWTYPE_QUERY) {// 查询视图
                    if (view.orders == '1') {
                        queryViewId = view.viewid;
                        queryViewDetail = view;
                    }
                }
            }
        	ip.setGrid(viewModel.gridViewModel, "/df/cs/getAttachList.do", options);
        };
        
        /**
         * 附件删除按钮事件
         */
        viewModel.attachDelete = function() {
        	var selectRow = viewModel.gridViewModel.gridData.getSelectedRows();
        	if(selectRow.length > 0) {
        		var ids="";
        		for(var i=0;i<selectRow.length;i++){
        			var entity_id = selectRow[i].data.id.value; 
             	    ids+=entity_id+",";
        		}
        		ids=ids.substring(0,ids.length-1);
        		  
            	$.ajax({
                    type: 'get',
                    url: deleteListURL + "?tokenid=" + tokenid + "&ajax=noCache",
                    data: {"ids":ids},
                    dataType: 'JSON',
                    async: false,
                    success: function (result) {
                        viewModel.refresh();
                        ip.ipInfoJump("附件删除成功", 'success');
                    }, error: function () {
                        ip.ipInfoJump("错误", 'error');
                    }
                });
        	}else {
        		ip.ipInfoJump("请选择删除附件", 'info');
        	}
        };   
        
        viewModel.up =function(){
      	  	$("#fileupload").val("");
      	  	$("#remark").val("");
      	  	$('#inputModal').modal('show');

        }; 

        /**
         * 附件上传
         */
        viewModel.attachUpload = function() {
        	  var fileInfo =new FormData($("#myupload")[0]);
              var files=$('#fileupload')[0].files;
              
              for(var i=0;i<files.length;i++){
            	  var fileName=files[i].name;
            	  var str=fileName.substring(fileName.indexOf("."));
            	  if(typeof(uploadType[str.toLocaleLowerCase()])=="undefined"){
	        	    	return ip.ipInfoJump("禁止上传后缀为【"+str+"】的文件", 'info');
            	  }
            	  fileInfo.append( fileName,files[i] );
              }
              fileInfo.append("entityId",entityId);
              fileInfo.append("entityName",entityName);
              fileInfo.append("oid",oid);
              fileInfo.append("dep_id",dep_id);
              fileInfo.append("dep_code",dep_code);
              fileInfo.append("remark",$("#remark").val());
              if(files.length>0){
            	  $.ajax({
  	        		type: 'post',
  	        		url: "/df/cs/uploadAttach.do?tokenid=" + tokenid + "&ajax=noCache",
  	        	    data: fileInfo,
  	        	    processData:false,
  	                contentType:false,
  	        	    async: false,
  	        	    success: function(data){
  	        	    	ip.ipInfoJump("附件上传成功", 'success');
  	        	    	viewModel.refresh();
  	        	    },
  	        	    error: function(){
  	        	    	ip.ipInfoJump("附件上传失败", 'info');
  	        	    }
                });
              }
             
              $('#inputModal').modal('hide');

        }; 
        
        /**
         * 下载附件
         * @param attach_id
         */
        viewModel.downloadFile = function () {
        	var selectRow = viewModel.gridViewModel.gridData.getSelectedRows();
        	if(selectRow.length ==1) {        		
            	var form = $("<form id='downloadForm'>");
        	    form.attr('style', 'display:none');
        	    form.attr('target', '');
        	    form.attr('method', 'post');
        	    var url = "/df/cs/downloadAttach.do?tokenid=" + tokenid + "&ajax=noCache";
        	    form.attr('action', url);
        	    var input = $('<input>');
        	    input.attr('type', 'hidden');
        	    input.attr('name', 'attach_id');
        	    input.attr('value', selectRow[0].data.id.value);
        	    $('body').append(form);
        	    form.append(input);
        	    form.submit();
        	    form.remove();
        	    $('#downloadForm').remove();
        	}else if(selectRow.length > 1){
        		ip.ipInfoJump("请选择一个附件下载", 'info');
        	}else {
        		ip.ipInfoJump("请选择下载附件", 'info');
        	}
      	 
        };
        
        
        /**
         * 附件预览
         */
//        viewModel.showFile = function () {
//        	var selectRow = viewModel.gridViewModel.gridData.getSelectedRows();
//        	var url = selectRow[0].data.file_url.value;
//        	if(selectRow.length ==1) {
//        		$.ajax({
//	        		type: 'GET',
//	        		url: "/df/cs/openAttach.do?tokenid=" + tokenid + "&ajax=noCache",
//	        	    data: {"filePath":url},
//	        	    dataType: 'JSON',
//	        	    async: false,
//	        	    success: function(data){
//
//	        	    },
//	        	    error: function(){
//	        	    	ip.ipInfoJump("附件预览失败", 'info');
//	        	    }
//              });
//        	    
//        	}else if(selectRow.length > 1){
//        		ip.ipInfoJump("请选择一个附件预览", 'info');
//        	}else {
//        		ip.ipInfoJump("请选择预览附件", 'info');
//        	}
//      	 
// 
//        };

      //切换录入页面
        viewModel.sup_input = function() {
            $(".sup-first-trial").each(function () {
                $(this).addClass("sup_fade");
            })
            $(".sup-review").each(function () {
                $(this).addClass("sup_fade");
            })
            $(".sup-input").each(function () {
                $(this).removeClass("sup_fade");
            })
        }
        //初始化
        function init() {
            u.createApp({
                el: document.body,
                model: viewModel
            });
            var url = window.location.href;
            entityId = url.split("entityId=")[1].split("&")[0];
            entityName = url.split("entityName=")[1].split("&")[0];
            oid = url.split("oid=")[1].split("&")[0];
            dep_id = url.split("dep_id=")[1].split("&")[0];
            dep_code = url.split("dep_code=")[1].split("&")[0];
            modelFlag = url.split("modelFlag=")[1].split("&")[0];
            admin= url.split("admin=")[1].split("&")[0];
            
            
			//初始化列表数据
			//viewModel.supData("","","");//die

			////初始化grid
            viewModel.initData();
            
            if(admin=='Y'){
            	//$("#down").removeAttr("disabled","disabled");
            	$("#down").show();
            	//$("#show").show();
            	$("#delete").show();
            	$("#input").show();
            }else{
            	$("#down").show();
            	$("#delete").hide();
            	$("#input").hide();
            	//$("#show").show();


            }
        }
        init();
    }
);