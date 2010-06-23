package com.janrain.jira.plugins;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.opensymphony.user.EntityNotFoundException;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.janrain.jira.plugins.lib.RPX;
import com.opensymphony.user.User;
import com.opensymphony.user.UserManager;

//import org.apache.commons.httpclient.Cookie;
import javax.servlet.http.Cookie;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.w3c.dom.Element;


public class RPXEndpoint implements Filter 
{

	private final UserManager fUserManager = UserManager.getInstance(); // used to interface with JIRA users
	public void destroy() 
	{
		// TODO Auto-generated method stub

	}
	
	private void setErrorCookie(HttpServletRequest request, HttpServletResponse response, String value)
	{
	    Cookie[] cookies = request.getCookies();
	    Cookie errorCookie = null;
	    String name = null;
	    
	    System.out.println("Setting engageAuthError cookie to: " + value);
	    
	    if(cookies != null)
	    {
	    	for (int i = 0; i < cookies.length; i++)
	    	{
	    		Cookie cookie = cookies[i];
	    		if (cookie.getName().contains("engageGadgetCookieName"))
	    			name = cookie.getName().substring(0, cookie.getName().indexOf("engageGadgetCookieName"));
	    		
	    		System.out.println("Cookie named: " + cookie.getName());
	    		System.out.println("Cookie domain: " + cookie.getDomain());
	    	}
	    }
	    name += "engageAuthError";
	    
		System.out.println("Cookie name found to be: " + name);

		
		
		
		if (name != null)
			errorCookie = new Cookie(name, value);
		else
			return;
		
		
		errorCookie.setPath("/");
		//errorCookie getPath()    setDomain("/");
		response.addCookie(errorCookie);
	}
	

	
	
	/*
	 * This is called when RPX redirects back to the token_url
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		System.out.println("doFilter");
		
	    HttpServletResponse response = (HttpServletResponse) arg1;
	    HttpServletRequest request = (HttpServletRequest) arg0;
	    
	    
	    
	    
//	    setErrorCookie(response, "authenticationError", "");
	    
	    RPXManager rpxManager = new RPXManager();
				
		if(!rpxManager.isConfigured()) 
		{
			System.out.println("RPX not configured");
			setErrorCookie(request, response,  "RPX Not Configured.");
			response.sendRedirect("/jira");
			return;
		}
		
		System.out.println("Creating RPX instance");
		RPX rpx = new RPX(rpxManager.getValue("apiKey"), rpxManager.getValue("base_url"));
		System.out.println("Created");


		System.out.println("Getting token");
		String token = arg0.getParameter("token");
		System.out.println("Token: " + token);
		
		if(token == null) 
		{
			setErrorCookie(request, response,  "Token not found in response.");
			response.sendRedirect("/jira");
			return;
		}
		 
		Element rpxAuthInfo = null;
		 
		try	
		{

			System.out.println("Calling auth_info");
			rpxAuthInfo = rpx.authInfo(token);
			System.out.println("auth_info response # of elements: " + rpxAuthInfo.getChildNodes().getLength());
		}	 
		catch(RuntimeException runtimeException) 
		{
			// return with an error
			System.out.println("Runtime Exception Occured :");// + runtimeException.getMessage());
			setErrorCookie(request, response,  "Runtime Exception caught during auth_info parsing.");
			response.sendRedirect("/jira");
			return;
		}
		catch(Exception exception) 
		{
			// return with an error
			System.out.println("Exception Occured :" + exception.getMessage());
			setErrorCookie(request, response,  "Exception caught during auth_info parsing.");
			response.sendRedirect("/jira");
			return;
		}

		System.out.println("Getting JIRA user name");
		String username = rpxManager.getJIRAUsername(rpxAuthInfo);
		System.out.println("JIRA user name: " + username);
		
		if(username == null) 
		{
			setErrorCookie(request, response,  "Authenticated user is not mapped to a JIRA account.");
			response.sendRedirect("/jira");
			return;
		}
		
		/*
		 *  Looks up the user in JIRA and logs them in if they exist.
		 *  NOTE: JIRA usernames are all lowercase and are unique.
		 */
		User user = null;
	   	    
	    try 
	    {
			System.out.println("Getting JIRA user");
	    	user = fUserManager.getUser(username);
			System.out.println("JIRA user: " + user);
	    }
	    catch (EntityNotFoundException e) 
	    {
	    	// return with an error
			e.printStackTrace();
			String message = "Authenticated user, " + username + ", not found in JIRA database.";
			setErrorCookie(request, response,  message);
			response.sendRedirect("/jira");
			return;
		}
	    
		System.out.println("Logging user in");
	    //HttpServletRequest request = (HttpServletRequest) arg0;
	    //HttpServletResponse response = (HttpServletResponse) arg1;
	    request.getSession().setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
	    request.getSession().setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
		System.out.println("User logged in");

		System.out.println("Redirecting");
		response.sendRedirect("/jira");

	}
	
	 
	/*
	 * Called before doFilter
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}
