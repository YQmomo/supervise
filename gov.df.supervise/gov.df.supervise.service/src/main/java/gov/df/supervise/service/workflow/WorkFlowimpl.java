package gov.df.supervise.service.workflow;

import gov.df.fap.service.util.sessionmanager.SessionUtil;
import gov.df.fap.util.Tools;
import gov.df.supervise.api.workflow.WorkFlow;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufgov.ip.apiUtils.UUIDTools;

@Service
public class WorkFlowimpl implements WorkFlow {
  @Autowired
  private workFlowDao workFlowdao;

  /**
   * 走工作流
   * @param user 用户对象
   * @param operationRemark 审核意见
   * @param dataList 数据对象
   * @param isAccount 是否记账进行额度生成/控制
   * @param autoCcid 是否自动生成CCID
   * @param autoRcid 是否自动生成RCID
  * @param inspectFlag 是否监控，false不监控，true监控
   * @return
   */
  public boolean doWorkFlow(String menu_id, String entity_id, String billtype_code, String op_type, String op_name,
    String opinion) {
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("menu_id", menu_id);
    param.put("billtype_code", billtype_code);
    Map<String, Object> data = workFlowdao.SelectMenuNode(param);
    String wf_id = data.get("WF_ID").toString();
    String cur_node_id = data.get("CUR_NODE_ID").toString();
    Map<String, Object> map = workFlowdao.SelectNode(cur_node_id);
    String node_type = map.get("NODE_TYPE").toString();
    String from_node_id = data.get("FROM_NODE_ID").toString();
    Map<String, Object> billdata = workFlowdao.selectTableName(billtype_code);
    String table_name = billdata.get("TABLE_NAME").toString();
    String billtype_name = billdata.get("BILLTYPE_NAME").toString();
    String op_date = Tools.getCurrDate();
    String trace_id = UUIDTools.uuidRandom(); // 自动生成id
    String op_user = (String) SessionUtil.getUserInfoContext().getUserID();
    if (op_type != null && op_type.equals("INPUT")) {
      int sum = workFlowdao.getCount(entity_id);
      if (sum == 0) {
        String id = UUIDTools.uuidRandom(); // 自动生成id
        param.put("id", id);
        param.put("wf_id", wf_id);
        param.put("billtype_code", billtype_code);
        param.put("table_name", table_name);
        param.put("entity_id", entity_id);
        param.put("node_id", from_node_id);
        param.put("status_code", "1");
        param.put("op_user", op_user);
        param.put("op_date", op_date);
        param.put("counter", 1);
        workFlowdao.InsertWorkFlow(param);
        param.put("id", trace_id);
        param.put("wf_id", wf_id);
        param.put("billtype_code", billtype_code);
        param.put("billtype_name", billtype_name);
        param.put("table_name", table_name);
        param.put("entity_id", entity_id);
        param.put("pre_node_id", from_node_id);
        param.put("pre_node_status", "1");
        param.put("cur_node_id", from_node_id);
        param.put("cur_node_status", "1");
        param.put("menu_id", menu_id);
        param.put("op_type", op_type);
        param.put("op_name", op_name);
        param.put("op_user", op_user);
        param.put("op_date", op_date);
        param.put("counter", 1);
        param.put("opinion", opinion);
        workFlowdao.InsertWorkTrace(param);
      } else {
        Map<String, Object> res = workFlowdao.getWFCurItem(entity_id);
        String pre_node_id = res.get("NODE_ID").toString();
        String pre_node_status = res.get("STATUS_CODE").toString();
        int counter = Integer.parseInt(res.get("COUNTER").toString());
        param.put("entity_id", entity_id);
        param.put("node_id", from_node_id);
        param.put("status_code", "1");
        param.put("op_user", op_user);
        param.put("op_date", op_date);
        param.put("counter", counter);
        workFlowdao.updateWorkFlow(param);
        param.put("id", trace_id);
        param.put("wf_id", wf_id);
        param.put("billtype_code", billtype_code);
        param.put("billtype_name", billtype_name);
        param.put("table_name", table_name);
        param.put("entity_id", entity_id);
        param.put("pre_node_id", pre_node_id);
        param.put("pre_node_status", pre_node_status);
        param.put("cur_node_id", from_node_id);
        param.put("cur_node_status", "1");
        param.put("menu_id", menu_id);
        param.put("op_type", op_type);
        param.put("op_name", op_name);
        param.put("op_user", op_user);
        param.put("op_date", op_date);
        param.put("counter", counter + 1);
        param.put("opinion", opinion);
        workFlowdao.InsertWorkTrace(param);
      }
      // param.put("entity_id", entity_id);
      // param.put("table_name", table_name);
      //workFlowdao.updateStatus1(param);
      param.put("entity_id", entity_id);
      param.put("table_name", table_name);
      param.put("is_end", 0);
      workFlowdao.updateIsEnd(param);
      return true;
    } else if (op_type != null && op_type.equals("NEXT")) {
      Map<String, Object> res = workFlowdao.getWFCurItem(entity_id);
      String pre_node_id = res.get("NODE_ID").toString();
      String pre_node_status = res.get("STATUS_CODE").toString();
      int counter = Integer.parseInt(res.get("COUNTER").toString());
      param.put("entity_id", entity_id);
      param.put("node_id", cur_node_id);
      param.put("status_code", "1");
      param.put("op_user", op_user);
      param.put("op_date", op_date);
      param.put("counter", counter);
      workFlowdao.updateWorkFlow(param);
      param.put("id", trace_id);
      param.put("wf_id", wf_id);
      param.put("billtype_code", billtype_code);
      param.put("billtype_name", billtype_name);
      param.put("table_name", table_name);
      param.put("entity_id", entity_id);
      param.put("pre_node_id", pre_node_id);
      param.put("pre_node_status", pre_node_status);
      param.put("cur_node_id", cur_node_id);
      param.put("cur_node_status", "1");
      param.put("menu_id", menu_id);
      param.put("op_type", op_type);
      param.put("op_name", op_name);
      param.put("op_user", op_user);
      param.put("op_date", op_date);
      param.put("counter", counter + 1);
      param.put("opinion", opinion);
      workFlowdao.InsertWorkTrace(param);
      //      param.put("entity_id", entity_id);
      //      param.put("table_name", table_name);
      //      workFlowdao.updateStatus1(param);
      return true;
    } else if (op_type != null && op_type.equals("END")) {
      int sum = workFlowdao.getCountEnd(entity_id);
      if (sum == 0) {
        String id = UUIDTools.uuidRandom(); // 自动生成id
        Map<String, Object> res = workFlowdao.getWFCurItem(entity_id);
        String pre_node_id = res.get("NODE_ID").toString();
        String pre_node_status = res.get("STATUS_CODE").toString();
        int counter = Integer.parseInt(res.get("COUNTER").toString());
        param.put("id", id);
        param.put("wf_id", wf_id);
        param.put("billtype_code", billtype_code);
        param.put("table_name", table_name);
        param.put("entity_id", entity_id);
        param.put("node_id", cur_node_id);
        param.put("status_code", 1);
        param.put("op_user", op_user);
        param.put("op_date", op_date);
        param.put("counter", counter + 1);
        workFlowdao.InsertWorkFlowEnd(param);
        param.put("id", trace_id);
        param.put("wf_id", wf_id);
        param.put("billtype_code", billtype_code);
        param.put("billtype_name", billtype_name);
        param.put("table_name", table_name);
        param.put("entity_id", entity_id);
        param.put("pre_node_id", pre_node_id);
        param.put("pre_node_status", pre_node_status);
        param.put("cur_node_id", cur_node_id);
        param.put("cur_node_status", 1);
        param.put("menu_id", menu_id);
        param.put("op_type", op_type);
        param.put("op_name", op_name);
        param.put("op_user", op_user);
        param.put("op_date", op_date);
        param.put("counter", counter + 1);
        param.put("opinion", opinion);
        workFlowdao.InsertWorkTrace(param);
        //      param.put("entity_id", entity_id);
        //      param.put("table_name", table_name);
        //      workFlowdao.updateStatus1(param);
        param.put("entity_id", entity_id);
        param.put("table_name", table_name);
        param.put("is_end", 1);
        workFlowdao.updateIsEnd(param);
        workFlowdao.deleteWFCurItem(entity_id);
      }
      return true;
    } else if (op_type != null && op_type.equals("BACK")) {
      Map<String, Object> res = workFlowdao.getWFCurItem(entity_id);
      String pre_node_id = res.get("NODE_ID").toString();
      String pre_node_status = res.get("STATUS_CODE").toString();
      int counter = Integer.parseInt(res.get("COUNTER").toString());
      param.put("entity_id", entity_id);
      param.put("node_id", from_node_id);
      param.put("status_code", 0);
      param.put("op_user", op_user);
      param.put("op_date", op_date);
      param.put("counter", counter);
      workFlowdao.updateWorkFlow(param);
      param.put("id", trace_id);
      param.put("wf_id", wf_id);
      param.put("billtype_code", billtype_code);
      param.put("billtype_name", billtype_name);
      param.put("table_name", table_name);
      param.put("entity_id", entity_id);
      param.put("pre_node_id", pre_node_id);
      param.put("pre_node_status", pre_node_status);
      param.put("cur_node_id", from_node_id);
      param.put("cur_node_status", 0);
      param.put("menu_id", menu_id);
      param.put("op_type", op_type);
      param.put("op_name", op_name);
      param.put("op_user", op_user);
      param.put("op_date", op_date);
      param.put("counter", counter + 1);
      param.put("opinion", opinion);
      workFlowdao.InsertWorkTrace(param);
      //      param.put("entity_id", entity_id);
      //      param.put("table_name", table_name);
      //      workFlowdao.updateStatus2(param);
      return true;
    } else if (op_type != null && op_type.equals("UNDO")) {
      int sum = workFlowdao.getCountEnd(entity_id);
      if (sum != 0) {
        if (!node_type.equals("") && node_type.equals("2")) {
          String id = UUIDTools.uuidRandom(); // 自动生成id
          Map<String, Object> res = workFlowdao.getWFEndItem(entity_id);
          String pre_node_id = res.get("NODE_ID").toString();
          String pre_node_status = res.get("STATUS_CODE").toString();
          int counter = Integer.parseInt(res.get("COUNTER").toString());
          param.put("id", id);
          param.put("wf_id", wf_id);
          param.put("billtype_code", billtype_code);
          param.put("table_name", table_name);
          param.put("entity_id", entity_id);
          param.put("node_id", from_node_id);
          param.put("status_code", 1);
          param.put("op_user", op_user);
          param.put("op_date", op_date);
          param.put("counter", counter + 1);
          workFlowdao.InsertWorkFlow(param);
          param.put("id", trace_id);
          param.put("wf_id", wf_id);
          param.put("billtype_code", billtype_code);
          param.put("billtype_name", billtype_name);
          param.put("table_name", table_name);
          param.put("entity_id", entity_id);
          param.put("pre_node_id", pre_node_id);
          param.put("pre_node_status", pre_node_status);
          param.put("cur_node_id", from_node_id);
          param.put("cur_node_status", 1);
          param.put("menu_id", menu_id);
          param.put("op_type", op_type);
          param.put("op_name", op_name);
          param.put("op_user", op_user);
          param.put("op_date", op_date);
          param.put("counter", counter + 1);
          param.put("opinion", opinion);
          workFlowdao.InsertWorkTrace(param);
          workFlowdao.deleteWFEndItem(entity_id);
          param.put("entity_id", entity_id);
          param.put("table_name", table_name);
          param.put("is_end", 0);
          workFlowdao.updateIsEnd(param);
        }
      } else {
        Map<String, Object> res = workFlowdao.getWFCurItem(entity_id);
        String pre_node_id = res.get("NODE_ID").toString();
        String pre_node_status = res.get("STATUS_CODE").toString();
        int counter = Integer.parseInt(res.get("COUNTER").toString());
        param.put("entity_id", entity_id);
        param.put("node_id", from_node_id);
        param.put("status_code", 1);
        param.put("op_user", op_user);
        param.put("op_date", op_date);
        param.put("counter", counter);
        workFlowdao.updateWorkFlow(param);
        param.put("id", trace_id);
        param.put("wf_id", wf_id);
        param.put("billtype_code", billtype_code);
        param.put("billtype_name", billtype_name);
        param.put("table_name", table_name);
        param.put("entity_id", entity_id);
        param.put("pre_node_id", pre_node_id);
        param.put("pre_node_status", pre_node_status);
        param.put("cur_node_id", from_node_id);
        param.put("cur_node_status", 1);
        param.put("menu_id", menu_id);
        param.put("op_type", op_type);
        param.put("op_name", op_name);
        param.put("op_user", op_user);
        param.put("op_date", op_date);
        param.put("counter", counter + 1);
        param.put("opinion", opinion);
        workFlowdao.InsertWorkTrace(param);
      }
      return true;
    } else if (op_type != null && op_type.equals("DEL")) {
      int sum = workFlowdao.getCount(entity_id);
      if (sum != 0) {
        Map<String, Object> res = workFlowdao.getWFCurItem(entity_id);
        String pre_node_id = res.get("NODE_ID").toString();
        String pre_node_status = res.get("STATUS_CODE").toString();
        int counter = Integer.parseInt(res.get("COUNTER").toString());
        param.put("id", res.get("ID").toString());
        param.put("wf_id", res.get("WF_ID").toString());
        param.put("billtype_code", res.get("BILLTYPE_CODE").toString());
        param.put("table_name", res.get("TABLE_NAME").toString());
        param.put("entity_id", res.get("ENTITY_ID").toString());
        param.put("node_id", res.get("NODE_ID").toString());
        param.put("status_code", res.get("STATUS_CODE").toString());
        param.put("op_user", res.get("OP_USER").toString());
        param.put("op_date", res.get("OP_DATE").toString());
        param.put("counter", res.get("COUNTER").toString());
        workFlowdao.InsertWorkFlowDel(param);
        workFlowdao.deleteWFCurItem(entity_id);
        param.put("id", trace_id);
        param.put("wf_id", wf_id);
        param.put("billtype_code", billtype_code);
        param.put("billtype_name", billtype_name);
        param.put("table_name", table_name);
        param.put("entity_id", entity_id);
        param.put("pre_node_id", pre_node_id);
        param.put("pre_node_status", pre_node_status);
        param.put("cur_node_id", from_node_id);
        param.put("cur_node_status", 1);
        param.put("menu_id", menu_id);
        param.put("op_type", op_type);
        param.put("op_name", op_name);
        param.put("op_user", op_user);
        param.put("op_date", op_date);
        param.put("counter", counter + 1);
        param.put("opinion", opinion);
        workFlowdao.InsertWorkTrace(param);
      }
    }
    return false;

  }
}
