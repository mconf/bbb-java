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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Meetings {

	private static final Logger log = LoggerFactory.getLogger(Meetings.class);

	private List<Meeting> meetings = new ArrayList<Meeting>();

	public void setMeetings(List<Meeting> meetings) {
		this.meetings = meetings;
	}

	public List<Meeting> getMeetings() {
		return meetings;
	}

	public Meetings() {

	}

	public int parse(String str) throws ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException, DOMException, ParseException {
		meetings.clear();
		log.debug("parsing getMeetings response: {}", str);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new ByteArrayInputStream(str.getBytes("UTF-8")));
		doc.getDocumentElement().normalize();
		
		Node first_node = doc.getFirstChild();
		if (first_node == null) {
			log.error("Parsing a non-XML response for getMeetings");
			return JoinServiceBase.E_MOBILE_NOT_SUPPORTED;
		} 
		
		boolean check_return_code;
				
		if (first_node.getNodeName().equals("meetings")) {
			log.info("The given response is a mobile getMeetings");
			check_return_code = true;
		} else if (first_node.getNodeName().equals("response")) {
			log.info("The given response is a default getMeetings, or it's an error response");
			
			NodeList return_code_list = doc.getElementsByTagName("returncode");
			if (return_code_list == null || return_code_list.getLength() <= 0 || !return_code_list.item(0).getFirstChild().getNodeValue().equals("SUCCESS"))
				// there's no return code on the message (it's weird), or it's not success
				return JoinServiceBase.E_UNKNOWN_ERROR;
			check_return_code = false;
		} else {
			return JoinServiceBase.E_MOBILE_NOT_SUPPORTED;
		}
		
		NodeList meetings_node = doc.getElementsByTagName("meeting");
		if (meetings_node != null) {
			for (int i = 0; i < meetings_node.getLength(); ++i) {
				Meeting meeting = new Meeting();
				if (meeting.parse((Element) meetings_node.item(i), check_return_code)) 
					meetings.add(meeting);
			}
		}

		return JoinServiceBase.E_OK;
	}

	@Override
	public String toString() {
		if (meetings.isEmpty())
			return "No meetings currently running";
		
		String str = "";
		for (Meeting meeting : meetings) {
			str += meeting.toString() + "\n"; 
		}
		return str.substring(0, str.length() - 1);
	}

}
