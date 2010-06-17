package com.janrain.jira.plugins.webwork;

import java.util.Enumeration;

import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.opensymphony.module.propertyset.PropertySet;
import com.janrain.jira.plugins.*;

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
	
	
	public ActionRPXConfig() throws Exception {
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
		//if(request.getParameterValues())
		for (Enumeration e =  request.getParameterNames(); e.hasMoreElements() ;) 
		{
            String n = (String)e.nextElement();
            String[] vals = request.getParameterValues(n);
            System.out.println("name " + n + ": " + vals[0]);
        
            if (n.equals("apiKey"))
            {
            	apiKey = vals[0];
            }
		}
		
//		addErrorMessage("An error occurred");
	}
	
	/**
     * This method is always called when this Action's .jspa URL is
     * invoked if there were no errors in doValidation().
     */
	protected String doExecute() throws Exception 
	{
		System.out.println("doExecute");
		
		RPXManager rpxManager = new RPXManager();
//		try 
//		{
			System.out.println(rpxManager.configure(apiKey)); 
//		} 
//		catch(Exception e) 
//			{ return INPUT; } 
		
		PropertySet PS = PropertiesManager.getInstance().getPropertySet();
	    System.out.println(PS.getKeys());
	    System.out.println(PS.getText("com.janrain.rpx.apiKey"));
	    System.out.println(PS.getText("com.janrain.rpx.base_url"));
		
	    return INPUT;
//		return SUCCESS;
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
		//return INPUT;
		return super.doDefault();
	}
}