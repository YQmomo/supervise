package gov.df.supervise.api.csofinfo;

import java.util.List;

public interface csofInfoService {
  public List getDisplayTitle(String chr_id);

  public List getYear(String ele_code);

  public List getTreeByuser(String ele_code);

  public List getData(String ele_code);

}
