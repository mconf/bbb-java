package org.mconf.bbb.api;

import java.util.HashMap;
import java.util.Map;

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

	public Map<String, Object> getLockSettings()
	{
		Map<String, Object> lockSettings = new HashMap<String, Object>();

		lockSettings.put("disableCam", Boolean.valueOf(parser.getNodeAttribute("lock", "disableCamForLockedUsers")));
		lockSettings.put("disableMic", Boolean.valueOf(parser.getNodeAttribute("lock", "disableMicForLockedUsers")));
		lockSettings.put("disablePrivateChat", Boolean.valueOf(parser.getNodeAttribute("lock", "disablePrivateChatForLockedUsers")));
		lockSettings.put("disablePublicChat", Boolean.valueOf(parser.getNodeAttribute("lock", "disablePublicChatForLockedUsers")));
		lockSettings.put("lockedLayout", Boolean.valueOf(parser.getNodeAttribute("lock", "lockLayoutForLockedUsers")));

		return lockSettings;
	}

	public boolean getLockOnStart()
	{
		String lockOnStart = parser.getNodeAttribute("meeting", "lockOnStart");
		if (lockOnStart != null) {
			return Boolean.parseBoolean(lockOnStart);
		} else return false;
	}

	public boolean getMuteOnStart()
	{
		String muteOnStart = parser.getNodeAttribute("meeting", "muteOnStart");
		if (muteOnStart != null) {
			return Boolean.parseBoolean(muteOnStart);
		} else return false;
	}
}