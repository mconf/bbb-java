package org.mconf.bbb.api;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BbbServerConfig {
	
	private ParserConfiguration parser;	
	private static final Logger log = LoggerFactory.getLogger(BbbServerConfig.class);
	
	
	public BbbServerConfig(String configXml) throws Exception
	{
		parser = new ParserConfiguration(configXml);
	}
	
		
	public boolean videoIsPresenterShareOnly()
	{
		String result = parser.getModuleAttribute("VideoconfModule","presenterShareOnly");
		if(result == null || result.equals("false"))
				return false;
		else
			return true;
	}
	
	public boolean videoIsAutoStart()
	{
		String result = parser.getModuleAttribute("VideoconfModule","autoStart");
		if(result == null || result.equals("false"))
				return false;
		else
			return true;
	}
	
	public boolean audioIsAutoJoin()
	{
		String result = parser.getModuleAttribute("PhoneModule","autoJoin");
		if(result == null || result.equals("false"))
				return false;
		else
			return true;
	}		

}
