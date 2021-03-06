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
 * CreateData: 2017-11-15下午8:30:57
 * </p>
 * 
 * @author songlr3
 * @version 1.0
 */
package gov.df.supervise.service.view;

import gov.df.fap.service.util.dao.GeneralDAO;
import gov.df.fap.service.util.sessionmanager.SessionUtil;
import gov.df.fap.util.Tools;
import gov.df.supervise.api.view.CsofViewService;
import gov.df.supervise.bean.view.CsofCViewCellEntity;
import gov.df.supervise.bean.view.CsofCViewEntity;
import gov.df.supervise.bean.view.CsofCViewSheetEntity;
import gov.df.supervise.bean.view.CsofSupDataEntity;
import gov.df.supervise.service.common.SessionUtilEx;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.ufgov.ip.apiUtils.UUIDTools;

@Service
public class CsofViewServiceImpl implements CsofViewService {

	@Autowired
	private CsofCViewDao csofCViewDao;

	@Autowired
	private CsofCViewCellDao csofCViewCellDao;

	@Autowired
	private CsofCViewSheetDao csofCViewSheetDao;

	@Autowired
	private CsofSupDataDao csofSupDataDao;

	@Autowired
	@Qualifier("generalDAO")
	GeneralDAO dao;

	public String getYear() {
		return SessionUtilEx.getLoginYear();
	}

	public String getRgCode() {
		return SessionUtilEx.getRgCode();
	}

	public String getDate() {
		return Tools.getCurrDate();
	}

	public String getUser() {
		return SessionUtil.getUserInfoContext().getUserID();
	}

	public void saveCsofCView(CsofCViewEntity csofCView) {
		csofCViewDao.insertSelective(csofCView);
	}

	public void saveCsofCViewSheet(CsofCViewSheetEntity csofCViewSheet) {
		csofCViewSheet.setCreateDate(getDate());
		csofCViewSheet.setLastVer(getDate());
		csofCViewSheet.setIsValid((short) 1);
		csofCViewSheetDao.insertSelective(csofCViewSheet);
	}

	public void saveCsofCViewCell(CsofCViewCellEntity csofCViewCell) {
		csofCViewCellDao.insertSelective(csofCViewCell);
	}

	public List<CsofCViewCellEntity> selectIsEdit(String sheetId) {
		return csofCViewCellDao.selectIsEditBySheetId(sheetId);
	}

	public void createTable(String dataTable, String sql) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dataTable", dataTable);
		map.put("sql", sql);
		csofCViewCellDao.createTable(map);
	}

	public void alterTable(String dataTable, String sql) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dataTable", dataTable);
		map.put("sql", sql);
		csofCViewCellDao.alterTable(map);
	}

	/**
	 * 保存数据表数据
	 * 
	 * @param mapExcel
	 * @see gov.df.supervise.api.view.CsofViewService#saveExcel(java.util.Map)
	 * @author songlr3 at 2017-12-5下午2:28:39
	 */
	@SuppressWarnings({ "rawtypes" })
	public void saveExcel(Map<String, List<Map>> mapExcel, String billtypeCode,
			String billId, String objtypeId, String objId, String supCycle,
			String supDate) {
		List<Map> excel = new ArrayList<Map>();
		for (String key : mapExcel.keySet()) {
			String sheetId = key;
			String dataId = "";
			String sql = "";
			String nameStr = "";
			CsofSupDataEntity csofSupData = selectCsofSupDataBySheetId(sheetId,
					billId);
			CsofCViewSheetEntity csofCViewSheet = csofCViewSheetDao
					.selectByPrimaryKey(sheetId);
			String dataTable = csofCViewSheet.getDataTable().toUpperCase();
			List<String> ZDNamestr = csofSupDataDao.getZDNamestr(dataTable);// 获取该dataTable表中C开头字段
			for (int s = 0; s < ZDNamestr.size(); s++) {
				nameStr = nameStr + ZDNamestr.get(s);
				if (s + 1 < ZDNamestr.size()) {
					nameStr = nameStr + ",";
				}
			}
			if (csofSupData!= null) {
				dataId = csofSupData.getDataId();
				String delSQL = "delete from " + csofCViewSheet.getDataTable()
						+ " where data_id = '" + dataId + "'";
				dao.executeBySql(delSQL);
			} else {
				csofSupData = new CsofSupDataEntity();
				dataId = UUIDTools.uuidRandom();
				csofSupData.setBillId(billId);
				csofSupData.setBilltypeCode(billtypeCode);
				csofSupData.setObjtypeId(objtypeId);
				csofSupData.setObjId(objId);
				csofSupData.setSupCycle(Short.parseShort(supCycle));
				csofSupData.setSupDate(supDate);
				csofSupData.setDataId(dataId);
				csofSupData.setViewId(csofCViewSheet.getViewId());
				csofSupData.setSheetId(sheetId);
				csofSupData.setDataTable(csofCViewSheet.getDataTable());
				csofSupData.setOid((String) SessionUtil.getUserInfoContext()
						.getOrgCode());
				csofSupData.setCreateDate(getDate());
				csofSupData.setLastVer(getDate());
				csofSupDataDao.insert(csofSupData);
			}
			excel = mapExcel.get(key);
			for (int i = 0; i < excel.size(); i++) {				
				if(excel.get(i).get("SHEET_ID")!=null&&!"".equals(excel.get(i).get("SHEET_ID"))){
					String valueStr = "";

					// if(excel.get(i).get("ROW_ID")==null||"".equals(excel.get(i).get("ROW_ID"))){
					// inster
					// 循环字段获取相应value
					for (int s1 = 0; s1 < ZDNamestr.size(); s1++) {
						String s = "";
						s = (String)excel.get(i).get(ZDNamestr.get(s1));
						if(s==null||"".equals(s)){
							s = "";
						}else{
							s = s.replaceAll(",","");
							s = s.replaceAll(";","");
						}
						valueStr = valueStr	+ s;
						if (s1 + 1 < ZDNamestr.size()) {
							valueStr = valueStr + "','";
						}
					}
					String rowIndex = "";
					if(excel.get(i).get("ROW_INDEX")==null||"".equals(excel.get(i).get("ROW_INDEX"))){
						rowIndex = "0";
					}else{
						rowIndex = (excel.get(i).get("ROW_INDEX")).toString();
					}
//					String rowIndex = (excel.get(i).get("ROW_INDEX")).toString();
//					if("".equals(rowIndex)||rowIndex==null){
//						rowIndex = "0";
//					}
					sql = "insert into " + csofCViewSheet.getDataTable() + " ("
							+ "DATA_ID,LAST_VER,ROW_ID,ROW_INDEX," + nameStr + " )"
							+ " values ('" + dataId + "','" + getDate() + "','"
							+ UUIDTools.uuidRandom() + "','"
							+ rowIndex + "','" + valueStr
							+ "' )";
					// csofSupDataDao.insertSql(sql);
					dao.executeBySql(sql);
					// }
					// else{
					// //update
					// String update = "";
					// for(int s1=0;s1<ZDNamestr.size();s1++){
					// String name = ZDNamestr.get(s1);
					// String value = (String) excel.get(i).get(ZDNamestr.get(s1));
					// update = update + name +"="+value;
					// if(s1+1<ZDNamestr.size()){
					// update = update+",";
					// }
					// }
					// sql = "update "+csofCViewSheet.getDataTable() + " set "+
					// "LAST_VER=" + getDate()+
					// ",ROW_INDEX=" + excel.get(i).get("ROW_INDEX") + "," + update
					// + "where ROW_ID=" + excel.get(i).get("ROW_ID");
					// //csofSupDataDao.updateSql(sql);
					// dao.executeBySql(sql);
					// }
				}				
			}
		}

	}

	/**
	 * 按照sheet页签 逐行 逐格进行解析
	 * 
	 * @param wookbook
	 * @param viewID
	 * @return
	 * @author songlr3 at 2017-11-17下午4:48:59
	 */
	public void getExcel(Workbook wookbook, String viewID, String viewCode,
			String updateflag) {
		Map<String, Object> mapC = new HashMap<String, Object>();// 列
		Map<String, Object> mapR = new HashMap<String, Object>();// 行
		int sheetsNum = wookbook.getNumberOfSheets();
		String viewId = viewID;
		// 循环excel中sheet表
		for (int i = 0; i < sheetsNum; i++) {
			CsofCViewSheetEntity csofCViewSheet = new CsofCViewSheetEntity();
			Sheet sheet = wookbook.getSheetAt(i);
			String sheetName = sheet.getSheetName();
			int lastRowNum = sheet.getLastRowNum();
			Map<String, String> map[] = getRowSpanColSpanMap(sheet);
			Row row = null; // 兼容
			Cell cell = null; // 兼容
			mapC = getmapC(sheet);// {3=zjzf, 2=dwmc, 1=dwbm, 5=hj, 4=sqzf}
			mapR = getmapR(sheet);// {3=3, 2=2, 1=1, 4=4}
			String sheetId = UUIDTools.uuidRandom();
			csofCViewSheet.setViewId(viewId);
			csofCViewSheet.setSheetId(sheetId);
			String sheetCode = sheetName.split("_")[0];
			String sheetname = sheetName.split("_")[1];
			csofCViewSheet.setSheetCode(sheetCode);
			csofCViewSheet.setSheetName(sheetname);
			boolean sheetType = false;
			short sheettype = 0;
			for (int r = 1; r <= mapR.size(); r++) {
				if (((String) mapR.get(String.valueOf(r))).subSequence(0, 1)
						.equals("b")) {
					sheetType = true;
				}
			}
			if (sheetType == true) {
				sheettype = 2;// 列表式
			} else {
				sheettype = 1;// 凭证式
			}
			csofCViewSheet.setSheetType(sheettype);
			csofCViewSheet.setSheetIndex((short) (i + 1));
			String dataTable = "CSOF_D_" + viewCode.toUpperCase() + "_"
					+ sheetCode.toUpperCase();
			csofCViewSheet.setDataTable(dataTable);
			saveCsofCViewSheet(csofCViewSheet);
			// 循环sheet表中行列单元格
			for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) {
				row = sheet.getRow(rowNum);
				if (row != null) {
					int lastColNum = row.getLastCellNum();
					for (int colNum = 1; colNum < lastColNum; colNum++) {
						CsofCViewCellEntity csofCViewCell = new CsofCViewCellEntity();
						cell = row.getCell(colNum);
						if (cell != null) {
							csofCViewCell.setViewId(viewId);
							csofCViewCell.setSheetId(sheetId);
							csofCViewCell.setCellId(UUIDTools.uuidRandom());
							csofCViewCell.setCellCode(cell.getAddress()
									.toString());
							csofCViewCell.setCsofCode("C" + "_"
									+ mapC.get(String.valueOf(colNum)) + "_"
									+ mapR.get(String.valueOf(rowNum)));
							String rowType = mapR.get(String.valueOf(rowNum))
									.toString().substring(0, 1).equals("H") ? "HEAD"
									: (mapR.get(String.valueOf(rowNum))
											.toString().substring(0, 1)
											.equals("F") ? "FOOT" : "BODY");
							csofCViewCell.setRowType(rowType);
							if (!cell.toString().equals("")) {
								if (cell.toString().substring(0, 1).equals("#")) {
									csofCViewCell.setInputType(cell.toString()
											.toUpperCase());
									csofCViewCell.setIsEdit((short) 1);
									csofCViewCell.setCellTxt("-");
								} else {
									csofCViewCell.setCellTxt(cell.toString());
									csofCViewCell.setInputType("-");
									csofCViewCell.setIsEdit((short) 0);
								}
							} else {
								csofCViewCell.setCellTxt("-");
								csofCViewCell.setInputType("-");
								csofCViewCell.setIsEdit((short) 0);
							}

							csofCViewCell.setColIndex((short) colNum);
							csofCViewCell.setRowIndex((short) rowNum);
							if (map[0].containsKey(rowNum + "," + colNum)) {
								String pointString = map[0].get(rowNum + ","
										+ colNum);
								map[0].remove(rowNum + "," + colNum);
								int bottomeRow = Integer.valueOf(pointString
										.split(",")[0]);
								int bottomeCol = Integer.valueOf(pointString
										.split(",")[1]);
								int rowSpan = bottomeRow - rowNum + 1;
								int colSpan = bottomeCol - colNum + 1;
								csofCViewCell.setRowspan((short) rowSpan);
								csofCViewCell.setColspan((short) colSpan);
							} else if (map[1]
									.containsKey(rowNum + "," + colNum)) {
								map[1].remove(rowNum + "," + colNum);
								csofCViewCell.setRowspan((short) 1);
								csofCViewCell.setColspan((short) 1);
							} else {
								csofCViewCell.setRowspan((short) 0);
								csofCViewCell.setColspan((short) 0);
							}
							csofCViewCell.setCellHeight("-");
							csofCViewCell
									.setCellWidth(String.valueOf(cell
											.getSheet().getColumnWidthInPixels(
													colNum)));
							csofCViewCell.setCellStyle("-");
							csofCViewCell.setCellFormula("-");
							csofCViewCell.setIsShow((short) 1);
							if (((String) mapR.get(String.valueOf(rowNum)))
									.substring(0, 1).equals("b")) {
								csofCViewCell.setIsListRow((short) 1);
							} else {
								csofCViewCell.setIsListRow((short) 0);
							}
							if (csofCViewCell.getRowspan() == (short) 0
									&& csofCViewCell.getColspan() == (short) 0) {
								csofCViewCell.setIsSpan((short) 0);
							} else {
								csofCViewCell.setIsSpan((short) 1);
							}
							saveCsofCViewCell(csofCViewCell);
						}
					}
				}
			}
			List<CsofCViewCellEntity> isEditdata = selectIsEdit(sheetId);
			String sql = "";
			if (updateflag.equals("1")) {
				List<String> ZDNamestr = csofSupDataDao.getZDNamestr(dataTable);
				for (int isEdit = 0; isEdit < isEditdata.size(); isEdit++) {
					String inputType = isEditdata.get(isEdit).getInputType();
					String name = isEditdata.get(isEdit).getCsofCode();
					String type = getparam().get(inputType);
					if (!ZDNamestr.contains(name)) {
						sql = name + " " + type;
						alterTable(dataTable, sql);
					}
				}

			} else {
				sql = "(data_id VARCHAR2(64),last_ver VARCHAR2(20),row_id VARCHAR2(64),row_index NUMBER(10)";
				for (int isEdit = 0; isEdit < isEditdata.size(); isEdit++) {
					String inputType = isEditdata.get(isEdit).getInputType();
					String name = isEditdata.get(isEdit).getCsofCode();
					String type = getparam().get(inputType);
					if (isEdit == isEditdata.size() - 1) {
						String e = "," + name + " " + type + ")";
						sql = sql + e;
					} else {
						String m = "," + name + " " + type;
						sql = sql + m;
					}
				}
				createTable(dataTable, sql);
			}
		}
	}

	/**
	 * 通过excel原单元格，确定自带单元格行坐标，如B1:c1:1
	 * 
	 * @param sheet
	 * @return
	 * @author songlr3 at 2017-11-17上午11:12:26
	 */
	private static Map<String, Object> getmapR(Sheet sheet) {
		Map<String, Object> map = new HashMap<String, Object>();
		Row row = null;
		Cell cell = null;
		int colNum = 0;
		int lastRowNum = sheet.getLastRowNum();
		for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) {
			row = sheet.getRow(rowNum);
			cell = row.getCell(colNum);
			String name = cell.toString().toUpperCase();
			// String name =
			// cell.toString().substring(0,cell.toString().indexOf("."));
			map.put(String.valueOf(rowNum), name);
		}
		return map;
	}

	/**
	 * 通过excel原单元格，确定自带单元格列坐标，如B:r1:1
	 * 
	 * @param sheet
	 * @return
	 * @author songlr3 at 2017-11-17上午10:06:34
	 */
	private static Map<String, Object> getmapC(Sheet sheet) {
		Map<String, Object> map = new HashMap<String, Object>();
		Cell cell = null;
		int rowNum = 0;
		Row row = sheet.getRow(rowNum);
		int lastColNum = row.getLastCellNum();
		for (int colNum = 1; colNum < lastColNum; colNum++) {
			cell = row.getCell(colNum);
			String name = cell.toString().toUpperCase();
			map.put(String.valueOf(colNum), name);
		}
		return map;
	}

	/**
	 * 获取sheet表中合并的单元格位置坐标
	 * 
	 * @param sheet
	 * @return
	 * @author songlr3 at 2017-11-28上午9:29:48
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map<String, String>[] getRowSpanColSpanMap(Sheet sheet) {
		Map<String, String> map0 = new HashMap<String, String>();
		Map<String, String> map1 = new HashMap<String, String>();
		int mergedNum = sheet.getNumMergedRegions();
		CellRangeAddress range = null;
		for (int i = 0; i < mergedNum; i++) {
			range = sheet.getMergedRegion(i);
			int topRow = range.getFirstRow();// 第一行
			int topCol = range.getFirstColumn();// 第一列
			int bottomRow = range.getLastRow();// 最后一行
			int bottomCol = range.getLastColumn();// 最后一列
			map0.put(topRow + "," + topCol, bottomRow + "," + bottomCol);
			int tempRow = topRow;
			while (tempRow <= bottomRow) {
				int tempCol = topCol;
				while (tempCol <= bottomCol) {
					map1.put(tempRow + "," + tempCol, "");
					tempCol++;
				}
				tempRow++;
			}
			map1.remove(topRow + "," + topCol);
		}
		Map[] map = { map0, map1 };
		return map;
	}

	/**
	 * 获取相对应的数据类型
	 */
	private static Map<String, String> getparam() {
		Map<String, String> param = new HashMap<String, String>();
		param.put("#S", "VARCHAR2(64)");
		param.put("#T", "VARCHAR2(200)");
		param.put("#M", "NUMBER(16,2)");
		param.put("#N", "NUMBER(10)");
		param.put("#P", "NUMBER(10,1)");
		param.put("#D", "VARCHAR2(20)");
		return param;
	}

	public void getExcelInfoX(XSSFWorkbook xWb, String viewId, String viewCode,
			String updateflag) {
		getExcel(xWb, viewId, viewCode, updateflag);
	}

	public void getExcelInfoH(HSSFWorkbook hWb, String viewId, String viewCode,
			String updateflag) {
		getExcel(hWb, viewId, viewCode, updateflag);
	}

	/**
	 * 获取excel模板数据
	 * 
	 * @param viewId
	 * @return
	 * @see gov.df.supervise.api.view.CsofViewService#getExcel(java.lang.String)
	 * @author songlr3 at 2017-12-5下午2:29:49
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	public Map<String, List<Map>> getExcel(String viewId) {
		Map<String, List<Map>> list = new HashMap<String, List<Map>>();
		List<Map> excelData = csofCViewCellDao.getExcel(viewId);
		List<Map> head = new ArrayList<Map>();
		List<Map> body = new ArrayList<Map>();
		List<Map> foot = new ArrayList<Map>();
		Map<String, List<Map>> d_data = new HashMap<String, List<Map>>();
		for (int i = 0; i < excelData.size(); i++) {
			if (((String) excelData.get(i).get("CSOF_CODE")).split("_")[2]
					.substring(0, 1).equals("H")) {
				head.add(excelData.get(i));
			} else if (((String) excelData.get(i).get("CSOF_CODE")).split("_")[2]
					.substring(0, 1).equals("F")) {
				foot.add(excelData.get(i));
			} else {
				body.add(excelData.get(i));
			}
		}
		list.put("head", head);
		list.put("foot", foot);
		list.put("body", body);
		// List<CsofSupDataEntity> csofSupData =
		// csofSupDataDao.selectByViewId(viewId);
		// if(!csofSupData.isEmpty()){
		// for(int j=0;j<csofSupData.size();j++){
		// String dataTable = csofSupData.get(j).getDataTable();
		// // String sheetId = csofSupData.get(j).getSheetId();
		// String dataId = csofSupData.get(j).getDataId();
		// String sql =
		// "select c.*,d.sheet_id,d.view_id from csof_sup_data d,"+dataTable +
		// " c where 1=1 and c.data_id=d.data_id and d.data_id='"+dataId+"'";
		// // List<Map> data = csofSupDataDao.getExcelData(sql);
		// List<Map> data = dao.findBySql(sql);
		// // d_data.addAll(data);
		// d_data.put("data", data);
		// }
		// }
		// dataExcel.put("view", list);
		// // dataExcel.put("data", d_data);
		return list;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CsofSupDataEntity selectCsofSupDataBySheetId(String sheetId,
			String billId) {
		Map param = new HashMap();
		param.put("sheetId", sheetId);
		param.put("billId", billId);
		CsofSupDataEntity csofSupData = csofSupDataDao
				.selectCsofSupDataBySheetId(param);
		return csofSupData;
	}

	public List<CsofCViewEntity> selectAllExcel() {
		List<CsofCViewEntity> data = csofCViewDao.selectAllExcel();
		return data;
	}

	public void deleteAllExcel(String viewId) {
		csofCViewCellDao.deleteCellByViewId(viewId);
		csofCViewSheetDao.deleteSheetByViewId(viewId);
		csofCViewDao.deleteByPrimaryKey(viewId);
	}

	/**
	 * 获取相应数据表数据
	 * 
	 * @param map
	 * @return
	 * @see gov.df.supervise.api.view.CsofViewService#getData(java.util.Map)
	 * @author songlr3 at 2017-12-5下午2:30:09
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public List<Map> getData(Map map) {
		List<CsofSupDataEntity> csofSupData = csofSupDataDao.selectSupData(map);
		List<Map> d_data = new ArrayList<Map>();
		if (!csofSupData.isEmpty()) {
			for (int j = 0; j < csofSupData.size(); j++) {
				Map param = new HashMap();
				String dataTable = csofSupData.get(j).getDataTable();
				// String sheetId = csofSupData.get(j).getSheetId();
				String dataId = csofSupData.get(j).getDataId();
				String sql = "select c.*,d.sheet_id,d.view_id from csof_sup_data d,"
						+ dataTable
						+ " c where 1=1 and c.data_id=d.data_id and d.data_id='"
						+ dataId + "'";
				param.put("dataTable", dataTable);
				param.put("dataId", dataId);
				List<Map> data = csofSupDataDao.getExcelData(param);
				// List<Map> data = dao.findBySql(sql);
				// d_data.addAll(data);
				d_data.addAll(data);
			}
		}
		return d_data;
	}

	public void saveExcelDataX(XSSFWorkbook xWb, String viewId,
			String billtypeCode, String billId, String objtypeId, String objId,
			String supCycle, String supDate) {
		saveExcelData(xWb, viewId, billtypeCode, billId, objtypeId, objId,
				supCycle, supDate);
	}

	public void saveExcelDataH(HSSFWorkbook hWb, String viewId,
			String billtypeCode, String billId, String objtypeId, String objId,
			String supCycle, String supDate) {
		saveExcelData(hWb, viewId, billtypeCode, billId, objtypeId, objId,
				supCycle, supDate);
	}

	/**
	 * 解析导入excel数据
	 * 
	 * @param wookbook
	 * @param viewID
	 * @author songlr3 at 2017-12-5下午4:07:00
	 * @param supDate
	 * @param supCycle
	 * @param objId
	 * @param objtypeId
	 * @param billId
	 * @param billtypeCode
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void saveExcelData(Workbook wookbook, String viewID,
			String billtypeCode, String billId, String objtypeId, String objId,
			String supCycle, String supDate) {
		int sheetsNum = wookbook.getNumberOfSheets();
		String viewId = viewID;
		Map<String, List<Map>> mapS = new HashMap<String, List<Map>>();
		// 循环excel中sheet表
		for (int i = 0; i < sheetsNum; i++) {
			List<Map> list = new ArrayList<Map>();
			Sheet sheet = wookbook.getSheetAt(i);
			String sheetName = sheet.getSheetName();
			String sheetCode = sheetName.split("_")[0];
			Map param = new HashMap();
			param.put("sheetCode", sheetCode);
			param.put("viewId", viewId);
			CsofCViewSheetEntity csofCViewSheet = csofCViewSheetDao
					.getCsofCViewSheet(param);
			String sheetId = csofCViewSheet.getSheetId();
			List<CsofCViewCellEntity> csofCViewCell = csofCViewCellDao
					.getCsofCViewCell(sheetId);
			Map getHeadFootNum = csofCViewSheetDao.getHeadFootNum(sheetId);
			int headNum = Integer.parseInt(getHeadFootNum.get("HEAD_NUM")
					.toString());
			int footNum = Integer.parseInt(getHeadFootNum.get("FOOT_NUM")
					.toString());
			int lastRowNum = sheet.getLastRowNum() - footNum;// 暂时写着，会改
			int addFootIndex = sheet.getLastRowNum()-headNum-footNum-1;
			Row row = null; // 兼容
			Cell cell = null; // 兼容					
			String viewType = "2";//viewType为标识，1为变长表、2为单据表
			for(int t = 0; t<csofCViewCell.size();t++){
				if(csofCViewCell.get(t).getRowType().equals("BODY")){
					viewType = "1";
				}
			}
			// 循环sheet表中行列单元格	
			if("1".equals(viewType)){
				for (int rowNum = 1 + headNum; rowNum <= lastRowNum; rowNum++) {// 此处rowNum的+2也是暂时写着，会改
					Map map = new HashMap();
					row = sheet.getRow(rowNum);
					if (row != null) {
						for (int code = 0; code < csofCViewCell.size(); code++) {
							if(csofCViewCell.get(code).getRowType().equals("BODY")){
								if(csofCViewCell.get(code).getInputType().equals("#D")){
									cell = row.getCell(csofCViewCell.get(code).getColIndex());
									Date d = cell.getDateCellValue();
									DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
									String date = formater.format(d);
									map.put(csofCViewCell.get(code).getCsofCode(),date);
								}else{
									cell = row.getCell(csofCViewCell.get(code)
											.getColIndex());
									map.put(csofCViewCell.get(code).getCsofCode(),
											cell.toString());
								}							
							}else if(csofCViewCell.get(code).getRowType().equals("HEAD")){
								if(csofCViewCell.get(code).getInputType().equals("#D")){
									cell = sheet.getRow(csofCViewCell.get(code).getRowIndex()).getCell(csofCViewCell.get(code).getColIndex());
									Date d = cell.getDateCellValue();
									DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
									String date = formater.format(d);
									map.put(csofCViewCell.get(code).getCsofCode(),date);
								}else{
									cell = sheet.getRow(csofCViewCell.get(code).getRowIndex()).getCell(csofCViewCell.get(code)
											.getColIndex());
									map.put(csofCViewCell.get(code).getCsofCode(),
											cell.toString());
								}							
							}else if(csofCViewCell.get(code).getRowType().equals("FOOT")){
								if(csofCViewCell.get(code).getInputType().equals("#D")){
									cell = sheet.getRow(csofCViewCell.get(code).getRowIndex()+addFootIndex).getCell(csofCViewCell.get(code).getColIndex());
									Date d = cell.getDateCellValue();
									DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
									String date = formater.format(d);
									map.put(csofCViewCell.get(code).getCsofCode(),date);
								}else{
									cell = sheet.getRow(csofCViewCell.get(code).getRowIndex()+addFootIndex).getCell(csofCViewCell.get(code)
											.getColIndex());
									map.put(csofCViewCell.get(code).getCsofCode(),
											cell.toString());
								}							
							}
						}
					}
					map.put("ROW_ID", "");
					map.put("DATA_ID", "");
					map.put("ROW_INDEX", rowNum);
					map.put("LAST_VER", getDate());
					map.put("SHEET_ID", sheetId);
					list.add(map);
				}
			}else if("2".equals(viewType)){
				Map map = new HashMap();				
				for (int code = 0; code < csofCViewCell.size(); code++) {
					if(csofCViewCell.get(code).getRowType().equals("HEAD")){
						if(csofCViewCell.get(code).getInputType().equals("#D")){
							cell = sheet.getRow(csofCViewCell.get(code).getRowIndex()).getCell(csofCViewCell.get(code).getColIndex());
							Date d = cell.getDateCellValue();
							DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
							String date = formater.format(d);
							map.put(csofCViewCell.get(code).getCsofCode(),date);
						}else{
							cell = sheet.getRow(csofCViewCell.get(code).getRowIndex()).getCell(csofCViewCell.get(code)
									.getColIndex());
							map.put(csofCViewCell.get(code).getCsofCode(),
									cell.toString());
						}							
					}else if(csofCViewCell.get(code).getRowType().equals("FOOT")){
						if(csofCViewCell.get(code).getInputType().equals("#D")){
							cell = sheet.getRow(csofCViewCell.get(code).getRowIndex()+addFootIndex).getCell(csofCViewCell.get(code).getColIndex());
							Date d = cell.getDateCellValue();
							DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
							String date = formater.format(d);
							map.put(csofCViewCell.get(code).getCsofCode(),date);
						}else{
							cell = sheet.getRow(csofCViewCell.get(code).getRowIndex()+addFootIndex).getCell(csofCViewCell.get(code)
									.getColIndex());
							map.put(csofCViewCell.get(code).getCsofCode(),
									cell.toString());
						}							
					}
				}				
				map.put("ROW_ID", "");
				map.put("DATA_ID", "");
				map.put("ROW_INDEX", 0);
				map.put("LAST_VER", getDate());
				map.put("SHEET_ID", sheetId);
				list.add(map);
			}
			mapS.put(sheetId, list);
		}
		saveExcel(mapS, billtypeCode, billId, objtypeId, objId, supCycle,
				supDate);// 调用上面保存数据方法
	}

	/**
	 * 获取excel模板数据 by sheetId
	 * 
	 * @param sheetId
	 * @return
	 * @see gov.df.supervise.api.view.CsofViewService#getExcelBySheetId(java.lang.String)
	 * @author songlr3 at 2017-12-8上午4:05:28
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	public Map<String, List<Map>> getExcelBySheetId(String sheetId) {
		Map<String, List<Map>> list = new HashMap<String, List<Map>>();
		List<Map> excelData = csofCViewCellDao.getExcelBySheetId(sheetId);
		List<Map> head = new ArrayList<Map>();
		List<Map> body = new ArrayList<Map>();
		List<Map> foot = new ArrayList<Map>();
		Map<String, List<Map>> d_data = new HashMap<String, List<Map>>();
		for (int i = 0; i < excelData.size(); i++) {
			if (((String) excelData.get(i).get("CSOF_CODE")).split("_")[2]
					.substring(0, 1).equals("H")) {
				head.add(excelData.get(i));
			} else if (((String) excelData.get(i).get("CSOF_CODE")).split("_")[2]
					.substring(0, 1).equals("F")) {
				foot.add(excelData.get(i));
			} else {
				body.add(excelData.get(i));
			}
		}
		list.put("head", head);
		list.put("foot", foot);
		list.put("body", body);
		return list;
	}

}
