package gov.df.supervise.api.workflow;

public interface WorkFlow {

  public boolean doWorkFlow(String menu_id, String entity_id, String billtype_code, String op_type, String op_name,
    String opinion);

}
