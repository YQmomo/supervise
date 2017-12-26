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
 * CreateData: 2017-9-4下午3:05:13
 * </p>
 * 
 * @author songlr3
 * @version 1.0
 */
package gov.df.supervise.api.itemsupervision;

import java.util.List;
import java.util.Map;

public interface ItemSupervisionService {

	List<?> selectObjectByDep(Map<String, Object> param);

	List<?> selectEReport();

	List<?> selectEDep();

	 void deleteBySid(String sid);

	void saveNewItem(String supNo, String supTypeId,
			String supTypeCode, String supTypeName, String supName,
			String workType, String mofdepId, String mofdepCode,
			String mofdepName, String objTypeId, String objTypeCode,
			String objTypeName, String depId, String depCode, String depName,
			String supCycle, String supMode, String startDate, String endDate,
			Short isUnion, String unionOrgs, String supContent, String remark,
			@SuppressWarnings("rawtypes") List<Map> objectlist, @SuppressWarnings("rawtypes") List<Map> eReportlist);

	@SuppressWarnings("rawtypes")
	List<Map> selectAllSup(String supTypeId);

	List<?> selectSupBySid(String sid);

	List<?> selectDepBySid(String sid);

	List<?> selectDepByDepId(Map<String, Object> param);

	void decompose(String depTaskId, String sid, String supNo, String supName,
			Short supMode, String depId, String depCode, String depName,
			String oid, String planBeginDate, String planEndDate,
			Long planCost, String remark, String supCycle);

}
