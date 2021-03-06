package gov.df.fap.service.login.filter;

import gov.df.fap.api.fapcommon.IUserSync;
import gov.df.fap.bean.portal.UserInfoDFCommon;
import gov.df.fap.bean.user.UserDTO;
import gov.df.fap.service.util.sessionmanager.SessionUtil;
import gov.df.fap.util.factory.ServiceFactory;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DfCommonFilter extends HttpServlet implements Filter {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private IUserSync iUserSyncManager;

  @Override
  public void init(FilterConfig paramFilterConfig) throws ServletException {
    // TODO Auto-generated method stub

  }

  @Override
  public void doFilter(ServletRequest paramServletRequest, ServletResponse paramServletResponse,
    FilterChain paramFilterChain) throws IOException, ServletException {
    // TODO Auto-generated method stub
    HttpServletRequest httpRequest = (HttpServletRequest) paramServletRequest;
    HttpServletResponse  response = (HttpServletResponse) paramServletResponse;
    String referer=httpRequest.getHeader("Referer")!=null?httpRequest.getHeader("Referer"):"";
    String host=httpRequest.getHeader("Host")!=null?httpRequest.getHeader("Host"):"";
    if(!referer.equals("")&&(!referer.contains(host))){
    	return;
    }    
    String url = httpRequest.getServletPath();
    String tokenid = httpRequest.getParameter("tokenid");    
    if(null != tokenid && !tokenid.equals("") && !UserInfoDFCommon.isTokenidValid(tokenid)){
    	response.setStatus(404);
		response.sendRedirect("/df/login/message.html");
	}    
    if (iUserSyncManager == null) {
        iUserSyncManager = (IUserSync) ServiceFactory.getBean("userCommonService");
    }
    
    if (httpRequest.getParameter("ajax") != null && !"".equals(httpRequest.getParameter("ajax"))) {
      ((HttpServletResponse) paramServletResponse).setHeader("Cache-Control", "no-cache");
      if (url.equals("/df/login/userLogin.do")) {
        RequestDispatcher requestDispatcher = paramServletRequest.getRequestDispatcher("/df/login/userLogin.do");
        requestDispatcher.forward(paramServletRequest, paramServletResponse);
        return;
      }
      UserDTO userdto = null;
      try {
        userdto = (UserDTO) iUserSyncManager.getUser(tokenid);
        if(null==userdto){
        	return;
        }
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return;
      }
      HashMap user_context = new HashMap();
      user_context.put("user_id", userdto.getUser_id());
      user_context.put("user_name", userdto.getUser_name());
      user_context.put("user_code", userdto.getUser_code());
      user_context.put("rg_code", userdto.getRg_code());
      user_context.put("sys_id", "df");
      user_context.put("set_year", String.valueOf(userdto.getSet_year()));
      user_context.put("menu_id", httpRequest.getParameter("svMenuId"));
      user_context.put("role_id", httpRequest.getParameter("svRoleId"));
      user_context.put("role_code", httpRequest.getParameter("svRoleCode"));
      user_context.put("trans_date", httpRequest.getParameter("svTransDate"));
      user_context.put("fiscal_period", httpRequest.getParameter("svTransDate"));
      user_context.put("rg_name", httpRequest.getParameter("svRgName"));
      user_context.put("org_type", userdto.getOrg_type());
      user_context.put("org_code", userdto.getOrg_code());//监管系统专员办id(oid用org_code代替)
      user_context.put("belong_org", userdto.getBelong_org());//csof监管项目处室id(dep_id用belong_org代替)
      SessionUtil.getUserInfoContext().setContext(user_context);
    }else if(url.contains("html") && !url.contains("message.html") && !url.contains("csoflogin.html") 
    		&& !url.contains("login.html")){
        if(null==SessionUtil.getUserInfoContext().getContext()
        		||(null==SessionUtil.getUserInfoContext().getContext().get("user_id")
        		||SessionUtil.getUserInfoContext().getContext().get("user_id").toString().equals(""))){
        	
        	if(null == tokenid|| tokenid.equals("")||null ==iUserSyncManager.getUser(tokenid)){
            	response.setStatus(404);
        		response.sendRedirect("/df/login/message.html");
        	}
        }
     
    }
    paramFilterChain.doFilter(paramServletRequest, paramServletResponse);
  }
  

}
