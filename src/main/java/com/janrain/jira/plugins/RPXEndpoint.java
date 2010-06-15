package com.janrain.jira.plugins;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.opensymphony.user.EntityNotFoundException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.janrain.jira.plugins.lib.RPX;
import com.opensymphony.user.User;
import com.opensymphony.user.UserManager;

import org.w3c.dom.Element;

public class RPXEndpoint implements Filter {

	private final UserManager fUserManager = UserManager.getInstance(); // used to interface with JIRA users
	public void destroy() {
		// TODO Auto-generated method stub

	}
	
	/*
	 * This is called when RPX redirects back to the token_url
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		System.out.println("doFilter");
		
		RPXManager rpxManager = new RPXManager();
		if(!rpxManager.isConfigured()) {
			System.out.println("RPX not configured");
			// return with an error
		}
		//RPX rpx = new RPX("ae9b5f0d67179bb78cea3d14815ca4d4a2b29f3a", "http://ren-sandbox.moldy.janrain.com:8080/");
		RPX rpx = new RPX(rpxManager.getValue("com.janrain.rpx.apiKey"), rpxManager.getValue("com.janrain.rpx.base_url"));
		
		String token = arg0.getParameter("token");
		
		if(token == null) {
			// return with an error
		}
		 
		Element rpxAuthInfo = null;
		 
		try	{
			rpxAuthInfo = rpx.authInfo(token);
		}	 
		catch(RuntimeException runtimeException) {
			// return with an error
			System.out.println("Exception Occured :" + runtimeException.getMessage());
		}
		catch(Exception exception) {
			// return with an error
			System.out.println("Exception Occured :" + exception.getMessage());
		}
		
		String username = rpxManager.getJIRAUsername(rpxAuthInfo);
		if(username == null) {
			// return with an error
		}
		
		/*
		 *  Looks up the user in JIRA and logs them in if they exist.
		 *  NOTE: JIRA usernames are all lowercase and are unique.
		 */
		User user = null;
	   	    
	    try {
	    	user = fUserManager.getUser(username);
	    }
	    catch (EntityNotFoundException e) {
	    	// return with an error
			e.printStackTrace();
		}
	    HttpServletRequest request = (HttpServletRequest) arg0;
	    HttpServletResponse response = (HttpServletResponse) arg1;
	    request.getSession().setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
	    request.getSession().setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);

	    response.sendRedirect("/jira");

	}
	
//	public void rpx_end()
//	{
//		
//	}
	 
	/*
	 * Called before doFilter
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}
