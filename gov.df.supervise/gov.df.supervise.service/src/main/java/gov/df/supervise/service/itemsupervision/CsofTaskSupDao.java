package gov.df.supervise.service.itemsupervision;

import java.util.List;
import java.util.Map;

import gov.df.supervise.bean.task.CsofTaskSup;
import gov.df.supervise.service.common.persistence.annotation.MyBatisDao;

@MyBatisDao
public interface CsofTaskSupDao {
    /**
     *
     * @mbggenerated 2017-09-01
     */
    int deleteByPrimaryKey(String sid);

    /**
     *
     * @mbggenerated 2017-09-01
     */
    int insert(CsofTaskSup record);

    /**
     *
     * @mbggenerated 2017-09-01
     */
    int insertSelective(CsofTaskSup record);

    /**
     *
     * @mbggenerated 2017-09-01
     */
    @SuppressWarnings("rawtypes")
	List<Map> selectByPrimaryKey(String sid);

    /**
     *
     * @mbggenerated 2017-09-01
     */
    int updateByPrimaryKeySelective(CsofTaskSup record);

    /**
     *
     * @mbggenerated 2017-09-01
     */
    int updateByPrimaryKey(CsofTaskSup record);

	List<?> selectObjectByDep(Map<String, Object> param);

	List<?> selectEReport();

	List<?> selectEDep(String oid);

	void deleteDepBySid(String sid);

	void deleteAgencyBySid(String sid);

	void deleteSupBySid(String sid);

	void deleteUserBySid(String sid);

	void deleteReportBySid(String sid);

	List<?> selectSupBySid(String sid);

	void updateDepBySid(String sid);

	void updateSupBySid(String sid);

	void updateUserBySid(String sid);
}