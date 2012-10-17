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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class JoinedMeeting {
	private String returncode;
	private String fullname;
	private String confname;
	private String meetingID;
	private String externUserID;
	private String role;
	private String conference;
	private String room;
	private String voicebridge;
	private String webvoiceconf;
	private String mode;
	private String record;
	private String welcome;
	private String message;
	private String server;
	private String internalUserID;

	public JoinedMeeting() {
		
	}
	
	/*
	 *  	
	 * <response>
	 * 	<returncode>SUCCESS</returncode>
	 * 	<fullname>My name</fullname>
	 * 	<confname>Demo Meeting</confname>
	 * 	<meetingID>Demo Meeting</meetingID>
	 * 	<externUserID>cabfia5gbt9z</externUserID>
	 * 	<role>MODERATOR</role>
	 * 	<conference>19697a6c-e69c-484b-8bf0-cbf3f2542a4a</conference>
	 * 	<room>19697a6c-e69c-484b-8bf0-cbf3f2542a4a</room>
	 * 	<voicebridge>77293</voicebridge>
	 * 	<webvoiceconf>77293</webvoiceconf>
	 * 	<mode>LIVE</mode>
	 * 	<record>false</record>
	 * 	<welcome>&lt;br&gt;Welcome to this BigBlueButton Demo Server.&lt;br&gt;&lt;br&gt;For help using BigBlueButton &lt;a href="event:http://www.bigbluebutton.org/content/videos"&gt;&lt;u&gt;check out these videos&lt;/u&gt;&lt;/a&gt;.&lt;br&gt;&lt;br&gt;</welcome>
	 * </response>
	 */
	public void parse(String str) throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {	
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new ByteArrayInputStream(str.getBytes("UTF-8")));
		doc.getDocumentElement().normalize();

		Element nodeResponse = (Element) doc.getElementsByTagName("response").item(0);
		returncode = ParserUtils.getNodeValue(nodeResponse, "returncode");
		
		if (returncode.equals("SUCCESS")) {		
			fullname = ParserUtils.getNodeValue(nodeResponse, "fullname");
			confname = ParserUtils.getNodeValue(nodeResponse, "confname");
			meetingID = ParserUtils.getNodeValue(nodeResponse, "meetingID");
			externUserID = ParserUtils.getNodeValue(nodeResponse, "externUserID");
			internalUserID = ParserUtils.getNodeValue(nodeResponse, "internalUserID");
			role = ParserUtils.getNodeValue(nodeResponse, "role");
			conference = ParserUtils.getNodeValue(nodeResponse, "conference");
			room = ParserUtils.getNodeValue(nodeResponse, "room");
			voicebridge = ParserUtils.getNodeValue(nodeResponse, "voicebridge");
			webvoiceconf = ParserUtils.getNodeValue(nodeResponse, "webvoiceconf");
			mode = ParserUtils.getNodeValue(nodeResponse, "mode");
			record = ParserUtils.getNodeValue(nodeResponse, "record");
			welcome = ParserUtils.getNodeValue(nodeResponse, "welcome");
			server = ParserUtils.getNodeValue(nodeResponse, "server");
		} else {
			message = ParserUtils.getNodeValue(nodeResponse, "message");
		}
	}

	public String getReturncode() {
		return returncode;
	}

	public String getFullname() {
		return fullname;
	}

	public String getConfname() {
		return confname;
	}

	public String getMeetingID() {
		return meetingID;
	}

	public String getExternUserID() {
		return externUserID;
	}

	public String getRole() {
		return role;
	}

	public String getConference() {
		return conference;
	}

	public String getRoom() {
		return room;
	}

	public String getVoicebridge() {
		return voicebridge;
	}

	public String getWebvoiceconf() {
		return webvoiceconf;
	}

	public String getMode() {
		return mode;
	}

	public String getRecord() {
		return record;
	}

	public String getWelcome() {
		return welcome;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getServer() {
		return server;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("returncode: ");
		builder.append(returncode);
		builder.append("\nfullname: ");
		builder.append(fullname);
		builder.append("\nconfname: ");
		builder.append(confname);
		builder.append("\nmeetingID: ");
		builder.append(meetingID);
		builder.append("\nexternUserID: ");
		builder.append(externUserID);
		builder.append("\nrole: ");
		builder.append(role);
		builder.append("\nconference: ");
		builder.append(conference);
		builder.append("\nroom: ");
		builder.append(room);
		builder.append("\nvoicebridge: ");
		builder.append(voicebridge);
		builder.append("\nwebvoiceconf: ");
		builder.append(webvoiceconf);
		builder.append("\nmode: ");
		builder.append(mode);
		builder.append("\nrecord: ");
		builder.append(record);
		builder.append("\nwelcome: ");
		builder.append(welcome);
		builder.append("\nmessage: ");
		builder.append(message);
		builder.append("\nserver: ");
		builder.append(server);
		builder.append("\ninternalUserID: ");
		builder.append(internalUserID);
		return builder.toString();
	}

	public String getInternalUserID() {
		return internalUserID;
	}

	public void setInternalUserID(String internalUserID) {
		this.internalUserID = internalUserID;
	}
}
