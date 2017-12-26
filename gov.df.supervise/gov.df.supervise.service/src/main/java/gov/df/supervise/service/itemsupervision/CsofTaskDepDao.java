package gov.df.supervise.service.itemsupervision;

import java.util.List;
import java.util.Map;

import gov.df.supervise.bean.task.CsofTaskDep;
import gov.df.supervise.service.common.persistence.annotation.MyBatisDao;

@MyBatisDao
public interface CsofTaskDepDao {
    /**
     *
     * @mbggenerated 2017-09-04
     */
    int deleteByPrimaryKey(String id);

    /**
     *
     * @mbggenerated 2017-09-04
     */
    int insert(CsofTaskDep record);

    /**
     *
     * @mbggenerated 2017-09-04
     */
    int insertSelective(CsofTaskDep record);

    /**
     *
     * @mbggenerated 2017-09-04
     */
    CsofTaskDep selectByPrimaryKey(String id);

    /**
     *
     * @mbggenerated 2017-09-04
     */
    int updateByPrimaryKeySelective(CsofTaskDep record);

    /**
     *
     * @mbggenerated 2017-09-04
     */
    int updateByPrimaryKey(CsofTaskDep record);

	List<?> selectDepBySid(String sid);

	List<?> selectDepByDepId(String depId);

	@SuppressWarnings("rawtypes")
	List<Map> selectDepByDepId(Map<String, Object> param);

	@SuppressWarnings("rawtypes")
	List<Map> selectAgencyBy(Map<String, Object> param);
}