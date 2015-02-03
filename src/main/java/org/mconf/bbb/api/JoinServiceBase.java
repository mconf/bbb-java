package org.mconf.bbb.api;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
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
	public static final int E_CANNOT_GET_CONFIGXML = 13;
	
	
	private static final Logger log = LoggerFactory.getLogger(JoinServiceBase.class);

	protected JoinedMeeting joinedMeeting = null;
	protected String serverUrl = "";
	protected int serverPort = 0;
	protected Meetings meetings = new Meetings();
	protected boolean loaded = false;
	protected ApplicationService appService = null;
	protected BbbServerConfig serverConfig = null;
	
	public abstract String getVersion();
	public abstract Map<String, Object> getLockSettings();
	public abstract boolean getLockOnStart();
	public abstract boolean getMuteOnStart();
	
	protected abstract String getCreateMeetingUrl(String meetingID);
	protected abstract String getLoadUrl();
	protected abstract String getJoinUrl(Meeting meeting, String name, boolean moderator);
	protected abstract String getApiPath();
	
	protected String getFullDemoPath() {
		return getFullServerUrl() + getApiPath();
	}
	
	private String getFullServerUrl() {
		return serverUrl + ":" + serverPort;
	}
	
	public int createMeeting(String meetingID) {
		String createUrl = getFullDemoPath() + getCreateMeetingUrl(meetingID);
		log.debug("create URL: {}", createUrl);
		String response = "Unknown error";
		try {
			response = getUrl(createUrl);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Can't get the url {}", createUrl);
		}
		if (getVersion() == ApplicationService.VERSION_0_9 ||
				getVersion() == ApplicationService.VERSION_0_81) {
			if (ParserUtils.getNodeValueFromResponse(response, "returncode").equals("SUCCESS"))
				return E_OK;
		} else {
			if (meetingID.equals(response)) return E_OK;
		}
		log.error("create response: {}", response);
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
	
	protected int join(String joinUrl) { //.
		joinedMeeting = new JoinedMeeting();
		try {
			String joinResponse = getUrl(joinUrl);
			log.debug("join response: {}", joinResponse);
			joinedMeeting.parseXML(joinResponse);
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
		joinedMeeting = new JoinedMeeting();
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet method = new HttpGet(joinUrl);
			HttpContext context = new BasicHttpContext();
			HttpResponse httpResponse = client.execute(method, context);
			if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				log.debug("HTTP GET {} return {}", joinUrl, httpResponse.getStatusLine().getStatusCode());
			HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute( 
	                ExecutionContext.HTTP_REQUEST);

			if (!currentReq.getURI().getPath().equals("/client/BigBlueButton.html")) {
				log.warn("It was redirected to {} instead of /client/BigBlueButton.html: the server was branded" +
						" and the HTML name was changed, or it's an error. However, it will continue processing", currentReq.getURI().getPath());
			}

			HttpHost currentHost = (HttpHost) context.getAttribute( 
	                ExecutionContext.HTTP_TARGET_HOST);
	        String enterUrl = currentHost.toURI() + "/bigbluebutton/api/enter";
			
	        // have to modify the answer of the api/enter in case when the join
	        // message is answered by another host (proxy)
	        EntityUtils.consume(httpResponse.getEntity());
			String enterResponse = getUrl(client, enterUrl).replace("</response>", "<server>" + currentHost.toURI() + "</server></response>");
			
			// TODO: check if this will continue this way with 0.81
			if (getVersion() == ApplicationService.VERSION_0_9 ||
				getVersion() == ApplicationService.VERSION_0_81) {
				joinedMeeting.parseJSON(enterResponse);
			} else {
				joinedMeeting.parseXML(enterResponse);
			}
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
	
	public int setServerConfiguration()
	{	
		String configAddress = "http://" + appService.getServerUrl() + ":" + Integer.toString(appService.getServerPort()) + "/client/conf/config.xml";
		log.debug("Trying to fetch {}", configAddress);
		try 
		{
			serverConfig = new BbbServerConfig(getUrl(configAddress));				
		} 
		catch (Exception e) 
		{
			log.error("Couldn't get config.xml");
			return E_CANNOT_GET_CONFIGXML;
		}	

		return E_OK;
	}
	
	public BbbServerConfig getServerConfiguration()
	{
		return serverConfig;
	}
	
	protected static String getUrl(String url) throws IOException, ClientProtocolException {
    	HttpClient client = new DefaultHttpClient();
		return getUrl(client, url);
	}
	
	protected static String getUrl(HttpClient client, String url) throws IOException, ClientProtocolException {
		HttpGet method = new HttpGet(url);
		HttpResponse httpResponse = client.execute(method);
		
		if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
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
