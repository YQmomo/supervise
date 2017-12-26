var rgset_relation;
var rg_code;
var set_year;
$(function(){
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
		}
	});	
	refreshCaptcha(); 
	$("#captchaImg").on("click", refreshCaptcha); 
//	 $("#captcha").on("keyup", checkCaptchaInput);
});

function login() {
	var userName = $('#username').val();
    var passWord = $('#password').val();
    var setYear = $("#setYear").val();
   	var rgCode = $("#rgCode").val();
   	var Code = $("#captcha").val();
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
	pwdremember();
	
	var password = hex_md5(passWord);
	
	$.ajax({
		url:"/df/login/userLogin.do",
		type: 'POST',
		data:{"ajax":"ajax","userName":userName,"passWord":password,"setYear":setYear,"rgCode":rgCode,"code":Code},
		dataType: 'json',
		success: function (data){
			var flag = data.flag;
			var tokenId = data.tokenid;
			if(flag == "1"){
				//dfp.localStorage.setItem(dfp.key.tokenid, dfp.key.dfValidFlag+tokenId);
				window.location.href="/df/portal/admin/index/index.html?tokenid="+tokenId;	
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