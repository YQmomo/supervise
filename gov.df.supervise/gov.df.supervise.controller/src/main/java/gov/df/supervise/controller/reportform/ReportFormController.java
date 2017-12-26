/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2017 北京用友政务软件有限公司
 * </p>
 * <p>
 * Company: 北京用友政务软件有限公司
 * </p>
 * <p>
 * CreateData: 2017-8-27上午11:46:47
 * </p>
 * 
 * @author songlr3
 * @version 1.0
 */
package gov.df.supervise.controller.reportform;

import gov.df.fap.service.util.sessionmanager.SessionUtil;
import gov.df.supervise.api.reportfprm.ReportFormService;
import gov.df.supervise.bean.report.CsofEReportEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/ReportForm")
public class ReportFormController {

	@Autowired
	private ReportFormService reportFormService;

	/**
	 * 录入列表通过SID、BILLTYPE_CODE查询
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/selectSupReportBySidBillcode.do")
	public @ResponseBody
	Map<String, Object> selectSupReportBySidBillcode(
			HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> param = new HashMap<String, Object>();
		List<?> data = new ArrayList<Object>();
		String sid = request.getParameter("sid");
		String billtypeCode = request.getParameter("billtypeCode");
		param.put("sid", sid);
		param.put("billtypeCode", billtypeCode);
		try {
			data = reportFormService.selectSupReportBySidBillcode(param);
			result.put("data", data);
			result.put("errorCode", 0);
		} catch (Exception e) {
			result.put("errorCode", -1);
			result.put("errorMsg", "查询失败");
			result.put("message", "异常"); // zfn 未来改成标准消息异常
		}
		return result;
	}

	/**
	 * 通过CHR_ID查询拼接报表URL
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/selectEReportByChrId.do")
	public// @ResponseBody
	// Map<String, Object>
	String selectEReportByChrId(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Map<String, Object> result = new HashMap<String, Object>();
		CsofEReportEntity csofEReportEntity = new CsofEReportEntity();
		String URL;
		String chrId = request.getParameter("chrId");
		String userid = ("&userid=" + request.getParameter("obj_id"));
		String bbq;
		String param_add_str;
		if (request.getParameter("param_add_str") == ""
				|| "".equals(request.getParameter("param_add_str"))) {
			param_add_str = "";
		} else {
			String str = request.getParameter("param_add_str");
			String str1 = str.replaceAll("@", "=");
			String str2 = str1.replaceAll("|", "&");
			param_add_str = ("&" + str2);
		}
		String CSOF_REPORT_IP_URL = reportFormService.getIpValue();
		String CSOF_REPORT_BI_URL = reportFormService.getBiValue();
		String id = ("id=" + reportFormService.getUser());
		String pw = ("&pw=" + reportFormService.getPw());
		String target = ("&target=task");
		// String task = ("&task=" + csofEReportEntity.getReportFile());
		// String resid = ("resid=" + csofEReportEntity.getReportFile());
		try {
			csofEReportEntity = reportFormService.selectEReportByChrId(chrId);
			if (csofEReportEntity.getReportType() == "1"
					|| csofEReportEntity.getReportType().equals("1")) {
				String task = ("&task=" + csofEReportEntity.getReportFile());
				if (request.getParameter("bbq_date") == ""
						|| "".equals(request.getParameter("bbq_date"))) {
					bbq = "";
				} else {
					bbq = ("&bbq=" + request.getParameter("bbq_date") + "----")
							.substring(0, 13);
					// if (csofEReportEntity.getReportCycle() == 1 ||
					// csofEReportEntity.getReportCycle().equals(1)) {
					// bbq = ("&bbq="
					// + request.getParameter("bbq_date").substring(0,4) +
					// "----").substring(0, 13);
					// } else if (csofEReportEntity.getReportCycle() == 2 ||
					// csofEReportEntity.getReportCycle().equals(2)) {
					// String bbqf;
					// if (request.getParameter("bbq_date").substring(4, 6) ==
					// "01"
					// || request.getParameter("bbq_date").substring(4, 6) ==
					// "02"
					// || request.getParameter("bbq_date").substring(4, 6) ==
					// "03") {
					// bbqf = "01";
					// } else if (request.getParameter("bbq_date").substring(4,
					// 6) == "04"
					// || request.getParameter("bbq_date").substring(4, 6) ==
					// "05"
					// || request.getParameter("bbq_date").substring(4, 6) ==
					// "06") {
					// bbqf = "02";
					// } else if (request.getParameter("bbq_date").substring(4,
					// 6) == "07"
					// || request.getParameter("bbq_date").substring(4, 6) ==
					// "08"
					// || request.getParameter("bbq_date").substring(4, 6) ==
					// "09") {
					// bbqf = "03";
					// } else {
					// bbqf = "04";
					// }
					// bbq = ("&bbq="
					// + request.getParameter("bbq_date").substring(0,4) + bbqf
					// + "----").substring(0, 13);
					// } else if (csofEReportEntity.getReportCycle() == 3 ||
					// csofEReportEntity.getReportCycle().equals(3)) {
					// bbq = ("&bbq="
					// + request.getParameter("bbq_date").substring(0,6) +
					// "----").substring(0, 13);
					// } else {
					// bbq = ("&bbq="
					// + request.getParameter("bbq_date").substring(0,8) +
					// "----").substring(0, 13);
					// }
				}
				if (request.getParameter("readonly") == "false"
						|| request.getParameter("readonly").equals("false")) {
					URL = (CSOF_REPORT_IP_URL + id + pw + target + task
							+ userid + bbq + param_add_str);
				} else {
					String readonly = "&readonly=true";
					URL = (CSOF_REPORT_IP_URL + id + pw + target + task
							+ userid + bbq + param_add_str + readonly);
				}
			} else {
				String resid = ("resid=" + csofEReportEntity.getReportFile());
				id = "&" + id;
				URL = (CSOF_REPORT_BI_URL + resid + id + pw
						+ "&showmenu=false&showparams=true&calcnow=true" + param_add_str);
			}
			// response.sendRedirect("http://www.baidu.com");
			// response.sendRedirect(URL);
			// String data = sendPost(URL, "key=123&v=456");
			// System.out.println(data);
			result.put("data", URL);
			result.put("errorCode", 0);
			System.out.println("------------------------------" + URL
					+ "---------------------------------------");
			// return "forward:/" + URL;
			response.sendRedirect(URL);
		} catch (Exception e) {
			result.put("errorCode", -1);
			result.put("errorMsg", "查询路径拼接失败");
			result.put("message", "异常"); // zfn 未来改成标准消息异常
		}
		// return null;
		return null;
	}

	/**
	 * 政策法规登录报表URL
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/selectEReportByUserType.do")
	public String
	// @ResponseBody Map<String, Object>
	selectEReportByUserType(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		String URL;
		String userType = request.getParameter("userType");
		String dep_code = request.getParameter("dep_code");
		String user_code = request.getParameter("user_code");
		String CSOF_REPORT_IP_URL = reportFormService.getIpValue();
		String id = null;
		String pw = ("&pw=" + reportFormService.getPw());
		String org_code = (String) SessionUtil.getUserInfoContext()
				.getOrgCode();// 获取org_code作为专员办id(oid)
		try {
			if (userType.equals("oid")) {
				id = ("id=" + org_code);
			} else if (userType.equals("user_code")) {
				id = ("id=" + dep_code + user_code);
			} else if (userType.equals("sys")) {
				id = ("id=" + reportFormService.getUser());
			} else if (userType.equals("")) {
				id = ("id=" + reportFormService.getUser());
			}
			URL = (CSOF_REPORT_IP_URL + id + pw);
			response.sendRedirect(URL);
			result.put("data", URL);
			result.put("errorCode", 0);
		} catch (Exception e) {
			result.put("errorCode", -1);
			result.put("message", "异常"); // zfn 未来改成标准消息异常
		}
		return null;
	}

	/**
	 * 政策法规内容报表URL
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/selectEReportByIsPublic.do")
	public String // @ResponseBody Map<String, Object>
	selectEReportByIsPublic(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		String URL;
		String isPublic = request.getParameter("isPublic");
		String groupId = request.getParameter("groupId");
		String canManage = ("&canManage=" + request.getParameter("canManage"));
		String CSOF_REPORT_BU_URL = reportFormService.getBuValue();
		String GroupId = null;
		String cmd = ("cmd=listBulletinByGroup");
		String org_code = (String) SessionUtil.getUserInfoContext()
				.getOrgCode();// 获取org_code作为专员办id(oid)
		try {
			if (isPublic.equals("0")) {
				GroupId = ("&groupId=" + groupId + org_code);
			} else if (isPublic.equals("1")) {
				GroupId = ("&groupId=" + groupId);
			} else if (isPublic.equals("")) {
				GroupId = ("&groupId=" + groupId + org_code);
			}
			URL = (CSOF_REPORT_BU_URL + cmd + GroupId + canManage);
			response.sendRedirect(URL);
			result.put("data", URL);
			result.put("errorCode", 0);
		} catch (Exception e) {
			result.put("errorCode", -1);
			result.put("message", "异常"); // zfn 未来改成标准消息异常
		}
		return null;
	}

	/**
	 * 查询分析报表
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/selectBiReportByChrId.do")
	public String selectBiReportByChrId(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Map<String, Object> result = new HashMap<String, Object>();
		CsofEReportEntity csofEReportEntity = new CsofEReportEntity();
		String URL;
		String accId = request.getParameter("accId");
		String sumFlag = request.getParameter("sumFlag");
		String oId = request.getParameter("oId");
		String setId = request.getParameter("setId");
		String bookId = request.getParameter("bookId");
		String chrId;
		try {
			chrId = accId.equals("1") & sumFlag.equals("2") ? "1" : (accId.equals("1") & sumFlag.equals("3") ? "2" : 
				(accId.equals("2") & sumFlag.equals("2")) ? "3" : "4");
			csofEReportEntity = reportFormService.selectEReportByChrId(chrId);
			// if(accId=="1"&sumFlag=="2"){
			// chrId = "1";
			// csofEReportEntity =
			// reportFormService.selectEReportByChrId(chrId);
			// }else if(accId=="1"&sumFlag=="3"){
			// chrId = "2";
			// csofEReportEntity =
			// reportFormService.selectEReportByChrId(chrId);
			// }else if(accId=="2"&sumFlag=="2"){
			// chrId = "3";
			// csofEReportEntity =
			// reportFormService.selectEReportByChrId(chrId);
			// }else{
			// chrId = "4";
			// csofEReportEntity =
			// reportFormService.selectEReportByChrId(chrId);
			// }
			String CSOF_REPORT_BI_URL = reportFormService.getBiValue();
			String id = ("&id=" + reportFormService.getUser());
			String pw = ("&pw=" + reportFormService.getPw());
			String resid = ("resid=" + csofEReportEntity.getReportFile());
			URL = (CSOF_REPORT_BI_URL + resid + id + pw + "&@oId=" + oId
					+ "&@setId=" + setId);
			response.sendRedirect(URL);
			result.put("data", URL);
			result.put("errorCode", 0);
		} catch (Exception e) {
			result.put("errorCode", -1);
			result.put("errorMsg", "查询路径拼接失败");
			result.put("message", "异常"); // zfn 未来改成标准消息异常
		}
		System.out.printf("", result);
		return null;
	}

	// /**
	// * 向指定 URL 发送POST方法的请求
	// *
	// * @param url
	// * 发送请求的 URL
	// * @param param
	// * 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	// * @return 所代表远程资源的响应结果
	// */
	// public static String sendPost(String url, String param) {
	// OutputStreamWriter out = null;
	// BufferedReader in = null;
	// String result = "";
	// try {
	// URL realUrl = new URL(url);
	// // 打开和URL之间的连接
	// URLConnection conn = realUrl.openConnection();
	// // 设置通用的请求属性
	// conn.setRequestProperty("accept", "*/*");
	// conn.setRequestProperty("connection", "Keep-Alive");
	// conn.setRequestProperty("user-agent",
	// "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
	// // 发送POST请求必须设置如下两行
	// conn.setDoOutput(true);
	// conn.setDoInput(true);
	// // 获取URLConnection对象对应的输出流
	// out = new OutputStreamWriter(conn.getOutputStream(),"UTF-8");
	// // conn.setRequestProperty("Accept-Charset", "UTF-8");
	// // conn.setRequestProperty("contentType", "UTF-8");
	// // 发送请求参数
	// // out.print(param);
	// // flush输出流的缓冲
	// out.flush();
	// // 定义BufferedReader输入流来读取URL的响应
	// in = new BufferedReader(
	// new InputStreamReader(conn.getInputStream()));
	// String line;
	// while ((line = in.readLine()) != null) {
	// result += line;
	// }
	// } catch (Exception e) {
	// System.out.println("发送 POST 请求出现异常！"+e);
	// e.printStackTrace();
	// }
	// //使用finally块来关闭输出流、输入流
	// finally{
	// try{
	// if(out!=null){
	// out.close();
	// }
	// if(in!=null){
	// in.close();
	// }
	// }
	// catch(IOException ex){
	// ex.printStackTrace();
	// }
	// }
	// return result;
	// }

}
