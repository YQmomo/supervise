package gov.df.fap.bean.workflow;

import java.io.Serializable;

/** @author  CodeGenerator */
public class SysWfConditionLine implements Serializable {

  /**
  * 
  */
  private static final long serialVersionUID = 2351278529430599332L;

  private String CONDITION_ID;

  private String LINE_ID;

  private SysWfCondition sysWfConditions;

  private String CONDITION_TABLE_NAME;

  private String COLUMN_NAME;

  private String OPERATOR;

  private String CONSTANT;

  private String LOGIC_OPERATOR;

  private String CREATE_USER;

  private String CREATE_DATE;

  private String LATEST_OP_USER;

  private String LATEST_OP_DATE;

  private String LEFT_PARE;

  private String RIGHT_PARE;

  private String columnType;

  private String LAST_VER;

  private String LEFT_PARAID;

  private String RIGHT_PARAID;

  private Long LINE_SORT;

  private String LEFT_PARANAME;

  private String RIGHT_PARANAME;

  //liwenquan add 2013-08-16
  private String LEFT_PARAVALUETYPE;

  //liwenquan add 2013-08-16
  private String RIGHT_PARAVALUETYPE;

  //参数类型1 要素 2非要素liwenquan add 2013-08-16
  private String para_type;

  public String getLEFT_PARAVALUETYPE() {
    return LEFT_PARAVALUETYPE;
  }

  public void setLEFT_PARAVALUETYPE(String lEFT_PARAVALUETYPE) {
    LEFT_PARAVALUETYPE = lEFT_PARAVALUETYPE;
  }

  //add by liuzw 20120420 增加对多财政多年度的支持
  private String RG_CODE;

  public String getRG_CODE() {
    return RG_CODE;
  }

  public void setRG_CODE(String rG_CODE) {
    RG_CODE = rG_CODE;
  }

  private int SET_YEAR;

  public int getSET_YEAR() {
    return this.SET_YEAR;
  }

  public void setSET_YEAR(int SET_YEAR) {
    this.SET_YEAR = SET_YEAR;
  }

  public String getRIGHT_PARANAME() {
    return RIGHT_PARANAME;
  }

  public void setRIGHT_PARANAME(String right_paraname) {
    RIGHT_PARANAME = right_paraname;
  }

  public String getLEFT_PARANAME() {
    return LEFT_PARANAME;
  }

  public void setLEFT_PARANAME(String left_paraname) {
    LEFT_PARANAME = left_paraname;
  }

  public String getCOLUMN_NAME() {
    return COLUMN_NAME;
  }

  public void setCOLUMN_NAME(String column_name) {
    COLUMN_NAME = column_name;
  }

  public String getColumnType() {
    return columnType;
  }

  public void setColumnType(String columnType) {
    this.columnType = columnType;
  }

  public String getCONDITION_TABLE_NAME() {
    return CONDITION_TABLE_NAME;
  }

  public void setCONDITION_TABLE_NAME(String condition_table_name) {
    CONDITION_TABLE_NAME = condition_table_name;
  }

  public String getCONSTANT() {
    return CONSTANT;
  }

  public void setCONSTANT(String constant) {
    CONSTANT = constant;
  }

  public String getCREATE_DATE() {
    return CREATE_DATE;
  }

  public void setCREATE_DATE(String create_date) {
    CREATE_DATE = create_date;
  }

  public String getCREATE_USER() {
    return CREATE_USER;
  }

  public void setCREATE_USER(String create_user) {
    CREATE_USER = create_user;
  }

  public String getLAST_VER() {
    return LAST_VER;
  }

  public void setLAST_VER(String last_ver) {
    LAST_VER = last_ver;
  }

  public String getLATEST_OP_DATE() {
    return LATEST_OP_DATE;
  }

  public void setLATEST_OP_DATE(String latest_op_date) {
    LATEST_OP_DATE = latest_op_date;
  }

  public String getLATEST_OP_USER() {
    return LATEST_OP_USER;
  }

  public void setLATEST_OP_USER(String latest_op_user) {
    LATEST_OP_USER = latest_op_user;
  }

  public String getLEFT_PARAID() {
    return LEFT_PARAID;
  }

  public void setLEFT_PARAID(String left_paraid) {
    LEFT_PARAID = left_paraid;
  }

  public String getLEFT_PARE() {
    return LEFT_PARE;
  }

  public void setLEFT_PARE(String left_pare) {
    LEFT_PARE = left_pare;
  }

  public String getLINE_ID() {
    return LINE_ID;
  }

  public void setLINE_ID(String line_id) {
    LINE_ID = line_id;
  }

  public Long getLINE_SORT() {
    return LINE_SORT;
  }

  public void setLINE_SORT(Long line_sort) {
    LINE_SORT = line_sort;
  }

  public String getLOGIC_OPERATOR() {
    return LOGIC_OPERATOR;
  }

  public void setLOGIC_OPERATOR(String logic_operator) {
    LOGIC_OPERATOR = logic_operator;
  }

  public String getOPERATOR() {
    return OPERATOR;
  }

  public void setOPERATOR(String operator) {
    OPERATOR = operator;
  }

  public String getRIGHT_PARAID() {
    return RIGHT_PARAID;
  }

  public void setRIGHT_PARAID(String right_paraid) {
    RIGHT_PARAID = right_paraid;
  }

  public String getRIGHT_PARE() {
    return RIGHT_PARE;
  }

  public void setRIGHT_PARE(String right_pare) {
    RIGHT_PARE = right_pare;
  }

  public SysWfCondition getSysWfConditions() {
    return sysWfConditions;
  }

  public void setSysWfConditions(SysWfCondition sysWfConditions) {
    this.sysWfConditions = sysWfConditions;
  }

  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  public String getCONDITION_ID() {
    return CONDITION_ID;
  }

  public void setCONDITION_ID(String condition_id) {
    CONDITION_ID = condition_id;
  }

  public String getRIGHT_PARAVALUETYPE() {
    return RIGHT_PARAVALUETYPE;
  }

  public void setRIGHT_PARAVALUETYPE(String rIGHT_PARAVALUETYPE) {
    RIGHT_PARAVALUETYPE = rIGHT_PARAVALUETYPE;
  }

  public String getPara_type() {
    return para_type;
  }

  public void setPara_type(String para_type) {
    this.para_type = para_type;
  }

}
