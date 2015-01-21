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

package org.mconf.bbb.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.json.JSONException;

import org.jboss.netty.channel.Channel;
import org.mconf.bbb.BigBlueButtonClient.OnKickUserListener;
import org.mconf.bbb.BigBlueButtonClient.OnParticipantJoinedListener;
import org.mconf.bbb.BigBlueButtonClient.OnParticipantLeftListener;
import org.mconf.bbb.BigBlueButtonClient.OnParticipantStatusChangeListener;
import org.mconf.bbb.MainRtmpConnection;
import org.mconf.bbb.Module;
import org.mconf.bbb.api.ApplicationService;
import org.red5.server.api.IAttributeStore;
import org.red5.server.api.so.IClientSharedObject;
import org.red5.server.api.so.ISharedObjectBase;
import org.red5.server.api.so.ISharedObjectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.CommandAmf0;

public class UsersModule extends Module implements ISharedObjectListener {
	private static final Logger log = LoggerFactory.getLogger(UsersModule.class);

	private final IClientSharedObject participantsSO;

	private Map<String, Participant> participants = new ConcurrentHashMap<String, Participant>();
	private int moderatorCount = 0, participantCount = 0;
	private String joinServiceVersion;

	public UsersModule(MainRtmpConnection handler, Channel channel) {
		super(handler, channel);
		
		joinServiceVersion = handler.getContext().getJoinService().getApplicationService().getVersion();
		
		if (joinServiceVersion.equals(ApplicationService.VERSION_0_81) ||
			joinServiceVersion.equals(ApplicationService.VERSION_0_9)) {
			participantsSO = null;
			doQueryParticipants();
		} else {
			participantsSO = handler.getSharedObject("participantsSO", false);
			participantsSO.addSharedObjectListener(this);
			participantsSO.connect(channel);
		}
	}

	@Override
	public void onSharedObjectClear(ISharedObjectBase so) {
		log.debug("onSharedObjectClear");
		doQueryParticipants();
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

	@SuppressWarnings("unchecked")
	@Override
	public void onSharedObjectSend(ISharedObjectBase so, 
			String method, List<?> params) {
		log.debug("onSharedObjectSend");

		if (so.equals(participantsSO)) {
			if (method.equals("kickUserCallback")) {
				IParticipant p = getParticipant(params.get(0));
				if (handler.getContext().isMyself(p.getUserId())) {
					for (OnKickUserListener l : handler.getContext().getKickUserListeners())
						l.onKickMyself();
					channel.close();
				} else
					for (OnKickUserListener l : handler.getContext().getKickUserListeners())
						l.onKickUser(p);
				return;
			}
			if (method.equals("participantLeft")) {
				IParticipant p = getParticipant(params.get(0));
				
				synchronized (handler.getContext().getParticipantLeftListeners()) {
					for (OnParticipantLeftListener l : handler.getContext().getParticipantLeftListeners())
						l.onParticipantLeft(p);
				}

				if(p.getRole().equals("MODERATOR"))
					moderatorCount--;
				else
					participantCount--;

				log.debug("participantLeft: {}", p);
				participants.remove(p.getUserId());

				return;
			}
			if (method.equals("participantJoined")) {
				Participant p = new Participant((Map<String, Object>) params.get(0), joinServiceVersion);
				onParticipantJoined(p);
				return;
			}
			if (method.equals("participantStatusChange")) {
				Participant p = getParticipant(params.get(0));
				
				if (p != null)
					onParticipantStatusChange(p, (String) params.get(1), params.get(2));
				return;
			}
		}
	}
	
	static public String getUserIdFromObject(Object param) {
		if (param.getClass() == Double.class)
			return Integer.toString(((Double) param).intValue());
		else if (param.getClass() == String.class)
			return (String) param;
		else
			return null;
	}
	
	private Participant getParticipant(Object param) {
		String userId = getUserIdFromObject(param);
		if (userId != null)
			return participants.get(userId);
		else
			return null;
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so, String key,
			Object value) {
		log.debug("onSharedObjectUpdate 1");
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so,
			IAttributeStore values) {
		log.debug("onSharedObjectUpdate 2");
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so,
			Map<String, Object> values) {
		log.debug("onSharedObjectUpdate 3");
	}

	/**
	 * {@link} https://github.com/bigbluebutton/bigbluebutton/blob/master/bigbluebutton-client/src/org/bigbluebutton/modules/chat/services/PrivateChatSharedObjectService.as#L142
	 */
	public void doQueryParticipants() {
		Command cmd = new CommandAmf0("participants.getParticipants", null);
		handler.writeCommandExpectingResult(channel, cmd);
	}

	/**
	 * example:
	 * [MAP {count=2.0, participants={112={status={raiseHand=false, hasStream=false, presenter=false}, name=Eclipse, userid=112.0, role=VIEWER}, 97={status={raiseHand=false, hasStream=false, presenter=true}, name=Felipe, userid=97.0, role=MODERATOR}}}]
	 * [1 COMMAND_AMF0 c3 #0 t0 (0) s299] name: _result, transactionId: 4, object: null, args: [{count=2.0, participants={112={status={raiseHand=false, hasStream=false, presenter=false}, name=Eclipse, userid=112.0, role=VIEWER}, 97={status={raiseHand=false, hasStream=false, presenter=true}, name=Felipe, userid=97.0, role=MODERATOR}}}]
	 */
	@SuppressWarnings("unchecked")
	public boolean onQueryParticipants(String resultFor, Command command) {
		if (resultFor.equals("participants.getParticipants")) {
			Map<String, Object> args = (Map<String, Object>) command.getArg(0);

			if (args != null) {
				participants.clear();

				@SuppressWarnings("unused")
				int count = ((Double) args.get("count")).intValue();

				Map<String, Object> participantsMap = (Map<String, Object>) args.get("participants");

				for (Map.Entry<String, Object> entry : participantsMap.entrySet()) {
					Participant p = new Participant((Map<String, Object>) entry.getValue(), joinServiceVersion);
					onParticipantJoined(p);
				}
			}
			return true;
		}
		return false;
	}

	public Map<String, Participant> getParticipants() {
		return participants;
	}

	public void onParticipantJoined(Participant p) {
		log.info("new participant: {}", p.toString());
		participants.put(p.getUserId(), p);
		if (p.isModerator())
			moderatorCount++;
		else
			participantCount++;
		for (OnParticipantJoinedListener l : handler.getContext().getParticipantJoinedListeners())
			l.onParticipantJoined(p);
	}

	private void onParticipantStatusChange(Participant p, String key,
			Object value) {
		log.debug("participantStatusChange: " + p.getName() + " status: " + key + " value: " + value.toString());
		if (key.equals("presenter")) {
			p.getStatus().setPresenter((Boolean) value);
			for (OnParticipantStatusChangeListener l : handler.getContext().getParticipantStatusChangeListeners())
				l.onChangePresenter(p);
		} else if (key.equals("hasStream")) {
			p.getStatus().setHasStream(value);
			for (OnParticipantStatusChangeListener l : handler.getContext().getParticipantStatusChangeListeners())
				l.onChangeHasStream(p);
		} else if (key.equals("streamName")) {
			p.getStatus().setStreamName((String) value);
		} else if (key.equals("raiseHand")) {
			p.getStatus().setRaiseHand((Boolean) value);
			for (OnParticipantStatusChangeListener l : handler.getContext().getParticipantStatusChangeListeners())
				l.onChangeRaiseHand(p);
		}
	}

	public void raiseHand(String userId, boolean value) {
		Command cmd = new CommandAmf0("participants.setParticipantStatus", null, userId, "raiseHand", value);
		handler.writeCommandExpectingResult(channel, cmd);
	}

	public void assignPresenter(String userId) {
		// as it's implemented on bigbluebutton-client/src/org/bigbluebutton/modules/present/business/PresentSOService.as:353
		Participant p = participants.get(userId);
		if (p == null) {
			log.warn("Inconsistent state here");
			return;
		}
		
		if (joinServiceVersion.equals(ApplicationService.VERSION_0_7)) {
			Command cmd = new CommandAmf0("presentation.assignPresenter", null, userId, p.getName(), 1);
			handler.writeCommandExpectingResult(channel, cmd);
		}
		
		else { //if (joinServiceVersion == JoinService0Dot8.class)
			Command cmd = new CommandAmf0("participants.assignPresenter", null, userId, p.getName(), 1);
			handler.writeCommandExpectingResult(channel, cmd);
		}
	}

	public void addStream(String streamName) {
		if (joinServiceVersion.equals(ApplicationService.VERSION_0_7)) {
	    	Command cmd = new CommandAmf0("participants.setParticipantStatus", null, handler.getContext().getMyUserId(), "streamName", streamName);
	    	handler.writeCommandExpectingResult(channel, cmd);
	    	
	    	cmd = new CommandAmf0("participants.setParticipantStatus", null, handler.getContext().getMyUserId(), "hasStream", true);
	    	handler.writeCommandExpectingResult(channel, cmd);
		} else { //if (joinServiceVersion == JoinService0Dot8.class) 
	    	Command cmd = new CommandAmf0("participants.setParticipantStatus", null, handler.getContext().getMyUserId(), "hasStream", "true,stream=" + streamName);
	    	handler.writeCommandExpectingResult(channel, cmd);
		}
	}

	public void removeStream(String streamName) {
		if (joinServiceVersion.equals(ApplicationService.VERSION_0_7)) {
			Command cmd = new CommandAmf0("participants.setParticipantStatus", null, handler.getContext().getMyUserId(), "");
			handler.writeCommandExpectingResult(channel, cmd);
	
			cmd = new CommandAmf0("participants.setParticipantStatus", null, handler.getContext().getMyUserId(), "hasStream", false);
			handler.writeCommandExpectingResult(channel, cmd);
		} else { //if (joinServiceVersion == JoinService0Dot8.class) {
	    	Command cmd = new CommandAmf0("participants.setParticipantStatus", null, handler.getContext().getMyUserId(), "hasStream", "false,stream=" + streamName);
	    	handler.writeCommandExpectingResult(channel, cmd);
		}
	}

	public void kickUser(String userId) {
		if (handler.getContext().getMyself().isModerator()) {
			List<Object> list = new ArrayList<Object>();
			list.add(userId);
			participantsSO.sendMessage("kickUserCallback", list);
		}
	}

	@Override
	public boolean onCommand(String resultFor, Command command) {
		if (onQueryParticipants(resultFor, command)) {
			handler.getContext().createChatModule(handler, channel);
			handler.getContext().createListenersModule(handler, channel);
			return true;
		} else
			return false;
	}

	public int getModeratorCount() {
		return moderatorCount;
	}

	public int getParticipantCount() {
		return participantCount;
	}

	public boolean onMessageFromServer090(Command command) {
		JSONObject jobj = null; // TODO: Remove this from here

		String msgName = (String) command.getArg(0);
		switch (msgName) {
			case "getUsersReply":
				System.out.println(msgName);

				// TODO: Remove this from here
				try {
					Map<String, Object> map = (HashMap<String, Object>) command.getArg(1);
					jobj = new JSONObject((String) map.get("msg"));
				} catch (JSONException je) {
					System.out.println(je.toString());
				}
				///////////////////////////////

				handleGetUsersReply(jobj);
				return true;
			case "participantJoined":
				System.out.println(msgName);
				handleParticipantJoined(jobj);
				return true;
			case "participantLeft":
				System.out.println(msgName);
				handleParticipantLeft(jobj);
				return true;
			case "assignPresenterCallback":
				System.out.println(msgName);
				handleAssignPresenterCallback(jobj);
				return true;
			case "meetingEnded":
				System.out.println(msgName);
				handleLogout(jobj);
				return true;
			case "meetingHasEnded":
				System.out.println(msgName);
				handleMeetingHasEnded(jobj);
				return true;
			case "meetingState":
				System.out.println(msgName);
				handleMeetingState(jobj);
				return true; 
			case "participantStatusChange":
				System.out.println(msgName);
				handleParticipantStatusChange(jobj);
				return true;
			case "userRaisedHand":
				System.out.println(msgName);
				handleUserRaisedHand(jobj);
				return true;
			case "userLoweredHand":
				System.out.println(msgName);
				handleUserLoweredHand(jobj);
				return true;
			case "userSharedWebcam":
				System.out.println(msgName);
				handleUserSharedWebcam(jobj);
				return true;
			case "userUnsharedWebcam":
				System.out.println(msgName);
				handleUserUnsharedWebcam(jobj);
				return true;
			case "getRecordingStatusReply":
				System.out.println(msgName);
				handleGetRecordingStatusReply(jobj);
				return true;
			case "recordingStatusChanged":
				System.out.println(msgName);
				handleRecordingStatusChanged(jobj);
				return true;
			case "joinMeetingReply":
				System.out.println(msgName);
				handleJoinedMeeting(jobj);
				return true;
			case "user_listening_only":
				System.out.println(msgName);
				handleUserListeningOnly(jobj);
				return true;
			case "permissionsSettingsChanged":
				System.out.println(msgName);
				handlePermissionsSettingsChanged(jobj);
				return true;
			default:
				return false;
		}
	}

	private void handleGetUsersReply(JSONObject jobj) {
//		handler.getContext().createChatModule(handler, channel);
//		handler.getContext().createListenersModule(handler, channel);
	}

	private void handleParticipantJoined(JSONObject jobj) {

	}

	private void handleParticipantLeft(JSONObject jobj) {

	}

	private void handleAssignPresenterCallback(JSONObject jobj) {

	}

	private void handleLogout(JSONObject jobj) {

	}

	private void handleMeetingHasEnded(JSONObject jobj) {

	}

	private void handleMeetingState(JSONObject jobj) {

	}

	private void handleParticipantStatusChange(JSONObject jobj) {

	}

	private void handleUserRaisedHand(JSONObject jobj) {

	}

	private void handleUserLoweredHand(JSONObject jobj) {

	}

	private void handleUserSharedWebcam(JSONObject jobj) {

	}

	private void handleUserUnsharedWebcam(JSONObject jobj) {

	}

	private void handleGetRecordingStatusReply(JSONObject jobj) {

	}

	private void handleRecordingStatusChanged(JSONObject jobj) {

	}

	private void handleJoinedMeeting(JSONObject jobj) {

	}

	private void handleUserListeningOnly(JSONObject jobj) {

	}

	private void handlePermissionsSettingsChanged(JSONObject jobj) {

	}
}