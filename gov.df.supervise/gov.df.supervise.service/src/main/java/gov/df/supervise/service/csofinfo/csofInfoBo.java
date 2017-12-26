package gov.df.supervise.service.csofinfo;

import gov.df.fap.service.util.sessionmanager.SessionUtil;
import gov.df.supervise.api.csofinfo.csofInfoService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gov.df.supervice.util.SuperviseToolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 信息档案库
 * @author tongya
 *
 */
@Service
public class csofInfoBo implements csofInfoService {
  @Autowired
  private csofInfoDao csofInfodao;

  //获取库的动态页签
  public List getDisplayTitle(String chr_id) {
    return csofInfodao.getDisplayTitle(chr_id);
  }

  //获取期间
  public List getYear(String ele_code) {
    Map<String, String> param = new HashMap<String, String>();
    String ele_source = csofInfodao.findEleSource(ele_code);
    param.put("ele_source", ele_source);
    return csofInfodao.findYear(param);
  }

  //获取列表树
  public List getTreeByuser(String ele_code) {
    Map<String, String> param = new HashMap<String, String>();
    String user_id = (String) SessionUtil.getUserInfoContext().getUserID();
    String ele_source = csofInfodao.findEleSource(ele_code);
    String elecode = ele_code;
    if (ele_code.equals("CSOF_MOFAGENCY")) {
      elecode = "CSOF_AGENCY";
    }
    param.put("ele_source", ele_source);
    param.put("ele_code", elecode);
    param.put("user_id", user_id);
    return csofInfodao.findDataByuser(param);
  }

  //通用获取要素信息
  public List getData(String ele_code) {
    Map<String, String> param = new HashMap<String, String>();
    String ele_source = csofInfodao.findEleSource(ele_code);
    if(SuperviseToolUtil.sql_inj(ele_source)){
        param.put("ele_source", ele_source);
    }else{
        return null;
    }
    return csofInfodao.findData(param);
  }
}
