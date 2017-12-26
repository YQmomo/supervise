package gov.df.supervise.bean.examine;

import java.io.Serializable;

/**
 * 业务表csof_sup_bill的实体
 * @author tongya
 *
 */
public class CsofSupBillEntity implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String id;

  private String task_id;

  public String getTask_name() {
    return task_name;
  }

  public void setTask_name(String task_name) {
    this.task_name = task_name;
  }

  public String getTask_no() {
    return task_no;
  }

  public void setTask_no(String task_no) {
    this.task_no = task_no;
  }

  private String task_name;

  private String task_no;

  public String getTask_id() {
    return task_id;
  }

  public void setTask_id(String task_id) {
    this.task_id = task_id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSup_no() {
    return sup_no;
  }

  public void setSup_no(String sup_no) {
    this.sup_no = sup_no;
  }

  public String getBill_no() {
    return bill_no;
  }

  public void setBill_no(String bill_no) {
    this.bill_no = bill_no;
  }

  public String getMatter() {
    return matter;
  }

  public void setMatter(String matter) {
    this.matter = matter;
  }

  public String getSup_num() {
    return sup_num;
  }

  public void setSup_num(String sup_num) {
    this.sup_num = sup_num;
  }

  public String getSup_money() {
    return sup_money;
  }

  public void setSup_money(String sup_money) {
    this.sup_money = sup_money;
  }

  public String getRecord_user() {
    return record_user;
  }

  public void setRecord_user(String record_user) {
    this.record_user = record_user;
  }

  public String getRecord_date() {
    return record_date;
  }

  public void setRecord_date(String record_date) {
    this.record_date = record_date;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public String getLatest_op_user() {
    return latest_op_user;
  }

  public void setLatest_op_user(String latest_op_user) {
    this.latest_op_user = latest_op_user;
  }

  public String getLaest_op_date() {
    return laest_op_date;
  }

  public void setLaest_op_date(String laest_op_date) {
    this.laest_op_date = laest_op_date;
  }

  public String getCreate_user() {
    return create_user;
  }

  public void setCreate_user(String create_user) {
    this.create_user = create_user;
  }

  public String getCreate_date() {
    return create_date;
  }

  public void setCreate_date(String create_date) {
    this.create_date = create_date;
  }

  public String getSet_year() {
    return set_year;
  }

  public void setSet_year(String set_year) {
    this.set_year = set_year;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getIs_end() {
    return is_end;
  }

  public void setIs_end(String is_end) {
    this.is_end = is_end;
  }

  public String getSid() {
    return sid;
  }

  public void setSid(String sid) {
    this.sid = sid;
  }

  public String getDep_id() {
    return dep_id;
  }

  public void setDep_id(String dep_id) {
    this.dep_id = dep_id;
  }

  public String getDep_code() {
    return dep_code;
  }

  public void setDep_code(String dep_code) {
    this.dep_code = dep_code;
  }

  public String getDep_name() {
    return dep_name;
  }

  public void setDep_name(String dep_name) {
    this.dep_name = dep_name;
  }

  public String getAgency_id() {
    return agency_id;
  }

  public void setAgency_id(String agency_id) {
    this.agency_id = agency_id;
  }

  public String getAgency_code() {
    return agency_code;
  }

  public void setAgency_code(String agency_code) {
    this.agency_code = agency_code;
  }

  public String getAgency_name() {
    return agency_name;
  }

  public void setAgency_name(String agency_name) {
    this.agency_name = agency_name;
  }

  public String getOid() {
    return oid;
  }

  public void setOid(String oid) {
    this.oid = oid;
  }

  public String getSup_name() {
    return sup_name;
  }

  public void setSup_name(String sup_name) {
    this.sup_name = sup_name;
  }

  public String getBilltype_code() {
    return billtype_code;
  }

  public void setBilltype_code(String billtype_code) {
    this.billtype_code = billtype_code;
  }

  public String getBilltype_name() {
    return billtype_name;
  }

  public void setBilltype_name(String billtype_name) {
    this.billtype_name = billtype_name;
  }

  public String getIs_valid() {
    return is_valid;
  }

  public void setIs_valid(String is_valid) {
    this.is_valid = is_valid;
  }

  public String getSum_type() {
    return sum_type;
  }

  public void setSum_type(String sum_type) {
    this.sum_type = sum_type;
  }

  public String getMofdep_bill_id() {
    return mofdep_bill_id;
  }

  public void setMofdep_bill_id(String mofdep_bill_id) {
    this.mofdep_bill_id = mofdep_bill_id;
  }

  public String getOffice_bill_id() {
    return office_bill_id;
  }

  public void setOffice_bill_id(String office_bill_id) {
    this.office_bill_id = office_bill_id;
  }

  private String sup_no;

  private String bill_no;

  private String matter;

  private String sup_num;

  private String sup_money;

  private String record_user;

  private String record_date;

  private String action;

  private String remark;

  private String latest_op_user;

  private String laest_op_date;

  private String create_user;

  private String create_date;

  private String set_year;

  private String status;

  private String is_end;

  private String sid;

  private String dep_id;

  private String dep_code;

  private String dep_name;

  private String agency_id;

  private String agency_code;

  private String agency_name;

  private String oid;

  private String sup_name;

  private int sup_cycle;

  public int getSup_cycle() {
    return sup_cycle;
  }

  public void setSup_cycle(int sup_cycle) {
    this.sup_cycle = sup_cycle;
  }

  private String billtype_code;

  private String billtype_name;

  private String is_valid;

  private String sum_type;

  private String mofdep_bill_id;

  private String office_bill_id;

  private String mofdep_id;

  private String mofdep_code;

  private String mofdep_name;

  public String getMofdep_name() {
    return mofdep_name;
  }

  public void setMofdep_name(String mofdep_name) {
    this.mofdep_name = mofdep_name;
  }

  public String getMofdep_id() {
    return mofdep_id;
  }

  public void setMofdep_id(String mofdep_id) {
    this.mofdep_id = mofdep_id;
  }

  public String getMofdep_code() {
    return mofdep_code;
  }

  public void setMofdep_code(String mofdep_code) {
    this.mofdep_code = mofdep_code;
  }

  public String getOffice_name() {
    return office_name;
  }

  public void setOffice_name(String office_name) {
    this.office_name = office_name;
  }

  private String office_name;

}
