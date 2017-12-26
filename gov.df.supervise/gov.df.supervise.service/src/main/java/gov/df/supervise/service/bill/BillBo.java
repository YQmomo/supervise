package gov.df.supervise.service.bill;

import gov.df.fap.service.util.dao.GeneralDAO;
import gov.df.fap.service.util.gl.core.CommonUtil;
import gov.df.fap.service.util.sessionmanager.SessionUtil;
import gov.df.supervise.api.bill.BillService;
import gov.df.supervise.api.common.CommonService;
import gov.df.supervise.api.workflow.WorkFlow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ufgov.ip.apiUtils.UUIDTools;

@Service
public class BillBo implements BillService {

  @Autowired
  @Qualifier("generalDAO")
  GeneralDAO dao;

  @Autowired
  private CommonService commonService; // 监管事项服务

  @Autowired
  private WorkFlow workflow;

  public int deleteMofBill(String ids, String key) {
    String condition = "";
    String value = "";
    if (!ids.equals("")) {
      String[] values = ids.split(",");
      for (int i = 0; i < values.length; i++) {
        value += "'" + values[i] + "',";
      }
      value = value.substring(0, value.lastIndexOf(","));
      condition = " and " + key + " in (" + ids + ")";
    }
    return commonService.deleteDataList("CSOF_MOF_BILL", condition);
  }

  /**
   * 总任务单保存方法
   */
  @Transactional(readOnly = false)
  public int saveMofBill(List officeList, List supList, String billtype_code, Map workMap) {
    List sqlList = new ArrayList<String>();

    String ids = "";
    for (int i = 0; i < supList.size(); i++) {
      Map supData = (Map) supList.get(i);

      for (int j = 0; j < officeList.size(); j++) {
        Map officeData = (Map) officeList.get(j);

        String rg_code = CommonUtil.getRgCode();
        String set_year = CommonUtil.getSetYear();
        String create_user = CommonUtil.getUserId();
        String date = CommonUtil.getDate("yyyy-MM-dd");
        Map<String, String> mofData = new HashMap<String, String>();
        String id = UUIDTools.uuidRandom();
        ids += id + ",";
        mofData.put("id", id);
        mofData.put("sid", supData.get("CHR_ID").toString());
        mofData.put("oid", officeData.get("CHR_ID").toString());
        mofData.put("rg_code", rg_code);
        mofData.put("set_year", set_year);
        mofData.put("create_date", date);
        mofData.put("latest_op_date", date);
        mofData.put("create_user", create_user);
        mofData.put("latest_op_user", create_user);

        Map billInfo = commonService.getBillInfo(billtype_code, mofData);
        if (!billInfo.isEmpty()) {
          mofData.put("bill_no", billInfo.get("bill_no").toString());
          mofData.put("billtype_code", billInfo.get("billtype_code").toString());
          mofData.put("billtype_name", billInfo.get("billtype_name").toString());
        }
        String sql = commonService.getInsertSql(mofData, "csof_mof_bill");
        sqlList.add(sql);

      }

    }

    int[] result = dao.executeBatchBySql(sqlList);
    if (!ids.equals("")) {
      ids = ids.substring(0, ids.length() - 1);
      workMap.put("billtype_code", billtype_code);
      boolean workFlag = commonService.saveWorkFlow(workMap, ids.split(","));
    }
    return result.length;
  }

  public int updateMofBill(Map mofBillData, String billtype_code) {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
     * 任务下达
     * @param ids  本方法适应   批量
     * @return
     */
  public int approveTask(String ids, Map workMap) {
    String approve_user = CommonUtil.getUserCode();
    String approve_date = CommonUtil.getDate("yyyy-MM-dd HH:mm:ss");
    String condition = "";
    String value = "";
    if (ids.contains(",")) {
      String[] values = ids.split(",");
      for (int i = 0; i < values.length; i++) {
        value += "'" + values[i] + "'";
      }
      condition = " WHERE ID IN (" + value + ")";
    } else {
      condition = " WHERE ID = '" + ids + "'";
    }

    String sql = "UPDATE CSOF_MOF_BILL SET APPROVE_USER = '" + approve_user + "' , APPROVE_DATE = '" + approve_date
      + "' " + condition;

    ids = ids.replace("'", "");
    boolean workFlag = commonService.saveWorkFlow(workMap, ids.split(","));
    return dao.executeBySql(sql);
  }

  /**
     * 任务登记
     * @param ids  本方法适应   批量
     * @return
     */
  public int receiveTask(String ids, Map workMap) {
    String approve_user = CommonUtil.getUserCode();
    String approve_date = CommonUtil.getDate("yyyy-MM-dd HH:mm:ss");
    String condition = "";
    String value = "";
    if (ids.contains(",")) {
      String[] values = ids.split(",");
      for (int i = 0; i < values.length; i++) {
        value += "'" + values[i] + "'";
      }
      condition = " WHERE ID IN (" + value + ")";
    } else {
      condition = " WHERE ID = '" + ids + "'";
    }
    String sql = "UPDATE CSOF_MOF_BILL SET RECEIVE_USER = '" + approve_user + "' , RECEIVE_DATE = '" + approve_date
      + "' " + condition;

    ids = ids.replace("'", "");
    boolean workFlag = commonService.saveWorkFlow(workMap, ids.split(","));
    return dao.executeBySql(sql);
  }

  /**
   * 
   */
  @Transactional(readOnly = false)
  public int saveTaskBill(List objectList, List agencyList, Map mofData, List taskList, Map mofWorkMap, Map taskWorkMap) {

    String rg_code = CommonUtil.getRgCode();
    String set_year = CommonUtil.getSetYear();
    String user_id = CommonUtil.getUserId();
    String date = CommonUtil.getDate("yyyy-MM-dd");
    String taskIds = "";
    List taskBillSql = new ArrayList();
    String isAll = mofData.get("IS_ALLOBJ").toString();
    Map sumMap = saveTaskObject(isAll, agencyList, mofData);

    for (int i = 0; i < objectList.size(); i++) {
      Map obj = (Map) objectList.get(i);

      for (int j = 0; j < taskList.size(); j++) {
        Map task = (Map) taskList.get(j);
        String no = task.get("TASK_NO").toString() + "-" + obj.get("CHR_CODE").toString()
          + task.get("DETAIL_NO").toString();
        Map<String, String> taskBill = new HashMap<String, String>();
        String dep_id = obj.get("CHR_ID").toString();
        String task_id = UUIDTools.uuidRandom();
        taskIds += task_id + ",";
        taskBill.put("ID", task_id);
        taskBill.put("TASK_NO", no);
        taskBill.put("TASK_NAME", task.get("TASK_NAME").toString());
        taskBill.put("IS_ALLOBJ", isAll);
        taskBill.put("DEP_ID", dep_id);
        taskBill.put("DEP_CODE", obj.get("CHR_CODE").toString());
        taskBill.put("DEP_NAME", obj.get("CHR_NAME").toString());
        taskBill.put("BILLTYPE_CODE", taskWorkMap.get("billtype_code").toString());
        taskBill.put("PLAN_BEGIN_DATE", task.get("PLAN_BEGIN_DATE").toString());
        taskBill.put("PLAN_END_DATE", task.get("PLAN_END_DATE").toString());
        taskBill.put("PLAN_COST", task.get("PLAN_COST").toString());
        taskBill.put("SUP_NUM", sumMap.get(dep_id) != null ? sumMap.get(dep_id).toString() : "0");
        taskBill.put("OID", obj.get("OID").toString());
        taskBill.put("SID", mofData.get("SID").toString());
        taskBill.put("MOF_BILL_ID", mofData.get("ID").toString());
        taskBill.put("REMARK", task.get("REMARK").toString());
        taskBill.put("RG_CODE", rg_code);
        taskBill.put("SET_YEAR", set_year);
        taskBill.put("CREATE_USER", user_id);
        taskBill.put("CREATE_DATE", date);
        taskBill.put("LATEST_OP_USER", user_id);
        taskBill.put("LATEST_OP_DATE", date);
        taskBill.put("BILL_NO", no);
        taskBillSql.add(commonService.getInsertSql(taskBill, "csof_task_bill"));
      }
    }

    sumMap.get("mof_sum");

    //保存分解的处室任务csof_task_bill
    dao.executeBatchBySql(taskBillSql);

    //处室任csof_task_bill务保存至工作流
    commonService.saveWorkFlow(taskWorkMap, taskIds.split(","));

    //总任务单csof_mof_bill流程进入下一步
    commonService.saveWorkFlow(mofWorkMap, new String[] { mofData.get("id").toString() });

    return 0;
  }

  /**
   * 保存监管单位授权数据,计算监管单位数量保存至总任务单csof_mof_bill
   * @param isAll  是否全部监管单位 0:部分 1:全部
   * @param agencyList 授权监管单位树
   * @param mofBill 总任务单csof_mof_bill
   * @return
   */
  @Transactional(readOnly = false)
  public Map saveTaskObject(String isAll, List agencyList, Map mofBill) {
    int result_num = 0;
    int mof_sum = 0; //总任务单的监管数量合计
    int dep_sum = 0; //处室监管单位数量合计
    Map sumMap = new HashMap<String, Integer>(); //用来计算处室所监管的单位数量
    String oid = SessionUtil.getUserInfoContext().getOrgCode();

    if (agencyList.size() == 0) {
      agencyList = commonService.getDataList("vw_csof_e_agency", " and IS_LEAF=1 and ENABLED=1 and OID='" + oid + "'",
        false, "");
    }

    if (isAll.equals("0")) {
      List sqlList = new ArrayList(); //单位树批量保存的sql集合

      for (int i = 0; i < agencyList.size(); i++) {
        Map agency = (Map) agencyList.get(i);
        Map<String, String> objectMap = new HashMap<String, String>();
        String type = agency.get("TYPE").toString();
        String is_leaf = String.valueOf(agency.get("IS_LEAF"));
        String dep_id = agency.get("DEP_ID").toString();

        objectMap.put("TYPE", type);
        objectMap.put("DEP_ID", dep_id);
        objectMap.put("IS_LEAF", is_leaf);
        objectMap.put("OID", agency.get("OID").toString());
        objectMap.put("SID", mofBill.get("SID").toString());
        objectMap.put("MOF_BILL_ID", mofBill.get("ID").toString());
        objectMap.put("OBJECT_ID", agency.get("CHR_ID").toString());
        objectMap.put("OBJ_TYPE_ID", mofBill.get("obj_type_id").toString());
        objectMap.put("OBJ_TYPE_CODE", mofBill.get("obj_type_code").toString());
        objectMap.put("OBJ_TYPE_NAME", mofBill.get("obj_type_name").toString());

        if (type.equals("AGENCY") && is_leaf.equals("1")) {
          mof_sum++;
          String depSum = sumMap.get(dep_id) != null ? sumMap.get(dep_id).toString() : "0";
          sumMap.put(dep_id, Integer.parseInt(depSum) + 1);
        }

        sqlList.add(commonService.getInsertSql(objectMap, "csof_task_object"));
      }
      String sql = "  update csof_mof_bill set IS_ALLOBJ = 0 where id = '" + mofBill.get("ID") + "'";
      dao.executeBySql(sql);
      dao.executeBatchBySql(sqlList);
    } else {
      //全部监管机构，计算监管单位数量
      mof_sum = agencyList.size();

      for (int i = 0; i < agencyList.size(); i++) {
        Map agency = (Map) agencyList.get(i);
        String dep_id = agency.get("dep_id").toString();
        String depSum = sumMap.get(dep_id) != null ? sumMap.get(dep_id).toString() : "0";
        sumMap.put(agency.get("dep_id"), Integer.parseInt(depSum) + 1);
      }
    }
    sumMap.put("mof_sum", mof_sum);

    return sumMap;
  }

  /**
   * 撤销分解
   */
  @Transactional(readOnly = false)
  public int deleteTaskBill(Map mofData, Map mofWorkMap, Map taskWorkMap) {

    String mof_bill_id = mofData.get("id").toString();
    String is_all = mofData.get("is_allobj").toString();
    String condition = " and mof_bill_id = '" + mof_bill_id + "'";
    if (is_all.equals("0")) {
      //部分监管，才需要删除授权关联表信息
      commonService.deleteDataList("csof_task_object", condition);
    }
    commonService.deleteDataList("csof_task_bill", condition);
    List taskList = commonService.getDataList("csof_task_bill", condition, false, "");
    for (int i = 0; i < taskList.size(); i++) {
      Map task = (Map) taskList.get(i);
      taskWorkMap.put("entity_id", task.get("id").toString());
      commonService.doWorkFlow(taskWorkMap);
    }

    commonService.doWorkFlow(mofWorkMap);

    return 0;
  }

  /**
     * 处室任务发布
     * @param is_put
     * @return
     */
  @Transactional(readOnly = false)
  public int publishTask(String id, Map workMap) {
    String publish_user = CommonUtil.getUserCode();
    String publish_date = CommonUtil.getDate("yyyy-MM-dd HH:mm:ss");
    String sql = "update csof_task_bill set is_pub = 1 , publish_user='" + publish_user + "' , publish_date ='"
      + publish_date + "'  where id = '" + id + "'";
    boolean doWork = commonService.doWorkFlow(workMap);
    int result = 0;
    if (doWork) {
      result = dao.executeBySql(sql);
    }
    return result;
  }

  /**
   * 撤销发布
   */
  @Transactional(readOnly = false)
  public int unpublishTask(String id, Map workMap) {
    int result = 0;
    String sql = "update csof_task_bill set is_pub = 0 , publish_user='' , publish_date =''  where id = '" + id + "' ";
    boolean doWork = commonService.doWorkFlow(workMap);
    if (doWork) {
      result = dao.executeBySql(sql);
    }
    return result;
  }

}
