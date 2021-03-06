package gov.df.supervise.api.attachment;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

/**
 * 附件服务接口
 * @author w
 * 2017-08-22 
 *
 */
public interface AttachmentService {

  /**
   * 批量查询附件
   * @param attachIds 附件id集合
   * @return 
   * @throws Exception
   */
  public List getAttachList(String condition, String pageInfo) throws Exception;

  /**
   * 通过Id查询附件
   */
  public List getAttachById(String id);

  /**
   * 查询全部附件
   * @return  附件集合 List
   */
  public List getAllAttachments();

  /**
   * 删除附件
   * @param ids 附件id集合字符串
   * @return 执行条目数
   */
  public int deleteAttachments(String ids);

  /**
   * 附件上传后保存附件基本信息
   * @param attachMap 附件参数集
   */
  public void saveAttachment(Map<String, String> attachMap);

  /**
   * 获取上传类型
   * @return
   */
  public String getUploadMode();

  //获取ftp服务器地址
  public String getUploadFtpIp();

  //获取ftp服务器端口
  public String getUploadFtpPort();

  //获取ftp服务器用户名
  public String getUploadFtpUser();

  //获取ftp服务器密码
  public String getUploadFtpPassword();

  /**
   * 获取上传路径
   * @return
   */
  public String getUploadRootPath();

  public List getAttachByIds(String ids);

  public int getCount();

  public Map upload(MultipartFile file, String pathFile, String FTP_ADDRESS, String FTP_PORT, String FTP_USERNAME,
    String FTP_PASSWORD);

  //ftp下载
  public Map download(String filePath, String filename, String FTP_ADDRESS, String FTP_PORT, String FTP_USERNAME,
    String FTP_PASSWORD);

  //预览
  public Map<String, String> previewFile(String fileName, String filePath, String fileType, String rootPath,
    HttpServletResponse response) throws Exception;

  /**
  * 查询上传的文件名
  */
  public List getNameById(String id);

  /**
   * 文件获取接口 通过文件id获取
   * @param fileId 文件id
   * @return 文件信息Map
   */
  public Map getFileById(String fileId);

  public boolean deletePreviewFile(String srcPath) throws Exception;

}
