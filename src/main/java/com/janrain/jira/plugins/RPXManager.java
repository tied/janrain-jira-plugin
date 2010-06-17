package com.janrain.jira.plugins;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
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
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
 
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;




@Path("/startUrl")
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
	
	@GET
	@AnonymousAllowed
	@Produces({MediaType.TEXT_PLAIN})
	public String getStartUrl()
	{
		PropertySet PS = PropertiesManager.getInstance().getPropertySet();
		String str = null;
		
		try  
		{
			str = PS.getText("com.janrain.rpx.base_url");
		} 
		catch (PropertyException e) 
		{
			str = null;
		}
		return str + "/openid/start?token_url=http%3A%2F%2Flocalhost%3A2990%2Fjira%2Frpx_end";
	}

	
	/*
	 * Parses the XML auth_info response for a mapped username. If none exists, returns null
	 */
	public String getJIRAUsername(Element authInfo) 
	{
		String str = null;
		/*
		 * Here you would parse out the mapped username from the auth_info payload.
		 * If no mapping exists, return null.
		 */
		
		try
		{
			Element profile = (Element)authInfo.getElementsByTagName("profile").item(0);
			
			System.out.println("Profile length: " + profile.getChildNodes().getLength());
			if(profile.getElementsByTagName("primaryKey").getLength() == 0)
			{
				System.out.println("Primary Key not there");
			}
			str = profile.getElementsByTagName("primaryKey").item(0).getTextContent();
		}
		catch (Exception e)
		{
		}
		
		return str;
	}
	
	/*
	 * Ensure that the plugin has the values it needs.
	 */
	public boolean isConfigured() {
		return ((getValue("apiKey") != null) && (getValue("base_url") != null));
	}
	
	public String configure(String apiKey)
	{
		System.out.println("Storing the apiKey: " + apiKey);
		//if(!isConfigured())
			storeKeyValue("apiKey", apiKey);
		//else
		//	apiKey = getValue("apiKey");
		
		String data = "apiKey=" + apiKey + "&format=xml";

		System.out.println("Stored");
		System.out.println("Calling plugin/lookup_rp: " + "https://rpxnow.com/plugin/lookup_rp?" + data);
		
        try 
        {

            URL url = new URL("https://rpxnow.com/plugin/lookup_rp?" + data);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            //conn.setDoOutput(true);
            conn.connect();

            System.out.println("Called");
            
            System.out.println("Parsing response");
          
//            OutputStreamWriter osw = new OutputStreamWriter(
//                conn.getOutputStream(), "UTF-8");
//            osw.write(data);
//            osw.close();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringElementContentWhitespace(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(conn.getInputStream());

            System.out.println(doc.getChildNodes().getLength());
            
            Element pluginResponse = (Element)doc.getFirstChild();
            
            System.out.println(pluginResponse);
            
            if (pluginResponse.getElementsByTagName("realmScheme").getLength() == 0 ||
            	pluginResponse.getElementsByTagName("realm").getLength() == 0) 
            {
                //throw new RuntimeException("Unexpected API error");
            	return "Unexpected API error";
            }
            
            String base_url;
            
            try
            {
            	base_url = pluginResponse.getElementsByTagName("realmScheme").item(0).getTextContent() + "://" + 
            					  pluginResponse.getElementsByTagName("realm").item(0).getTextContent();
            }
            catch (DOMException e)
            {
                //throw new RuntimeException("Unexpected XML error", e);
            	return "Unexpected XML error";
            }
            
            System.out.println("Parsed");
            System.out.println("Storing the base_url: " + base_url);

            storeKeyValue("base_url", base_url);
   			
    		System.out.println("Stored");
        } 
        catch (MalformedURLException e) 
        {
            //throw new RuntimeException("Unexpected URL error", e);
        	return "Unexpected URL error";
        } 
        catch (IOException e) 
        {
        	// TODO: This is where non-existent RPs go
            //throw new RuntimeException("Unexpected IO error", e);
     
        	return "Unexpected IO error";
        }
        catch (ParserConfigurationException e) 
        {
            //throw new RuntimeException("Unexpected XML error", e);
        	return "Unexpected XML error";
        }
        catch (SAXException e) 
        {
            //throw new RuntimeException("Unexpected XML error", e);
        	return "Unexpected XML error";
        }
     
        return "Success";
	}
}