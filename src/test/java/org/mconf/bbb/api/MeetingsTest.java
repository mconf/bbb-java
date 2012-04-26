package org.mconf.bbb.api;

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeetingsTest extends TestCase {

	private static final Logger log = LoggerFactory.getLogger(MeetingsTest.class);
	
	private static final String MOBILE_RETURN = 
			"<meetings>" +
				"<meeting>" +
					"<returncode>SUCCESS</returncode>" +
					"<meetingName>Demo</meetingName>" +
					"<meetingID>Demo Meeting2 018</meetingID>" +
					"<internalMeetingID>b8f5f456993911b7bd61946b6f00287f7e51ae1a-1335298185762</internalMeetingID>" +
					"<createTime>1335298185762</createTime>" +
					"<voiceBridge>67632</voiceBridge>" +
					"<attendeePW>AF5YI3RR</attendeePW>" +
					"<moderatorPW>kfhbRDhM</moderatorPW>" +
					"<running>true</running>" +
					"<recording>false</recording>" +
					"<hasBeenForciblyEnded>false</hasBeenForciblyEnded>" +
					"<startTime>1335298188201</startTime>" +
					"<endTime>1335298302668</endTime>" +
					"<participantCount>1</participantCount>" +
					"<maxUsers>20</maxUsers>" +
					"<moderatorCount>1</moderatorCount>" +
					"<listenerCount>0</listenerCount>" +
					"<attendees>" +
						"<attendee>" +
							"<userID>r9qqzlimykxc</userID>" +
							"<fullName>Bot 053</fullName>" +
							"<role>MODERATOR</role>" +
							"<isPresenter>false</isPresenter>" +
							"<hasVideoStream>false</hasVideoStream>" +
							"<videoStreamName></videoStreamName>" +
						"</attendee>" +
					"</attendees>" +
					"<metadata></metadata>" +
					"<messageKey></messageKey>" +
					"<message></message>" +
				"</meeting>" +
			"</meetings>";
	private static final String DEFAULT_RETURN = 
			"<response>" +
				"<returncode>SUCCESS</returncode>" +
				"<meetings>" +
					"<meeting>" +
						"<meetingID>Demo Meeting2 018</meetingID>" +
						"<meetingName>Demo</meetingName>" +
						"<createTime>1335298185762</createTime>" +
						"<attendeePW>AF5YI3RR</attendeePW>" +
						"<moderatorPW>kfhbRDhM</moderatorPW>" +
						"<hasBeenForciblyEnded>false</hasBeenForciblyEnded>" +
						"<running>true</running>" +
						"<participantCount>1</participantCount>" +
					"</meeting>" +
				"</meetings>" +
			"</response>";
	
	@Test
	public void testMobileApi() {
		Meetings meetings = new Meetings();
		try {
			assertEquals(JoinServiceBase.E_OK, meetings.parse(MOBILE_RETURN));
			log.debug(meetings.toString());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testDefaultApi() {
		Meetings meetings = new Meetings();
		try {
			assertEquals(JoinServiceBase.E_OK, meetings.parse(DEFAULT_RETURN));
			log.debug(meetings.toString());
		} catch (Exception e) {
			fail();
		}
	}
}
