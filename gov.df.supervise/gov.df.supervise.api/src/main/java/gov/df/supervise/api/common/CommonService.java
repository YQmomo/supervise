package gov.df.supervise.api.common;

import java.util.List;
import java.util.Map;

/**
 * 监管事项接口
 * 
 * @author w 2017-08-22
 * 
 */
public interface CommonService {

	/**
	 * 根据条件查询单表所有数据
	 * 
	 * @param tableName	单表查询表名
	 * @param condition 查询条件
	 * @param proFlag   是否带权限查询
	 * @param pageInfo  分页信息
	 * @return 返回查询结果集List
	 */
	public List getDataList(String tableName, String condition, boolean proFlag, String pageInfo);

	/**
	 * 单表通用删除方法
	 * 
	 * @param tableName  删除表名
	 * @param condition  删除数据过滤条件
	 * @return 数据执行条目数
	 */
	public int deleteDataList(String tableName, String condition);
	
	
	public String getInsertSql(Map<String, String> map, String tableName);
	
	public Map getBillInfo(String billtype_code,Map data);

	public boolean saveWorkFlow(Map workMap,String [] ids);
	
	/**
	 * 单个工作流处理
	 * @param workMap
	 * @return
	 */
	public boolean doWorkFlow(Map workMap);
	
	/**
	 * 
	 * @param menu_id
	 * @param status
	 * @param billtype_code
	 * @param conditionMap		过滤条件参数map key:字段  value:值
	 * @return
	 */
	public List getWorkData(String menu_id,String status,String billtype_code,Map conditionMap,String pageInfo);
	
	/**
	 * 拼接高级查询
	 * @param conditionMap
	 * @return
	 */
	public String getCondition(Map conditionMap);

}
