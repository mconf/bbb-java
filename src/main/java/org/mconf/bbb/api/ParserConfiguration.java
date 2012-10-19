package org.mconf.bbb.api;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ParserConfiguration {
	Document doc;
	
	private static final Logger log = LoggerFactory.getLogger(ParserConfiguration.class);	
	
	
	public ParserConfiguration(String xml) throws Exception
	{			
		log.debug(xml);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		doc = db.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		doc.getDocumentElement().normalize();	
	}
	
	
	public String getNodeAttribute(String tagName, String attributeName)
	{
		try
		{
			Attr attribute = (Attr) doc.getElementsByTagName(tagName).item(0).getAttributes().getNamedItem(attributeName);
			return attribute.getValue();
		}
		catch(Exception e)
		{
			return null;
		}		
	}
	
	
	public String getNodeValue(String tagName)
	{
		Element nodeResponse = (Element) doc.getElementsByTagName("config").item(0);
		return ParserUtils.getNodeValue(nodeResponse, tagName);
	}
	
	
	public String getModuleAttribute(String moduleName, String attributeName)
	{
		try
		{
			Attr attribute = (Attr) getModule(moduleName).getAttributes().getNamedItem(attributeName);
			return attribute.getValue();
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	
	private Node getModule(String name)
	{
		for(int i = 0 ; i < getModules().getLength() ; i++)
		{
			Attr moduleName = (Attr) getModules().item(i).getAttributes().getNamedItem("name");
			if(moduleName.getValue().equals(name))
				return getModules().item(i);
		}
		
		return null;
	}
	
	private NodeList getModules()
	{
		return doc.getElementsByTagName("module");		
	}
		
	
}
