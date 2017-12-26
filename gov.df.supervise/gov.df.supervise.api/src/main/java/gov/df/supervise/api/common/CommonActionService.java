package gov.df.supervise.api.common;

import java.util.List;
import java.util.Map;

/**
 * 单表简单增删改查接口
 * @author Administrator
 *
 */
public interface CommonActionService {

	/**
	 * 新增
	 * @param billInfo
	 * @param saveData
	 * @return
	 */
	public int saveData (Map billInfo , Map saveData);
	
	/**
	 * 拼接过滤条件
	 * @param conditionMap
	 * @return
	 */
	public String getCondition (Map conditionMap,Map conditionRela);
	
	/**
	 * 批量新增
	 * @param billtype_code 表单编码
	 * @param saveList  新增数据集合
	 * @return 新增条目数
	 */
	public int saveBatachData (String billtype_code, List saveList);
	
	/**
	 * 删除
	 * @param billInfo  单据信息
	 * @param conditionMap  删除数据过滤条件Map集合
	 * @return 数据执行条目数
	 */
	public int deleteData (Map billInfo , Map conditionMap,Map conditionRela);
	
	/**
	 * 修改 先删除后新增,受事务控制
	 * @param billInfo  单据信息
	 * @param conditionMap 修改过滤条件
	 * @param saveData 修改字段
	 * @return
	 */
	public int updateData (Map billInfo , Map conditionMap , Map saveData,Map conditionRela);
	public int updateDataByFiled (Map billInfo , Map conditionMap , Map updateData,Map conditionRela);
	
	/**
	 * 分页查询
	 * @param billInfo  单据信息
	 * @param pageInfo  分页信息
	 * @param conditionMap  自动装载过滤条件
	 * @param conditionStr  前端拼接好的条件
	 * @return
	 */
	public Map getData (Map billInfo , String pageInfo , Map conditionMap , Map likeConditionMap);
	
	/**
	 * 操作分发
	 * @param action
	 * @param billtype_code
	 * @param pageInfo
	 * @param operationMap
	 * @param conditionMap
	 * @param batchData
	 * @return
	 */
	public Map<String,Object> action(String action, String billtype_code , String pageInfo, Map operationMap ,  List batchData , Map conditionMap ,Map likeConditionMap);

}
