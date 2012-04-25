package org.mconf.bbb.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.junit.Test;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

public class MeetingsTest extends TestCase {

	private static final String MOBILE_RETURN = "<meetings><meeting><returncode>SUCCESS</returncode><meetingName>Demo</meetingName><meetingID>Demo Meeting2 018</meetingID><internalMeetingID>b8f5f456993911b7bd61946b6f00287f7e51ae1a-1335298185762</internalMeetingID><createTime>1335298185762</createTime><voiceBridge>67632</voiceBridge><attendeePW>AF5YI3RR</attendeePW><moderatorPW>kfhbRDhM</moderatorPW><running>true</running><recording>false</recording><hasBeenForciblyEnded>false</hasBeenForciblyEnded><startTime>1335298188201</startTime><endTime>1335298302668</endTime><participantCount>1</participantCount><maxUsers>20</maxUsers><moderatorCount>1</moderatorCount><listenerCount>0</listenerCount><attendees><attendee><userID>r9qqzlimykxc</userID><fullName>Bot 053</fullName><role>MODERATOR</role><isPresenter>false</isPresenter><hasVideoStream>false</hasVideoStream><videoStreamName></videoStreamName></attendee></attendees><metadata></metadata><messageKey></messageKey><message></message></meeting><meeting><returncode>SUCCESS</returncode><meetingName>Demo Meeting</meetingName><meetingID>Demo Meeting</meetingID><internalMeetingID>183f0bf3a0982a127bdb8161e0c44eb696b3e75c-1335366476889</internalMeetingID><createTime>1335366476889</createTime><voiceBridge>72067</voiceBridge><attendeePW>ap</attendeePW><moderatorPW>mp</moderatorPW><running>true</running><recording>false</recording><hasBeenForciblyEnded>false</hasBeenForciblyEnded><startTime>1335366478250</startTime><endTime>0</endTime><participantCount>1</participantCount><maxUsers>20</maxUsers><moderatorCount>1</moderatorCount><listenerCount>0</listenerCount><attendees><attendee><userID>py5zgkej453s</userID><fullName>teste</fullName><role>MODERATOR</role><isPresenter>true</isPresenter><hasVideoStream>false</hasVideoStream><videoStreamName></videoStreamName></attendee></attendees><metadata></metadata><messageKey></messageKey><message></message></meeting></meetings>";
	private static final String DEFAULT_RETURN = "<response><returncode>SUCCESS</returncode><meetings><meeting><meetingID>Demo Meeting2 018</meetingID><meetingName>Demo</meetingName><createTime>1335298185762</createTime><attendeePW>AF5YI3RR</attendeePW><moderatorPW>kfhbRDhM</moderatorPW><hasBeenForciblyEnded>false</hasBeenForciblyEnded><running>true</running><participantCount>1</participantCount></meeting><meeting><meetingID>Demo Meeting</meetingID><meetingName>Demo Meeting</meetingName><createTime>1335366476889</createTime><attendeePW>ap</attendeePW><moderatorPW>mp</moderatorPW><hasBeenForciblyEnded>false</hasBeenForciblyEnded><running>true</running><participantCount>1</participantCount></meeting></meetings></response>";
	
//	<meetings>
//		<meeting>
//			<returncode>SUCCESS</returncode>
//			<meetingName>English 101</meetingName>
//			<meetingID>English 101</meetingID>
//			<createTime>1312994955454</createTime>
//			<attendeePW>ap</attendeePW>
//			<moderatorPW>mp</moderatorPW>
//			<running>true</running>
//			<hasBeenForciblyEnded>false</hasBeenForciblyEnded>
//			<startTime>1312994958384</startTime>
//			<endTime>0</endTime>
//			<participantCount>1</participantCount>
//			<maxUsers>20</maxUsers>
//			<moderatorCount>1</moderatorCount>
//			<attendees>
//				<attendee>
//					<userID>236</userID>
//					<fullName>fcecagno@gmail.com</fullName>
//					<role>MODERATOR</role>
//				</attendee>
//					<attendee>
//					<userID>237</userID>
//					<fullName>test@gmail.com</fullName>
//					<role>VIEWER</role>
//				</attendee>
//			</attendees>
//			<metadata>
//				<email>fcecagno@gmail.com</email>
//				<description>Test</description>
//				<meetingId>English 101</meetingId>
//			</metadata>
//			<messageKey></messageKey>
//			<message></message>
//		</meeting>
//	</meetings>	
	@Test
	public void testParseCorrect() throws UnsupportedEncodingException, DOMException, ParserConfigurationException, SAXException, IOException, ParseException {
		Meetings meetings = new Meetings();
		meetings.parse("<meetings><meeting><returncode>SUCCESS</returncode><meetingName>English 101</meetingName><meetingID>English 101</meetingID><createTime>1312994955454</createTime><attendeePW>ap</attendeePW><moderatorPW>mp</moderatorPW><running>true</running><hasBeenForciblyEnded>false</hasBeenForciblyEnded><startTime>1312994958384</startTime><endTime>0</endTime><participantCount>1</participantCount><maxUsers>20</maxUsers><moderatorCount>1</moderatorCount><attendees><attendee><userID>236</userID><fullName>fcecagno@gmail.com</fullName><role>MODERATOR</role></attendee><attendee><userID>237</userID><fullName>test@gmail.com</fullName><role>VIEWER</role></attendee></attendees><metadata><email>fcecagno@gmail.com</email><description>Test</description><meetingId>English 101</meetingId></metadata><messageKey></messageKey><message></message></meeting></meetings>");
		assertTrue(meetings.getMeetings().size() == 1);
		
		Meeting meeting = meetings.getMeetings().get(0);
			
		assertTrue(meeting.getReturncode().equals("SUCCESS"));
		assertTrue(meeting.getMeetingName().equals("English 101"));
		assertTrue(meeting.getMeetingID().equals("English 101"));
		assertTrue(meeting.getCreateTime().equals(new Date(new Long("1312994955454"))));
		assertTrue(meeting.getAttendeePW().equals("ap"));
		assertTrue(meeting.getModeratorPW().equals("mp"));
		assertFalse(meeting.isHasBeenForciblyEnded());
		assertTrue(meeting.getStartTime().equals(new Date(new Long("1312994958384"))));
		assertTrue(meeting.getEndTime().equals(new Date(new Long("0"))));
		assertTrue(meeting.getParticipantCount() == 1);
		assertTrue(meeting.getMaxUsers() == 20);
		assertTrue(meeting.getModeratorCount() == 1);
		
		assertTrue(meeting.getAttendees().size() == 2);
		assertTrue(meeting.getAttendees().get(0).getUserID().equals("236"));
		assertTrue(meeting.getAttendees().get(0).getFullName().equals("fcecagno@gmail.com"));
		assertTrue(meeting.getAttendees().get(0).getRole().equals("MODERATOR"));
		assertTrue(meeting.getAttendees().get(1).getUserID().equals("237"));
		assertTrue(meeting.getAttendees().get(1).getFullName().equals("test@gmail.com"));
		assertTrue(meeting.getAttendees().get(1).getRole().equals("VIEWER"));
		
		assertTrue(meeting.getMetadata().getEmail().equals("fcecagno@gmail.com"));
		assertTrue(meeting.getMetadata().getDescription().equals("Test"));
		assertTrue(meeting.getMetadata().getMeetingId().equals("English 101"));
		
		assertTrue(meeting.getMessageKey().isEmpty());
		assertTrue(meeting.getMessage().isEmpty());
	}
	
	@Test
	public void testParseMissingTags() throws UnsupportedEncodingException, DOMException, ParserConfigurationException, SAXException, IOException, ParseException {
		Meetings meetings = new Meetings();
		meetings.parse("<meetings><meeting><returncode>SUCCESS</returncode><meetingID>English 101</meetingID><createTime>1312994955454</createTime><attendeePW>ap</attendeePW><moderatorPW>mp</moderatorPW><running>true</running><hasBeenForciblyEnded>false</hasBeenForciblyEnded><startTime>1312994958384</startTime><endTime>0</endTime><participantCount>1</participantCount><maxUsers>20</maxUsers><moderatorCount>1</moderatorCount><attendees><attendee><userID>236</userID><fullName>fcecagno@gmail.com</fullName><role>MODERATOR</role></attendee><attendee><userID>237</userID><fullName>test@gmail.com</fullName><role>VIEWER</role></attendee></attendees><metadata><email>fcecagno@gmail.com</email><description>Test</description><meetingId>English 101</meetingId></metadata></meeting></meetings>");
		assertTrue(meetings.getMeetings().size() == 1);
		
		Meeting meeting = meetings.getMeetings().get(0);
			
		assertTrue(meeting.getReturncode().equals("SUCCESS"));
//		assertTrue(meeting.getMeetingName().equals("English 101"));
		assertTrue(meeting.getMeetingID().equals("English 101"));
		assertTrue(meeting.getCreateTime().equals(new Date(new Long("1312994955454"))));
		assertTrue(meeting.getAttendeePW().equals("ap"));
		assertTrue(meeting.getModeratorPW().equals("mp"));
		assertFalse(meeting.isHasBeenForciblyEnded());
		assertTrue(meeting.getStartTime().equals(new Date(new Long("1312994958384"))));
		assertTrue(meeting.getEndTime().equals(new Date(new Long("0"))));
		assertTrue(meeting.getParticipantCount() == 1);
		assertTrue(meeting.getMaxUsers() == 20);
		assertTrue(meeting.getModeratorCount() == 1);
		
		assertTrue(meeting.getAttendees().size() == 2);
		assertTrue(meeting.getAttendees().get(0).getUserID().equals("236"));
		assertTrue(meeting.getAttendees().get(0).getFullName().equals("fcecagno@gmail.com"));
		assertTrue(meeting.getAttendees().get(0).getRole().equals("MODERATOR"));
		assertTrue(meeting.getAttendees().get(1).getUserID().equals("237"));
		assertTrue(meeting.getAttendees().get(1).getFullName().equals("test@gmail.com"));
		assertTrue(meeting.getAttendees().get(1).getRole().equals("VIEWER"));
		
		assertTrue(meeting.getMetadata().getEmail().equals("fcecagno@gmail.com"));
		assertTrue(meeting.getMetadata().getDescription().equals("Test"));
		assertTrue(meeting.getMetadata().getMeetingId().equals("English 101"));
		
		assertTrue(meeting.getMessageKey().isEmpty());
		assertTrue(meeting.getMessage().isEmpty());
	}
	
	@Test
	public void testParseNoAttendees() throws UnsupportedEncodingException, DOMException, ParserConfigurationException, SAXException, IOException, ParseException {
		Meetings meetings = new Meetings();
		meetings.parse("<meetings><meeting><returncode>SUCCESS</returncode><meetingName>English 101</meetingName><meetingID>English 101</meetingID><createTime>1313009478450</createTime><attendeePW>ap</attendeePW><moderatorPW>mp</moderatorPW><running>false</running><hasBeenForciblyEnded>true</hasBeenForciblyEnded><startTime>1313009481417</startTime><endTime>1313010108752</endTime><participantCount>0</participantCount><maxUsers>20</maxUsers><moderatorCount>0</moderatorCount><attendees></attendees><metadata><email>ck@test</email><description>ck</description><meetingId>English 101</meetingId></metadata><messageKey></messageKey><message></message></meeting></meetings>");
	}
	
	@Test
	public void testMobileApi() {
		Meetings meetings = new Meetings();
		try {
			assertEquals(meetings.parse(MOBILE_RETURN), JoinServiceBase.E_OK);
			
			Meeting meeting = meetings.getMeetings().get(0);
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testDefaultApi() {
		Meetings meetings = new Meetings();
		try {
			assertEquals(meetings.parse(DEFAULT_RETURN), JoinServiceBase.E_OK);
		} catch (Exception e) {
			fail();
		}
	}
}
