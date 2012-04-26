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

import org.w3c.dom.Element;

public class Attendee {
	private String userID;
	private String fullName;
	private String role;
	private boolean isPresenter;
	private boolean hasVideoStream;
	private String videoStreamName;

	public boolean parse(Element elementAttendee) {
		userID = ParserUtils.getNodeValue(elementAttendee, "userID");
		fullName = ParserUtils.getNodeValue(elementAttendee, "fullName");
		role = ParserUtils.getNodeValue(elementAttendee, "role");
		isPresenter = Boolean.parseBoolean(ParserUtils.getNodeValue(elementAttendee, "isPresenter", true));
		hasVideoStream = Boolean.parseBoolean(ParserUtils.getNodeValue(elementAttendee, "hasVideoStream", true));
		videoStreamName = ParserUtils.getNodeValue(elementAttendee, "videoStreamName");
		
		return true;
	}
	
	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isPresenter() {
		return isPresenter;
	}

	public void setPresenter(boolean isPresenter) {
		this.isPresenter = isPresenter;
	}

	public boolean isHasVideoStream() {
		return hasVideoStream;
	}

	public void setHasVideoStream(boolean hasVideoStream) {
		this.hasVideoStream = hasVideoStream;
	}

	public String getVideoStreamName() {
		return videoStreamName;
	}

	public void setVideoStreamName(String videoStreamName) {
		this.videoStreamName = videoStreamName;
	}

	@Override
	public String toString() {
		return "[fullName=" + fullName + ", role=" + role
				+ ", userID=" + userID + "]";
	}
}
