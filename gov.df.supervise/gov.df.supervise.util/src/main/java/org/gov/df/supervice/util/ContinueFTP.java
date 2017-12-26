package org.gov.df.supervice.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/** */
/**  
* 支持断点续传的FTP实用类  
* @version 0.1 实现基本断点上传下载  
* @version 0.2 实现上传下载进度汇报  
* @version 0.3 实现中文目录创建及中文文件创建，添加对于中文的支持  
*/
public class ContinueFTP implements Runnable {

  //枚举类UploadStatus代码 

  public enum UploadStatus {
    Create_Directory_Fail, //远程服务器相应目录创建失败 
    Create_Directory_Success, //远程服务器创建目录成功 
    Upload_New_File_Success, //上传新文件成功 
    Upload_New_File_Failed, //上传新文件失败 
    File_Exits, //文件已经存在 
    Remote_Bigger_Local, //远程文件大于本地文件 
    Upload_From_Break_Success, //断点续传成功 
    Upload_From_Break_Failed, //断点续传失败 
    Delete_Remote_Faild; //删除远程文件失败 
  }

  //枚举类DownloadStatus代码 
  public enum DownloadStatus {
    Remote_File_Noexist, //远程文件不存在 
    Local_Bigger_Remote, //本地文件大于远程文件 
    Download_From_Break_Success, //断点下载文件成功 
    Download_From_Break_Failed, //断点下载文件失败 
    Download_New_Success, //全新下载文件成功 
    Download_New_Failed; //全新下载文件失败 
  }

  public static FTPClient ftpClient = new FTPClient();

  private String ftpURL, username, pwd, ftpport, file1, file2;

  public ContinueFTP(String _ftpURL, String _username, String _pwd, String _ftpport, String _file1, String _file2) {
    //设置将过程中使用到的命令输出到控制台   
    ftpURL = _ftpURL;
    username = _username;
    pwd = _pwd;
    ftpport = _ftpport;
    file1 = _file1;
    file2 = _file2;
    this.ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
  }

  /** */
  /**  
  * 连接到FTP服务器  
  * @param hostname 主机名  
  * @param port 端口  
  * @param username 用户名  
  * @param password 密码  
  * @return 是否连接成功  
  * @throws IOException  
  */
  public static boolean connect(String hostname, int port, String username, String password) throws IOException {

    ftpClient.connect(hostname, port);
    ftpClient.setControlEncoding("GBK");
    if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
      if (ftpClient.login(username, password)) {
        return true;
      }
    }
    disconnect();
    return false;
  }

  /** */
  /**  
  * 从FTP服务器上下载文件,支持断点续传，上传百分比汇报  
  * @param remote 远程文件路径  
  * @param local 本地文件路径  
  * @return 上传的状态  
  * @throws IOException  
  */
  public static Map<String, Object> download(String remote, String local) throws IOException {
    Map<String, Object> map = new HashMap<String, Object>();
    //设置被动模式   
    ftpClient.enterLocalPassiveMode();
    //设置以二进制方式传输   
    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
    DownloadStatus result;

    //检查远程文件是否存在   
    FTPFile[] files = ftpClient.listFiles(new String(remote.getBytes("GBK"), "iso-8859-1"));
    if (files.length != 1) {
      System.out.println("远程文件不存在");
      map.put("process", null);
      map.put("status", DownloadStatus.Remote_File_Noexist);
      return map;
    }

    long lRemoteSize = files[0].getSize();
    File f = new File(local);
    //本地存在文件，进行断点下载   
    if (f.exists()) {
      long localSize = f.length();
      //判断本地文件大小是否大于远程文件大小   
      if (localSize >= lRemoteSize) {
        System.out.println("本地文件大于远程文件，下载中止");
        map.put("process", null);
        map.put("status", DownloadStatus.Local_Bigger_Remote);
        return map;
      }

      //进行断点续传，并记录状态   
      FileOutputStream out = new FileOutputStream(f, true);
      ftpClient.setRestartOffset(localSize);
      InputStream in = ftpClient.retrieveFileStream(new String(remote.getBytes("GBK"), "iso-8859-1"));
      byte[] bytes = new byte[1024];
      long step = lRemoteSize / 100 == 0 ? 1 : lRemoteSize / 100;

      long process = localSize / step;
      int c;
      while ((c = in.read(bytes)) != -1) {
        out.write(bytes, 0, c);
        localSize += c;
        long nowProcess = localSize / step;
        if (nowProcess > process) {
          process = nowProcess;
          if (process % 10 == 0)
            System.out.println("下载进度：" + process);
          //TODO 更新文件下载进度,值存放在process变量中   
        }
      }
      in.close();
      out.close();
      boolean isDo = ftpClient.completePendingCommand();
      if (isDo) {
        result = DownloadStatus.Download_From_Break_Success;
      } else {
        result = DownloadStatus.Download_From_Break_Failed;
      }
    } else {
      OutputStream out = new FileOutputStream(f);
      InputStream in = ftpClient.retrieveFileStream(new String(remote.getBytes("GBK"), "iso-8859-1"));
      byte[] bytes = new byte[1024];
      long step = lRemoteSize / 100 == 0 ? 1 : lRemoteSize / 100;
      long process = 0;
      long localSize = 0L;
      int c;
      while ((c = in.read(bytes)) != -1) {
        out.write(bytes, 0, c);
        localSize += c;
        long nowProcess = localSize / step;
        if (nowProcess > process) {
          process = nowProcess;
          if (process % 10 == 0)
            System.out.println("下载进度：" + process);
          //TODO 更新文件下载进度,值存放在process变量中   
        }
      }
      in.close();
      out.close();
      boolean upNewStatus = ftpClient.completePendingCommand();
      if (upNewStatus) {

        result = DownloadStatus.Download_New_Success;
        map.put("process", process);
        map.put("status", result);
      } else {
        result = DownloadStatus.Download_New_Failed;
        map.put("process", process);
        map.put("status", result);
      }
    }
    return map;
  }

  /** */
  /**  
  * 上传文件到FTP服务器，支持断点续传  
  * @param local 本地文件名称，绝对路径  
  * @param remote 远程文件路径，使用/home/directory1/subdirectory/file.ext或是 http://www.guihua.org /subdirectory/file.ext 按照Linux上的路径指定方式，支持多级目录嵌套，支持递归创建不存在的目录结构  
  * @return 上传结果  
  * @throws IOException  
  */
  public static Map<String, Object> upload(MultipartFile local, String remote) throws IOException {
    //设置PassiveMode传输   
    ftpClient.enterLocalPassiveMode();
    //设置以二进制流的方式传输   
    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
    ftpClient.setControlEncoding("GBK");
    UploadStatus status;
    Map<String, Object> result = new HashMap<String, Object>();
    //对远程目录的处理   
    String remoteFileName = remote;
    if (remote.contains("/")) {
      remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);
      //创建服务器远程目录结构，创建失败直接返回   
      if (CreateDirecroty(remote, ftpClient) == UploadStatus.Create_Directory_Fail) {
        result.put("status", UploadStatus.Create_Directory_Fail);
        result.put("process", null);
        return result;
      }
    }

    //检查远程是否存在文件   
    FTPFile[] files = ftpClient.listFiles(new String(remoteFileName.getBytes("GBK"), "iso-8859-1"));
    if (files.length == 1) {
      long remoteSize = files[0].getSize();
      // File f = new File(local);
      long localSize = local.getSize();
      if (remoteSize == localSize) {
        result.put("status", UploadStatus.File_Exits);
        result.put("process", null);
        return result;
        //return UploadStatus.File_Exits;
      } else if (remoteSize > localSize) {
        //return UploadStatus.Remote_Bigger_Local;
        result.put("status", UploadStatus.Remote_Bigger_Local);
        result.put("process", null);
        return result;
      }

      //尝试移动文件内读取指针,实现断点续传   
      result = uploadFile(remoteFileName, local, ftpClient, remoteSize);

      //如果断点续传没有成功，则删除服务器上文件，重新上传   
      if (result.get("status") == UploadStatus.Upload_From_Break_Failed) {
        if (!ftpClient.deleteFile(remoteFileName)) {
          // return UploadStatus.Delete_Remote_Faild;
          result.put("status", UploadStatus.Delete_Remote_Faild);
          result.put("process", null);
          return result;
        }
        result = uploadFile(remoteFileName, local, ftpClient, 0);
      }
    } else {
      result = uploadFile(remoteFileName, local, ftpClient, 0);
    }
    return result;
  }

  /** */
  /**  
  * 断开与远程服务器的连接  
  * @throws IOException  
  */
  public static void disconnect() throws IOException {
    if (ftpClient.isConnected()) {
      ftpClient.disconnect();
    }
  }

  /** */
  /**  
  * 递归创建远程服务器目录  
  * @param remote 远程服务器文件绝对路径  
  * @param ftpClient FTPClient 对象  
  * @return 目录创建是否成功  
  * @throws IOException  
  */
  public static UploadStatus CreateDirecroty(String remote, FTPClient ftpClient) throws IOException {
    UploadStatus status = UploadStatus.Create_Directory_Success;
    String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
    if (!directory.equalsIgnoreCase("/")
      && !ftpClient.changeWorkingDirectory(new String(directory.getBytes("GBK"), "iso-8859-1"))) {
      //如果远程目录不存在，则递归创建远程服务器目录   
      int start = 0;
      int end = 0;
      if (directory.startsWith("/")) {
        start = 1;
      } else {
        start = 0;
      }
      end = directory.indexOf("/", start);
      while (true) {
        String subDirectory = new String(remote.substring(start, end).getBytes("GBK"), "iso-8859-1");
        if (!ftpClient.changeWorkingDirectory(subDirectory)) {
          if (ftpClient.makeDirectory(subDirectory)) {
            ftpClient.changeWorkingDirectory(subDirectory);
          } else {
            System.out.println("创建目录失败");
            return UploadStatus.Create_Directory_Fail;
          }
        }

        start = end + 1;
        end = directory.indexOf("/", start);

        //检查所有目录是否创建完毕   
        if (end <= start) {
          break;
        }
      }
    }
    return status;
  }

  /** */
  /**  
  * 上传文件到服务器,新上传和断点续传  
  * @param remoteFile 远程文件名，在上传之前已经将服务器工作目录做了改变  
  * @param localFile 本地文件 File句柄，绝对路径  
  * @param processStep 需要显示的处理进度步进值  
  * @param ftpClient FTPClient 引用  
  * @return  
  * @throws IOException  
  */
  public static Map uploadFile(String remoteFile, MultipartFile localFile, FTPClient ftpClient, long remoteSize)
    throws IOException {
    UploadStatus status;
    Map<String, Object> map = new HashMap<String, Object>();
    //显示进度的上传   
    long step = localFile.getSize() / 100 == 0 ? 1 : localFile.getSize() / 100;

    System.out.println(localFile.getSize());
    String errorCode = "0";
    String errorMsg = "";
    long process = 0;
    long localreadbytes = 0L;
    CommonsMultipartFile cf = (CommonsMultipartFile) localFile;
    DiskFileItem fi = (DiskFileItem) cf.getFileItem();
    File localFile1 = fi.getStoreLocation();
    //File localFile1 = (File) localFile;

    if (localFile1.exists()) {
      RandomAccessFile raf = new RandomAccessFile(localFile1, "r");
      OutputStream out = ftpClient.appendFileStream(new String(remoteFile.getBytes("GBK"), "iso-8859-1"));
      //断点续传   
      if (remoteSize > 0) {
        ftpClient.setRestartOffset(remoteSize);
        process = remoteSize / step;
        raf.seek(remoteSize);
        localreadbytes = remoteSize;
      }
      byte[] bytes = new byte[1024];
      int c;
      while ((c = raf.read(bytes)) != -1) {
        System.out.println(bytes.toString());
        out.write(bytes, 0, c);
        localreadbytes += c;
        if (localreadbytes / step != process) {
          process = localreadbytes / step;
          System.out.println("上传进度:" + process);
          //TODO 汇报上传状态   
        }
      }
      out.flush();
      raf.close();
      out.close();
      boolean result = ftpClient.completePendingCommand();
      if (remoteSize > 0) {
        status = result ? UploadStatus.Upload_From_Break_Success : UploadStatus.Upload_From_Break_Failed;
      } else {
        status = result ? UploadStatus.Upload_New_File_Success : UploadStatus.Upload_New_File_Failed;
      }
      map.put("status", status);
      map.put("process", process);
      errorCode = "0";
    } else {
      map.put("status", "文件未上传成功");
      errorCode = "1";
      errorMsg = "文件未上传成功";

    }
    map.put("process", process);
    map.put("errorCode", errorCode);
    map.put("errorMsg", errorMsg);
    return map;
  }

  @Test
  public void run() {
    //    // TODO Auto-generated method stub 
    //
    //    try {
    //      this.connect(ftpURL, new java.lang.Integer(ftpport), username, pwd);
    //      //           myFtp.ftpClient.makeDirectory(new String(" 电视剧".getBytes("GBK"),"iso-8859-1"));   
    //      //           myFtp.ftpClient.changeWorkingDirectory(new String(" 电视剧".getBytes("GBK"),"iso-8859-1"));   
    //      //           myFtp.ftpClient.makeDirectory(new String(" 走西口".getBytes("GBK"),"iso-8859-1"));   
    //      //           System.out.println(myFtp.upload("http://www.5a520.cn /yw.flv", "/yw.flv",5));   
    //      //           System.out.println(myFtp.upload("http://www.5a520.cn /走西口24.mp4","/央视走西口/新浪网/走西口 24.mp4"));   
    //      //   System.out.println(myFtp.download("/ 央视走西口/新浪网/走西口24.mp4", "E:\\走西口242.mp4")); 
    //
    //      this.download(file1, file2);
    //      //  System.out.println(myFtp.upload("c:\\a.iso", "/a.iso")); 
    //      this.disconnect();
    //    } catch (IOException e) {
    //      System.out.println("连接FTP出错：" + e.getMessage());
    //    }
  }

  public static boolean deleteFtpFile(String url, int port, String username, String password, String remotePath,
    String fileName) {
    boolean success = false;
    FTPClient ftp = new FTPClient();
    try {
      int reply;

      // 连接FTP服务器  
      if (port > -1) {
        ftp.connect(url, port);
      } else {
        ftp.connect(url);
      }

      // 登录  
      ftp.login(username, password);
      reply = ftp.getReplyCode();
      if (!FTPReply.isPositiveCompletion(reply)) {
        ftp.disconnect();
        return success;
      }

      // 转移到FTP服务器目录  
      ftp.changeWorkingDirectory(remotePath);
      success = ftp.deleteFile(remotePath);
      ftp.logout();
    } catch (IOException e) {
      // logger.error(EXCEPTION_NAME, e);  
      success = false;
    } finally {
      if (ftp.isConnected()) {
        try {
          ftp.disconnect();
        } catch (IOException e) {
          //logger.error(EXCEPTION_NAME, e);  
        }
      }
    }
    return success;
  }
}