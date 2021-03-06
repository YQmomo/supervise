/**
 * Created by wenben on 2017/10/18.
 */
require(['jquery','knockout','./js/webuploader.js','./js/jquery.media.js', 'bootstrap', 'uui','tree', 'grid', 'ip'], function($,ko,WebUploader,media) {
    window.ko = ko;

    var $list=$("#fileList"); //
    var $btn =$("#pickUploadFile");
    var thumbnailWidth = 1;   //缩略图高度和宽度 （单位是像素），当宽高度是0~1的时候，是按照百分比计算，具体可以看api文档
    var thumbnailHeight = 1;

    var fileMd5;  //文件唯一标识

    var maxUploadSize;//文件上传的最大文件大小


    /* 自定义参数 */
    var fileName, //文件名称
        oldProcess, // 如果该文件上传过，记录该文件的进度
        count = 0, // 当前正在上传文件在数组中的下标，一次上传多个文件时使用
        fileArr = [], //文件数组，当有文件被添加到队列时，就push到数组中
        map = {}; //key存储文件id，value存储该文件的上传进度

    var fileSrcMap = {};//所有文件对象  fileMap.fileId = file 格式存储file  src

    var /*options = ip.getCommonOptions({}),*/
        tokenid = ip.getTokenId();//通过ip.js获取token值

    /* 请求链接 */
    var requireUrl = {
        GET_ATTACH_LIST : '/df/cs/getAttachList.do?tokenid='+ tokenid + '&ajax=noCache',  //附件查询
        DEL_ATTACH : '/df/cs/delAttach.do?tokenid='+ tokenid + '&ajax=noCache',  //附件删除
        UPLOAD_ATTACH : '/df/cs/uploadAttach.do?tokenid='+ tokenid,  //附件上传
        DOWNLOAD_ATTACH : '/df/cs/downloadAttach.do?tokenid='+ tokenid + '&ajax=noCache',  //附件下载
        OPEN_ATTACH : '/df/cs/previewFile.do?tokenid='+ tokenid + '&ajax=noCache',  //附件预览
        CANCEL_OPEN_ATTACH: '/df/cs/deletepreviewFile.do?tokenid='+ tokenid + '&ajax=noCache',  //取消预览
    };

    /* 根据链接url获取一些参数值 */
    var url = window.location.href;
    var entityId;
    if(url.split("autoentityId=").length > 1){
    	
    }else{
    	entityId = url.split("entityId=")[1].split("&")[0];
    }
    var entityName = url.split("entityName=").length > 1 ? url.split("entityName=")[1].split("&")[0] : '',
        oid = url.split("oid=").length > 1 ? url.split("oid=")[1].split("&")[0] : '',
        dep_id = url.split("dep_id=").length > 1 ? url.split("dep_id=")[1].split("&")[0] : '',
        dep_code = url.split("dep_code=").length > 1 ? url.split("dep_code=")[1].split("&")[0] : '',
        modelFlag = url.split("modelFlag=").length > 1 ? url.split("modelFlag=")[1].split("&")[0] : '',
        modular = url.split("modular=").length > 1 ? url.split("modular=")[1].split("&")[0] : '',
        limitOne = url.split("limitOne=").length > 1 ? url.split("limitOne=")[1].split("&")[0] : '0',
        limitType =  url.split("limitType=").length > 1 ? url.split("limitType=")[1].split("&")[0] : '',
        admin= url.split("admin=").length > 1 ? url.split("admin=")[1].split("&")[0] : '';
    /*var entityId = "ed1405eb-f941-40ae-b4c6-673370857d3e",//先将这些参数置空，避免测试报错
        entityName = "csof_sup_bill",
        module_code ="TASK,SUP,INFO,ACC,FILE"
        oid = "51",
        dep_id = "{2C4BC2D8-9920-4583-8A81-EF4AC7D67A3F}",
        dep_code = "5102",
        modelFlag = "0",
        admin= "Y";*/


    var viewModel = {};

    var commonFun = {//公共方法
        getNowTime : function () {//得到当前时间
            var nowDate = new Date();
            var yy = nowDate.getFullYear();
            var mm = nowDate.getMonth();
            var dd = nowDate.getDate();
             /*var hh = nowDate.getHours();
            var MM = nowDate.getMinutes();
            var ss = nowDate.getSeconds();*/
            //nowDate = nowDate.toString().split('GMT')[0];//先将时间转化为字符形式，通过分割得到简易时间

            function addZero(e){
                if(e < 10){
                    return '0' + e;
                }else{
                    return e;
                }
            }

             nowDate = yy + '/' + addZero(mm) + '/' + addZero(dd);

            //var nowTime = yy + '-' + addZero(mm) + '-' + addZero(dd) + ' ' + addZero(hh) + ':' + addZero(MM) + ':' + addZero(ss) ;
            return nowDate;
        },
        
        //显示过场的文件名    name : 字符串      num: 需要的文件长度
        showName: function(name, num) {
        	var len = name.length;
        	if(len > num) {
        		return name.substring(0, num) + '...';
        	} else {
        		return name;
        	}
        },

        
        isAdmin: function(e){//将admin作为参数e传进函数
        	var adminFlag = e == 'Y' ? true : false;
        	if(!adminFlag) {
        		$('button').hide();//所有按钮隐藏
        		$('#pickFile').show();//显示下载按钮
        		$('.dealWith > div:last').hide();
        		$('.state').hide();
        		$('#canclePre').show();//取消预览按钮
        	}else {
        		$('button').removeAttr('disabled');
        		$('.dealWith').show();
        		$('.state').show();
        	};
        },
        
        inArray: function(ele, arr){//判断元素在数组里是否存在，ele： 元素   arr： 数组
        	var exitFlag = 0;
        	$.each(arr, function(index, item){
        		if(item == ele) {
        			exitFlag ++;
        		}
        	});
        	var result = exitFlag > 0 ? true : false;
        	return result;
        },

        getFileSize : function(e){//将文件大小从字节装换为通用的表达方式 ，e为文件的大小
            var i = 0,
                sizeName ;//文件表达类型 ，分别为 B,KB,MB,GB,TB
            while(e > 1024){
                e = Math.round(e /1024);
                i++;
            }
            if(i == 0){
                sizeName = 'B';
            }
            if(i == 1){
                sizeName = 'KB';
            }
            if(i == 2){
                sizeName = 'MB';
            }
            if(i == 3){
                sizeName = 'GB';
            }
            if(i == 4){
                sizeName = 'TB';
            }
            return (e + sizeName);
        },
        getProcess : function (e) {//得到当前的上传进度,e为需要查询的文件名 ： fileName
            var process;//定义获取的上传进度
            $.ajax({
                type : 'POST',
                url : '',
                data : {
                    fileName : e
                },
                success : function (map) {
                    process = map.data;
                }
            });
            return process;
        },
        deleteFile : function (arr) {//  arr为当前要删除的文件的数组集（这个删除可以考虑后台接收数组参数，减少请求）
            var data = "";
            $.each(arr, function(index, item){
                data+=item+",";
            });
            data = data.substring(0,data.length-1);
            $.get(requireUrl.DEL_ATTACH,{ids : data}, function (map) {
                if(map.errorCode == '0') {
                    $.each(arr, function (index, item) {//item : 每个列表的id
                        $('#' + item).remove();
                    });
                    ip.ipInfoJump(map.data, 'info');
                }else{
                    ip.ipInfoJump(map.errorMsg, 'info');
                }
            });
        },
        openFile : function (e ,type) { //文件预览，e为文件的路径path,文件的格式
        	$('#preShowCon').html('文件加载中。。。。。');
        	$('#fileLoad').hide();
        	$('#preShowModel').show();
            $.get(requireUrl.OPEN_ATTACH, {attach_id : e}, function (map) {
            	if(map.errorCode == '0'){
            		if (map.data.typeFlag === '0') { //pdf预览
    					$('#preShowCon').media({width:870,height:430,src:map.data.htmlString});
    					$('#preShowCon').css('overflow','none');
    				} else if (map.data.typeFlag === '1') {//图片预览
    					var imgHtml = '<img src="' + map.data.htmlString + '" alt="" />';
    					$('#preShowCon').html(imgHtml);
    				} else { //其它office文件预览
    				    //$('#csof-right-iframe').html(mydata.htmlString);
    					$('#preShowCon').html(map.data.htmlString);
    					$('#preShowCon').css('overflow','auto');
    				}
            		fileName = map.data.fileName;
            	} else {
            		$('#preShowCon').html(map.message);
            	}
                
            });
        },
        showList : function(){//后台查询，显示列表
            var arr = [];
            var condition_map = {};
            var condition_rela = {};
                condition_map.entity_id = entityId;
                condition_rela.entity_id = "0";
            $.get(requireUrl.GET_ATTACH_LIST, {"condition_data": JSON.stringify(condition_map) ,"condition_rela": JSON.stringify(condition_rela)},function (map) {//初始化获取已上传文件列表
                maxUploadSize = map.maxUploadSize;
                uploader.options.fileSizeLimit = maxUploadSize; //将大小限制赋值给组件参数
                $.each(map.dataDetail, function (index, item) {
                    if(item.entity_id == entityId) {
                        arr.push(item);
                    }
                });
            }).then(function () {//ajax方法作为一个promise对象，当完成对于数组的组成后，执行下一步，避免异步执行
            	$list.html('');//先清空所有列表
                $.each(arr, function (index, item) {
                    var html =  '<div id="' + item.id + '" class="file-item">' +
                        '<div class="col-md-1 col-sm-1 col-xs-1 checkBox"><input type="checkbox"  name="listItem" value="'+ item.id +'" /></div>'+
                        '<div class="info col-md-6 col-sm-6 col-xs-5"> <div class="fileName" data-toggle="tooltip" title="'+ item.file_name +'">'+ commonFun.showName(item.file_name, 21) + '</div><div class="col-md-2 col-sm-2 col-xs-2 fileSize">'+ item.file_size +'</div><div class="col-md-8 col-sm-8 col-xs-8 fileDate" data-toggle="tooltip" title="'+ item.create_date +'">'+ item.create_date.substring(0,21) +'</div></div>' +
                        '<div class="state col-md-2 col-sm-2 col-xs-3"><span class="success">上传成功</span></div>'+
                        '<div class="col-md-3 col-sm-3 col-xs-3 dealWith">' +
                        '<div id="'+ item.id +'icon1"><span class="glyphicon glyphicon-file" aria-hidden="true"></span><span class="preShow icon upload-item">预览  </span></div>'+
                        '<div id="'+ item.id +'icon2"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span><span class="deleteItem icon">删除</span></div>'+
                        '</div>'+
                        '</div>';
                    $list.append(html);
                    commonFun.isAdmin(admin);
                });
            });
            
        }

    };

    /* 上传状态所需要的变化 */
    var uploadState = {
        success : function (e) { // e: file
            $('#' + e.id).find('.state').html('<div class="progress"><div class="progress-bar" role="progressbar" aria-valuenow=99" aria-valuemin="0" aria-valuemax="100" style="width: 99%;">99%</div></div>');
        },

        fail : function (e, msg) {// e : file
            var message = msg || '发生未知错误导致上传失败！';
            $('#' + e.id).find('.state').html('<span class="failed">上传失败</span>');
            ip.ipInfoJump(message, 'err');
            $('#' + e.id + 'icon1').html('<span class="glyphicon glyphicon-repeat" aria-hidden="true"></span><span class="preShow icon upload-restart">重试</span>');
        }
    };



    var uploader = WebUploader.create({
        // 选完文件后，是否自动上传。
        auto: true,

        // swf文件路径
        swf: 'js/Uploader.swf',

        // 文件接收服务端。这是通过插件的上传方法，但是个人觉得现在文件加入上传列表的时候可以进行上传
        //，由于文件时自动上传的，所以交互在那里写，后端的参数在这使用会产生不匹配
        server: '/df/cs/uploadAttach.do?tokenid='+ tokenid ,

        // 选择文件的按钮。可选。
        // 内部根据当前运行是创建，可能是input元素，也可能是flash.
        pick: '#pickUploadFile',

       /* chunked : false, //开始分片上传

        chunkSize : 100*1024, //每个片段的大小    1M

        chunkRetry : 3, //由于网络问题导致上传失败，可以允许多少次重新上传

        threads : 3, //上传并发数，允许最大的上传进程数的默认值
*/
        formData : {
            entityId : entityId,
            entityName : entityName,
            oid : oid,
            modular: modular,
            dep_id : dep_id,
            dep_code : dep_code
        },

        duplicate : true, //是否重复上传（同时选择多个一样的文件），true可以重复上传
        
        accept: {
            extensions: limitType == '' ? "gif,jpg,jpeg,png,doc,docx,xls,xlsx,ppt,pptx,pdf,txt,sql" : limitType//限制的文件格式
        },
        
        fileNumLimit: limitOne == '1' ? 1 : undefined,//限制上传文件的个数

        /*prepareNextFile : true,//上传当前分片时处理下一个分片
*/
        method:'POST', //上传方法

    });
    
    
    
    /*
    * 1.需要文件保存的请求
    * 2.查询文件上传状态请求，查询文件是否上传过，若上传过，返回上传的比
    * 3.文件预览功能请求
    * 4.
    *
    * */


    // 当有文件添加进来的时候
    $('#pickUploadFile').removeClass('webuploader-pick');
    uploader.on( 'fileQueued', function( file ) {  // webuploader事件.当选择文件后，文件被加载到文件队列中，触发该事件。等效于 uploader.onFileueued = function(file){...} ，类似js的事件定义。
        //通过本地记录判断是否曾经上传过此文件,判断状态是否继续还是开始上传
        var $li = $(
                '<div id="' + file.id + '" class="file-item">' +
                '<div class="col-md-1 col-sm-1 col-xs-1 checkBox"><input type="checkbox"  name="listItem" value="'+ file.id +'" /></div>'+
                '<div class="info col-md-6 col-sm-6 col-xs-6"> <div class="fileName">'+ file.name.substring(0,12) + '</div><div class="col-md-2 col-sm-2 col-xs-2 fileSize">'+ commonFun.getFileSize(file.size) +'</div><div class="col-md-8 col-sm-8 col-xs-8 fileDate">'+ commonFun.getNowTime() +'</div></div>' +
                    '<div class="state col-md-2 col-sm-2 col-xs-2"><div class="progress"><div class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">0%</div></div></div>'+
                    '<div class="col-md-3 col-sm-3 col-xs-3 dealWith">' +
                        '<div id="'+ file.id +'icon1"><span class="glyphicon glyphicon-arrow-up" aria-hidden="true"></span><span class="preShow icon upload-item">上传</span></div>'+
                        '<div id="'+ file.id +'icon2"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span><span class="deleteItem icon">删除</span></div>'+
                    '</div>'+
                '</div>'
            ),
        $img = $('#preShowCon');
        $img.html('');

        //删除
        $li.on('click', '.deleteItem', function () {
            $(this).parent().parent().parent().remove();
            uploader.removeFile(file);
        });

        //上传
        $li.on('click', '.upload-item', function () {
            $(this).parent().html('<span class="glyphicon glyphicon-pause" aria-hidden="true"></span><span class="preShow icon upload-stop">暂停</span>');
            uploader.upload(file);
            
        });
        //暂停
        $li.on('click', '.upload-stop', function () {
            uploader.stop(true);
            $(this).parent().html('<span class="glyphicon glyphicon-triangle-right" aria-hidden="true"></span><span class="preShow icon upload-continue">继续</span>');
        });

        //继续
        $li.on('click', '.upload-continue', function () {
            uploader.upload(file);
            var per = window.localStorage.getItem(file.id + '_p');
            $(this).parent().html('<span class="glyphicon glyphicon-pause" aria-hidden="true"></span><span class="preShow icon upload-stop">暂停</span>');
        });

        //重试
        $li.on('click', '.upload-restart', function () {
            $('#' + file.id).find('.state').html('<div class="progress"><div class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">0%</div></div');
            uploader.upload(file);
            $(this).parent().html('<span class="glyphicon glyphicon-pause" aria-hidden="true"></span><span class="preShow icon">文件上传中...</span>');
        });

        // $list为容器jQuery实例
        $list.append( $li );
       // $list.appendTo($li);
    });

    //文件开始上传前
    uploader.on('uploadStart', function (file) {
        $('#' + file.id + 'icon1').html('<span class="preShow icon " >文件上传中...</span>');
        var fileAcc = ["gif","jpg","jpeg","png","doc", "docx","xls","xlsx","ppt","pptx","pdf","txt","sql"];
        var type = file.ext;
        if(commonFun.inArray(type, fileAcc)) {
        	//
        } else {
        	ip.ipInfoJump('文件格式不正确！', 'info');
        	uploader.removeFile(file);
        }
        if(file.size == 0) {
        	ip.ipInfoJump('文件为空，请重新选择文件', 'info');
        	uploader.removeFile(file);
        }
        if(url.split("autoentityId=").length > 1){
        	 $.post('/df/task/getUUID.do?tokenid='+ tokenid + '&ajax=' + 'noCache',function(map){
                 entityId = map;
             });
        };
    });


    /* 服务端请求成功 */
    uploader.on('beforeFileQueued', function (file, res) {//file : 文件  ，res : 响应，服务端返回数据
        if(file.size > maxUploadSize){
            ip.ipInfoJump('文件过大，无法上传！','info');
        }
    });

    /* 服务端请求失败 */
    uploader.on('uploadError', function (file, reason) {// file: 文件对象， reason: 出错的code
        //uploader.upload(file);
        uploadState.fail(file);
    });

    /* 上传进度显示 */
    uploader.on('uploadProgress', function (file, perc) {// file: 文件对象， per: Number类型，上传的进度
        if(perc < 1){
            var per = Math.round(perc*100);
            $('#' + file.id).find('.progress-bar')
                .attr({"aria-valuenow" : per})
                .css({"width" : per + '%'})
                .text(per + '%');
        }else{
        	uploadState.success(file);
        	commonFun.showList();
        }

    });
    
    uploader.on('error', function(errType) {
    	if(errType == 'Q_TYPE_DENIED') {
    		ip.ipInfoJump('请选择正确的文件格式!', 'info');
    	}
    	
    	if(errType == 'Q_EXCEED_NUM_LIMIT') {
    		ip.ipInfoJump('系统只允许上传一个文件!', 'info');
    	}
    });

    /* 批量删除 */
    $('#deleteFiles').click(function () {
        var arr = [];
        var lists = $('input[type="checkbox"]:checked');
        if(lists.length > 0){
        	//var val = lists.val();
            $.each(lists, function (index, item) {
                var val = item.defaultValue;
                arr.push(val);
            });
            commonFun.deleteFile(arr);
        }else{
        	ip.ipInfoJump('请选择要删除的文件！', 'info');
        }
        
    });

    /* 下载文件 */
    $('#pickFile').click(function () {
        var arr = [];
        var lists = $('input[type="checkbox"]:checked');
        if(lists.length == 1){
        	var form = $("<form id='downloadForm'>");
            var id = lists.val();
    	    form.attr('style', 'display:none');
    	    form.attr('target', '');
    	    form.attr('method', 'post');
    	    var url = "/df/cs/downloadAttach.do?tokenid=" + tokenid + "&ajax=noCache";
    	    form.attr('action', url);
    	    var input = $('<input>');
    	    input.attr('type', 'hidden');
    	    input.attr('name', 'attach_id');
    	    input.attr('value', lists.val());//这里是每个文件自动生成的id
    	    $('#fileList').append(form);
    	    form.append(input);
    	    form.submit();
    	    form.remove();
    	    
        }else{
        	ip.ipInfoJump("请选择一个附件下载", 'info');
        }
    });

    $list.on('click', '.deleteItem', function () {//文件删除
        var id = $(this).parent().parent().parent().attr('id'),
            ids = [];
        ids.push(id);
        commonFun.deleteFile(ids);
    });

    //预览
    $list.on('click', '.preShow', function () {
        var id = $(this).parent().parent().parent().attr('id');
        //对文件的预览进行判断，只会预览word 、excel、pdf三种格式的文件
        var name = $(this).parent().parent().parent().find('.fileName').attr('title');//获取文件名字
        var fileType = name.split('.')[name.split('.').length - 1];//文件的后缀,因为文件名内可能存在.所以只能取所有。分割开的最后一个元素才是后缀名
        var typeArr = ['doc', 'docx','xls' ,'xlsx', 'pdf','ppt','pptx','txt','sql','jpg','png','gif','jpeg'];//允许预览的文件后缀集合
        if(commonFun.inArray(fileType, typeArr)) {//判断读取的后缀是否在集合内
        	commonFun.openFile(id, fileType);
        }else {
        	ip.ipInfoJump('该文件不支持预览！', 'info');
        }
        
    });
    
    //关闭预览
    $('#canclePre').click(function(){
    	$('#fileLoad').show();
    	$('#preShowModel').hide();
    	$.get(requireUrl.CANCEL_OPEN_ATTACH, {fileName : fileName} ,function(map) {
    		if(map.errorCode == '0') {
    		}
    	});
    });
    
    //勾选全部
    $('#selectAll').click(function() {
    	if($('#selectAll').is(':checked')) {
    	    $('#fileList input[type="checkbox"]').prop('checked', 'checked');//将所有的list全部勾选
    	} else {
    		 $('#fileList input[type="checkbox"]').removeAttr('checked');//取消所有的list勾选状态  
    	}
    });
    
    
    //点击上传
    $('#fileUplaod').click(function() {
    	$('#pickUploadFile > div:last > label').click();
    });


function init(){  //整个JS代码初始化
        app = u.createApp({
            el: 'body',
            model: viewModel
        });
    commonFun.showList();
}
    init();
});