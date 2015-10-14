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
	protected String getCreateMeetingUrl(String meetingID, boolean record) {
		String action = "create";
		String parameters = "meetingID=" + urlEncode(meetingID) + "&name=" + urlEncode(meetingID) + "&record=" + record;
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
}
