package gov.df.supervise.api.document;

import gov.df.fap.bean.util.FPaginationDTO;

import java.util.List;

/**
 * 政策法规库接口
 * @author Administrator
 *
 */
public interface csofDocumentService {
  /**
   * 查询法规类型
   * @param ele_code
   * @return
   */
  public List getTreeByOid(String ele_code);

  /**
   * 新增政策法规
   */
  public void saveDocument(String id, String doctype_id, String title, String summary);

  /**
   * 通过法规类型查询政策法规
   */
  public List getDocumentById(String id, FPaginationDTO page);

  /**
   * 通过法规类型查询政策法规数量
   */
  public int getDocumentCountById(String id);

  /**
   * 查询政策法规
   */
  public List getDocument(FPaginationDTO page);

  /**
   * 查询政策法规数量
   */
  public int getDocumentCount();

  /**
   * 编辑政策法规
   */
  public void updateDocument(String id, String doctype_id, String title, String summary);

  /**
   * 删除政策法规
   */
  public void deleteDocument(String id);

  /**
   * 通过政策法规类型id查询政策法规类型名称
   */
  public String getDoctypeName(String doctype_id);

  /**
   * 查询工作台政策法规
   */
  public List getHomeDocument();
}
