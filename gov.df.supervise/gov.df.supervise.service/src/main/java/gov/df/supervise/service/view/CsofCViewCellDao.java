package gov.df.supervise.service.view;

import java.util.List;
import java.util.Map;

import gov.df.supervise.bean.view.CsofCViewCellEntity;
import gov.df.supervise.service.common.persistence.annotation.MyBatisDao;
@MyBatisDao
public interface CsofCViewCellDao {
    int deleteByPrimaryKey(String cellId);

    int insert(CsofCViewCellEntity record);

    int insertSelective(CsofCViewCellEntity record);

    CsofCViewCellEntity selectByPrimaryKey(String cellId);

    int updateByPrimaryKeySelective(CsofCViewCellEntity record);

    int updateByPrimaryKey(CsofCViewCellEntity record);
    
    List<CsofCViewCellEntity> selectIsEditBySheetId(String sheetId);

	void createTable(Map<String, Object> map);

	@SuppressWarnings("rawtypes")
	List<Map> getExcel(String viewId);

	void deleteCellByViewId(String viewId);
	
	void alterTable(Map<String, Object> map);

	List<CsofCViewCellEntity> getCsofCViewCell(String sheetId);

	@SuppressWarnings("rawtypes")
	List<Map> getExcelBySheetId(String sheetId);
}