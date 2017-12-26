package gov.df.supervise.api.task;

import java.util.List;
import java.util.Map;

/**
 * 附件服务接口
 * @author w
 * 2017-08-22 
 *
 */
public interface TaskService {

  /**
   * 保存监管事项、处室任务
   * @param map 监管事项信息
   * @param objlist  部门关联关系树
   * @param agencyList 勾选单位树
   * @return
   */
  public int saveTask(Map<String, String> map, List objlist, List agencyList, List reportList);

  public List getSupTree();

  public int saveTaskUser(String postData);

  /**
   * 带权限的数据查询
   */
  public List getPorData(String tableName, String condition);

  /**
   * 处室任务撤销分解
   */
  public int clearAssign(String chr_id, String type_code);

  /**
   * 根据条件查询单表所有数据
   * @param condition
   * @return
   */
  public List getData(String tableName, String condition);

  /**
   * 单表通用删除方法
   * @param tableName 删除表名
   * @param condition 删除数据过滤条件
   * @return	数据执行条目数
   */
  public int deleteData(String tableName, String condition);

  /**
   * 监管事项报表保存方法
   * @param tableName csof_sup_report
   * @return	数据执行条目数
   */
  public int saveReport(List reportList);

  /**
   * 修改 监管处室   csof_task_dep 
   * @param data  修改值
   * @return 执行条目数字
   */
  public int updateTaskDep(String data);

  /**
   * 处室任务发布
   * @param ids
   * @return
   */
  public int publishTask(String ids);

  /**
   * 任务下达
   * @param sids
   * @return
   */
  public int approveTask(String sids);

  public List getObjList();
}
