package gov.df.supervise.service.examine;

import gov.df.supervise.service.common.persistence.annotation.MyBatisDao;

import java.util.List;
import java.util.Map;

/**
 * 监管审核
 * @author tongya
 *
 */
@MyBatisDao
public interface ExamineDao {

  public List getwfAllBill(Map<String, Object> param);

  public List getwfBill(Map<String, Object> param);

  public List getwfBillEnd(Map<String, Object> param);

  public List selectAgencyAll(Map<String, Object> param);

  public List selectwfAgency(Map<String, Object> param);

  public List selectAgencyEnd(Map<String, Object> param);

  public List selectMofdepAll(Map<String, Object> param);

  public List selectwfMofdep(Map<String, Object> param);

  public List selectMofdepEnd(Map<String, Object> param);

  public List selectDepAll(Map<String, Object> param);

  public List selectwfDep(Map<String, Object> param);

  public List selectDepEnd(Map<String, Object> param);

  public List selectOfficeAll(Map<String, Object> param);

  public List selectwfOffice(Map<String, Object> param);

  public List selectOfficeEnd(Map<String, Object> param);

  /**
   * 结束节点 
   */
  public List getwfendAllBill(Map<String, Object> param);

  public List getwfendBillEnd(Map<String, Object> param);

  public List selectendAgencyAll(Map<String, Object> param);

  public List selectendAgencyEnd(Map<String, Object> param);

  public List selectendMofdepAll(Map<String, Object> param);

  public List selectendMofdepEnd(Map<String, Object> param);

  public List selectendDepAll(Map<String, Object> param);

  public List selectendDepEnd(Map<String, Object> param);

  public List selectendOfficeAll(Map<String, Object> param);

  public List selectendOfficeEnd(Map<String, Object> param);

  /**
   * 监管内容录入界面的删除操作
   * @param param
   */
  public void update(Map<String, String> param);

  /**
   * 处汇总操作
   */
  public String selectBillName(String billtype_code);

  public void insertBillMofDep(Map<String, Object> param);

  public void updateSupBillBydep(Map<String, Object> param);

  public void updateSupBillByoffice(Map<String, Object> param);

  public int countSupMoney(String mofdep_id);

  public int countSupNum(String mofdep_id);

  /**
   * 
   * @param chr_code
   * @return mofdep_bill_id
   */
  public String getId(String chr_code);

  /**
   * 处撤销汇总
   * @param param
   */
  public void deleteBillMofDep(String id);

  /**
   * 办汇总
   */
  public int countOfficeSupMoney(String oid);

  public int countOfficeSupNum(String oid);

  /**
   * 
   * @param chr_id
   * @return office_bill_id
   */
  public String getOfficeId(String chr_id);

  /**
   * 生成汇总数据到sup_bill_office表中
   * @param param
   */
  public void insertBillOffice(Map<String, Object> param);

  /**
   * 办撤销汇总
   * @param param
   */
  public void deleteBillOffice(String id);

  //动态按钮获取
  public List getActionButton(String menu_id);

  public Map<String, Object> SelectMenuNode(Map<String, Object> param);

  /**
   * 查找处汇总后生成的单据
   * @param param
   * @return
   */
  public List selectMofdepBill(Map<String, Object> param);

  /**
   * 查找办汇总后生成的单据
   * @param param
   * @return
   */
  public List selectOfficeBill(Map<String, Object> param);

  /**
   * 录入数据到sup_bill表中
   * @param param
   */
  public void saveSupBill(Map<String, Object> param);

  /**
   * 获取任务表的数据
   * @param param
   * @return
   */
  public Map<String, Object> getTaskSup(Map<String, Object> param);

  public Map<String, Object> getMofdep(String mofdep_id);

  public Map<String, Object> getDep(String dep_id);

  public int getCount(Map<String, Object> param);

  public int getEndCount(Map<String, Object> param);

  public int getMofDepCount(Map<String, Object> param);

  public int getOfficeCount(Map<String, Object> param);

  public int getEndMofDepCount(Map<String, Object> param);

  public int getEndOfficeCount(Map<String, Object> param);

  public int getMofDepCount1(String mofdep_bill_id);

  public int getOfficeCount1(String office_bill_id);

}
