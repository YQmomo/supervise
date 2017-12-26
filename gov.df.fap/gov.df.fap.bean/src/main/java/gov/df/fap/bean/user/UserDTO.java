package gov.df.fap.bean.user;

import gov.df.fap.bean.util.DTO;

import java.util.Map;

/** @author Hibernate CodeGenerator */
public class UserDTO extends DTO {

  /**
  * 
  */
  private static final long serialVersionUID = -5074345366563248879L;

  /** identifier field */

  private String session_id;

  private String user_id;

  /** persistent field */
  private String user_code;

  /** nullable persistent field */
  private String user_name;

  /** nullable persistent field */
  private String password;

  /** persistent field */
  private String org_type;

  /** nullable persistent field */
  private String org_code;

  /** persistent field */
  private int level_num;

  /** nullable persistent field */
  private Integer is_leaf;

  /** nullable persistent field */
  private Integer gender;

  /** nullable persistent field */
  private String telephone;

  /** nullable persistent field */
  private String mobile;

  /** persistent field */
  private int enabled;

  /** nullable persistent field */
  private String headship_code;

  /** nullable persistent field */
  private String birthday;

  /** nullable persistent field */
  private String address;

  /** nullable persistent field */
  private String email;

  /** persistent field */
  private int is_audit;

  private String user_ip;

  /** default constructor */

  /** belong_type,add by beaf at 2008-12-02*/
  private String belong_type;

  private String belong_org; //csof

  public String getBelong_org() {
    return belong_org;
  }

  public void setBelong_org(String belong_org) {
    this.belong_org = belong_org;
  }

  private String sys_app;

  private String security_level;

  private String set_year;

  private String rg_code;

  public UserDTO(Map map) {
    this.address = (String) isValidOrNull(map.get("address"));
    //this.audit_date = (String) isValidOrNull(map.get("audit_date"));                 
    //this.audit_user = (String) isValidOrNull(map.get("audit_user"));                 
    //this.belong_org = (String) isValidOrNull(map.get("belong_org"));                 
    this.belong_type = (String) isValidOrNull(map.get("belong_type"));
    this.belong_org = (String) isValidOrNull(map.get("belong_org"));
    this.birthday = (String) isValidOrNull(map.get("birthday"));
    //this.ca_serial = (String) isValidOrNull(map.get("ca_serial"));                   
    //this.co_name = (String) isValidOrNull(map.get("co_name"));                       
    this.email = (String) isValidOrNull(map.get("email"));
    //this.emp_index = (Integer) isValidOrNull(map.get("emp_index"));                   
    //this.enabled = (Integer) isValidOrNull(map.get("enabled"));                       
    //this.gender = (Integer) isValidOrNull(map.get("gender"));                         
    //this.gp_org = (String) isValidOrNull(map.get("gp_org"));                         
    this.headship_code = (String) isValidOrNull(map.get("headship_code"));
    //this.identity_card = (String) isValidOrNull(map.get("identity_card"));           
    //this.imei = (String) isValidOrNull(map.get("imei"));                             
    //this.imsi = (String) isValidOrNull(map.get("imsi"));                             
    //this.init_password = (Integer) isValidOrNull(map.get("init_password"));           
    //this.is_audit = (Integer) isValidOrNull(map.get("is_audit"));                     
    //this.is_blacklist = (String) isValidOrNull(map.get("is_blacklist"));             
    //this.is_edit_pwd = (Integer) isValidOrNull(map.get("is_edit_pwd"));               
    //this.is_ever_locked = (Integer) isValidOrNull(map.get("is_ever_locked"));         
    //this.is_leaf = (Integer) isValidOrNull(map.get("is_leaf"));                       
    //this.is_three_security = (Integer) isValidOrNull(map.get("is_three_security"));   
    //this.last_ver = (String) isValidOrNull(map.get("last_ver"));                     
    //this.level_num = (Integer) isValidOrNull(map.get("level_num"));                   
    //this.locked_date = (String) isValidOrNull(map.get("locked_date"));               
    //this.mb_id = (String) isValidOrNull(map.get("mb_id"));                           
    this.mobile = (String) isValidOrNull(map.get("mobile"));
    //this.nickname = (String) isValidOrNull(map.get("nickname"));                     
    this.org_code = (String) isValidOrNull(map.get("org_code"));
    this.org_type = (String) isValidOrNull(map.get("org_type"));
    this.password = (String) isValidOrNull(map.get("password"));
    //this.payment_password = (String) isValidOrNull(map.get("payment_password"));     
    //this.photo = (String) isValidOrNull(map.get("photo"));                           
    //this.photo_blobid = (String) isValidOrNull(map.get("photo_blobid"));             
    this.rg_code = (String) isValidOrNull(map.get("rg_code"));
    //this.rtxuin = (String) isValidOrNull(map.get("rtxuin"));                         
    //this.security_level = (Integer) isValidOrNull(map.get("security_level"));         
    this.set_year = (String) isValidOrNull(map.get("set_year"));
    this.telephone = (String) isValidOrNull(map.get("telephone"));
    //this.title_tech = (String) isValidOrNull(map.get("title_tech"));                 
    this.user_code = (String) isValidOrNull(map.get("user_code"));
    this.user_id = (String) isValidOrNull(map.get("user_id"));
    this.user_name = (String) isValidOrNull(map.get("user_name"));
    //this.user_type = (Integer) isValidOrNull(map.get("user_type"));                   
    //this.weixin = (String) isValidOrNull(map.get("weixin"));  
  }

  static Object isValidOrNull(Object obj) {
    return obj == null ? null : obj;
  }

  public String getSet_year() {
    return set_year;
  }

  public void setSet_year(String set_year) {
    this.set_year = set_year;
  }

  public String getRg_code() {
    return rg_code;
  }

  public void setRg_code(String rg_code) {
    this.rg_code = rg_code;
  }

  public String getSecurity_level() {
    return security_level;
  }

  public void setSecurity_level(String security_level) {
    this.security_level = security_level;
  }

  public UserDTO() {
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public int getEnabled() {
    return enabled;
  }

  public void setEnabled(int enabled) {
    this.enabled = enabled;
  }

  public Integer getGender() {
    return gender;
  }

  public void setGender(Integer gender) {
    this.gender = gender;
  }

  public String getHeadship_code() {
    return headship_code;
  }

  public void setHeadship_code(String headship_code) {
    this.headship_code = headship_code;
  }

  public Integer getIs_leaf() {
    return is_leaf;
  }

  public void setIs_leaf(Integer is_leaf) {
    this.is_leaf = is_leaf;
  }

  public int getLevel_num() {
    return level_num;
  }

  public void setLevel_num(int level_num) {
    this.level_num = level_num;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getOrg_code() {
    return org_code;
  }

  public void setOrg_code(String org_code) {
    this.org_code = org_code;
  }

  public String getOrg_type() {
    return org_type;
  }

  public void setOrg_type(String org_type) {
    this.org_type = org_type;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getTelephone() {
    return telephone;
  }

  public void setTelephone(String telephone) {
    this.telephone = telephone;
  }

  public String getUser_code() {
    return user_code;
  }

  public void setUser_code(String user_code) {
    this.user_code = user_code;
  }

  public String getUser_id() {
    return user_id;
  }

  public void setUser_id(String user_id) {
    this.user_id = user_id;
  }

  public String getUser_name() {
    return user_name;
  }

  public void setUser_name(String user_name) {
    this.user_name = user_name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getBirthday() {
    return birthday;
  }

  public void setBirthday(String birthday) {
    this.birthday = birthday;
  }

  public int getIs_audit() {
    return is_audit;
  }

  public void setIs_audit(int is_audit) {
    this.is_audit = is_audit;
  }

  public String getUser_ip() {
    return user_ip;
  }

  public void setUser_ip(String user_ip) {
    this.user_ip = user_ip;
  }

  public String getSession_id() {
    return session_id;
  }

  public void setSession_id(String session_id) {
    this.session_id = session_id;
  }

  public String getBelong_type() {
    return belong_type;
  }

  public void setBelong_type(String belong_type) {
    this.belong_type = belong_type;
  }

  public String getSys_app() {
    return sys_app;
  }

  public void setSys_app(String sys_app) {
    this.sys_app = sys_app;
  }

}
