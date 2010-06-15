package com.janrain.jira.plugins;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.opensymphony.user.UserManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class RPXManager {

	public RPXManager() {}
	
	/*
	 * To avoid key name collisions, keys should follow the form "com.janrain.rpx.xxx"
	 */
	public void storeKeyValue(String key, String value) 
	{
		PropertySet PS = PropertiesManager.getInstance().getPropertySet();
		key = "com.janrain.rpx." + key;
		try { PS.remove(key); } catch (Exception e) {} // you can't overwrite pre-existing keys
		PS.setText(key,value);
	}
	
	public String getValue(String key) {
		PropertySet PS = PropertiesManager.getInstance().getPropertySet();
		String str = null;
		key = "com.janrain.rpx." + key;
		
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
		return ((getValue("apiKey") != null) && (getValue("base_url") != null));
	}
	
	public void configure(String apiKey)
	{
		System.out.println("Storing the apiKey: " + apiKey);
		//if(!isConfigured())
			storeKeyValue("apiKey", apiKey);
		//else
		//	apiKey = getValue("apiKey");
		
		String data = "apiKey=" + apiKey + "&format=xml";

		System.out.println("Stored");
		System.out.println("Calling plugin/lookup_rp");
		
        try 
        {

            URL url = new URL("https://rpxnow.com/plugin/lookup_rp?" + data);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            //conn.setDoOutput(true);
            conn.connect();
            //OutputStreamWriter osw = new OutputStreamWriter(
            //    conn.getOutputStream(), "UTF-8");
//            osw.write(data);
//            osw.close();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringElementContentWhitespace(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(conn.getInputStream());
            Element response = (Element)doc.getFirstChild();
            
            System.out.println("Called");
            System.out.println(response.toString());
        } 
        catch (MalformedURLException e) 
        {
            throw new RuntimeException("Unexpected URL error", e);
        } 
        catch (IOException e) 
        {
        	// TODO: This is where non-existent RPs go
            throw new RuntimeException("Unexpected IO error", e);
        }
        catch (ParserConfigurationException e) 
        {
            throw new RuntimeException("Unexpected XML error", e);
        } 
        catch (SAXException e) 
        {
            throw new RuntimeException("Unexpected XML error", e);
        }

	}
	
}
