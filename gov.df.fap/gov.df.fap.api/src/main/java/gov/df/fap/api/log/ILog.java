/*
 * @(#)ILog.java	1.0 22/03/06
 *
 * Copyright 2006 by Founder Sprint 1st, Inc. All rights reserved.
 */
package gov.df.fap.api.log;

import gov.df.fap.bean.log.LogDTO;

import java.util.Map;

/**
 * 日志组件服务端对外接口
 * @version 1.0
 * @author victor
 * @since java 1.4.1
*/
public interface ILog {
  /**
   * 采用log4j方式输出调试信息
   * @param className 调用日志类(caller)
   * @param infoMessage 调试信息
   */
  public void info(String className, String infoMessage);

  /**
   * 采用log4j方式输出调试信息
   * @param className 调用日志类(caller)
   * @param debugMessage 调试信息
   */
  public void debug(String className, String debugMessage);

  /**
   * 采用log4j方式输出错误信息
   * @param className 调用日志类(caller)
   * @param errorMessage 错误信息
   */
  public void error(String className, String errorMessage);

  /**
   * 采用数据库输出业务操作日志
   * @param data 业务操作日志信息对象
   */
  public void writeLog(LogDTO data) throws Exception;

  /**
   * 采用数据库输出业务操作日志
   * @param data 业务操作日志信息对象
   */
  public void writeMapLog(Map data) throws Exception;
  
  public void saveLog(LogDTO data) throws Exception;
}
