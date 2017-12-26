package gov.df.fap.service.workflow;

import gov.df.fap.api.gl.interfaces.IVoucherService;
import gov.df.fap.api.workflow.IBillTypeServices;
import gov.df.fap.api.workflow.IInspectService;
import gov.df.fap.api.workflow.ISysRegulationListener;
import gov.df.fap.api.workflow.IWorkFlow;
import gov.df.fap.api.workflow.message.MsgService;
import gov.df.fap.api.workflow.sysregulation.IWorkFlowRuleFactory;
import gov.df.fap.bean.message.MsgServiceParam;
import gov.df.fap.bean.rule.FVoucherDTO;
import gov.df.fap.bean.workflow.FBillTypeDTO;
import gov.df.fap.service.util.UtilCache;
import gov.df.fap.service.util.dao.GeneralDAO;
import gov.df.fap.service.util.datasource.TypeOfDB;
import gov.df.fap.service.util.exceptions.workflow.WFErrorNodeException;
import gov.df.fap.service.util.exceptions.workflow.WFIllegalSqlOperateException;
import gov.df.fap.service.util.exceptions.workflow.WorkFlowExceptionConstants;
import gov.df.fap.service.util.sessionmanager.SessionUtil;
import gov.df.fap.util.StringUtil;
import gov.df.fap.util.Tools;
import gov.df.fap.util.date.DateHandler;
import gov.df.fap.util.exception.FAppException;
import gov.df.fap.util.xml.XMLData;


import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Title: 工作流组实现类
 * </p>
 * <p>
 * </p>
 * <p>
 * Copyright: Copyright (c) 2015-2017 北京用友政务软件有限公司
 * </p>
 * <p>
 * Company:北京用友政务软件有限公司
 * </p>
 * 
 * @version 1.0
 * @author hult
 * @param <CsEntity>
 * @since java 1.6
 */
@Service
public class WorkFlowBO<CsEntity> implements IWorkFlow {

  @Autowired
  @Qualifier("generalDAO")
  GeneralDAO dao;

  @Autowired
  private IWorkFlowRuleFactory workFlowRuleFactory;

  @Autowired
  private IVoucherService voucherService;

  @Autowired
  private IBillTypeServices billtype;

  public GeneralDAO getDao() {
    return dao;
  }

  public void setDao(GeneralDAO dao) {
    this.dao = dao;
  }

  /**工作流节点关联单表字段名 */
  private static Map ITEMBILLFIELDS = new HashMap();

  /**工作流转线 的上下节点和条件 */
  private static Map CONDITIONS = new HashMap();

  /**工作流转 节点的全部上面的同步节点 */
  private static Map PRENODES = new HashMap();

  /**同步节点 */
  private static Map GATHERFLAG = new HashMap();

  /** 
   * 删除工作流缓存
   */
  public static void clearCache() {
    ITEMBILLFIELDS.clear();
    CONDITIONS.clear();
    PRENODES.clear();
    GATHERFLAG.clear();
  }

  /**
   * 批量单数据处理操作。
   * 
   * @param menuid
   *            菜单ID
   * @param roleid
   *            角色ID
   * @param actiontype
   *            操作类型CODE
   * @param operationname
   *            操作名称
   * @param operationdate
   *            操作时间
   * @param operationremark
   *            操作意见
   * @param detail_table_name
   *            明细表名
   * @param billDtos
   *            业务单DTO列表，必须含有明细，通过getDetails()获取明细
   * @param auto_tolly_flag
   *            是否通过工作流自动记账 
   * @param inspect_flag 是否资金监控 
   * @return List（内部含BillObject,都有details)
   * @throws Exception
   *             错误信息
   */
  public List doBatchBillProcessNextObj(String menuid, String roleid, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List billDtos, boolean auto_tolly_flag,
    boolean inspect_flag) throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) ((FVoucherDTO) billDtos.get(0)).getDetails().get(0));
    }
    String[] creates = getCreateUser();
    String create_user = creates[0];
    String create_user_id = creates[1];
    Session session = dao.getSession();
    int tollyFlag = -9; //记账标记
    int gatherFlag = 1;//是否同步节点 0同步 1异步   
    try {
      long wf_id = 0;
      Connection conn = session.connection();
      String currentNodeId = null;
      /* 所有明细 */
      ArrayList allDetails = new ArrayList();
      PreparedStatement insertCurrentTaskCachePsmt = getInsertCurrentTaskCachePsmt(conn);
      PreparedStatement insertCurrentItemPsmt = getInsertCurrentItemPsmt(conn);
      long nCurrentNodeId = 0;
      boolean cangonext = false;
      for (int i = 0, n = billDtos.size(); i < n; i++) {
        FVoucherDTO billDto = (FVoucherDTO) billDtos.get(i);
        List details = billDto.getDetails();
        FVoucherDTO detailDto = (FVoucherDTO) details.get(0);

        //获取第一个明细的当前流程节点
        currentNodeId = getCurrentNodeId(menuid, roleid, detailDto.getVou_id(), conn);
        nCurrentNodeId = Long.parseLong(currentNodeId);
        tollyFlag = getTollyFlag(nCurrentNodeId, actionType, conn);

        //取节点下的工作流
        List conditions = getNextConditionId(currentNodeId, conn);

        for (int j = 0; j < conditions.size(); j++) {
          //菜单 角色是流转线的条件
          detailDto.setModule_id(menuid);
          detailDto.setRole_id(roleid);

          Object[] condition = (Object[]) conditions.get(j);
          if ("#".equals(condition[2]) || workFlowRuleFactory.isMatchByBSH((String) condition[2], detailDto)) {
            cangonext = true;//满足流转线条件 流程向下一步
            wf_id = ((Long) condition[0]).longValue();
            final Long lNextNodeId = (Long) condition[1];
            final long next_node_id = lNextNodeId.longValue();
            gatherFlag = this.getGatherFlag(lNextNodeId, conn);

            for (int k = 0, m = details.size(); k < m; k++) {
              detailDto = (FVoucherDTO) details.get(k);
              //设置单号（默认前提：单中有明细，就该有单号
              detailDto.setVou_bill_id(billDto.getVou_id());
              //设置工作流转
              setValues4InsertCurrentTaskCachePsmt(insertCurrentTaskCachePsmt, wf_id, detail_table_name,
                detailDto.getVou_id(), nCurrentNodeId, ((Long) condition[3]).longValue(), actionType, create_user,
                operationName, operationDate, operationRemark, detailDto.getVou_money(), tollyFlag,
                detailDto.getBilltype_code(), detailDto.getRcid(), detailDto.getCcid(), detailDto.getSet_month(),
                create_user_id, detailDto.getVou_bill_id());

              if (gatherFlag == 0)//如果是同步
              {
                if (existPreTask(detailDto.getVou_id(), wf_id, nCurrentNodeId, next_node_id, conn))//存在还未到达同步的节点的工作流
                {
                  continue;
                }
              }
              //设置工作流转节点
              if (next_node_id > 0)//0为结束节点
              {
                setValues4InsertCurrentItemPsmt(insertCurrentItemPsmt, detailDto.getVou_id(), next_node_id,
                  getNextItemBillId(detailDto, lNextNodeId, conn), detailDto.getRcid(), detailDto.getCcid(), "001",
                  detailDto.getMb_id(), detailDto.getAgency_id(), detailDto.getPb_id(), detailDto.getCb_id(),
                  detailDto.getIb_id(), detailDto.getAgency_id());
              }
            }
          }
        }
        if (!cangonext) {
          throw new WFErrorNodeException("主单id为" + billDto.getVou_id() + "明细id为" + detailDto.getVou_id()
            + "未找到下一步流转节点，请检查流转线");
        }
        //各个单的流转可能不同，但同一单的数据流转应该相同
        cangonext = false;
        allDetails.addAll(details);
      }

      //转变节点信息      
      //删除原节点当前信息
      int[] updates = deleteCurrentItem(allDetails, nCurrentNodeId, conn);
      for (int i = 0; i < updates.length; i++) {
        if (updates[i] <= 0) {
          throw new WFErrorNodeException("明细id为" + ((FVoucherDTO) allDetails.get(i)).getVou_id() + "未找到当前流程节点信息");
        }
      }
      //统一监控
      if (inspect_flag) {
        this.doInspect(String.valueOf(wf_id), currentNodeId, menuid, roleid, actionType, operationName, operationDate,
          operationRemark, detail_table_name, allDetails, conn);
      }
      //插入流程信息
      insertCurrentTaskCachePsmt.executeBatch();
      //删除上一个节点的的已完成信息
      deleteCompleteItem(allDetails, nCurrentNodeId, conn);
      //增加当前节点的的已完成信息
      insertCompleteItem(allDetails, nCurrentNodeId, conn, "002");
      //将当前的流转信息转移到历史表
      copyCurrentTask(allDetails, nCurrentNodeId, conn);
      //记录工作流转流程前后信息
      insertRoute(allDetails, nCurrentNodeId, conn);
      //删除原有的流程信息
      deleteCurrentTask(allDetails, nCurrentNodeId, conn);
      //将数据从临时表写入正式表
      copyCacheTasks(conn, wf_id);
      //插入节点信息
      insertCurrentItemPsmt.executeBatch();
      if (tollyFlag == 0)//在途审核记账
      {
        voucherService.updateBatch(allDetails);
      } else if (tollyFlag == 1) {//终审记账
        voucherService.auditBatch(allDetails);
      }
    } finally {
      dao.closeSession(session);
    }
    return billDtos;
  }

  /**
   * 批量明细数据处理操作。
   * 
   * @param menuid
   *            菜单ID
   * @param roleid
   *            角色ID
   * @param actiontype
   *            操作类型CODE
   * @param operationname
   *            操作名称
   * @param operationdate
   *            操作时间
   * @param operationremark
   *            操作意见
   * @param detail_table_name
   *            明细表名
   * @param detailDtos
   *            明细
   * @param auto_tolly_flag
   *            是否通过工作流自动记账 
   * @param inspect_flag 是否资金监控 
   * @return List（内部含Object)
   * @throws Exception
   *             错误信息
   */
  public List doBatchDetailProcessNextObj(String menuid, String roleid, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List detailDtos, boolean auto_tolly_flag,
    boolean inspect_flag) throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) detailDtos.get(0));
    }
    String[] creates = getCreateUser();
    String create_user = creates[0];
    String create_user_id = creates[1];

    Session session = dao.getSession();
    int tollyFlag = -1; //记账标记
    ArrayList onList = new ArrayList();//在途审核   

    ArrayList endList = new ArrayList();//终审    
    try {
      Connection conn = session.connection();
      String currentNodeId = null;

      long lCurrentNodeId = 0;
      List remainList = detailDtos;
      while (remainList.size() > 0) {
        FVoucherDTO detailDto = (FVoucherDTO) remainList.get(0);
        //获取第一个明细的当前流程节点
        currentNodeId = getCurrentNodeId(menuid, roleid, detailDto.getVou_id(), conn);
        lCurrentNodeId = Long.parseLong(currentNodeId);
        tollyFlag = getTollyFlag(lCurrentNodeId, actionType, conn);
        List conditions = getNextConditionId(currentNodeId, conn);
        //走工作流
        remainList = this.doSameDetailProcessNextObj(lCurrentNodeId, menuid, roleid, actionType, operationName,
          operationDate, operationRemark, detail_table_name, remainList, auto_tolly_flag, create_user, create_user_id,
          conn, workFlowRuleFactory, conditions, tollyFlag, tollyFlag == 1 ? endList : onList, inspect_flag);//在途终审记账列表内部赋值
      }

      //获取系统配置，消息平台工作流自动发送模式是否启用，1启用，0停用
      //      String msgWorkflowModeStatus = (String) SessionUtil.getParaMap().get("MSG_AUTOWORKFLOW_IS_ENABLE");
      //      if (null != msgWorkflowModeStatus && "1".equals(msgWorkflowModeStatus.trim())) {
      //        List detailDtoList = detailDtos;
      //        String msgWorkflowModeIsInstant = (String) SessionUtil.getParaMap().get("MSG_AUTOWORKFLOW_IS_INSTANT");
      //        boolean isInstant = true;
      //        if (null != msgWorkflowModeIsInstant && "0".equals(msgWorkflowModeIsInstant.trim())) {
      //          isInstant = false;
      //        }
      //        this.doMessageTask(currentNodeId, actionType, detailDtoList, create_user_id, operationRemark, isInstant);
      //      }

    } finally {
      dao.closeSession(session);
    }
    if (onList.size() > 0)//在途审核记账
    {
      voucherService.updateBatch(onList);
    }
    if (endList.size() > 0) {//终审记账
      voucherService.auditBatch(endList);
    }

    return detailDtos;
  }

  /**
   * 按照指定的工作流节点，进行流程操作
   * 
   * 批量明细数据处理操作
   * 
   * @param menuid
   *            菜单ID
   * @param roleid
   *            角色ID
   * @param actiontype
   *            操作类型CODE
   * @param operationname
   *            操作名称
   * @param operationdate
   *            操作时间
   * @param operationremark
   *            操作意见
   * @param detail_table_name
   *            明细表名
   * @param detailDtos
   *            明细
   * @param auto_tolly_flag
   *            是否通过工作流自动记账 
   * @param inspect_flag 是否资金监控 
   * @param nextNodeIds 下一节点
   * @return List（内部含Object)
   * @throws Exception
   *             错误信息
   */
  public List doBatchDetailProcessNextNodes(String menuid, String roleid, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List detailDtos, boolean auto_tolly_flag,
    boolean inspect_flag, String[] nextNodeIds) throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) detailDtos.get(0));
    }
    String[] creates = getCreateUser();
    String create_user = creates[0];
    String create_user_id = creates[1];

    Session session = dao.getSession();
    int tollyFlag = -1; //记账标记
    ArrayList onList = new ArrayList();//在途审核   
    ArrayList endList = new ArrayList();//终审    
    try {
      Connection conn = session.connection();
      String currentNodeId = null;

      long lCurrentNodeId = 0;
      List remainList = detailDtos;
      while (remainList.size() > 0) {
        FVoucherDTO detailDto = (FVoucherDTO) remainList.get(0);
        //获取第一个明细的当前流程节点
        if (StringUtil.isNull(currentNodeId)) {
          currentNodeId = getCurrentNodeId(menuid, roleid, detailDto.getVou_id(), conn);
          lCurrentNodeId = Long.parseLong(currentNodeId);
        }
        if (tollyFlag == -1) {
          tollyFlag = getTollyFlag(lCurrentNodeId, actionType, conn);
        }
        List conditions = getPreNodeIdByNextNode(nextNodeIds);

        //走工作流
        remainList = this.doSameDetailProcessNextNodes(lCurrentNodeId, actionType, operationName, operationDate,
          operationRemark, detail_table_name, remainList, auto_tolly_flag, create_user, create_user_id, conn,
          workFlowRuleFactory, conditions, tollyFlag, tollyFlag == 1 ? endList : onList, inspect_flag, nextNodeIds);//在途终审记账列表内部赋值
      }

      //获取系统配置，消息平台工作流自动发送模式是否启用，1启用，0停用
      //      String msgWorkflowModeStatus = (String) SessionUtil.getParaMap().get("MSG_AUTOWORKFLOW_IS_ENABLE");
      //      if (null != msgWorkflowModeStatus && "1".equals(msgWorkflowModeStatus.trim())) {
      //        List detailDtoList = detailDtos;
      //        String msgWorkflowModeIsInstant = (String) SessionUtil.getParaMap().get("MSG_AUTOWORKFLOW_IS_INSTANT");
      //        boolean isInstant = true;
      //        if (null != msgWorkflowModeIsInstant && "0".equals(msgWorkflowModeIsInstant.trim())) {
      //          isInstant = false;
      //        }
      //        this.doMessageTask(currentNodeId, actionType, detailDtoList, create_user_id, operationRemark, isInstant);
      //      }

    } finally {
      dao.closeSession(session);
    }
    if (onList.size() > 0)//在途审核记账
    {
      voucherService.updateBatch(onList);
    }
    if (endList.size() > 0) {//终审记账
      voucherService.auditBatch(endList);
    }

    return detailDtos;
  }

  /**
   * 批量审核同一节点的明细数据处理操作，返回不在该节点没有审核的数据列表
   * @param nCurrentItemNodeId
   * @param menuid
   * @param roleid
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param auto_tolly_flag
   * @param create_user
   * @param create_user_id
   * @param pcon
   * @param ruleFactory
   * @param conditions
   * @param tollyFlag
   * @param tollyList
   * @param inspect_flag 是否资金监控 
   * @return
   * @throws Exception
   */
  private List doSameDetailProcessNextObj(long nCurrentItemNodeId, String menuid, String roleid, String actionType,
    String operationName, String operationDate, String operationRemark, String detail_table_name, List detailDtos,
    boolean auto_tolly_flag, String create_user, String create_user_id, Connection pcon,
    IWorkFlowRuleFactory ruleFactory, List conditions, int tollyFlag, List tollyList, boolean inspect_flag)
    throws Exception {
    /** 相同节点数据 */
    ArrayList sameList = new ArrayList();
    /** 不再同一节点剩余未走完流程的数据 */
    ArrayList remainList = new ArrayList();
    long wf_id = 0;
    //转变节点信息 ,删除原节点当前信息
    int[] curentUpdates = deleteCurrentItem(detailDtos, nCurrentItemNodeId, pcon);
    for (int i = 0; i < curentUpdates.length; i++) {
      //在当前节点就会被更新
      if (curentUpdates[i] <= 0) {
        //说明相同菜单角色配到了多个节点 需要在查找
        remainList.add(detailDtos.get(i));
      } else {
        sameList.add(detailDtos.get(i));
        if (tollyFlag >= 0) {
          //记录记账列表
          tollyList.add(detailDtos.get(i));
        }
      }
    }
    PreparedStatement insertCurrentTaskCachePsmt = getInsertCurrentTaskCachePsmt(pcon);
    PreparedStatement insertCurrentItemPsmt = getInsertCurrentItemPsmt(pcon);
    try {
      boolean cangonext = false;
      for (int i = 0, n = sameList.size(); i < n; i++) {
        FVoucherDTO detailDto = (FVoucherDTO) sameList.get(i);
        //菜单 角色是流转线的条件
        detailDto.setModule_id(menuid);
        detailDto.setRole_id(roleid);

        for (int j = 0; j < conditions.size(); j++) {
          Object[] condition = (Object[]) conditions.get(j);
          if ("#".equals(condition[2]) || ruleFactory.isMatchByBSH((String) condition[2], detailDto)) {
            //满足流转线条件
            cangonext = true;
            wf_id = ((Long) condition[0]).longValue();
            final Long lNextNodeId = (Long) condition[1];
            final long next_node_id = lNextNodeId.longValue();

            //流转线规则处理
            if (!"#".equals(condition[4]) && !StringUtil.isNull(condition[4])) {
              this.doBatchLineRule(nCurrentItemNodeId, next_node_id, (String) condition[4]);
            }

            setValues4InsertCurrentTaskCachePsmt(insertCurrentTaskCachePsmt, wf_id, detail_table_name,
              detailDto.getVou_id(), nCurrentItemNodeId, ((Long) condition[3]).longValue(), actionType, create_user,
              operationName, operationDate, operationRemark, detailDto.getVou_money(), tollyFlag,
              detailDto.getBilltype_code(), detailDto.getRcid(), detailDto.getCcid(), detailDto.getSet_month(),
              create_user_id, detailDto.getVou_bill_id());
            int gatherFlag = this.getGatherFlag(lNextNodeId, pcon);
            if (gatherFlag == 0)//如果是同步
            {
              if (existPreTask(detailDto.getVou_id(), wf_id, nCurrentItemNodeId, next_node_id, pcon))//存在还未到达同步的节点的工作流,不插入item表
              {
                continue;
              }
            }
            if (next_node_id > 0)//0为结束节点
            {
              setValues4InsertCurrentItemPsmt(insertCurrentItemPsmt, detailDto.getVou_id(), next_node_id,
                getNextItemBillId(detailDto, lNextNodeId, pcon), detailDto.getRcid(), detailDto.getCcid(), "001",
                detailDto.getMb_id(), detailDto.getAgency_id(), detailDto.getPb_id(), detailDto.getCb_id(),
                detailDto.getIb_id(), detailDto.getAgency_id());
            }
          }
        }
        if (!cangonext) {
          throw new WFErrorNodeException("明细id为" + detailDto.getVou_id() + "未找到下一步流转节点，请检查流转线");
        }
        //各个单的流转可能不同，但同一单的数据流转应该相同
        cangonext = false;
      }
      //统一监控
      if (inspect_flag) {
        this.doInspect(String.valueOf(wf_id), String.valueOf(nCurrentItemNodeId), menuid, roleid, actionType,
          operationName, operationDate, operationRemark, detail_table_name, sameList, pcon);
      }
      //插入流程信息
      insertCurrentTaskCachePsmt.executeBatch();
      //删除上一个节点的的已完成信息
      deleteCompleteItem(sameList, nCurrentItemNodeId, pcon);
      //增加当前节点的的已完成信息
      insertCompleteItem(sameList, nCurrentItemNodeId, pcon, "002");
      //将当前的流转信息转移到历史表
      copyCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //记录工作流转流程前后信息
      insertRoute(sameList, nCurrentItemNodeId, pcon);
      //删除原有的流程信息
      deleteCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //将数据从临时表写入正式表
      copyCacheTasks(pcon, wf_id);

      insertCurrentItemPsmt.executeBatch();
    } finally {
      insertCurrentTaskCachePsmt.close();
      insertCurrentItemPsmt.close();
    }

    return remainList;
  }

  /**
   * 按照指定的节点批量审核同一节点的明细数据处理操作
   * @param nCurrentItemNodeId
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param auto_tolly_flag
   * @param create_user
   * @param create_user_id
   * @param pcon
   * @param ruleFactory
   * @param conditions
   * @param tollyFlag
   * @param tollyList
   * @param inspect_flag 是否资金监控 
   * @return
   * @throws Exception
   */
  private List doSameDetailProcessNextNodes(long nCurrentItemNodeId, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List detailDtos, boolean auto_tolly_flag,
    String create_user, String create_user_id, Connection pcon, IWorkFlowRuleFactory ruleFactory, List conditions,
    int tollyFlag, List tollyList, boolean inspect_flag, String[] nextNodes) throws Exception {
    /** 相同节点数据 */
    ArrayList sameList = new ArrayList();
    /** 不再同一节点剩余未走完流程的数据 */
    ArrayList remainList = new ArrayList();
    long wf_id = 0;
    //转变节点信息 ,删除原节点当前信息
    int[] curentUpdates = deleteCurrentItem(detailDtos, nCurrentItemNodeId, pcon);
    for (int i = 0; i < curentUpdates.length; i++) {
      //在当前节点就会被更新
      if (curentUpdates[i] <= 0) {
        //说明相同菜单角色配到了多个节点 需要在查找
        remainList.add(detailDtos.get(i));
      } else {
        sameList.add(detailDtos.get(i));
        if (tollyFlag >= 0) {
          //记录记账列表
          tollyList.add(detailDtos.get(i));
        }
      }
    }
    PreparedStatement insertCurrentTaskCachePsmt = getInsertCurrentTaskCachePsmt(pcon);
    PreparedStatement insertCurrentItemPsmt = getInsertCurrentItemPsmt(pcon);
    try {
      boolean cangonext = false;
      for (int i = 0, n = sameList.size(); i < n; i++) {
        FVoucherDTO detailDto = (FVoucherDTO) sameList.get(i);
        for (int j = 0; j < conditions.size(); j++) {
          Object[] condition = (Object[]) conditions.get(j);
          cangonext = true;
          wf_id = ((Long) condition[0]).longValue();
          final Long lNextNodeId = (Long) condition[2];
          final Long next_node_id = lNextNodeId.longValue();
          //流转线规则处理
          if (!"#".equals(condition[3]) && !StringUtil.isNull(condition[3])) {
            this.doBatchLineRule(nCurrentItemNodeId, next_node_id, (String) condition[3]);
          }
          setValues4InsertCurrentTaskCachePsmt(insertCurrentTaskCachePsmt, wf_id, detail_table_name,
            detailDto.getVou_id(), nCurrentItemNodeId, next_node_id, actionType, create_user, operationName,
            operationDate, operationRemark, detailDto.getVou_money(), tollyFlag, detailDto.getBilltype_code(),
            detailDto.getRcid(), detailDto.getCcid(), detailDto.getSet_month(), create_user_id,
            detailDto.getVou_bill_id());
          int gatherFlag = this.getGatherFlag(lNextNodeId, pcon);
          if (gatherFlag == 0)//如果是同步
          {
            if (existPreTask(detailDto.getVou_id(), wf_id, nCurrentItemNodeId, next_node_id, pcon))//存在还未到达同步的节点的工作流,不插入item表
            {
              continue;
            }
          }
          if (next_node_id > 0)//0为结束节点
          {
            setValues4InsertCurrentItemPsmt(insertCurrentItemPsmt, detailDto.getVou_id(), next_node_id,
              getNextItemBillId(detailDto, lNextNodeId, pcon), detailDto.getRcid(), detailDto.getCcid(), "001",
              detailDto.getMb_id(), detailDto.getAgency_id(), detailDto.getPb_id(), detailDto.getCb_id(),
              detailDto.getIb_id(), detailDto.getAgency_id());
          }
        }
        if (!cangonext) {
          throw new WFErrorNodeException("明细id为" + detailDto.getVou_id() + "未找到下一步流转节点，请检查流转线");
        }
        //各个单的流转可能不同，但同一单的数据流转应该相同
        cangonext = false;
      }
      //统一监控
      //      if (inspect_flag) {
      //        this.doInspect(String.valueOf(wf_id), String.valueOf(nCurrentItemNodeId), menuid, roleid, actionType,
      //          operationName, operationDate, operationRemark, detail_table_name, sameList, pcon);
      //      }
      //插入流程信息
      insertCurrentTaskCachePsmt.executeBatch();
      //删除上一个节点的的已完成信息
      deleteCompleteItem(sameList, nCurrentItemNodeId, pcon);
      //增加当前节点的的已完成信息
      insertCompleteItem(sameList, nCurrentItemNodeId, pcon, "002");
      //将当前的流转信息转移到历史表
      copyCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //记录工作流转流程前后信息
      insertRoute(sameList, nCurrentItemNodeId, pcon);
      //删除原有的流程信息
      deleteCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //将数据从临时表写入正式表
      copyCacheTasks(pcon, wf_id);

      insertCurrentItemPsmt.executeBatch();
    } finally {
      insertCurrentTaskCachePsmt.close();
      insertCurrentItemPsmt.close();
    }

    return remainList;
  }

  /**
   * 批量单数据处理撤销审核退回等操作，按明细调用。
   * 
   * @param menuid
   *            菜单ID
   * @param roleid
   *            角色ID
   * @param actiontype
   *            操作类型CODE
   * @param operationname
   *            操作名称
   * @param operationdate
   *            操作时间
   * @param operationremark
   *            操作意见
   * @param detail_table_name
   *            明细表名
   * @param billDtos
   *            业务单DTO列表，必须含有明细，通过getDetails()获取明细
   * @param inspect_flag 是否资金监控 
   * @return List（内部含Object)
   * @throws Exception
   *             错误信息
   */
  public List doBatchBillProcessRecallObj(String menuid, String roleid, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List billDtos, boolean inspect_flag)
    throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) ((FVoucherDTO) billDtos.get(0)).getDetails().get(0));
    }
    ArrayList allDettail = new ArrayList();
    for (int i = 0, n = billDtos.size(); i < n; i++) {
      allDettail.addAll(((FVoucherDTO) billDtos.get(i)).getDetails());
    }
    doBatchDetailProcessRecallObj(menuid, roleid, actionType, operationName, operationDate, operationRemark,
      detail_table_name, allDettail, inspect_flag);
    return billDtos;
  }

  /**
   * 批量明细数据处理撤销操作，按历史记录的工作流信息撤销记账。
   * 
   * @param menuid
   *            菜单ID
   * @param roleid
   *            角色ID
   * @param actiontype
   *            操作类型CODE
   * @param operationname
   *            操作名称
   * @param operationdate
   *            操作时间
   * @param operationremark
   *            操作意见
   * @param detail_table_name
   *            明细表名
   * @param detailDtos
   *            明细DTO列表
   * @param inspect_flag 是否资金监控 
   * @return List（内部含Object)
   * @throws Exception
   *             错误信息
   */
  public List doBatchDetailProcessRecallObj(String menuid, String roleid, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List detailDtos, boolean inspect_flag)
    throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) detailDtos.get(0));
    }
    String[] creates = getCreateUser();
    String create_user = creates[0];

    Session session = dao.getSession();

    ArrayList onList = new ArrayList();//在途审核   

    ArrayList endList = new ArrayList();//终审    
    try {
      Connection conn = session.connection();
      //已完成的节点
      String completeNodeId = null;

      long lCompleteNodeId = 0;
      List remainList = detailDtos;
      while (remainList.size() > 0) {
        FVoucherDTO detailDto = (FVoucherDTO) remainList.get(0);
        //获取第一个明细的已完成流程节点
        completeNodeId = getCompleteNodeId(menuid, roleid, detailDto.getVou_id(), conn);
        lCompleteNodeId = Long.parseLong(completeNodeId);

        remainList = this.doSameDetailProcessRecallObj(lCompleteNodeId, menuid, roleid, remainList, conn, onList,
          endList, create_user, operationDate, detail_table_name, inspect_flag);//在途终审记账列表内部赋值
      }
      if (onList.size() > 0)//在途审核记账
      {
        voucherService.updateBatch(onList);
      }
      if (endList.size() > 0) {//终审记账
        voucherService.cancelAuditBatch(endList);
      }
    } finally {
      dao.closeSession(session);
    }

    return detailDtos;
  }

  /**
   * 按节点撤销批量明细数据操作，返回不在该节点没有撤销的数据列表。
   * @param nCompleteItemNodeId
   * @param menuid
   * @param roleid
   * @param detailDtos
   * @param pcon
   * @param onList
   * @param endList
   * @param create_user
   * @param operationDate
   * @param detail_table_name
   * @param inspect_flag 是否资金监控 
   * @return
   * @throws Exception
   */
  private List doSameDetailProcessRecallObj(long nCompleteItemNodeId, String menuid, String roleid, List detailDtos,
    Connection pcon, List onList, List endList, String create_user, String operationDate, String detail_table_name,
    boolean inspect_flag) throws Exception {
    /** 相同节点数据 */
    ArrayList sameList = new ArrayList();
    /** 不在同一节点剩余未走完流程的数据 */
    ArrayList remainList = new ArrayList();

    String rg_code = getRgCode();
    String setYear = getSetYear();

    //转变节点信息      
    //原节点已完成信息
    Long completeItemNodeId = new Long(nCompleteItemNodeId);
    int[] completeUpdates = deleteRecallCompleteItem(detailDtos, nCompleteItemNodeId, pcon);

    for (int i = 0; i < completeUpdates.length; i++) {
      //在当前节点就会被更新
      if (completeUpdates[i] <= 0) {
        //说明相同菜单角色配到了多个节点 需要在查找
        remainList.add(detailDtos.get(i));
      } else {
        sameList.add(detailDtos.get(i));
      }
    }
    PreparedStatement delCurrentItemsPsmt = null;//删除current_item
    PreparedStatement delCurrentTasksPsmt = null;//删除current_tasks
    PreparedStatement delEndTasksPsmt = null; //删除end_tasks
    PreparedStatement delCompleteTasksPsmt = null;//删除complete_tasks
    PreparedStatement complete2CurrentTasksPsmt = null;//从complete_tasks copy到current_tasks
    PreparedStatement curend2CompleteTasksPsmt = null;//从current_tasks end_tasks copy到complete_tasks
    PreparedStatement insertCompleteItemPsmt = null;//插入current_item
    PreparedStatement insertCurrentItemPsmt = null; //插入complete_item

    try {
      delCurrentItemsPsmt = pcon
        .prepareStatement("delete sys_wf_current_item a where exists (select 1 from sys_wf_current_tasks b where b.entity_id=a.entity_id and b.next_node_id=a.node_id and task_id = ? and b.rg_code = ? and b.set_year = ? ) and a.rg_code = ? and a.set_year = ?");
      delCurrentTasksPsmt = pcon
        .prepareStatement("delete sys_wf_current_tasks where task_id = ? and rg_code=? and set_year=?");
      delEndTasksPsmt = pcon.prepareStatement("delete sys_wf_end_tasks where task_id = ? and rg_code=? and set_year=?");
      delCompleteTasksPsmt = pcon
        .prepareStatement("delete sys_wf_complete_tasks where task_id = ? and rg_code = ? and set_year = ?");
      complete2CurrentTasksPsmt = pcon
        .prepareStatement("insert into sys_wf_current_tasks select * from sys_wf_complete_tasks where task_id = ?  and rg_code = ? and set_year = ?");
      curend2CompleteTasksPsmt = pcon
        .prepareStatement("insert into sys_wf_complete_tasks("
          + "task_id, wf_id, wf_table_name, entity_id, current_node_id, next_node_id, action_type_code, is_undo, create_user, create_date, undo_user, undo_date, operation_name, operation_date, operation_remark, init_money, result_money, remark, tolly_flag, bill_type_code, bill_id, rcid, ccid, last_ver, send_msg_date, auto_audit_date, set_month, update_flag, create_user_id, rg_code, set_year)"
          + " (select task_id,wf_id,wf_table_name,entity_id,current_node_id,next_node_id,action_type_code,1 ,create_user,create_date,?,?,operation_name,operation_date,operation_remark,init_money,result_money,remark,tolly_flag,bill_type_code,bill_id,rcid,ccid,last_ver,send_msg_date,auto_audit_date,set_month,update_flag,create_user_id,rg_code,set_year from sys_wf_current_tasks where task_id = ? and rg_code=? and set_year=? union all select task_id,wf_id,wf_table_name,entity_id,current_node_id,next_node_id,action_type_code,1 ,create_user,create_date,?,?,operation_name,operation_date,operation_remark,init_money,result_money,remark,tolly_flag,bill_type_code,bill_id,rcid,ccid,last_ver,send_msg_date,auto_audit_date,set_month,update_flag,create_user_id,rg_code,set_year from sys_wf_end_tasks where task_id = ? and rg_code=? and set_year=?)");
      insertCompleteItemPsmt = pcon
        .prepareStatement("insert into sys_wf_complete_item(entity_id,bill_id,node_id,status_code,rcid,ccid,rg_code,set_year,mb_id,agency_id,pb_id,cb_id,ib_id,gp_agency_id)  select entity_id,bill_id,current_node_id,"
          + (TypeOfDB.isOracle() ? "decode(action_type_code,'BACK','003','002')"
            : " case action_type_code when 'BACK' then'003' else '002' end ")
          + ",rcid,ccid,rg_code,set_year,?,?,?,?,?,? from sys_wf_current_tasks where task_id = ? and rg_code=? and set_year =? and action_type_code <>'EDIT'");
      insertCurrentItemPsmt = pcon
        .prepareStatement("insert into sys_wf_current_item(entity_id,bill_id,node_id,status_code,rcid,ccid,rg_code,set_year,mb_id,agency_id,pb_id,cb_id,ib_id,gp_agency_id) values(?,?,?,(select "
          + (TypeOfDB.isOracle() ? " decode(action_type_code,'BACK','004','001')"
            : " case action_type_code when 'BACK' then '004' else '001' end ")
          + " from sys_wf_current_tasks where task_id = ? and rg_code = ? and set_year =?),?,?,?,?,?,?,?,?,?,?)");

      for (int i = 0, n = sameList.size(); i < n; i++) {
        FVoucherDTO detailDto = (FVoucherDTO) sameList.get(i);
        //从明细数据中获取机构冗余信息
        XMLData xmlDataTmp = this.getOrgDetail(detailDto);
        //当前要处理的工作流流程       
        long[] currentWfTasks = getCurrentTaskId(detailDto, nCompleteItemNodeId, onList, endList, pcon,
          delCurrentTasksPsmt, delEndTasksPsmt, curend2CompleteTasksPsmt, delCurrentItemsPsmt, create_user,
          operationDate);
        //历史 taskid
        long[] routeCompleteTasks = this.getCompleteTaskId(currentWfTasks[0], pcon, delCompleteTasksPsmt,
          complete2CurrentTasksPsmt, insertCompleteItemPsmt, xmlDataTmp);
        //需要撤销时 所有的数据都没有变动        
        if (!this.canRecallTaskId(routeCompleteTasks[0], pcon)) {
          throw new Exception("明细id为" + detailDto.getVou_id() + "的数据已进行了下一步操作无法撤销");
        }
        insertCurrentItemPsmt.setString(1, detailDto.getVou_id());
        insertCurrentItemPsmt.setString(2, getNextItemBillId(detailDto, completeItemNodeId, pcon));
        insertCurrentItemPsmt.setLong(3, nCompleteItemNodeId);
        insertCurrentItemPsmt.setLong(4, routeCompleteTasks[0]);
        insertCurrentItemPsmt.setString(5, rg_code);
        insertCurrentItemPsmt.setInt(6, Integer.parseInt(setYear));
        insertCurrentItemPsmt.setString(7, detailDto.getRcid());
        insertCurrentItemPsmt.setString(8, detailDto.getCcid());
        insertCurrentItemPsmt.setString(9, rg_code);
        insertCurrentItemPsmt.setInt(10, Integer.parseInt(setYear));
        insertCurrentItemPsmt.setString(11, (String) xmlDataTmp.get("mb_id"));
        insertCurrentItemPsmt.setString(12, (String) xmlDataTmp.get("agency_id"));
        insertCurrentItemPsmt.setString(13, (String) xmlDataTmp.get("pb_id"));
        insertCurrentItemPsmt.setString(14, (String) xmlDataTmp.get("cb_id"));
        insertCurrentItemPsmt.setString(15, (String) xmlDataTmp.get("ib_id"));
        insertCurrentItemPsmt.setString(16, (String) xmlDataTmp.get("gp_agency_id"));
        insertCurrentItemPsmt.addBatch();
      }
      //      统一监控
      if (inspect_flag) {
        this.doInspect(String.valueOf(this.getWfIdByNodeId(nCompleteItemNodeId, pcon)),
          String.valueOf(nCompleteItemNodeId), menuid, roleid, "RECALL", "撤销", operationDate, "撤销", detail_table_name,
          sameList, pcon);
      }
      delCurrentItemsPsmt.executeBatch();
      complete2CurrentTasksPsmt.executeBatch();

      curend2CompleteTasksPsmt.executeBatch();
      insertCompleteItemPsmt.executeBatch();
      delCurrentTasksPsmt.executeBatch();
      delEndTasksPsmt.executeBatch();
      delCompleteTasksPsmt.executeBatch();
      insertCurrentItemPsmt.executeBatch();
    } finally {
      delCurrentItemsPsmt.close();
      complete2CurrentTasksPsmt.close();
      delCurrentTasksPsmt.close();
      delEndTasksPsmt.close();
      delCompleteTasksPsmt.close();
      curend2CompleteTasksPsmt.close();
      insertCompleteItemPsmt.close();
      insertCurrentItemPsmt.close();
    }

    return remainList;
  }

  /**
   * 批量单数据处理退回操作。
   * 
   * @param menuid
   *            菜单ID
   * @param roleid
   *            角色ID
   * @param actiontype
   *            操作类型CODE
   * @param operationname
   *            操作名称
   * @param operationdate
   *            操作时间
   * @param operationremark
   *            操作意见
   * @param detail_table_name
   *            明细表名
   * @param billDtos
   *            业务单DTO列表，必须含有明细，通过getDetails()获取明细
   * @param auto_tolly_flag 是否使用工作流记账
   * @param inspect_flag 是否资金监控 
   * @return List（内部含Object)
   * @throws Exception
   *             错误信息
   */
  public List doBatchBillProcessBackObj(String menuid, String roleid, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List billDtos, boolean tollyFlag,
    boolean inspect_flag) throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) ((FVoucherDTO) billDtos.get(0)).getDetails().get(0));
    }
    ArrayList allDettail = new ArrayList();
    for (int i = 0, n = billDtos.size(); i < n; i++) {
      allDettail.addAll(((FVoucherDTO) billDtos.get(i)).getDetails());
    }
    doBatchDetailProcessBackObj(menuid, roleid, actionType, operationName, operationDate, operationRemark,
      detail_table_name, allDettail, tollyFlag, inspect_flag);
    return billDtos;
  }

  /**
   * 批量单数据处理删除、作废操作。
   * 
   * @param menuid
   *            菜单ID
   * @param roleid
   *            角色ID
   * @param actiontype
   *            操作类型CODE
   * @param operationname
   *            操作名称
   * @param operationdate
   *            操作时间
   * @param operationremark
   *            操作意见
   * @param detail_table_name
   *            明细表名
   * @param billDtos
   *            业务单DTO列表，必须含有明细，通过getDetails()获取明细
   * @param auto_tolly_flag 是否使用工作流记账
   * @param inspect_flag 是否资金监控 
   * @return List（内部含Object)
   * @throws Exception
   *             错误信息
   */
  public List doBatchBillProcessDelOrDiscardObj(String menuid, String roleid, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List billDtos, boolean tollyFlag,
    boolean inspect_flag) throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) ((FVoucherDTO) billDtos.get(0)).getDetails().get(0));
    }
    ArrayList allDettail = new ArrayList();
    for (int i = 0, n = billDtos.size(); i < n; i++) {
      allDettail.addAll(((FVoucherDTO) billDtos.get(i)).getDetails());
    }
    doBatchDetailProcessDelOrDiscardObj(menuid, roleid, actionType, operationName, operationDate, operationRemark,
      detail_table_name, allDettail, tollyFlag, inspect_flag);
    return billDtos;
  }

  /**
   * 批量明细数据退回处理操作。
   * @param menuid
   * @param roleid
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param auto_tolly_flag 是否使用工作流记账
   * @return
   * @param inspect_flag 是否资金监控 
   * @throws Exception
   */
  public List doBatchDetailProcessBackObj(String menuid, String roleid, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List detailDtos, boolean auto_tolly_flag,
    boolean inspect_flag) throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) detailDtos.get(0));
    }
    String[] creates = getCreateUser();
    String create_user = creates[0];
    String create_user_id = creates[1];

    Session session = dao.getSession();
    int tollyFlag = -1; //记账标记
    ArrayList onList = new ArrayList();//在途审核   

    ArrayList endList = new ArrayList();//终审    
    try {
      Connection conn = session.connection();
      String currentNodeId = null;

      long lCurrentNodeId = 0;
      List remainList = detailDtos;
      while (remainList.size() > 0) {
        FVoucherDTO detailDto = (FVoucherDTO) remainList.get(0);
        //获取第一个明细的当前流程节点
        currentNodeId = getCurrentNodeId(menuid, roleid, detailDto.getVou_id(), conn);
        lCurrentNodeId = Long.parseLong(currentNodeId);
        if (auto_tolly_flag) {
          tollyFlag = getTollyFlag(lCurrentNodeId, actionType, conn);
        }
        List conditions = getBackConditionId(currentNodeId, conn);

        remainList = this.doSameDetailProcessBackObj(lCurrentNodeId, menuid, roleid, actionType, operationName,
          operationDate, operationRemark, detail_table_name, remainList, auto_tolly_flag, create_user, create_user_id,
          conn, workFlowRuleFactory, conditions, tollyFlag, tollyFlag == 1 ? endList : onList, inspect_flag);//在途终审记账列表内部赋值
      }

    } finally {
      dao.closeSession(session);
    }
    if (onList.size() > 0)//在途审核记账
    {
      voucherService.updateBatch(onList);
    }
    if (endList.size() > 0) {//终审记账
      voucherService.auditBatch(endList);
    }

    return detailDtos;
  }

  /**
   * 按节点批量明细数据处理退回操作，返回不在该节点没有退回的数据列表。
   * @param nCurrentItemNodeId
   * @param menuid
   * @param roleid
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param auto_tolly_flag
   * @param create_user
   * @param create_user_id
   * @param pcon
   * @param ruleFactory
   * @param conditions
   * @param tollyFlag
   * @param tollyList
   * @param inspect_flag 是否资金监控 
   * @return
   * @throws Exception
   */
  private List doSameDetailProcessBackObj(long nCurrentItemNodeId, String menuid, String roleid, String actionType,
    String operationName, String operationDate, String operationRemark, String detail_table_name, List detailDtos,
    boolean auto_tolly_flag, String create_user, String create_user_id, Connection pcon,
    IWorkFlowRuleFactory ruleFactory, List conditions, int tollyFlag, List tollyList, boolean inspect_flag)
    throws Exception {
    /** 相同节点数据 */
    ArrayList sameList = new ArrayList();
    /** 不再同一节点剩余未走完流程的数据 */
    ArrayList remainList = new ArrayList();
    long wf_id = 0;

    /**根据当前的节点查询从上一节点开始的路由（2014-8-20 李文全）
     * 如果从上一节点开始存在一条路由说明上一节点的工作流节点没有分叉
     * 如果从上一节点开始存在一条以上的路由说明上一节点的工作流节点是分叉的
     */
    int detailNum = detailDtos.size();
    for (int i = 0; i < detailNum; i++) {
      FVoucherDTO detailDto = (FVoucherDTO) detailDtos.get(i);
      //根据当前节点查询来源节点，当前节点有可能是正向，也有可能是逆向，
      String preNodeId = getPreNodeId(nCurrentItemNodeId, detailDto.getVou_id(), pcon);
      long iPreNodeId = Long.parseLong(preNodeId);
      //工作流id
      wf_id = getWfIdByNodeId(nCurrentItemNodeId, pcon);
      //来源节点开始的路由
      long[] preWfTasks = getPreTaskId(detailDto.getVou_id(), iPreNodeId, pcon);
      //来源节点是否存在分支，即当前节点是否存在平级节点
      if (preWfTasks != null && preWfTasks.length > 1) {
        //来源节点的所有后续节点,直至结束节点，并做简单排序
        long[] nextNodes = getNextNodeIds(wf_id, iPreNodeId, pcon);
        if (nextNodes != null) {
          Arrays.sort(nextNodes);
        } else {
          throw new Exception("明细数据" + detailDto.getVou_id() + "未找到当前流程节点信息！");
        }
        //判断分支流程数据，是否已进行后续操作
        if (this.canBackTask(wf_id, detailDto.getVou_id(), detail_table_name, iPreNodeId, nextNodes, pcon)) {
          throw new Exception("明细数据" + detailDto.getVou_id() + "的分支流程数据已进行后续操作，不能退回！");
        }
      }
    }

    //删除原节点当前信息
    // int[] curentUpdates = deleteCurrentItem(detailDtos, nCurrentItemNodeId, pcon);
    int[] curentUpdates = deleteCurAndOtherItem(detailDtos, nCurrentItemNodeId, pcon);

    for (int i = 0; i < curentUpdates.length; i++) {
      //在当前节点就会被更新
      if (curentUpdates[i] <= 0) {
        //说明相同菜单角色配到了多个节点 需要在查找
        remainList.add(detailDtos.get(i));
      } else {
        sameList.add(detailDtos.get(i));
        if (tollyFlag >= 0) {
          //记录记账列表
          tollyList.add(detailDtos.get(i));
        }
      }
    }
    PreparedStatement insertCurrentTaskCachePsmt = getInsertCurrentTaskCachePsmt(pcon);
    PreparedStatement insertCurrentItemPsmt = getInsertCurrentItemPsmt(pcon);
    try {
      boolean cangonext = false;
      int conditionSize = conditions.size();
      for (int i = 0, n = sameList.size(); i < n; i++) {
        FVoucherDTO detailDto = (FVoucherDTO) sameList.get(i);
        //菜单 角色是流转线的条件
        detailDto.setModule_id(menuid);
        detailDto.setRole_id(roleid);

        for (int j = 0; j < conditionSize; j++) {
          Object[] condition = (Object[]) conditions.get(j);
          if (conditionSize > 1)//如果退回节点超过1个则需要判断历史表中是否存在退回节点，退回的节点必须在历史上曾经走过该节点
          {
            if (!existHisTask(detailDto.getVou_id(), ((Long) condition[1]).longValue(), pcon)) {
              continue;
            }
          }
          if ("#".equals(condition[2]) || ruleFactory.isMatchByBSH((String) condition[2], detailDto)) {
            //满足流转线条件
            cangonext = true;
            wf_id = ((Long) condition[0]).longValue();

            final Long lNextNodeId = (Long) condition[1];
            final long next_node_id = lNextNodeId.longValue();

            setValues4InsertCurrentTaskCachePsmt(insertCurrentTaskCachePsmt, wf_id, detail_table_name,
              detailDto.getVou_id(), nCurrentItemNodeId, next_node_id, actionType, create_user, operationName,
              operationDate, operationRemark, detailDto.getVou_money(), tollyFlag, detailDto.getBilltype_code(),
              detailDto.getRcid(), detailDto.getCcid(), detailDto.getSet_month(), create_user_id,
              detailDto.getVou_bill_id());
            int gatherFlag = this.getGatherFlag(lNextNodeId, pcon);
            if (gatherFlag == 0)//如果是同步
            {
              if (existNextTask(detailDto.getVou_id(), wf_id, nCurrentItemNodeId, next_node_id, pcon))//存在还未到达同步的节点的工作流,不插入item表
              {
                continue;
              }
            }
            setValues4InsertCurrentItemPsmt(insertCurrentItemPsmt, detailDto.getVou_id(), next_node_id,
              getNextItemBillId(detailDto, lNextNodeId, pcon), detailDto.getRcid(), detailDto.getCcid(), "004",
              detailDto.getMb_id(), detailDto.getAgency_id(), detailDto.getPb_id(), detailDto.getCb_id(),
              detailDto.getIb_id(), detailDto.getAgency_id());
          }
        }
        if (!cangonext) {
          throw new WFErrorNodeException("明细id为" + detailDto.getVou_id() + "未找到可退回流转节点，请检查流转线");
        }
        //各个单的流转可能不同，但同一单的数据流转应该相同
        cangonext = false;
      }

      //统一监控
      if (inspect_flag) {
        this.doInspect(String.valueOf(wf_id), String.valueOf(nCurrentItemNodeId), menuid, roleid, actionType,
          operationName, operationDate, operationRemark, detail_table_name, sameList, pcon);
      }

      //插入流程信息
      insertCurrentTaskCachePsmt.executeBatch();

      //删除上一个节点的的已完成信息
      deleteCompleteItem(sameList, nCurrentItemNodeId, pcon);

      //增加当前节点的的已完成信息
      insertCompleteItem(sameList, nCurrentItemNodeId, pcon, "003");

      //将当前的流转信息转移到历史表
      copyCurrentTask(sameList, nCurrentItemNodeId, pcon);

      //记录工作流转流程前后信息
      insertRoute(sameList, nCurrentItemNodeId, pcon);

      //删除原有的流程信息
      //deleteCurrentTask(sameList, nCurrentItemNodeId, pcon);
      deleteCurrentAndOtherTask(sameList, nCurrentItemNodeId, pcon);

      //将数据从临时表写入正式表
      copyCacheTasks(pcon, wf_id);

      insertCurrentItemPsmt.executeBatch();
    } finally {
      insertCurrentTaskCachePsmt.close();
      insertCurrentItemPsmt.close();
    }

    return remainList;
  }

  /**
   * 批量明细数据退回到录入处理操作,不记账（一般有问题才退回录入的，如果额度不足就能退回了，就是不够了才退的）。
   * @param menuid
   * @param roleid
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param inspect_flag 是否资金监控 
   * @return
   * @throws Exception
   */
  public List doBatchDetailProcessBackToInputObj(String menuid, String roleid, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List detailDtos, boolean inspect_flag)
    throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) detailDtos.get(0));
    }
    String[] creates = getCreateUser();
    String create_user = creates[0];
    String create_user_id = creates[1];
    Session session = dao.getSession();
    int tollyFlag = -1; //记账标记
    Connection conn = session.connection();
    PreparedStatement insertCurrentTaskCachePsmt = getInsertCurrentTaskCachePsmt(conn);
    PreparedStatement insertCurrentItemPsmt = getInsertCurrentItemPsmt(conn);
    try {

      String currentNodeId = null;

      long lCurrentNodeId = 0;
      List remainList = detailDtos;
      while (remainList.size() > 0) {
        FVoucherDTO detailDto = (FVoucherDTO) remainList.get(0);
        //获取第一个明细的当前流程节点
        currentNodeId = getCurrentNodeId(menuid, roleid, detailDto.getVou_id(), conn);
        lCurrentNodeId = Long.parseLong(currentNodeId);
        int[] curentUpdates = deleteCurrentItem(remainList, lCurrentNodeId, conn);
        long wf_id = this.getWfIdByNodeId(lCurrentNodeId, conn);
        List sameList = new ArrayList();
        List tempList = new ArrayList();
        for (int i = 0; i < curentUpdates.length; i++) {
          //在当前节点就会被更新
          if (curentUpdates[i] <= 0) {
            //说明相同菜单角色配到了多个节点 需要在查找
            tempList.add(detailDtos.get(i));
          } else {
            detailDto = (FVoucherDTO) remainList.get(i);
            sameList.add(remainList.get(i));
            final long next_node_id = this.getBackInputNextId(detailDto.getVou_id(), detail_table_name, conn);
            if (this.existCurrentTask(detailDto.getVou_id(), detail_table_name, lCurrentNodeId, conn)) {
              throw new WFErrorNodeException("明细id为：" + detailDto.getVou_id() + "存在多分支流程，不能退回到录入节点");
            }
            setValues4InsertCurrentTaskCachePsmt(insertCurrentTaskCachePsmt, wf_id, detail_table_name,
              detailDto.getVou_id(), lCurrentNodeId, next_node_id, actionType, create_user, operationName,
              operationDate, operationRemark, detailDto.getVou_money(), tollyFlag, detailDto.getBilltype_code(),
              detailDto.getRcid(), detailDto.getCcid(), detailDto.getSet_month(), create_user_id,
              detailDto.getVou_bill_id());

            setValues4InsertCurrentItemPsmt(insertCurrentItemPsmt, detailDto.getVou_id(), next_node_id,
              getNextItemBillId(detailDto, new Long(next_node_id), conn), detailDto.getRcid(), detailDto.getCcid(),
              "004", detailDto.getMb_id(), detailDto.getAgency_id(), detailDto.getPb_id(), detailDto.getCb_id(),
              detailDto.getIb_id(), detailDto.getAgency_id());
          }
        }
        remainList = tempList;

        if (inspect_flag) {
          this.doInspect(String.valueOf(wf_id), currentNodeId, menuid, roleid, actionType, operationName,
            operationDate, operationRemark, detail_table_name, sameList, conn);
        }
        //插入流程信息
        insertCurrentTaskCachePsmt.executeBatch();
        //删除上一个节点的的已完成信息
        deleteCompleteItem(sameList, lCurrentNodeId, conn);
        //增加当前节点的的已完成信息
        insertCompleteItem(sameList, lCurrentNodeId, conn, "003");
        //将当前的流转信息转移到历史表
        copyCurrentTask(sameList, lCurrentNodeId, conn);
        //记录工作流转流程前后信息
        insertRoute(sameList, lCurrentNodeId, conn);
        //删除原有的流程信息
        deleteCurrentTask(sameList, lCurrentNodeId, conn);
        //将数据从临时表写入正式表
        copyCacheTasks(conn, wf_id);

        insertCurrentItemPsmt.executeBatch();
      }
    } finally {
      insertCurrentTaskCachePsmt.close();
      insertCurrentItemPsmt.close();
    }
    return detailDtos;
  }

  /**
   * 批量明细数据录入操作,不需要在工作流配置录入记账，根据auto_tolly_flag直接记账。
   * @param menuid
   * @param roleid
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param auto_tolly_flag
   * @param inspect_flag 是否资金监控 
   * @return
   * @throws Exception
   */
  public List doBatchDetailProcessInputObj(String menuid, String roleid, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List detailDtos, boolean auto_tolly_flag,
    boolean inspect_flag) throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) detailDtos.get(0));
    }
    String[] creates = getCreateUser();
    String create_user = creates[0];
    String create_user_id = creates[1];

    Session session = dao.getSession();
    int tollyFlag = -1; //记账标记      
    try {
      Connection conn = session.connection();
      PreparedStatement insertCurrentTaskPsmt = getInsertCurrentTaskPsmt(conn);
      PreparedStatement insertCurrentItemPsmt = getInsertCurrentItemPsmt(conn);

      boolean canInsert;
      List conditions = getInputConditionId(menuid, roleid, detail_table_name, conn);
      for (int i = 0, n = detailDtos.size(); i < n; i++) {
        canInsert = true;
        FVoucherDTO detailDto = (FVoucherDTO) detailDtos.get(i);

        //菜单 角色是流转线的条件
        //        detailDto.setModule_id(menuid);
        //        detailDto.setRole_id(roleid);

        for (int j = 0; j < conditions.size(); j++) {
          Object[] condition = (Object[]) conditions.get(j);
          if ("#".equals(condition[2]) || workFlowRuleFactory.isMatchByBSH((String) condition[2], detailDto)) {
            //满足流转线条件
            if (canInsert) {
              canInsert = false;
            } else {
              log("数据找到多个初始录入节点，请检查工作流入口条件和工作流转线");
              throw new WFErrorNodeException("数据找到多个初始录入节点，请检查工作流入口条件和工作流转线");
            }
            final long wf_id = ((Long) condition[0]).longValue();
            final Long lNextNodeId = (Long) condition[1];
            final long next_node_id = lNextNodeId.longValue();
            final long nCurrentItemNodeId = ((Long) condition[3]).longValue();

            //插入工作流current_task表数据
            setValues4InsertCurrentTaskCachePsmt(insertCurrentTaskPsmt, wf_id, detail_table_name,
              detailDto.getVou_id(), nCurrentItemNodeId, next_node_id, actionType, create_user, operationName,
              operationDate, operationRemark, "", tollyFlag, "", "", "", 5, create_user_id, "");
            setValues4InsertCurrentItemPsmt(insertCurrentItemPsmt, detailDto.getVou_id(), next_node_id, "", "", "",
              "001", "", "", "", "", "", "");
          }
        }
        if (canInsert) {
          throw new WFErrorNodeException("明细id为" + detailDto.getVou_id() + "未找到录入节点，请检查工作流入口条件和工作流转线");
        }

        //插入流程信息
        insertCurrentTaskPsmt.executeBatch();

        insertCurrentItemPsmt.executeBatch();
      }

    } finally {
      dao.closeSession(session);
    }
    if (auto_tolly_flag)//录入记账
    {
      voucherService.saveBatch(detailDtos);
    }

    return detailDtos;
  }

  /**
   *  批量明细数据修改处理操作,不需要在工作流配置修改记账，根据auto_tolly_flag直接记账。
   * @param menuid
   * @param roleid
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param auto_tolly_flag
   * @param inspect_flag 是否资金监控 
   * @return
   * @throws Exception
   */
  public List doBatchDetailProcessEditObj(String menuid, String roleid, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List detailDtos, boolean auto_tolly_flag,
    boolean inspect_flag) throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) detailDtos.get(0));
    }
    String[] creates = getCreateUser();
    String create_user = creates[0];
    String create_user_id = creates[1];
    Session session = dao.getSession();
    try {
      Connection conn = session.connection();
      String currentNodeId = null;

      long lCurrentNodeId = 0;
      List remainList = detailDtos;
      while (remainList.size() > 0) {
        FVoucherDTO detailDto = (FVoucherDTO) remainList.get(0);
        //获取第一个明细的当前流程节点
        currentNodeId = getCurrentNodeId(menuid, roleid, detailDto.getVou_id(), conn);
        lCurrentNodeId = Long.parseLong(currentNodeId);
        remainList = this.doSameDetailProcessEditObj(lCurrentNodeId, menuid, roleid, actionType, operationName,
          operationDate, operationRemark, detail_table_name, remainList, auto_tolly_flag, create_user, create_user_id,
          conn, inspect_flag);//在途终审记账列表内部赋值
      }

    } finally {
      dao.closeSession(session);
    }
    if (auto_tolly_flag)//在途审核记账
    {
      voucherService.updateBatch(detailDtos);
    }

    return detailDtos;
  }

  /**
   * 批量明细数据修改处理操作,不需要在工作流配置修改记账，根据auto_tolly_flag直接记账。
   * @param nCurrentItemNodeId
   * @param menuid
   * @param roleid
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param auto_tolly_flag
   * @param create_user
   * @param create_user_id
   * @param pcon
   * @param inspect_flag 是否资金监控 
   * @return
   * @throws Exception
   */
  private List doSameDetailProcessEditObj(long nCurrentItemNodeId, String menuid, String roleid, String actionType,
    String operationName, String operationDate, String operationRemark, String detail_table_name, List detailDtos,
    boolean auto_tolly_flag, String create_user, String create_user_id, Connection pcon, boolean inspect_flag)
    throws Exception {
    /** 相同节点数据 */
    ArrayList sameList = new ArrayList();
    /** 不再同一节点剩余未走完流程的数据 */
    ArrayList remainList = new ArrayList();

    //    转变节点信息      
    //修改原节点当前信息，状态改成未审核
    int[] curentUpdates = updateCurrentItem(detailDtos, nCurrentItemNodeId, pcon, "001");
    for (int i = 0; i < curentUpdates.length; i++) {
      //在当前节点就会被更新
      if (curentUpdates[i] <= 0) {
        //说明相同菜单角色配到了多个节点 需要在查找
        remainList.add(detailDtos.get(i));
      } else {
        sameList.add(detailDtos.get(i));
      }
    }
    PreparedStatement insertCurrentTaskCachePsmt = getCopyCurrentTaskCachePsmt(pcon);

    try {
      for (int i = 0, n = sameList.size(); i < n; i++) {
        FVoucherDTO detailDto = (FVoucherDTO) sameList.get(i);
        setValues4CopyCurrentTaskCachePsmt(insertCurrentTaskCachePsmt, detailDto.getVou_id(), nCurrentItemNodeId,
          actionType, create_user, operationName, operationDate, operationRemark, detailDto.getVou_money(),
          auto_tolly_flag ? 0 : -1, detailDto.getBilltype_code(), detailDto.getRcid(), detailDto.getCcid(),
          detailDto.getSet_month(), create_user_id);
      }
      //      统一监控
      if (inspect_flag) {
        this.doInspect(String.valueOf(this.getWfIdByNodeId(nCurrentItemNodeId, pcon)),
          String.valueOf(nCurrentItemNodeId), menuid, roleid, actionType, operationName, operationDate,
          operationRemark, detail_table_name, sameList, pcon);
      }
      //插入流程信息
      insertCurrentTaskCachePsmt.executeBatch();
      //删除上一个节点的的已完成信息
      deleteCompleteItem(sameList, nCurrentItemNodeId, pcon);
      //将当前的流转信息转移到历史表
      copyCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //记录工作流转流程前后信息
      insertRoute(sameList, nCurrentItemNodeId, pcon);
      //删除原有的流程信息
      deleteCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //将数据从临时表写入正式表
      copyCacheTasks(pcon);

    } finally {
      insertCurrentTaskCachePsmt.close();

    }

    return remainList;
  }

  /**
   * 批量明细数据修改挂起操作，不记账。
   * @param menuid
   * @param roleid
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @return
   * @throws Exception
   */
  public List doBatchDetailProcessHangObj(String menuid, String roleid, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List detailDtos) throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) detailDtos.get(0));
    }
    String[] creates = getCreateUser();
    String create_user = creates[0];
    String create_user_id = creates[1];
    Session session = dao.getSession();

    try {
      Connection conn = session.connection();
      String currentNodeId = null;

      long lCurrentNodeId = 0;
      List remainList = detailDtos;
      while (remainList.size() > 0) {
        FVoucherDTO detailDto = (FVoucherDTO) remainList.get(0);
        //获取第一个明细的当前流程节点
        currentNodeId = getCurrentNodeId(menuid, roleid, detailDto.getVou_id(), conn);
        lCurrentNodeId = Long.parseLong(currentNodeId);

        remainList = this.doSameDetailProcessHangObj(lCurrentNodeId, menuid, roleid, actionType, operationName,
          operationDate, operationRemark, detail_table_name, remainList, create_user, create_user_id, conn);//在途终审记账列表内部赋值
      }

    } finally {
      dao.closeSession(session);
    }

    return detailDtos;
  }

  /**
   * 批量明细数据挂起处理操作,不记账。
   * @param nCurrentItemNodeId
   * @param menuid
   * @param roleid
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param create_user
   * @param create_user_id
   * @param pcon
   * @return
   * @throws Exception
   */
  private List doSameDetailProcessHangObj(long nCurrentItemNodeId, String menuid, String roleid, String actionType,
    String operationName, String operationDate, String operationRemark, String detail_table_name, List detailDtos,
    String create_user, String create_user_id, Connection pcon) throws Exception {
    /** 相同节点数据 */
    ArrayList sameList = new ArrayList();
    /** 不再同一节点剩余未走完流程的数据 */
    ArrayList remainList = new ArrayList();

    //    转变节点信息      
    //修改原节点当前信息，状态改成未审核
    int[] curentUpdates = deleteCurrentItem(detailDtos, nCurrentItemNodeId, pcon);
    for (int i = 0; i < curentUpdates.length; i++) {
      //在当前节点就会被更新
      if (curentUpdates[i] <= 0) {
        //说明相同菜单角色配到了多个节点 需要在查找
        remainList.add(detailDtos.get(i));
      } else {
        sameList.add(detailDtos.get(i));
      }
    }
    PreparedStatement insertCurrentTaskCachePsmt = getCopyCurrentTaskCachePsmt(pcon);

    try {
      for (int i = 0, n = sameList.size(); i < n; i++) {
        FVoucherDTO detailDto = (FVoucherDTO) sameList.get(i);
        setValues4CopyCurrentTaskCachePsmt(insertCurrentTaskCachePsmt, detailDto.getVou_id(), nCurrentItemNodeId,
          actionType, create_user, operationName, operationDate, operationRemark, detailDto.getVou_money(), -1,
          detailDto.getBilltype_code(), detailDto.getRcid(), detailDto.getCcid(), detailDto.getSet_month(),
          create_user_id);
      }

      //插入流程信息
      insertCurrentTaskCachePsmt.executeBatch();
      //删除上一个节点的的已完成信息
      deleteCompleteItem(sameList, nCurrentItemNodeId, pcon);
      //挂起
      insertCompleteItem(sameList, nCurrentItemNodeId, pcon, "101");
      //将当前的流转信息转移到历史表
      copyCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //记录工作流转流程前后信息
      insertRoute(sameList, nCurrentItemNodeId, pcon);
      //删除原有的流程信息
      deleteCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //将数据从临时表写入正式表
      copyCacheTasks(pcon);

    } finally {
      insertCurrentTaskCachePsmt.close();
    }
    return remainList;
  }

  /**
   * 批量明细数据删除作废操作，不判读配置操作流的是否配置了删除作废记账根据auto_tolly_flag直接记账。
   * @param menuid
   * @param roleid
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param auto_tolly_flag 是否要记账
   * @param inspect_flag 是否资金监控 
   * @return
   * @throws Exception
   */
  public List doBatchDetailProcessDelOrDiscardObj(String menuid, String roleid, String actionType,
    String operationName, String operationDate, String operationRemark, String detail_table_name, List detailDtos,
    boolean auto_tolly_flag, boolean inspect_flag) throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) detailDtos.get(0));
    }
    String[] creates = getCreateUser();
    String create_user = creates[0];
    String create_user_id = creates[1];
    Session session = dao.getSession();

    try {
      Connection conn = session.connection();
      String currentNodeId = null;

      long lCurrentNodeId = 0;
      List remainList = detailDtos;
      while (remainList.size() > 0) {
        FVoucherDTO detailDto = (FVoucherDTO) remainList.get(0);
        //获取第一个明细的当前流程节点
        currentNodeId = getCurrentNodeId(menuid, roleid, detailDto.getVou_id(), conn);
        lCurrentNodeId = Long.parseLong(currentNodeId);

        remainList = this.doSameDetailProcessDelOrDiscardObj(lCurrentNodeId, menuid, roleid, actionType, operationName,
          operationDate, operationRemark, detail_table_name, remainList, create_user, create_user_id, conn,
          inspect_flag);//在途终审记账列表内部赋值
      }

    } finally {
      dao.closeSession(session);
    }
    if (auto_tolly_flag)//记账
    {
      if ("DISCARD".equals(actionType)) {
        voucherService.invalidBatch(detailDtos);
      } else if ("DELETE".equals(actionType)) {
        voucherService.deleteBatch(detailDtos);
      }
    }

    return detailDtos;
  }

  /**
   * 年结批量明细数据删除作废操作，不判读配置操作流的是否配置了删除作废记账根据auto_tolly_flag直接记账,不根据菜单角色直接作废删除。
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param auto_tolly_flag 是否要记账
   * @param inspect_flag 是否资金监控 
   * @return
   * @throws Exception
   */
  public List doYeadEndBatchDetailProcessDelOrDiscard(String actionType, String operationName, String operationDate,
    String operationRemark, String detail_table_name, List detailDtos, boolean auto_tolly_flag, boolean inspect_flag)
    throws Exception {
    if (detail_table_name == null) {
      detail_table_name = this.getTableName((FVoucherDTO) detailDtos.get(0));
    }
    String[] creates = getCreateUser();
    String create_user = creates[0];
    String create_user_id = creates[1];
    Session session = dao.getSession();

    try {
      Connection conn = session.connection();
      List currentNodeId = null;
      long lCurrentNodeId = 0;
      //已处理过列表
      List process = new ArrayList();
      List remainList = new ArrayList();
      remainList.addAll(detailDtos);
      for (int i = 0; i < detailDtos.size(); i++) {
        FVoucherDTO detailDto = (FVoucherDTO) remainList.get(i);
        //获得工作流终审标记  0终审，1未终审
        int is_end = detailDto.getIs_end();
        if (is_end == 0) {
          doEndBatchDetailNew(actionType, operationName, operationDate, operationRemark, detailDto, conn,
            detail_table_name);
        } else {
          //获取第一个明细的当前流程节点
          currentNodeId = getYeadEndCurrentNodeId(detailDto.getVou_id(), conn);
          if (currentNodeId == null || currentNodeId.size() == 0) {
            if (process.contains(detailDto)) {
              remainList.remove(i);
              continue;
            } else {
              throw new WFErrorNodeException(detailDto.getVou_id() + "未找到当前流程节点信息");
            }
          }
          for (int j = 0; j < currentNodeId.size(); j++) {
            lCurrentNodeId = Long.parseLong((String) currentNodeId.get(j));

            List list = this.doYearEndSameDetailProcessDelOrDiscardObj(lCurrentNodeId, actionType, operationName,
              operationDate, operationRemark, detail_table_name, remainList, create_user, create_user_id, conn,
              inspect_flag);//在途终审记账列表内部赋值
            process.add(list);
          }
          remainList.remove(i);
        }
      }
    } finally {
      dao.closeSession(session);
    }
    if (auto_tolly_flag)//记账
    {
      if ("DISCARD".equals(actionType)) {
        voucherService.invalidBatch(detailDtos);
      } else if ("DELETE".equals(actionType)) {
        voucherService.deleteBatch(detailDtos);
      }
    }

    return detailDtos;
  }

  /**
   * 已终审的数据走流程。
   * @param actionType操作类型
   * @param operationName
   * @param operationDate
   * @param operationRemark备注
   * @param vou数据对象
   * @param st
   * @param table_name表名
   * @return
   * @throws Exception
   */
  private void doEndBatchDetailNew(String actionType, String operationName, String operationDate,
    String operationRemark, FVoucherDTO vou, Connection conn, String table_name) throws Exception {

    String tolly_flag = "0";
    String entity_id = vou.getVou_id();
    String remark = "";

    //处理作废，在current表中插入 discard的数据
    insertDiscard(actionType, operationName, operationDate, operationRemark, remark, tolly_flag, entity_id, conn,
      table_name);

    // 将end tasks表中的数据挪到complete tasks表中
    moveEndTask(entity_id, table_name, conn);

    // 处理sys_wf_task_routing
    insertTaskRouting(entity_id, table_name, conn);

    // 删除end tasks表中的当前数据
    deleteEndTask(entity_id, table_name, conn);

    // 在complete item 表中删除当前的数据    
    deleteCompleteItem(entity_id, table_name, conn);

  }

  /** 将年初控制数作废记录插入到CURRENT表中的SQL */
  private static String INSERTCURRENTBYDISCARDSQL = "insert into sys_wf_current_tasks (TASK_ID,WF_ID,WF_TABLE_NAME,ENTITY_ID,CURRENT_NODE_ID,"
    + "NEXT_NODE_ID,ACTION_TYPE_CODE,IS_UNDO,CREATE_USER,CREATE_DATE,UNDO_USER, UNDO_DATE,OPERATION_NAME,"
    + "OPERATION_DATE,OPERATION_REMARK,INIT_MONEY,RESULT_MONEY,REMARK, TOLLY_FLAG,BILL_TYPE_CODE,"
    + "BILL_ID,RCID,CCID,SEND_MSG_DATE,AUTO_AUDIT_DATE,SET_MONTH,UPDATE_FLAG,CREATE_USER_ID,"
    + "rg_code,set_year) select "
    + (TypeOfDB.isOracle() ? " SEQ_SYS_WF_TASK_ID.Nextval " : " Nextval('SEQ_SYS_WF_TASK_ID') ")
    + ", wf_id, wf_table_name, entity_id,"
    + "  case when action_type_code in ('NEXT', 'BACK', 'INPUT') then next_node_id "
    + "when action_type_code in ('EDIT', 'HANG') then current_node_id end  next_node_id, 0,?, 0, ?, ?, null, null, ?, ?, ?, init_money, result_money, ?, "
    + "?, bill_type_code, bill_id, rcid, ccid, send_msg_date, auto_audit_date, set_month, 1, ? ,?,?  from sys_wf_end_tasks where entity_id = ? and wf_table_name=? and rg_code=? and set_year=?";

  /**
   * 将作废记录插入CURRENT表中的方法。
   * @param actionType操作类型
   * @param operationName
   * @param operationDate
   * @param operationRemark备注
   * @param remark
   * @param tolly_flag
   * @param entity_id
   * @param conn
   * @param table_name表名
   * @return
   * @throws Exception
   */
  private int insertDiscard(String actionType, String operationName, String operationDate, String operationRemark,
    String remark, String tolly_flag, String entity_id, Connection conn, String table_name) throws SQLException {

    String rg_code = getRgCode();
    String setYear = getSetYear();
    String create_date = Tools.getCurrDate();
    String[] creates = getCreateUser();
    String create_user = creates[0];
    String create_user_id = creates[1];

    PreparedStatement ptmt = null;

    try {
      ptmt = conn.prepareStatement(INSERTCURRENTBYDISCARDSQL);
      ptmt.setString(1, actionType);
      ptmt.setString(2, create_user);
      ptmt.setString(3, create_date);
      ptmt.setString(4, operationName);
      ptmt.setString(5, operationDate);
      ptmt.setString(6, operationRemark);
      ptmt.setString(7, remark);
      ptmt.setString(8, tolly_flag);
      ptmt.setString(9, create_user_id);
      ptmt.setString(10, rg_code);
      ptmt.setString(11, setYear);
      ptmt.setString(12, entity_id);
      ptmt.setString(13, table_name);
      ptmt.setString(14, rg_code);
      ptmt.setString(15, setYear);

      return ptmt.executeUpdate();

    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n SQL:"
        + INSERTCURRENTBYDISCARDSQL + "\n 参数：actionType:" + actionType + " create_user:" + create_user
        + " create_date:" + create_date + " operationName:" + operationName + " operationDate:" + operationDate
        + " operationRemark:" + operationRemark + " remark:" + remark + " tolly_flag:" + tolly_flag
        + " create_user_id:" + create_user_id + " rg_code:" + rg_code + " set_year" + setYear + " entity_id"
        + entity_id + " table_name" + table_name);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ptmt != null)
        ptmt.close();
    }
  }

  /** 将END_TASK表中的记录插入到COMPLETE_TASK中的SQL*/
  private static String INSERTCOMPLETEFROMENDSQL = "insert into sys_wf_complete_tasks select b.* from sys_wf_end_tasks b"
    + " where b.entity_id =? and update_flag = '1' and wf_table_name=? and rg_code=? and set_year=?";

  /**
   * 将END_TASK中的记录移至COMPLETE_TASK中的方法。
   * @param entity_id
   * @param conn
   * @param table_name表名
   * @return
   * @throws Exception
   */
  private int moveEndTask(String entity_id, String table_name, Connection conn) throws SQLException {

    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ptmt = null;
    try {
      ptmt = conn.prepareStatement(INSERTCOMPLETEFROMENDSQL);
      ptmt.setString(1, entity_id);
      ptmt.setString(2, table_name);
      ptmt.setString(3, rg_code);
      ptmt.setString(4, setYear);

      return ptmt.executeUpdate();

    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n SQL:"
        + INSERTCOMPLETEFROMENDSQL + "\n 参数：entity_id:" + entity_id + " table_name" + table_name + " rg_code:"
        + rg_code + " set_year" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ptmt != null) {
        ptmt.close();
      }
    }
  };

  /** 处理sys_wf_task_routing的SQL */
  private static String INSERTTASKROUTINGSQL = "insert into sys_wf_task_routing select v1.task_id, v2.task_id, null,? as set_year,? as rg_code from"
    + " ( select t1.task_id,  case when action_type_code in ('NEXT', 'BACK', 'INPUT') then next_node_id  when action_type_code in ('EDIT', 'HANG') then current_node_id end  next_node_id"
    + " from sys_wf_current_tasks t1 where t1.entity_id = ? and t1.update_flag = '1' and t1.wf_table_name=?) v1, (select t2.task_id, t2.current_node_id from sys_wf_current_tasks t2"
    + " where t2.entity_id =? and t2.update_flag = '0' and t2.wf_table_name=? ) v2 where v1.next_node_id = v2.current_node_id ";

  /**
   * 处理sys_wf_task_routing的方法。
   * @param entity_id
   * @param conn
   * @param table_name表名
   * @return
   * @throws Exception
   */
  private int insertTaskRouting(String entity_id, String table_name, Connection conn) throws SQLException {
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ptmt = null;
    try {
      ptmt = conn.prepareStatement(INSERTTASKROUTINGSQL);
      ptmt.setString(1, setYear);
      ptmt.setString(2, rg_code);
      ptmt.setString(3, entity_id);
      ptmt.setString(4, table_name);
      ptmt.setString(5, entity_id);
      ptmt.setString(6, table_name);

      return ptmt.executeUpdate();

    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n SQL:" + INSERTTASKROUTINGSQL
        + "\n 参数：entity_id:" + entity_id + " table_name" + table_name + " rg_code:" + rg_code + " set_year" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ptmt != null) {
        ptmt.close();
      }
    }
  }

  /** 删除END_TASK的SQL */
  private static String DELETEENDTASKSQL = "delete from sys_wf_end_tasks b where b.update_flag = '1' and b.entity_id =? "
    + "and b.wf_table_name=? and b.rg_code=? and b.set_year= ?";

  /**
   * 处理END_TASK的方法。
   * @param entity_id
   * @param conn
   * @param table_name表名
   * @return
   * @throws Exception
   */
  private int deleteEndTask(String entity_id, String table_name, Connection conn) throws SQLException {
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ptmt = null;
    try {
      ptmt = conn.prepareStatement(DELETEENDTASKSQL);
      ptmt.setString(1, entity_id);
      ptmt.setString(2, table_name);
      ptmt.setString(3, rg_code);
      ptmt.setString(4, setYear);
      return ptmt.executeUpdate();
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + " SQL:" + DELETEENDTASKSQL
        + " 参数：entity_id:" + entity_id + " table_name:" + table_name + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ptmt != null) {
        ptmt.close();
      }
    }
  }

  /** 删除COMPLETE_ITEM的SQL */
  private static String DELETECOMPLETEITEMSQL = "delete from sys_wf_complete_item c where c.entity_id = ? and c.set_year = ?"
    + " and c.rg_code = ? and exists ( select 1 from sys_wf_current_tasks ct where ct.entity_id=?"
    + " and (c.node_id=ct.current_node_id or c.node_id=ct.next_node_id) and ct.wf_table_name=? union "
    + " select 1 from sys_wf_complete_tasks mt where mt.entity_id= ? and (c.node_id=mt.current_node_id or c.node_id=mt.next_node_id) and mt.wf_table_name=?)";

  /**
   * 处理COMPLETE_ITEM的方法。
   * @param entity_id
   * @param conn
   * @param table_name表名
   * @return
   * @throws Exception
   */
  private int deleteCompleteItem(String entity_id, String table_name, Connection conn) throws Exception {
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ptmt = null;
    try {
      ptmt = conn.prepareStatement(DELETECOMPLETEITEMSQL);
      ptmt.setString(1, entity_id);
      ptmt.setString(2, setYear);
      ptmt.setString(3, rg_code);
      ptmt.setString(4, entity_id);
      ptmt.setString(5, table_name);
      ptmt.setString(6, entity_id);
      ptmt.setString(7, table_name);

      return ptmt.executeUpdate();

    } catch (WFIllegalSqlOperateException e) {
      StringBuffer sb = new StringBuffer(e.getMessage() + " SQL为：" + DELETECOMPLETEITEMSQL + " 参数为：entity_id:"
        + entity_id + " set_year:" + setYear + " rg_code:" + rg_code + " table_name:" + table_name);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ptmt != null) {
        ptmt.close();
      }
    }
  }

  /**
   * 批量明细数据作废处理操作,按节点作废。
   * @param nCurrentItemNodeId  
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param create_user
   * @param create_user_id
   * @param pcon
   * @param inspect_flag 是否资金监控 
   * @return
   * @throws Exception
   */

  public List doDetailProcessDelOrDiscardObjByNodeId(long nCurrentItemNodeId, String actionType, String operationName,
    String operationDate, String operationRemark, String detail_table_name, List detailDtos, Connection pcon)
    throws Exception {
    /** 相同节点数据 */
    ArrayList sameList = new ArrayList();
    /** 不再同一节点剩余未走完流程的数据 */
    ArrayList remainList = new ArrayList();
    String[] creates = getCreateUser();
    String create_user = creates[0];
    String create_user_id = creates[1];

    //    转变节点信息      
    //修改原节点当前信息，状态改成未审核
    int[] curentUpdates = deleteCurrentItem(detailDtos, nCurrentItemNodeId, pcon);
    for (int i = 0; i < curentUpdates.length; i++) {
      //在当前节点就会被更新
      if (curentUpdates[i] <= 0) {
        //说明相同菜单角色配到了多个节点 需要在查找
        remainList.add(detailDtos.get(i));
      } else {
        sameList.add(detailDtos.get(i));
      }
    }
    PreparedStatement insertCurrentTaskCachePsmt = getCopyCurrentTaskCachePsmt(pcon);

    try {

      for (int i = 0, n = sameList.size(); i < n; i++) {
        FVoucherDTO detailDto = (FVoucherDTO) sameList.get(i);

        setValues4CopyCurrentTaskCachePsmt(insertCurrentTaskCachePsmt, detailDto.getVou_id(), nCurrentItemNodeId,
          actionType, create_user, operationName, operationDate, operationRemark, detailDto.getVou_money(), -1,
          detailDto.getBilltype_code(), detailDto.getRcid(), detailDto.getCcid(), detailDto.getSet_month(),
          create_user_id);
      }
      //插入流程信息
      insertCurrentTaskCachePsmt.executeBatch();
      //删除上一个节点的的已完成信息
      deleteCompleteItem(sameList, nCurrentItemNodeId, pcon);
      //删除或作废
      insertCompleteItem(sameList, nCurrentItemNodeId, pcon, "DELETE".equals(actionType) ? "102" : "103");
      //将当前的流转信息转移到历史表
      copyCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //记录工作流转流程前后信息
      insertRoute(sameList, nCurrentItemNodeId, pcon);
      //删除原有的流程信息
      deleteCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //将数据从临时表写入正式表
      copyCacheTasks(pcon);

    } finally {
      insertCurrentTaskCachePsmt.close();

    }

    return remainList;
  }

  /**
   * 批量明细数据作废处理操作。
   * @param nCurrentItemNodeId
   * @param menuid
   * @param roleid
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param create_user
   * @param create_user_id
   * @param pcon
   * @param inspect_flag 是否资金监控 
   * @return
   * @throws Exception
   */

  private List doSameDetailProcessDelOrDiscardObj(long nCurrentItemNodeId, String menuid, String roleid,
    String actionType, String operationName, String operationDate, String operationRemark, String detail_table_name,
    List detailDtos, String create_user, String create_user_id, Connection pcon, boolean inspect_flag) throws Exception {
    /** 相同节点数据 */
    ArrayList sameList = new ArrayList();
    /** 不再同一节点剩余未走完流程的数据 */
    ArrayList remainList = new ArrayList();

    //    转变节点信息      
    //修改原节点当前信息，状态改成未审核
    int[] curentUpdates = deleteCurrentItem(detailDtos, nCurrentItemNodeId, pcon);
    for (int i = 0; i < curentUpdates.length; i++) {
      //在当前节点就会被更新
      if (curentUpdates[i] <= 0) {
        //说明相同菜单角色配到了多个节点 需要在查找
        remainList.add(detailDtos.get(i));
      } else {
        sameList.add(detailDtos.get(i));
      }
    }
    PreparedStatement insertCurrentTaskCachePsmt = getCopyCurrentTaskCachePsmt(pcon);

    try {

      for (int i = 0, n = sameList.size(); i < n; i++) {
        FVoucherDTO detailDto = (FVoucherDTO) sameList.get(i);

        if (this.existCurrentTask(detailDto.getVou_id(), detail_table_name, nCurrentItemNodeId, pcon)) {
          throw new WFErrorNodeException("明细数据" + detailDto.getVou_id() + "存在不在当前删除作废节点的多个流程数据不能正常作废删除");
        }

        setValues4CopyCurrentTaskCachePsmt(insertCurrentTaskCachePsmt, detailDto.getVou_id(), nCurrentItemNodeId,
          actionType, create_user, operationName, operationDate, operationRemark, detailDto.getVou_money(), -1,
          detailDto.getBilltype_code(), detailDto.getRcid(), detailDto.getCcid(), detailDto.getSet_month(),
          create_user_id);
      }
      //      统一监控
      if (inspect_flag) {
        this.doInspect(String.valueOf(this.getWfIdByNodeId(nCurrentItemNodeId, pcon)),
          String.valueOf(nCurrentItemNodeId), menuid, roleid, actionType, operationName, operationDate,
          operationRemark, detail_table_name, sameList, pcon);
      }
      //插入流程信息
      insertCurrentTaskCachePsmt.executeBatch();
      //删除上一个节点的的已完成信息
      deleteCompleteItem(sameList, nCurrentItemNodeId, pcon);
      //删除或作废
      insertCompleteItem(sameList, nCurrentItemNodeId, pcon, "DELETE".equals(actionType) ? "102" : "103");
      //将当前的流转信息转移到历史表
      copyCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //记录工作流转流程前后信息
      insertRoute(sameList, nCurrentItemNodeId, pcon);
      //删除原有的流程信息
      deleteCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //将数据从临时表写入正式表
      copyCacheTasks(pcon);

    } finally {
      insertCurrentTaskCachePsmt.close();

    }

    return remainList;
  }

  /**
   * 批量明细数据作废处理操作。
   * @param nCurrentItemNodeId
   * @param menuid
   * @param roleid
   * @param actionType
   * @param operationName
   * @param operationDate
   * @param operationRemark
   * @param detail_table_name
   * @param detailDtos
   * @param create_user
   * @param create_user_id
   * @param pcon
   * @param inspect_flag 是否资金监控 
   * @return
   * @throws Exception
   */

  private List doYearEndSameDetailProcessDelOrDiscardObj(long nCurrentItemNodeId, String actionType,
    String operationName, String operationDate, String operationRemark, String detail_table_name, List detailDtos,
    String create_user, String create_user_id, Connection pcon, boolean inspect_flag) throws Exception {
    /** 相同节点数据 */
    ArrayList sameList = new ArrayList();

    //    转变节点信息      
    //修改原节点当前信息，状态改成未审核
    int[] curentUpdates = deleteCurrentItem(detailDtos, nCurrentItemNodeId, pcon);
    for (int i = 0; i < curentUpdates.length; i++) {
      //在当前节点就会被更新
      if (curentUpdates[i] > 0) {
        sameList.add(detailDtos.get(i));
      }
    }
    PreparedStatement insertCurrentTaskCachePsmt = getCopyCurrentTaskCachePsmt(pcon);

    try {

      for (int i = 0, n = sameList.size(); i < n; i++) {
        FVoucherDTO detailDto = (FVoucherDTO) sameList.get(i);

        if (this.existCurrentTask(detailDto.getVou_id(), detail_table_name, nCurrentItemNodeId, pcon)) {
          throw new WFErrorNodeException("明细数据" + detailDto.getVou_id() + "存在不在当前删除作废节点的多个流程数据不能正常作废删除");
        }

        setValues4CopyCurrentTaskCachePsmt(insertCurrentTaskCachePsmt, detailDto.getVou_id(), nCurrentItemNodeId,
          actionType, create_user, operationName, operationDate, operationRemark, detailDto.getVou_money(), -1,
          detailDto.getBilltype_code(), detailDto.getRcid(), detailDto.getCcid(), detailDto.getSet_month(),
          create_user_id);
      }

      //插入流程信息
      insertCurrentTaskCachePsmt.executeBatch();
      //删除上一个节点的的已完成信息
      deleteCompleteItem(sameList, nCurrentItemNodeId, pcon);
      //删除或作废
      insertCompleteItem(sameList, nCurrentItemNodeId, pcon, "DELETE".equals(actionType) ? "102" : "103");
      //将当前的流转信息转移到历史表
      copyCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //记录工作流转流程前后信息
      insertRoute(sameList, nCurrentItemNodeId, pcon);
      //删除原有的流程信息
      deleteCurrentTask(sameList, nCurrentItemNodeId, pcon);
      //将数据从临时表写入正式表
      copyCacheTasks(pcon);

    } finally {
      insertCurrentTaskCachePsmt.close();

    }

    return sameList;
  }

  /*  查询当前任务的taskid */
  //modify by liuzw 20120410 增加对多财政多年度的支持
  private static String CURRENTTASKIDSQL = "select task_id,tolly_flag from sys_wf_current_tasks a  "
    + " where a.current_node_id = ? and a.entity_id=?  and a.rg_code=? and a.set_year=? union all select task_id,tolly_flag from sys_wf_end_tasks b  "
    + " where b.current_node_id = ? and b.entity_id=?  and b.rg_code=? and b.set_year=? ";

  private long[] tempTasks = new long[100];//同一个节点最多有100个退回流转线 一般不会超过10个但为了加速可保险期间统一申请了100个

  /**
   * 取当前的流程taskid，用于撤销
   * @param dto
   * @param completeItemNodeId
   * @param onList
   * @param endList
   * @param pcon
   * @param delCurrentTasksPsmt
   * @param delEndTasksPsmt
   * @param curend2CompleteTasksPsmt
   * @param delCurrentItemsPsmt
   * @param createUser
   * @param operationDate
   * @return
   * @throws Exception
   */
  private long[] getCurrentTaskId(FVoucherDTO dto, long completeItemNodeId, List onList, List endList, Connection pcon,
    PreparedStatement delCurrentTasksPsmt, PreparedStatement delEndTasksPsmt,
    PreparedStatement curend2CompleteTasksPsmt, PreparedStatement delCurrentItemsPsmt, String createUser,
    String operationDate) throws Exception {
    PreparedStatement ps = null;
    String rg_code = getRgCode();
    String setYear = getSetYear();
    long[] tasks = null;
    try {
      ps = pcon.prepareStatement(CURRENTTASKIDSQL);
      ps.setLong(1, completeItemNodeId);
      ps.setString(2, dto.getVou_id());
      ps.setString(3, rg_code);
      ps.setInt(4, Integer.parseInt(setYear));
      ps.setLong(5, completeItemNodeId);
      ps.setString(6, dto.getVou_id());
      ps.setString(7, rg_code);
      ps.setInt(8, Integer.parseInt(setYear));
      ResultSet rs = ps.executeQuery();
      int n = 0;
      while (rs.next()) {
        long taskId = rs.getLong(1);
        if (n == 0) {
          int tollyFlag = rs.getInt(2);
          if (tollyFlag == 0)//原来是在途审核 需要撤销在途审核
          {
            onList.add(dto);
          } else if (tollyFlag == 1)//原来是终审审核 需要撤销终审审核
          {
            endList.add(dto);
          }
        }
        tempTasks[n] = taskId;
        delCurrentTasksPsmt.setLong(1, taskId);
        delCurrentTasksPsmt.setString(2, rg_code);
        delCurrentTasksPsmt.setInt(3, Integer.parseInt(setYear));
        delCurrentTasksPsmt.addBatch();
        delEndTasksPsmt.setLong(1, taskId);
        delEndTasksPsmt.setString(2, rg_code);
        delEndTasksPsmt.setInt(3, Integer.parseInt(setYear));
        delEndTasksPsmt.addBatch();
        curend2CompleteTasksPsmt.setString(1, createUser);
        curend2CompleteTasksPsmt.setString(2, operationDate);
        curend2CompleteTasksPsmt.setLong(3, taskId);
        curend2CompleteTasksPsmt.setString(4, rg_code);
        curend2CompleteTasksPsmt.setInt(5, Integer.parseInt(setYear));
        curend2CompleteTasksPsmt.setString(6, createUser);
        curend2CompleteTasksPsmt.setString(7, operationDate);
        curend2CompleteTasksPsmt.setLong(8, taskId);
        curend2CompleteTasksPsmt.setString(9, rg_code);
        curend2CompleteTasksPsmt.setInt(10, Integer.parseInt(setYear));
        curend2CompleteTasksPsmt.addBatch();
        delCurrentItemsPsmt.setLong(1, taskId);
        delCurrentItemsPsmt.setString(2, rg_code);
        delCurrentItemsPsmt.setInt(3, Integer.parseInt(setYear));
        delCurrentItemsPsmt.setString(4, rg_code);
        delCurrentItemsPsmt.setInt(5, Integer.parseInt(setYear));
        delCurrentItemsPsmt.addBatch();
        n++;
      }
      if (n == 0) {
        StringBuffer sb = new StringBuffer("明细id为" + dto.getVou_id() + "没有找到流程数据无法撤销" + "\n" + ";sql="
          + CURRENTTASKIDSQL + "\n参数:" + " current_node_id:" + completeItemNodeId + " entity_id" + dto.getVou_id()
          + "rg_code" + rg_code + " set_year:" + setYear);
        log(sb.toString());
        throw new WFErrorNodeException("明细id为" + dto.getVou_id() + "没有找到流程数据无法撤销");
      }
      tasks = new long[n];
      System.arraycopy(tempTasks, 0, tasks, 0, n);
      rs.close();
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n" + ";sql=" + CURRENTTASKIDSQL
        + " 参数:" + " current_node_id:" + completeItemNodeId + " entity_id" + dto.getVou_id() + "rg_code" + rg_code
        + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
        ps = null;
      }

    }

    return tasks;
  }

  /**
   * 根据当前流程节点及明细ID，取前节点的taskid，用于退回
   * @param dto
   * @param completeItemNodeId
   * @param onList
   * @param endList
   * @param pcon
   * @param delCurrentTasksPsmt
   * @param delEndTasksPsmt
   * @param curend2CompleteTasksPsmt
   * @param delCurrentItemsPsmt
   * @param createUser
   * @param operationDate
   * @return
   * @throws Exception
   */
  private long[] getPreTaskId(String vouId, long nCurrentItemNodeId, Connection pcon) throws Exception {
    String rg_code = getRgCode();
    String set_Year = getSetYear();
    PreparedStatement ps = null;
    ResultSet rs = null;
    long[] tasks = null;
    long[] sameLevelNodes = this.getSameLevelNextNodes(nCurrentItemNodeId, pcon);
    StringBuffer sql = new StringBuffer();
    sql
      .append("select t.current_node_id,t.next_node_id from sys_wf_current_tasks t where t.current_node_id=?  and t.entity_id=? and t.rg_code=? and t.set_year=? and t.next_node_id in (");
    sql.append(Tools.arrToSqllong(sameLevelNodes)).append(")").append(" union ");
    sql
      .append("select sw.current_node_id,sw.next_node_id from sys_wf_complete_tasks sw where sw.current_node_id=?  and sw.entity_id=? and sw.rg_code=? and sw.set_year=?  and sw.next_node_id in (");
    sql.append(Tools.arrToSqllong(sameLevelNodes)).append(")");
    try {
      ps = pcon.prepareStatement(sql.toString());
      ps.setLong(1, nCurrentItemNodeId);
      ps.setString(2, vouId);
      ps.setString(3, rg_code);
      ps.setInt(4, Integer.parseInt(set_Year));
      ps.setLong(5, nCurrentItemNodeId);
      ps.setString(6, vouId);
      ps.setString(7, rg_code);
      ps.setInt(8, Integer.parseInt(set_Year));
      rs = ps.executeQuery();
      int n = 0;
      while (rs.next()) {
        long taskId = rs.getLong(2);
        tempTasks[n] = taskId;
        n++;
      }
      tasks = new long[n];
      System.arraycopy(tempTasks, 0, tasks, 0, n);
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n" + ";sql=" + CURRENTTASKIDSQL
        + " 参数:" + " current_node_id:" + nCurrentItemNodeId + " entity_id" + vouId + "rg_code" + rg_code + " set_year:"
        + set_Year);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
        ps = null;
      }
      if (rs != null) {
        rs.close();
        rs = null;
      }

    }

    return tasks;
  }

  /**
   * 判断当前节点的来源节点下是否存在分支数据，且分支流程中已有部分流程走了后续流程，存在这种数据应将多个流程撤销才能再进行退回
   * @param wf_id
   * @param entity_id  明细id
   * @param wf_table_name  业务表
   * @param pre_node_id 来源节点
   * @param nextNodes  来源节点的所有后续节点
   * @param pcon
   * @return
   * @throws SQLException
   * @author justin(liwq2) 2014-8-21 
   */
  private boolean canBackTask(long wf_id, String entity_id, String wf_table_name, long pre_node_id, long[] nextNodes,
    Connection pcon) throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    String rg_code = getRgCode();
    String set_Year = getSetYear();
    //同级节点
    long[] sameLevelNodes = getSameLevelNextNodes(pre_node_id, pcon);
    StringBuffer sb = new StringBuffer();
    sb.append(" select current_node_id from sys_wf_current_tasks where current_node_id <> ? and next_node_id not in(");
    sb.append(Tools.arrToSqllong(sameLevelNodes)).append(") ");
    sb.append(" and wf_table_name =?  and entity_id =?");
    sb.append(" and rg_code = ? and set_year=? union all select current_node_id  from sys_wf_end_tasks where wf_table_name = ? and entity_id = ? and rg_code = ? and set_year=? ");

    try {
      //1-获取工作流表中当前明细的其他流程信息
      ps = pcon.prepareStatement(sb.toString());
      ps.setLong(1, pre_node_id);
      ps.setString(2, wf_table_name);
      ps.setString(3, entity_id);
      ps.setString(4, rg_code);
      ps.setInt(5, Integer.parseInt(set_Year));
      ps.setString(6, wf_table_name);
      ps.setString(7, entity_id);
      ps.setString(8, rg_code);
      ps.setInt(9, Integer.parseInt(set_Year));
      rs = ps.executeQuery();
      //3-判断工作流表中的其他节点数
      while (rs.next()) {
        long node = rs.getLong(1);
        //3-判断其他流程的当前节点是否是来源节点的后续节点
        /**
         * 			节点1
         *         / 	\
         *       节点2 	节点3
         *      /   \
         *	节点2-1  节点2-2   
         *				\
         *			节点2-2-1
         * 这种情况下 sys_wf_current_tasks表中有可能会存在 节点1——节点3、节点2——节点2-1、节点2-2——节点2-2-1类型的数据，
         * 当节点2-1退回至节点2或节点1时，需要区分节点1——节点3与节点2-2——节点2-2-1这两种数据的不同
         * */
        if (Arrays.binarySearch(nextNodes, node) >= 0) {
          return true;
        }
      }
    } finally {
      if (ps != null) {
        ps.close();
        ps = null;
      }
      if (rs != null) {
        rs.close();
        rs = null;
      }
    }
    return false;
  }

  /**
   * 通过流转历史表取到历史task_id同时准备从数据从历史表转移到当前表
   * @param taskId
   * @param pcon
   * @param delCompleteTasksPsmt
   * @param complete2CurrentTasksPsmt
   * @param insertCompleteItemPsmt
   * @param detailData 2013-06-04李文全增加 
   * @return
   * @throws Exception
   */
  private long[] getCompleteTaskId(long taskId, Connection pcon, PreparedStatement delCompleteTasksPsmt,
    PreparedStatement complete2CurrentTasksPsmt, PreparedStatement insertCompleteItemPsmt, XMLData xmlDataTmp)
    throws Exception {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;

    long[] tasks = null;
    String sql = "select task_id from sys_wf_task_routing where next_task_id=? and rg_code=? and set_year=?";
    try {
      ps = pcon.prepareStatement(sql);
      ps.setLong(1, taskId);
      ps.setString(2, rg_code);
      ps.setInt(3, Integer.parseInt(setYear));

      ResultSet rs = ps.executeQuery();
      int n = 0;
      while (rs.next()) {
        //modify by liuzw 20120410 增加对多财政多年度的支持
        tempTasks[n] = rs.getLong(1);
        delCompleteTasksPsmt.setLong(1, tempTasks[n]);
        delCompleteTasksPsmt.setString(2, rg_code);
        delCompleteTasksPsmt.setInt(3, Integer.parseInt(setYear));
        delCompleteTasksPsmt.addBatch();
        complete2CurrentTasksPsmt.setLong(1, tempTasks[n]);
        complete2CurrentTasksPsmt.setString(2, rg_code);
        complete2CurrentTasksPsmt.setInt(3, Integer.parseInt(setYear));
        complete2CurrentTasksPsmt.addBatch();
        insertCompleteItemPsmt.setString(1, (String) xmlDataTmp.get("mb_id"));
        insertCompleteItemPsmt.setString(2, (String) xmlDataTmp.get("agency_id"));
        insertCompleteItemPsmt.setString(3, (String) xmlDataTmp.get("pb_id"));
        insertCompleteItemPsmt.setString(4, (String) xmlDataTmp.get("cb_id"));
        insertCompleteItemPsmt.setString(5, (String) xmlDataTmp.get("ib_id"));
        insertCompleteItemPsmt.setString(6, (String) xmlDataTmp.get("gp_agency_id"));
        insertCompleteItemPsmt.setLong(7, tempTasks[n]);
        insertCompleteItemPsmt.setString(8, rg_code);
        insertCompleteItemPsmt.setInt(9, Integer.parseInt(setYear));
        insertCompleteItemPsmt.addBatch();
        n++;
      }
      if (n == 0) {
        throw new WFErrorNodeException("task_id为" + taskId + "没有找到流程前后任务无法撤销");
      }
      tasks = new long[n];
      System.arraycopy(tempTasks, 0, tasks, 0, n);

      rs.close();
      ps.close();
      ps = null;

    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n" + e.getMessage() + ";sql="
        + sql + " 参数:" + "taskid:" + taskId + " rg_code" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }

    return tasks;
  }

  /**
   * 判断是否可以撤销，历史记录中is_undo=1的才能撤销,为1 表示做过撤销。
   * @param taskId
   * @param pcon
   * @return
   * @throws Exception
   */
  private boolean canRecallTaskId(long taskId, Connection pcon) throws Exception {
    PreparedStatement ps = null;
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    boolean bool = true;
    String sql = "select is_undo from sys_wf_complete_tasks where task_id in (select next_task_id from sys_wf_task_routing where task_id=? and rg_code=? and set_year=?) and rg_code=? and set_year=?";
    try {
      ps = pcon.prepareStatement(sql);
      ps.setLong(1, taskId);
      ps.setString(2, rg_code);
      ps.setInt(3, Integer.parseInt(setYear));
      ps.setString(4, rg_code);
      ps.setInt(5, Integer.parseInt(setYear));

      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        if (rs.getInt(1) == 0) {
          return false;
        }
      }
      rs.close();
      ps.close();
      ps = null;
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\nsql=" + sql + "\n参数:"
        + "taskid:" + taskId + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }

    return bool;
  }

  //查询当前节点
  private static String CURRENTNODEIDSQL = "select b.node_id from sys_wf_current_item b "
    + " where exists (select 1 from sys_wf_module_node c where c.node_id=b.node_id and c.module_id=? and c.rg_code = b.rg_code and c.set_year = b.set_year) "
    + " and exists (select 1 from sys_wf_role_node d where d.node_id=b.node_id and d.role_id=? and d.rg_code = b.rg_code and d.set_year = b.set_year) "
    + " and b.entity_id=?  and b.rg_code=? and b.set_year=? ";

  /**
   * 根据菜单，角色和当前数据获取当前前进节点，如果没有获取到返回null
   * @param menuid
   * @param roleid
   * @param entity_id
   * @param pcon
   * @return
  * @throws Exception 
   */
  private String getCurrentNodeId(String menuid, String roleid, String entity_id, Connection pcon) throws Exception {
    PreparedStatement ps = null;
    String currentNodeId = null;

    String rg_code = getRgCode();
    String setYear = getSetYear();
    try {
      ps = pcon.prepareStatement(CURRENTNODEIDSQL);
      ps.setString(1, menuid);
      ps.setString(2, roleid);
      ps.setString(3, entity_id);
      ps.setString(4, rg_code);
      ps.setInt(5, Integer.parseInt(setYear));
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        currentNodeId = rs.getString(1);
      }
      rs.close();
    } catch (SQLException e) {
      throw new FAppException(e);
    } finally {
      if (ps != null)
        ps.close();
    }
    if (currentNodeId == null) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.NOT_FOUND_CURRENT_NODE + "\n SQL："
        + CURRENTNODEIDSQL + " 参数：menuid:" + menuid + " role_id:" + roleid + " entity_id:" + entity_id + " set_year:"
        + setYear + " rg_code" + rg_code);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.NOT_FOUND_CURRENT_NODE);
    }
    return currentNodeId;
  }

  /*  查询当前节点sql */
  private static String YEADENDCURRENTNODEIDSQL = "select a.node_id from sys_wf_nodes a,sys_wf_current_item b "
    + " where a.node_id = b.node_id and b.entity_id=?  and b.rg_code=? and b.set_year=? and a.rg_code=? and a.set_year=?";

  /**
   * 根据下一节点查询出工作流上一节点相关信息
   * @param menuid
   * @param roleid
   * @param entity_id
   * @param pcon
   * @return
  * @throws Exception 
   */
  private List getPreNodeIdByNextNode(String[] nextNodes) throws Exception {
    String rg_code = getRgCode();
    String setYear = getSetYear();
    List list = new ArrayList();
    String plsqlString = Tools.arrToSqlString(nextNodes);
    List currentNodesList = dao.findBySql(QUEYRPRENODEBYNEXTNODE + " and t.next_node_id in(" + plsqlString.toString()
      + ")", new String[] { rg_code, setYear });
    if (currentNodesList != null && currentNodesList.size() > 0) {
      for (int k = 0; k < currentNodesList.size(); k++) {
        Map obj = (Map) currentNodesList.get(k);
        Object[] wf = new Object[5];
        wf[0] = new Long(obj.get("wf_id").toString());
        wf[1] = new Long(obj.get("node_id").toString());
        wf[2] = new Long(obj.get("next_node_id").toString());
        wf[3] = obj.get("line_rule_id");
        list.add(wf);
      }
    }
    return list;
  }

  private static String QUEYRPRENODEBYNEXTNODE = "select t.wf_id,t.node_id,t.next_node_id,t.line_rule_id from sys_wf_node_conditions t where t.routing_type='001' and t.rg_code=? and t.set_year=? ";

  /**
   * 根据菜单，角色和当前数据获取当前前进节点，如果没有获取到返回null
   * @param menuid
   * @param roleid
   * @param entity_id
   * @param pcon
   * @return
   * @throws FAppException
   */
  private List getYeadEndCurrentNodeId(String entity_id, Connection pcon) throws Exception {
    PreparedStatement ps = null;
    List currentNodeId = new ArrayList();
    String rg_code = getRgCode();
    String setYear = getSetYear();
    try {
      ps = pcon.prepareStatement(YEADENDCURRENTNODEIDSQL);

      ps.setString(1, entity_id);
      ps.setString(2, rg_code);
      ps.setInt(3, Integer.parseInt(setYear));
      ps.setString(4, rg_code);
      ps.setInt(5, Integer.parseInt(setYear));
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        currentNodeId.add(rs.getString(1));
      }

      rs.close();
      ps.close();
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n  sql为："
        + YEADENDCURRENTNODEIDSQL + "参数为：entity_id:" + entity_id + " rg_code" + rg_code + " set_year" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }

    return currentNodeId;
  }

  /*  查询当前已完成节点sql */
  //modify by liuzw 20120327 增加对多财政多年度的支持
  private static String COMPLETENODEIDBACKSQL = "select a.node_id from sys_wf_nodes a,sys_wf_complete_item b "
    + " where exists (select 1 from sys_wf_module_node c where c.node_id=a.node_id and c.module_id=?  and c.rg_code=? and c.set_year=?) "
    + " and exists (select 1 from sys_wf_role_node d where d.node_id=a.node_id and d.role_id=? and d.rg_code=? and d.set_year=?) "
    + " and a.node_id = b.node_id and b.entity_id=? and b.rg_code=? and b.set_year=? and a.rg_code=? and a.set_year=?";

  /**
   * 根据菜单，角色和当前数据获取当前完成操作节点，准备撤销，如果没有获取到返回null
   * @param menuid
   * @param roleid
   * @param entity_id
   * @param pcon
   * @return
   * @throws Exception 
   * @throws FAppException
   */
  private String getCompleteNodeId(String menuid, String roleid, String entity_id, Connection pcon) throws Exception {
    PreparedStatement ps = null;
    String currentNodeId = null;
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    try {
      //modify by liuzw 20120410 增加对多财政多年度的支持
      ps = pcon.prepareStatement(COMPLETENODEIDBACKSQL);
      ps.setString(1, menuid);
      ps.setString(2, rg_code);
      ps.setInt(3, Integer.parseInt(setYear));
      ps.setString(4, roleid);
      ps.setString(5, rg_code);
      ps.setInt(6, Integer.parseInt(setYear));
      ps.setString(7, entity_id);
      ps.setString(8, rg_code);
      ps.setInt(9, Integer.parseInt(setYear));
      ps.setString(10, rg_code);
      ps.setInt(11, Integer.parseInt(setYear));
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        currentNodeId = rs.getString(1);
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + " SQL:" + COMPLETENODEIDBACKSQL
        + " 参数：menuid:" + menuid + " role_id:" + roleid + " entity_id:" + entity_id + " rg_code:" + rg_code
        + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }
    if (currentNodeId == null) {
      StringBuffer sb = new StringBuffer("明细id:" + entity_id + WorkFlowExceptionConstants.NOT_FOUND_COMPLETE_NODE
        + " SQL:" + COMPLETENODEIDBACKSQL + " 参数：menuid:" + menuid + " role_id:" + roleid + " entity_id:" + entity_id
        + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new WFErrorNodeException("明细id:" + entity_id + WorkFlowExceptionConstants.NOT_FOUND_COMPLETE_NODE);
    }
    return currentNodeId;
  }

  /*  删除当前节点sql */
  //增加对多财政多年度的支持
  private static String DELETECURRENTNODESQL = "delete sys_wf_current_item where entity_id=? and node_id=? and rg_code=? and set_year=?";

  /**
   * 删除当前节点
   * @param dtoDetails
   * @param node_id
   * @param pcon
   * @throws SQLException 
   */
  private int[] deleteCurrentItem(List dtoDetails, long node_id, Connection pcon) throws SQLException {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();

    PreparedStatement ps = null;
    int[] updates = new int[dtoDetails.size()];
    String vouId = "";
    try {
      ps = pcon.prepareStatement(DELETECURRENTNODESQL);
      //modify by liuzw 20120410 增加对多财政多年度的支持
      for (int i = 0, n = dtoDetails.size(); i < n; i++) {
        vouId = ((FVoucherDTO) dtoDetails.get(i)).getVou_id();
        ps.setString(1, vouId);
        ps.setLong(2, node_id);
        ps.setString(3, rg_code);
        ps.setInt(4, Integer.parseInt(setYear));
        updates[i] = ps.executeUpdate();
      }
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\nSQL:" + DELETECURRENTNODESQL
        + "\n参数：vou_id:" + vouId + " node_id:" + node_id + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return updates;
  }

  /**
   * 退回时删除当前的及与与同级的分支数据
   * @param dtoDetails 工作流明细
   * @param node_id 当前节点
   * @param pcon
   * @throws SQLException 
   * @author justin(liwq2)
   */
  private int[] deleteCurAndOtherItem(List dtoDetails, long node_id, Connection pcon) throws Exception {
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    int[] updates = new int[dtoDetails.size()];
    String vouId = "";
    //将同级分支数据一并删除
    StringBuffer sb = new StringBuffer();
    sb.append(" delete sys_wf_current_item where entity_id=? and rg_code=? and set_year=? and node_id in (");

    try {
      // 增加对多财政多年度的支持
      int n = dtoDetails.size();
      for (int i = 0; i < n; i++) {
        StringBuffer sbr = new StringBuffer();
        vouId = ((FVoucherDTO) dtoDetails.get(i)).getVou_id();
        String preNodeId = getPreNodeId(node_id, vouId, pcon);
        //同级节点 
        long[] sameLevelNodes = getSameLevelNextNodes(Long.parseLong(preNodeId), pcon);
        sbr.append(sb).append(Tools.arrToSqllong(sameLevelNodes)).append(") ");
        ps = pcon.prepareStatement(sbr.toString());
        ps.setString(1, vouId);
        ps.setString(2, rg_code);
        ps.setInt(3, Integer.parseInt(setYear));
        updates[i] = ps.executeUpdate();
      }
    } catch (SQLException e) {
      StringBuffer sbr = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\nSQL:" + DELETECURRENTNODESQL
        + "\n参数：vou_id:" + vouId + " node_id:" + node_id + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sbr.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
        ps = null;
      }
    }
    return updates;
  }

  /**
   * 修改当前节点
   * @param dtoDetails
   * @param node_id
   * @param pcon
   * @throws SQLException
   */
  private int[] updateCurrentItem(List dtoDetails, long node_id, Connection pcon, String status_code)
    throws SQLException {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    int[] updates = new int[dtoDetails.size()];
    String sql = "update sys_wf_current_item set status_code=? where entity_id=? and node_id=?  and rg_code=? and set_year=?";
    String vouId = "";
    try {
      //modify by liuzw 20120410 增加对多财政多年度的支持
      ps = pcon.prepareStatement(sql);

      for (int i = 0, n = dtoDetails.size(); i < n; i++) {
        vouId = ((FVoucherDTO) dtoDetails.get(i)).getVou_id();
        ps.setString(1, status_code);
        ps.setString(2, vouId);
        ps.setLong(3, node_id);
        ps.setString(4, rg_code);
        ps.setInt(5, Integer.parseInt(setYear));
        updates[i] = ps.executeUpdate();
      }
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\nSQL:" + DELETECURRENTNODESQL
        + "\n参数：status_code:" + status_code + " node_id:" + node_id + " vou_id:" + vouId + " rg_code:" + rg_code
        + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return updates;
  }

  /**
   * 删除当前流程
   * @param dtoDetails
   * @param node_id
   * @param pcon
   * @throws SQLException
   */
  private void deleteCurrentTask(List dtoDetails, long node_id, Connection pcon) throws Exception {
    PreparedStatement ps = null;
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    String sql = "delete sys_wf_current_tasks where (current_node_id=? or next_node_id=?) and entity_id=? and rg_code =? and set_year=?";
    String vouId = "";
    try {
      //modify by liuzw 20120410 增加对多财政多年度的支持
      ps = pcon.prepareStatement(sql);
      for (int i = 0, n = dtoDetails.size(); i < n; i++) {
        vouId = ((FVoucherDTO) dtoDetails.get(i)).getVou_id();
        ps.setLong(1, node_id);
        ps.setLong(2, node_id);
        ps.setString(3, vouId);
        ps.setString(4, rg_code);
        ps.setInt(5, Integer.parseInt(setYear));
        if (ps.executeUpdate() <= 0) {
          StringBuffer sb = new StringBuffer("明细id为" + vouId + "未找到当前流程流转信息\n" + " SQL:" + sql + " 参数：vou_id:" + vouId
            + " node_id:" + node_id + " rg_code:" + rg_code + " set_year:" + setYear);
          log(sb.toString());
          throw new WFErrorNodeException("明细id为" + vouId + "未找到当前流程流转信息");
        }
      }
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\nSQL:" + sql + "\n参数：vou_id:"
        + vouId + " node_id:" + node_id + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * 退回时删除current_task及同级分支的其它数据
   * @param dtoDetails 明细数据
   * @param node_id    当前节点id
   * @param pcon
   * @throws SQLException
   * @author justin(liwq2)
   */
  private void deleteCurrentAndOtherTask(List dtoDetails, long node_id, Connection pcon) throws Exception {
    PreparedStatement ps = null;
    String rg_code = getRgCode();
    String setYear = getSetYear();
    //String sql = "delete sys_wf_current_tasks where (current_node_id=? or next_node_id=?) and entity_id=? and rg_code =? and set_year=?";
    StringBuffer sql = new StringBuffer();
    sql.append(" delete sys_wf_current_tasks where entity_id=? and rg_code =? and set_year=? and ");

    String vouId = "";
    try {
      int n = dtoDetails.size();
      for (int i = 0; i < n; i++) {
        StringBuffer sb = new StringBuffer();
        vouId = ((FVoucherDTO) dtoDetails.get(i)).getVou_id();
        //来源节点
        String preNodeId = getPreNodeId(node_id, vouId, pcon);
        //同级节点 
        long[] sameLevelNodes = getSameLevelNextNodes(Long.parseLong(preNodeId), pcon);
        sb.append(sql).append(" (next_node_id in (").append(Tools.arrToSqllong(sameLevelNodes))
          .append(") or current_node_id in ( ").append(Tools.arrToSqllong(sameLevelNodes)).append(" )) ");
        ps = pcon.prepareStatement(sb.toString());
        ps.setString(1, vouId);
        ps.setString(2, rg_code);
        ps.setInt(3, Integer.parseInt(setYear));
        if (ps.executeUpdate() <= 0) {
          StringBuffer sb2 = new StringBuffer("明细id为" + vouId + "未找到当前流程流转信息\n" + " SQL:" + sql + " 参数：vou_id:" + vouId
            + " node_id:" + node_id + " rg_code:" + rg_code + " set_year:" + setYear);
          log(sb2.toString());
          throw new WFErrorNodeException("明细id为" + vouId + "未找到当前流程流转信息");
        }
      }
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\nSQL:" + sql + "\n参数：vou_id:"
        + vouId + " node_id:" + node_id + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
        ps = null;
      }
    }
  }

  /**
   * 删除当前流程
   * @param dtoDetails
   * @param node_id
   * @param pcon
   * @throws SQLException
   */
  private int[] insertCompleteItem(List dtoDetails, long node_id, Connection pcon, String status_code) throws Exception {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    int[] updates = new int[dtoDetails.size()];
    try {
      ps = pcon
        .prepareStatement("insert into sys_wf_complete_item(entity_id,bill_id,node_id,rcid,ccid,status_code,rg_code,set_year,mb_id,agency_id,pb_id,cb_id,ib_id,gp_agency_id) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (int i = 0, n = dtoDetails.size(); i < n; i++) {
        //冗余机构权限，如果为空，则置为"#",以兼容指标管理系统
        XMLData xmldata = this.getOrgDetail(((FVoucherDTO) dtoDetails.get(i)));
        ps.setString(1, ((FVoucherDTO) dtoDetails.get(i)).getVou_id());
        ps.setString(2, ((FVoucherDTO) dtoDetails.get(i)).getVou_bill_id());
        ps.setLong(3, node_id);
        ps.setString(4, ((FVoucherDTO) dtoDetails.get(i)).getRcid());
        ps.setString(5, ((FVoucherDTO) dtoDetails.get(i)).getCcid());
        ps.setString(6, status_code);
        ps.setString(7, rg_code);
        ps.setInt(8, Integer.parseInt(setYear));
        ps.setString(9, (String) xmldata.get("mb_id"));
        ps.setString(10, (String) xmldata.get("agency_id"));
        ps.setString(11, (String) xmldata.get("pb_id"));
        ps.setString(12, (String) xmldata.get("cb_id"));
        ps.setString(13, (String) xmldata.get("ib_id"));
        ps.setString(14, (String) xmldata.get("gp_agency_id"));
        updates[i] = ps.executeUpdate();
        if (updates[i] <= 0) {
          throw new WFErrorNodeException("明细id为" + ((FVoucherDTO) dtoDetails.get(i)).getVou_id() + "不能保存已完成节点信息");
        }
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return updates;
  }

  /**
   * 获取预处理的当前节点
   * @param pcon
   * @return
   * @throws SQLException
   */
  private PreparedStatement getInsertCurrentItemPsmt(Connection pcon) throws SQLException {
    //modify by liuzw 20120410 增加对多财政多年度的支持
    return pcon
      .prepareStatement("insert into sys_wf_current_item(entity_id,bill_id,node_id,rcid,ccid,status_code,rg_code,set_year,mb_id,agency_id,pb_id,cb_id,ib_id,gp_agency_id) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
  }

  /**
   * 批处理SQL赋值
   * @throws SQLException 
   * 
   * 
   * @throws Exception
   */
  private void setValues4InsertCurrentItemPsmt(PreparedStatement insertCurrentItemPsmt, String entityId, long nodeId,
    String billId, String rcid, String ccid, String status_code, String mb_id, String agency_id, String pb_id,
    String cb_id, String ib_id, String gp_agency_id) throws SQLException {

    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    //冗余机构权限，如果为空，则置为"#",以兼容指标管理系统
    if (mb_id == null || mb_id.equals(""))
      mb_id = "#";
    if (agency_id == null || agency_id.equals(""))
      agency_id = "#";
    if (pb_id == null || pb_id.equals(""))
      pb_id = "#";
    if (cb_id == null || cb_id.equals(""))
      cb_id = "#";
    if (ib_id == null || ib_id.equals(""))
      ib_id = "#";
    if (gp_agency_id == null || gp_agency_id.equals(""))
      gp_agency_id = "#";

    //modify by liuzw 20120327 增加对多财政多年度的支持
    insertCurrentItemPsmt.setString(1, entityId);
    insertCurrentItemPsmt.setString(2, billId);
    insertCurrentItemPsmt.setLong(3, nodeId);
    insertCurrentItemPsmt.setString(4, rcid);
    insertCurrentItemPsmt.setString(5, ccid);
    insertCurrentItemPsmt.setString(6, status_code);
    insertCurrentItemPsmt.setString(7, rg_code);
    insertCurrentItemPsmt.setInt(8, Integer.parseInt(setYear));
    insertCurrentItemPsmt.setString(9, mb_id);
    insertCurrentItemPsmt.setString(10, agency_id);
    insertCurrentItemPsmt.setString(11, pb_id);
    insertCurrentItemPsmt.setString(12, cb_id);
    insertCurrentItemPsmt.setString(13, ib_id);
    insertCurrentItemPsmt.setString(14, gp_agency_id);
    insertCurrentItemPsmt.addBatch();
  }

  /**  删除当前流程流转sql */
  //modify by liuzw 20120410 增加对多财政多年度的支持
  private static String COPYCURRENTTASKSQL = "insert into  sys_wf_complete_tasks select * from sys_wf_current_tasks where (current_node_id=? or next_node_id=?) and entity_id=? and rg_code=? and set_year=? ";

  /**
   * 删除当前流程
   * @param dtoDetails
   * @param node_id
   * @param pcon
  * @throws Exception 
   */
  private int[] copyCurrentTask(List dtoDetails, long node_id, Connection pcon) throws Exception {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    String vouId = "";
    int[] updates = new int[dtoDetails.size()];
    try {
      ps = pcon.prepareStatement(COPYCURRENTTASKSQL);
      for (int i = 0, n = dtoDetails.size(); i < n; i++) {
        vouId = ((FVoucherDTO) dtoDetails.get(i)).getVou_id();
        ps.setLong(1, node_id);
        ps.setLong(2, node_id);
        ps.setString(3, vouId);
        ps.setString(4, rg_code);
        ps.setInt(5, Integer.parseInt(setYear));
        updates[i] = ps.executeUpdate();
        if (updates[i] <= 0) {
          StringBuffer sb = new StringBuffer("明细id为" + vouId + "未能转移当前流程流转信息\n" + "Sql为：" + COPYCURRENTTASKSQL
            + "\n参数为：node_id:" + node_id + " vouId:" + vouId + " rg_code:" + rg_code + " set_year:" + setYear);
          System.out.println(sb.toString());
          log(sb.toString());
          throw new WFErrorNodeException("明细id为" + vouId + "未能转移当前流程流转信息");
        }
      }
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\nSql为：" + COPYCURRENTTASKSQL
        + "\n参数为：node_id:" + node_id + " vouId:" + vouId + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new SQLException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return updates;
  }

  /*  删除已完成节点sql */
  //modify by liuzw 20120410 增加对多财政多年度的支持
  private static String DELETECOMPLETENODESQL = "delete sys_wf_complete_item where "
    + " node_id in (select decode(current_node_id,?,next_node_id,current_node_id) from sys_wf_current_tasks where (current_node_id=? or next_node_id=?) and entity_id=? and rg_code=? and set_year=?) and entity_id=? and rg_code=? and set_year=? ";

  /**
   * 删除完成节点
   * @param dtoDetails
   * @param node_id
   * @param pcon
   * @return
   * @throws Exception 
   */
  private int[] deleteCompleteItem(List dtoDetails, long node_id, Connection pcon) throws Exception {
    PreparedStatement ps = null;
    String rg_code = getRgCode();
    String setYear = getSetYear();
    String vouId = "";
    try {
      ps = pcon.prepareStatement(DELETECOMPLETENODESQL);
      int[] rBatsh = new int[dtoDetails.size()];
      for (int i = 0, n = dtoDetails.size(); i < n; i++) {
        vouId = ((FVoucherDTO) dtoDetails.get(i)).getVou_id();
        ps.setLong(1, node_id);
        ps.setLong(2, node_id);
        ps.setLong(3, node_id);
        ps.setString(4, vouId);
        ps.setString(5, rg_code);
        ps.setInt(6, Integer.parseInt(setYear));
        ps.setString(7, vouId);
        ps.setString(8, rg_code);
        ps.setInt(9, Integer.parseInt(setYear));
        rBatsh[i] = ps.executeUpdate();
      }
      return rBatsh;
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(e.getMessage() + DELETECOMPLETENODESQL + "参数：node_id:" + node_id + " vou_id:"
        + vouId + " set_year:" + setYear + " rg_code:" + rg_code);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  //modify by liuzw 20120410 增加对多财政多年度的支持
  /*  删除要撤销已完成节点sql */
  private static String DELETERECALLCOMPLETENODESQL = "delete sys_wf_complete_item where "
    + " node_id =? and entity_id=?  and rg_code=? and set_year=?";

  /**
   * 删除完成节点
   * @param dtoDetails
   * @param node_id
   * @param pcon
   * @return
   * @throws SQLException
   */
  private int[] deleteRecallCompleteItem(List dtoDetails, long node_id, Connection pcon) throws SQLException {
    PreparedStatement ps = null;
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    String vouId = "";
    try {
      //modify by liuzw 20120410 增加对多财政多年度的支持
      ps = pcon.prepareStatement(DELETERECALLCOMPLETENODESQL);
      int[] rBatsh = new int[dtoDetails.size()];
      for (int i = 0, n = dtoDetails.size(); i < n; i++) {
        vouId = ((FVoucherDTO) dtoDetails.get(i)).getVou_id();
        ps.setLong(1, node_id);
        ps.setString(2, vouId);
        ps.setString(3, rg_code);
        ps.setInt(4, Integer.parseInt(setYear));
        rBatsh[i] = ps.executeUpdate();
      }
      return rBatsh;
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n SQL:"
        + DELETERECALLCOMPLETENODESQL + " 参数：node_id:" + node_id + " vou_id:" + vouId + " rg_code:" + rg_code
        + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  //modify by liuzw 20120410 增加对多财政多年度的支持
  /** 工作流流转 */
  private static String ROUTTASKSQL = " insert into sys_wf_task_routing(TASK_ID,NEXT_TASK_ID,RG_CODE,SET_YEAR)  "
    + " select * from (select  e.task_id from sys_wf_current_tasks e where  (e.next_node_id =? or e.current_node_id=? )and e.entity_id=? and e.rg_code=? and e.set_year=?) "
    + ",( select  f.task_id as NEXT_TASK_ID,f.rg_code,f.set_year from sys_wf_cache_tasks f where f.entity_id=? and f.rg_code=? and f.set_year=?)";

  /**
   * 工作流流转
   * @param dtoDetails
   * @param node_id
   * @param pcon
   * @return
   * @throws SQLException
   */
  private int[] insertRoute(List dtoDetails, long node_id, Connection pcon) throws SQLException {
    PreparedStatement ps = null;
    String rg_code = getRgCode();
    String setYear = getSetYear();
    String vouId = "";
    try {
      //增加对多财政多年度的支持
      ps = pcon.prepareStatement(ROUTTASKSQL);
      for (int i = 0, n = dtoDetails.size(); i < n; i++) {
        vouId = ((FVoucherDTO) dtoDetails.get(i)).getVou_id();
        ps.setLong(1, node_id);
        ps.setLong(2, node_id);
        ps.setString(3, vouId);
        ps.setString(4, rg_code);
        ps.setInt(5, Integer.parseInt(setYear));
        ps.setString(6, vouId);
        ps.setString(7, rg_code);
        ps.setInt(8, Integer.parseInt(setYear));
        ps.addBatch();
      }
      return ps.executeBatch();
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n SQL:" + ROUTTASKSQL
        + " 参数：current_node_id:" + node_id + " next_node_id:" + node_id + " entity_id:" + vouId + " rg_code:" + rg_code
        + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * 根据当前节点查询一下节点流转线
   */
  private static String NEXTNODEIDCONDITIONSQL = " select distinct t.wf_id,t.node_id,"
    + "(select "
    + (TypeOfDB.isOracle() ? " decode(node_type,'003',0,t.next_node_id) "
      : " case node_type when '003' then 0 else t.next_node_id end ")
    + " from sys_wf_nodes s where s.node_id=t.next_node_id and s.rg_code=? and s.set_year=?) as next_node_id,t.condition_id "
    + ",t.next_node_id as new_node_id,t.line_rule_id from sys_wf_node_conditions t where   t.node_id=? and t.routing_type=? and t.rg_code=? and t.set_year=?";

  /**
   * 根据当前节点获取下一节点工作流流转条件
   * @param current_node_id
   * @param pcon
   * @return
   * @throws SQLException 
   */
  private List getNextConditionId(String current_node_id, Connection pcon) throws SQLException {
    String rg_code = getRgCode();
    String setYear = getSetYear();
    //如果下一节点是结束节点 next_node_id=0
    String key = rg_code + setYear + "-" + current_node_id;
    List list = (List) CONDITIONS.get(key);
    PreparedStatement ps = null;
    if (list != null && list.size() > 0)
      return (List) CONDITIONS.get(key);
    try {
      ps = pcon.prepareStatement(NEXTNODEIDCONDITIONSQL);
      ps.setString(1, rg_code);
      ps.setInt(2, Integer.parseInt(setYear));
      ps.setLong(3, Long.parseLong(current_node_id));
      ps.setString(4, "001");
      ps.setString(5, rg_code);
      ps.setInt(6, Integer.parseInt(setYear));

      ResultSet rs = ps.executeQuery();
      list = new ArrayList();
      while (rs.next()) {
        Object[] wf = new Object[5];
        wf[0] = new Long(rs.getLong("wf_id"));
        wf[1] = new Long(rs.getLong("next_node_id"));
        wf[2] = rs.getString("condition_id");
        wf[3] = new Long(rs.getLong("new_node_id"));
        wf[4] = rs.getString("line_rule_id");
        list.add(wf);
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\nsql为："
        + NEXTNODEIDCONDITIONSQL + "\n参数为：current_node_id:" + current_node_id + " routing_type:001" + " rg_code"
        + rg_code + " set_year" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }
    CONDITIONS.put(key, list);
    return list;
  }

  /**
   * 退回流转线
   */
  private static String BACKNODEIDCONDITIONSQL = " select distinct t.wf_id,t.next_node_id,t.condition_id "
    + " from sys_wf_node_conditions t where   t.node_id=? and t.routing_type='002' and t.rg_code=? and t.set_year=?";

  /**
   * 没有退回流转线，将正常的流转线反向做成退回流转线
   */
  private static String DEFAULTBACKNODEIDCONDITIONSQL = " select distinct t.wf_id,t.node_id as next_node_id,t.condition_id "
    + " from sys_wf_node_conditions t where   t.next_node_id=? and t.routing_type='001' and t.rg_code=? and t.set_year=?";

  /**
   * 获取退回工作流转条件
   * @param current_node_id
   * @param pcon
   * @return
   * @throws SQLException 
   */
  private List getBackConditionId(String current_node_id, Connection pcon) throws SQLException {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    String key = rg_code + setYear + "back-" + current_node_id;
    List list = (List) CONDITIONS.get(key);
    if (null != list && list.size() > 0) {
      return list;
    }
    String sql = "";
    PreparedStatement ps = null;
    try {
      //modify by liuzw 20120410 增加对多财政多年度的支持
      sql = BACKNODEIDCONDITIONSQL;
      ps = pcon.prepareStatement(sql);
      ps.setLong(1, Long.parseLong(current_node_id));
      ps.setString(2, rg_code);
      ps.setInt(3, Integer.parseInt(setYear));

      ResultSet rs = ps.executeQuery();
      list = new ArrayList();
      while (rs.next()) {
        Object[] wf = new Object[3];

        wf[0] = new Long(rs.getLong("wf_id"));
        wf[1] = new Long(rs.getLong("next_node_id"));
        wf[2] = rs.getString("condition_id");
        list.add(wf);
      }
      rs.close();
      ps.close();
      if (list.size() == 0)//没有退回的线直接将原流转线反向
      {
        sql = DEFAULTBACKNODEIDCONDITIONSQL;
        ps = pcon.prepareStatement(sql);
        ps.setLong(1, Long.parseLong(current_node_id));
        ps.setString(2, rg_code);
        ps.setInt(3, Integer.parseInt(setYear));

        rs = ps.executeQuery();
        while (rs.next()) {
          Object[] wf = new Object[3];

          wf[0] = new Long(rs.getLong("wf_id"));
          wf[1] = new Long(rs.getLong("next_node_id"));
          wf[2] = rs.getString("condition_id");
          list.add(wf);
        }
        rs.close();
        ps.close();
      }
      CONDITIONS.put(key, list);
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\nSQL:" + sql
        + "\n参数：current_node_id:" + current_node_id + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }
    return list;
  }

  /**
   * 获取录入工作流转条件
   * @param current_node_id
   * @param pcon
   * @return
   * @throws SQLException 
   */
  private List getInputConditionId(String menu_id, String role_id, String tableName, Connection pcon)
    throws SQLException {
    String rg_code = getRgCode();
    String setYear = getSetYear();
    String key = SessionUtil.getLoginYear() + "-" + tableName + "-" + menu_id + "-" + role_id + "-" + rg_code;
    List list = (List) CONDITIONS.get(key);
    if (list != null && list.size() > 0)
      return list;
    StringBuffer sbSql = new StringBuffer();
    //增加对多财政多年度的支持
    sbSql
      .append("select distinct t.wf_id,t.node_id,t.next_node_id,t.condition_id,t.line_rule_id ")
      .append(" from sys_wf_node_conditions t where   (node_id,routing_type)  in ")
      .append(
        "( select  node_id,node_type routing_type from sys_wf_nodes s where s.node_type='001' and  s.WF_TABLE_NAME=? and s.rg_code=? and s.set_year=?) ")
      .append(
        " and exists (select 1 from sys_wf_module_node s where s.node_id=t.next_node_id and s.module_id=? and s.rg_code=? and s.set_year=?)")
      .append(
        " and exists (select 1 from sys_wf_role_node s where s.node_id=t.next_node_id and s.role_id=? and s.rg_code=? and s.set_year=?) and t.rg_code=? and t.set_year=?");
    ResultSet rs = null;
    PreparedStatement ps = null;
    try {
      ps = pcon.prepareStatement(sbSql.toString());
      ps.setString(1, tableName);
      ps.setString(2, rg_code);
      ps.setInt(3, Integer.parseInt(setYear));
      ps.setString(4, menu_id);
      ps.setString(5, rg_code);
      ps.setInt(6, Integer.parseInt(setYear));
      ps.setString(7, role_id);
      ps.setString(8, rg_code);
      ps.setInt(9, Integer.parseInt(setYear));
      ps.setString(10, rg_code);
      ps.setInt(11, Integer.parseInt(setYear));

      rs = ps.executeQuery();
      list = new ArrayList();
      while (rs.next()) {
        Object[] wf = new Object[5];
        wf[0] = new Long(rs.getLong("wf_id"));
        wf[1] = new Long(rs.getLong("next_node_id"));
        wf[2] = rs.getString("condition_id");
        wf[3] = new Long(rs.getLong("node_id"));
        wf[4] = rs.getString("line_rule_id");
        list.add(wf);
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n  sql为：" + sbSql.toString()
        + "\n参数为：tableName:" + tableName + " menuid:" + menu_id + " role_id:" + role_id + " rg_code" + rg_code
        + " set_year" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }
    CONDITIONS.put(key, list);
    return list;
  }

  /**
   * 插入流程加速表
   */
  private static String insertCurrentCacheTaskSql = new StringBuffer()
    .append("insert into sys_wf_cache_tasks ")
    .append(" (TASK_ID,UPDATE_FLAG,CREATE_DATE,IS_UNDO,WF_ID,WF_TABLE_NAME,ENTITY_ID,")
    .append("CURRENT_NODE_ID,NEXT_NODE_ID,ACTION_TYPE_CODE,CREATE_USER,")
    .append("OPERATION_NAME,OPERATION_DATE,OPERATION_REMARK,RESULT_MONEY,")
    .append("TOLLY_FLAG,BILL_TYPE_CODE,RCID,CCID,SET_MONTH,CREATE_USER_ID,BILL_ID,RG_CODE,SET_YEAR) ")
    .append(
      "values( " + (TypeOfDB.isOracle() ? " SEQ_SYS_WF_TASK_ID.Nextval " : " Nextval('SEQ_SYS_WF_TASK_ID') ")
        + ",1,to_char(sysdate,'YYYY-MM-DD HH24:MI:SS'),0 ,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)").toString();

  /**
   * 获取预处理的当前任务
   * @param pcon
   * @return
   * @throws SQLException
   */
  private PreparedStatement getInsertCurrentTaskCachePsmt(Connection pcon) throws SQLException {
    PreparedStatement ps = pcon.prepareStatement("delete sys_wf_cache_tasks");
    ps.execute();
    ps.close();
    return pcon.prepareStatement(insertCurrentCacheTaskSql);
  }

  /**
   * 修改流程时加速表
   */
  private static String copyCurrentCacheTaskSql = new StringBuffer()
    .append("insert into sys_wf_cache_tasks ")
    .append(" (TASK_ID,UPDATE_FLAG,CREATE_DATE,IS_UNDO,WF_ID,WF_TABLE_NAME,ENTITY_ID,BILL_ID,")
    .append("CURRENT_NODE_ID,NEXT_NODE_ID,ACTION_TYPE_CODE,CREATE_USER,")
    .append("OPERATION_NAME,OPERATION_DATE,OPERATION_REMARK,RESULT_MONEY,")
    .append("TOLLY_FLAG,BILL_TYPE_CODE,RCID,CCID,SET_MONTH,CREATE_USER_ID,RG_CODE,SET_YEAR) ")
    .append(
      "select "
        + (TypeOfDB.isOracle() ? " SEQ_SYS_WF_TASK_ID.Nextval,1,to_char(sysdate,'YYYY-MM-DD HH24:MI:SS')"
          : " Nextval('SEQ_SYS_WF_TASK_ID'), 1, date_format(sysdate(),'%Y-%m-%d %H:%i:%s') ")
        + ",0 ,WF_ID,WF_TABLE_NAME,ENTITY_ID,BILL_ID,")
    .append("?,0,?,?,?,?,?,?,?,?,?,?,?,?,?,? ")
    .append(
      " from sys_wf_current_tasks where  (current_node_id=? or next_node_id=?) and entity_id=? and rg_code=? and set_year=? ")
    .toString();

  /**
   * 获取预处理的当前任务
   * @param pcon
   * @return
   * @throws SQLException
   */
  private PreparedStatement getCopyCurrentTaskCachePsmt(Connection pcon) throws SQLException {
    PreparedStatement ps = pcon.prepareStatement("delete sys_wf_cache_tasks");
    ps.execute();
    ps.close();
    return pcon.prepareStatement(copyCurrentCacheTaskSql);
  }

  /**
   * 批处理SQL赋值
   * @throws SQLException 
   * 
   * 
   * @throws Exception
   */
  private void setValues4CopyCurrentTaskCachePsmt(PreparedStatement copyCurrentTaskPsmt, String entityId,
    long currentNodeId, String actionType, String createUser, String operationName, String operationDate,
    String operationRemark, String resultMoney, int tollyFlag, String billTypeCode, String rcid, String ccid,
    int setMonth, String createUserId) throws FAppException, SQLException {
    String rg_code = getRgCode();
    String setYear = getSetYear();
    copyCurrentTaskPsmt.setLong(1, currentNodeId);
    copyCurrentTaskPsmt.setString(2, actionType);
    copyCurrentTaskPsmt.setString(3, createUser);
    copyCurrentTaskPsmt.setString(4, operationName);
    copyCurrentTaskPsmt.setString(5, operationDate);
    copyCurrentTaskPsmt.setString(6, operationRemark);
    copyCurrentTaskPsmt.setString(7, resultMoney);
    copyCurrentTaskPsmt.setInt(8, tollyFlag);
    copyCurrentTaskPsmt.setString(9, billTypeCode);
    copyCurrentTaskPsmt.setString(10, rcid);
    copyCurrentTaskPsmt.setString(11, ccid);
    copyCurrentTaskPsmt.setInt(12, setMonth);
    copyCurrentTaskPsmt.setString(13, createUserId);
    copyCurrentTaskPsmt.setString(14, rg_code);
    copyCurrentTaskPsmt.setInt(15, Integer.parseInt(setYear));
    copyCurrentTaskPsmt.setLong(16, currentNodeId);
    copyCurrentTaskPsmt.setLong(17, currentNodeId);
    copyCurrentTaskPsmt.setString(18, entityId);
    copyCurrentTaskPsmt.setString(19, rg_code);
    copyCurrentTaskPsmt.setInt(20, Integer.parseInt(setYear));
    copyCurrentTaskPsmt.addBatch();
  }

  /**
   * 插入流程加速表
   */
  private static String insertCurrentTaskSql = new StringBuffer()
    .append("insert into sys_wf_current_tasks ")
    .append(" (TASK_ID,UPDATE_FLAG,CREATE_DATE,IS_UNDO,WF_ID,WF_TABLE_NAME,ENTITY_ID,")
    .append("CURRENT_NODE_ID,NEXT_NODE_ID,ACTION_TYPE_CODE,CREATE_USER,")
    .append("OPERATION_NAME,OPERATION_DATE,OPERATION_REMARK,RESULT_MONEY,")
    .append("TOLLY_FLAG,BILL_TYPE_CODE,RCID,CCID,SET_MONTH,CREATE_USER_ID,BILL_ID,RG_CODE,SET_YEAR) ")
    .append(
      "values( "
        + (TypeOfDB.isOracle() ? " SEQ_SYS_WF_TASK_ID.Nextval,1,to_char(sysdate,'YYYY-MM-DD HH24:MI:SS')"
          : " Nextval('SEQ_SYS_WF_TASK_ID'), 1, date_format(sysdate(),'%Y-%m-%d %H:%i:%s') ")
        + ",0 ,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)").toString();

  /**
   * 获取预处理的当前任务
   * @param pcon
   * @return
   * @throws SQLException
   */
  private PreparedStatement getInsertCurrentTaskPsmt(Connection pcon) throws SQLException {
    return pcon.prepareStatement(insertCurrentTaskSql);
  }

  /**
   * 批处理SQL赋值
   * @throws SQLException 
   * @throws Exception
   */
  private void setValues4InsertCurrentTaskCachePsmt(PreparedStatement insertCurrentTaskPsmt, long wfId,
    String wfTableName, String entityId, long currentNodeId, long nextNodeId, String actionType, String createUser,
    String operationName, String operationDate, String operationRemark, String resultMoney, int tollyFlag,
    String billTypeCode, String rcid, String ccid, int setMonth, String createUserId, String billId)
    throws FAppException, SQLException {
    String rg_code = getRgCode();
    String setYear = getSetYear();
    insertCurrentTaskPsmt.setLong(1, wfId);
    insertCurrentTaskPsmt.setString(2, wfTableName);
    insertCurrentTaskPsmt.setString(3, entityId);
    insertCurrentTaskPsmt.setLong(4, currentNodeId);
    insertCurrentTaskPsmt.setLong(5, nextNodeId);
    insertCurrentTaskPsmt.setString(6, actionType);
    insertCurrentTaskPsmt.setString(7, createUser);
    insertCurrentTaskPsmt.setString(8, operationName);
    insertCurrentTaskPsmt.setString(9, operationDate);
    insertCurrentTaskPsmt.setString(10, operationRemark);
    insertCurrentTaskPsmt.setString(11, resultMoney);
    insertCurrentTaskPsmt.setInt(12, tollyFlag);
    insertCurrentTaskPsmt.setString(13, billTypeCode);
    insertCurrentTaskPsmt.setString(14, rcid);
    insertCurrentTaskPsmt.setString(15, ccid);
    insertCurrentTaskPsmt.setInt(16, setMonth);
    insertCurrentTaskPsmt.setString(17, createUserId);
    insertCurrentTaskPsmt.setString(18, billId);
    insertCurrentTaskPsmt.setString(19, rg_code);
    insertCurrentTaskPsmt.setInt(20, Integer.parseInt(setYear));
    insertCurrentTaskPsmt.addBatch();
  }

  //增加对多财政多年度的支持
  /** 从cache表copy到结束表 */
  private static String COPYCACHE2ENDSQL = " insert into sys_wf_end_tasks select t.* from sys_wf_cache_tasks t  "
    + "where exists (select 1 from  sys_wf_nodes w  where w.node_id =t.next_node_id  and w.node_type = '003') and t.rg_code=? and t.set_year=?";

  /** 删除结束cache流程 */
  private static String DELCACHE2ENDSQL = " delete sys_wf_cache_tasks  t  "
    + "where exists (select 1 from  sys_wf_nodes w  where w.node_id =t.next_node_id  and w.node_type = '003') and t.rg_code=? and t.set_year=?";

  /** 删除结束流程节点 */
  private static String DELENDCURRENTITEMSQL = " delete sys_wf_current_item  t  "
    + "where exists (select 1 from  sys_wf_nodes w  where w.node_id =t.node_id  and w.node_type = '003' and w.wf_id=?) and t.rg_code=? and t.set_year=?";

  /** 删除结束流程节点  无流程ID */
  private static String DELENDCURRENTITEMSQL_NOWF_ID = " delete sys_wf_current_item  t  "
    + "where exists (select 1 from  sys_wf_nodes w  where w.node_id =t.node_id  and w.node_type = '003' ) and t.rg_code=? and t.set_year=?";

  /** 从cache表copy到当前表 */
  private static String COPYCACHE2CURRENTSQL = " insert into sys_wf_current_tasks select t.* from sys_wf_cache_tasks t where t.rg_code=? and t.set_year=? ";

  /**
   * 将cache表数据转移到正式表
   * @param pcon
   * @param wf_id
   * @return
   * @throws Exception 
   */
  private int copyCacheTasks(Connection pcon, long wf_id) throws Exception {
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    int cache = 0;
    String sql = "";
    try {
      sql = COPYCACHE2ENDSQL;
      ps = pcon.prepareStatement(COPYCACHE2ENDSQL);
      ps.setString(1, rg_code);
      ps.setInt(2, Integer.parseInt(setYear));
      cache = ps.executeUpdate();
      ps.close();
      sql = DELCACHE2ENDSQL;
      ps = pcon.prepareStatement(DELCACHE2ENDSQL);
      ps.setString(1, rg_code);
      ps.setInt(2, Integer.parseInt(setYear));
      cache = ps.executeUpdate();
      ps.close();
      sql = DELENDCURRENTITEMSQL;
      ps = pcon.prepareStatement(DELENDCURRENTITEMSQL);
      ps.setLong(1, wf_id);
      ps.setString(2, rg_code);
      ps.setInt(3, Integer.parseInt(setYear));
      cache = ps.executeUpdate();
      ps.close();
      sql = COPYCACHE2CURRENTSQL;
      ps = pcon.prepareStatement(COPYCACHE2CURRENTSQL);
      ps.setString(1, rg_code);
      ps.setInt(2, Integer.parseInt(setYear));
      cache = ps.executeUpdate();
      ps.close();
      ps = null;
    } catch (SQLException e) {
      log(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\nsql为：" + sql + "\n参数为：rg_code:" + rg_code + " set_year:"
        + setYear);
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return cache;
  }

  /**
   * 将cache表数据转移到正式表
   * @param pcon
   * @return
   * @throws Exception 
   */
  private int copyCacheTasks(Connection pcon) throws Exception {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    int cache = 0;
    String sql = "";
    try {
      sql = COPYCACHE2ENDSQL;
      ps = pcon.prepareStatement(COPYCACHE2ENDSQL);
      ps.setString(1, rg_code);
      ps.setInt(2, Integer.parseInt(setYear));
      cache = ps.executeUpdate();
      ps.close();
      sql = DELCACHE2ENDSQL;
      ps = pcon.prepareStatement(DELCACHE2ENDSQL);
      ps.setString(1, rg_code);
      ps.setInt(2, Integer.parseInt(setYear));
      cache = ps.executeUpdate();
      ps.close();
      sql = DELENDCURRENTITEMSQL;
      ps = pcon.prepareStatement(DELENDCURRENTITEMSQL_NOWF_ID);
      ps.setString(1, rg_code);
      ps.setInt(2, Integer.parseInt(setYear));
      cache = ps.executeUpdate();
      ps.close();
      sql = COPYCACHE2CURRENTSQL;
      ps = pcon.prepareStatement(COPYCACHE2CURRENTSQL);
      ps.setString(1, rg_code);
      ps.setInt(2, Integer.parseInt(setYear));
      cache = ps.executeUpdate();
      ps.close();
      ps = null;
    } catch (SQLException e) {
      log(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\nsql为：" + sql + "\n参数为：rg_code:" + rg_code + " set_year:"
        + setYear);
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return cache;
  }

  /**
   * 获取关联单表的id值
   * @param dto
   * @param node_id
   * @param pcon
   * @return
   * @throws SQLException
  * @throws IllegalAccessException 
   */
  private String getNextItemBillId(FVoucherDTO dto, Long node_id, Connection pcon) throws Exception {
    //modify by zhangjunyang on 2011-06-03
    //先以传进来的单号为准，没传的时候才去找关联
    if (dto.getVou_bill_id() != null && !dto.getVou_bill_id().equals("")) {
      return dto.getVou_bill_id();
    }
    String billIdField = getBillIdField(node_id, pcon);
    if (billIdField.endsWith("id")) {
      try {
        dto.setVou_bill_id(BeanUtils.getProperty(dto, billIdField));
        return dto.getVou_bill_id();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        throw e;
      } catch (InvocationTargetException e) {
        e.printStackTrace();
        throw e;
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
        throw e;
      }
    }
    return null;
  }

  /**
   * 根据节点查找对应 关联的单表字段名
   * @param node_id
   * @param pcon
   * @return
   * @throws SQLException
   */
  private String getBillIdField(Long node_id, Connection pcon) throws SQLException {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    String key = rg_code + setYear + node_id;
    String field = null;
    if (ITEMBILLFIELDS.get(key) != null) {
      return (String) ITEMBILLFIELDS.get(key);
    }
    PreparedStatement ps = null;
    ResultSet rs = null;
    String sql = "select relation_column_name from sys_wf_nodes where node_id=? and rg_code=? and set_year=?";
    try {
      ps = pcon.prepareStatement(sql);
      ps.setLong(1, node_id.longValue());
      ps.setString(2, rg_code);
      ps.setInt(3, Integer.parseInt(setYear));
      rs = ps.executeQuery();
      if (rs.next()) {
        field = rs.getString(1);
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\nsql为：" + sql
        + "\n参数为：node_id:" + node_id + " rg_code" + rg_code + " set_year" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }
    if (field == null) {
      field = "";
    } else {
      field = field.toLowerCase();
    }

    ITEMBILLFIELDS.put(key, field);
    return field;

  }

  /**
   * 获取记账标志
   * @param node_id
   * @param actionType
   * @param pcon
   * @return
   * @throws SQLException
   */
  private int getTollyFlag(long node_id, String actionType, Connection pcon) throws SQLException {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    String sql = "select tolly_flag from sys_wf_node_tolly_action_type where node_id=? and action_type_code=? and rg_code=? and set_year=?";
    //modify by liuzw 20120410 增加对多财政多年度的支持  
    int tollyFlag = -1;
    try {
      ps = pcon.prepareStatement(sql);
      ps.setLong(1, node_id);
      ps.setString(2, actionType);
      ps.setString(3, rg_code);
      ps.setInt(4, Integer.parseInt(setYear));
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        tollyFlag = rs.getInt(1);
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\nsql为：" + sql
        + "\n参数为：node_id:" + node_id + " action_type" + actionType + " rg_code" + rg_code + " set_year" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }
    return tollyFlag;
  }

  /**
   * 取节点同步信息，判断是否同步节点
   * @param node_id
   * @param pcon
   * @return
   * @throws SQLException
   */
  private int getGatherFlag(Long node_id, Connection pcon) throws SQLException {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    String key = rg_code + setYear + node_id;
    if (GATHERFLAG.get(key) != null) {
      return ((Integer) GATHERFLAG.get(key)).intValue();
    }
    //modify by liuzw 20120410 增加对多财政多年度的支持
    PreparedStatement ps = null;
    ps = pcon.prepareStatement("select gather_flag from sys_wf_nodes where node_id=? and rg_code=? and set_year=?");
    ps.setLong(1, node_id.longValue());
    ps.setString(2, rg_code);
    ps.setInt(3, Integer.parseInt(setYear));

    int gatherFlag = 1;
    ResultSet rs = ps.executeQuery();
    if (rs.next()) {
      gatherFlag = rs.getInt(1);
    }
    rs.close();
    ps.close();
    GATHERFLAG.put(key, new Integer(gatherFlag));
    return gatherFlag;
  }

  /**
   * 节点同步操作时，查询节点的所有前面节点
   * @param wf_id
   * @param nCurrentNodeId
   * @param pcon
   * @return
   * @throws SQLException
   */
  private long[] getPreNodeIds(long wf_id, long nCurrentNodeId, Connection pcon) throws SQLException {
    String key = nCurrentNodeId + "-" + SessionUtil.getLoginYear() + "-" + SessionUtil.getRgCode();
    if (PRENODES.get(key) != null) {
      return (long[]) PRENODES.get(key);
    }
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();

    PreparedStatement ps = null;
    //modify by liuzw 20120410 增加对多财政多年度的支持
    String sql = "select node_id,next_node_id from sys_wf_node_conditions where routing_type='001' and wf_id=? and rg_code=? and set_year=?";
    ArrayList list = new ArrayList();
    long[] node;
    try {
      ps = pcon.prepareStatement(sql);
      ps.setLong(1, wf_id);
      ps.setString(2, rg_code);
      ps.setInt(3, Integer.parseInt(setYear));

      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        node = new long[2];
        node[0] = rs.getLong(1);
        node[1] = rs.getLong(2);
        list.add(node);
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n错误sql为：" + sql + "参数为： wf_id:"
        + wf_id + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }
    long[] preNodes = new long[list.size() + 1];
    preNodes[0] = nCurrentNodeId;
    int preSize = 1;
    int increase = 0;
    do {
      increase = 0;
      for (int i = 0; i < list.size(); i++) {
        node = (long[]) list.get(i);

        for (int j = 0; j < preSize; j++) {
          if (node[1] == preNodes[j])//后一个节点在列表中 退出循环 随后 将前一个节点判断是否在节点列表中，没有需要将上一个放入列表 
          {
            int k = 0;
            for (k = 0; k < preSize; k++) {
              if (node[0] == preNodes[k]) {
                break;
              }
            }
            if (k == preSize)//前一个节点不在节点列表中，上一个放入列表 
            {
              increase++;
              preNodes[preSize] = node[0];
              preSize++;
            }
            break;
          }
        }
      }
    } while (increase > 0);//循环时还找到上一个节点 继续找
    long[] rPreNodeIds = new long[preSize - 1];//第一个是自身

    System.arraycopy(preNodes, 1, rPreNodeIds, 0, preSize - 1);

    PRENODES.put(key, rPreNodeIds);
    return rPreNodeIds;
  }

  /**
   * 来根据当前节点查询来源节点,取最近一次的操作
   * @param cuuent_node_id 当前节点
   * @param entity_id 明细id
   * @param pcon 
   * @return 
   * @throws SQLException
   * @author justin(liwq2)
   */
  private String getPreNodeId(long cuurent_node_id, String entity_id, Connection pcon) throws SQLException {
    StringBuffer sb = new StringBuffer();
    sb.append("select sw.task_id,sw.current_node_id from sys_wf_current_tasks sw  where ");
    sb.append(" sw.entity_id=? and sw.next_node_id = ?  and sw.rg_code = ? and sw.set_year = ? and sw.action_type_code = 'NEXT'");
    sb.append(" union ");
    sb.append("select sw.task_id,sw.current_node_id from sys_wf_complete_tasks sw  where ");
    sb.append(" sw.entity_id=? and sw.next_node_id = ?  and sw.rg_code = ? and sw.set_year = ? and sw.action_type_code = 'NEXT'");
    sb.append(" order by task_id desc ");
    PreparedStatement ps = null;
    ResultSet rs = null;
    String sourceNodeid = null;
    String rg_code = getRgCode();
    String setYear = getSetYear();
    try {
      ps = pcon.prepareStatement(sb.toString());
      ps.setString(1, entity_id);
      ps.setLong(2, cuurent_node_id);
      ps.setString(3, rg_code);
      ps.setInt(4, Integer.parseInt(setYear));
      ps.setString(5, entity_id);
      ps.setLong(6, cuurent_node_id);
      ps.setString(7, rg_code);
      ps.setInt(8, Integer.parseInt(setYear));
      rs = ps.executeQuery();
      while (rs.next()) {
        sourceNodeid = rs.getString(2);
        break;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.NOT_FOUND_CURRENT_NODE);
    } finally {
      if (ps != null) {
        ps.close();
        ps = null;
      }
      if (rs != null) {
        rs.close();
        rs = null;
      }
    }
    return sourceNodeid;
  }

  /**
   * 是否存在还没有到达同步节点的流程数据，用于同步节点的审核，所有分支到同步节点后数据可见
   * @param entity_id
   * @param wf_id
   * @param pcurrent_node_id
   * @param pnext_node_id
   * @param pcon
   * @return
   * @throws SQLException
   */
  private boolean existPreTask(String entity_id, long wf_id, long pcurrent_node_id, long pnext_node_id, Connection pcon)
    throws SQLException {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    String sql = "select "
      + (TypeOfDB.isOracle() ? "decode(next_node_id,0,current_node_id,next_node_id)"
        : " case next_node_id when 0 then current_node_id else next_node_id end ")
      + " as next_node_id from sys_wf_current_tasks where next_node_id<>? and next_node_id<>? and entity_id=? and rg_code=? and set_year=?";
    try {
      // by ydz 由于当前传入pcurrent_node_id 取值为sys_wf_current_item 的node_id, 此处应为sys_wf_current_tasks 表的next_node_id. 这里需要排除当前分支数据和已走到此同步节点数据。
      //modify by liuzw 20120410 增加对多财政多年度的支持
      ps = pcon.prepareStatement(sql);

      ps.setLong(1, pcurrent_node_id);
      ps.setLong(2, pnext_node_id);
      ps.setString(3, entity_id);
      ps.setString(4, rg_code);
      ps.setInt(5, Integer.parseInt(setYear));
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        long nextNodeId = rs.getLong(1);
        long[] preNode = getPreNodeIds(wf_id, pnext_node_id, pcon);
        for (int i = 0; i < preNode.length; i++) {
          if (nextNodeId == preNode[i])//当前工作流存在前置节点的工作流程
          {
            return true;
          }
        }
      }
      rs.close();
      ps.close();
      ps = null;
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n错误sql为：" + sql
        + "参数为：pcurrent_node_id:" + pcurrent_node_id + " pnext_node_id:" + pnext_node_id + " entity_id" + entity_id
        + " rg_code" + rg_code + " set_year" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }
    return false;
  }

  /**
   * 是否存在还没有到达操作节点的流程数据，用于退回到录入，多分枝情况下不能退回到录入，即使需要也是多个流程撤销后同步到一个节点后退回
   * @param entity_id
   * @param wf_id
   * @param pcurrent_node_id
   * @param pnext_node_id
   * @param pcon
   * @return
  * @throws Exception 
   */
  private boolean existCurrentTask(String entity_id, String wf_table_name, long pcurrent_node_id, Connection pcon)
    throws Exception {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    String sql = "select 1 from sys_wf_current_tasks where current_node_id<>? and next_node_id<>? and wf_table_name=? and entity_id=? union all select 1 from sys_wf_end_tasks where wf_table_name =? and entity_id=? and rg_code=? and set_year=?";
    try {
      ps = pcon.prepareStatement(sql);
      ps.setLong(1, pcurrent_node_id);
      ps.setLong(2, pcurrent_node_id);
      ps.setString(3, wf_table_name);
      ps.setString(4, entity_id);
      ps.setString(5, wf_table_name);
      ps.setString(6, entity_id);
      ps.setString(7, rg_code);
      ps.setInt(8, Integer.parseInt(setYear));

      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
      rs.close();
      ps.close();
      ps = null;
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n错误sql为：" + sql
        + "参数为：pcurrent_node_id:" + pcurrent_node_id + " wf_table_name:" + wf_table_name + " entity_id" + entity_id
        + " rg_code" + rg_code + " set_year" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }
    return false;
  }

  /**
   * 获取录入节点,退回到录入节点使用
   * @param entity_id
   * @param wf_table_name
   * @param pcon
   * @return
   * @throws SQLException
   */
  private long getBackInputNextId(String entity_id, String wf_table_name, Connection pcon) throws Exception {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    long nextNodeId = 0;
    String sql = "select next_node_id from sys_wf_complete_tasks where action_type_code='INPUT' and wf_table_name=? and entity_id=? and rg_code=? and set_year=?";
    try {
      ps = pcon.prepareStatement(sql);
      ps.setString(1, wf_table_name);
      ps.setString(2, entity_id);
      ps.setString(3, rg_code);
      ps.setInt(4, Integer.parseInt(setYear));
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        nextNodeId = rs.getLong(1);
      } else {
        StringBuffer sb = new StringBuffer("entity_id:" + entity_id + WorkFlowExceptionConstants.NOT_FOUND_HIS_NODE
          + "\nsql为：" + sql + "参数为： wf_table_name:" + wf_table_name + " entity_id" + entity_id + " rg_code" + rg_code
          + " set_year" + setYear);
        log(sb.toString());
        throw new WFErrorNodeException(WorkFlowExceptionConstants.NOT_FOUND_HIS_NODE);
      }
      rs.close();
      ps.close();
      ps = null;
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n错误sql为：" + sql
        + "参数为： wf_table_name:" + wf_table_name + " entity_id" + entity_id + " rg_code" + rg_code + " set_year"
        + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }
    return nextNodeId;
  }

  /**
   * 是否存在还没有到达同步节点后续的流程数据，同步节点需要所有分支到到一个节点才能显示数据，保存到current_item
   * @param entity_id
   * @param wf_id
   * @param pcurrent_node_id
   * @param pnext_node_id
   * @param pcon
   * @return
   * @throws SQLException
   */
  private boolean existNextTask(String entity_id, long wf_id, long pcurrent_node_id, long pnext_node_id, Connection pcon)
    throws SQLException {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    String sql = "";
    try {
      //modify by liuzw 20120410 增加对多财政多年度的支持
      sql = "select "
        + (TypeOfDB.isOracle() ? " decode(next_node_id,0,current_node_id,next_node_id)"
          : " case next_node_id when 0 then current_node_id else next_node_id end ")
        + " as next_node_id from sys_wf_current_tasks where current_node_id<>? and next_node_id<>? and entity_id=? and rg_code=? and set_year=?";
      ps = pcon.prepareStatement(sql);

      ps.setLong(1, pcurrent_node_id);
      ps.setLong(2, pnext_node_id);
      ps.setString(3, entity_id);
      ps.setString(4, rg_code);
      ps.setInt(5, Integer.parseInt(setYear));
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        long nextNodeId = rs.getLong(1);
        long[] preNode = getNextNodeIds(wf_id, nextNodeId, pcon);
        for (int i = 0; i < preNode.length; i++) {
          if (nextNodeId == preNode[i])//当前工作流存在后续节点的工作流程
          {
            return true;
          }
        }
      }
      rs.close();
      ps.close();
      ps = null;
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n错误sql为：" + sql
        + "参数为： pcurrent_node_id:" + pcurrent_node_id + " pnext_node_id:" + pnext_node_id + " entity_id:" + entity_id
        + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }
    return false;
  }

  /**
   * 是否存在曾到达节点的流程数据，用于退回，在存在多个可退回节点时退到曾经到达过的节点
   * @param entity_id
   * @param wf_id
   * @param pnode_id
   * @param pcon
   * @return
   * @throws SQLException
   */
  private boolean existHisTask(String entity_id, long pnode_id, Connection pcon) throws SQLException {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    ResultSet rs = null;
    String sql = "";
    try {
      //by ydz 如果为开始节点，则不退回
      sql = "select 1 from sys_wf_nodes where node_type <>'001' and node_id= ? and rg_code=? and set_year=?";
      ps = pcon.prepareStatement(sql);
      ps.setLong(1, pnode_id);
      ps.setString(2, rg_code);
      ps.setInt(3, Integer.parseInt(setYear));
      rs = ps.executeQuery();
      if (!rs.next()) {
        return false;
      }
      //modify by liuzw 20120410 增加对多财政多年度的支持
      sql = "select 1 from sys_wf_complete_tasks where (current_node_id=? or next_node_id=?) and entity_id=? and rg_code=? and set_year=?";
      ps = pcon.prepareStatement(sql);

      ps.setLong(1, pnode_id);
      ps.setLong(2, pnode_id);
      ps.setString(3, entity_id);
      ps.setString(4, rg_code);
      ps.setInt(5, Integer.parseInt(setYear));
      rs = ps.executeQuery();
      if (!rs.next()) {
        return false;
      }
      rs.close();
      ps.close();
      ps = null;
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n错误sql为：" + sql
        + "参数为： pnode_id:" + pnode_id + " entity_id:" + entity_id + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null)
        ps.close();
    }
    return true;
  }

  /**
   * 节点同步操作时，查询节点的所有下面节点,用于退回时，全部节点退回后才能继续下一步操作
   * @param wf_id
   * @param nCurrentNodeId
   * @param pcon
   * @return
   * @throws SQLException
   */
  private long[] getNextNodeIds(long wf_id, long nCurrentNodeId, Connection pcon) throws SQLException {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    String key = nCurrentNodeId + "-next" + setYear + "-" + rg_code;
    if (PRENODES.get(key) != null) {
      return (long[]) PRENODES.get(key);
    }
    //modify by liuzw 20120410 增加对多财政多年度的支持
    PreparedStatement ps = null;
    ResultSet rs = null;
    ArrayList list = new ArrayList();
    long[] node;
    String sql = "select next_node_id,node_id from sys_wf_node_conditions where routing_type='001' and wf_id=? and rg_code=? and set_year=?";
    try {
      ps = pcon.prepareStatement(sql);
      ps.setLong(1, wf_id);
      ps.setString(2, rg_code);
      ps.setInt(3, Integer.parseInt(setYear));

      rs = ps.executeQuery();
      while (rs.next()) {
        node = new long[2];
        node[0] = rs.getLong(1);
        node[1] = rs.getLong(2);
        list.add(node);
      }
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n错误sql为：" + sql + "参数为： wf_id:"
        + wf_id + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.WF_ERROR_SQL_OP);
    } finally {
      if (ps != null) {
        ps.close();
        ps = null;
      }
      if (rs != null) {
        rs.close();
        rs = null;
      }
    }
    long[] nextNodes = new long[list.size() + 1];
    nextNodes[0] = nCurrentNodeId;
    int nextSize = 1;
    int increase = 0;
    do {
      increase = 0;
      for (int i = 0; i < list.size(); i++) {
        node = (long[]) list.get(i);

        for (int j = 0; j < nextSize; j++) {
          if (node[1] == nextNodes[j])//后一个节点在列表中 退出循环 随后 将前一个节点判断是否在节点列表中，没有需要将上一个放入列表 
          {
            int k = 0;
            for (k = 0; k < nextSize; k++) {
              if (node[0] == nextNodes[k]) {
                break;
              }
            }
            if (k == nextSize)//前一个节点不在节点列表中，上一个放入列表 
            {
              increase++;
              nextNodes[nextSize] = node[0];
              nextSize++;
            }
            break;
          }
        }

      }
    } while (increase > 0);//循环时还找到上一个节点 继续找
    long[] rNextNodeIds = new long[nextSize - 1];//第一个是自身
    System.arraycopy(nextNodes, 1, rNextNodeIds, 0, nextSize - 1);
    PRENODES.put(key, rNextNodeIds);
    return rNextNodeIds;
  }

  /**
   * 得到同一来源的同级节点
   * @param wf_id
   * @param nCurrentNodeId
   * @param pcon
   * @return
   * @throws SQLException
   * @author justin(liwq2)
   */
  private long[] getSameLevelNextNodes(long nCurrentNodeId, Connection pcon) throws SQLException {
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    ResultSet rs = null;
    int n = 0;
    String sql = "select next_node_id,node_id from sys_wf_node_conditions where routing_type='001' and node_id=? and rg_code=? and set_year=?";
    try {
      ps = pcon.prepareStatement(sql);
      ps.setLong(1, nCurrentNodeId);
      ps.setString(2, rg_code);
      ps.setInt(3, Integer.parseInt(setYear));
      rs = ps.executeQuery();
      while (rs.next()) {
        tempTasks[n] = rs.getLong(1);
        n++;
      }
    } catch (SQLException e) {
      StringBuffer sb = new StringBuffer(WorkFlowExceptionConstants.WF_ERROR_SQL_OP + "\n错误sql为：" + sql
        + "节点为： node_id:" + nCurrentNodeId + " rg_code:" + rg_code + " set_year:" + setYear);
      log(sb.toString());
      throw new WFIllegalSqlOperateException(WorkFlowExceptionConstants.NOT_FOUND_CURRENT_NODE);
    } finally {
      if (ps != null) {
        ps.close();
        ps = null;
      }
      if (rs != null) {
        rs.close();
        rs = null;
      }
    }
    long[] sameLevelNode = new long[n];
    System.arraycopy(tempTasks, 0, sameLevelNode, 0, n);
    return sameLevelNode;
  }

  /**
   * 获取当前用户
   * @return
   */
  private String[] getCreateUser() {
    String create_user = "";
    String create_user_id = "";
    // 张晓楠添加，2010-11-15，BUG-2338，显示代理用户
    String authorieduser_name = (String) SessionUtil.getUserInfoContext().getAttribute("authorieduser_name");
    String authorieduser_id = (String) SessionUtil.getUserInfoContext().getAttribute("authorieduser_id");

    if (SessionUtil.getUserInfoContext().getAttribute("user_name") != null
      && !SessionUtil.getUserInfoContext().getAttribute("user_name").toString().equals("")) {
      create_user = SessionUtil.getUserInfoContext().getAttribute("user_name").toString();
    }
    if (SessionUtil.getUserInfoContext().getAttribute("user_id") != null
      && !SessionUtil.getUserInfoContext().getAttribute("user_id").toString().equals("")) {
      create_user_id = SessionUtil.getUserInfoContext().getAttribute("user_id").toString();
    }
    if (authorieduser_name != null && authorieduser_name.length() > 0 && !authorieduser_name.equals(create_user)) {
      create_user = authorieduser_name + "(代理：" + create_user + ")";
    }

    if (authorieduser_id != null && authorieduser_id.length() > 0 && !authorieduser_id.equals(create_user_id)) {
      create_user_id = authorieduser_id;
    }
    return new String[] { create_user, create_user_id };
  }

  /**
   * 调用资金监控接口
   * @param wf_id
   * @param inspectNodeId
   * @param menuid
   * @param roleid
   * @param actiontypeid
   * @param operationname
   * @param operationdate
   * @param operationremark
   * @param detail_table_name
   * @param detailDtos
   * @param pcon 
   * @throws Exception
   */
  private void doInspect(String wf_id, String inspectNodeId, String menuid, String roleid, String actiontypeid,
    String operationname, String operationdate, String operationremark, String detail_table_name, List detailDtos,
    Connection pcon) throws Exception {
    //资金监控
    IInspectService inspectService = null;
    try {
      inspectService = (IInspectService) SessionUtil.getServerBean("sys.inspectService");
    } catch (Exception e) {
      //没有配置资金监控 不做任何动作
    }

    // 如果访问不到监控类包，则不能调用其接口。
    if (inspectService != null && (!"NEXT".equalsIgnoreCase(actiontypeid) || isInspectNode(inspectNodeId, pcon))) {
      for (int i = 0; i < detailDtos.size(); i++) {
        final FVoucherDTO dto = (FVoucherDTO) detailDtos.get(i);
        dto.setWarning(inspectService.inspectInstance(wf_id, detail_table_name, inspectNodeId, null, actiontypeid,
          operationname, operationdate, operationremark, null, (FVoucherDTO) dto, menuid, roleid));
      }
    }
  }

  /**
   * 消息平台执行任务
   * @param currentNodeId
   * @param actionType
   * @param detailDtoList
   * @param isInstant
   */
  private void doMessageTask(String currentNodeId, String actionType, List detailDtoList, String sendUserId,
    String remark, boolean isInstant) {
    //消息平台服务
    MsgService msgService = null;
    List newDtoList = detailDtoList;
    try {
      //工作流即时消息
      if (isInstant) {
        msgService = (MsgService) SessionUtil.getServerBean("server.msg.openservice.TemplateWorkflowMsgService");
      }
      //工作流队列消息
      else {
        msgService = (MsgService) SessionUtil
          .getServerBean("server.msg.openservice.TemplateWorkflowAutoTaskMsgService");
        newDtoList = new ArrayList();
        int size = detailDtoList.size();
        for (int i = 0; i < size; i++) {
          FVoucherDTO dto = new FVoucherDTO();
          BeanUtils.copyProperties(dto, detailDtoList.get(i));
          newDtoList.add(dto);
        }
      }
    } catch (Exception e) {
      log("获取消息平台服务失败！\r\n" + e.getMessage());
    }

    if (msgService != null) {
      MsgServiceParam msgParam = new MsgServiceParam();
      msgParam.setWfNodeId(Long.valueOf(currentNodeId.trim()));
      msgParam.setWfActionType(actionType);
      msgParam.setBusinessDtoList(newDtoList);
      msgParam.setSendUserId(sendUserId);
      msgParam.setRemark(remark);

      msgService.sendMessage(msgParam);
    }
  }

  /**
   * 查找节点是否是监控节点
   * @param inspectNodeId
   * @param pcon
   * @return
  * @throws SQLException 
   */
  private boolean isInspectNode(String inspectNodeId, Connection pcon) throws SQLException {

    boolean bool = true;
    PreparedStatement ps = null;
    try {
      ps = pcon.prepareStatement("select 1 from inspect_flow_node where node_id = ?");
      ps.setString(1, inspectNodeId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        bool = true;
      } else {
        bool = false;
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      bool = false;
    } finally {
      if (ps != null)
        ps.close();
    }
    return bool;
  }

  /**
   * 根据节点找到流程
   * @param nodeId
   * @param pcon
   * @return
   * @throws SQLException
   */
  private long getWfIdByNodeId(long nodeId, Connection pcon) throws SQLException {
    //add by liuzw 20120410
    String rg_code = getRgCode();
    String setYear = getSetYear();
    PreparedStatement ps = null;
    ps = pcon.prepareStatement("select wf_id from sys_wf_nodes where node_id = ? and rg_code=? and set_year=?");
    ps.setLong(1, nodeId);
    ps.setString(2, rg_code);
    ps.setInt(3, Integer.parseInt(setYear));
    ResultSet rs = ps.executeQuery();
    long wf_id = 0;
    if (rs.next()) {
      wf_id = rs.getLong(1);
    }
    rs.close();
    ps.close();
    return wf_id;
  }

  /**
   * 根据明细单据获取表名
   * @param inputFVoucherDto
   * @return
   */
  private String getTableName(FVoucherDTO inputFVoucherDto) {

    String billType_code = (String) (inputFVoucherDto.getBilltype_code());

    String table_name = UtilCache.getTableNameByBillTypeCode(billType_code);
    if (table_name != null && !table_name.equals("")) {
      return table_name;
    } else {
      table_name = ((FBillTypeDTO) billtype.getBillTypeByCode(billType_code)).getTable_name();
      UtilCache.putTableNameByBillTypeCode(billType_code, table_name);
    }

    return table_name;
  }

  /**
   * 得到系统业务年度
   * 
   * @return
   * 
   * add by liuzw 20120326
   * 
   */
  public String getSetYear() {
    String set_year = (String) SessionUtil.getUserInfoContext().getSetYear();
    if (set_year == null || set_year.equalsIgnoreCase("")) {
      log("通过SessionUtil.getUserInfoContext().getSetYear()方法获取年度为空");
      set_year = String.valueOf(DateHandler.getCurrentYear());
    }
    return set_year;
  }

  /**
   * 得到行政区划代码
   * @return
   * add by liuzw 20120326
   */
  private String getRgCode() {
    String rg_code = SessionUtil.getRgCode();
    if (rg_code == null || rg_code.equals("")) {
      System.out.println("当前区划没有设置，请配置当前区划/n 类名" + this.getClass() + "\n getRgCode()");
      throw new NullPointerException("当前区划没有设置，请配置当前区划");
    }
    return rg_code;
  }

  /**
   * 记录日志 并打印信息
   * @param message
   */
  private void log(String message) {

  }

  /**
   * 监控处理预警强制通过数据
   * @param wf_id工作流ID
   * @param detail_table_name业务数据表名
   * @param listFVoucherDTO业务凭证或业务明细
   * @throws Exception违规则抛出异常InspectException
   * 
   */
  public void inspectDealResult(String wf_id, String detail_table_name, List listFVoucherDTO) throws Exception {
    //调用资金监控，批量数据处理优化
    IInspectService inspectService = null;
    try {
      inspectService = (IInspectService) SessionUtil.getServerBean("sys.inspectService");
    } catch (Exception e) {
      //没有配置资金监控 不做任何动作
      this.log("没有启动资金监控服务！");
    }
    if (inspectService != null) // 如果访问不到监控类包，则不能调用其接口。
    {
      inspectService.inspectDealResult(wf_id, detail_table_name, listFVoucherDTO);
    }
  }

  /**
   * 批量数据调用资金监控接口进行监控
   * @param menuid
   * @param role_id
   * @param actiontypeid
   * @param operation_name
   * @param operation_date
   * @param operation_remark
   * @param detail_table_name
   * @param listFVoucherDTO，资金监控接口监控完后会修改list中每个FVoucherDTO的监控结果标志，如果违规有违规描述，联系人信息等
   * @param boolean isDetail是单还是明细 true是明细，false是单
  *  @return map中包括两个key,分别为wf_id,detail_table_name
   * @throws Exception
   * @author zwh
   */
  public Map doBatchInspect(String menuid, String role_id, String action_type, String operation_name,
    String operation_date, String operation_remark, String detail_table_name, List listFVoucherDTO, boolean isDetail)
    throws Exception {
    //1.查询得到调用资金监控需要的参数
    Map map = this.getBatchInspectPar(menuid, role_id, action_type, operation_name, operation_date, operation_remark,
      detail_table_name, listFVoucherDTO, isDetail);

    //2.调用资金监控，批量数据处理优化
    IInspectService inspectService = null;
    try {
      inspectService = (IInspectService) SessionUtil.getServerBean("sys.inspectService");
    } catch (Exception e) {
      //没有配置资金监控 不做任何动作
      this.log("没有启动资金监控服务！");
    }
    if (inspectService != null) // 如果访问不到监控类包，则不能调用其接口。
    {
      inspectService.inspectBatchInstance((String) map.get("wf_id"), detail_table_name,
        (String) map.get("current_node_id"), null, action_type, operation_name, operation_date, operation_remark, null,
        listFVoucherDTO, menuid, role_id);
    }
    return map;
  }

  /**
   * 流转线规则处理
   * @param currentNodeId  当前节点
   * @param nextNodeId   下一节点
   * @param ruleLineId   规则id
   * @author justin
   * @return
   */
  public boolean doBatchLineRule(long currentNodeId, long nextNodeId, String ruleLineId) {
    StringBuffer sBuffer = new StringBuffer(
      "select * from sys_wf_rule_monitor t where t.line_rule_id=? and t.rg_code=? and t.set_year=? ");
    StringBuffer nBuffer = new StringBuffer(
      "select se.rule_type,se.rule_name,se.rule_content from sys_wf_extend_rule se,sys_wf_line_rule t ");
    nBuffer.append(" where se.rule_id = t.rule_id and se.rg_code = t.rg_code and se.set_year = t.set_year ");
    nBuffer.append(" and t.line_rule_id = ? and t.rg_code=? and t.set_year=? order by t.rule_order ");
    //查询规则类
    List ruleParaList = dao.findBySql(sBuffer.toString(), new String[] { ruleLineId, getRgCode(), getSetYear() });
    //规则
    List ruleList = dao.findBySql(nBuffer.toString(), new String[] { ruleLineId, getRgCode(), getSetYear() });
    if (ruleParaList != null && ruleParaList.size() > 0) {
      String ruleClassString = (String) ((Map) ruleParaList.get(0)).get("class_name");
      ISysRegulationListener p1;
      Object obj = null;
      try {
        obj = Class.forName(ruleClassString).newInstance();
        if (obj instanceof ISysRegulationListener) {
          p1 = (ISysRegulationListener) obj;
          p1.beforeLinceRulePropertiesSet(currentNodeId, nextNodeId, ruleList);
        }
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return true;
  }

  /**
   * 获取批量监控需要的工作流id，节点等参数
   * @param menuid
   * @param role_id
   * @param actiontypeid
   * @param operation_name
   * @param operation_date
   * @param operation_remark
   * @param detail_table_name
   * @param listFVoucherDTO
   * @param boolean isDetail是单还是明细 true是明细，false是单
  *  @return Map取得监控需要的参数，包括：String wf_id, String current_node_id,detail_table_name
    map中包括两个key,分别为wf_id,current_node_id,detail_table_name
   * @throws Exception
   * @author zwh
   */
  public Map getBatchInspectPar(String menuid, String role_id, String action_type, String operation_name,
    String operation_date, String operation_remark, String detail_table_name, List listFVoucherDTO, boolean isDetail)
    throws Exception {
    Map map = new HashMap();
    String wf_id = "";
    String current_node_id = "";
    FVoucherDTO detailDto = null;
    Session session = dao.getSession();
    try {
      Connection conn = session.connection();
      //1.获取第一个明细数据dto
      if (isDetail) {//
        detailDto = (FVoucherDTO) listFVoucherDTO.get(0);

      } else {
        FVoucherDTO billDto = (FVoucherDTO) listFVoucherDTO.get(0);
        detailDto = (FVoucherDTO) billDto.getDetails().get(0);
      }
      //2.获取第一个明细的当前流程节点
      current_node_id = getCurrentNodeId(menuid, role_id, detailDto.getVou_id(), conn);
      List conditions = getNextConditionId(current_node_id, conn);
      for (int j = 0; j < conditions.size(); j++) {
        Object[] condition = (Object[]) conditions.get(j);
        if ("#".equals(condition[2]) || workFlowRuleFactory.isMatchByBSH((String) condition[2], detailDto)) {
          wf_id = String.valueOf(((Long) condition[0]).longValue());
          break;
        }
      }
    } finally {
      dao.closeSession(session);
      map.put("wf_id", wf_id);
      map.put("current_node_id", current_node_id);
      map.put("detail_table_name", detail_table_name);
    }
    return map;
  }

  /**
   * 获取明细对象
   * @param Dto
   * @return
   */
  public XMLData getOrgDetail(FVoucherDTO DTO) {
    XMLData orgData = new XMLData();
    if (DTO != null) {
      //冗余机构权限，如果为空，则置为"#",以兼容指标管理系统
      String mb_id = DTO.getMb_id();//业务处室
      if (mb_id == null || mb_id.equals("")) {
        orgData.put("mb_id", "#");
      } else {
        orgData.put("mb_id", mb_id);
      }

      String agency_id = DTO.getAgency_id();//预算单位
      if (agency_id == null || agency_id.equals("")) {
        orgData.put("agency_id", "#");
      } else {
        orgData.put("agency_id", agency_id);
      }

      String pb_id = DTO.getPb_id();//代理银行
      if (pb_id == null || pb_id.equals("")) {
        orgData.put("pb_id", "#");
      } else {
        orgData.put("pb_id", pb_id);
      }

      String cb_id = DTO.getCb_id();//清算银行
      if (cb_id == null || cb_id.equals("")) {
        orgData.put("cb_id", "#");
      } else {
        orgData.put("cb_id", cb_id);
      }

      String ib_id = DTO.getIb_id();//收入银行
      if (ib_id == null || ib_id.equals("")) {
        orgData.put("ib_id", "#");
      } else {
        orgData.put("ib_id", ib_id);
      }

      String gp_agency_id = DTO.getGp_agency_id();//采购机构
      if (gp_agency_id == null || gp_agency_id.equals("")) {
        orgData.put("gp_agency_id", "#");
      } else {
        orgData.put("gp_agency_id", gp_agency_id);
      }
    }
    return orgData;
  }

  /**
   * 根据菜单，角色查询当前节点的全部下一节点
   * @param menuid
   * @param roleid
   * @param entity_id
   * @param pcon
   * @return
  * @throws Exception 
   */
  public List getNextNodes(String menuid, String roleid) throws Exception {
    PreparedStatement ps = null;
    String currentNodeId = null;

    String rg_code = getRgCode();
    String setYear = getSetYear();
    //当前节点
    List curentNodeList = dao.findBySql(QUERYCURRENTNODEBYROLEMENU, new String[] { setYear, rg_code, roleid, menuid });
    if (curentNodeList == null || (curentNodeList != null && curentNodeList.size() > 1))
      return null;
    String currentNode = (String) ((Map) curentNodeList.get(0)).get("node_id");
    List nextNodesList = dao.findBySql(QUERYNEXTNODESBYCURRENTNODE, new String[] { setYear, rg_code, currentNode });
    return nextNodesList;
  }

  //根据当前节点查询下一节点
  private static String QUERYNEXTNODESBYCURRENTNODE = "select next_node_id,(select  sn.node_name from sys_wf_nodes sn where sn.node_id=sw.next_node_id) node_name from sys_wf_node_conditions sw "
    + " where  sw.routing_type='001' and sw.set_year=? and sw.rg_code=? and sw.node_id =? ";

  //根据菜单、角色查询当前节点
  private static String QUERYCURRENTNODEBYROLEMENU = "select sw.node_id from sys_wf_nodes sw,sys_wf_role_node sr,sys_wf_module_node sm where sw.node_id = sr.node_id and sw.node_id = sm.node_id and sw.set_year = sr.set_year "
    + " and sw.rg_code = sm.rg_code and sw.set_year=? and sm.rg_code=? and sr.role_id =? and sm.module_id=?";

  /**
   * 根据菜单，角色查询当前节点的全部下一节点
   * @param menuid
   * @param roleid
   * @param entity_id
   * @param pcon
   * @return
  * @throws Exception 
   */
  public List getCurrentNodes(String menuid, String roleid) throws Exception {
    PreparedStatement ps = null;
    String currentNodeId = null;

    String rg_code = getRgCode();
    String setYear = getSetYear();
    //当前节点
    List curentNodeList = dao.findBySql(QUERYCURRENTNODEBYROLEMENU, new String[] { setYear, rg_code, roleid, menuid });
    return curentNodeList;
  }

}
