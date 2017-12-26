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
 * CreateData: 2017-8-25上午9:56:45
 * </p>
 * 
 * @author songlr3
 * @version 1.0
 */
package gov.df.supervise.service.workbench;

import gov.df.fap.service.util.sessionmanager.SessionUtil;
import gov.df.supervise.api.workbench.HomePageService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HomePageServiceImpl implements HomePageService {

  @Autowired
  private HomePageDao homePageDao;

  public List<?> selectToDo(String userId) {
    Map<String, Object> param = new HashMap<String, Object>();
    String org_code = (String) SessionUtil.getUserInfoContext().getOrgCode();//获取org_code作为专员办id(oid)
    param.put("oid", org_code);
    param.put("userId", userId);
    return homePageDao.selectToDo(param);
  }

  public List<?> selectGongaoTitle() {
    return homePageDao.selectGongaoTitle();
  }

  public List<?> selectGongaoById(String Id) {
    return homePageDao.selectGongaoById(Id);
  }

  /**
   * 查询公告
   */
  public List getBulletin() {

    return homePageDao.getBulletin();

  }

}
