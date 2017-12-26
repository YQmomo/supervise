package gov.df.supervise.api.examine;

import java.util.List;

public interface ExamineService {

  public List selectAll(String chr_code, String char_id, String type, String oid, String status, String billtype_code,
    String menu_id, String task_id) throws Exception;

  public String getYear();

  public String getRgCode();

  public void update(String id, String is_valid);

  //处汇总操作
  public void doCollect(String billtype_code, String data, int total, String bill_no) throws Exception;

  /**
   *处 撤销汇总
   * @param param
   */
  public void undoSummary(String chr_id, String id, int total) throws Exception;

  //办汇总操作
  public void doOfficeCollect(String billtype_code, String data, int total, String bill_no) throws Exception;

  //办撤销汇总
  public void undoOfficeSummary(String chr_id, String id, int total) throws Exception;

  //动态按钮获取
  public List getActionButton(String menu_id);

  public List selectBill(String billtype_code, String chr_code, String chr_id);

  public boolean inputSupBill(String sid, String bill_no, String id, String billtype_code, String chr_id,
    String chr_code, String chr_name, String task_id, String sup_date, String dep_id, String mofdep_id);

  public boolean ifExistBill(String billtype_code, String chr_id, String task_id);

  public boolean saveSupBill(String sid, String bill_no, String bill_id, String billtype_code, String chr_id,
    String chr_code, String chr_name, String task_id, String sup_date, String dep_id, String mofdep_id);

}
