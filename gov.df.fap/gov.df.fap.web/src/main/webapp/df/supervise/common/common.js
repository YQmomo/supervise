var csof = {};

//获取tokenId方法
function getTokenId(){
	var current_url = location.search;
	var params = (current_url || "").split('&');
	for(var i = 0; i < params.length; i++){
		if(params[i].toLowerCase().indexOf("tokenid=") > -1){
			return (params[i].split('=')[1]);
		}
	}
	return "";
};

//验证是否为数字
csof.isNum = function(value){
    if( value != null && value.length>0 && isNaN(value) == false){
        return true;
    }else{
        return false;
    }
};

/*
 * 金额格式化
 * s:要转换的数据
 * n:小数点的位数
*/
csof.fmoney = function(s, n)   
{   
   n = n > 0 && n <= 20 ? n : 2;   
   s = parseFloat((s + "").replace(/[^\d\.-]/g, "")).toFixed(n) + "";   
   var l = s.split(".")[0].split("").reverse(),   
   r = s.split(".")[1];   
   t = "";   
   for(i = 0; i < l.length; i ++ )   
   {   
      t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? "," : "");   
   }   
   return t.split("").reverse().join("") + "." + r;   
};

//刷新
function publicRefresh(id) {
	location.reload();
};	

/*录入视图校验
 * aim：视图DataTable
 * noVerifyRid：不需要验证的idsMap，注：不加viewid
 * */
function verifyInputView(aim,noVerifyRid){
	var sup_array = [];
	sup_array[0] = {};
	var nullFlag = 0;
	for(var i=0;i<aim.length;i++) {
		if(aim[i].type == "treeassist") {
			var value = $('#'+aim[i].id+"-h")[0].value;
		}else{
			var value = $('#'+aim[i].id)[0].value;
		}
		if(noVerifyRid.length > 0){
			var verifyFlag = 0;
			for(var j=0;j<noVerifyRid.length;j++){
				if(aim[i].id.split("-")[0] == noVerifyRid[j]){
					verifyFlag++;
				}
			}
			if(verifyFlag == 0){
				if(value == "" || value == null || value == "-1"){
					nullFlag++;
					$('#'+aim[i].id)[0].style.borderColor = 'red';
				}else{
					$('#'+aim[i].id)[0].style.borderColor = '#ccc';
				}
			}else{
				$('#'+aim[i].id)[0].style.borderColor = '#ccc';
			}
		}else{
			if(value == "" || value == null || value == "-1"){
				nullFlag++;
				$('#'+aim[i].id)[0].style.borderColor = 'red';
			}else{
				$('#'+aim[i].id)[0].style.borderColor = '#ccc';
			}
		}
	}
	if(nullFlag == 0){
		return true;
	}else{
		return false;
	}
};

//截止时间大于起始时间
function checkData(startTime,endTime){              
    if(startTime.length>0 && endTime.length>0){     
        var startTmp=startTime.split("-");  
        var endTmp=endTime.split("-");  
        var sd=new Date(startTmp[0],startTmp[1],startTmp[2]);  
        var ed=new Date(endTmp[0],endTmp[1],endTmp[2]);  
        if(sd.getTime()>ed.getTime()){   
            return false;     
        }     
    }     
    return true;     
};

//初始化按钮
function initBtns(menuId) {
	var resultData;
	$.ajax({
        type: 'get',
        url: '/df/cs/getActionButton.do' + "?tokenid=" + getTokenId() + "&ajax=noCache",
        data: {"menu_id":menuId},
        dataType: 'JSON',
        async: false,
        success: function (result) {
        	if(result.errorCode == "0") {
        		resultData = result.data;
        		for(var i=0;i<resultData.length;i++) {
        			$("#btns-group").append("<button class='" + resultData[i].CLASS_NAME + "' type='button' id='"+resultData[i].BUTTON_ID+"' "+resultData[i].PARAM+" onclick='"+resultData[i].FUNC_NAME+"(this.id)'>"+resultData[i].DISPLAY_TITLE+"</button>");
        			if(resultData[i].ENABLED == "0") {
        				$("#"+resultData[i].BUTTON_ID).attr("disabled","disabled");
        			}
        		}	            		
        	}else{
        		return false;
        	}	            	
        }, error: function () {
        	return false;
        }
    });
	return resultData;
}; 

//通过id获取按钮信息
function getBtnMsgById(btnsData,id) {
	for(var bt = 0; bt<btnsData.length;bt++) {
		if(btnsData[bt].BUTTON_ID == id) {
			var btn = btnsData[bt];
			return btn;
		}
	}
};

/*实时计算textarea的输入字数,并限制输入
 * obj：textarea对象
 * maxLength：限制最大字数
 * areaId:输入字数显示*/
function checkTextareaLength(obj,maxLength,menuId) {
	$('#'+menuId+'-textNum').text(obj.value.length);// 这句是在键盘按下时，实时的显示字数
	if (obj.value.length > maxLength) {
		$('#'+menuId+'-textNum').text(maxLength);// 长度大于100时0处显示的也只是100
		obj.value = obj.value.substring(0,maxLength); 
		alert("输入字数达到限制！");
	}
}

/*意见框*/
csof.opinionModal = function(titleMsg,sureId, cancelCla,menuId) {
	// 带确定 取消的消息提示框
	var configModal = $("#"+menuId+"-opinion-modal")[0];
	if (!configModal) {
		var innerHTML = "<div id='"+menuId+"-opinion-modal' class='modal '><div class='modal-dialog'  role='document'>";
		innerHTML += '<div class="modal-content"><div class="modal-header">';
		innerHTML += '<button type="button" class="close ' + cancelCla + '" data-dismiss="modal" aria-hidden="true">&times;</button>';
		innerHTML += '<h4 class="modal-title" id="'+menuId+'-opinionModalLabel">' + titleMsg + '</h4></div><div class="modal-body"  style="height: 300px;">';
		innerHTML += '<div class="col-md-12" style="padding: 10px 0;"><label for="'+menuId+'-op_name" class="text-right" style="height: 0;width:21%;font-size: 12px; font-weight: normal;">业务操作：</label>';
		innerHTML += '<span type="text" id="'+menuId+'-op_name"></span><span class="pull-right" style="padding: 20px 60px 0 0;"><span id="'+menuId+'-textNum">0</span>/100</div><div class="col-md-12" style="padding: 0px;">';
		innerHTML += '<label for="'+menuId+'-opinion" class="text-right" style="height: 0;width: 21%;font-size: 12px; font-weight: normal;">意见内容：</label>';
		innerHTML += '<textarea style="width:69%;height: 200px;" id="'+menuId+'-opinion" onpropertychange="checkTextareaLength(this,100,\''+menuId+'\');" onkeyup="verifyInput(this,\'200\')" oninput="checkTextareaLength(this,100,\''+menuId+'\');"></textarea></div>';
		innerHTML += '</div><div class="modal-footer"><button type="button" class="'+menuId+'-cancel-opinion btn btn-default ' + cancelCla + '" data-dismiss="modal">取消</button>';
		innerHTML += '<button type="button" id=' + sureId + ' class="btn btn-primary '+menuId+'-sure-opinion">确认</button></div></div></div></div>';
		$("body").append(innerHTML);
		$('#'+menuId+'-opinion-modal').modal('show');
	} else {
		$('#'+menuId+'-opinion-modal').modal('show');
		$("#"+menuId+"-opinionModalLabel").text(titleMsg);
		$("."+menuId+"-sure-opinion").attr("id", sureId);
		$("."+menuId+"-cancel-opinion").addClass(cancelCla);
	}
};

/*
 * 初始化要素树
 * elecode:要素
 * dataTable:存放树的datatable
 * areaId:树的id
*/
function initTreeByElecode(elecode,dataTable,areaId){
    $.get('/df/tree/getElementData.do?tokenid='+ getTokenId() + '&ajax=noCache', {'ele_code': elecode}, function(map){
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
            dataTable.setSimpleData(data);
            var treeObj = $.fn.zTree.getZTreeObj(areaId);
            treeObj.expandAll(true);
            treeObj.cancelSelectedNode();
        }else{
            ip.ipInfoJump(map.errorMsg, 'error');
        }
    });
};

/*input和textaera加入校验
 * 校验长度，num(input为30，textaera为100)
 * 校验非法字符
 */
function verifyInput(obj,num){
	var w = 0; 
	var rs = ""; 
	var pattern = new RegExp("[`~!@#$^&*()=|{}':;',\\[\\].<>/?~]"); 
    var sql_str="and ,delete ,or ,exec ,insert ,select ,union ,update ,*,',join ,>,<";  
	for (var i=0; i<obj.value.length; i++) { 
		//charCodeAt()获取字符串中某一个字符的编码 
		var c = obj.value.charCodeAt(i); 
		//单字节加1 
		if ((c >= 0x0001 && c <= 0x007e) || (0xff60<=c && c<=0xff9f)) { 
		   w++; 
		} else { 
		   w+=2; 
		} 
		rs = rs+obj.value.substr(i, 1).replace(pattern, ''); 
		if (w > num) { 
		   rs = rs.substr(0,i); 
		   alert("输入字数达到限制！");
		   break; 
		}
    }
	var sqlStr=sql_str.split(',');  
    var flag=false;  
    for(var i=0;i<sqlStr.length;i++){  
        if(rs.toLowerCase().indexOf(sqlStr[i])!=-1){  
        	rs = rs.replace(new RegExp(sqlStr[i], "g"), "");
            alert("用户名字符中包含了敏感字符"+sql_str+",请重新输入！"); 
            break;  
        }  
    }  
    if(obj.value != rs){
    	obj.value = rs;
    }
}
$(document).keydown(function(event){
    switch(event.keyCode){
       case 13:return false; 
       }
});