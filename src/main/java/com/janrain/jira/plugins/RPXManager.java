package com.janrain.jira.plugins;

import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.opensymphony.user.UserManager;

import org.w3c.dom.Element;

import javax.servlet.http.HttpServletResponse;

public class RPXManager {

	public RPXManager() {}
	
	/*
	 * To avoid key name collisions, keys should follow the form "com.janrain.rpx.xxx"
	 */
	public void storeKeyValue(String key, String value) {
		PropertySet PS = PropertiesManager.getInstance().getPropertySet();
		try { PS.remove(key); } catch (Exception e) {} // you can't overwrite pre-existing keys
		PS.setText(key,value);
	}
	
	public String getValue(String key) {
		PropertySet PS = PropertiesManager.getInstance().getPropertySet();
		String str = null;
		try  {
			str = PS.getText(key);
		} catch (PropertyException e) {
			str = null;
		}
		return str;
	}
	
	/*
	 * Parses the XML auth_info response for a mapped username. If none exists, returns null
	 */
	public String getJIRAUsername(Element authInfo) {
		String str = null;
		/*
		 * Here you would parse out the mapped username from the auth_info payload.
		 * If no mapping exists, return null.
		 */
		return str;
	}
	
	/*
	 * Ensure that the plugin has the values it needs.
	 */
	public boolean isConfigured() {
		return ((getValue("com.janrain.rpx.apiKey") != null) && (getValue("com.janrain.rpx.base_url") != null));
	}
	
}
