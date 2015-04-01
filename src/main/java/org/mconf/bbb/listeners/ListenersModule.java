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

package org.mconf.bbb.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import org.jboss.netty.channel.Channel;
import org.mconf.bbb.MainRtmpConnection;
import org.mconf.bbb.Module;
import org.mconf.bbb.BigBlueButtonClient.OnListenerJoinedListener;
import org.mconf.bbb.BigBlueButtonClient.OnListenerLeftListener;
import org.mconf.bbb.BigBlueButtonClient.OnListenerStatusChangeListener;
import org.mconf.bbb.api.ApplicationService;
import org.red5.server.api.IAttributeStore;
import org.red5.server.api.so.IClientSharedObject;
import org.red5.server.api.so.ISharedObjectBase;
import org.red5.server.api.so.ISharedObjectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.CommandAmf0;

public class ListenersModule extends Module implements ISharedObjectListener {
	
	private static final Logger log = LoggerFactory.getLogger(ListenersModule.class);
	private final IClientSharedObject voiceSO;
	
	private Map<Integer, Listener> listeners = new HashMap<Integer, Listener>();
	private boolean roomMuted;

	public ListenersModule(MainRtmpConnection handler, Channel channel) {
		super(handler, channel);
		
		if (version.equals(ApplicationService.VERSION_0_9)) {
			voiceSO = null;
		} else {
			voiceSO = handler.getSharedObject("meetMeUsersSO", false);
			voiceSO.addSharedObjectListener(this);
			voiceSO.connect(channel);
		}
	}
	
	@Override
	public void onSharedObjectClear(ISharedObjectBase so) {
		log.debug("onSharedObjectClear");
		doGetCurrentUsers();
		doGetRoomMuteState();
	}

	public void doGetCurrentUsers() {
    	Command cmd = new CommandAmf0("voice.getMeetMeUsers", null);
    	handler.writeCommandExpectingResult(channel, cmd);
	}

	/*
		<< [STRING _result]
		<< [NUMBER 5.0]
		<< [NULL null]
		<< [NUMBER 1.0]
		<< [BOOLEAN false]
		<< [BOOLEAN false]
		<< [STRING Felipe]
		<< [NUMBER 5.0]
		<< [BOOLEAN false]
		<< [MAP {talking=false, muted=false, name=Felipe, participant=5.0, locked=false}]	
		<< [MAP {5={talking=false, muted=false, name=Felipe, participant=5.0, locked=false}}]
		<< [MAP {count=1.0, participants={5={talking=false, muted=false, name=Felipe, participant=5.0, locked=false}}}]
		<< [1 COMMAND_AMF0 c3 #0 t0 (0) s144] name: _result, transactionId: 5, object: null, args: [{count=1.0, participants={5={talking=false, muted=false, name=Felipe, participant=5.0, locked=false}}}]
		server command: _result
		result for method call: voice.getMeetMeUsers
		
		<< [STRING _result]
		<< [NUMBER 5.0]
		<< [NULL null]
		<< [NUMBER 0.0]
		<< [MAP {count=0.0}]
		<< [1 COMMAND_AMF0 c3 #0 t0 (0) s44] name: _result, transactionId: 5, object: null, args: [{count=0.0}]
		server command: _result
		result for method call: voice.getMeetMeUsers
	 */
	@SuppressWarnings("unchecked")
	public boolean onGetCurrentUsers(String resultFor, Command command) {
		if (resultFor.equals("voice.getMeetMeUsers")) {
			
			
			listeners.clear();
			// the userId is different from the UsersModule
			Map<String, Object> currentUsers = (Map<String, Object>) command.getArg(0);
			int count = ((Double) currentUsers.get("count")).intValue();
			if (count > 0) {
				Map<String, Object> participants = (Map<String, Object>) currentUsers.get("participants");
				
				for (Map.Entry<String, Object> entry : participants.entrySet()) {
					@SuppressWarnings("unused")
					int userId = Integer.parseInt(entry.getKey());
					
					Listener listener = new Listener((Map<String, Object>) entry.getValue());
					onListenerJoined(listener);
				}
			}
			return true;
		}
		return false;
	}

	public void doGetRoomMuteState() {
    	Command cmd = new CommandAmf0("voice.isRoomMuted", null);
    	handler.writeCommandExpectingResult(channel, cmd);
	}

	public boolean onGetRoomMuteState(String resultFor, Command command) {
		if (resultFor.equals("voice.isRoomMuted")) {
			setRoomMuted((Boolean) command.getArg(0));
			return true;
		}
		return false;
	}

	public void doLockMuteUser(int userId, boolean lock) {
    	Command cmd = new CommandAmf0("voice.lockMuteUser", null, Double.valueOf(userId), Boolean.valueOf(lock));
    	handler.writeCommandExpectingResult(channel, cmd);
	}
	
	public void doMuteUnmuteUser(int userId, boolean mute) {
    	Command cmd = new CommandAmf0("voice.muteUnmuteUser", null, Double.valueOf(userId), Boolean.valueOf(mute));
    	handler.writeCommandExpectingResult(channel, cmd);
	}
	
	public void doMuteAllUsers(boolean mute) {
    	Command cmd = new CommandAmf0("voice.muteAllUsers", null, Boolean.valueOf(mute));
    	handler.writeCommandExpectingResult(channel, cmd);
    	doGetRoomMuteState();
	}
	
	public void doEjectUser(int userId) {
    	Command cmd = new CommandAmf0("voice.kickUSer", null, Double.valueOf(userId));
    	handler.writeCommandExpectingResult(channel, cmd);
	}
	
	@Override
	public void onSharedObjectConnect(ISharedObjectBase so) {
		log.debug("onSharedObjectConnect");
	}

	@Override
	public void onSharedObjectDelete(ISharedObjectBase so, String key) {
		log.debug("onSharedObjectDelete");
	}

	@Override
	public void onSharedObjectDisconnect(ISharedObjectBase so) {
		log.debug("onSharedObjectDisconnect");
	}


	@Override
	public void onSharedObjectSend(ISharedObjectBase so, String method,
			List<?> params) {
		if (method.equals("userJoin")) {
			//	meetMeUsersSO { SOEvent(SERVER_SEND_MESSAGE, userJoin, [5.0, Felipe, Felipe, false, false, false]) }
			Listener listener = new Listener(params);
			if (!listeners.containsKey(listener.getUserId())) {
				onListenerJoined(listener);
			} else {
				log.warn("The listener {} is already in the list", listener.getUserId());
			}
		} else if (method.equals("userTalk")) {
			//	meetMeUsersSO { SOEvent(SERVER_SEND_MESSAGE, userTalk, [5.0, true]) }
			int userId = ((Double) params.get(0)).intValue();
			IListener listener = listeners.get(userId);
			
			if (listener != null) {
				listener.setTalking((Boolean) params.get(1));
				for (OnListenerStatusChangeListener l : handler.getContext().getListenerStatusChangeListeners())
					l.onChangeIsTalking(listener);
			} else {
				log.warn("Can't find the listener {} on userTalk", userId);
			}
		} else if (method.equals("userLockedMute")) {
			// meetMeUsersSO { SOEvent(SERVER_SEND_MESSAGE, userLockedMute, [4.0, true]) }
			int userId = ((Double) params.get(0)).intValue();
			IListener listener = listeners.get(userId);
			
			if (listener != null) {
				listener.setLocked((Boolean) params.get(1));
			} else {
				log.warn("Can't find the listener {} on userLockedMute", userId);
			}
		} else if (method.equals("userMute")) {
			// meetMeUsersSO { SOEvent(SERVER_SEND_MESSAGE, userMute, [4.0, true]) }
			int userId = ((Double) params.get(0)).intValue();
			IListener listener = listeners.get(userId);
			
			if (listener != null) {
				listener.setMuted((Boolean) params.get(1));
				for (OnListenerStatusChangeListener l : handler.getContext().getListenerStatusChangeListeners())
					l.onChangeIsMuted(listener);
			} else {
				log.warn("Can't find the listener {} on userMute", userId);
			}
		} else if (method.equals("userLeft")) {
			// meetMeUsersSO { SOEvent(SERVER_SEND_MESSAGE, userLeft, [2.0]) }
			int userId = ((Double) params.get(0)).intValue();
			IListener listener = listeners.get(userId);
			
			if (listener != null) {
				for (OnListenerLeftListener l : handler.getContext().getListenerLeftListeners())
					l.onListenerLeft(listener);
				listeners.remove(userId);
			} else {
				log.warn("Can't find the listener {} on userLeft", userId);
			}
		} else if (method.equals("muteStateCallback")) {
			// meetMeUsersSO { SOEvent(SERVER_SEND_MESSAGE, muteStateCallback, [false]) }
			setRoomMuted((Boolean) params.get(0));
		}
		log.debug("onSharedObjectSend");
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so, String key,
			Object value) {
		log.debug("onSharedObjectUpdate1");
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so,
			IAttributeStore values) {
		log.debug("onSharedObjectUpdate2");
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so,
			Map<String, Object> values) {
		log.debug("onSharedObjectUpdate3");
	}

	@Override
	public boolean onCommand(String resultFor, Command command) {
		if (onGetCurrentUsers(resultFor, command)
				|| onGetRoomMuteState(resultFor, command)) {
			return true;
		} else
			return false;
	}

	public void setRoomMuted(boolean roomMuted) {
		this.roomMuted = roomMuted;
	}

	public boolean isRoomMuted() {
		return roomMuted;
	}

	public Map<Integer, Listener> getListeners() {
		return listeners;
	}
	
	public void onListenerJoined(Listener p) {
		log.debug("New listener: " + p.toString());
		listeners.put(p.getUserId(), p);			
		for (OnListenerJoinedListener l : handler.getContext().getListenerJoinedListeners())
			l.onListenerJoined(p);
	}

	private Listener getJoinedVoiceUser(JSONObject jobj) {
		Listener l = null;
		JSONObject voiceUser = (JSONObject) getFromMessage(jobj, "voiceUser");
		boolean joined = (boolean) getFromMessage(voiceUser, "joined");

		if (joined) l = new Listener(voiceUser);

		return l;
	}

	public boolean onMessageFromServer(Command command) {
		String msgName = (String) command.getArg(0);
		switch (msgName) {
			case "getUsersReply":
				handleGetUsersReply(getMessage(command.getArg(1)));
				return true;
			case "userJoinedVoice":
				handleUserJoinedVoice(getMessage(command.getArg(1)));
				return true;
			case "userLeftVoice":
				handleUserLeftVoice(getMessage(command.getArg(1)));
				return true;
			case "voiceUserMuted":
				handleVoiceUserMuted(getMessage(command.getArg(1)));
				return true;
			case "voiceUserTalking":
				handleVoiceUserTalking(getMessage(command.getArg(1)));
				return true;
			case "meetingMuted":
				handleMeetingMuted(getMessage(command.getArg(1)));
				return true;
			case "meetingState":
				handleMeetingState(getMessage(command.getArg(1)));
				return true;
			default:
				return false;
		}
	}

	private void handleGetUsersReply(JSONObject jobj) {
		JSONArray users = (JSONArray) getFromMessage(jobj, "users");

		listeners.clear();
		for (int i = 0; i < users.length(); i++) {
			try {
				Listener l = getJoinedVoiceUser(users.getJSONObject(i));
				if (l != null) onListenerJoined(l);
			} catch (JSONException je) {
				System.out.println(je.toString());
			}
		}
	}

	private void handleMeetingState(JSONObject jobj) {
		roomMuted = (boolean) getFromMessage(jobj, "meetingMuted");
	}

	private void handleUserJoinedVoice(JSONObject jobj) {
		JSONObject user = (JSONObject) getFromMessage(jobj, "user");
		Listener l = new Listener((JSONObject) getFromMessage(user, "voiceUser"));
		onListenerJoined(l);
	}

	private Integer getUserId(String participantId) {
		for (Map.Entry<Integer, Listener> entry : listeners.entrySet()) {
			Listener l = entry.getValue();
			if (participantId.equals(l.getParticipantId())) return entry.getKey();
		}
		return -1;
	}

	private void handleUserLeftVoice(JSONObject jobj) {
		JSONObject user = (JSONObject) getFromMessage(jobj, "user");
		String participantId = (String) getFromMessage(user, "userId");
		Integer userId = getUserId(participantId);
		Listener listener = listeners.get(userId);

		if (listener != null) {
			for (OnListenerLeftListener l : handler.getContext().getListenerLeftListeners())
				l.onListenerLeft(listener);
			listeners.remove(userId);
		} else {
			log.warn("Can't find the listener {} on userLeft", userId);
		}
	}

	private void handleVoiceUserMuted(JSONObject jobj) {
		Listener listener = listeners.get(Integer.parseInt((String) getFromMessage(jobj, "voiceUserId")));

		if (listener != null) {
			listener.setMuted((Boolean) getFromMessage(jobj, "muted"));
			for (OnListenerStatusChangeListener l : handler.getContext().getListenerStatusChangeListeners())
				l.onChangeIsMuted(listener);
		} else {
			log.warn("Can't find the listener {} on userMute", listener.getUserId());
		}
	}

	private void handleVoiceUserTalking(JSONObject jobj) {
		Integer voiceUserId = Integer.parseInt((String) getFromMessage(jobj, "voiceUserId"));
		Listener listener = listeners.get(voiceUserId);

		if (listener != null) {
			listener.setTalking((Boolean) getFromMessage(jobj, "talking"));
			for (OnListenerStatusChangeListener l : handler.getContext().getListenerStatusChangeListeners())
				l.onChangeIsTalking(listener);
		} else {
			log.warn("Can't find the listener {} on userTalk", voiceUserId);
		}
	}

	private void handleMeetingMuted(JSONObject jobj) {

	}
}
