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
 * CreateData: 2017-8-27下午3:43:07
 * </p>
 * 
 * @author songlr3
 * @version 1.0
 */
package gov.df.supervise.api.reportfprm;

import gov.df.supervise.bean.report.CsofEReportEntity;

import java.util.List;
import java.util.Map;

public interface ReportFormService {

  List<?> selectSupReportBySidBillcode(Map<String, Object> param);

  CsofEReportEntity selectEReportByChrId(String chrId);

  /**
   * 获取B报表URL头
   * @return
   * @author songlr3 at 2017-8-29下午3:42:56
   */
  String getBiValue();

  /**
   * 获取I报表URL头
   * @return
   * @author songlr3 at 2017-8-29下午3:42:59
   */
  String getIpValue();

  /**
   * 获取报表系统用户名
   * @return
   * @author songlr3 at 2017-8-29下午3:43:11
   */
  String getUser();

  /**
   * 获取报表系统用户名密码
   * @return
   * @author songlr3 at 2017-8-29下午3:43:16
   */
  String getPw();

  /**
   * 获取BU报表URL头
   * @return
   * @author songlr3 at 2017-8-29下午3:42:59
   */
  String getBuValue();

}
