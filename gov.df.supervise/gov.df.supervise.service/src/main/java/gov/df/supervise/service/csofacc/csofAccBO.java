package gov.df.supervise.service.csofacc;

import gov.df.fap.bean.util.FPaginationDTO;
import gov.df.fap.service.util.sessionmanager.SessionUtil;
import gov.df.fap.util.Tools;
import gov.df.supervise.api.csofacc.csofAccService;
import gov.df.supervise.bean.csofacc.csofAccProblemEntity;
import gov.df.supervise.bean.csofacc.csofAccSupDepEntity;
import gov.df.supervise.bean.csofacc.csofAccSupEndEntity;
import gov.df.supervise.bean.csofacc.csofAccSupEntity;
import gov.df.supervise.bean.csofacc.csofAccSupOfficeEntity;
import gov.df.supervise.bean.csofacc.csofAccWorkEntity;
import gov.df.supervise.bean.csofacc.csofTaskUserEntity;
import gov.df.supervise.service.Tree.elementTreeDao;
import gov.df.supervise.service.common.SessionUtilEx;
import gov.df.supervise.service.examine.ExamineDao;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.ufgov.ip.apiUtils.UUIDTools;

@Service
public class csofAccBO implements csofAccService {
  @Autowired
  private csofAccDao csofAccDao;

  @Autowired
  private elementTreeDao elementTreedao;

  @Autowired
  private ExamineDao csDao;

  /**
   * 查询监管类型树
   */
  public List getSupType(String ele_code, String acc_id) {
    Map<String, Object> param = new HashMap<String, Object>();
    String ele_source = elementTreedao.findEleSource(ele_code);
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    param.put("ele_source", ele_source);
    param.put("acc_id", acc_id);
    param.put("user_id", user_id);
    return csofAccDao.getSupType(param);
  }

  public void saveAccSup(String data, String sup_no, String book_id, String acc_id) throws IOException {
    Map<String, Object> param = new HashMap<String, Object>();
    csofAccSupEntity Data = JSONObject.parseObject(data, csofAccSupEntity.class);
    Map<String, Object> map = csofAccDao.getAccSupByBookId(book_id);
    String id = UUIDTools.uuidRandom(); // 自动生成id
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String org_code = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    String date = Tools.getCurrDate();
    String mofdep_id = Data.getMofdep_name().split("@")[0];
    String mofdep_code = Data.getMofdep_name().split("@")[2];
    String mofdep_name1 = Data.getMofdep_name().split("@")[1];
    String mofdep_name = URLDecoder.decode(mofdep_name1, "UTF-8");
    param.put("id", id);
    param.put("sid", id);
    param.put("acc_id", acc_id);
    param.put("book_id", book_id);
    param.put("set_id", map.get("ID").toString());
    param.put("set_month", map.get("SET_MONTH").toString());
    param.put("year", map.get("SET_YEAR").toString());
    param.put("sup_no", sup_no);
    param.put("sup_cost", 0);
    param.put("sup_money", 0);
    param.put("sup_object", Data.getSup_object());
    param.put("sup_name", Data.getSup_name());
    param.put("status", 0);
    param.put("is_valid", 1);
    param.put("is_end", 0);
    param.put("mofdep_id", mofdep_id);
    param.put("mofdep_code", mofdep_code);
    param.put("mofdep_name", mofdep_name);
    param.put("oid", org_code);
    param.put("dep_id", Data.getDep_name().split("@")[0]);
    param.put("dep_code", Data.getDep_name().split("@")[2]);
    param.put("dep_name", URLDecoder.decode(Data.getDep_name().split("@")[1], "UTF-8"));
    param.put("sup_type_id", Data.getSup_type_name().split("@")[0]);
    param.put("sup_type_code", Data.getSup_type_name().split("@")[2]);
    param.put("sup_type_name", URLDecoder.decode(Data.getSup_type_name().split("@")[1], "UTF-8"));
    param.put("obj_type_id", Data.getObj_type_name().split("@")[0]);
    param.put("obj_type_code", Data.getObj_type_name().split("@")[2]);
    param.put("obj_type_name", URLDecoder.decode(Data.getObj_type_name().split("@")[1], "UTF-8"));
    param.put("is_allobj", Data.getIs_allobj());
    param.put("sup_num", Data.getSup_num());
    param.put("work_type", Data.getWork_type());
    param.put("sup_mode", Data.getSup_mode());
    param.put("sup_cycle", Data.getSup_cycle());
    param.put("start_date", Data.getStart_date());
    param.put("end_date", Data.getEnd_date());
    param.put("sup_content", Data.getSup_content());
    param.put("remark", Data.getRemark());
    param.put("create_user", user_id);
    param.put("create_date", date);
    param.put("latest_op_user", user_id);
    param.put("latest_op_date", date);
    param.put("set_year", SessionUtilEx.getLoginYear());
    param.put("rg_code", SessionUtilEx.getRgCode());
    param.put("is_input", 1);
    csofAccDao.saveAccSup(param);
  }

  public Map<String, Object> getAccSup(String entity_id) {
    Map<String, Object> data = csofAccDao.getAccSup(entity_id);
    return data;
  }

  public void updateAccSup(String data) throws IOException {
    Map<String, Object> param = new HashMap<String, Object>();
    csofAccSupEntity Data = JSONObject.parseObject(data, csofAccSupEntity.class);
    param.put("id", Data.getId());
    param.put("sid", Data.getSid());
    param.put("sup_no", Data.getSup_no());
    param.put("sup_name", Data.getSup_name());
    param.put("status", Data.getStatus());
    param.put("is_valid", Data.getIs_valid());
    param.put("is_end", Data.getIs_end());
    if (Data.getMofdep_name() != null && !Data.getMofdep_name().equals("")) {
      param.put("mofdep_id", Data.getMofdep_name().split("@")[0]);
      param.put("mofdep_code", Data.getMofdep_name().split("@")[2]);
      param.put("mofdep_name", URLDecoder.decode(Data.getMofdep_name().split("@")[1], "UTF-8"));
    }
    param.put("oid", Data.getOid());
    if (Data.getDep_name() != null && !Data.getDep_name().equals("")) {
      param.put("dep_id", Data.getDep_name().split("@")[0]);
      param.put("dep_code", Data.getDep_name().split("@")[2]);
      param.put("dep_name", URLDecoder.decode(Data.getDep_name().split("@")[1], "UTF-8"));
    }
    if (Data.getSup_type_name() != null && !Data.getSup_type_name().equals("")) {
      param.put("sup_type_id", Data.getSup_type_name().split("@")[0]);
      param.put("sup_type_code", Data.getSup_type_name().split("@")[2]);
      param.put("sup_type_name", URLDecoder.decode(Data.getSup_type_name().split("@")[1], "UTF-8"));
    }
    if (Data.getObj_type_name() != null && !Data.getObj_type_name().equals("")) {
      param.put("obj_type_id", Data.getObj_type_name().split("@")[0]);
      param.put("obj_type_code", Data.getObj_type_name().split("@")[2]);
      param.put("obj_type_name", URLDecoder.decode(Data.getObj_type_name().split("@")[1], "UTF-8"));
    }
    param.put("is_allobj", Data.getIs_allobj());
    param.put("sup_num", Data.getSup_num());
    param.put("work_type", Data.getWork_type());
    param.put("sup_mode", Data.getSup_mode());
    param.put("sup_cycle", Data.getSup_cycle());
    param.put("start_date", Data.getStart_date());
    param.put("end_date", Data.getEnd_date());
    param.put("sup_content", Data.getSup_content());
    param.put("remark", Data.getRemark());
    param.put("create_user", Data.getCreate_user());
    param.put("create_date", Data.getCreate_date());
    param.put("latest_op_user", Data.getLatest_op_user());
    param.put("latest_op_date", Data.getLatest_op_date());
    param.put("set_year", Data.getSet_year());
    param.put("rg_code", Data.getRg_code());
    param.put("sup_object", Data.getSup_object());
    csofAccDao.updateAccSup(param);
  }

  public void deleteAccSup(String entity_id) {
    csofAccDao.deleteAccSup(entity_id);
  }

  /**
   * 工作记录
   */
  public void saveAccWork(String data, String work_no, String set_month, String book_id, String set_year,
    String entity_id) {
    Map<String, Object> param = new HashMap<String, Object>();
    csofAccWorkEntity Data = JSONObject.parseObject(data, csofAccWorkEntity.class);
    csofAccSupEntity map = csofAccDao.findAccSupBySid(entity_id);
    String id = UUIDTools.uuidRandom(); // 自动生成id
    Map<String, Object> res = csofAccDao.getAccSupByBookId(book_id);
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String org_code = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    String date = Tools.getCurrDate();
    param.put("id", id);
    param.put("work_no", work_no);
    param.put("entity_id", entity_id);
    param.put("sup_no", Data.getSup_no());
    param.put("sup_name", Data.getSup_name());
    param.put("is_valid", 1);
    param.put("is_end", 0);
    param.put("mofdep_id", Data.getMofdep_id());
    param.put("work_content", Data.getWork_content());
    param.put("mofdep_code", Data.getMofdep_code());
    param.put("mofdep_name", Data.getMofdep_name());
    param.put("oid", org_code);
    param.put("obj_type_id", Data.getObj_type_id());
    param.put("obj_type_code", Data.getObj_type_code());
    param.put("obj_type_name", Data.getObj_type_name());
    param.put("work_type", Data.getWork_type());
    param.put("work_progress", Data.getWork_progress());
    param.put("workload", Data.getWorkload());
    param.put("fund_scale", Data.getFund_scale());
    param.put("registration_time", Data.getRegistration_time());
    param.put("remark", Data.getRemark());
    param.put("create_user", user_id);
    param.put("create_date", date);
    param.put("latest_op_user", user_id);
    param.put("latest_op_date", date);
    param.put("set_month", set_month);
    param.put("book_id", book_id);
    param.put("year", set_year);
    param.put("set_id", res.get("ID").toString());
    param.put("executor", Data.getExecutor());
    param.put("set_year", SessionUtilEx.getLoginYear());
    param.put("rg_code", SessionUtilEx.getRgCode());
    csofAccDao.saveAccWork(param);
    param.put("entity_id", entity_id);
    param.put("status", Data.getWork_progress());
    csofAccDao.updateSupStatus(param);

    if (!map.getSup_cost().equals("") && map.getSup_cost() != null && !map.getSup_money().equals("")
      && map.getSup_money() != null) {
      param.put("entity_id", entity_id);
      param.put("workload", Integer.parseInt(map.getSup_cost()) + Integer.parseInt(Data.getWorkload()));
      param.put("fund_scale", Integer.parseInt(map.getSup_money()) + Integer.parseInt(Data.getFund_scale()));
      param.put("work_content", Data.getWork_content());
      csofAccDao.updateAccSupByWorkload(param);
    } else {
      param.put("entity_id", entity_id);
      param.put("workload", Data.getWorkload());
      param.put("fund_scale", Data.getFund_scale());
      param.put("work_content", Data.getWork_content());
      csofAccDao.updateAccSupByWorkload(param);
    }

  }

  public List getAccWork(String entity_id) {
    List data = csofAccDao.getAccWork(entity_id);
    return data;
  }

  public void deleteAllAccWork(String entity_id) {
    csofAccDao.deleteAllAccWork(entity_id);
  }

  public void deleteAccWork(String entity_id, String id) {
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("entity_id", entity_id);
    param.put("id", id);
    csofAccDao.deleteAccWork(param);
  }

  /**
   * 问题记录
   */
  public void saveAccProblem(String data, String entity_id) {
    Map<String, Object> param = new HashMap<String, Object>();
    csofAccProblemEntity Data = JSONObject.parseObject(data, csofAccProblemEntity.class);
    String id = UUIDTools.uuidRandom(); // 自动生成id
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String org_code = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    String date = Tools.getCurrDate();
    param.put("id", id);
    param.put("entity_id", entity_id);
    param.put("sup_no", Data.getSup_no());
    param.put("sup_name", Data.getSup_name());
    param.put("is_valid", 1);
    param.put("is_end", 0);
    param.put("oid", org_code);
    param.put("obj_type_id", Data.getObj_type_id());
    param.put("obj_type_code", Data.getObj_type_code());
    param.put("obj_type_name", Data.getObj_type_name());
    param.put("discover_problems", Data.getDiscover_problems());
    param.put("audit_basis", Data.getAudit_basis());
    param.put("audit_opinion", Data.getAudit_opinion());
    param.put("handle", Data.getHandle());
    param.put("grouping_name", Data.getGrouping_name());
    param.put("recording_user", Data.getRecording_user());
    param.put("recording_time", date);
    param.put("remark", Data.getRemark());
    param.put("create_user", user_id);
    param.put("create_date", date);
    param.put("latest_op_user", user_id);
    param.put("latest_op_date", date);
    param.put("set_year", SessionUtilEx.getLoginYear());
    param.put("rg_code", SessionUtilEx.getRgCode());
    csofAccDao.saveAccProblem(param);
  }

  public List getAccProblem(String entity_id) {
    List data = csofAccDao.getAccProblem(entity_id);
    return data;
  }

  public void updateAccProblem(String data) {
    Map<String, Object> param = new HashMap<String, Object>();
    csofAccProblemEntity Data = JSONObject.parseObject(data, csofAccProblemEntity.class);
    param.put("id", Data.getId());
    param.put("entity_id", Data.getEntity_id());
    param.put("sup_no", Data.getSup_no());
    param.put("sup_name", Data.getSup_name());
    param.put("is_valid", 1);
    param.put("is_end", 0);
    param.put("oid", Data.getOid());
    param.put("obj_type_id", Data.getObj_type_id());
    param.put("obj_type_code", Data.getObj_type_code());
    param.put("obj_type_name", Data.getObj_type_name());
    param.put("discover_problems", Data.getDiscover_problems());
    param.put("audit_basis", Data.getAudit_basis());
    param.put("audit_opinion", Data.getAudit_opinion());
    param.put("handle", Data.getHandle());
    param.put("grouping_name", Data.getGrouping_name());
    param.put("recording_user", Data.getRecording_user());
    param.put("recording_time", Data.getRecording_time());
    param.put("remark", Data.getRemark());
    param.put("create_user", Data.getCreate_user());
    param.put("create_date", Data.getCreate_date());
    param.put("latest_op_user", Data.getLatest_op_user());
    param.put("latest_op_date", Data.getLatest_op_date());
    param.put("set_year", SessionUtilEx.getLoginYear());
    param.put("rg_code", SessionUtilEx.getRgCode());
    csofAccDao.updateAccProblem(param);
  }

  public void deleteAllAccProblem(String entity_id) {
    csofAccDao.deleteAllAccProblem(entity_id);
  }

  public void deleteAccProblem(String entity_id, String id) {
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("entity_id", entity_id);
    param.put("id", id);
    csofAccDao.deleteAccProblem(param);
  }

  public List getSupData(String ele_code, String acc_id) {
    String ele_source = elementTreedao.findEleSource(ele_code);
    Map<String, Object> param = new HashMap<String, Object>();
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    param.put("acc_id", acc_id);
    param.put("user_id", user_id);
    param.put("ele_source", ele_source);
    return csofAccDao.getSupData(param);
  }

  /**
   * 通过监管类型来过树
   */
  public List getSupDataById(String ele_code, String chr_id, String acc_id) {
    String ele_source = elementTreedao.findEleSource(ele_code);
    Map<String, Object> param = new HashMap<String, Object>();
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    param.put("ele_source", ele_source);
    param.put("chr_id", chr_id);
    param.put("acc_id", acc_id);
    param.put("user_id", user_id);
    return csofAccDao.getSupDataById(param);
  }

  /**
   * 获取月结树信息
   */
  public List getBookSetById(String book_id, String menu_id, String billtype_code) {
    Map<String, Object> param = new HashMap<String, Object>();
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String org_code = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    param.put("book_id", book_id);
    param.put("oid", org_code);
    param.put("menu_id", menu_id);
    return csofAccDao.getBookSetById(param);
  }

  /**
   * 通过点击树获取工作记录
   */
  public List getAccWorkByMonth(String set_month, String book_id, String parent_id, String is_close) {
    Map<String, Object> param = new HashMap<String, Object>();
    String set_year = csofAccDao.getYear(parent_id);
    List data = null;
    if (is_close.equals("0")) {
      param.put("set_month", set_month);
      param.put("book_id", book_id);
      param.put("set_year", set_year);
      data = csofAccDao.getAccWorkByMonth(param);
    } else if (is_close.equals("1")) {
      param.put("set_month", set_month);
      param.put("book_id", book_id);
      param.put("set_year", set_year);
      data = csofAccDao.getAccWorkEndByMonth(param);
    }
    return data;
  }

  //月结
  public boolean updateAccBookSet(String set_id, String id, String set_month, String parent_id, String book_id) {
    Map<String, Object> param = new HashMap<String, Object>();
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String org_code = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    String date = Tools.getCurrDate();
    String year = csofAccDao.getYear(parent_id);
    String setId;
    if (id.equals("")) {
      if (!set_month.equals("12")) {
        int month = Integer.parseInt(set_month);
        int months = month + 1;
        String setMonth = String.valueOf(months);
        if (months < 10) {
          setMonth = "0" + setMonth;
        }
        param.put("set_month", setMonth);
        param.put("set_year", year);
        param.put("book_id", book_id);
        setId = csofAccDao.getSetId(param);
      } else {
        param.put("set_month", 01);
        param.put("set_year", year + 1);
        param.put("book_id", book_id);
        setId = csofAccDao.getSetId(param);
      }
      param.put("set_id", set_id);
      param.put("is_close", 1);
      param.put("close_user", user_id);
      param.put("close_date", date);
      csofAccDao.updateAccBookBySetid(param);
      param.put("setId", setId);
      param.put("is_open", 1);
      param.put("open_user", user_id);
      param.put("open_date", date);
      csofAccDao.updateAccBookSet(param);
      //刷新book表的年月
      if (!set_month.equals("12")) {
        int month = Integer.parseInt(set_month);
        int months = month + 1;
        String setMonth = String.valueOf(months);
        if (months < 10) {
          setMonth = "0" + setMonth;
        }
        //刷新book表的年月
        param.put("set_month", setMonth);
        param.put("set_year", year);
        param.put("book_id", book_id);
        csofAccDao.updateDate(param);
      } else {
        param.put("set_month", 01);
        param.put("set_year", year + 1);
        param.put("book_id", book_id);
        csofAccDao.updateDate(param);
      }
    } else {
      Map<String, Object> data = csofAccDao.getAccSup(id);
      int num = csofAccDao.getCount(id);
      if (num == 0) {
        if (!set_month.equals("12")) {
          int month = Integer.parseInt(set_month);
          int months = month + 1;
          String setMonth = String.valueOf(months);
          if (months < 10) {
            setMonth = "0" + setMonth;
          }
          param.put("set_month", setMonth);
          param.put("set_year", year);
          param.put("book_id", book_id);
          setId = csofAccDao.getSetId(param);
        } else {
          param.put("set_month", 01);
          param.put("set_year", year + 1);
          param.put("book_id", book_id);
          setId = csofAccDao.getSetId(param);
        }
        param.put("set_id", set_id);
        param.put("is_close", 1);
        param.put("close_user", user_id);
        param.put("close_date", date);
        csofAccDao.updateAccBookBySetid(param);
        param.put("setId", setId);
        param.put("is_open", 1);
        param.put("open_user", user_id);
        param.put("open_date", date);
        csofAccDao.updateAccBookSet(param);
        param.put("id", data.get("ID").toString());
        param.put("sid", data.get("SID").toString());
        param.put("set_id", data.get("SET_ID").toString());
        param.put("sup_no", data.get("SUP_NO").toString());
        param.put("sup_name", data.get("SUP_NAME").toString());
        param.put("status", data.get("STATUS").toString());
        if (data.get("WORK_CONTENT") == null) {
          param.put("work_content", "");
        } else {
          param.put("work_content", data.get("WORK_CONTENT").toString());
        }
        param.put("is_valid", Integer.parseInt(data.get("IS_VALID").toString()));
        param.put("is_end", Integer.parseInt(data.get("IS_END").toString()));
        param.put("mofdep_id", data.get("MOFDEP_ID").toString());
        param.put("mofdep_code", data.get("MOFDEP_CODE").toString());
        param.put("mofdep_name", data.get("MOFDEP_NAME").toString());
        param.put("oid", data.get("OID").toString());
        param.put("dep_id", data.get("DEP_ID").toString());
        param.put("dep_code", data.get("DEP_CODE").toString());
        param.put("dep_name", data.get("DEP_NAME").toString());
        param.put("sup_type_id", data.get("SUP_TYPE_ID").toString());
        param.put("sup_type_code", data.get("SUP_TYPE_CODE").toString());
        param.put("sup_type_name", data.get("SUP_TYPE_NAME").toString());
        param.put("obj_type_id", data.get("OBJ_TYPE_ID").toString());
        param.put("obj_type_code", data.get("OBJ_TYPE_CODE").toString());
        param.put("obj_type_name", data.get("OBJ_TYPE_NAME").toString());
        param.put("is_allobj", Integer.parseInt(data.get("IS_ALLOBJ").toString()));
        param.put("sup_num", Integer.parseInt(data.get("SUP_NUM").toString()));
        param.put("work_type", data.get("WORK_TYPE").toString());
        param.put("sup_mode", data.get("SUP_MODE").toString());
        param.put("sup_cycle", data.get("SUP_CYCLE").toString());
        param.put("start_date", data.get("START_DATE").toString());
        param.put("end_date", data.get("END_DATE").toString());
        param.put("sup_content", data.get("SUP_CONTENT").toString());
        if (data.get("REMARK") == null) {
          param.put("remark", "");
        } else {
          param.put("remark", data.get("REMARK").toString());
        }
        param.put("create_user", user_id);
        param.put("create_date", date);
        param.put("latest_op_user", user_id);
        param.put("latest_op_date", date);
        param.put("book_id", data.get("BOOK_ID").toString());
        param.put("set_id", data.get("SET_ID").toString());
        param.put("set_month", data.get("SET_MONTH").toString());
        param.put("year", data.get("YEAR").toString());
        param.put("sup_money", data.get("SUP_MONEY").toString());
        param.put("sup_cost", data.get("SUP_COST").toString());
        if (data.get("SUP_OBJECT") == null) {
          param.put("sup_object", "");
        } else {
          param.put("sup_object", data.get("SUP_OBJECT").toString());
        }
        param.put("set_year", SessionUtilEx.getLoginYear());
        param.put("rg_code", SessionUtilEx.getRgCode());
        param.put("set_year", SessionUtilEx.getLoginYear());
        param.put("rg_code", SessionUtilEx.getRgCode());
        param.put("acc_id", data.get("ACC_ID").toString());
        param.put("is_input", Integer.parseInt(data.get("IS_INPUT").toString()));
        if (Integer.parseInt(data.get("IS_INPUT").toString()) == 1) {
          param.put("dep_task_id", "");
          param.put("task_name", "");
          param.put("task_no", "");
        } else {
          param.put("dep_task_id", data.get("DEP_TASK_ID").toString());
          param.put("task_name", data.get("TASK_NAME").toString());
          param.put("task_no", data.get("TASK_NO").toString());
        }
        csofAccDao.saveAccSupEnd(param);
        if (!set_month.equals("12")) {
          int month = Integer.parseInt(set_month);
          int months = month + 1;
          String setMonth = String.valueOf(months);
          if (months < 10) {
            setMonth = "0" + setMonth;
          }
          param.put("set_month", setMonth);
          param.put("set_year", year);
          param.put("id", id);
          param.put("setId", setId);
          csofAccDao.updateAccSupBySid(param);
        } else {
          param.put("set_month", 01);
          param.put("set_year", year + 1);
          param.put("id", id);
          param.put("setId", setId);
          csofAccDao.updateAccSupBySid(param);
        }
        //刷新book表的年月
        if (!set_month.equals("12")) {
          int month = Integer.parseInt(set_month);
          int months = month + 1;
          String setMonth = String.valueOf(months);
          if (months < 10) {
            setMonth = "0" + setMonth;
          }
          //刷新book表的年月
          param.put("set_month", setMonth);
          param.put("set_year", year);
          param.put("book_id", book_id);
          csofAccDao.updateDate(param);
        } else {
          param.put("set_month", 01);
          param.put("set_year", year + 1);
          param.put("book_id", book_id);
          csofAccDao.updateDate(param);
        }
      }
    }
    return true;
  }

  //反月结
  public boolean modityAccBookSet(String set_id, String id, String set_month, String parent_id, String book_id) {
    Map<String, Object> param = new HashMap<String, Object>();
    String year = csofAccDao.getYear(parent_id);
    String setId;
    if (id.equals("")) {
      if (!set_month.equals("12")) {
        int month = Integer.parseInt(set_month);
        int months = month + 1;
        String setMonth = String.valueOf(months);
        if (months < 10) {
          setMonth = "0" + setMonth;
        }
        param.put("set_month", setMonth);
        param.put("set_year", year);
        param.put("book_id", book_id);
        setId = csofAccDao.getSetId(param);
      } else {
        param.put("set_month", 01);
        param.put("set_year", year + 1);
        param.put("book_id", book_id);
        setId = csofAccDao.getSetId(param);
      }
      param.put("set_id", set_id);
      param.put("is_close", 0);
      param.put("close_user", "");
      param.put("close_date", "");
      csofAccDao.updateAccBookBySetid(param);
      param.put("setId", setId);
      param.put("is_open", 0);
      param.put("open_user", "");
      param.put("open_date", "");
      csofAccDao.updateAccBookSet(param);
      //刷新book表的年月
      param.put("set_month", set_month);
      param.put("set_year", year);
      param.put("book_id", book_id);
      csofAccDao.updateDate(param);
    } else {
      param.put("set_id", set_id);
      param.put("id", id);
      int num = csofAccDao.getCountEnd(param);
      if (num != 0) {
        if (!set_month.equals("12")) {
          int month = Integer.parseInt(set_month);
          int months = month + 1;
          String setMonth = String.valueOf(months);
          if (months < 10) {
            setMonth = "0" + setMonth;
          }
          param.put("set_month", setMonth);
          param.put("set_year", year);
          param.put("book_id", book_id);
          setId = csofAccDao.getSetId(param);
        } else {
          param.put("set_month", 01);
          param.put("set_year", year + 1);
          param.put("book_id", book_id);
          setId = csofAccDao.getSetId(param);
        }
        param.put("set_id", set_id);
        param.put("is_close", 0);
        param.put("close_user", "");
        param.put("close_date", "");
        csofAccDao.updateAccBookBySetid(param);
        param.put("setId", setId);
        param.put("is_open", 0);
        param.put("open_user", "");
        param.put("open_date", "");
        csofAccDao.updateAccBookSet(param);
        param.put("id", id);
        param.put("set_id", set_id);
        param.put("set_month", set_month);
        param.put("set_year", year);
        csofAccDao.modityAccSupBysid(param);
        param.put("id", id);
        param.put("set_id", set_id);
        csofAccDao.deleteAccSupEnd(param);
        //刷新book表的年月
        param.put("set_month", set_month);
        param.put("set_year", year);
        param.put("book_id", book_id);
        csofAccDao.updateDate(param);
      }
    }
    return true;

  }

  //台账处汇总
  /**
   * 得到处汇总左侧树
   */
  public List getBookDepById(String book_id, String menu_id, String billtype_code) {
    Map<String, Object> param = new HashMap<String, Object>();
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String org_code = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    param.put("book_id", book_id);
    param.put("oid", org_code);
    param.put("menu_id", menu_id);
    return csofAccDao.getBookDepById(param);
  }

  /**
   * 点击树显示详细信息
   */
  public List getAccSupEndByBook(String dep_code, String chr_name, String parent_id) {
    Map<String, Object> param = new HashMap<String, Object>();
    String year = csofAccDao.getChrCode(parent_id);
    param.put("dep_code", dep_code);
    param.put("set_month", chr_name);
    param.put("year", year);
    return csofAccDao.getAccSupEndByBook(param);
  }

  /**
   * 点击树显示处汇总表信息
   */
  public List getAccSupDepBySetid(String book_id, String chr_name, String parent_id, String chr_code) {
    Map<String, Object> param = new HashMap<String, Object>();
    String year = csofAccDao.getChrCode(parent_id);
    param.put("book_id", book_id);
    param.put("set_month", chr_name);
    param.put("year", year);
    param.put("set_id", chr_code);
    return csofAccDao.getAccSupDepBySetid(param);
  }

  /**
   * 点击树查看明细台账的数据
   */
  public List getAccSupEndBysetid(String book_id, String chr_code) {
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("chr_code", chr_code);
    return csofAccDao.getAccSupEndBysetid(param);
  }

  /**
   * 插入数据到处汇总表中
   * @throws Exception 
   */

  public boolean saveAccSupDep(String acc_sup_dep_id, String sid, String set_id, String sup_type_id, String sup_name,
    String dep_id, String dep_code, String dep_name, String chr_code, String parent_id, String book_id,
    String chr_name, String dep_task_id, String Id, String obj_type_id) throws Exception {
    Map<String, Object> param = new HashMap<String, Object>();
    String year = csofAccDao.getChrCode(parent_id);
    String setId;
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String org_code = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    String date = Tools.getCurrDate();
    if (Id.equals("")) {
      if (!chr_name.equals("12")) {
        int month = Integer.parseInt(chr_name);
        int months = month + 1;
        String setMonth = String.valueOf(months);
        if (months < 10) {
          setMonth = "0" + setMonth;
        }
        param.put("set_month", setMonth);
        param.put("set_year", year);
        param.put("book_id", book_id);
        setId = csofAccDao.getSetId(param);
      } else {
        param.put("set_month", 01);
        param.put("set_year", year + 1);
        param.put("book_id", book_id);
        setId = csofAccDao.getSetId(param);
      }

      param.put("set_id", chr_code);
      param.put("is_close", 1);
      param.put("close_user", user_id);
      param.put("close_date", date);
      csofAccDao.updateAccBookBySetid(param);
      param.put("setId", setId);
      param.put("is_open", 1);
      param.put("open_user", user_id);
      param.put("open_date", date);
      csofAccDao.updateAccBookSet(param);
      //刷新book表的年月
      if (!chr_name.equals("12")) {
        int month = Integer.parseInt(chr_name);
        int months = month + 1;
        String setMonth = String.valueOf(months);
        if (months < 10) {
          setMonth = "0" + setMonth;
        }
        //刷新book表的年月
        param.put("set_month", setMonth);
        param.put("set_year", year);
        param.put("book_id", book_id);
        csofAccDao.updateDate(param);
      } else {
        param.put("set_month", 01);
        param.put("set_year", year + 1);
        param.put("book_id", book_id);
        csofAccDao.updateDate(param);
      }
      return true;
    } else {
      param.put("end_id", Id);
      param.put("set_id", set_id);
      csofAccSupEndEntity data = csofAccDao.FindAccSup(param);
      String id = UUIDTools.uuidRandom(); // 自动生成id
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      Calendar calendar = new GregorianCalendar();
      Date d1 = df.parse(data.getStart_date());
      Date d2 = df.parse(data.getEnd_date());
      long days = (d2.getTime() - d1.getTime()) / (60 * 60 * 1000 * 24);
      if (acc_sup_dep_id == null) {
        if (data.getIs_input() == 0) {
          param.put("dep_task_id", dep_task_id);
          param.put("sid", sid);
          param.put("set_month", chr_name);
          param.put("year", year);
          param.put("set_id", chr_code);
          int sum = csofAccDao.getTaskAccCount(param);
          param.put("dep_task_id", dep_task_id);
          param.put("sid", sid);
          param.put("set_month", chr_name);
          param.put("year", year);
          param.put("set_id", chr_code);
          Map<String, Object> res = csofAccDao.getTaskAccSupDep(param);
          if (sum == 0) {
            param.put("id", id);
            param.put("sid", sid);
            param.put("dep_task_id", dep_task_id);
            param.put("task_no", data.getTask_no());
            param.put("task_name", data.getTask_name());
            param.put("acc_id", data.getAcc_id());
            param.put("sup_no", data.getSup_no());
            param.put("sup_name", data.getSup_name());
            param.put("is_valid", 1);
            param.put("is_end", 0);
            param.put("oid", org_code);
            param.put("dep_id", dep_id);//汇总处室id
            param.put("dep_code", dep_code);
            param.put("dep_name", dep_name);
            param.put("sup_type_id", data.getSup_type_id());
            param.put("sup_type_code", data.getSup_type_code());
            param.put("sup_type_name", data.getSup_type_name());
            param.put("regulatory_area", "");//监管区域
            param.put("status", data.getStatus());
            param.put("work_type", data.getWork_type());
            param.put("regulatory_results", "");//监管成果
            param.put("feedback", "");//利用及反馈
            param.put("create_user", user_id);
            param.put("create_date", date);
            if (data.getWork_content() == null) {
              param.put("work_content", "");
            } else {
              param.put("work_content", data.getWork_content());
            }
            param.put("latest_op_user", user_id);
            param.put("latest_op_date", date);
            param.put("set_year", SessionUtilEx.getLoginYear());
            param.put("rg_code", SessionUtilEx.getRgCode());
            param.put("obj_type_name", data.getObj_type_name());
            param.put("obj_type_code", data.getObj_type_code());
            param.put("obj_type_id", data.getObj_type_id());
            param.put("sup_content", data.getSup_content());//监管内容
            param.put("sup_money", data.getSup_money());//资金规模
            param.put("time_requirement", days);//时间要求 
            param.put("sup_cost", data.getSup_cost()); //工作量
            if (data.getSup_object() == null) {
              param.put("sup_object", "");
            } else {
              param.put("sup_object", data.getSup_object());
            }
            if (data.getRemark() == null) {
              param.put("remark", "");
            } else {
              param.put("remark", data.getRemark());
            }
            param.put("set_month", chr_name);
            param.put("year", year);
            param.put("book_id", book_id);
            param.put("set_id", chr_code);
            param.put("is_input", data.getIs_input());
            csofAccDao.saveAccSupDep(param);
            param.put("end_id", Id);
            param.put("id", id);
            param.put("set_id", set_id);
            csofAccDao.updateAccSupEnd(param);
          } else {
            param.put("dep_task_id", dep_task_id);
            param.put("sid", sid);
            param.put("set_month", chr_name);
            param.put("set_id", chr_code);
            param.put("year", year);
            param.put("sup_num", Integer.parseInt(res.get("SUP_NUM").toString()) + data.getSup_num());
            param.put("sup_money",
              Integer.parseInt(res.get("SUP_MONEY").toString()) + Integer.parseInt(data.getSup_money()));//资金规模
            param.put("time_requirement", Integer.parseInt(res.get("TIME_REQUIREMENT").toString()) + days);//时间要求 
            param.put("sup_cost",
              Integer.parseInt(res.get("SUP_COST").toString()) + Integer.parseInt(data.getSup_cost())); //工作量
            csofAccDao.updateTaskAccSupDep(param);
            param.put("end_id", Id);
            param.put("set_id", set_id);
            param.put("id", res.get("ID").toString());
            csofAccDao.updateAccSupEnd(param);
          }
        } else {
          param.put("sid", sid);
          param.put("sup_type_id", sup_type_id);
          param.put("obj_type_id", obj_type_id);
          param.put("sup_name", sup_name);
          param.put("set_month", chr_name);
          param.put("year", year);
          param.put("set_id", chr_code);
          int num = csofAccDao.getAccCount(param);
          param.put("sid", sid);
          param.put("sup_type_id", sup_type_id);
          param.put("obj_type_id", obj_type_id);
          param.put("sup_name", sup_name);
          param.put("set_month", chr_name);
          param.put("year", year);
          param.put("set_id", chr_code);
          Map<String, Object> map = csofAccDao.getAccSupDep(param);
          if (num == 0) {
            param.put("id", id);
            param.put("sid", sid);
            param.put("dep_task_id", "");
            param.put("task_no", "");
            param.put("task_name", "");
            param.put("acc_id", data.getAcc_id());
            param.put("sup_no", data.getSup_no());
            param.put("sup_name", data.getSup_name());
            param.put("is_valid", 1);
            param.put("is_end", 0);
            param.put("oid", org_code);
            param.put("dep_id", dep_id);//汇总处室id
            param.put("dep_code", dep_code);
            param.put("dep_name", dep_name);
            param.put("sup_type_id", data.getSup_type_id());
            param.put("sup_type_code", data.getSup_type_code());
            param.put("sup_type_name", data.getSup_type_name());
            param.put("regulatory_area", "");//监管区域
            param.put("status", data.getStatus());
            param.put("work_type", data.getWork_type());
            param.put("regulatory_results", "");//监管成果
            param.put("feedback", "");//利用及反馈
            if (data.getWork_content() == null) {
              param.put("work_content", "");
            } else {
              param.put("work_content", data.getWork_content());
            }
            param.put("create_user", user_id);
            param.put("create_date", date);
            param.put("latest_op_user", user_id);
            param.put("latest_op_date", date);
            param.put("set_year", SessionUtilEx.getLoginYear());
            param.put("rg_code", SessionUtilEx.getRgCode());
            param.put("obj_type_name", data.getObj_type_name());
            param.put("obj_type_code", data.getObj_type_code());
            param.put("obj_type_id", data.getObj_type_id());
            param.put("sup_content", data.getSup_content());//监管内容
            param.put("sup_money", data.getSup_money());//资金规模
            param.put("time_requirement", days);//时间要求 
            param.put("sup_cost", data.getSup_cost()); //工作量
            if (data.getSup_object() == null) {
              param.put("sup_object", "");
            } else {
              param.put("sup_object", data.getSup_object());
            }
            if (data.getRemark() == null) {
              param.put("remark", "");
            } else {
              param.put("remark", data.getRemark());
            }
            param.put("set_month", chr_name);
            param.put("year", year);
            param.put("book_id", book_id);
            param.put("set_id", chr_code);
            param.put("is_input", data.getIs_input());
            csofAccDao.saveAccSupDep(param);
            param.put("end_id", Id);
            param.put("id", id);
            param.put("set_id", set_id);
            csofAccDao.updateAccSupEnd(param);
          } else {
            param.put("sid", sid);
            param.put("sup_type_id", sup_type_id);
            param.put("obj_type_id", obj_type_id);
            param.put("sup_name", sup_name);
            param.put("set_month", chr_name);
            param.put("set_id", chr_code);
            param.put("year", year);
            param.put("sup_num", Integer.parseInt(map.get("SUP_NUM").toString()) + data.getSup_num());
            param.put("sup_money",
              Integer.parseInt(map.get("SUP_MONEY").toString()) + Integer.parseInt(data.getSup_money()));//资金规模
            param.put("time_requirement", Integer.parseInt(map.get("TIME_REQUIREMENT").toString()) + days);//时间要求 
            param.put("sup_cost",
              Integer.parseInt(map.get("SUP_COST").toString()) + Integer.parseInt(data.getSup_cost())); //工作量
            csofAccDao.updateAccSupDep(param);
            param.put("end_id", Id);
            param.put("set_id", set_id);
            param.put("id", map.get("ID").toString());
            csofAccDao.updateAccSupEnd(param);
          }
        }

        if (!chr_name.equals("12")) {
          int month = Integer.parseInt(chr_name);
          int months = month + 1;
          String setMonth = String.valueOf(months);
          if (months < 10) {
            setMonth = "0" + setMonth;
          }
          param.put("set_month", setMonth);
          param.put("set_year", year);
          param.put("book_id", book_id);
          setId = csofAccDao.getSetId(param);
        } else {
          param.put("set_month", 01);
          param.put("set_year", year + 1);
          param.put("book_id", book_id);
          setId = csofAccDao.getSetId(param);
        }

        param.put("set_id", chr_code);
        param.put("is_close", 1);
        param.put("close_user", user_id);
        param.put("close_date", date);
        csofAccDao.updateAccBookBySetid(param);
        param.put("setId", setId);
        param.put("is_open", 1);
        param.put("open_user", user_id);
        param.put("open_date", date);
        csofAccDao.updateAccBookSet(param);
        //刷新book表的年月
        if (!chr_name.equals("12")) {
          int month = Integer.parseInt(chr_name);
          int months = month + 1;
          String setMonth = String.valueOf(months);
          if (months < 10) {
            setMonth = "0" + setMonth;
          }
          //刷新book表的年月
          param.put("set_month", setMonth);
          param.put("set_year", year);
          param.put("book_id", book_id);
          csofAccDao.updateDate(param);
        } else {
          param.put("set_month", 01);
          param.put("set_year", year + 1);
          param.put("book_id", book_id);
          csofAccDao.updateDate(param);
        }
        return true;
      } else {
        return false;
      }
    }

  }

  /**
   * 撤销汇总，通过sup_name,sup_type_id,set_month,year等条件删除数据
   * 
   */
  public boolean deleteAccSupDep(String acc_sup_dep_id, String sid, String set_id, String sup_type_id, String sup_name,
    String chr_code, String parent_id, String chr_name, String book_id, String dep_task_id, String Id, int is_input,
    String obj_type_id) {
    Map<String, Object> param = new HashMap<String, Object>();
    String year = csofAccDao.getChrCode(parent_id);
    String setId;
    if (Id.equals("")) {
      if (!chr_name.equals("12")) {
        int month = Integer.parseInt(chr_name);
        int months = month + 1;
        String setMonth = String.valueOf(months);
        if (months < 10) {
          setMonth = "0" + setMonth;
        }
        param.put("set_month", setMonth);
        param.put("set_year", year);
        param.put("book_id", book_id);
        setId = csofAccDao.getSetId(param);
      } else {
        param.put("set_month", 01);
        param.put("set_year", year + 1);
        param.put("book_id", book_id);
        setId = csofAccDao.getSetId(param);
      }
      param.put("set_id", chr_code);
      param.put("is_close", 0);
      param.put("close_user", "");
      param.put("close_date", "");
      csofAccDao.updateAccBookBySetid(param);
      param.put("setId", setId);
      param.put("is_open", 0);
      param.put("open_user", "");
      param.put("open_date", "");
      csofAccDao.updateAccBookSet(param);
      //刷新book表的年月
      param.put("set_month", chr_name);
      param.put("set_year", year);
      param.put("book_id", book_id);
      csofAccDao.updateDate(param);
      return true;
    } else {
      param.put("sid", sid);
      param.put("obj_type_id", obj_type_id);
      param.put("sup_type_id", sup_type_id);
      param.put("sup_name", sup_name);
      param.put("set_month", chr_name);
      param.put("year", year);
      param.put("set_id", chr_code);
      int num = csofAccDao.getAccCount(param);

      if (acc_sup_dep_id != null) {
        if (!chr_name.equals("12")) {
          int month = Integer.parseInt(chr_name);
          int months = month + 1;
          String setMonth = String.valueOf(months);
          if (months < 10) {
            setMonth = "0" + setMonth;
          }
          param.put("set_month", setMonth);
          param.put("set_year", year);
          param.put("book_id", book_id);
          setId = csofAccDao.getSetId(param);
        } else {
          param.put("set_month", 01);
          param.put("set_year", year + 1);
          param.put("book_id", book_id);
          setId = csofAccDao.getSetId(param);
        }
        param.put("set_id", chr_code);
        param.put("is_close", 0);
        param.put("close_user", "");
        param.put("close_date", "");
        csofAccDao.updateAccBookBySetid(param);
        param.put("setId", setId);
        param.put("is_open", 0);
        param.put("open_user", "");
        param.put("open_date", "");
        csofAccDao.updateAccBookSet(param);
        //刷新book表的年月
        param.put("set_month", chr_name);
        param.put("set_year", year);
        param.put("book_id", book_id);
        csofAccDao.updateDate(param);
        if (is_input == 0) {
          param.put("dep_task_id", dep_task_id);
          param.put("sid", sid);
          param.put("set_month", chr_name);
          param.put("year", year);
          param.put("set_id", chr_code);
          int sum = csofAccDao.getTaskAccCount(param);
          if (sum != 0) {
            param.put("end_id", Id);
            param.put("set_id", set_id);
            param.put("id", "");
            csofAccDao.updateAccSupEnd(param);
            param.put("dep_task_id", dep_task_id);
            param.put("sid", sid);
            param.put("set_month", chr_name);
            param.put("year", year);
            param.put("set_id", chr_code);
            csofAccDao.deleteTaskAccSupDep(param);
          } else {
            param.put("end_id", Id);
            param.put("set_id", set_id);
            param.put("id", "");
            csofAccDao.updateAccSupEnd(param);
          }
        } else {
          if (num != 0) {
            param.put("end_id", Id);
            param.put("set_id", set_id);
            param.put("id", "");
            csofAccDao.updateAccSupEnd(param);
            param.put("sid", sid);
            param.put("sup_type_id", sup_type_id);
            param.put("obj_type_id", obj_type_id);
            param.put("sup_name", sup_name);
            param.put("set_month", chr_name);
            param.put("year", year);
            param.put("set_id", chr_code);
            csofAccDao.deleteAccSupDep(param);
          } else {
            param.put("end_id", Id);
            param.put("set_id", set_id);
            param.put("id", "");
            csofAccDao.updateAccSupEnd(param);
          }
        }
        return true;
      } else {
        return false;
      }
    }
  }

  //台账办汇总
  /**
   * 台账办汇总树
   * @param book_id
   */
  public List getAccSupOfficeById(String book_id, String menu_id, String billtype_code) {
    Map<String, Object> param = new HashMap<String, Object>();
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String org_code = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    param.put("book_id", book_id);
    param.put("oid", org_code);
    param.put("menu_id", menu_id);
    return csofAccDao.getAccSupOfficeById(param);
  }

  /**
   * 点击树显示详细信息
   */
  public List getAccSupDepByBook(String oid, String chr_name, String parent_id) {
    Map<String, Object> param = new HashMap<String, Object>();
    String year = csofAccDao.getOfficeChrCode(parent_id);
    param.put("oid", oid);
    param.put("set_month", chr_name);
    param.put("year", year);
    return csofAccDao.getAccSupDepByBook(param);
  }

  /**
   * 点击树显示办汇总表信息
   */
  public List getAccSupOfficeBySetid(String book_id, String chr_name, String parent_id, String chr_code) {
    Map<String, Object> param = new HashMap<String, Object>();
    String year = csofAccDao.getOfficeChrCode(parent_id);
    param.put("book_id", book_id);
    param.put("set_month", chr_name);
    param.put("year", year);
    param.put("set_id", chr_code);
    return csofAccDao.getAccSupOfficeBySetid(param);
  }

  /**
   * 点击树查看处台账的数据
   */
  public List getAccSupDepBysetid(String book_id, String chr_code) {
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("chr_code", chr_code);
    return csofAccDao.getAccSupDepBysetid(param);
  }

  /**
   * 插入数据到办汇总表中
   * @throws Exception 
   */

  public boolean saveAccSupOffice(String acc_sup_office_id, String acc_sup_dep_id, String set_id, String sup_type_id,
    String sup_name, String chr_code, String parent_id, String book_id, String chr_name, String sid, String obj_type_id)
    throws Exception {
    Map<String, Object> param = new HashMap<String, Object>();
    String year = csofAccDao.getOfficeChrCode(parent_id);
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String org_code = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    String date = Tools.getCurrDate();
    String setId;
    if (acc_sup_dep_id.equals("")) {
      if (!chr_name.equals("12")) {
        int month = Integer.parseInt(chr_name);
        int months = month + 1;
        String setMonth = String.valueOf(months);
        if (months < 10) {
          setMonth = "0" + setMonth;
        }
        param.put("set_month", setMonth);
        param.put("set_year", year);
        param.put("book_id", book_id);
        setId = csofAccDao.getSetId(param);
      } else {
        param.put("set_month", 01);
        param.put("set_year", year + 1);
        param.put("book_id", book_id);
        setId = csofAccDao.getSetId(param);
      }
      param.put("set_id", chr_code);
      param.put("is_close", 1);
      param.put("close_user", user_id);
      param.put("close_date", date);
      csofAccDao.updateAccBookBySetid(param);
      param.put("setId", setId);
      param.put("is_open", 1);
      param.put("open_user", user_id);
      param.put("open_date", date);
      csofAccDao.updateAccBookSet(param);
      //刷新book表的年月
      if (!chr_name.equals("12")) {
        int month = Integer.parseInt(chr_name);
        int months = month + 1;
        String setMonth = String.valueOf(months);
        if (months < 10) {
          setMonth = "0" + setMonth;
        }
        //刷新book表的年月
        param.put("set_month", setMonth);
        param.put("set_year", year);
        param.put("book_id", book_id);
        csofAccDao.updateDate(param);
      } else {
        param.put("set_month", 01);
        param.put("set_year", year + 1);
        param.put("book_id", book_id);
        csofAccDao.updateDate(param);
      }
      return true;
    } else {
      param.put("acc_sup_dep_id", acc_sup_dep_id);
      param.put("set_id", set_id);
      csofAccSupDepEntity data = csofAccDao.FindAccSupOffice(param);

      String id = UUIDTools.uuidRandom(); // 自动生成id

      DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      Calendar calendar = new GregorianCalendar();
      if (acc_sup_office_id == null) {
        if (data.getIs_input() == 0) {
          param.put("sid", sid);
          param.put("set_month", chr_name);
          param.put("year", year);
          param.put("set_id", chr_code);
          int sum = csofAccDao.getTaskAccOfficeCount(param);
          param.put("sid", sid);
          param.put("set_month", chr_name);
          param.put("year", year);
          param.put("set_id", chr_code);
          Map<String, Object> res = csofAccDao.getTaskAccSupOffice(param);
          if (sum == 0) {
            param.put("id", id);
            param.put("sid", sid);
            param.put("sup_no", data.getSup_no());
            param.put("acc_id", data.getAcc_id());
            param.put("sup_name", data.getSup_name());
            param.put("is_valid", 1);
            param.put("is_end", 0);
            param.put("oid", org_code);
            param.put("sup_type_id", data.getSup_type_id());
            param.put("sup_type_code", data.getSup_type_code());
            param.put("sup_type_name", data.getSup_type_name());
            param.put("regulatory_area", "");//监管区域
            if (data.getWork_content() == null) {
              param.put("work_content", "");
            } else {
              param.put("work_content", data.getWork_content());
            }
            param.put("status", data.getStatus());
            param.put("work_type", data.getWork_type());
            param.put("regulatory_results", "");//监管成果
            param.put("feedback", "");//利用及反馈
            param.put("create_user", user_id);
            param.put("create_date", date);
            param.put("latest_op_user", user_id);
            param.put("latest_op_date", date);
            param.put("set_year", SessionUtilEx.getLoginYear());
            param.put("rg_code", SessionUtilEx.getRgCode());
            param.put("obj_type_id", data.getObj_type_id());
            param.put("obj_type_code", data.getObj_type_code());
            param.put("obj_type_name", data.getObj_type_name());
            param.put("sup_content", data.getSup_content());//监管内容
            param.put("sup_money", data.getSup_money());//资金规模
            param.put("time_requirement", data.getTime_requirement());//时间要求 
            param.put("sup_cost", data.getSup_cost()); //工作量
            if (data.getSup_object() == null) {
              param.put("sup_object", "");
            } else {
              param.put("sup_object", data.getSup_object());
            }
            if (data.getRemark() == null) {
              param.put("remark", "");
            } else {
              param.put("remark", data.getRemark());
            }
            param.put("set_month", chr_name);
            param.put("year", year);
            param.put("book_id", book_id);
            param.put("set_id", chr_code);
            param.put("is_input", data.getIs_input());
            csofAccDao.saveAccSupOffice(param);
            param.put("sid", acc_sup_dep_id);
            param.put("id", id);
            param.put("set_id", set_id);
            csofAccDao.motityAccSupDep(param);
          } else {
            param.put("sid", sid);
            param.put("set_month", chr_name);
            param.put("set_id", chr_code);
            param.put("year", year);
            param.put("sup_num", Integer.parseInt(res.get("SUP_NUM").toString()) + data.getSup_num());
            param.put("sup_money",
              Integer.parseInt(res.get("SUP_MONEY").toString()) + Integer.parseInt(data.getSup_money()));//资金规模
            param.put("time_requirement",
              Integer.parseInt(res.get("TIME_REQUIREMENT").toString()) + Integer.parseInt(data.getTime_requirement()));//时间要求 
            param.put("sup_cost",
              Integer.parseInt(res.get("SUP_COST").toString()) + Integer.parseInt(data.getSup_cost())); //工作量
            csofAccDao.updateTaskAccSupOffice(param);
            param.put("acc_sup_dep_id", acc_sup_dep_id);
            param.put("set_id", set_id);
            param.put("id", res.get("ID").toString());
            csofAccDao.motityAccSupDep(param);
          }
        } else {
          param.put("sid", sid);
          param.put("sup_type_id", sup_type_id);
          param.put("sup_name", sup_name);
          param.put("obj_type_id", obj_type_id);
          param.put("set_month", chr_name);
          param.put("year", year);
          param.put("set_id", chr_code);
          int num = csofAccDao.getAccOfficeCount(param);
          param.put("sid", sid);
          param.put("sup_type_id", sup_type_id);
          param.put("obj_type_id", obj_type_id);
          param.put("sup_name", sup_name);
          param.put("set_month", chr_name);
          param.put("year", year);
          param.put("set_id", chr_code);
          Map<String, Object> map = csofAccDao.getAccSupOffice(param);
          if (num == 0) {
            param.put("id", id);
            param.put("sid", "");
            param.put("sup_no", "");
            param.put("acc_id", data.getAcc_id());
            param.put("sup_name", data.getSup_name());
            param.put("is_valid", 1);
            param.put("is_end", 0);
            param.put("oid", org_code);
            param.put("sup_type_id", data.getSup_type_id());
            param.put("sup_type_code", data.getSup_type_code());
            param.put("sup_type_name", data.getSup_type_name());
            param.put("regulatory_area", "");//监管区域
            param.put("status", data.getStatus());
            param.put("work_type", data.getWork_type());
            param.put("regulatory_results", "");//监管成果
            param.put("feedback", "");//利用及反馈
            param.put("create_user", user_id);
            param.put("create_date", date);
            if (data.getWork_content() == null) {
              param.put("work_content", "");
            } else {
              param.put("work_content", data.getWork_content());
            }
            param.put("latest_op_user", user_id);
            param.put("latest_op_date", date);
            param.put("set_year", SessionUtilEx.getLoginYear());
            param.put("rg_code", SessionUtilEx.getRgCode());
            param.put("obj_type_id", data.getObj_type_id());
            param.put("obj_type_code", data.getObj_type_code());
            param.put("obj_type_name", data.getObj_type_name());
            param.put("sup_content", data.getSup_content());//监管内容
            param.put("sup_money", data.getSup_money());//资金规模
            param.put("time_requirement", data.getTime_requirement());//时间要求 
            param.put("sup_cost", data.getSup_cost()); //工作量
            if (data.getSup_object() == null) {
              param.put("sup_object", "");
            } else {
              param.put("sup_object", data.getSup_object());
            }
            if (data.getRemark() == null) {
              param.put("remark", "");
            } else {
              param.put("remark", data.getRemark());
            }
            param.put("set_month", chr_name);
            param.put("year", year);
            param.put("book_id", book_id);
            param.put("set_id", chr_code);
            param.put("is_input", data.getIs_input());
            csofAccDao.saveAccSupOffice(param);
            param.put("sid", acc_sup_dep_id);
            param.put("id", id);
            param.put("set_id", set_id);
            csofAccDao.motityAccSupDep(param);
          } else {
            param.put("sid", sid);
            param.put("sup_type_id", sup_type_id);
            param.put("obj_type_id", obj_type_id);
            param.put("sup_name", sup_name);
            param.put("set_month", chr_name);
            param.put("set_id", chr_code);
            param.put("year", year);
            param.put("sup_num", Integer.parseInt(map.get("SUP_NUM").toString()) + data.getSup_num());//资金规模
            param.put("sup_money",
              Integer.parseInt(map.get("SUP_MONEY").toString()) + Integer.parseInt(data.getSup_money()));//资金规模
            param.put("time_requirement",
              Integer.parseInt(map.get("TIME_REQUIREMENT").toString()) + Integer.parseInt(data.getTime_requirement()));//时间要求 
            param.put("sup_cost",
              Integer.parseInt(map.get("SUP_COST").toString()) + Integer.parseInt(data.getSup_cost())); //工作量
            csofAccDao.updateAccSupOffice(param);
            param.put("acc_sup_dep_id", acc_sup_dep_id);
            param.put("set_id", set_id);
            param.put("id", map.get("ID").toString());
            csofAccDao.motityAccSupDep(param);
          }
        }

        if (!chr_name.equals("12")) {
          int month = Integer.parseInt(chr_name);
          int months = month + 1;
          String setMonth = String.valueOf(months);
          if (months < 10) {
            setMonth = "0" + setMonth;
          }
          param.put("set_month", setMonth);
          param.put("set_year", year);
          param.put("book_id", book_id);
          setId = csofAccDao.getSetId(param);
        } else {
          param.put("set_month", 01);
          param.put("set_year", year + 1);
          param.put("book_id", book_id);
          setId = csofAccDao.getSetId(param);
        }
        param.put("set_id", chr_code);
        param.put("is_close", 1);
        param.put("close_user", user_id);
        param.put("close_date", date);
        csofAccDao.updateAccBookBySetid(param);
        param.put("setId", setId);
        param.put("is_open", 1);
        param.put("open_user", user_id);
        param.put("open_date", date);
        csofAccDao.updateAccBookSet(param);
        //刷新book表的年月
        if (!chr_name.equals("12")) {
          int month = Integer.parseInt(chr_name);
          int months = month + 1;
          String setMonth = String.valueOf(months);
          if (months < 10) {
            setMonth = "0" + setMonth;
          }
          //刷新book表的年月
          param.put("set_month", setMonth);
          param.put("set_year", year);
          param.put("book_id", book_id);
          csofAccDao.updateDate(param);
        } else {
          param.put("set_month", 01);
          param.put("set_year", year + 1);
          param.put("book_id", book_id);
          csofAccDao.updateDate(param);
        }
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * 撤销汇总，通过sup_name,sup_type_id,set_month,year等条件删除数据
   * @param param
   * @return
   */
  public boolean deleteAccSupOffice(String acc_sup_office_id, String acc_sup_dep_id, String set_id, String sup_type_id,
    String sup_name, String chr_code, String parent_id, String chr_name, String book_id, String sid, int is_intput,
    String obj_type_id) {
    Map<String, Object> param = new HashMap<String, Object>();
    String year = csofAccDao.getOfficeChrCode(parent_id);
    String setId;
    if (acc_sup_dep_id.equals("")) {
      if (!chr_name.equals("12")) {
        int month = Integer.parseInt(chr_name);
        int months = month + 1;
        String setMonth = String.valueOf(months);
        if (months < 10) {
          setMonth = "0" + setMonth;
        }
        param.put("set_month", setMonth);
        param.put("set_year", year);
        param.put("book_id", book_id);
        setId = csofAccDao.getSetId(param);
      } else {
        param.put("set_month", 01);
        param.put("set_year", year + 1);
        param.put("book_id", book_id);
        setId = csofAccDao.getSetId(param);
      }
      param.put("set_id", chr_code);
      param.put("is_close", 0);
      param.put("close_user", "");
      param.put("close_date", "");
      csofAccDao.updateAccBookBySetid(param);
      param.put("setId", setId);
      param.put("is_open", 0);
      param.put("open_user", "");
      param.put("open_date", "");
      csofAccDao.updateAccBookSet(param);
      //刷新book表的年月
      param.put("set_month", chr_name);
      param.put("set_year", year);
      param.put("book_id", book_id);
      csofAccDao.updateDate(param);
      return true;
    } else {
      param.put("sid", sid);
      param.put("sup_type_id", sup_type_id);
      param.put("obj_type_id", obj_type_id);
      param.put("sup_name", sup_name);
      param.put("set_month", chr_name);
      param.put("year", year);
      param.put("set_id", chr_code);
      int num = csofAccDao.getAccOfficeCount(param);

      if (acc_sup_office_id != null) {
        if (!chr_name.equals("12")) {
          int month = Integer.parseInt(chr_name);
          int months = month + 1;
          String setMonth = String.valueOf(months);
          if (months < 10) {
            setMonth = "0" + setMonth;
          }
          param.put("set_month", setMonth);
          param.put("set_year", year);
          param.put("book_id", book_id);
          setId = csofAccDao.getSetId(param);
        } else {
          param.put("set_month", 01);
          param.put("set_year", year + 1);
          param.put("book_id", book_id);
          setId = csofAccDao.getSetId(param);
        }
        param.put("set_id", chr_code);
        param.put("is_close", 0);
        param.put("close_user", "");
        param.put("close_date", "");
        csofAccDao.updateAccBookBySetid(param);
        param.put("setId", setId);
        param.put("is_open", 0);
        param.put("open_user", "");
        param.put("open_date", "");
        csofAccDao.updateAccBookSet(param);
        //刷新book表的年月
        param.put("set_month", chr_name);
        param.put("set_year", year);
        param.put("book_id", book_id);
        csofAccDao.updateDate(param);
        if (is_intput == 0) {
          param.put("sid", sid);
          param.put("set_month", chr_name);
          param.put("year", year);
          param.put("set_id", chr_code);
          int sum = csofAccDao.getTaskAccOfficeCount(param);
          if (sum != 0) {
            param.put("acc_sup_dep_id", acc_sup_dep_id);
            param.put("set_id", set_id);
            param.put("id", "");
            csofAccDao.motityAccSupDep(param);
            param.put("sid", sid);
            param.put("set_month", chr_name);
            param.put("year", year);
            param.put("set_id", chr_code);
            csofAccDao.deleteTaskAccSupOffice(param);
          } else {
            param.put("acc_sup_dep_id", acc_sup_dep_id);
            param.put("set_id", set_id);
            param.put("id", "");
            csofAccDao.motityAccSupDep(param);
          }
        } else {
          if (num != 0) {
            param.put("acc_sup_dep_id", acc_sup_dep_id);
            param.put("set_id", set_id);
            param.put("id", "");
            csofAccDao.motityAccSupDep(param);
            param.put("sid", sid);
            param.put("sup_type_id", sup_type_id);
            param.put("obj_type_id", obj_type_id);
            param.put("sup_name", sup_name);
            param.put("set_month", chr_name);
            param.put("year", year);
            param.put("set_id", chr_code);
            csofAccDao.deleteAccSupOffice(param);
          } else {
            param.put("acc_sup_dep_id", acc_sup_dep_id);
            param.put("set_id", set_id);
            param.put("id", "");
            csofAccDao.motityAccSupDep(param);
          }
        }
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * 得到台账菜单
   */
  public List getAccmenu(String user_id, String oid) {
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("user_id", user_id);
    param.put("oid", oid);
    return csofAccDao.getAccmenu(param);
  }

  /**
   * 查询未过账数据
   */
  public List getTaskUser(String user_id, String oid, FPaginationDTO page) {
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("user_id", user_id);
    param.put("oid", oid);
    param.put("startpage", page.getPagecount() * (page.getCurrpage() - 1));
    param.put("endpage", page.getPagecount() * (page.getCurrpage()));
    return csofAccDao.getTaskUser(param);
  }

  /**
   * 查询未过账数据数量
   */
  public int getTaskUserCount(String user_id, String oid) {
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("user_id", user_id);
    param.put("oid", oid);

    return csofAccDao.getTaskUserCount(param);
  }

  /***
   * 过账
   */
  public void saveTaskUser(String data, String book_id, String dep_id, String oid, String acc_id) {
    Map<String, Object> param = new HashMap<String, Object>();
    csofTaskUserEntity Data = JSONObject.parseObject(data, csofTaskUserEntity.class);
    Map<String, Object> map = csofAccDao.getAccSupByBookId(book_id);
    String id = UUIDTools.uuidRandom(); // 自动生成id
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String org_code = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    String date = Tools.getCurrDate();
    param.put("id", id);
    param.put("sid", Data.getSid());
    param.put("acc_id", acc_id);
    param.put("dep_task_id", Data.getId());
    param.put("task_no", Data.getTask_no());
    param.put("task_name", Data.getTask_name());
    param.put("book_id", book_id);
    param.put("set_id", map.get("ID").toString());
    param.put("set_month", map.get("SET_MONTH").toString());
    param.put("year", map.get("SET_YEAR").toString());
    param.put("work_content", "");
    param.put("sup_no", Data.getSup_no());
    param.put("sup_cost", Data.getSup_cost());
    param.put("sup_money", Data.getSup_money());
    param.put("sup_name", Data.getSup_name());
    param.put("status", 0);
    param.put("is_valid", 1);
    param.put("is_end", 0);
    param.put("mofdep_id", Data.getMofdep_id());
    param.put("mofdep_code", Data.getMofdep_code());
    param.put("mofdep_name", Data.getMofdep_name());
    param.put("oid", Data.getOid());
    param.put("dep_id", Data.getDep_id());
    param.put("dep_code", Data.getDep_code());
    param.put("dep_name", Data.getDep_name());
    param.put("sup_type_id", Data.getSup_type_id());
    param.put("sup_type_code", Data.getSup_type_code());
    param.put("sup_type_name", Data.getSup_type_name());
    param.put("obj_type_id", Data.getObj_type_id());
    param.put("obj_type_code", Data.getObj_type_code());
    param.put("obj_type_name", Data.getObj_type_name());
    param.put("is_allobj", Data.getIs_allobj());
    param.put("sup_num", Data.getSup_num());
    if (Data.getSup_object() == null) {
      param.put("sup_object", "");
    } else {
      param.put("sup_object", Data.getSup_object());
    }
    param.put("work_type", Data.getWork_type());
    param.put("sup_mode", Data.getSup_mode());
    param.put("sup_cycle", Data.getSup_cycle());
    param.put("start_date", Data.getSup_start_date());
    param.put("end_date", Data.getSup_end_date());
    param.put("sup_content", Data.getSup_content());
    if (Data.getSup_remark() == null) {
      param.put("remark", "");
    } else {
      param.put("remark", Data.getSup_remark());
    }
    param.put("create_user", user_id);
    param.put("create_date", date);
    param.put("latest_op_user", user_id);
    param.put("latest_op_date", date);
    param.put("set_year", Data.getSet_year());
    param.put("rg_code", Data.getRg_code());
    param.put("is_input", 0);
    csofAccDao.saveAccSupById(param);
    param.put("entity_id", Data.getId());
    param.put("book_id", book_id);
    param.put("table_name", "CSOF_TASK_BILL");
    param.put("acc_flag", 1);
    param.put("acc_date", date);
    param.put("acc_user", user_id);
    param.put("acc_user", dep_id);
    param.put("acc_user", oid);
    csofAccDao.saveAccLog(param);
  }

  /**
   * 查询acc要素数据
   */
  public List getCsofAcc(String ele_code, String book_id) {
    Map<String, Object> param = new HashMap<String, Object>();
    String ele_source = elementTreedao.findEleSource(ele_code);
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String org_code = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    param.put("ele_source", ele_source);
    param.put("user_id", user_id);
    param.put("oid", org_code);
    param.put("book_id", book_id);
    return csofAccDao.getCsofAcc(param);
  }

  /**
   * 通过acc_id 过滤树
   */
  public List getSupTreeByAccId(String ele_code, String chr_id) {
    String ele_source = elementTreedao.findEleSource(ele_code);
    Map<String, Object> param = new HashMap<String, Object>();
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    param.put("acc_id", chr_id);
    param.put("user_id", user_id);
    param.put("ele_source", ele_source);
    return csofAccDao.getSupTreeByAccId(param);
  }

  /**
   * 通过dep_id 查询该处室的人员
   */
  public List getUserNameByDepId(String dep_id) {
    Map<String, Object> param = new HashMap<String, Object>();
    String org_code = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    param.put("dep_id", dep_id);
    param.put("oid", org_code);
    return csofAccDao.getUserNameByDepId(param);
  }

  /**
   * 修改处汇总数据
   */
  public void updateAccSupDep(String data) throws IOException {
    Map<String, Object> param = new HashMap<String, Object>();
    csofAccSupDepEntity Data = JSONObject.parseObject(data, csofAccSupDepEntity.class);
    param.put("id", Data.getId());
    param.put("sup_name", Data.getSup_name());
    param.put("is_valid", Data.getIs_valid());
    param.put("is_end", Data.getIs_end());
    param.put("oid", Data.getOid());
    param.put("dep_id", Data.getDep_id());
    param.put("dep_code", Data.getDep_code());
    param.put("dep_name", Data.getDep_name());
    param.put("sup_type_id", Data.getSup_type_id());
    param.put("sup_type_code", Data.getSup_type_code());
    param.put("sup_type_name", Data.getSup_type_name());
    param.put("obj_type_name", Data.getObj_type_name());
    param.put("obj_type_id", Data.getObj_type_id());
    param.put("obj_type_code", Data.getObj_type_code());
    param.put("regulatory_area", Data.getRegulatory_area());
    param.put("sup_content", Data.getSup_content());
    param.put("time_requirement", Data.getTime_requirement());
    param.put("work_type", Data.getWork_type());
    param.put("status", Data.getStatus());
    param.put("regulatory_results", Data.getRegulatory_results());
    param.put("feedback", Data.getFeedback());
    param.put("remark", Data.getRemark());
    param.put("create_user", Data.getCreate_user());
    param.put("create_date", Data.getCreate_date());
    param.put("latest_op_user", Data.getLatest_op_user());
    param.put("latest_op_date", Data.getLatest_op_date());
    param.put("set_year", Data.getSet_year());
    param.put("rg_code", Data.getRg_code());
    param.put("year", Data.getYear());
    param.put("book_id", Data.getBook_id());
    param.put("set_id", Data.getSet_id());
    param.put("acc_sup_office_id", Data.getAcc_sup_office_id());
    param.put("acc_id", Data.getAcc_id());
    param.put("dep_task_id", Data.getDep_task_id());
    param.put("task_no", Data.getTask_no());
    param.put("task_name", Data.getTask_name());
    param.put("sid", Data.getSid());
    param.put("sup_no", Data.getSup_no());
    param.put("is_input", Data.getIs_input());
    param.put("sup_money", Data.getSup_money());
    param.put("sup_cost", Data.getSup_cost());
    param.put("sup_num", Data.getSup_num());
    param.put("sup_object", Data.getSup_object());
    param.put("set_month", Data.getSet_month());
    param.put("work_content", Data.getWork_content());
    csofAccDao.modityAccSupDep(param);
  }

  /**
   * 修改办汇总数据
   */
  public void updateAccSupOffice(String data) throws IOException {
    Map<String, Object> param = new HashMap<String, Object>();
    csofAccSupOfficeEntity Data = JSONObject.parseObject(data, csofAccSupOfficeEntity.class);
    param.put("id", Data.getId());
    param.put("sup_name", Data.getSup_name());
    param.put("oid", Data.getOid());
    param.put("sup_type_id", Data.getSup_type_id());
    param.put("sup_type_code", Data.getSup_type_code());
    param.put("sup_type_name", Data.getSup_type_name());
    param.put("obj_type_name", Data.getObj_type_name());
    param.put("obj_type_id", Data.getObj_type_id());
    param.put("obj_type_code", Data.getObj_type_code());
    param.put("regulatory_area", Data.getRegulatory_area());
    param.put("sup_content", Data.getSup_content());
    param.put("time_requirement", Data.getTime_requirement());
    param.put("work_type", Data.getWork_type());
    param.put("status", Data.getStatus());
    param.put("regulatory_results", Data.getRegulatory_results());
    param.put("feedback", Data.getFeedback());
    param.put("remark", Data.getRemark());
    param.put("create_user", Data.getCreate_user());
    param.put("create_date", Data.getCreate_date());
    param.put("latest_op_user", Data.getLatest_op_user());
    param.put("latest_op_date", Data.getLatest_op_date());
    param.put("set_year", Data.getSet_year());
    param.put("rg_code", Data.getRg_code());
    param.put("year", Data.getYear());
    param.put("book_id", Data.getBook_id());
    param.put("set_id", Data.getSet_id());
    param.put("acc_id", Data.getAcc_id());
    param.put("sid", Data.getSid());
    param.put("sup_no", Data.getSup_no());
    param.put("is_input", Data.getIs_input());
    param.put("sup_money", Data.getSup_money());
    param.put("sup_cost", Data.getSup_cost());
    param.put("sup_num", Data.getSup_num());
    param.put("sup_object", Data.getSup_object());
    param.put("set_month", Data.getSet_month());
    param.put("work_content", Data.getWork_content());
    csofAccDao.modityAccSupOffice(param);
  }

}
