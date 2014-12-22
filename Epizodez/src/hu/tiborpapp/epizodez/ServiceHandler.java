package hu.tiborpapp.epizodez;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ServiceHandler {

	static String response = null;
	public final static int GET = 1;
	public final static int POST = 2;

	public ServiceHandler() {
	}

	/**
	 * 
	 * @param url - URL to make request
	 * @param method - HTTP request method
	 * @return
	 */
	public String makeServiceCall(String url, int method) {
		try {
			// http client
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpEntity httpEntity = null;
			HttpResponse httpResponse = null;
			
			// Checking http request method type
			if (method == POST) {
				HttpPost httpPost = new HttpPost(url);
				httpResponse = httpClient.execute(httpPost);

			} else if (method == GET) {
				
				HttpGet httpGet = new HttpGet(url);
				httpResponse = httpClient.execute(httpGet);
			}
			httpEntity = httpResponse.getEntity();
			response = EntityUtils.toString(httpEntity);

		} catch (Exception e) {
			e.printStackTrace();
		} 		
		return response;
	}

	/**
	 * Getting DOM element
	 * @param xml
	 * @return
	 */
	public Document getDomElement(String xml){
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
		        is.setCharacterStream(new StringReader(xml));
		        doc = db.parse(is); 

			} catch (Exception e){
				e.printStackTrace();
				return null;
			}
	        return doc;
	}
	
	 /**
	  * Getting node value
	  * @param element
	  * @return
	  */
	 public final String getElementValue(Node element) {
	     Node child;
	     if( element != null){
	         if (element.hasChildNodes()){
	             for(child = element.getFirstChild(); child != null; child = child.getNextSibling()){
	                 if(child.getNodeType() == Node.TEXT_NODE){
	                     return child.getNodeValue();
	                 }
	             }
	         }
	     }
	     return "";
	 }
	 
	/**
	 * Getting element value
	 * @param item
	 * @param str
	 * @return
	 */
	 public String getValue(Element item, String str) {		
			NodeList n = item.getElementsByTagName(str);
			return this.getElementValue(n.item(0));
		}
}
