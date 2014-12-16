package org.mconf.bbb.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserUtils {

	private static final Logger log = LoggerFactory.getLogger(ParserUtils.class);

	static public String getNodeValue(Element element, String tagName) {
		NodeList list = element.getElementsByTagName(tagName);
		if (list != null
				&& list.getLength() > 0
				&& list.item(0) != null
				&& list.item(0).getFirstChild() != null) {
			return list.item(0).getFirstChild().getNodeValue();
		} else
			return "";
	}
	
	static public String getNodeValue(Element element, String tagName, boolean numeric) {
		String result = getNodeValue(element, tagName);
		if (result.length() == 0 && numeric)
			return "0";
		return result;
	}

	static public String getNodeValueFromResponse(String response, String tagName) {
		Element nodeResponse = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new ByteArrayInputStream(response.getBytes("UTF-8")));
			doc.getDocumentElement().normalize();
			nodeResponse = (Element) doc.getElementsByTagName("response").item(0);
		} catch (UnsupportedEncodingException uee) {
			log.error(uee.toString());
		} catch (SAXException saxe) {
			log.error(saxe.toString());
		} catch (IOException ioe) {
			log.error(ioe.toString());
		} catch (ParserConfigurationException pce) {
			log.error(pce.toString());
		}
		if (nodeResponse != null) return getNodeValue(nodeResponse, tagName);
		else return "";
	}
	
}
