var rgset_relation;
var rg_code;
var set_year;
var org_code;
$(function(){
	$.ajax({
        type: 'POST',
        url: "/df/csofinfo/getData.do?",
        cache:false,
        data: {"ele_code":"CSOF_OFFICE"},
        dataType: 'JSON',
        async: false,
        success: function(data){
			var data = data.data;
			console.log(data);
			for(var i=0;i<data.length;i++){
				$("#organ").append('<option value="'+data[i].CHR_ID+'">'+data[i].CHR_CODE+data[i].CHR_NAME+'</option>');
			}
					
	}
        });
	refreshCaptcha(); 
	$("#captchaImg").on("click", refreshCaptcha); 
//	 $("#captcha").on("keyup", checkCaptchaInput);
	// 加载年度、区划信息
	$.ajax({
		url:"/df/portal/getYearRgcode.do?",
		type: 'POST',
		cache:false,
		success: function (data){
		rgset_relation = data.rgset_relation; //map
		rg_code = data.rg_code;
		set_year = data.set_year;
		
		var rg_codeHtml = "";
		for(var i in rg_code){
			rg_codeHtml += '<option value="'+rg_code[i]+'">'+rg_code[i]+'</option>';
		}
		$("#rgCode").html(rg_codeHtml);
	$("#rg_code").on("blur", function(){
		// TODO 按rgcode加载年度
	
	});	
		var set_yearHtml = "";
		for(var i in set_year){
			set_yearHtml += '<option value="'+set_year[i]+'">'+set_year[i]+'</option>';
		}
		$("#setYear").html(set_yearHtml);
	$("#set_year").on("blur", function(){
	// TODO 按年度加载rgcode
	
  });
	}});
//	// 加载机构,放入到select中(需要在后台加入这个方法)
//	$.get("/df/portal/getOrgCode.do?tokenid=before", function(data){
//		//rgset_relation = data.rgset_relation; //map
//		console.log(data);
//		org_code = data.org_code;
//		//var organHtml = "";
//		for(var i=1;i<org_code.length;i++){
//			$("#organ").append('<option value="'+org_code[i].chr_id+'">'+org_code[i].chr_code+org_code[i].chr_name+'</option>');
//		}
////		$("#organ").html(organHtml);
//	});
});
function refreshCaptcha() {  
	$("#captcha").val("");
    //重载验证码  
    $('#captchaImg').attr('src', '/df/login/getImage?' + Math.random());  
}  

function checkCaptchaInput() {
	var captchaText = $(this).val();
	 if(captchaText.length <=3 ){ //验证码一般大于三位  
         $("#captchaChecked").hide();  
         return;  
     }  
	$.ajax({
		type : 'post',
		url : "/df/login/checkCode?",// 验证验证码
		data : {
			code : captchaText
		},
		dataType : 'JSON',
		async : false,
		success : function(result) {
			if (result.errorCode == "1") {
				$("#captchaChecked").show();
				captchaChecked = true;
			} else if(result.errorCode == "0" || result.errorCode == "-1"){
				$("#captchaChecked").hide();
				captchaChecked = false;
				alert(result.errorMsg);
				refreshCaptcha(); 
			}
		},
		error : function() {
			alert(result.errorMsg);
		}
	});
}  

function login() {
	var orGan = $("#organ").val();
	var userName = $('#username').val();
    var passWord = $('#password').val();
    var setYear = $("#setYear").val();
   	var rgCode = $("#rgCode").val();
   	var Code = $("#captcha").val();   	
   	if(orGan == ""){
   		alert("请选择机构！");
   		$("#organ").focus();
   		return false;
   	}
    if (userName == ""){
   		alert("请输入用户名！");
   		$("#username").focus();
   		return false;
   	}	
   	if (passWord == ""){
   		alert("请输入密码！");
   		$("#password").focus();
   		return false;
   	}
   	if (setYear == "" || setYear == "0"){
   		alert("请选择年度！");
   		$("#setYear").focus();
   		return false;
   	}
   	if (rgCode == "" || rgCode == "0"){
   		alert("请选择区划！");
   		$("#rgCode").focus();
   		return false;
   	}
	
	// pwd check
	//pwdCheck(passWord);
	//if(pwdCheck(passWord))
		//location.href = window.location.href="/df/portal/admin/login/register.html?tokenid="+tokenid;
	
	// pwd remember
	pwdremember();
	
	var password = hex_md5(passWord);
	//在userLogin中要加入organ和验证码，
	$.ajax({
		url:"/df/login/userLogin.do",
		type: 'POST',
		data:{"ajax":"ajax","userName":userName,"passWord":password,"setYear":setYear,"rgCode":rgCode,"oid" :orGan,"code":Code},
		dataType: 'json',
		success: function (data){
			var flag = data.flag;
			var tokenId = data.tokenid;
			if(flag == "1"){
				//dfp.localStorage.setItem(dfp.key.tokenid, dfp.key.dfValidFlag+tokenId);
				window.location.href="/df/portal/admin/index/csofindex.html?tokenid="+tokenId;	
				//window.location.href="/df/portal/admin/index/csofindex.html?tokenid="+tokenId;	
			}else if(flag == "9"){
				$("#captchaChecked").hide();
				captchaChecked = false;
				refreshCaptcha(); 
				alert("验证码失效！");
				return false;
			}else if(flag == "8"){
				$("#captchaChecked").hide();
				captchaChecked = false;
				refreshCaptcha(); 
				alert("验证码输入错误！");
				return false;
			}else if (flag == "0"){
				alert("用户名或密码错误！");
				refreshCaptcha();
				return false;
			}else if (flag == "-1"){
				refreshCaptcha(); 
				alert(data.message, 'info');
				return false;
			}
		}
	});
}

