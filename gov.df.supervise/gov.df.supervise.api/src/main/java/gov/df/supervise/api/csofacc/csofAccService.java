package gov.df.supervise.api.csofacc;

import gov.df.fap.bean.util.FPaginationDTO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface csofAccService {
  /*
  * 查询监管类型树
  */
  public List getSupType(String ele_code, String acc_id);

  public void saveAccSup(String data, String sup_no, String book_id, String acc_id) throws IOException;

  public Map<String, Object> getAccSup(String entity_id);

  public void updateAccSup(String data) throws IOException;

  public void deleteAccSup(String entity_id);

  /**
   * 工作记录
   */
  public void saveAccWork(String data, String work_no, String set_month, String book_id, String set_year,
    String entity_id);

  public List getAccWork(String entity_id);

  public void deleteAllAccWork(String entity_id);

  public void deleteAccWork(String entity_id, String id);

  /**
   * 
   * 问题记录
   */
  public void saveAccProblem(String data, String entity_id);

  public List getAccProblem(String entity_id);

  public void updateAccProblem(String data);

  public void deleteAllAccProblem(String entity_id);

  public void deleteAccProblem(String entity_id, String id);

  public List getSupData(String ele_code, String acc_id);

  /**
   * 通过监管类型来过树
   */
  public List getSupDataById(String ele_code, String chr_id, String acc_id);

  /**
   * 获取月结树信息
   */
  public List getBookSetById(String book_id, String menu_id, String billtype_code);

  /**
   * 通过点击树获取工作记录
   */
  public List getAccWorkByMonth(String set_month, String book_id, String parent_id, String is_close);

  //月结
  public boolean updateAccBookSet(String set_id, String id, String set_month, String parent_id, String book_id);

  //反月结
  public boolean modityAccBookSet(String set_id, String id, String set_month, String parent_id, String book_id);

  /**
   * 插入数据到处汇总表中
   * @return 
   * @throws Exception 
   */
  public boolean saveAccSupDep(String acc_sup_dep_id, String sid, String set_id, String sup_type_id, String sup_name,
    String dep_id, String dep_code, String dep_name, String chr_code, String parent_id, String book_id,
    String chr_name, String dep_task_id, String Id, String obj_type_id) throws Exception;

  /**
   * 得到处汇总左侧树
   */
  public List getBookDepById(String book_id, String menu_id, String billtype_code);

  /**
   * 点击树显示详细信息
   */
  public List getAccSupEndByBook(String dep_code, String chr_name, String parent_id);

  /**
   * 点击树显示处汇总表信息
   */
  public List getAccSupDepBySetid(String book_id, String chr_name, String parent_id, String chr_code);

  /**
   * 点击树查看明细台账的数据
   */
  public List getAccSupEndBysetid(String book_id, String chr_code);

  /**
   * 撤销汇总，通过sup_name,sup_type_id,set_month,year等条件删除数据
   * @return 
   * 
   */
  public boolean deleteAccSupDep(String acc_sup_dep_id, String sid, String set_id, String sup_type_id, String sup_name,
    String chr_code, String parent_id, String chr_name, String book_id, String dep_task_id, String Id, int is_input,
    String obj_type_id);

  //台账办汇总
  /**
   * 台账办汇总树
   * @param book_id
   */
  public List getAccSupOfficeById(String book_id, String menu_id, String billtype_code);

  /**
   * 点击树显示详细信息
   */
  public List getAccSupDepByBook(String oid, String chr_name, String parent_id);

  /**
   * 点击树显示办汇总表信息
   */
  public List getAccSupOfficeBySetid(String book_id, String chr_name, String parent_id, String chr_code);

  /**
   * 点击树查看处台账的数据
   */
  public List getAccSupDepBysetid(String book_id, String chr_code);

  /**
   * 插入数据到办汇总表中
   * @return 
   * @throws Exception 
   */
  public boolean saveAccSupOffice(String acc_sup_office_id, String acc_sup_dep_id, String set_id, String sup_type_id,
    String sup_name, String chr_code, String parent_id, String book_id, String chr_name, String sid, String obj_type_id)
    throws Exception;

  /**
   * 撤销汇总，通过sup_name,sup_type_id,set_month,year等条件删除数据
   * @param param
   * @return
   */
  public boolean deleteAccSupOffice(String acc_sup_office_id, String acc_sup_dep_id, String set_id, String sup_type_id,
    String sup_name, String chr_code, String parent_id, String chr_name, String book_id, String sid, int is_input,
    String obj_type_id);

  /**
   * 得到台账菜单
   */
  public List getAccmenu(String user_id, String oid);

  /**
   * 查询未过账数据
   */
  public List getTaskUser(String user_id, String oid, FPaginationDTO page);

  /**
   * 查询未过账数据数量
   */
  public int getTaskUserCount(String user_id, String oid);

  /***
   * 过账
   */
  public void saveTaskUser(String data, String book_id, String dep_id, String oid, String acc_id);

  /**
   * 查询acc要素数据
   */
  public List getCsofAcc(String ele_code, String book_id);

  /**
   * 通过acc_id 过滤树
   */
  public List getSupTreeByAccId(String ele_code, String chr_id);

  /**
   * 通过dep_id 查询该处室的人员
   */
  public List getUserNameByDepId(String dep_id);

  /**
   * 修改处汇总数据
   */
  public void updateAccSupDep(String data) throws IOException;

  /**
   * 修改办汇总数据
   */
  public void updateAccSupOffice(String data) throws IOException;
}
