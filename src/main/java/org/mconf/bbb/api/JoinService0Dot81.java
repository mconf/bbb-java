package org.mconf.bbb.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinService0Dot81 extends JoinServiceBase {
	private static final Logger log = LoggerFactory.getLogger(JoinService0Dot8.class);

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
	public int join(String meetingID, String name, boolean moderator) { //.
		return super.join(meetingID, name, moderator);
	}

	@Override
	protected String getLoadUrl() {
		String action = "getMeetings";
		return action + "?checksum=" + checksum(action + salt);
	}
	
	@Override
	public String getVersion() {
		return "0.81";
	}
	
	@Override
	protected int join(String joinUrl) {
		return standardJoin(joinUrl);
	}
}
