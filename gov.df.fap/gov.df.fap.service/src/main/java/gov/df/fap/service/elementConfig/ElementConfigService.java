package gov.df.fap.service.elementConfig;

import gov.df.fap.api.elementConfig.IElementConfigService;
import gov.df.fap.api.paging.IPagingUtilService;
import gov.df.fap.service.util.UUIDRandom;
import gov.df.fap.service.util.dao.GeneralDAO;
import gov.df.fap.service.util.datasource.SQLUtil;
import gov.df.fap.service.util.datasource.TypeOfDB;
import gov.df.fap.service.util.sessionmanager.SessionUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 要素设置
 * @author hp
 * @version 2017-4
 */
@Service
public class ElementConfigService implements IElementConfigService {

  @Autowired
  @Qualifier("generalDAO")
  private GeneralDAO generalDAO;

  @Autowired
  private IPagingUtilService iPagingUtilService;

  /**
   * 初始化 表格信息加载
   */
  public Map<String, Object> init(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String sql = "select * from sys_element where rg_code = ? and set_year = ? order by ele_code";
    List list = generalDAO.findBySql(sql, new String[] { rg_code, year });
    map.put("eledetail", list);
    return map;
  }

  /**
   * 查询表格信息加载
   */
  public Map<String, Object> sysGrid(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String sys_id = request.getParameter("sys_id");
    String andsql = "";
    if ("000".equals(sys_id)) {
      andsql = " and (sys_id = ? or sys_id is null) ";
    } else {
      andsql = " and sys_id = ? ";
    }
    String sql = "select * from sys_element where rg_code = ? and set_year = ? " + andsql + " order by ele_code";
    List list = generalDAO.findBySql(sql, new String[] { rg_code, year, sys_id });
    map.put("eledetail", list);
    return map;
  }

  /**
   * 录入视图树
   */
  public Map<String, Object> entertree(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String sql = null;
    if(TypeOfDB.isOracle()) {
		sql = "select t.ui_id  chr_id , t.ui_code chr_code , t.ui_code||'  '||t.ui_name as codename,t.ui_name chr_name ,'1' parent_id　from sys_uimanager t "
    					+ "where t.ui_type = '001' and t.sys_id = '001' and t.rg_code = ? and t.set_year = ?  order by ui_code ";
    } else if(TypeOfDB.isMySQL()) {
    	sql = "select t.ui_id  chr_id , t.ui_code chr_code , concat(t.ui_code, '  ', t.ui_name) codename,t.ui_name chr_name ,'1' parent_id from sys_uimanager t where t.ui_type = '001' and t.sys_id = '001' and t.rg_code = ? and t.set_year = ?  order by ui_code";
    }
    List list = generalDAO.findBySql(sql, new String[] { rg_code, year });
    Map<String, Object> map1 = new HashMap<String, Object>();
    map1.put("chr_id", "1");
    map1.put("codename", "全部");
    map1.put("parent_id", "0");
    list.add(map1);
    map.put("entertree", list);
    return map;
  }

  /**
   * 视图数据查询
   */
  public Map<String, Object> queryenter(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String enter_code = request.getParameter("enter_code");
    String list_code = request.getParameter("list_code");
    String sql = null;
    if(TypeOfDB.isOracle()) {
    	sql = "select t.ui_id  chr_id , t.ui_code chr_code , t.ui_code||'  '||t.ui_name codename,t.ui_name chr_name ,'1' parent_id from sys_uimanager t "
    			+ "where t.ui_type = ? and t.sys_id = '001' and t.rg_code = ? and t.set_year = ? and ui_code = ? order by ui_code ";
    } else if(TypeOfDB.isMySQL()) {
    	sql = "select t.ui_id  chr_id , t.ui_code chr_code , concat(t.ui_code, '  ', t.ui_name) codename,t.ui_name chr_name ,'1' parent_id from sys_uimanager t "
    			+ "where t.ui_type = ? and t.sys_id = '001' and t.rg_code = ? and t.set_year = ? and ui_code = ? order by ui_code ";
    }
    
    List list1 = generalDAO.findBySql(sql, new String[] { "001", rg_code, year, enter_code });
    map.put("enter", list1);
    List list2 = generalDAO.findBySql(sql, new String[] { "002", rg_code, year, list_code });
    map.put("list", list2);
    return map;
  }

  /**
   * 列表视图树
   */
  public Map<String, Object> listtree(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String sql = null;
    if(TypeOfDB.isOracle()) {
    	sql = "select t.ui_id  chr_id , t.ui_code chr_code , t.ui_code||'  '||t.ui_name codename,t.ui_name chr_name ,'1' parent_id　　from sys_uimanager t "
    			+ "where t.ui_type = '002' and t.sys_id = '001' and t.rg_code = ? and t.set_year = ? order by ui_code";
    } else if(TypeOfDB.isMySQL()) {
    	sql = "select t.ui_id  chr_id , t.ui_code chr_code , concat(t.ui_code, '  ', t.ui_name) codename,t.ui_name chr_name ,'1' parent_id from sys_uimanager t "
    			+ "where t.ui_type = '002' and t.sys_id = '001' and t.rg_code = ? and t.set_year = ? order by ui_code";
    }
    List list = generalDAO.findBySql(sql, new String[] { rg_code, year });
    Map<String, Object> map1 = new HashMap<String, Object>();
    map1.put("chr_id", "1");
    map1.put("codename", "全部");
    map1.put("parent_id", "0");
    list.add(map1);
    map.put("listtree", list);
    return map;
  }

  /**
   * 删除基础数据  eleid基础数据id
   */
  public Map<String, Object> deleteEle(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String eleid = request.getParameter("eleid");
    String sql = null;
    if(TypeOfDB.isOracle()) {
    	sql = "delete sys_element t where t.chr_id = ?";
    } else if(TypeOfDB.isMySQL()) {
    	sql = "delete from sys_element where chr_id = ?";
    }
    int num = generalDAO.executeBySql(sql, new String[] { eleid });
    map.put("num", num);
    return map;
  }

  /**
   * 插入基础数据  eleid基础数据id
   */
  public Map<String, Object> insertEle(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String returnId = UUIDRandom.generate();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String ele_source = request.getParameter("ele_source");
    String ele_code = request.getParameter("ele_code");
    String ele_name = request.getParameter("ele_name");
    String ref_mode = request.getParameter("ref_mode");
    String czgb_code = request.getParameter("czgb_code");
    String level_name = request.getParameter("level_name");
    String sql = null;
    if(TypeOfDB.isOracle()) {
    	sql = "insert into sys_element(chr_id,set_year,ele_source,ele_code,ele_name,ref_mode,czgb_code,level_name,is_operate,rg_code,LATEST_OP_DATE)values(?,?,upper(?),upper(?),?,?,upper(?),?,?,?,to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'))";
    } else if(TypeOfDB.isMySQL()) {
    	sql = "insert into sys_element(chr_id,set_year,ele_source,ele_code,ele_name,ref_mode,czgb_code,level_name,is_operate,rg_code,LATEST_OP_DATE)values(?,?,upper(?),upper(?),?,?,upper(?),?,?,?,date_format(now(),'%Y-%m-%d %H:%i:%s'))";
    	
    }
    int num = generalDAO.executeBySql(sql, new String[] { returnId, year, ele_source, ele_code, ele_name, ref_mode,
      czgb_code, level_name, "1", rg_code });
    map.put("num", num);
    return map;
  }

  /**
   * 更新基础数据
   */
  public Map<String, Object> updateEle(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String ele_source = request.getParameter("ele_source");
    String ele_code = request.getParameter("ele_code");
    String ele_name = request.getParameter("ele_name");
    String ref_mode = request.getParameter("ref_mode");
    String czgb_code = request.getParameter("czgb_code");
    String level_name = request.getParameter("level_name");
    String chr_id = request.getParameter("chr_id");
    String sql = "update sys_element t set t.ele_source = upper(?) ,t.ele_code=upper(?),t.ele_name=?,t.ref_mode=?,t.czgb_code = ? ,t.level_name=? where t.chr_id = ?";
    int num = generalDAO.executeBySql(sql, new String[] { ele_source, ele_code, ele_name, ref_mode, czgb_code,
      level_name, chr_id });
    map.put("num", num);
    return map;
  }

  /**
   * 更新级次信息
   */
  public Map<String, Object> updateEleCode(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String max_level = request.getParameter("max_level");
    String code_rule = request.getParameter("code_rule");
    String chr_id = request.getParameter("chr_id");
    String sql = "update sys_element t set t.max_level=?,t.code_rule=? where t.chr_id = ?";
    int num = generalDAO.executeBySql(sql, new String[] { max_level, code_rule, chr_id });
    map.put("num", num);
    return map;
  }

  /**
   * 初始化数据元树
   */
  public Map<String, Object> dataElementTree(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String elesql = " select t.ele_id id , t.sys_id pid ," + SQLUtil.replaceLinkChar("t.ele_code||' '||t.ele_name") + " as name ,t.ele_name, t.ele_code code , t.ele_colname ,"
      + " t.ele_type,t.ele_format ,t.parameter , t.paratype , t.default_value from SYS_ELEMENT_COLUMN t where t.rg_code = ? and t.set_year = ?  order by t.ele_code ";
    List list = generalDAO.findBySql(elesql, new Object[] { rg_code, year });
    String syssql = " select t.sys_id id , '0' pid , " + SQLUtil.replaceLinkChar("t.sys_id||' '||t.sys_name") + " as name from sys_app t  where t.enabled = '1' order by t.sys_id ";
    List list2 = generalDAO.findBySql(syssql, new Object[] {});
    list.addAll(list2);
    map.put("dataDetail", list);
    String sql = "select t.sys_id , " + SQLUtil.replaceLinkChar("t.sys_id || ' ' || t.sys_name") + " as sys_name, '0' pid from sys_app t order by t.sys_id ";
    List list3 = generalDAO.findBySql(sql, new Object[] {});
    map.put("selectDetail", list3);
    return map;
  }

  /**
   * 初始化数据元树刨除默认数据元
   */
  public Map<String, Object> dataElementTree1(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String elesql = " select t.ele_id id , t.sys_id pid ," + SQLUtil.replaceLinkChar("t.ele_code||' '||t.ele_name") + " as name ,t.ele_name, t.ele_code code , t.ele_colname ,"
      + " t.ele_type,t.ele_format ,t.parameter , t.paratype , t.default_value from SYS_ELEMENT_COLUMN t where t.is_sysdefault <> '1' and t.rg_code = ? and t.set_year = ?  order by t.ele_code ";
    List list = generalDAO.findBySql(elesql, new Object[] { rg_code, year });
    String syssql = " select t.sys_id id , '0' pid , " + SQLUtil.replaceLinkChar("t.sys_id||' '||t.sys_name") + " as name from sys_app t  where t.enabled = '1' order by t.sys_id ";
    List list2 = generalDAO.findBySql(syssql, new Object[] {});
    list.addAll(list2);
    map.put("dataDetail", list);
    String sql = "select t.sys_id , " + SQLUtil.replaceLinkChar("t.sys_id || ' ' || t.sys_name") + " as sys_name, '0' pid from sys_app t order by t.sys_id ";
    List list3 = generalDAO.findBySql(sql, new Object[] {});
    map.put("selectDetail", list3);
    return map;
  }

  /**
   * 数据元表个数据
   */
  public Map<String, Object> eleGridQuery(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String sys_id = request.getParameter("sys_id");
    String sql = " select * from SYS_ELEMENT_COLUMN t where t.sys_id = ? and t.rg_code = ? and t.set_year = ? order by t.ele_code ";
    List list = generalDAO.findBySql(sql, new Object[] { sys_id, rg_code, year });
    map.put("dataDetail", list);
    return map;
  }

  /**
   * 插入数据元树
   */
  public Map<String, Object> insertEleData(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String returnId = UUIDRandom.generate();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String ele_code = request.getParameter("ele_code");
    String ele_name = request.getParameter("ele_name");
    String ele_colname = request.getParameter("ele_colname");
    String ele_type = request.getParameter("ele_type");
    String ele_format = request.getParameter("ele_format");
    String parameter = request.getParameter("parameter");
    String paratype = request.getParameter("paratype");
    String default_value = request.getParameter("default_value");
    String sys_id = request.getParameter("sys_id");
    String insertsql = "insert into SYS_ELEMENT_COLUMN (ele_id,ELE_CODE,ELE_NAME,ELE_COLNAME,ELE_TYPE,ELE_FORMAT,RG_CODE,SET_YEAR,SYS_ID,PARAMETER,PARATYPE,CREATTIME,DEFAULT_VALUE) "
      + " values (?,upper(?),?,upper(?),?,?,?,?,?,?,?,to_char(sysdate , 'yyyymmddhh24miss'),?)";
    int i = generalDAO.executeBySql(insertsql, new Object[] { returnId, ele_code, ele_name, ele_colname, ele_type,
      ele_format, rg_code, year, sys_id, parameter, paratype, default_value });
    map.put("eleid", returnId);
    map.put("flag", "1");
    return map;
  }

  /**
   * 更新数据元数据
   */
  public Map<String, Object> updateEleData(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String returnId = request.getParameter("ele_id");
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String ele_code = request.getParameter("ele_code");
    String old_code = request.getParameter("old_code");
    String ele_name = request.getParameter("ele_name");
    String ele_colname = request.getParameter("ele_colname");
    String ele_type = request.getParameter("ele_type");
    String ele_format = request.getParameter("ele_format");
    String parameter = request.getParameter("parameter");
    String paratype = request.getParameter("paratype");
    String default_value = request.getParameter("default_value");
    String sys_id = request.getParameter("sys_id");
    String delsql = " delete SYS_ELEMENT_COLUMN where ele_id = ? ";
    if (!ele_code.equals(old_code)) {
      String sql2 = " select 1 from SYS_ELEMENT_COLUMN where ele_code = ? and rg_code = ? and set_year = ? ";
      List list = generalDAO.findBySql(sql2, new Object[] { ele_code, rg_code, year });
      if (list.size() > 0) {
        map.put("flag", "0");
        map.put("message", "编码已存在");
        return map;
      }
    }
    int k = generalDAO.executeBySql(delsql, new Object[] { returnId });
    String insertsql = "insert into SYS_ELEMENT_COLUMN (ele_id,ELE_CODE,ELE_NAME,ELE_COLNAME,ELE_TYPE,ELE_FORMAT,RG_CODE,SET_YEAR,SYS_ID,PARAMETER,PARATYPE,CREATTIME,DEFAULT_VALUE) "
      + " values (?,?,?,?,?,?,?,?,?,?,?,to_char(sysdate , 'yyyymmddhh24miss'),?)";
    int i = generalDAO.executeBySql(insertsql, new Object[] { returnId, ele_code, ele_name, ele_colname, ele_type,
      ele_format, rg_code, year, sys_id, parameter, paratype, default_value });
    String upsql = "update sys_tablemanager_filed t set  t.field_code , t.field_name = ? ,t.field_type=?,t.para_type=?,t.parameter = ? ,t.default_value =? ,t.element_code = ?  where t.ele_id = ? ";
    int z = generalDAO.executeBySql(insertsql, new Object[] { ele_colname, ele_name, ele_type, paratype, parameter,
      default_value, ele_code, returnId });

    map.put("eleid", returnId);
    map.put("flag", "1");
    return map;
  }

  /**
   * 删除数据元数据
   */
  public Map<String, Object> delEleData(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    try {
      String returnId = request.getParameter("selid");
      if (returnId == null || "".equals(returnId)) {
        map.put("flag", 0);
        return map;
      }
      String[] ele_id = returnId.split(",");
      int k = 0;
      for (int z = 0; z < ele_id.length; z++) {
        String delsql = " delete SYS_ELEMENT_COLUMN where ele_id = ? ";
        generalDAO.executeBySql(delsql, new Object[] { ele_id[z] });
        k++;
      }
      map.put("num", k);
      map.put("flag", 1);
    } catch (Exception e) {
      map.put("flag", 0);
      e.printStackTrace();
    }
    return map;
  }

  public Map<String, Object> queryFormData(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String returnId = request.getParameter("ele_id");
    String sql = " select * from SYS_ELEMENT_COLUMN t where t.ele_id = ? ";
    List list = generalDAO.findBySql(sql, new Object[] { returnId });
    map.put("dataDetail", list);
    return map;
  }

  /**
   * 基础数据树
   */
  public Map<String, Object> getElementDetailTree(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String elesql = " select t.chr_id id , t.ele_code , " + SQLUtil.replaceLinkChar("t.ele_code || ' ' || t.ele_name") + " as name , " + SQLUtil.replaceNvl("nvl") + "(t.sys_id,'000') as pid , is_view ,is_operate from sys_element t where t.set_year = ? and t.rg_code = ? order by t.ele_code ";
    List list = generalDAO.findBySql(elesql, new Object[] { year, rg_code });
    String syssql = " select t.sys_id id , '0' pid , " + SQLUtil.replaceLinkChar("t.sys_id||' '||t.sys_name") + " as name from sys_app t  where t.enabled = '1' order by t.sys_id ";
    List list2 = generalDAO.findBySql(syssql, new Object[] {});
    Map map2 = new HashMap();
    map2.put("id", "0");
    map2.put("name", "全部");
    map2.put("pid", "");
    list2.add(map2);
    list.addAll(list2);
    map.put("dataDetail", list);
    String sql = "select t.sys_id , " + SQLUtil.replaceLinkChar("t.sys_id || ' ' || t.sys_name") + " as sys_name, '0' pid from sys_app t order by t.sys_id ";
    List list3 = generalDAO.findBySql(sql, new Object[] {});
    map.put("selectDetail", list3);
    return map;
  }

  /**
   * 基础数据要素树
   */
  public Map<String, Object> getElementSourceTree(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String ele_code = request.getParameter("ele_code");
    String elesql = " select  t.ele_source source from sys_element t where t.ele_code = ?  and t.set_year = ? and t.rg_code =?  ";
    List list = generalDAO.findBySql(elesql, new Object[] { ele_code, year, rg_code });
    String tablename = "";
    if (list.size() > 0) {
      Map map1 = (Map) list.get(0);
      tablename = (String) map1.get("source");
    }
    String sourcesql = " select  a.chr_id id, "+SQLUtil.replaceNvl("nvl")+"(a.parent_id,'0') as pid , " + SQLUtil.replaceLinkChar("a.chr_code || ' ' || a.chr_name") + " as name , a.chr_code  from  "
      + tablename + "  a where a.set_year = ? and a.rg_code = ? order by a.chr_code ";
    List list1 = generalDAO.findBySql(sourcesql, new Object[] { year, rg_code });
    Map map2 = new HashMap();
    map2.put("id", "0");
    map2.put("name", "全部");
    map2.put("pid", "");
    list1.add(map2);
    map.put("treeData", list1);
    map.put("tablename", tablename);
    return map;
  }

  /**
   * 获取相应子系统基础要素
   */
  public Map<String, Object> getElementDetailGrid(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String sys_id = request.getParameter("sys_id");
    String sourceSql = " select  t.* from sys_element t where t.sys_id = ?  and t.set_year = ? and t.rg_code =?  ";
    List list1 = generalDAO.findBySql(sourceSql, new Object[] { sys_id, year, rg_code });
    map.put("gridData", list1);
    return map;
  }

  /**
   * 基础要素表数据
   */
  public Map<String, Object> getElementSourceGrid(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String tablename = request.getParameter("tablename");
    String chr_id = request.getParameter("chr_id");
    String type = request.getParameter("type");
    String sourceSql = " select a.* from " + tablename + " a where a.set_year = ? and a.rg_code = ? ";
    if ("1".equals(type)) {
      sourceSql = sourceSql + " start with a.chr_id = ? connect by prior a.chr_id = a.parent_id ";
    }
    sourceSql = sourceSql + " order by a.chr_code ";
    Object[] para;
    if ("1".equals(type)) {
      para = new Object[] { year, rg_code, chr_id };
    } else {
      para = new Object[] { year, rg_code };
    }
    //    List list1 = generalDAO.findBySql(sourceSql, para);
    try {
      map = iPagingUtilService.PagingCommon(sourceSql, para, request);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return map;
  }

  /**
   * 基础要素表列信息
   */
  public Map<String, Object> getElementColumn(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String tablename = request.getParameter("tablename");
    String sql = null;
    if(TypeOfDB.isOracle()){
      sql = " select lower(t.COLUMN_NAME) ele_code , nvl(a.ele_name,t.COLUMN_NAME) ele_name , nvl(a.paratype,'1') paratype ,a.parameter,a.is_show from "
        + "user_tab_columns t , SYS_ELEMENT_COLUMN a where t.TABLE_NAME = upper(?) and a.ele_colname(+) = t.COLUMN_NAME "
        + " and a.rg_code(+)=? and a.set_year(+) = ? order by t.COLUMN_ID ";
    }else if(TypeOfDB.isMySQL()){
      sql = "select LOWER(T.COLUMN_NAME) ELE_CODE, ifnull(A.ELE_NAME, T.COLUMN_NAME) ELE_NAME, ifnull(A.PARATYPE, '1') PARATYPE, A.PARAMETER, A.IS_SHOW from Information_schema.columns T " + 
        "left join SYS_ELEMENT_COLUMN A on A.ELE_COLNAME = T.COLUMN_NAME where T.TABLE_NAME = UPPER(?) and A.RG_CODE = ?  and A.SET_YEAR = ? and t.TABLE_SCHEMA in( select database()) order by T.ORDINAL_POSITION";
    }
    List list1 = generalDAO.findBySql(sql, new Object[] { tablename, rg_code, year });
    map.put("dataDetail", list1);
    return map;
  }

  /**
   * 新增要素
   */
  public Map<String, Object> insertElementColumn(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String chr_id = UUIDRandom.generate();
    String id = request.getParameter("id");
    String ele_code = request.getParameter("ele_code");
    String ele_source = request.getParameter("ele_source");
    String ele_name = request.getParameter("ele_name");
    String czgb_code = request.getParameter("czgb_code");
    String enabled = request.getParameter("enabled");
    String dataRight = request.getParameter("is_rightfilter");
    String is_operate = request.getParameter("is_operate");
    String source_type = request.getParameter("source_type");
    String source_column = request.getParameter("source_column");
    String sys_id = request.getParameter("sys_id");
    String is_view = request.getParameter("is_view");
    try {
      String testSql = "select 1 from " + ele_source;
      generalDAO.findBySql(testSql);
      map.put("flag", "0");
      map.put("Message", "该表已存在");
      return map;
    } catch (Exception e) {
      System.out.println("表不存在可创建");
    }
    String testSql = " select 1 from sys_element t where t.rg_code = ? and t.set_year = ? and t.rg_code = ? ";
    List elelist = generalDAO.findBySql(testSql, new Object[] { rg_code, year, ele_code });
    if (elelist.size() > 0) {
      map.put("flag", "0");
      map.put("Message", "编码已存在");
      return map;
    }
    String filedSql = "insert into SYS_TABLEMANAGER_FILED (guid,CHR_ID,ELE_CODE,TABLE_NAME,FIELD_CODE,FIELD_SIZE,FIELD_NAME,FIELD_TYPE,PARA_TYPE,"
      + "PARAMETER,DEFAULT_VALUE,ENABLE_NULL,EDIT_SHOW,SYS_ID,RG_CODE,SET_YEAR,IS_SYSDEFAULT,ELE_ID,ELEMENT_CODE,CREATEDATE )"
      + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'))";
    String tableCreate = "create table " + ele_source + " ( ";
    int filedcount = 0;
    String colsql = " select * from SYS_ELEMENT_COLUMN t where t.rg_code = ? and t.set_year = ? and t.IS_SYSDEFAULT = '1' order by ele_code";
    List<Map> deflist = generalDAO.findBySql(colsql, new Object[] { rg_code, year });
    for (Map defMap : deflist) {
      String nullsql = "";
      String defsql = "";
      String typesql = "";
      String guid = UUIDRandom.generate();
      String nullType = (String) defMap.get("is_null");
      String code = (String) defMap.get("ele_code");
      String ele_colname = (String) defMap.get("ele_colname");
      String dataType = (String) defMap.get("is_show");
      String ele_type = (String) defMap.get("ele_type");
      String ele_format = (String) defMap.get("ele_format");
      String parameter = (String) defMap.get("parameter");
      String paratype = (String) defMap.get("paratype");
      String name = (String) defMap.get("ele_name");
      String ele_id = (String) defMap.get("ele_id");
      String default_value = (String) defMap.get("default_value");
      if ("0".equals(nullType)) {
        nullsql = " not null ";
      }
      if (!"".equals(default_value) && default_value != null) {
        defsql = "default " + default_value;
      }
      if ("1".equals(ele_type)) {
        typesql = "VARCHAR2(" + ele_format + ")";
      } else if ("2".equals(ele_type)) {
        typesql = "NUMBER(" + ele_format + ")";
      } else if ("3".equals(ele_type)) {
        typesql = "DATE";
      } else if ("4".equals(ele_type)) {
        typesql = "NUMBER(" + ele_format + ",2)";
      }
      tableCreate = tableCreate + ele_colname + " " + typesql + " " + defsql + " " + nullsql + " ,";
      generalDAO.executeBySql(filedSql, new Object[] { guid, chr_id, ele_code, ele_source, ele_colname, ele_format,
        name, ele_type, paratype, parameter, default_value, nullType, dataType, sys_id, rg_code, year, "1", ele_id,
        code });
      filedcount++;
    }
    if (!"".equals(source_column) && source_column != null) {
      JSONObject jsonObject = JSONObject.fromObject(source_column);
      Map<String, Object> map1 = (Map<String, Object>) jsonObject;
      Iterator<String> iter = map1.keySet().iterator();
      while (iter.hasNext()) {
        String nullsql = "";
        String defsql = "";
        String typesql = "";
        String key = iter.next();
        Map value = (Map) map1.get(key);
        String guid = UUIDRandom.generate();
        String nullType = (String) value.get("btn").toString();
        String dataType = (String) value.get("data").toString();
        String ele_type = (String) value.get("ele_type").toString();
        String ele_format = (String) value.get("ele_format").toString();
        String parameter = (String) value.get("parameter").toString();
        if ("null".equals(parameter)) {
          parameter = "";
        }
        String paratype = (String) value.get("paratype").toString();
        String name = (String) value.get("ele_name").toString();
        String code = (String) value.get("ele_code").toString();
        String ele_id = (String) value.get("ele_id").toString();
        String default_value = (String) value.get("default_value").toString();
        if ("null".equals(default_value)) {
          default_value = "";
        }
        String ele_colname = (String) value.get("ele_colname").toString();
        if ("0".equals(nullType)) {
          nullsql = " not null ";
        }
        if (!"".equals(default_value) && default_value != "null") {
          defsql = "default " + default_value;
        }
        if ("1".equals(ele_type)) {
          typesql = "VARCHAR2(" + ele_format + ")";
        } else if ("2".equals(ele_type)) {
          typesql = "NUMBER(" + ele_format + ")";
        } else if ("3".equals(ele_type)) {
          typesql = "DATE";
        } else if ("4".equals(ele_type)) {
          typesql = "NUMBER(" + ele_format + ",2)";
        }
        tableCreate = tableCreate + ele_colname + " " + typesql + " " + defsql + " " + nullsql + " ,";
        generalDAO.executeBySql(filedSql, new Object[] { guid, chr_id, ele_code, ele_source, ele_colname, ele_format,
          name, ele_type, paratype, parameter, default_value, nullType, dataType, sys_id, rg_code, year, "0", ele_id,
          code });
        filedcount++;
      }
    }
    tableCreate = tableCreate.substring(0, tableCreate.length() - 1);
    tableCreate = tableCreate + " ) ";

    String intElementSql = " insert into sys_element(chr_id,set_year,ele_source,ele_code,ele_name,enabled,czgb_code,rg_code,is_operate,sys_id,is_rightfilter,is_view,latest_op_date)"
      + "values(?,?,?,?,?,?,?,?,?,?,?,?,to_char(sysdate,'yyyy-mm-dd hh24:mi:ss')) ";
    int i = generalDAO.executeBySql(intElementSql, new Object[] { chr_id, year, ele_source, ele_code, ele_name,
      enabled, czgb_code, rg_code, is_operate, sys_id, dataRight, is_view });

    String managerSql = "insert into sys_tablemanager(chr_id,table_code,table_name,table_type,table_desc,is_system,create_date,latest_op_date,id_column_name,sys_id)"
      + "values(?,?,?,?,?,?,to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),?,?)";
    String guid = UUIDRandom.generate();
    generalDAO.executeBySql(managerSql, new Object[] { guid, ele_source, ele_name, "101", ele_name, "1", "CHR_ID",
      sys_id });
    if ("0".equals(is_view)) {
      generalDAO.executeBySql(tableCreate);
    }
    map.put("chr_id", chr_id);
    map.put("flag", "1");
    return map;

  }

  /**
   * 查询要素source信息
   */
  public Map<String, Object> queryElementColumn(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String chr_id = request.getParameter("chr_id");
    String ele_source = request.getParameter("ele_source");
    String sql = " select t.element_code as code ,t.ele_id id, t.field_name ele_name,t.field_code ele_colname, t.* from SYS_TABLEMANAGER_FILED t where t.rg_code = ? and t.set_year = ? and t.chr_id = ? and t.is_sysdefault = '0' ";
    List list1 = generalDAO.findBySql(sql, new Object[] { rg_code, year, chr_id });
    String contsql = " select count(1) a from  " + ele_source;
    String num = "0";
    try {
      List list2 = generalDAO.findBySql(contsql);
      Map map1 = (Map) list2.get(0);
      num = (String) map1.get("a");
    } catch (Exception e) {
      e.printStackTrace();
      map.put("message", "表不存在");
    }
    map.put("source", list1);
    map.put("num", num);
    return map;
  }

  public Map<String, Object> updateElementColumn(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String chr_id = request.getParameter("chr_id");
    String ele_code = request.getParameter("ele_code");
    String ele_source = request.getParameter("ele_source");
    String ele_name = request.getParameter("ele_name");
    String czgb_code = request.getParameter("czgb_code");
    String enabled = request.getParameter("enabled");
    String dataRight = request.getParameter("is_rightfilter");
    String is_operate = request.getParameter("is_operate");
    String source_type = request.getParameter("source_type");
    String sys_id = request.getParameter("sys_id");
    String griddata = request.getParameter("griddata");
    String upsql = " update sys_element t set t.ele_code = ? ,t.ele_name = ? , t.enabled = ?"
      + " , t.is_rightfilter = ? , t.is_operate =? , t.sys_id = ? , t.czgb_code = ? where t.chr_id = ? ";
    int k = generalDAO.executeBySql(upsql, new Object[] { ele_code, ele_name, enabled, dataRight, is_operate, sys_id,
      czgb_code, chr_id });
    JSONObject jsonObject = JSONObject.fromObject(griddata);
    Map<String, Object> map1 = (Map<String, Object>) jsonObject;
    if (map1.size() > 0) {
      String updateEditSql = " update SYS_TABLEMANAGER_FILED t set t.edit_show = ? where t.guid = ? ";
      Iterator<String> iter = map1.keySet().iterator();
      while (iter.hasNext()) {
        String key = iter.next();
        String value = (String) map1.get(key);
        generalDAO.executeBySql(updateEditSql, new Object[] { value, key });
      }
    }
    map.put("flag", "1");
    map.put("chr_id", chr_id);
    return map;
  }

  public Map<String, Object> updateNodataElementColumn(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String chr_id = request.getParameter("chr_id");
    String ele_code = request.getParameter("ele_code");
    String ele_source = request.getParameter("ele_source");
    String ele_name = request.getParameter("ele_name");
    String czgb_code = request.getParameter("czgb_code");
    String enabled = request.getParameter("enabled");
    String dataRight = request.getParameter("is_rightfilter");
    String is_operate = request.getParameter("is_operate");
    String source_type = request.getParameter("source_type");
    String sys_id = request.getParameter("sys_id");
    String is_view = request.getParameter("is_view");
    String griddata = request.getParameter("griddata");
    String upsql = " update sys_element t set t.ele_code = ? ,t.ele_name = ? , t.enabled = ?"
      + " , t.is_rightfilter = ? , t.is_operate =? , t.sys_id = ? , t.czgb_code = ? where t.chr_id = ? ";
    int k = generalDAO.executeBySql(upsql, new Object[] { ele_code, ele_name, enabled, dataRight, is_operate, sys_id,
      czgb_code, chr_id });
    String source_column = request.getParameter("source_column");
    String delsql = " delete  SYS_TABLEMANAGER_FILED t where  t.table_name = ?　and t.rg_code = ? and t.set_year = ? ";
    generalDAO.executeBySql(delsql, new Object[] { ele_source, rg_code, year });

    String filedSql = "insert into SYS_TABLEMANAGER_FILED (guid,CHR_ID,ELE_CODE,TABLE_NAME,FIELD_CODE,FIELD_SIZE,FIELD_NAME,FIELD_TYPE,PARA_TYPE,"
      + "PARAMETER,DEFAULT_VALUE,ENABLE_NULL,EDIT_SHOW,SYS_ID,RG_CODE,SET_YEAR,IS_SYSDEFAULT,ELE_ID,ELEMENT_CODE,CREATEDATE )"
      + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'))";
    String tableCreate = "create table " + ele_source + " ( ";
    int filedcount = 0;
    String colsql = " select * from SYS_ELEMENT_COLUMN t where t.rg_code = ? and t.set_year = ? and t.IS_SYSDEFAULT = '1' order by ele_code";
    List<Map> deflist = generalDAO.findBySql(colsql, new Object[] { rg_code, year });
    for (Map defMap : deflist) {
      String nullsql = "";
      String defsql = "";
      String typesql = "";
      String guid = UUIDRandom.generate();
      String nullType = (String) defMap.get("is_null");
      String code = (String) defMap.get("ele_code");
      String ele_colname = (String) defMap.get("ele_colname");
      String dataType = (String) defMap.get("is_show");
      String ele_type = (String) defMap.get("ele_type");
      String ele_format = (String) defMap.get("ele_format");
      String parameter = (String) defMap.get("parameter");
      String paratype = (String) defMap.get("paratype");
      String name = (String) defMap.get("ele_name");
      String ele_id = (String) defMap.get("ele_id");
      String default_value = (String) defMap.get("default_value");
      if ("0".equals(nullType)) {
        nullsql = " not null ";
      }
      if (!"".equals(default_value) && default_value != null) {
        defsql = "default " + default_value;
      }
      if ("1".equals(ele_type)) {
        typesql = "VARCHAR2(" + ele_format + ")";
      } else if ("2".equals(ele_type)) {
        typesql = "NUMBER(" + ele_format + ")";
      } else if ("3".equals(ele_type)) {
        typesql = "DATE";
      } else if ("4".equals(ele_type)) {
        typesql = "NUMBER(" + ele_format + ",2)";
      }
      tableCreate = tableCreate + ele_colname + " " + typesql + " " + defsql + " " + nullsql + " ,";
      generalDAO.executeBySql(filedSql, new Object[] { guid, chr_id, ele_code, ele_source, ele_colname, ele_format,
        name, ele_type, paratype, parameter, default_value, nullType, dataType, sys_id, rg_code, year, "1", ele_id,
        code });
      filedcount++;
    }
    if (!"".equals(source_column) && source_column != null) {
      JSONObject jsonObject = JSONObject.fromObject(source_column);
      Map<String, Object> map1 = (Map<String, Object>) jsonObject;
      Iterator<String> iter = map1.keySet().iterator();
      while (iter.hasNext()) {
        String nullsql = "";
        String defsql = "";
        String typesql = "";
        String key = iter.next();
        Map value = (Map) map1.get(key);
        String guid = UUIDRandom.generate();
        String nullType = (String) value.get("btn").toString();
        String dataType = (String) value.get("data").toString();
        String ele_type = (String) value.get("ele_type").toString();
        String ele_format = (String) value.get("ele_format").toString();
        String parameter = (String) value.get("parameter").toString();
        if ("null".equals(parameter)) {
          parameter = "";
        }
        String paratype = (String) value.get("paratype").toString();
        String name = (String) value.get("ele_name").toString();
        String code = (String) value.get("ele_code").toString();
        String ele_id = (String) value.get("ele_id").toString();
        String default_value = (String) value.get("default_value").toString();
        if ("null".equals(default_value)) {
          default_value = "";
        }
        String ele_colname = (String) value.get("ele_colname").toString();
        if ("0".equals(nullType)) {
          nullsql = " not null ";
        }
        if (!"".equals(default_value) && default_value != "null") {
          defsql = "default " + default_value;
        }
        if ("1".equals(ele_type)) {
          typesql = "VARCHAR2(" + ele_format + ")";
        } else if ("2".equals(ele_type)) {
          typesql = "NUMBER(" + ele_format + ")";
        } else if ("3".equals(ele_type)) {
          typesql = "DATE";
        } else if ("4".equals(ele_type)) {
          typesql = "NUMBER(" + ele_format + ",2)";
        }
        tableCreate = tableCreate + ele_colname + " " + typesql + " " + defsql + " " + nullsql + " ,";
        generalDAO.executeBySql(filedSql, new Object[] { guid, chr_id, ele_code, ele_source, ele_colname, ele_format,
          name, ele_type, paratype, parameter, default_value, nullType, dataType, sys_id, rg_code, year, "0", ele_id,
          code });
        filedcount++;
      }
    }
    tableCreate = tableCreate.substring(0, tableCreate.length() - 1);
    tableCreate = tableCreate + " ) ";
    if ("0".equals(is_view)) {
      String drop = " drop table " + ele_source;
      generalDAO.executeBySql(drop);
      generalDAO.executeBySql(tableCreate);
    }
    map.put("flag", "1");
    map.put("chr_id", chr_id);
    return map;
  }

  /**
   * 自动生成form信息
   */
  public Map<String, Object> getElementForm(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String tablename = request.getParameter("tablename");
    String sql = " select * from SYS_TABLEMANAGER_FILED t where t.table_name = ? and t.rg_code = ? and t.set_year = ? order by element_code ";
    List list = generalDAO.findBySql(sql, new Object[] { tablename, rg_code, year });
    map.put("flag", "1");
    map.put("detail", list);
    return map;
  }

  /**
   * 插入要素值
   */
  public Map<String, Object> updateElementDate(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String chr_id = UUIDRandom.generate();
    String upobj = request.getParameter("upobj");
    String pid = request.getParameter("pid");
    String tablename = request.getParameter("tablename");
    int level_num = -1;
    if (!"".equals(pid) && pid != null) {
      String selsql = " select t.level_num , t.is_leaf from " + tablename + " t  where t.chr_id = ?  ";
      List list = generalDAO.findBySql(selsql, new Object[] { pid });
      if (list.size() > 0) {
        Map map2 = (Map) list.get(0);
        level_num = Integer.parseInt((String)map2.get("level_num"));
//        level_num = (Integer) map2.get("level_num");
        String is_leaf = (String) map2.get("is_leaf");
        if ("1".equals(is_leaf)) {
          String upsql = "update  " + tablename + " t set t.is_leaf = '0' where t.chr_id = ?";
          generalDAO.executeBySql(upsql, new Object[] { pid });
        }
      }
    }
    level_num++;
    String insql = " insert into " + tablename + "( ";
    String datasql = " values( ";
    JSONObject jsonObject = JSONObject.fromObject(upobj);
    Map<String, Object> map1 = (Map<String, Object>) jsonObject;
    Iterator<String> iter = map1.keySet().iterator();
    while (iter.hasNext()) {
      String key = iter.next();
      String value = (String) map1.get(key);
      insql = insql + key + " ,";
      datasql = datasql + "'" + value + "' ,";
      if (key.equals("CHR_CODE")) {
        insql = insql + " DISP_CODE ,";
        datasql = datasql + "'" + value + "' ,";
      }
    }
    insql = insql + "rg_code,set_year,chr_id,is_leaf,level_num,LATEST_OP_DATE)";
    datasql = datasql + "'" + rg_code + "','" + year + "','" + chr_id + "','1','" + level_num
      + "',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'))";
    String sql = insql + datasql;
    int k = generalDAO.executeBySql(sql);

    map.put("flag", "1");
    return map;
  }

  /**
   * 更新要素启用状态
   */
  public Map<String, Object> updateEnableDate(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String chr_id = request.getParameter("chr_id");
    String enabled = request.getParameter("enabled");
    String sql = " update sys_element t set t.enabled = decode(?,0,1,1,0) where t.chr_id = ? ";
    int k = generalDAO.executeBySql(sql, new Object[] { enabled, chr_id });
    map.put("flag", "1");
    return map;
  }

  /**
   * 更新要素值
   */
  public Map<String, Object> updateElementData1(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String upobj = request.getParameter("upobj");
    String tablename = request.getParameter("tablename");
    String pid = request.getParameter("pid");
    String oldpid = request.getParameter("oldpid");
    String chr_id = request.getParameter("chr_id");
    int level_num = -1;
    if (!pid.equals(oldpid)) {
      String sql = "select 1 from (select * from " + tablename + " start with chr_id = ? "
        + "connect by prior parent_id = chr_id ) a where a.chr_id = ?    ";
      List list = generalDAO.findBySql(sql, new Object[] { pid, chr_id });
      if (list.size() > 0) {
        map.put("flag", "0");
        map.put("message", "不可设置该节点为父节点");
        return map;
      }

      if (!"".equals(pid) && pid != null) {
        String selsql = " select t.level_num , t.is_leaf from " + tablename + " t  where t.chr_id = ?  ";
        List list2 = generalDAO.findBySql(selsql, new Object[] { pid });
        if (list2.size() > 0) {
          Map map2 = (Map) list2.get(0);
          level_num = Integer.parseInt((String) map2.get("level_num"));
          String is_leaf = (String) map2.get("is_leaf");
          if ("1".equals(is_leaf)) {
            String upsql = "update  " + tablename + " t set t.is_leaf = '0' where t.chr_id = ?";
            generalDAO.executeBySql(upsql, new Object[] { pid });
          }
        }
      }

      if (!"".equals(oldpid) && oldpid != null) {
        String selsql = " select 1 from " + tablename + " t  where t.chr_id = ?  ";
        List list2 = generalDAO.findBySql(selsql, new Object[] { oldpid });
        if (list.size() == 0) {
          String upsql = "update  " + tablename + " t set t.is_leaf = '1' where t.chr_id = ?";
          generalDAO.executeBySql(upsql, new Object[] { oldpid });
        }
      }
    }
    level_num++;
    String upsql = " update " + tablename + " set ";
    JSONObject jsonObject = JSONObject.fromObject(upobj);
    Map<String, Object> map1 = (Map<String, Object>) jsonObject;
    Iterator<String> iter = map1.keySet().iterator();
    while (iter.hasNext()) {
      String key = iter.next();
      String value = map1.get(key).toString();
      if ("null".equals(value)) {
        value = "";
      }
      upsql = upsql + key + " = '" + value + "',";
    }
    upsql = upsql + "level_num = '" + level_num + "' where chr_id = '" + chr_id + "'";
    int k = generalDAO.executeBySql(upsql);
    map.put("flag", "1");
    return map;
  }

  /**
   * 删除要素值
   */
  public Map<String, Object> delElementData1(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String tablename = request.getParameter("tablename");
    String chr_id = request.getParameter("chr_id");
    int k = 0;
    String[] id = chr_id.split(",");
    String sql = " delete " + tablename + "  where chr_id = ? ";
    String upsql = "update " + tablename + " set is_leaf='1' where chr_id = (select t.parent_id from " + tablename
      + " t where t.chr_id = ?)";
    for (int i = 0; i < id.length; i++) {
      String selsql = "select 1 from " + tablename + " a where a.parent_id = (select t.parent_id from " + tablename
        + " t where t.chr_id = ?)";
      List list = generalDAO.findBySql(selsql, new Object[] { id[i] });
      if (list.size() == 0) {
        generalDAO.executeBySql(upsql, new Object[] { id[i] });
      }
      generalDAO.executeBySql(sql, new Object[] { id[i] });
      k++;
    }
    map.put("flag", "1");
    map.put("num", k);
    return map;
  }

  /**
   * 删除要素
   */
  public Map<String, Object> delElementData2(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String chr_id = request.getParameter("chr_id");
    String sql = " delete  sys_element  where chr_id = ? ";
    int k = generalDAO.executeBySql(sql, new Object[] { chr_id });
    map.put("flag", "1");
    map.put("num", k);
    return map;

  }

  /**
   * 删除要素全信息
   */
  public Map<String, Object> delElementData3(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<String, Object>();
    String year = SessionUtil.getLoginYear();
    String rg_code = SessionUtil.getRgCode();
    String chr_id = request.getParameter("chr_id");
    String selSql = " select ele_source,is_view from sys_element  where chr_id = ? ";
    String delSql = " delete  sys_tablemanager_filed t where t.chr_id = ? and t.rg_code = ? and t.set_year = ?  ";
    String sql = " delete  sys_element  where chr_id = ? ";
    List list = generalDAO.findBySql(selSql, new Object[] { chr_id });
    if (list.size() > 0) {
      Map map1 = (Map) list.get(0);
      String source = (String) map1.get("ele_source");
      String dropsql = "drop table " + source;
      String is_view = (String) map1.get("is_view");
      if (is_view != "1") {
        generalDAO.executeBySql(dropsql);
      }
    }
    generalDAO.executeBySql(delSql, new Object[] { chr_id, rg_code, year });
    int k = generalDAO.executeBySql(sql, new Object[] { chr_id });
    map.put("flag", "1");
    map.put("num", k);
    return map;

  }
}
