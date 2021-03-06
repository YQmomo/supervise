package org.gov.df.supervice.util;

import gov.df.fap.util.xml.XMLData;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.beanutils.BeanUtilsBean;

import com.alibaba.com.caucho.hessian.io.HessianInput;
import com.alibaba.com.caucho.hessian.io.HessianOutput;

/**
 * <p>Title: 网络数据传递对象，普通显示用和传递用</p>
 * <p>Description:可以将要传递的Map和dto进行转换后传递可以直接使用</p>
 * <p>Copyright: Copyright (c) 2010 </p>
 * <p>Company: 北京用友政务软件有限公司</p>
 * @Created on 2010-4-14
 * @authory hult
 * @version 1.0
 */
public class TableData implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -6328745432907674768L;

  public static final boolean isZip = false;

  public static TableData NULLTableData = new TableData();

  /**
   * 标题数据用来记录，键名 
   */
  private transient Object[] titles = null;

  /**
   * 存储原有数据对象的类名在传递之后恢复使用
   */
  private Class clazz = null;

  /**
   * 序列化使用，后台传递给前台默认构造
   *
   */
  private transient ByteArrayOutputStream byteOut = null;

  /**
   * 序列化使用，后台传递给前台默认构造,最后读出数据，根据isZip判断是否是压缩的数据流
   */
  protected byte[] byteData = null;

  /**
   * 压缩数据流传输，在完成时需要调用finish
   */
  private transient DeflaterOutputStream deflaterStream = null;

  /**
   * 通过HessianOutput输出对象流
   */
  private transient HessianOutput hessianOutput = null;

  /**
   * 数据行数
   */
  private int size = 0;

  /**
   * 通过HessianInput获取最终数据列表
   */
  private transient HessianInput hessianInput = null;

  /**
   * 属性获取设置BeanUtilsBean
   */
  private transient BeanUtilsBean beanUtilsBean = null;

  /**
   * 私有不能缺少用于传递后的序列化
   *
   */
  private TableData() {
  }

  /**
   * 构造TableData对象，利于前后台数据传输。
   * @param titles 每个对象包含的属性
   */
  public TableData(Object[] titles) {
    this(titles, null);
  }

  /**
   * 构造TableData对象，利于前后台数据传输。
   * @param titles 每个对象包含的属性
   * @param clz 对象的类型
   */
  public TableData(Object[] titles, Class clz) {

    this.titles = titles;
    this.clazz = clz == null ? XMLData.class : clz;

  }

  /**
   * 构造TableData对象，利于前后台数据传输。
   * @param titles 每个对象包含的属性
   * @param clz 对象的类型
   * @param rs 查询到的数据集
   * @throws SQLException 
   */
  public TableData(Object[] titles, Class clz, ResultSet rs) throws SQLException {

    this.titles = titles;
    this.clazz = clz == null ? XMLData.class : clz;
    this.addResult(rs);
  }

  /**
   * 设置数据类型
   * @param clz
   */
  public void setClazz(Class clz) {
    this.clazz = clz;
  }

  /**
   * 列表中对象的属性
   * @return
   */
  public Class getClazz() {
    return clazz;
  }

  /**
   * 取得表头字段
   * @return
   */
  public Object[] getTitle() {
    return titles;
  }

  /**
   * 将保存的数据转换成XMLData的列表形式
   * @return
   */
  private List toMapList(List dataList) {
    for (int i = 0; i < size; i++) {
      Object[] data;
      try {
        data = (Object[]) hessianInput.readObject();
      } catch (Exception e) {
        throw new BaseException(e);
      }
      dataList.add(this.getDataMap(data));
    }
    return dataList;
  }

  /**
   * 根据clazz记录的类信息将保存的数据信息
   * @return
   */
  public List toDataList() {
    LinkedList link = new LinkedList();
    if (size == 0) {
      return link;
    }
    beanUtilsBean = BeanUtilsBean.getInstance();
    ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
    if (isZip) {
      InflaterInputStream inflater = new InflaterInputStream(bais);
     
      hessianInput = new HessianInput(inflater);
    } else {
      hessianInput = new HessianInput(bais);
    }

    try {
      titles = (Object[]) hessianInput.readObject();
    } catch (Exception e2) {
      throw new BaseException(e2);
    }

    if (Map.class.isAssignableFrom(clazz)) {
      return toMapList(link);
    }

    for (int i = 0; i < size; i++) {
      Object[] data;
      try {
        data = (Object[]) hessianInput.readObject();
      } catch (Exception e1) {
        throw new BaseException(e1);
      }
      Object oriData = null;
      try {
        oriData = this.clazz.newInstance();
        for (int j = 0, n = titles.length; j < n; j++) {
          try {
            beanUtilsBean.copyProperty(oriData, (String) titles[j], data[j]);
          } catch (Exception e) {
            //逐个属性设置，不能设置的屏蔽异常保证能设置的设置
          }
        }
      } catch (Exception e) {
        throw new BaseException(e);
      }finally{
    	  if(null!=hessianInput){
    		  hessianInput.close();
    	  }
    	  try {
			if(null!=bais){
				bais.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
      link.add(oriData);
    }

    return link;
  }

  /**
   * 将一个数组根据头表头信息组装成一个Map对象
   * @param data
   * @return
   */
  private Map getDataMap(Object[] data) {
    XMLData xmlData = new XMLData();
    for (int i = 0, n = this.titles.length; i < n; i++) {
      if (data[i] != null && !"".equals(data[i])) {
        xmlData.put(titles[i], data[i]);
      }
    }
    return xmlData;
  }

  /**
   * 根据数据自动设置表头适应全字段传递情况
   * @param map
   */
  private void setTitleByMap(Map map) {
    this.titles = new String[map.size()];
    Iterator iterator = map.keySet().iterator();
    int i = 0;
    while (iterator.hasNext()) {
      titles[i] = iterator.next();
      i++;
    }
  }

  /**
   * 根据数据自动设置ResultSet适应全数据集传递
   * @param map
   * @throws SQLException 
   */
  private void setTitleByRs(ResultSet rs) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    this.titles = new String[rsmd.getColumnCount()];
    for (int i = 0, n = titles.length; i < n; i++) {
      titles[i] = rsmd.getColumnName(i + 1).toLowerCase();
    }
  }

  private void setTitleByDto(Object dto) {
    ArrayList prop = new ArrayList();
    PropertyDescriptor[] descriptors = this.beanUtilsBean.getPropertyUtils().getPropertyDescriptors(dto);

    for (int i = 0; i < descriptors.length; ++i) {
      if (descriptors[i].getReadMethod() != null) {
        prop.add(descriptors[i].getName());
      }
    }
    this.titles = prop.toArray();
  }

  /**
   * 往列表中加入一个新的Map对象
   * @param map
   */
  public void addMap(Map map) {
    if (titles == null) {
      this.setTitleByMap(map);
    }
    Object[] dataObj = new Object[titles.length];
    for (int i = 0, n = titles.length; i < n; i++) {
      dataObj[i] = map.get(titles[i]);
    }
    this.put(dataObj);

  }

  /**
   * 往列表中加入一个普通对象
   * @param dto
   */
  private void add(Object dto) {
    if (dto instanceof Map) {
      addMap((Map) dto);
      return;
    }
    if (this.titles == null) {
      setTitleByDto(dto);
    }
    Object[] dataObj = new Object[titles.length];
    for (int i = 0, n = titles.length; i < n; i++) {
      try {
        dataObj[i] = beanUtilsBean.getProperty(dto, (String) titles[i]);
      } catch (Exception e) {
        //设置属性没有对应属性的就忽略
      }
    }
    this.put(dataObj);
  }

  /**
   * 往列表中加入一个查询的数据集
   * @param dto
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   * @throws SQLException 
   */
  public void addResult(ResultSet rs) throws SQLException {
    if (titles == null) {
      //直接将全部数据取出
      this.setTitleByRs(rs);
      int n = titles.length;
      while (rs.next()) {
        Object[] dataObj = new Object[n];
        for (int i = 0; i < n; i++) {
          try {
            dataObj[i] = rs.getObject(i + 1);
          } catch (SQLException e) {

          }
        }
        this.put(dataObj);
      }

    } else {
      int n = titles.length;
      while (rs.next()) {
        Object[] dataObj = new Object[n];
        for (int i = 0; i < n; i++) {
          try {
            dataObj[i] = rs.getObject((String) titles[i]);
            //屏蔽部分没有的字段
          } catch (SQLException e) {

          }
        }
        this.put(dataObj);
      }
    }

    finish();
  }

  /**
   * 往列表中增加Map列表。
   * @param dataList
   */
  public void addDataByMapList(List dataList) {

    Iterator iterator = dataList.iterator();
    while (iterator.hasNext()) {
      addMap((Map) (iterator.next()));
    }
    finish();
  }

  /**
   * 往列表中增加普通对象列表。
   * @param dataList
   */
  public void addDataByDataList(List dataList) {
    Iterator iterator = dataList.iterator();
    if (dataList.size() > 0) {
      this.clazz = dataList.get(0).getClass();
    }
    beanUtilsBean = BeanUtilsBean.getInstance();
    while (iterator.hasNext()) {
      try {
        add(iterator.next());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    finish();
  }

  /**
   * 获取查询到数据条数
   * @return
   */
  public int size() {
    return size;
  }

  /**
   * 将数据通过HessianOutput压缩后报错到zlibData中
   * @param objData
   */
  public void put(Object objData) {

    if (size == 0) {

      byteOut = new ByteArrayOutputStream();
      if (isZip) {
        //压缩使用压缩数据流
        deflaterStream = new DeflaterOutputStream(byteOut);
        hessianOutput = new HessianOutput(deflaterStream);
      } else {
        //使用字节流传输
        hessianOutput = new HessianOutput(byteOut);
      }

      try {
        hessianOutput.writeObject(titles);
      } catch (IOException e) {
        throw new BaseException(e);
      }

    }
    try {
      hessianOutput.writeObject(objData);
      size++;
    } catch (IOException e) {
      throw new BaseException(e);
    }

  }

  /**
   * 数据设置完成后使用，不调用，不能正常压缩传递
   *
   */
  private void finish() {
    if (size == 0) {
      this.titles = null;
    } else {
      try {
        if (isZip) {
          this.deflaterStream.finish();
        }
        this.byteData = this.byteOut.toByteArray();

      } catch (IOException e) {
        throw new BaseException(e);
      }
    }
  }
}
