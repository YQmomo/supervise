package gov.df.supervise.service.view;

import java.util.List;
import java.util.Map;

import gov.df.supervise.bean.view.CsofCViewSheetEntity;
import gov.df.supervise.service.common.persistence.annotation.MyBatisDao;
@MyBatisDao
public interface CsofCViewSheetDao {
    int deleteByPrimaryKey(String sheetId);

    int insert(CsofCViewSheetEntity record);

    int insertSelective(CsofCViewSheetEntity record);

    CsofCViewSheetEntity selectByPrimaryKey(String sheetId);

    int updateByPrimaryKeySelective(CsofCViewSheetEntity record);

    int updateByPrimaryKey(CsofCViewSheetEntity record);

	List<String> selectColumnName(String dataTable);

	void deleteSheetByViewId(String viewId);

	CsofCViewSheetEntity getCsofCViewSheet(String sheetName);

	@SuppressWarnings("rawtypes")
	CsofCViewSheetEntity getCsofCViewSheet(Map param);

	Map getHeadFootNum(String sheetId);
}