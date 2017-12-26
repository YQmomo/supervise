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
 * CreateData: 2017-11-22上午9:22:55
 * </p>
 * 
 * @author songlr3
 * @version 1.0
 */
package gov.df.supervise.controller.view;

import gov.df.fap.util.xml.XMLData;
import gov.df.supervise.api.attachment.AttachmentService;
import gov.df.supervise.api.view.CsofViewService;
import gov.df.supervise.bean.view.CsofCViewEntity;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ufgov.ip.apiUtils.UUIDTools;

@Controller
@RequestMapping(value = "/CsofCView")
public class CsofCViewController {

	@Autowired
	private CsofViewService csofViewService;
	
	@Autowired
	private AttachmentService attachmentService; //调用附件组件

	/**
	 * 导入excel模板（单表）
	 * 
	 * @param map
	 * @return
	 * @author songlr3 at 2017-12-5下午1:48:06
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(method = RequestMethod.POST, value = "/saveCsofView.do")
	public @ResponseBody
	Map<String, Object> excel(@RequestBody Object map) {
		Map<String, Object> result = new HashMap<String, Object>();
		String attachId = (String) ((Map<String, Object>) map).get("attachId");
		Map fileData = attachmentService.getFileById(attachId);
		String isTempFile = fileData.get("isTempFile").toString();
		String filePath = fileData.get("localPath").toString();
		String fileName = fileData.get("fileName").toString();
		
	    File localFile = new File(filePath);
		CsofCViewEntity csofCView = new CsofCViewEntity();// 数据表实体
		String viewId = (String) ((Map<String, Object>) map).get("pViewId");
		String updateflag = "0";
		if (viewId == null || "".equals(viewId)) {
			viewId = UUIDTools.uuidRandom();
		} else {
			csofViewService.deleteAllExcel(viewId);
			updateflag = "1";
		}
		String viewCode = fileName.split("_")[0];
		csofCView.setViewId(viewId);
		csofCView.setViewName(fileName);
		csofCView.setViewCode(viewCode);
		csofCView.setObjtypeCode(null);
		csofCView.setObjtypeId(null);
		csofCView.setObjtypeName(null);
		FileInputStream fis = null;

		try {
			// Workbook wookbook = getWorkbook(fis);
			fis = new FileInputStream(localFile);
			Workbook wookbook = WorkbookFactory.create(fis);
			if (wookbook instanceof XSSFWorkbook) {
				XSSFWorkbook xWb = (XSSFWorkbook) wookbook;
				csofViewService
						.getExcelInfoX(xWb, viewId, viewCode, updateflag);
			} else if (wookbook instanceof HSSFWorkbook) {
				HSSFWorkbook hWb = (HSSFWorkbook) wookbook;
				csofViewService
						.getExcelInfoH(hWb, viewId, viewCode, updateflag);
			}
			csofViewService.saveCsofCView(csofCView);
			wookbook.close();
			result.put("errorCode", 0);
			result.put("errorMsg", "保存成功");
			result.put("viewId", viewId);
		} catch (Exception e) {
			result.put("errorCode", -1);
			result.put("errorMsg", "保存失败");
			result.put("message", "异常"); // zfn 未来改成标准消息异常
			e.printStackTrace();
			System.out.println(e.getMessage());
		}finally{
			if(null!=fis){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(isTempFile.equals("1") && localFile.exists()){
					localFile.delete();
				}
			}
		}
		return result;
	}

	/**
	 * 保存excel数据表数据
	 * 
	 * @param request
	 * @param map
	 * @return
	 * @author songlr3 at 2017-12-5下午1:48:41
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(method = RequestMethod.POST, value = "/saveExcel.do")
	public @ResponseBody
	Map<String, Object> saveExcel(HttpServletRequest request,
			@RequestBody Object map) {
		Map<String, Object> result = new HashMap<String, Object>();
		Object mapParam = ((Map<String, Object>) map).get("mapParam");
		String billtypeCode = (String) ((Map<String, Object>) map).get("billtypeCode");
		String billId = (String) ((Map<String, Object>) map).get("billId");
		String objtypeId = (String) ((Map<String, Object>) map).get("objtypeId");
		String objId = (String) ((Map<String, Object>) map).get("objId");
		String supCycle = (String) ((Map<String, Object>) map).get("supCycle");
		String supDate = (String) ((Map<String, Object>) map).get("supDate");
		if (billId.equals("") || billId == null) {
			result.put("errorCode", 1);
			result.put("errorMsg", "业务单据Id不能为空！");
			return result;
		}
		Map<String, List<Map>> mapExcel = (Map<String, List<Map>>) mapParam;
		try {
			csofViewService.saveExcel(mapExcel, billtypeCode, billId,
					objtypeId, objId, supCycle, supDate);
			result.put("errorCode", 0);
			result.put("errorMsg", "保存成功");
		} catch (Exception e) {
			result.put("errorCode", -1);
			result.put("errorMsg", "保存失败");
			result.put("message", "异常"); // zfn 未来改成标准消息异常
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取excel模板格式及数据
	 * 
	 * @param request
	 * @return
	 * @author songlr3 at 2017-12-5下午1:49:22
	 */
	@SuppressWarnings({ "rawtypes" })
	@RequestMapping(method = RequestMethod.POST, value = "/getExcel.do")
	public @ResponseBody
	Map<String, Object> getExcel(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, List<Map>> dataExcel = new HashMap<String, List<Map>>();
		try {
			String viewId = request.getParameter("viewId");
			dataExcel = csofViewService.getExcel(viewId);
			result.put("view", dataExcel);
			result.put("errorCode", 0);
			result.put("errorMsg", "查询成功");
		} catch (Exception e) {
			result.put("errorCode", -1);
			result.put("errorMsg", "查询失败");
			result.put("message", "异常"); // zfn 未来改成标准消息异常
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 获取excel模板格式及数据
	 * 
	 * @param request
	 * @return
	 * @author songlr3 at 2017-12-5下午1:49:22
	 */
	@SuppressWarnings({ "rawtypes" })
	@RequestMapping(method = RequestMethod.POST, value = "/getExcelBySheetId.do")
	public @ResponseBody
	Map<String, Object> getExcelBySheetId(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, List<Map>> dataExcel = new HashMap<String, List<Map>>();
		try {
			String sheetId = request.getParameter("sheetId");
			dataExcel = csofViewService.getExcelBySheetId(sheetId);
			result.put("view", dataExcel);
			result.put("errorCode", 0);
			result.put("errorMsg", "查询成功");
		} catch (Exception e) {
			result.put("errorCode", -1);
			result.put("errorMsg", "查询失败");
			result.put("message", "异常"); // zfn 未来改成标准消息异常
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取数据表数据
	 * 
	 * @param map
	 * @return
	 * @author songlr3 at 2017-12-5下午1:49:58
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(method = RequestMethod.POST, value = "/getData.do")
	public @ResponseBody
	Map<String, Object> getData(@RequestBody Object map) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map> data = new ArrayList<Map>();
		try {
			String billId = (String) ((Map<String, Object>) map).get("billId");
			if (billId.equals("") || billId == null) {
				result.put("errorCode", 1);
				result.put("errorMsg", "业务单据Id不能为空！");
				return result;
			}
			// String billId = (String) map.get("billId");
			// String objId = (String) map.get("objId");
			// String viewId = (String) map.get("viewId");
			// String sheetId = (String) map.get("sheetId");
			// String objtypeId = (String) map.get("objtypeId");
			// String supDate = (String) map.get("supDate");
			// String oid = (String) map.get("oid");
			data = csofViewService.getData((Map<String, Object>)map);
			result.put("data", data);
			result.put("errorCode", 0);
			result.put("errorMsg", "查询成功");
		} catch (Exception e) {
			result.put("errorCode", -1);
			result.put("errorMsg", "查询失败");
			result.put("message", "异常"); // zfn 未来改成标准消息异常
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取保存的所有excel表名及ID
	 * 
	 * @param request
	 * @return
	 * @author songlr3 at 2017-12-5下午1:50:58
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/selectAllExcel.do")
	public @ResponseBody
	Map<String, Object> selectAllExcel(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			List<CsofCViewEntity> data = csofViewService.selectAllExcel();
			result.put("data", data);
			result.put("errorCode", 0);
			result.put("errorMsg", "查询成功");
		} catch (Exception e) {
			result.put("errorCode", -1);
			result.put("errorMsg", "查询失败");
			result.put("message", "异常"); // zfn 未来改成标准消息异常
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 导入excel中的数据
	 * 
	 * @param map
	 * @return
	 * @author songlr3 at 2017-12-5下午1:57:45
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(method = RequestMethod.POST, value = "/saveExcelData.do")
	public @ResponseBody
	Map<String, Object> saveExcelData(HttpServletRequest request,
			@RequestBody Object map) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		Object mapParam = ((Map<String, Object>) map).get("mapParam");
		String attachId = (String) ((Map<String, Object>) mapParam).get("attachId");
		
		Map fileData = attachmentService.getFileById(attachId);
		String isTempFile = fileData.get("isTempFile").toString();
		String filePath = fileData.get("localPath").toString();
		String fileName = fileData.get("fileName").toString();
		
	    File localFile = new File(filePath);
		String viewId = (String) ((Map<String, Object>) mapParam).get("viewId");
		String billtypeCode = (String) ((Map<String, Object>) map).get("billtypeCode");
		String billId = (String) ((Map<String, Object>) map).get("billId");
		String objtypeId = (String) ((Map<String, Object>) map).get("objtypeId");
		String objId = (String) ((Map<String, Object>) map).get("objId");
		String supCycle = (String) ((Map<String, Object>) map).get("supCycle");
		String supDate = (String) ((Map<String, Object>) map).get("supDate");
		if (billId.equals("") || billId == null) {
			result.put("errorCode", 1);
			result.put("errorMsg", "业务单据Id不能为空！");
			return result;
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(localFile);
			Workbook wookbook = WorkbookFactory.create(fis);
			if (wookbook instanceof XSSFWorkbook) {
				XSSFWorkbook xWb = (XSSFWorkbook) wookbook;
				csofViewService.saveExcelDataX(xWb, viewId, billtypeCode,
						billId, objtypeId, objId, supCycle, supDate);
			} else if (wookbook instanceof HSSFWorkbook) {
				HSSFWorkbook hWb = (HSSFWorkbook) wookbook;
				csofViewService.saveExcelDataH(hWb, viewId, billtypeCode,
						billId, objtypeId, objId, supCycle, supDate);
			}
			wookbook.close();
			result.put("errorCode", 0);
			result.put("errorMsg", "保存成功");
		} catch (Exception e) {
			result.put("errorCode", -1);
			result.put("errorMsg", "保存失败");
			result.put("message", "异常"); // zfn 未来改成标准消息异常
			e.printStackTrace();
			System.out.println(e.getMessage());
		}finally{
			if(null!=fis){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(isTempFile.equals("1") && localFile.exists()){
					localFile.delete();
				}
			}
		}
		return result;
	}

}