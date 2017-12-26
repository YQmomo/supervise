package gov.df.supervise.service.Tree;

import gov.df.fap.service.util.sessionmanager.SessionUtil;
import gov.df.supervise.api.Tree.elementTreeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 列表树
 * @author tongya
 *
 */
@Service
public class elementTreeServiceimp implements elementTreeService {
  @Autowired
  private elementTreeDao elementTreedao;

  //初始化树列表
  public List initTree(String ele_code, String task_bill_id, int is_allobj, String obj_type_id) {
    Map<String, Object> param = new HashMap<String, Object>();
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String oid = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    String ele_source = elementTreedao.findEleSource(ele_code);
    param.put("ele_source", ele_source);
    param.put("ele_code", ele_code);
    param.put("user_id", user_id);
    param.put("oid", oid);
    param.put("task_bill_id", task_bill_id);
    param.put("is_allobj", is_allobj);
    param.put("obj_type_id", obj_type_id);
    return elementTreedao.findTree(param);
  }

  //获取要素的详细信息
  public List getElementData(String ele_code) {
    Map<String, Object> param = new HashMap<String, Object>();
    List data = null;
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String oid = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    String ele_source = elementTreedao.findEleSource(ele_code);
    Map<String, Object> map = elementTreedao.getElementByCode(ele_code);
    int IS_RIGHTFILTER = Integer.parseInt(map.get("IS_RIGHTFILTER").toString());
    int IS_LOCAL = Integer.parseInt(map.get("IS_LOCAL").toString());
    if (IS_RIGHTFILTER == 1 && IS_LOCAL == 1) {
      param.put("ele_source", ele_source);
      param.put("ele_code", ele_code);
      param.put("user_id", user_id);
      param.put("oid", oid);
      data = elementTreedao.getElementData(param);
    } else if (IS_RIGHTFILTER == 0) {
      param.put("ele_source", ele_source);
      data = elementTreedao.getElement(param);
    } else if (IS_RIGHTFILTER == 1 && IS_LOCAL == 0) {
      param.put("ele_source", ele_source);
      param.put("ele_code", ele_code);
      param.put("user_id", user_id);
      data = elementTreedao.getElementDataNo(param);
    }
    return data;
  }
}
