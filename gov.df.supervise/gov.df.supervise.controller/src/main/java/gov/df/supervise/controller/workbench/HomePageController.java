/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2017 北京用友政务软件有限公司
 * </p>
 * <p>
 * Company: 北京用友政务软件有限公司
 * </p>
 * <p>
 * CreateData: 2017-8-25上午9:48:15
 * </p>
 * 
 * @author songlr3
 * @version 1.0
 */
package gov.df.supervise.controller.workbench;

import gov.df.supervise.api.workbench.HomePageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/HomePage")
public class HomePageController {

  @Autowired
  private HomePageService homePageService;

  /**
   * 待办事项
   */
  @RequestMapping(method = RequestMethod.GET, value = "/getDpByDpId.do")
  public @ResponseBody
  Map<String, Object> selectToDo(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> result = new HashMap<String, Object>();
    List<?> data = new ArrayList<Object>();
    String userId = request.getParameter("userId");
    try {
      data = homePageService.selectToDo(userId);
      result.put("data", data);
      result.put("errorCode", 0);
    } catch (Exception e) {
      result.put("errorCode", -1);
      result.put("errorMsg", "查询失败");
      result.put("message", "异常"); // zfn 未来改成标准消息异常
    }
    return result;
  }

  /**
   * 最近公告列表显示
   */
  @RequestMapping(method = RequestMethod.GET, value = "/selectGongaoTitle.do")
  public @ResponseBody
  Map<String, Object> selectGongaoTitle(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> result = new HashMap<String, Object>();
    List<?> data = new ArrayList<Object>();
    try {
      data = homePageService.selectGongaoTitle();
      result.put("data", data);
      result.put("errorCode", 0);
    } catch (Exception e) {
      result.put("errorCode", -1);
      result.put("errorMsg", "查询失败");
      result.put("message", "异常"); // zfn 未来改成标准消息异常
    }
    return result;
  }

  /**
   * 通过Id查询最近公告信息
   */
  @RequestMapping(method = RequestMethod.GET, value = "/selectGongaoById.do")
  public @ResponseBody
  Map<String, Object> selectGongaoById(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> result = new HashMap<String, Object>();
    List<?> data = new ArrayList<Object>();
    String Id = request.getParameter("groupId");
    try {
      data = homePageService.selectGongaoById(Id);
      result.put("data", data);
      result.put("errorCode", 0);
    } catch (Exception e) {
      result.put("errorCode", -1);
      result.put("errorMsg", "查询失败");
      result.put("message", "异常"); // zfn 未来改成标准消息异常
    }
    return result;
  }

  /**
   * 公告信息点击更多返回报表系统公告链接
   */
  @RequestMapping(method = RequestMethod.POST, value = "/selectGongaoURL.do")
  public @ResponseBody
  Map<String, Object> selectGongaoURL(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> result = new HashMap<String, Object>();
    String URL;
    String CSOF_REPORT_IP_URL = "";
    String id = ("id=" + "gaoxb");
    String pw = ("&pw=" + "gaoxb");
    String target = ("&target=bullet");
    try {
      URL = CSOF_REPORT_IP_URL + id + pw + target;
      result.put("data", URL);
      result.put("errorCode", 0);
    } catch (Exception e) {
      result.put("errorCode", -1);
      result.put("errorMsg", "查询失败");
      result.put("message", "异常"); // zfn 未来改成标准消息异常
    }
    return result;
  }

  //查询公告
  @RequestMapping(value = "/getBulletin.do", method = RequestMethod.GET)
  @ResponseBody
  public Map<String, Object> getBulletin(HttpServletRequest request) throws Exception {
    Map<String, Object> result = new HashMap<String, Object>();
    try {
      List data = homePageService.getBulletin();
      result.put("errorCode", 0);
      result.put("data", data);
    } catch (Exception e) {
      result.put("errorCode", -1);
      result.put("errorMsg", "查询数据失败");
      result.put("message", "异常"); // zfn 未来改成标准消息异常
    }
    return result;
  }
}
