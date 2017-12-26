package gov.df.supervise.service.csofacc;

import gov.df.supervise.bean.csofacc.csofAccSupDepEntity;
import gov.df.supervise.bean.csofacc.csofAccSupEndEntity;
import gov.df.supervise.bean.csofacc.csofAccSupEntity;
import gov.df.supervise.service.common.persistence.annotation.MyBatisDao;

import java.util.List;
import java.util.Map;

@MyBatisDao
public interface csofAccDao {
  /**
   * 查询监管类型树
   */
  public List getSupType(Map<String, Object> param);

  /**
   * 新增事项
   * @param param
   */
  public void saveAccSup(Map<String, Object> param);

  /**
   * 查询事项详细信息
   * @param sid
   * @return
   */
  public Map<String, Object> getAccSup(String entity_id);

  /**
   * 修改事项
   * @param param
   */
  public void updateAccSup(Map<String, Object> param);

  /**
   * 删除事项
   * @param sid
   */
  public void deleteAccSup(String entity_id);

  /**
   * 新增工作记录
   * @param param
   */
  public void saveAccWork(Map<String, Object> param);

  /**
   * 通过新增工作记录时sid查询事项表的工作量和资金规模
   */
  public csofAccSupEntity findAccSupBySid(String entity_id);

  /**
   * 回写资金规模和工作量到事项表里
   * @param param
   */
  public void updateAccSupByWorkload(Map<String, Object> param);

  /**
   * 查询工作记录详细信息
   * @param sid
   * @return
   */
  public List getAccWork(String entity_id);

  /**
   * 单独删除工作记录
   * @param 
   */
  public void deleteAccWork(Map<String, Object> param);

  /**
   * 删除事项时同时删除工作记录
   * @param id
   */
  public void deleteAllAccWork(String entity_id);

  /**
   * 新增问题记录
   * @param param
   */
  public void saveAccProblem(Map<String, Object> param);

  /**
   * 查询问题记录详细信息
   * @param sid
   * @return
   */
  public List getAccProblem(String entity_id);

  /**
   * 修改问题记录
   * @param param
   */
  public void updateAccProblem(Map<String, Object> param);

  /**
   * 单独删除问题记录
   * @param 
   */
  public void deleteAccProblem(Map<String, Object> param);

  /**
   * 删除事项时同时删除问题记录
   * @param 
   */
  public void deleteAllAccProblem(String entity_id);

  /**
   * 录入界面的树
   * @param sup_type_id
   * @return
   */
  public List getSupData(Map<String, Object> param);

  /**
   * 通过监管类型来树
   */
  public List getSupDataById(Map<String, Object> param);

  /**
   * 修改事项表的工作进展
   */
  public void updateSupStatus(Map<String, Object> param);

  /**
   * 获取树信息
   */
  public List getBookSetById(Map<String, Object> param);

  /**
   * 通过点击树获取事项表
   */
  public List getAccWorkByMonth(Map<String, Object> param);

  /**
   *  通过点击树获取月结表
   * @param param
   * @return
   */
  public List getAccWorkEndByMonth(Map<String, Object> param);

  /**
   * 找到set_id，set_month,set_year写入事项表里
   */
  public Map<String, Object> getAccSupByBookId(String book_id);

  /**
   * 月结
   */
  /**
   * 通过父级id找到这个账套的年度
   * @param parent_id
   * @return
   */
  public String getYear(String parent_id);

  /**
   * 获取下一个账套的set_id
   * @param param
   * @return
   */
  public String getSetId(Map<String, Object> param);

  /**
   * 通过set_id来月结，反月结
   * @param set_id
   */
  public void updateAccBookBySetid(Map<String, Object> param);

  /**
   * 下一个账套的开账,反月结
   * @param setId
   */
  public void updateAccBookSet(Map<String, Object> param);

  /**
   * 如果这个事项已经完成就把set_id修改
   * @param 
   */
  public void updateAccSupBySid(Map<String, Object> param);

  /**
   * 月结时把事项表数据复制一份出去
   * @param param
   */
  public void saveAccSupEnd(Map<String, Object> param);

  /**
   * 查找月结表里已完成的事项数量
   */
  public int getCount(String sid);

  /**
   * 反月结
   */
  public void modityAccSupBysid(Map<String, Object> param);

  public void deleteAccSupEnd(Map<String, Object> param);

  /**
   * 查找月结表里当月的事项数量
   */
  public int getCountEnd(Map<String, Object> param);

  //台账处汇总
  /**
   * 得到处汇总左侧树
   */
  public List getBookDepById(Map<String, Object> param);

  /**
   * 点击树显示详细信息
   */
  public List getAccSupEndByBook(Map<String, Object> param);

  /**
   * 查看处汇总表数据
   */
  public List getAccSupDepBySetid(Map<String, Object> param);

  /**
   * 点击树查看明细台账的数据
   */
  public List getAccSupEndBysetid(Map<String, Object> param);

  /**
   * 插入数据到处汇总表中
   */
  public void saveAccSupDep(Map<String, Object> param);

  /**
   * 通过 sid,set_id查询事项
   * @param param
   * @return
   */
  public csofAccSupEndEntity FindAccSup(Map<String, Object> param);

  /**
   * 通过sup_name,sup_type_id查询事项名字和监管类型相同的事项数量
   * @param param
   * @return
   */
  public int getAccCount(Map<String, Object> param);

  /**
   * 通过dep_task_id,sid查询处汇总表事项数量
   */
  public int getTaskAccCount(Map<String, Object> param);

  /**
   * 通过sup_name,sup_type_id查询事项名字和监管类型相同的汇总表信息
   * @param param
   * @return
   */
  public Map<String, Object> getAccSupDep(Map<String, Object> param);

  /**
   *  通过dep_task_id,sid查询处汇总表事项信息
   */
  public Map<String, Object> getTaskAccSupDep(Map<String, Object> param);

  /**
   * 通过sup_name,sup_type_id修改汇总表里的值
   * @param param
   */
  public void updateAccSupDep(Map<String, Object> param);

  /**
   * 通过dep_task_id,sid修改汇总表里的值
   * @param param
   */
  public void updateTaskAccSupDep(Map<String, Object> param);

  /**
   * 汇总成功后回写汇总表id到月结表里
   * @param param
   */
  public void updateAccSupEnd(Map<String, Object> param);

  /**
   * 通过父级id查询父级的chr_code
   */
  public String getChrCode(String parent_id);

  //台账处撤销汇总
  /**
   * 撤销汇总，通过sup_name,sup_type_id,set_month,year等条件删除数据
   * @param param
   * @return
   */
  public void deleteAccSupDep(Map<String, Object> param);

  /**
   * dep_task_id,sid
   * @param parent_id
   * @return
   */
  public void deleteTaskAccSupDep(Map<String, Object> param);

  //台账办汇总
  public String getOfficeChrCode(String parent_id);

  /**
   * 台账办汇总树
   * @param book_id
   */
  public List getAccSupOfficeById(Map<String, Object> param);

  /**
   * 点击树显示详细信息
   */
  public List getAccSupDepByBook(Map<String, Object> param);

  /**
   * 点击树显示办汇总表详细信息
   */
  public List getAccSupOfficeBySetid(Map<String, Object> param);

  /**
   * 点击树查看处台账的数据
   */
  public List getAccSupDepBysetid(Map<String, Object> param);

  /**
   * 插入数据到办汇总表中
   */
  public void saveAccSupOffice(Map<String, Object> param);

  /**
   * 通过 id,book_id查询事项
   * @param param
   * @return
   */
  public csofAccSupDepEntity FindAccSupOffice(Map<String, Object> param);

  /**
   * 通过sup_name,sup_type_id查询事项名字和监管类型相同的事项数量
   * @param param
   * @return
   */
  public int getAccOfficeCount(Map<String, Object> param);

  /**
   * 通过sid查询事项名字和监管类型相同的事项数量
   * @param param
   * @return
   */
  public int getTaskAccOfficeCount(Map<String, Object> param);

  /**
   * 通过sup_name,sup_type_id查询事项名字和监管类型相同的办汇总表信息
   * @param param
   * @return
   */
  public Map<String, Object> getAccSupOffice(Map<String, Object> param);

  /**
   * 通过sid查询事项名字和监管类型相同的办汇总表信息
   * @param param
   * @return
   */
  public Map<String, Object> getTaskAccSupOffice(Map<String, Object> param);

  /**
   * 通过sup_name,sup_type_id修改办汇总表里的值
   * @param param
   */
  public void updateAccSupOffice(Map<String, Object> param);

  /**
   * 通过sid修改办汇总表里的值
   * @param param
   */
  public void updateTaskAccSupOffice(Map<String, Object> param);

  /**
   * 汇总成功后回写办汇总表id到月结表里
   * @param param
   */
  public void motityAccSupDep(Map<String, Object> param);

  //台账办撤销汇总
  /**
   * 撤销汇总，通过sup_name,sup_type_id,set_month,year等条件删除数据
   * @param param
   * @return
   */
  public void deleteAccSupOffice(Map<String, Object> param);

  /**
   * 撤销汇总，通过sid,set_month,year等条件删除数据
   * @param param
   * @return
   */
  public void deleteTaskAccSupOffice(Map<String, Object> param);

  /**
   * 得到台账菜单
   */
  public List getAccmenu(Map<String, Object> param);

  /**
   * 查询未过账的数据
   */
  public List getTaskUser(Map<String, Object> param);

  /**
   * 查询未过账的数据数量
   * @param param
   * @return
   */
  public int getTaskUserCount(Map<String, Object> param);

  /**
   * 插入数据到表里
   */
  public void saveAccLog(Map<String, Object> param);

  /**
   * 插入数据到台账的事项表里
   */
  public void saveAccSupById(Map<String, Object> param);

  /**
   * 查询acc要素数据
   */
  public List getCsofAcc(Map<String, Object> param);

  /**
   * 通过acc_id 过滤树
   */
  public List getSupTreeByAccId(Map<String, Object> param);

  /**
   * 通过dep_id 查询该处室的人员
   */
  public List getUserNameByDepId(Map<String, Object> param);

  /**
   * 月结，汇总刷新set_year,set_month
   */
  public void updateDate(Map<String, Object> param);

  /**
   * 修改处汇总的数据
   */
  public void modityAccSupDep(Map<String, Object> param);

  /**
   * 修改办汇总的数据
   */
  public void modityAccSupOffice(Map<String, Object> param);
}
