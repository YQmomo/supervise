package gov.df.supervise.controller.csofbill;

import gov.df.supervise.api.bill.BillService;
import gov.df.supervise.api.common.CommonService;

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

import com.alibaba.fastjson.JSONObject;

@Controller
@RequestMapping(value = "/df/bill")
public class MofBillController {

  @Autowired
  private CommonService commonService;

  @Autowired
  private BillService billService;

  /**
   * 总任务单(csof_mof_bill)查询方法
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(method = RequestMethod.GET, value = "/getMofBill.do")
  public @ResponseBody
  Map<String, Object> getMofBill(HttpServletRequest request, HttpServletResponse response) {

    Map<String, Object> map = new HashMap<String, Object>();
    try {
      List resultList = new ArrayList();
      String pageInfo = null != request.getParameter("pageInfo") ? request.getParameter("pageInfo") : "";
      String chr_id = null != request.getParameter("CHR_ID") ? request.getParameter("CHR_ID") : "";
      String type_code = null != request.getParameter("TYPE_CODE") ? request.getParameter("TYPE_CODE") : "";

      String condition = " where 1=1 and is_valid=1 ";
      if (!chr_id.equals("") && (!type_code.equals(""))) {
        condition += "AND " + type_code + "= '" + chr_id + "'";
      }

      resultList = commonService.getDataList("vw_csof_mof_bill", condition, false, pageInfo);

      map.put("flag", true);
      map.put("errorCode", "0");
      map.put("dataDetail", resultList);
      map.put("totalElements", resultList.size());
    } catch (Exception e) {
      e.printStackTrace();
      map.put("errorCode", "1");
      map.put("errorMsg", "获取数据出现异常");
    }

    return map;

  }

  /**
   * 总任务单生成方法
   * @param request
   * @param response
   * @return 
   */
  @RequestMapping(method = RequestMethod.POST, value = "/saveMofBill.do")
  public @ResponseBody
  Map<String, Object> saveMofBill(HttpServletRequest request, HttpServletResponse response) {

    Map<String, Object> map = new HashMap<String, Object>();
    try {

      int result = 0; //执行条目数
      String billtype_code = ""; //单据编号
      List officeList = new ArrayList();
      List supList = new ArrayList();
      Map workMap = new HashMap();
      if (null != request.getParameter("billtype_code")) {
        billtype_code = request.getParameter("billtype_code");
      }

      if (null != request.getParameter("officeList")) {
        officeList = JSONObject.parseArray(request.getParameter("officeList").toString(), Map.class);
      }

      if (null != request.getParameter("supList")) {
        supList = JSONObject.parseArray(request.getParameter("supList").toString(), Map.class);
      }

      if (null != request.getParameter("workFlowMap")) {
        workMap = JSONObject.parseObject(request.getParameter("workFlowMap").toString());
      }

      result = billService.saveMofBill(officeList, supList, billtype_code, workMap);

      map.put("flag", true);
      map.put("errorCode", "0");
      map.put("data", result);
    } catch (Exception e) {
      e.printStackTrace();
      map.put("errorCode", "1");
      map.put("errorMsg", "获取数据出现异常");
    }

    return map;

  }

  /**
   * 总任务单(csof_mof_bill)修改方法
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(method = RequestMethod.POST, value = "/updateMofBill.do")
  public @ResponseBody
  Map<String, Object> updateMofBill(HttpServletRequest request, HttpServletResponse response) {

    Map<String, Object> map = new HashMap<String, Object>();
    try {
      int result = 0;
      String billtype_code = ""; //单据编号
      Map mofBillData = new HashMap(); //总任务单生成录入数据

      if (null != request.getParameter("billtype_code")) {
        billtype_code = request.getParameter("billtype_code");
      }

      if (null != request.getParameter("mofBillData")) {
        mofBillData = JSONObject.parseObject(request.getParameter("mofBillData").toString());
      }
      result = billService.updateMofBill(mofBillData, billtype_code);

      map.put("flag", true);
      map.put("errorCode", "0");
      map.put("data", result);
    } catch (Exception e) {
      e.printStackTrace();
      map.put("errorCode", "1");
      map.put("errorMsg", "获取数据出现异常");
    }

    return map;

  }

  /**
   * 总任务单(csof_mof_bill)删除方法
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(method = RequestMethod.POST, value = "/deleteMofBill.do")
  public @ResponseBody
  Map<String, Object> deleteMofBill(HttpServletRequest request, HttpServletResponse response) {

    Map<String, Object> map = new HashMap<String, Object>();
    try {
      int result = 0;
      String ids = request.getParameter("ids") != null ? request.getParameter("ids") : "";
      result = billService.deleteMofBill(ids, "id"); //返回数据库执行条目数
      map.put("flag", true);
      map.put("errorCode", "0");
      map.put("result", result);
    } catch (Exception e) {
      e.printStackTrace();
      map.put("errorCode", "1");
      map.put("errorMsg", "获取数据出现异常");
    }

    return map;

  }

  @RequestMapping(method = RequestMethod.GET, value = "/getWorkData.do")
  public @ResponseBody
  Map<String, Object> getWorkData(HttpServletRequest request, HttpServletResponse response) {

    Map<String, Object> map = new HashMap<String, Object>();
    try {
      List resultList = new ArrayList();
      String pageInfo = "";
      String menu_id = "";
      String status = "";
      String billtype_code = "";
      Map conditionMap = new HashMap();
      if (null != request.getParameter("pageInfo")) {
    	  pageInfo = request.getParameter("pageInfo");
      }
      if (null != request.getParameter("menu_id")) {
    	  menu_id = request.getParameter("menu_id");
      }
      if (null != request.getParameter("status")) {
    	  status = request.getParameter("status");
      }
      if (null != request.getParameter("billtype_code")) {
    	  billtype_code = request.getParameter("billtype_code");
      }
      if (null != request.getParameter("conditionMap")) {
    	  conditionMap = JSONObject.parseObject(request.getParameter("conditionMap").toString());
      }
      if (!(menu_id.equals("") || status.equals("") || billtype_code.equals(""))) {
    	  resultList = commonService.getWorkData(menu_id, status, billtype_code, conditionMap, pageInfo);
      }
      map.put("flag", true);
      map.put("errorCode", "0");
      map.put("dataDetail", resultList);
      map.put("totalElements", resultList.size());
    } catch (Exception e) {
      e.printStackTrace();
      map.put("errorCode", "1");
      map.put("errorMsg", "获取数据出现异常");
    }

    return map;

  }
  
  /**
   * 总任务单下达
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(method = RequestMethod.POST, value = "/approveTask.do")
  public @ResponseBody
  Map<String, Object> approveTask(HttpServletRequest request, HttpServletResponse response) {
	  
	  Map<String, Object> map = new HashMap<String, Object>();
	  try {
		  Map workMap = new HashMap();
		  String id = "";
		  if(null!=request.getParameter("id")){
			  id = request.getParameter("id");
		  }
		  if (null != request.getParameter("workFlowMap")) {
		        workMap = JSONObject.parseObject(request.getParameter("workFlowMap").toString());
		  }
		  if(id.equals("")||workMap.isEmpty()){
			  map.put("errorCode", "1");
			  map.put("errorMsg", "请求参数不正确");
		  }else{
			  int resNum = billService.approveTask(id,workMap);
			  map.put("errorCode", "0");
			  map.put("totalElements", resNum);
		  }
		  
	  } catch (Exception e) {
		  e.printStackTrace();
		  map.put("errorCode", "1");
		  map.put("errorMsg", "获取数据出现异常");
    }

    return map;

  }
  
  /**
   * 总任务单登记接收
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(method = RequestMethod.POST, value = "/receiveTask.do")
  public @ResponseBody
  Map<String, Object> receiveTask(HttpServletRequest request, HttpServletResponse response) {
	  
	  Map<String, Object> map = new HashMap<String, Object>();
	  try {
		  Map workMap = new HashMap();
		  String id = "";
		  if(null!=request.getParameter("id")){
			  id = request.getParameter("id");
		  }
		  if (null != request.getParameter("workFlowMap")) {
		        workMap = JSONObject.parseObject(request.getParameter("workFlowMap").toString());
		  }
		  if(id.equals("")||workMap.isEmpty()){
			  map.put("errorCode", "1");
			  map.put("errorMsg", "请求参数不正确");
		  }else{
			  int resNum = billService.receiveTask(id,workMap);
			  map.put("errorCode", "0");
			  map.put("totalElements", resNum);
		  }
		  
	  } catch (Exception e) {
		  e.printStackTrace();
		  map.put("errorCode", "1");
		  map.put("errorMsg", "获取数据出现异常");
    }

    return map;

  }

  
  /**
   * 任务单分解
   * @param request
   * @param response
   * @return 
   */
  @RequestMapping(method = RequestMethod.POST, value = "/saveTaskBill.do")
  public @ResponseBody
  Map<String, Object> saveTaskBill(HttpServletRequest request, HttpServletResponse response) {

	  Map<String, Object> map = new HashMap<String, Object>();
	  try {

		  int result = 0;                      //执行条目数		  
		  Map mofData = new HashMap();      //总任务单信息
	      List objectList = new ArrayList();   //处室信息
	      List taskList = new ArrayList();     //任务期间列表
		  List agencyList = new ArrayList();   //单位树

		  Map mofWorkMap = new HashMap();      //总任务单 csof_mof_bill工作流信息
		  Map taskWorkMap = new HashMap();     //监管任务 csof_task_bill工作流信息
	      
	      if (null != request.getParameter("objectList")) {
	          objectList = JSONObject.parseArray(request.getParameter("objectList"), Map.class);
	      }
	      
	      if (null != request.getParameter("agencyList")) {
	    	  agencyList = JSONObject.parseArray(request.getParameter("agencyList").toString(), Map.class);
	      }
	
	      if (null != request.getParameter("mofData")) {
	    	  mofData = JSONObject.parseObject(request.getParameter("mofData").toString());
	      }
	      
	      if (null != request.getParameter("taskList")) {
	    	  taskList = JSONObject.parseArray(request.getParameter("taskList").toString(), Map.class);
	      }

	      if (null != request.getParameter("mof_workFlowMap")) {
	    	  mofWorkMap = JSONObject.parseObject(request.getParameter("mof_workFlowMap").toString());
	      }
	      
	      if (null != request.getParameter("task_workFlowMap")) {
	    	  taskWorkMap = JSONObject.parseObject(request.getParameter("task_workFlowMap").toString());
	      }
	      
	      result = billService.saveTaskBill(objectList, agencyList, mofData, taskList, mofWorkMap, taskWorkMap );
	
	      map.put("flag", true);
	      map.put("errorCode", "0");
	      map.put("data", result);
	  } catch (Exception e) {
	      e.printStackTrace();
	      map.put("errorCode", "1");
	      map.put("errorMsg", "获取数据出现异常");
    }

    return map;

  }
  
  /**
   * 撤銷任务单分解
   * @param request
   * @param response
   * @return 
   */
  @RequestMapping(method = RequestMethod.POST, value = "/deleteTaskBill.do")
  public @ResponseBody
  Map<String, Object> deleteTaskBill(HttpServletRequest request, HttpServletResponse response) {

	  Map<String, Object> map = new HashMap<String, Object>();
	  try {

		  int result = 0;                      //执行条目数		  
		  Map mofData = new HashMap();         //总任务单信息
		  Map mofWorkMap = new HashMap();      //总任务单 csof_mof_bill工作流信息
		  Map taskWorkMap = new HashMap();     //监管任务 csof_task_bill工作流信息
	
	      if (null != request.getParameter("mofData")) {
	    	  mofData = JSONObject.parseObject(request.getParameter("mofData").toString());
	      }

	      if (null != request.getParameter("mof_workFlowMap")) {
	    	  mofWorkMap = JSONObject.parseObject(request.getParameter("mof_workFlowMap").toString());
	      }
	      
	      if (null != request.getParameter("task_workFlowMap")) {
	    	  taskWorkMap = JSONObject.parseObject(request.getParameter("task_workFlowMap").toString());
	      }
	      
	      result = billService.deleteTaskBill(mofData, mofWorkMap, taskWorkMap);
	
	      map.put("flag", true);
	      map.put("errorCode", "0");
	      map.put("data", result);
	  } catch (Exception e) {
	      e.printStackTrace();
	      map.put("errorCode", "1");
	      map.put("errorMsg", "获取数据出现异常");
    }

    return map;

  }
  
  
  /**
   * 任务发布
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(method = RequestMethod.POST, value = "/publishTask.do")
  public @ResponseBody
  Map<String, Object> publishTask(HttpServletRequest request, HttpServletResponse response) {
	    Map<String, Object> map = new HashMap<String, Object>();
	    try {
	      String id = request.getParameter("id");
	      Map workMap = new HashMap();
	      if (null != request.getParameter("workFlowMap")) {
	    		workMap = JSONObject.parseObject(request.getParameter("workFlowMap").toString());
	      }
	      int resNum = billService.publishTask(id,workMap);
	      
	      map.put("errorCode", "0");
	      map.put("totalElements", resNum);
	    } catch (Exception e) {
	      e.printStackTrace();
	      map.put("errorCode", "1");
	      map.put("errorMsg", "获取数据出现异常");
	    }
	
	    return map;

  }
  
  /**
   * 撤销发布
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(method = RequestMethod.POST, value = "/undoPublishTask.do")
  public @ResponseBody
  Map<String, Object> unpublishTask(HttpServletRequest request, HttpServletResponse response) {
	    Map<String, Object> map = new HashMap<String, Object>();
	    try {
	    	Map workMap = new HashMap();
	    	String id = request.getParameter("id");
	    	if (null != request.getParameter("workFlowMap")) {
	    		workMap = JSONObject.parseObject(request.getParameter("workFlowMap").toString());
	    	}
	    	if(id.equals("")||workMap.isEmpty()){
	    		map.put("errorCode", "1");
	    		map.put("errorMsg", "请求参数不正确");
	    	}else{
		    	int resNum = billService.unpublishTask(id,workMap);
	    		map.put("errorCode", "0");
	    		map.put("totalElements", resNum);
	    	}

	     } catch (Exception e) {
	    		e.printStackTrace();
	    		map.put("errorCode", "1");
	    		map.put("errorMsg", "获取数据出现异常");
	     }
	
	    	return map;

  }

}
