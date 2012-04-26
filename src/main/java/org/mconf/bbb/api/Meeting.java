/*
 * GT-Mconf: Multiconference system for interoperable web and mobile
 * http://www.inf.ufrgs.br/prav/gtmconf
 * PRAV Labs - UFRGS
 * 
 * This file is part of Mconf-Mobile.
 *
 * Mconf-Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mconf-Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mconf-Mobile.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mconf.bbb.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Meeting {
	private String returncode; 
	private String meetingName;
	private String meetingID;
	private String internalMeetingID;
	private Date createTime;
	private int voiceBridge;
	private String attendeePW; 
	private String moderatorPW;
	private boolean running;
	private boolean recording;
	private boolean hasBeenForciblyEnded;
	private Date startTime;
	private Date endTime;
	private int participantCount;
	private int maxUsers;
	private int moderatorCount;
	private int listenerCount;
	private List<Attendee> attendees = new ArrayList<Attendee>();
	private Metadata metadata = new Metadata();
	private String messageKey;
	private String message;

	public Meeting() {
	}

	public boolean parse(Element elementMeeting, boolean check_return_code) {
		returncode = ParserUtils.getNodeValue(elementMeeting, "returncode");
		messageKey = ParserUtils.getNodeValue(elementMeeting, "messageKey");
		message = ParserUtils.getNodeValue(elementMeeting, "message");

		if (check_return_code && !returncode.equals("SUCCESS"))
			return false;
		
		meetingName = ParserUtils.getNodeValue(elementMeeting, "meetingName");
		meetingID = ParserUtils.getNodeValue(elementMeeting, "meetingID");
		internalMeetingID = ParserUtils.getNodeValue(elementMeeting, "internalMeetingID");
		createTime = new Date(Long.parseLong(ParserUtils.getNodeValue(elementMeeting, "createTime", true)));
		voiceBridge = Integer.parseInt(ParserUtils.getNodeValue(elementMeeting, "voiceBridge", true));
		attendeePW = ParserUtils.getNodeValue(elementMeeting, "attendeePW");
		moderatorPW = ParserUtils.getNodeValue(elementMeeting, "moderatorPW");
		running = Boolean.parseBoolean(ParserUtils.getNodeValue(elementMeeting, "running", true));
		recording = Boolean.parseBoolean(ParserUtils.getNodeValue(elementMeeting, "recording", true));
		hasBeenForciblyEnded = Boolean.parseBoolean(ParserUtils.getNodeValue(elementMeeting, "hasBeenForciblyEnded", true));
		try {
			startTime = new Date(Long.parseLong(ParserUtils.getNodeValue(elementMeeting, "startTime", true)));
			endTime = new Date(Long.parseLong(ParserUtils.getNodeValue(elementMeeting, "endTime", true)));
		} catch (Exception e) {

		}
		try {
			startTime = parseDate(ParserUtils.getNodeValue(elementMeeting, "startTime"));
			endTime = parseDate(ParserUtils.getNodeValue(elementMeeting, "endTime"));
		} catch (Exception e) {
			
		}
		participantCount = Integer.parseInt(ParserUtils.getNodeValue(elementMeeting, "participantCount", true));
		maxUsers = Integer.parseInt(ParserUtils.getNodeValue(elementMeeting, "maxUsers", true));
		moderatorCount = Integer.parseInt(ParserUtils.getNodeValue(elementMeeting, "moderatorCount", true));
		listenerCount = Integer.parseInt(ParserUtils.getNodeValue(elementMeeting, "listenerCount", true));
		
		NodeList nodeAttendees = elementMeeting.getElementsByTagName("attendee");
		for (int i = 0; i < nodeAttendees.getLength(); ++i) {
			Attendee attendee = new Attendee();
			if (attendee.parse((Element) nodeAttendees.item(i))) {
				attendees.add(attendee);
			}
		}

		NodeList nodeMetadata = elementMeeting.getElementsByTagName("metadata");
		if (nodeMetadata.getLength() > 0)
			metadata.parse((Element) nodeMetadata.item(0));

		return true;
	}
	
	private Date parseDate(String date) {
		DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.CANADA);
		try {
			date = date.replace(date.substring(20, 24), "");
			return dateFormat.parse(date);
		} catch (Exception e) {
			return new Date();
		}
	}

	public String getReturncode() {
		return returncode;
	}

	public void setReturncode(String returncode) {
		this.returncode = returncode;
	}

	public String getMeetingName() {
		return meetingName;
	}

	public void setMeetingName(String meetingName) {
		this.meetingName = meetingName;
	}

	public String getMeetingID() {
		return meetingID;
	}

	public void setMeetingID(String meetingID) {
		this.meetingID = meetingID;
	}

	public String getInternalMeetingID() {
		return internalMeetingID;
	}

	public void setInternalMeetingID(String internalMeetingID) {
		this.internalMeetingID = internalMeetingID;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getVoiceBridge() {
		return voiceBridge;
	}

	public void setVoiceBridge(int voiceBridge) {
		this.voiceBridge = voiceBridge;
	}

	public String getAttendeePW() {
		return attendeePW;
	}

	public void setAttendeePW(String attendeePW) {
		this.attendeePW = attendeePW;
	}

	public String getModeratorPW() {
		return moderatorPW;
	}

	public void setModeratorPW(String moderatorPW) {
		this.moderatorPW = moderatorPW;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isRecording() {
		return recording;
	}

	public void setRecording(boolean recording) {
		this.recording = recording;
	}

	public boolean isHasBeenForciblyEnded() {
		return hasBeenForciblyEnded;
	}

	public void setHasBeenForciblyEnded(boolean hasBeenForciblyEnded) {
		this.hasBeenForciblyEnded = hasBeenForciblyEnded;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public int getParticipantCount() {
		return participantCount;
	}

	public void setParticipantCount(int participantCount) {
		this.participantCount = participantCount;
	}

	public int getMaxUsers() {
		return maxUsers;
	}

	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}

	public int getModeratorCount() {
		return moderatorCount;
	}

	public void setModeratorCount(int moderatorCount) {
		this.moderatorCount = moderatorCount;
	}

	public int getListenerCount() {
		return listenerCount;
	}

	public void setListenerCount(int listenerCount) {
		this.listenerCount = listenerCount;
	}

	public List<Attendee> getAttendees() {
		return attendees;
	}

	public void setAttendees(List<Attendee> attendees) {
		this.attendees = attendees;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("returncode: ").append(returncode)
				.append("\nmeetingName: ").append(meetingName)
				.append("\nmeetingID: ").append(meetingID)
				.append("\ninternalMeetingID: ").append(internalMeetingID)
				.append("\ncreateTime: ").append(createTime)
				.append("\nvoiceBridge: ").append(voiceBridge)
				.append("\nattendeePW: ").append(attendeePW)
				.append("\nmoderatorPW: ").append(moderatorPW)
				.append("\nrunning: ").append(running).append("\nrecording: ")
				.append(recording).append("\nhasBeenForciblyEnded: ")
				.append(hasBeenForciblyEnded).append("\nstartTime: ")
				.append(startTime).append("\nendTime: ").append(endTime)
				.append("\nparticipantCount: ").append(participantCount)
				.append("\nmaxUsers: ").append(maxUsers)
				.append("\nmoderatorCount: ").append(moderatorCount)
				.append("\nlistenerCount: ").append(listenerCount)
				.append("\nattendees: ").append(attendees)
				.append("\nmetadata: ").append(metadata)
				.append("\nmessageKey: ").append(messageKey)
				.append("\nmessage: ").append(message);
		return builder.toString();
	}
	
}
