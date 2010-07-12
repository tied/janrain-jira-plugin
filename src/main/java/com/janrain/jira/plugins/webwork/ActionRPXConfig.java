package com.janrain.jira.plugins.webwork;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.janrain.jira.plugins.RPXManager;

/**
 * Webwork has an ActionSupport class that includes some commonly
 * encountered methods such as:
 *  doDefault, doValidation, doExecute, 
 *  getErrorMessages, getErrors, getHasErrorMessages, getHasErrors, 
 *  invalidInput, getRedirect
 * JIRA extends that class with JiraActionSupport and then JiraWebActionSupport.
 *
 * See the webwork-jira jar file shipped with JIRA and
 * https://svn.atlassian.com/svn/public/atlassian/vendor/webwork-1.4/trunk/src/main/webwork/action/ActionSupport.java
 */
public class ActionRPXConfig extends JiraWebActionSupport {
	String apiKey = null;
	RPXManager rpxManager = new RPXManager();
	String action = "none";
	
	public ActionRPXConfig() throws Exception 
	{
		System.out.println("ActionRPXConfig");
	}
	
	/**
     * Validate the parameters in the request. The HTML form may or may not
     * have been submitted yet since doValidate() is always called when
     * this Action's .jspa URL is invoked.
     *
     * If an error message is set and no input view exists,
     * then doExecute is not called and the view element named "error" in 
     * atlassian-plugin.xml is used. 
     *
     * If an error message is set and an input view does exist, then
     * the input view is used instead of the error view.
     *
     * The URL displayed in the browser doesn't change for an error,
     * just the view.
     *
     * No exceptions are thrown, instead errors and error messages are set.
     */
	protected void doValidation() 
	{
		System.out.println("doValidation");
		action = "none";

		for (Enumeration e = request.getParameterNames(); e.hasMoreElements(); ) 
		{
            String n = (String)e.nextElement();
            String[] vals = request.getParameterValues(n);
            System.out.println("name " + n + ": " + vals[0]);
        
            if (n.equals("action"))
            {
            	action = vals[0];
            }
            else if (n.equals("clear"))
            {
            	rpxManager.clear();
            }
            else if (n.equals("apiKey"))
            {
            	apiKey = vals[0];
            	apiKey = apiKey.trim();
            	
            	if (apiKey.length() > 0)
            	{
            		try 
            		{
            			rpxManager.configure(apiKey); 
            		} 
            		catch(Exception exception) 
            		{ 
            			addErrorMessage(exception.getLocalizedMessage());
            		}
            	}
            }
            else if (n.equals("providerUrl"))
            {
            	String providerUrl = vals[0].trim();
            	
            	if (providerUrl.length() > 0)
            	{
            		try
            		{
            			new URL(providerUrl);
            			rpxManager.storeKeyValue("providerUrl", providerUrl);
            		}
            		catch (MalformedURLException e1)
            		{
            			addErrorMessage("The provider URL entered was not a valid URL");
            			addErrorMessage(e1.getLocalizedMessage());
            		}
            	}
            }
		}
	}
	
	/**
     * This method is always called when this Action's .jspa URL is
     * invoked if there were no errors in doValidation().
     */
	protected String doExecute() throws Exception 
	{
		System.out.println("doExecute");
	
	    if(action.equals("cancel"))
	    	return SUCCESS;
	    
	    if(action.equals("reconfigure"))
	    	return INPUT;
	    
	    if(rpxManager.isConfigured())
	    	return SUCCESS;
	    
	    return INPUT;
    }
	
	/**
     * Set up default values, if any. If you don't have default
     * values, this is not needed.
     *
     * If you want to have default values in your form's fields when it
     * is loaded, then first call this method (or one with some other
     * such name as doInit) and set the local variables. Then return
     * "input" to use the input form view, and in the form use
     * ${myfirstparameter} to call getMyfirstparameter() to load the
     * local variables.
     */
	public String doDefault() throws Exception {
		System.out.println("doDefault");
		return super.doDefault();
	}
	
	public String getApiKey()
	{
		RPXManager rpxManager = new RPXManager();
		return rpxManager.getValue("apiKey");
	}
	
	public String getProviderUrl()
	{
		RPXManager rpxManager = new RPXManager();
		return rpxManager.getValue("providerUrl");
	}
}
