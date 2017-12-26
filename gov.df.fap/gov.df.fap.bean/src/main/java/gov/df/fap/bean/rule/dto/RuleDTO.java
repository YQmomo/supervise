package gov.df.fap.bean.rule.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class RuleDTO implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5314244984014741400L;

	/** identifier field */
    private String RULE_ID;

    /** persistent field */
    private String RULE_CODE;

    /** nullable persistent field */
    private String RULE_NAME;

    /** nullable persistent field */
    private String REMARK;

    /** persistent field */
    private String SET_YEAR;

    /** nullable persistent field */
    private int ENABLED;

    /** nullable persistent field */
    private String RULE_CLASSIFY;

    /** nullable persistent field */
    private String SYS_REMARK;

    /** nullable persistent field */
    private int RIGHT_TYPE;

    /** nullable persistent field */
    private String CREATE_USER;

    /** nullable persistent field */
    private String CREATE_DATE;

    /** nullable persistent field */
    private String LATEST_OP_USER;

    /** nullable persistent field */
    private String LATEST_OP_DATE; 
    
    /** not nullable field  add by kongcy */
    private String RG_CODE;


    public String getRG_CODE() {
		return RG_CODE;
	}

	public void setRG_CODE(String rG_CODE) {
		RG_CODE = rG_CODE;
	}

	public List right_group_list = new ArrayList();
    
    
    /** full constructor */
    public RuleDTO(String RULE_ID, String RULE_CODE, String RULE_NAME, String REMARK, String SET_YEAR, int ENABLED, String RULE_CLASSIFY, String SYS_REMARK, int RIGHT_TYPE, String CREATE_USER, String CREATE_DATE, String LATEST_OP_USER, String LATEST_OP_DATE) {
        this.RULE_ID = RULE_ID;
        this.RULE_CODE = RULE_CODE;
        this.RULE_NAME = RULE_NAME;
        this.REMARK = REMARK;
        this.SET_YEAR = SET_YEAR;
        this.ENABLED = ENABLED;
        this.RULE_CLASSIFY = RULE_CLASSIFY;
        this.SYS_REMARK = SYS_REMARK;
        this.RIGHT_TYPE = RIGHT_TYPE;
        this.CREATE_USER = CREATE_USER;
        this.CREATE_DATE = CREATE_DATE;
        this.LATEST_OP_USER = LATEST_OP_USER;
        this.LATEST_OP_DATE = LATEST_OP_DATE;
    }

    /** default constructor */
    public RuleDTO() {
    }

    /** minimal constructor */
    public RuleDTO(String RULE_ID, String RULE_CODE, String SET_YEAR) {
        this.RULE_ID = RULE_ID;
        this.RULE_CODE = RULE_CODE;
        this.SET_YEAR = SET_YEAR;
    }

    public String getRULE_ID() {
        return this.RULE_ID;
    }

    public void setRULE_ID(String RULE_ID) {
        this.RULE_ID = RULE_ID;
    }

    public String getRULE_CODE() {
        return this.RULE_CODE;
    }

    public void setRULE_CODE(String RULE_CODE) {
        this.RULE_CODE = RULE_CODE;
    }

    public String getRULE_NAME() {
        return this.RULE_NAME;
    }

    public void setRULE_NAME(String RULE_NAME) {
        this.RULE_NAME = RULE_NAME;
    }

    public String getREMARK() {
        return this.REMARK;
    }

    public void setREMARK(String REMARK) {
        this.REMARK = REMARK;
    }

    public String getSET_YEAR() {
        return this.SET_YEAR;
    }

    public void setSET_YEAR(String SET_YEAR) {
        this.SET_YEAR = SET_YEAR;
    }

    public int getENABLED() {
        return this.ENABLED;
    }

    public void setENABLED(int ENABLED) {
        this.ENABLED = ENABLED;
    }

    public String getRULE_CLASSIFY() {
        return this.RULE_CLASSIFY;
    }

    public void setRULE_CLASSIFY(String RULE_CLASSIFY) {
        this.RULE_CLASSIFY = RULE_CLASSIFY;
    }

    public String getSYS_REMARK() {
        return this.SYS_REMARK;
    }

    public void setSYS_REMARK(String SYS_REMARK) {
        this.SYS_REMARK = SYS_REMARK;
    }

    public int getRIGHT_TYPE() {
        return this.RIGHT_TYPE;
    }

    public void setRIGHT_TYPE(int RIGHT_TYPE) {
        this.RIGHT_TYPE = RIGHT_TYPE;
    }

    public String getCREATE_USER() {
        return this.CREATE_USER;
    }

    public void setCREATE_USER(String CREATE_USER) {
        this.CREATE_USER = CREATE_USER;
    }

    public String getCREATE_DATE() {
        return this.CREATE_DATE;
    }

    public void setCREATE_DATE(String CREATE_DATE) {
        this.CREATE_DATE = CREATE_DATE;
    }

    public String getLATEST_OP_USER() {
        return this.LATEST_OP_USER;
    }

    public void setLATEST_OP_USER(String LATEST_OP_USER) {
        this.LATEST_OP_USER = LATEST_OP_USER;
    }

    public String getLATEST_OP_DATE() {
        return this.LATEST_OP_DATE;
    }

    public void setLATEST_OP_DATE(String LATEST_OP_DATE) {
        this.LATEST_OP_DATE = LATEST_OP_DATE;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("RULE_ID", getRULE_ID())
            .toString();
    }

}
