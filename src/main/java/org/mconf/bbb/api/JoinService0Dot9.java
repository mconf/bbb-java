package org.mconf.bbb.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinService0Dot9 extends JoinServiceBase {
	private static final Logger log = LoggerFactory.getLogger(JoinService0Dot9.class);

	private String salt;

	public void setSalt(String salt) {
		if (!salt.equals(this.salt)) {
			this.salt = salt;
		}
	}
	
	public String getSalt() {
		return this.salt;
	}
	
	@Override
	protected String getCreateMeetingUrl(String meetingID) {
		String action = "create";
		String parameters = "meetingID=" + urlEncode(meetingID);
		return action + "?" + parameters + "&checksum=" + checksum(action + parameters + salt);
	}

	@Override
	protected String getApiPath() {
		return "/bigbluebutton/api/";
	}

	@Override
	protected String getJoinUrl(Meeting meeting, String name, boolean moderator) {
		String action = "join";
		String parameters = "meetingID=" + urlEncode(meeting.getMeetingID())
			+ "&fullName=" + urlEncode(name)
			+ "&password=" + urlEncode(moderator? meeting.getModeratorPW(): meeting.getAttendeePW());
		return action + "?" + parameters + "&checksum=" + checksum(action + parameters + salt);
	}
	
	@Override
	public int join(String meetingID, String name, boolean moderator) {
		return super.join(meetingID, name, moderator);
	}

	@Override
	protected String getLoadUrl() {
		String action = "getMeetings";
		return action + "?checksum=" + checksum(action + salt);
	}
	
	@Override
	public String getVersion() {
		return "0.9";
	}
	
	@Override
	protected int join(String joinUrl) {
		return standardJoin(joinUrl);
	}

	@Override
	public int setServerConfiguration() {
		String configAddress = serverUrl + "/client/conf/config.xml";

		log.debug("Trying to fetch {}", configAddress);
		try {
			serverConfig = new BbbServerConfig(getUrl(configAddress));
		} catch (Exception e) {
			log.error("Couldn't get config.xml");
			return E_CANNOT_GET_CONFIGXML;
		}

		return E_OK;
	}

	@Override
	public void setServer(String serverUrl) {
		Pattern pattern = Pattern.compile("(.*):(\\d*)");
		Matcher matcher = pattern.matcher(serverUrl);
		if (matcher.matches()) {
			this.serverUrl = matcher.group(1);
			this.serverPort = Integer.parseInt(matcher.group(2));
		} else {
			this.serverUrl = serverUrl;
			this.serverPort = 80;
		}
		setServerConfiguration();
	}

	public Map<String, Object> getLockSettings() {
		return serverConfig.getLockSettings();
	}
}
