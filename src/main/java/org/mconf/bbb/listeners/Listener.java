package org.mconf.bbb.listeners;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONException;

public class Listener implements IListener {
	private int userId;
	private String participantId;
	private String cidName;
	private String cidNum;
	private boolean muted;
	private boolean talking;
	private boolean locked;
	
	public Listener(){}
	
	public Listener(List<?> params) {
		userId = ((Double) params.get(0)).intValue();
		nameAndIdSetting( (String) params.get(1) );
		cidNum = (String) params.get(2);
		muted = (Boolean) params.get(3);
		talking = (Boolean) params.get(4);
		locked = (Boolean) params.get(5);
	}

	public Listener(Map<String, Object> attributes) {
		userId = ((Double) attributes.get("participant")).intValue();
		nameAndIdSetting((String) attributes.get("name"));
		talking = (Boolean) attributes.get("talking");					
		muted = (Boolean) attributes.get("muted");					
		locked = (Boolean) attributes.get("locked");
	}

	public Listener(JSONObject jobj) {
		try {
			userId = Integer.parseInt(jobj.getString("userId"));
			participantId = jobj.getString("webUserId");
			cidName = jobj.getString("callerName");
			cidNum = jobj.getString("callerNum");
			muted = jobj.getBoolean("muted");
			talking = jobj.getBoolean("talking");
			locked = jobj.getBoolean("locked");
		} catch (JSONException je) {
			System.out.println(je.toString());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mconf.bbb.listeners.IListener#setUserId(int)
	 */
	@Override
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	/* (non-Javadoc)
	 * @see org.mconf.bbb.listeners.IListener#getUserId()
	 */
	@Override
	public int getUserId() {
		return userId;
	}
	/* (non-Javadoc)
	 * @see org.mconf.bbb.listeners.IListener#getCidName()
	 */
	@Override
	public String getCidName() {
		return cidName;
	}
	/* (non-Javadoc)
	 * @see org.mconf.bbb.listeners.IListener#setCidName(java.lang.String)
	 */
	@Override
	public void setCidName(String cidName) {
		this.cidName = cidName;
	}
	/* (non-Javadoc)
	 * @see org.mconf.bbb.listeners.IListener#getCidNum()
	 */
	@Override
	public String getCidNum() {
		return cidNum;
	}
	/* (non-Javadoc)
	 * @see org.mconf.bbb.listeners.IListener#setCidNum(java.lang.String)
	 */
	@Override
	public void setCidNum(String cidNum) {
		this.cidNum = cidNum;
	}
	/* (non-Javadoc)
	 * @see org.mconf.bbb.listeners.IListener#isMuted()
	 */
	@Override
	public boolean isMuted() {
		return muted;
	}
	/* (non-Javadoc)
	 * @see org.mconf.bbb.listeners.IListener#setMuted(boolean)
	 */
	@Override
	public void setMuted(boolean muted) {
		this.muted = muted;
	}
	/* (non-Javadoc)
	 * @see org.mconf.bbb.listeners.IListener#isTalking()
	 */
	@Override
	public boolean isTalking() {
		return talking;
	}
	/* (non-Javadoc)
	 * @see org.mconf.bbb.listeners.IListener#setTalking(boolean)
	 */
	@Override
	public void setTalking(boolean talking) {
		this.talking = talking;
	}
	/* (non-Javadoc)
	 * @see org.mconf.bbb.listeners.IListener#isLocked()
	 */
	@Override
	public boolean isLocked() {
		return locked;
	}
	/* (non-Javadoc)
	 * @see org.mconf.bbb.listeners.IListener#setLocked(boolean)
	 */
	@Override
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	@Override
	public String toString() {
		return "Listener [cidName=" + cidName + ", cidNum=" + cidNum
				+ ", locked=" + locked + ", muted=" + muted + ", talking="
				+ talking + ", userId=" + userId + "]";
	}
	
	/**
	 * @return the participantId
	 */
	public String getParticipantId() {
		return participantId;
	}

	/**
	 * @param participantId the participantId to set
	 */
	public void setParticipantId(String participantId) {
		this.participantId = participantId;
	}

	private void nameAndIdSetting(String nameAndId) {
		if( Pattern.matches("\\d+\\-\\w+(\\s+\\w+)*", nameAndId)) {
			participantId = nameAndId.split("-")[0];
			cidName = nameAndId.split("-")[1];
		}
		else {
			participantId = "nil";
			cidName = nameAndId;
		}
		
	}
}
