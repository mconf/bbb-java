package org.mconf.bbb.api;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JoinServiceBase {
	
	public static final int E_OK = 0;
	public static final int E_CHECKSUM_NOT_INFORMED = 1;
	public static final int E_INVALID_CHECKSUM = 2;
	public static final int E_INVALID_TIMESTAMP = 3;
	public static final int E_EMPTY_SECURITY_KEY = 4;
	public static final int E_MISSING_PARAM_MEETINGID = 5;
	public static final int E_MISSING_PARAM_FULLNAME = 6;
	public static final int E_MISSING_PARAM_PASSWORD = 7;
	public static final int E_MISSING_PARAM_TIMESTAMP = 8;
	public static final int E_INVALID_URL = 9;
	public static final int E_SERVER_UNREACHABLE = 10;
	public static final int E_MOBILE_NOT_SUPPORTED = 11;
	public static final int E_UNKNOWN_ERROR = 12;
	
	private static final Logger log = LoggerFactory.getLogger(JoinServiceBase.class);

	protected JoinedMeeting joinedMeeting = null;
	protected String serverUrl = "";
	protected int serverPort = 0;
	protected Meetings meetings = new Meetings();
	protected boolean loaded = false;
	protected ApplicationService appService = null;
	
	public abstract String getVersion();
	protected abstract String getCreateMeetingUrl(String meetingID);
	protected abstract String getLoadUrl();
	protected abstract String getJoinUrl(Meeting meeting, String name, boolean moderator);
	protected abstract String getDemoPath();
	
	protected String getFullDemoPath() {
		return getFullServerUrl() + getDemoPath();
	}
	
	private String getFullServerUrl() {
		return serverUrl + ":" + serverPort;
	}
	
	public int createMeeting(String meetingID) { //.
		String createUrl = getFullDemoPath() + getCreateMeetingUrl(meetingID);
		String response = "Unknown error";
		try {
			response = getUrl(createUrl);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Can't get the url {}", createUrl);
		}
		
		if (meetingID.equals(response))
			return E_OK;
		else
			return E_SERVER_UNREACHABLE;
	}

	public int load() { //.
		String loadUrl = getFullDemoPath() + getLoadUrl();
		log.debug("getMeetings URL: " + loadUrl);
		
		int returnCode;
		loaded = false;
		try {
			returnCode = meetings.parse(getUrl(loadUrl)); 
		} catch (Exception e) {
			e.printStackTrace();
			log.info("Can't connect to {}", loadUrl);
			return E_SERVER_UNREACHABLE;
		}
		
		if(returnCode == E_OK) {
			log.debug(meetings.toString());
			loaded = true;
		}
		
		return returnCode;
	}
	
	public int join(String meetingID, String name, boolean moderator) { //.
		if (!loaded)
			return E_SERVER_UNREACHABLE;
		
		for (Meeting meeting : meetings.getMeetings()) {
			if (meeting.getMeetingID().equals(meetingID))
				return join(meeting, name, moderator);
		}
		return E_SERVER_UNREACHABLE;
	}

	public int join(Meeting meeting, String name, boolean moderator) {  
		return join(getFullDemoPath() + getJoinUrl(meeting, name, moderator));
	}
	
	private int join(String joinUrl) { //.
		joinedMeeting = new JoinedMeeting();
		try {
			String joinResponse = getUrl(joinUrl);
			log.debug("join response: {}", joinResponse);
			joinedMeeting.parse(joinResponse);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Can't join the url {}", joinUrl);
			return E_SERVER_UNREACHABLE;
		}
		
		return joinResponse();
	}

	private int joinResponse() {
		if (joinedMeeting.getReturncode().equals("SUCCESS")) {
			if (joinedMeeting.getServer().length() != 0)
				appService = new ApplicationService(joinedMeeting.getServer());
			else
				appService = new ApplicationService(serverUrl, getVersion());
			return E_OK;
		} else {
			if (joinedMeeting.getMessage() != null)
				log.error(joinedMeeting.getMessage());
			return E_SERVER_UNREACHABLE;
		}
	}
	
	public int standardJoin(String joinUrl) {
		String enterUrl = getFullServerUrl() + "/bigbluebutton/api/enter";
			
		joinedMeeting = new JoinedMeeting();
		try {
			HttpClient client = new DefaultHttpClient();
			getUrl(client, joinUrl);
			String enterResponse = getUrl(client, enterUrl);
			joinedMeeting.parse(enterResponse);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Can't join the url {}", joinUrl);
			return E_SERVER_UNREACHABLE;
		}

		return joinResponse();
	}
	
	public JoinedMeeting getJoinedMeeting() {
		return joinedMeeting;
	}

	public void resetJoinedMeeting() {
		joinedMeeting=null;
	}

	public List<Meeting> getMeetings() {
		return meetings.getMeetings();
	}

	public int getPort() {
		return serverPort;
	}

	public String getApiServerUrl() {
		return serverUrl;
	}
	
	public ApplicationService getApplicationService() {
		return appService;
	}
	
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
	}	

	public Meeting getMeetingByName(String meetingName) {
		for(Meeting meeting : meetings.getMeetings()) {
			if(meeting.getMeetingName().equals(meetingName))
				return meeting;
		}
		return null;
	}
	
	public Meeting getMeetingByID(String meetingID) {
		for(Meeting meeting : meetings.getMeetings()) {
			if(meeting.getMeetingID().equals(meetingID))
				return meeting;
		}
		return null;
	}
	
	protected static String getUrl(String url) throws IOException, ClientProtocolException {
    	HttpClient client = new DefaultHttpClient();
		return getUrl(client, url);
	}
	
	protected static String getUrl(HttpClient client, String url) throws IOException, ClientProtocolException {
		HttpGet method = new HttpGet(url);
		HttpResponse httpResponse = client.execute(method);
		
		if (httpResponse.getStatusLine().getStatusCode() != 200)
			log.debug("HTTP GET {} return {}", url, httpResponse.getStatusLine().getStatusCode());
		
		return EntityUtils.toString(httpResponse.getEntity()).trim();
	}

	protected static String checksum(String s) {
		String checksum = "";
		try {
			checksum = DigestUtils.shaHex(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return checksum;
	}
	
	protected static String urlEncode(String s) {	
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
}
