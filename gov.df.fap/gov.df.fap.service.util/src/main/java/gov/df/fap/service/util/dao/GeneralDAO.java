package gov.df.fap.service.util.dao;

import gov.df.fap.bean.user.UserInfoContext;
import gov.df.fap.bean.util.FPaginationDTO;
import gov.df.fap.service.util.datasource.MultiDataSource;
import gov.df.fap.service.util.sessionmanager.SessionUtil;
import gov.df.fap.util.xml.XMLData;

import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Component;

/*
 * 多数据源操作
 */
@Component("generalDAO")
public class GeneralDAO {
  @Autowired
  private HibernateTemplate hibernateTemplate;

  public GeneralDAO() {

  }

  public final Session getSession() {
    return getSession(this.hibernateTemplate.isAllowCreate());
  }

  private Session getSession(boolean allowCreate) {
    return new GovSession(!allowCreate ? SessionFactoryUtils.getSession(getSessionFactory(), false)
      : SessionFactoryUtils.getSession(getSessionFactory(), this.hibernateTemplate.getEntityInterceptor(),
        this.hibernateTemplate.getJdbcExceptionTranslator()));
  }

  public final void setSessionFactory(SessionFactory sessionFactory) {
    this.hibernateTemplate = createHibernateTemplate(sessionFactory);
  }

  protected HibernateTemplate createHibernateTemplate(SessionFactory sessionFactory) {
    return new HibernateTemplate(sessionFactory);
  }

  public final void closeSession(Session s) {
    GovSession gs = (GovSession) s;
    gs.closeSession();
    s = gs.getSession();
    try {
      boolean existingTransaction = SessionFactoryUtils.isSessionTransactional(s, getSessionFactory());
      if (existingTransaction == false) {
        SessionFactoryUtils.releaseSession(s, getSessionFactory());
      }
    } catch (HibernateException e) {
      e.printStackTrace();
    }
  }

  public final SessionFactory getSessionFactory() {
    return (this.hibernateTemplate != null ? this.hibernateTemplate.getSessionFactory() : null);
  }

  public final void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
    this.hibernateTemplate = hibernateTemplate;
  }

  public final HibernateTemplate getHibernateTemplate() {
    return hibernateTemplate;
  }

  public Object execute(HibernateCallback action) {
    return getHibernateTemplate().execute(action);
  }

  public List executeFind(HibernateCallback action) {
    return getHibernateTemplate().executeFind(action);
  }

  public void flush() {
    getHibernateTemplate().flush();
  }

  public void clear() {
    getHibernateTemplate().clear();
  }

  /**
   * 根据提交的sql和参数信息，执行sql语句，sql类型可以为delete,save,update
   * 
   * @param sql
   *            sql语句
   * @param params
   *            参数信息
   * @return 执行操作的记录数
   */
  public int executeBySql(final String sql, final Object[] params) {
    Object result = execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        PreparedStatement ps = conn.prepareStatement(sql);
        setParams(ps, params);
        int row = ps.executeUpdate();
        ps.close();
        return new Integer(row);
      }

    });
    return ((Integer) result).intValue();
  }

  /**
   * 根据提交的sql和参数信息，执行sql语句，sql类型可以为delete,insert,update
   * 
   * @param sql
   *            sql语句
   * @param paramsList
   *            参数信息 预处理，内部是Object[] params的List
   * @return 执行操作的记录数
   */
  public int[] executePreparedBatchBySql(final String sql, final List paramsList) {
    Object result = execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        Iterator iterator = paramsList.iterator();
        PreparedStatement ps = conn.prepareStatement(sql);
        while (iterator.hasNext()) {
          setParams(ps, (Object[]) iterator.next());
          ps.addBatch();
        }
        int[] rows = ps.executeBatch();
        ps.close();
        return rows;
      }

    });
    return (int[]) result;
  }

  /**
   * 根据提交的sql和参数信息，执行sql语句，sql类型可以为delete,insert,update
   * 
   * @param sql
   *            sql语句
   * @param paramsList
   *            参数信息  内部是可直接执行的sql
   * @return 执行操作的记录数
   */
  public int[] executeBatchBySql(final List sqlList) {
    Object result = execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        Iterator iterator = sqlList.iterator();
        Statement ps = conn.createStatement();
        while (iterator.hasNext()) {
          ps.addBatch((String) iterator.next());
        }
        int[] rows = ps.executeBatch();
        ps.close();
        return rows;
      }

    });
    return (int[]) result;
  }

  /**
   * 根据提交的sql，执行sql语句，sql类型可以为delete,save,update
   * 
   * @param sql
   *            sql语句
   */
  public int executeBySql(final String sql) {
    Object result = execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        PreparedStatement ps = conn.prepareStatement(sql);
        int row = ps.executeUpdate();
        ps.close();
        return new Integer(row);
      }

    });
    return ((Integer) result).intValue();
  }

  /**
   * 根据提交sql语句和参数信息，返回每一条为Map记录的list结果
   * 
   * @param sql
   *            sql语句
   * @param params
   *            参数信息
   * @return 返回每一条为Map记录的list结果
   */
  public List findBySql(final String sql, final Object[] params) {
    return executeFind(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        PreparedStatement ps = conn.prepareStatement(sql);
        setParams(ps, params);
        ResultSet rs = ps.executeQuery();
        List result = new ArrayList();
        Map map = null;
        for (; rs.next(); result.add(map))
          map = getHashMapByResultset(rs);

        rs.close();
        ps.close();
        return result;
      }

    });
  }

  /**
   * 根据提交sql语句和参数信息，返回每一条为Map记录的list结果
   * 
   * @param sql
   *            sql语句
   * @param params
   *            参数信息
   * @param clazz
   *            DTO对象类
   * @return 包含clazz对应的DTO对象记录结果
   */
  public List findBeanBySql(final String sql, final Object[] params, final Class clazz) {
    return executeFind(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        List result = new ArrayList();
        try {
          Connection conn = session.connection();
          PreparedStatement ps = conn.prepareStatement(sql);
          setParams(ps, params);
          ResultSet rs = ps.executeQuery();
          result = resultSet2Dto(rs, clazz);
          rs.close();
          ps.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        return result;
      }

    });
  }

  /**
   * oracle 分页查询方法  
   * @param pageInfo  前端分页参数
   * @param sql 执行的sql
   * @return
   */
  public List findByPageSql(String pageInfo, String sql) {
    FPaginationDTO page = new FPaginationDTO();
    if (pageInfo != null && (!pageInfo.equals(""))) {
      String[] pageArray = pageInfo.split("-");
      int pagecount = Integer.parseInt(pageArray[0]);
      int currpage = Integer.parseInt(pageArray[1]) + 1;

      sql = "select * from (" + "select t.*, rownum rw_ from (" + sql + ") t ) where rw_> " + pagecount
        * (currpage - 1) + " and rw_<=" + pagecount * (currpage);
    }

    return findBySql(sql);
  }

  public int getCount(String sql) {
    sql = "select count(*) sum from  ( " + sql + " )";
    List list = findBySql(sql);
    int sum = 0;
    if (list.size() == 1) {
      Map data = (Map) list.get(0);
      sum = null != data.get("sum") ? Integer.parseInt(data.get("sum").toString()) : 0;
    }
    return sum;
  }

  /**
   * 根据提交sql语句，返回每一条为Map记录的list结果
   * 
   * @param sql
   *            sql语句
   * @return返回每一条为Map记录的list结果
   */
  public List findBySql(final String sql) {
    return executeFind(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);

        List result = new ArrayList();
        Map map = null;
        for (; rs.next(); result.add(map))
          map = getHashMapByResultset(rs);
        rs.close();
        ps.close();
        return result;
      }

    });
  }

  /**
   * 根据查询语句，返回DTO对象的记录列表结果
   * 
   * @param sql
   *            查询语句
   * @param clazz
   *            DTO对象类
   * @return 包含clazz对应的DTO对象记录结果
   */
  public List findBeanBySql(final String sql, final Class clazz) {

    return executeFind(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        List result = new ArrayList();

        try {
          Connection conn = session.connection();
          PreparedStatement ps = conn.prepareStatement(sql);
          ResultSet rs = ps.executeQuery();
          result = resultSet2Dto(rs, clazz);
          rs.close();
          ps.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        return result;
      }

    });

  }

  /**
   * 数据库记录结果集转换为DTO对象结果集方法
   * 
   * @param rs
   *            数据库记录结果集
   * @param clazz
   *            DTO对象类
   * @return DTO对象结果集
   * @throws SQLException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private List resultSet2Dto(ResultSet rs, Class clazz) throws SQLException, InstantiationException,
    IllegalAccessException, InvocationTargetException {
    List ret = new ArrayList();
    List propertyList = new ArrayList();
    String[] fieldLst = null;
    try {
      // 取得类的属性集合
      PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(clazz);
      // 取得记录字段定义
      ResultSetMetaData rsMeta = rs.getMetaData();
      int iColumnCount = rsMeta.getColumnCount();
      fieldLst = new String[iColumnCount];
      for (int i = 0; i < iColumnCount; i++) {
        fieldLst[i] = rsMeta.getColumnName(i + 1).toLowerCase();
      }
      // 取得类和记录共同的字段
      String fieldName = null;
      for (int i = 0; i < properties.length; i++) {
        fieldName = properties[i].getName();
        for (int j = 0; j < fieldLst.length; j++) {
          if (fieldName.equalsIgnoreCase(fieldLst[j])) {
            propertyList.add(fieldName);
            break;
          }
        }
      }
      // 遍历记录结果集，转换为DTO对象
      while (rs.next()) {
        Object obj = clazz.newInstance();
        Iterator it = propertyList.iterator();

        while (it.hasNext()) {
          fieldName = (String) it.next();
          BeanUtils.setProperty(obj, fieldName, rs.getObject(fieldName));
        }
        ret.add(obj);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    } catch (InstantiationException e) {
      e.printStackTrace();
      throw e;
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      throw e;
    } catch (InvocationTargetException e) {
      e.printStackTrace();
      throw e;
    }
    return ret;

  }

  /**
   * 根据提交sql语句，返回每一条为Map记录的list结果,其中的Map中的KEY为大写
   * 
   * @param sql
   *            sql语句
   * @return返回每一条为Map记录的list结果 edited by zhadaopeng
   */
  public List findBySqlByUpper(final String sql) {
    return executeFind(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        List result = new ArrayList();
        Map map = null;
        for (; rs.next(); result.add(map))
          map = getHashMapByResultSetByUpper(rs);
        rs.close();
        ps.close();
        return result;
      }

    });
  }

  protected XMLData getHashMapByResultset(ResultSet rs) {
    XMLData hm = new XMLData();
    try {
      ResultSetMetaData rsMeta = rs.getMetaData();
      int i = rsMeta.getColumnCount();
      String sColumName = null;
      String value = null;
      for (; i > 0; i--) {
        int columnType = rsMeta.getColumnType(i);
        sColumName = rsMeta.getColumnName(i).toLowerCase();
        int columScale = rsMeta.getScale(i);
        // number类型必须特殊处理,否则如果直接getString,对于小数将无法正确显示
        // 如0.1将显示为.1
        if (columnType == Types.NUMERIC) {
          value = String.valueOf(rs.getBigDecimal(sColumName));
          // 李文全增加，2009－03－04 解决number(xx,2)时返回"null.00" 的问题
          if (value == null || value.equals("null") || value.equals("")) {
            value = "";
          } else {
            if (columScale > 0) {
              if (value.indexOf(".") == -1) {
                value += ".";
                for (int j = 0; j < columScale; j++) {
                  value += "0";
                }
              } else {
                // 补零的个数
                int length = columScale - (value.length() - value.indexOf(".")) + 1;
                for (int j = 0; j < length; j++) {
                  value += "0";
                }
              }
            }
          }
        } else if (columnType == Types.CLOB) {
          //暂时不处理BLOB、CLOB类型
          value = clob2String(rs.getClob(sColumName));
        } else if (columnType == Types.BLOB) {
          value = blob2String(rs.getBlob(sColumName));
        } else {
          value = rs.getString(sColumName);
        }

        if (value != null && value.equalsIgnoreCase("null"))
          value = null;
        hm.put(sColumName, value);

      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return hm;
  }

  public Map resultSet2Map(ResultSet rs) {
    if (rs == null) {
      return null;
    }
    Map map = null;
    ResultSetMetaData meta;
    try {
      meta = rs.getMetaData();
      int count = meta.getColumnCount();
      map = new HashMap();

      for (int i = 1; i <= count; i++) {
        if (rs.getObject(i) != null) {
          map.put(meta.getColumnName(i), rs.getObject(i));
        }
      }

    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return map;
  }

  /**
   * 根据ResultSet返回XMLData,其中的KEY为大写
   * 
   * @param--------ResultSet
   * @return-------XMLData edited by zhadaopeng
   */
  protected XMLData getHashMapByResultSetByUpper(ResultSet rs) {
    XMLData hm = new XMLData();
    try {
      ResultSetMetaData rsMeta = rs.getMetaData();
      int i = rsMeta.getColumnCount();
      String sColumName = null;
      String value = null;
      for (; i > 0; i--) {
        sColumName = rsMeta.getColumnName(i).toUpperCase();
        value = rs.getString(sColumName);
        hm.put(sColumName, value);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return hm;
  }

  protected void setParams(PreparedStatement ps, Object[] params) throws SQLException {
    if (params == null || params.length == 0)
      return;
    for (int i = 1; i <= params.length; i++)
      ps.setObject(i, params[i - 1]);
  }

  /**
   * 持久化数据前将客户端map转变为hibernate可映射的map
   * 
   * @param data
   *            客户端map数据
   * @return hibernate可映射的map
   * @throws Exception
   *             异常
   */
  public Map getHibernateMap(Map data) throws Exception {
    String table = (String) data.get("table");
    String tableClassName = null;
    if (table == null || table.equals("") || !table.startsWith("mappingfiles")) {
      tableClassName = (String) data.get("table_name");
    } else {
      tableClassName = table;
    }

    if (tableClassName == null || tableClassName.equals(""))
      throw new Exception("请指定对哪张表进行数据库操作");
    return data;
  }

  /**
   * 从数据库查询数据返回客户端之前转变为map，只针对单表所有字段操作
   * 
   * @param list
   *            从数据库获得的数据List
   * @return 返回给客户的list，每一条记录为map对象
   * @throws Exception
   *             异常
   */
  public List getClientMap(List list) throws Exception {
    List result = new ArrayList();
    Map[] data = null;
    if (list != null) {
      if (list.size() > 0) {
        data = new HashMap[list.size()];
        for (int i = 0; i < list.size(); i++) {
          Object obj = list.get(i);
          data[i] = BeanUtils.describe(obj);
          Map newData = new XMLData();
          try {
            Iterator it = data[i].keySet().iterator();
            while (it.hasNext()) {
              String key = (String) it.next();
              String value = (String) data[i].get(key);
              key = key.toLowerCase();
              newData.put(key, value);
            }
            newData.remove("class");
          } catch (Exception e) {
            e.printStackTrace();
            throw e;
          }
          result.add(newData);
        }
      }
    }

    return result;
  }

  /**
   * 从数据库查询数据返回客户端之前转变为map，针对多表部分字段操作
   * 
   * @param list
   *            从数据库获得的数据List
   * @return 返回给客户的list，每一条记录为map对象
   * @throws Exception
   *             异常
   * @deprecated 不推荐使用，建议使用findBySql()
   */
  public List getClientMap(List list, Object[] tableFields) throws Exception {
    List result = new ArrayList();
    Map[] data = null;
    if (list != null) {
      if (list.size() > 0) {
        data = new XMLData[list.size()];
        for (int i = 0; i < list.size(); i++) {
          Object[] objs = (Object[]) list.get(i);
          for (int j = 0; j < tableFields.length; j++) {
            String key = ((String) tableFields[j]).toLowerCase();
            Object value = (Object) objs[j];
            if (data[i] == null)
              data[i] = new XMLData();
            data[i].put(key, value);
          }
          result.add(data[i]);
        }
      }
    }

    return result;
  }

  public static void main(String[] args) {
    // System.out.println(parseTableClassName("mappingfiles.sysdb.ELEBudgetSource"));
  }

  /**
   * 根据年份取一个数据库连接
   * 
   * @param year
   *            String 要取的年份
   * 
   * @return Connection 返回一个连接
   * 
   * @throws SQLException
   *             取连接时发生错误，向外抛出sql错误
   * @author 黄节 2007年8月23日 添加
   */
  public Connection getConnectionByYear(int year) throws SQLException {
    MultiDataSource multiDataSource = (MultiDataSource) SessionUtil.getServerBean("multiDataSource");
    DataSource tmp = multiDataSource.getDataSourceWithYear(year);
    return tmp.getConnection();
  }

  /**
   * 根据年份、区划取一个数据库连接
   * 
   * @param year
   *            String 要取的年份
   * @param rgCode
   *            String 区划
   * 
   * @return Connection 返回一个连接
   * 
   * @throws SQLException
   *             取连接时发生错误，向外抛出sql错误
   * @author 黄节 2007年8月23日 添加
   */
  public Connection getConnectionByYearAndRgCode(int year, String rgCode) throws SQLException {
    MultiDataSource multiDataSource = (MultiDataSource) SessionUtil.getServerBean("multiDataSource");
    DataSource tmp = multiDataSource.getDataSourceWithRgYear(year, rgCode);
    return tmp.getConnection();
  }

  /**
   * 根据提交的sql和参数信息（指定年份），执行sql语句，sql类型可以为delete,save,update
   * 
   * @param year
   *            int 指定执行的年份
   * @param sql
   *            String sql语句
   * @param params
   *            Object[] 参数信息
   * 
   * @return int 执行操作的记录数
   * 
   * @throws SQLException
   *             向前抛出sql错误
   * @author 黄节 2007年8月23日 添加
   */
  public int executeBySqlWithYear(int year, final String sql, final Object[] params) throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      // 根据年份获取一个连接
      conn = getConnectionByYear(year);
      // 关闭自动提交
      conn.setAutoCommit(false);
      ps = conn.prepareStatement(sql);
      setParams(ps, params);
      int row = ps.executeUpdate();
      // 提交
      conn.commit();
      return row;
    } catch (SQLException ex) {
      conn.rollback();
      throw ex;
    } finally {
      if (ps != null) {
        ps.close();
        ps = null;
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * 根据提交sql语句和参数信息（指定年份），返回每一条为Map记录的list结果
   * 
   * @param year
   *            int 指定执行的年份
   * @param sql
   *            String sql语句
   * @param params
   *            Object[] 参数信息
   * 
   * @return List（XMLData）返回每一条为Map记录的list结果
   * 
   * @throws SQLException
   *             向前抛出sql错误
   * @author 黄节 2007年8月23日 添加
   */
  public List findBySqlWithYear(int year, final String sql, final Object[] params) throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      // 根据年份获取一个连接
      conn = getConnectionByYear(year);
      // 关闭自动提交
      conn.setAutoCommit(false);
      ps = conn.prepareStatement(sql);
      setParams(ps, params);
      rs = ps.executeQuery();
      // 提交
      conn.commit();
      // 转换数据
      List result = new ArrayList();
      Map map = null;
      for (; rs.next(); result.add(map))
        map = getHashMapByResultset(rs);
      return result;
    } finally {
      if (rs != null) {
        rs.close();
        rs = null;
      }
      if (ps != null) {
        ps.close();
        ps = null;
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * 根据提交sql语句和参数信息（指定年份），返回每一条为DTO记录的list结果
   * 
   * @param year
   *            int 指定执行的年份
   * @param sql
   *            String sql语句
   * @param params
   *            Object[] 参数信息
   * 
   * @param clazz
   *            DTO对象类
   * @return 包含clazz对应的DTO对象记录结果
   * 
   * @throws SQLException
   *             向前抛出sql错误
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  public List findBeanBySqlWithYear(int year, final String sql, final Object[] params, Class clazz)
    throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      // 根据年份获取一个连接
      conn = getConnectionByYear(year);
      // 关闭自动提交
      conn.setAutoCommit(false);
      ps = conn.prepareStatement(sql);
      setParams(ps, params);
      rs = ps.executeQuery();
      // 提交
      conn.commit();
      // 转换数据
      List result = new ArrayList();
      result = resultSet2Dto(rs, clazz);
      return result;
    } finally {
      if (rs != null) {
        rs.close();
        rs = null;
      }
      if (ps != null) {
        ps.close();
        ps = null;
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * 根据提交sql语句和参数信息（指定年份），返回每一条为Map记录的list结果
   * 
   * @param year
   *            int 指定执行的年份
   * @param rgCode
   *            String 指定执行的行政区划        
   * @param sql
   *            String sql语句
   * 
   * @return List（XMLData）返回每一条为Map记录的list结果
   * 
   * @throws SQLException
   *             向前抛出sql错误
   * @author kongcy 2012-03-27 添加
   */
  public List findBySqlWithRgYear(int year, String rgCode, final String sql) throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      // 根据年份获取一个连接
      conn = getConnectionByRGYear(year, rgCode);
      // 关闭自动提交
      conn.setAutoCommit(false);
      ps = conn.prepareStatement(sql);
      rs = ps.executeQuery();
      // 提交
      conn.commit();
      // 转换数据
      List result = new ArrayList();
      Map map = null;
      for (; rs.next(); result.add(map))
        map = getHashMapByResultset(rs);
      return result;
    } finally {
      if (rs != null) {
        rs.close();
        rs = null;
      }
      if (ps != null) {
        ps.close();
        ps = null;
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * 根据年份取一个数据库连接
   * 
   * @param year
   *            int 要取的年份
   * @param string
   *            rgCode 要取的行政区划
   * @return Connection 返回一个连接
   * 
   * @throws SQLException
   *             取连接时发生错误，向外抛出sql错误
   * @author kongcy 2012-03-27 添加
   */
  public Connection getConnectionByRGYear(int year, String rgCode) throws SQLException {
    MultiDataSource multiDataSource = (MultiDataSource) SessionUtil.getServerBean("multiDataSource");
    DataSource tmp = multiDataSource.getDataSourceWithRgYear(year, rgCode);
    return tmp.getConnection();
  }

  /**
   * 根据提交的sql和参数信息（指定年份），执行sql语句，sql类型可以为delete,save,update
   * 
   * @param year
   *            int 指定执行的年份
   * @param sql
   *            String sql语句
   * 
   * @return int 执行操作的记录数
   * 
   * @throws SQLException
   *             向前抛出sql错误
   * @author 黄节 2007年8月23日 添加
   */
  public int executeBySqlWithYear(int year, final String sql) throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      // 根据年份获取一个连接
      conn = getConnectionByYear(year);
      // 关闭自动提交
      conn.setAutoCommit(false);
      ps = conn.prepareStatement(sql);
      int row = ps.executeUpdate();
      // 提交
      conn.commit();
      return row;
    } catch (SQLException ex) {
      conn.rollback();
      throw ex;
    } finally {
      if (ps != null) {
        ps.close();
        ps = null;
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * 根据提交sql语句和参数信息（指定年份），返回每一条为Map记录的list结果
   * 
   * @param year
   *            int 指定执行的年份
   * @param sql
   *            String sql语句
   * 
   * @return List（XMLData）返回每一条为Map记录的list结果
   * 
   * @throws SQLException
   *             向前抛出sql错误
   * @author 黄节 2007年8月23日 添加
   */
  public List findBySqlWithYear(int year, final String sql) throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      // 根据年份获取一个连接
      conn = getConnectionByYear(year);
      // 关闭自动提交
      conn.setAutoCommit(false);
      ps = conn.prepareStatement(sql);
      rs = ps.executeQuery();
      // 提交
      conn.commit();
      // 转换数据
      List result = new ArrayList();
      Map map = null;
      for (; rs.next(); result.add(map))
        map = getHashMapByResultset(rs);
      return result;
    } finally {
      if (rs != null) {
        rs.close();
        rs = null;
      }
      if (ps != null) {
        ps.close();
        ps = null;
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   *查找所有数据源，返回所有的结果集。
   * 
   * @param sql
   *            String sql语句
   * 			 /key year+rgcode 
   * @return Map 					/key 参数 set_id==null?chr_code:chr_code+set_id 
   * 			 \Map 同一数据源下的所用参数
   * 								\value 参数chr_value
   * @throws SQLException
   *             向前抛出sql错误
   * @author 
   */
  public Map findBySqlWithAllDatasource(final String sql) throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    MultiDataSource multiDataSource = (MultiDataSource) SessionUtil.getServerBean("multiDataSource");
    Map dataSourcesMap = multiDataSource.getDataSources();
    Map returnMap = new HashMap();
    Set keySet = dataSourcesMap.keySet();
    Iterator it = keySet.iterator();
    while (it.hasNext()) {
      String key = (String) it.next();
      String[] rgyear = key.split("#");
      DataSource datasource = (DataSource) dataSourcesMap.get(key);
      String sql1 = sql.concat(" and rg_code = ? and set_year=?");
      try {
        conn = datasource.getConnection();
        ps = conn.prepareStatement(sql1);
        ps.setString(1, rgyear[0]);
        ps.setString(2, rgyear[1]);
        rs = ps.executeQuery();
        // 转换数据
        Map tempMap = new HashMap();
        Map map = null;
        for (; rs.next();) {
          map = getHashMapByResultset(rs);
          String mapKey = "";
          String set_id = (String) map.get("set_id");
          if (set_id != null && !"".equalsIgnoreCase(set_id.trim())) {
            mapKey = String.valueOf(map.get("chr_code")).trim() + "_" + set_id;
          } else {
            mapKey = String.valueOf(map.get("chr_code")).trim();
          }
          tempMap.put(String.valueOf(mapKey).trim(), String.valueOf(map.get("chr_value")).trim());
        }
        returnMap.put(rgyear[1].concat(rgyear[0]), tempMap);

      } finally {
        if (rs != null) {
          rs.close();
          rs = null;
        }
        if (ps != null) {
          ps.close();
          ps = null;
        }
        if (conn != null) {
          conn.close();
        }
      }

    }

    return returnMap;

  }

  /**
   * 存储表名和对应的字段
   */
  private static Map TableFieldsSQLMap = new HashMap();

  /**
   * 将dtoOrMap所含数据更新到tableName表中,返回更新数据条数，默认主键是
   * @param tableName
   * @param dtoOrMap
   * @return
   * @author
   */
  public int updateDataBySql(final String tableName, final Object dtoOrMap) {
    return updateDataBySql(tableName, getFieldsByTableName(tableName), dtoOrMap, new String[] { "id" });
  }

  /**
   * 将dtoOrMap所含数据更新到tableName表中,返回更新数据条数
   * @param tableName
   * @param dtoOrMap
   * @param id
   * @return
   * @author
   */
  public int updateDataBySql(final String tableName, final Object dtoOrMap, final String[] id) {
    return updateDataBySql(tableName, getFieldsByTableName(tableName), dtoOrMap, id);
  }

  /**
   * 将dtoOrMap所含数据更新到tableName表中,返回更新数据条数
   * @param tableName
   * @param valueFields 数据更新字段 字段名必须小写
   * @param dtoOrMap 数据
   * @param keyFields 主键字段 字段名必须小写
   * @return
   * @author
   */
  public int updateDataBySql(final String tableName, final String[] valueFields, final Object dtoOrMap,
    final String[] keyFields) {
    Object result = execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        PreparedStatement ps = conn.prepareStatement(getUpdateSql(tableName, valueFields, keyFields));
        setUpdateParams(ps, valueFields, keyFields, dtoOrMap);
        int update = ps.executeUpdate();
        ps.close();
        return new Integer(update);
      }
    });
    return ((Integer) result).intValue();
  }

  /**
   * 将批量更新dtoOrMap所含数据更新到tableName表中,返回逐条数据的影响条数，一般失败时int[i]==0
   * @param tableName
   * @param dtoOrMap 数据
   * @return
   * @author h
   */

  public int[] batchUpdateDataBySql(final String tableName, final List dtoOrMapList) {
    return batchUpdateDataBySql(tableName, getFieldsByTableName(tableName), dtoOrMapList, new String[] { "id" });
  }

  /**
   * 将批量更新dtoOrMap所含数据更新到tableName表中,返回逐条数据的影响条数，一般失败时int[i]==0
   * @param tableName
   * @param dtoOrMap 数据
   * @return
   * @author zgh 2012-05-24 添加
   */

  public int[] batchUpdateDataBySql(final String tableName, final List dtoOrMapList, final String[] id) {
    return batchUpdateDataBySql(tableName, getFieldsByTableName(tableName), dtoOrMapList, id);
  }

  /**
   * 将批量更新dtoOrMap所含数据更新到tableName表中,返回逐条数据的影响条数，一般失败时int[i]==0
   * @param tableName
   * @param valueFields 数据更新字段 字段名必须小写
   * @param dtoOrMap 数据
   * @param keyFields 主键字段 字段名必须小写
   * @return
   * @author hult 2011-03-01 添加
   */
  public int[] batchUpdateDataBySql(final String tableName, final String[] valueFields, final List dtoOrMapList,
    final String[] keyFields) {
    Object result = execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        PreparedStatement ps = conn.prepareStatement(getUpdateSql(tableName, valueFields, keyFields));
        for (int i = 0, n = dtoOrMapList.size(); i < n; i++) {
          setUpdateParams(ps, valueFields, keyFields, dtoOrMapList.get(i));
          ps.addBatch();
        }
        int[] batch = ps.executeBatch();
        ps.close();
        return batch;
      }
    });
    return (int[]) result;
  }

  /**
   * 按表名保存数据将dtoOrMap所含数据保存到tableName表中,返回成功与否
   * @param tableName
   * @param dtoOrMap
   * @return
   * @author hult 2011-03-01 添加
   */
  public boolean saveDataBySql(final String tableName, final Object dtoOrMap) {
    return saveDataBySql(tableName, getFieldsByTableName(tableName), dtoOrMap);
  }

  /**
   * 单条插入数据到表中，返回成功与否
   * @param tableName 
   * @param fields ，字段名必须小写
   * @param dtoOrMapList
   * @return
   * @author hult 2011-03-01 添加
   */
  public boolean saveDataBySql(final String tableName, final String[] fields, final Object dtoOrMap) {
    Object result = execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        PreparedStatement ps = conn.prepareStatement(getInserSql(tableName, fields));
        setParams(ps, fields, dtoOrMap);
        boolean bool = ps.execute();
        ps.close();
        return new Boolean(bool);
      }
    });
    return ((Boolean) result).booleanValue();
  }

  /**
   * 批量插入数据到表中，返回逐条数据的影响条数，一般失败时int[i]==0
   * @param tableName 
   * @param fields
   * @param dtoOrMapList
   * @return
   * @author hult 2011-03-01 添加
   */
  public int[] batchSaveDataBySql(final String tableName, final List dtoOrMapList) {
    return batchSaveDataBySql(tableName, getFieldsByTableName(tableName), dtoOrMapList);
  }

  /**
   * @author justin
   * @param entities
   */
  public void saveOrUpdateAll(List entities) {
    Iterator it = entities.iterator();
    while (it.hasNext()) {
      saveOrUpdate(it.next());
    }
  }

  public void saveOrUpdate(Object entity) {
    formatHibernateObj(entity);
    getHibernateTemplate().saveOrUpdate(entity);
  }

  /**
   * 批量插入数据到表中，返回逐条数据的影响条数，一般失败时int[i]==0
   * @param tableName 
   * @param fields 字段名必须小写
   * @param dtoOrMapList
   * @return
   * @author hult 2011-03-01 添加
   */
  public int[] batchSaveDataBySql(final String tableName, final String[] fields, final List dtoOrMapList) {
    Object result = execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        PreparedStatement ps = conn.prepareStatement(getInserSql(tableName, fields));
        for (int i = 0, n = dtoOrMapList.size(); i < n; i++) {
          setParams(ps, fields, dtoOrMapList.get(i));
          ps.addBatch();
        }
        int[] batch = ps.executeBatch();
        ps.close();
        return batch;
      }
    });
    return (int[]) result;
  }

  /**
   * 将dtoOrMap所含数据从tableName表删除,返回删除数据条数，默认主键"id"
   * @param tableName
   * @param dtoOrMap
   * @return
   * @author hult 2011-03-01 添加
   */
  public int deleteDataBySql(final String tableName, final Object dtoOrMap) {
    return deleteDataBySql(tableName, dtoOrMap, new String[] { "id" });
  }

  /**
   * 将dtoOrMap所含数据从tableName表按主键删除keyFields,返回删除数据条数，一般失败时int==0
   * @param tableName
   * @param valueFields 数据更新字段 字段名必须小写
   * @param dtoOrMap 数据
   * @param keyFields 主键字段 字段名必须小写
   * @return
   * @author hult 2011-03-01 添加
   */
  public int deleteDataBySql(final String tableName, final Object dtoOrMap, final String[] keyFields) {
    Object result = execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        PreparedStatement ps = conn.prepareStatement(getDeleteSql(tableName, keyFields));
        setParams(ps, keyFields, dtoOrMap);
        int delete = ps.executeUpdate();
        ps.close();
        return new Integer(delete);
      }
    });
    return ((Integer) result).intValue();
  }

  /**
   * 批量将dtoOrMapList所含数据从tableName表按主键keyFields删除,返回逐条删除的影响条数，一般失败时int[i]==0
   * @param tableName
   * @param dtoOrMap 数据
   * @return
   * @author hult 2011-03-01 添加
   */

  public int[] batchDeleteDataBySql(final String tableName, final List dtoOrMapList) {
    return batchDeleteDataBySql(tableName, dtoOrMapList, new String[] { "id" });
  }

  /**
   * 批量将dtoOrMapList所含数据从tableName表按主键keyFields删除,返回逐条删除的影响条数，一般失败时int[i]==0
   * @param tableName
   * @param dtoOrMap 数据
   * @param keyFields 主键字段 字段名必须小写
   * @return
   * @author hult 2011-03-01 添加
   */
  public int[] batchDeleteDataBySql(final String tableName, final List dtoOrMapList, final String[] keyFields) {
    Object result = execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        PreparedStatement ps = conn.prepareStatement(getDeleteSql(tableName, keyFields));
        for (int i = 0, n = dtoOrMapList.size(); i < n; i++) {
          setParams(ps, keyFields, dtoOrMapList.get(i));
          ps.addBatch();
        }
        int[] batch = ps.executeBatch();
        ps.close();
        return batch;
      }
    });
    return (int[]) result;
  }

  /**
   * 按字段逐个获取dtoOrMap的属性设置PreparedStatement
   * @param ps
   * @param fieldNames 字段名必须小写
   * @param dtoOrMap
   * @throws SQLException
   * @author hult 2011-03-01 添加
   */
  protected void setParams(PreparedStatement ps, String[] fieldNames, Object dtoOrMap) throws SQLException {
    if (fieldNames == null || fieldNames.length == 0)
      return;
    for (int i = 1, n = fieldNames.length; i <= n; i++) {
      try {
        Object obj = BeanUtils.getProperty(dtoOrMap, fieldNames[i - 1]);
        //因为字段"report_id"、"zero_is_null"、"rg_code"、"set_year"四个字段前台没有传过来，但是数据表中又是非空字段，所以在此给它添加赋值。
        if (obj == null) {
          if (fieldNames[i - 1].equals("report_id")) {
            obj = "100001";
          } else if (fieldNames[i - 1].equals("zero_is_null")) {
            obj = 0;
          } else if (fieldNames[i - 1].equals("rg_code")) {
            obj = SessionUtil.getRgCode();
          } else if (fieldNames[i - 1].equals("set_year")) {
            obj = SessionUtil.getLoginYear();
          } else {
            obj = "";
          }
        }
        ps.setObject(i, obj);
      } catch (Exception e) {
        try {
          Object obj = BeanUtils.getProperty(dtoOrMap, fieldNames[i - 1].toUpperCase());
          if (obj == null)
            obj = "";
          ps.setObject(i, obj);
        } catch (IllegalAccessException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
          ps.setString(i, "");
        } catch (InvocationTargetException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
          ps.setString(i, "");
        } catch (NoSuchMethodException e1) {
          // TODO Auto-generated catch block
          //e1.printStackTrace();
          ps.setString(i, "");
        }
        //				ps.setString(i,null);
      }
    }
  }

  /**
   * 按字段逐个获取dtoOrMap的属性设置PreparedStatement
   * @param ps
   * @param valueFieldNames 更新的字段 字段名必须小写
   * @param keyFieldNames 主键字段（查询用） 字段名必须小写
   * @param dtoOrMap
   * @throws SQLException
   * @author hult 2011-03-01 添加
   */
  protected void setUpdateParams(PreparedStatement ps, String[] valueFieldNames, String[] keyFieldNames, Object dtoOrMap)
    throws SQLException {
    if (valueFieldNames == null || keyFieldNames.length == 0)
      return;
    for (int i = 1, n = valueFieldNames.length; i <= n; i++) {
      try {
        ps.setObject(i, BeanUtils.getProperty(dtoOrMap, valueFieldNames[i - 1]));
      } catch (Exception e) {
        ps.setString(i, null);
      }
    }
    for (int i = valueFieldNames.length + 1, n = valueFieldNames.length + keyFieldNames.length; i <= n; i++) {
      try {
        ps.setObject(i, BeanUtils.getProperty(dtoOrMap, keyFieldNames[i - valueFieldNames.length - 1]));
      } catch (Exception e) {
        ps.setString(i, null);
      }
    }
  }

  /**
   * 获取删除sql语句
   * @param tableName
   * @param keyFields 字段名必须小写
   * @return
   * @author hult 2011-03-01 添加
   */
  private String getDeleteSql(final String tableName, final String[] keyFields) {
    StringBuffer deleteSql = new StringBuffer();

    deleteSql.append("delete from ").append(tableName).append(" where ").append(keyFields[0]).append("=?");
    for (int i = 1, n = keyFields.length; i < n; i++) {
      deleteSql.append(" and ").append(keyFields[i]).append("=?");
    }

    return deleteSql.toString();
  }

  /**
   * 获取更新sql语句
   * @param tableName
   * @param valueFields 值字段 字段名必须小写
   * @param keyFields 主键字段 字段名必须小写
   * @return
   * @author hult 2011-03-01 添加
   */
  private String getUpdateSql(final String tableName, final String[] valueFields, final String[] keyFields) {
    StringBuffer updateSql = new StringBuffer();

    updateSql.append("update ").append(tableName).append(" set ").append(valueFields[0]).append("=?");
    for (int i = 1, n = valueFields.length; i < n; i++) {
      updateSql.append(",").append(valueFields[i]).append("=?");
    }
    updateSql.append(" where ").append(keyFields[0]).append("=?");
    for (int i = 1, n = keyFields.length; i < n; i++) {
      updateSql.append(" and ").append(keyFields[i]).append("=?");
    }
    return updateSql.toString();
  }

  /**
   * 获取插入sql语句
   * @param tableName
   * @param fields 字段名必须小写
   * @return
   * @author hult 2011-03-01 添加
   */
  private String getInserSql(final String tableName, final String[] fields) {
    StringBuffer insertSql = new StringBuffer();
    StringBuffer valuesSql = new StringBuffer(" values(?");
    insertSql.append("insert into ").append(tableName).append("(").append(fields[0]);
    for (int i = 1, n = fields.length; i < n; i++) {
      insertSql.append(",").append(fields[i]);
      valuesSql.append(",?");
    }
    insertSql.append(")");
    valuesSql.append(")");
    return insertSql.append(valuesSql).toString();
  }

  /**
   * 获取表所有的字段
   * @param tableName
   * @return
   * @author hult 2011-03-01 添加
   */
  private String[] getFieldsByTableName(final String tableName) {
    Object result = TableFieldsSQLMap.get(tableName.toUpperCase());
    if (result == null) {
      result = execute(new HibernateCallback() {

        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          Connection conn = session.connection();
          String sql = "select * from " + tableName + " where 1=0 ";
          PreparedStatement ps = conn.prepareStatement(sql);
          ResultSet rs = ps.executeQuery();
          ResultSetMetaData metaData = rs.getMetaData();

          int n = metaData.getColumnCount();
          String[] fieldNames = new String[n];
          // modified by zhoulingling 2011-03-08数组下标从0开始，metaData.getColumnName()下标从1开始
          for (int i = 0; i < n; i++) {
            fieldNames[i] = metaData.getColumnName(i + 1).toLowerCase();
          }
          rs.close();
          ps.close();
          return fieldNames;
        }
      });
      TableFieldsSQLMap.put(tableName.toUpperCase(), result);
    }
    return (String[]) result;
  }

  /**
   * 根据提交的sql和参数信息，批量执行sql语句，sql类型可以为delete,insert,update,返回批量执行所影响的数据条数
   * @param sql
   * @param fieldParams
   * @param dtoOrMapList
   * @return
   */
  public int[] executeBatchBySql(final String sql, final String[] fieldParams, final List dtoOrMapList) {
    Object result = execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Connection conn = session.connection();
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0, n = dtoOrMapList.size(); i < n; i++) {
          setParams(ps, fieldParams, dtoOrMapList.get(i));
          ps.addBatch();
        }
        int[] batch = ps.executeBatch();
        ps.close();
        return batch;
      }

    });
    return (int[]) result;
  }

  /**
   * 解析数据库返回的clob字段类型
   * @param clob
   * @return
   */
  public final static String clob2String(Clob clob) {
    if (clob == null) {
      return null;
    }
    StringBuffer sb = new StringBuffer(65535);//64K 
    Reader clobStream = null;//创建一个输入流对象 
    try {
      clobStream = clob.getCharacterStream();
      char[] b = new char[60000];//每次获取60K 
      int i = 0;
      while ((i = clobStream.read(b)) != -1) {
        sb.append(b, 0, i);
      }
    } catch (Exception ex) {
      sb = null;
    } finally {
      try {
        if (clobStream != null)
          clobStream.close();
      } catch (Exception e) {
      }
    }
    if (sb == null)
      return null;
    else
      return sb.toString();
  }

  /**
   * 解析数据库返回的blob字段类型
   * @param blob
   * @return
   */
  public final static String blob2String(Blob blob) {
    String result = "";
    try {
      if (blob != null) {
        StringBuffer buffer = new StringBuffer();
        InputStream is = null;
        is = blob.getBinaryStream();
        InputStreamReader isr = new InputStreamReader(is);
        if (isr.ready()) {
          Reader reader = new BufferedReader(isr);
          int ch;
          while ((ch = reader.read()) > -1) {
            buffer.append((char) ch);
          }
        }
        isr.close();
        is.close();
        result = buffer.toString();
      }
    } catch (Exception e) {
      System.err.println("error : " + e.getMessage());
      return result;
    }
    return result;
  }

  //  add by gaoqb
  public int changeRgYear(final String rg_code, final String set_year) {
    Object result = execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        PreparedStatement pcye = null;
        PreparedStatement pcrg = null;
        try {
          Connection conn = session.connection();
          pcrg = conn.prepareCall("{call  pf_changeRgCode(?)}");
          pcrg.setString(1, rg_code);
          pcrg.execute();

          pcye = conn.prepareCall("{call initdf_changeSetYear(?)}");
          pcye.setString(1, set_year);
          pcye.execute();
          return new Integer(1);
        } catch (Exception e) {

          e.printStackTrace();
          return new Integer(0);
        } finally {
          pcye.close();
          pcrg.close();

        }

      }

    });
    return ((Integer) result).intValue();
  }

  private Object formatHibernateObj(Object hibernateObj) {
    Class objClass = hibernateObj.getClass();
    Method[] methods = objClass.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      String name = methods[i].getName();
      if (name.equalsIgnoreCase("setLAST_VER")) {
        Method setLastVer = methods[i];
        Object[] param = new Object[1];
        param[0] = formatDateText();
        try {
          setLastVer.invoke(hibernateObj, param);
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else if (name.equalsIgnoreCase("setLATEST_OP_USER")) {
        Method setlastUser = methods[i];
        Object[] param = new Object[1];
        UserInfoContext userInfoContext = SessionUtil.getUserInfoContext();
        if (userInfoContext != null) {
          param[0] = userInfoContext.getUserID();
        }
        try {
          setlastUser.invoke(hibernateObj, param);
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else if (name.equalsIgnoreCase("setLATEST_OP_DATE")) {
        Method setlastOperDate = methods[i];
        Object[] param = new Object[1];
        param[0] = formatDateText();
        try {
          setlastOperDate.invoke(hibernateObj, param);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return hibernateObj;
  }

  /**
   * 取服务器本地日期时间.
   * 
   * @return 格式化后的日期时间字符
   */
  private String formatDateText() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = new Date();
    return dateFormat.format(date);
  }

  /**
   * 执行sql脚本
   */
  public void execSqlFile(final String sqlFilePath) throws Exception {
    Object result = execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        try {
          Connection conn = session.connection();
          ScriptRunner runner = new ScriptRunner(conn);
          //下面配置不要随意更改，否则会出现各种问题  
          runner.setAutoCommit(true);//自动提交  
          runner.setFullLineDelimiter(false);
          runner.setDelimiter(";");////每条命令间的分隔符  
          runner.setSendFullScript(false);
          runner.setStopOnError(false);
          // runner.setLogWriter(null);//设置是否输出日志  
          //如果又多个sql文件，可以写多个runner.runScript(xxx),  
          runner.runScript(new InputStreamReader(new FileInputStream(sqlFilePath), "GBK"));
          conn.close();
        } catch (Exception e) {
        }
        return session;

      }

    });
  }
}
