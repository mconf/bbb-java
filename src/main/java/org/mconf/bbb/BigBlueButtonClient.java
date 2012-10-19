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

package org.mconf.bbb;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.Channel;
import org.mconf.bbb.api.JoinServiceBase;
import org.mconf.bbb.api.JoinServiceProxy;
import org.mconf.bbb.chat.ChatMessage;
import org.mconf.bbb.chat.ChatModule;
import org.mconf.bbb.listeners.IListener;
import org.mconf.bbb.listeners.ListenersModule;
import org.mconf.bbb.users.IParticipant;
import org.mconf.bbb.users.Participant;
import org.mconf.bbb.users.UsersModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.Audio;
import com.flazr.rtmp.message.Command;
import com.flazr.util.Utils;

public class BigBlueButtonClient {

	private static final Logger log = LoggerFactory.getLogger(BigBlueButtonClient.class);

	private MainRtmpConnection mainConnection = null;

	private JoinServiceProxy joinServiceProxy = new JoinServiceProxy();

	private int myUserId = -1;
	private ChatModule chatModule = null;
	private UsersModule usersModule = null;
	private ListenersModule listenersModule = null;

	public void setMyUserId(int myUserId) {
		this.myUserId = myUserId;
		log.info("My userID is {}", myUserId);
	}

	public MainRtmpConnection getConnection() {
		return mainConnection;
	}

	public int getMyUserId() {
		return myUserId;
	}
	
	public Participant getMyself()
	{
		return getUsersModule().getParticipants().get(myUserId);
	}

	public void createChatModule(MainRtmpConnection handler, Channel channel) {
		chatModule = new ChatModule(handler, channel);
	}

	public ChatModule getChatModule() {
		return chatModule;
	}

	public void createUsersModule(MainRtmpConnection handler,
			Channel channel) {
		usersModule = new UsersModule(handler, channel);
	}

	public UsersModule getUsersModule() {
		return usersModule;
	}

	public void createListenersModule(MainRtmpConnection handler,
			Channel channel) {
		listenersModule = new ListenersModule(handler, channel);
	}

	public ListenersModule getListenersModule() {
		return listenersModule;
	}

	public void createJoinService(String serverUrl) {
		if (serverUrl.contains("/bigbluebutton/api/"))
			serverUrl = serverUrl.substring(0, serverUrl.indexOf("/bigbluebutton/api/"));
		
		joinServiceProxy.setServer(serverUrl);
	}
	
	public void createJoinService(String serverUrl, String salt) {
		joinServiceProxy.setServer(serverUrl, salt);
	}
	
	public JoinServiceBase getJoinService() {
		return joinServiceProxy.getJoinService();
	}
	
	public boolean connectBigBlueButton() {
		ClientOptions opt = new ClientOptions();
		opt.setClientVersionToUse(Utils.fromHex("00000000"));
		opt.setHost(getJoinService().getApplicationService().getServerUrl());
		opt.setAppName("bigbluebutton/" + getJoinService().getJoinedMeeting().getConference());
		log.debug(opt.toString());

		mainConnection = new MainRtmpConnection(opt, this);
		return mainConnection.connect();
	}

	public void disconnect() {
		if (mainConnection != null)
			mainConnection.disconnect();
		joinServiceProxy.resetJoinedMeeting();
	}

	public Collection<Participant> getParticipants() {
		return usersModule.getParticipants().values();
	}

	public List<ChatMessage> getPublicChatMessages() {
		return chatModule.getPublicChatMessage();
	}

	public void sendPrivateChatMessage(String message, int userId) {
		chatModule.sendPrivateChatMessage(message, userId);
	}

	public void sendPublicChatMessage(String message) {
		chatModule.sendPublicChatMessage(message);
	}

	public void raiseHand(boolean value) {
		raiseHand(myUserId, value);
	}
	
	public void raiseHand(int userId, boolean value) {
		usersModule.raiseHand(userId, value);
	}
	
	public void assignPresenter(int userId) {
		usersModule.assignPresenter(userId);
	}

	public void kickUser(int userId) {
		usersModule.kickUser(userId);
	}

	public void kickListener(int listenerId) {
		listenersModule.doEjectUser(listenerId);
	}

	public void muteUnmuteListener(int listenerId, boolean value){
		listenersModule.doMuteUnmuteUser(listenerId,value);
	}

	public void muteUnmuteRoom(boolean value) {
		listenersModule.doMuteAllUsers(value);
	}

	public static void main(String[] args) {
		BigBlueButtonClient client = new BigBlueButtonClient();
		client.createJoinService("http://test.bigbluebutton.org/", "03b07");
		client.getJoinService().load();
		if ( (client.getJoinService().join("English 110", "Eclipse", false) == JoinServiceBase.E_OK) //.
				&& (client.getJoinService().getJoinedMeeting() != null)) {
			client.connectBigBlueButton();
		}
	}

	public boolean onCommand(String resultFor, Command command) {
		if (usersModule.onCommand(resultFor, command)
				|| chatModule.onCommand(resultFor, command)
				|| listenersModule.onCommand(resultFor, command))
			return true;
		else
			return false;
	}

	public boolean isConnected() {
		if (mainConnection == null)
			return false;
		else
			return mainConnection.isConnected();
	}

	/**
	 * Listeners and related methods definition
	 * 
	 * @author felipe
	 */
	
	public interface OnPublicChatMessageListener extends IBbbListener {
		public void onPublicChatMessage(ChatMessage message, IParticipant source);
		public void onPublicChatMessage(List<ChatMessage> publicChatMessages, Map<Integer, Participant> participants);
	}
	public interface OnPrivateChatMessageListener extends IBbbListener {
		public void onPrivateChatMessage(ChatMessage message, IParticipant source);
	}
	public interface OnConnectedListener extends IBbbListener {
		public void onConnectedSuccessfully();
		public void onConnectedUnsuccessfully();
	}
	public interface OnDisconnectedListener extends IBbbListener {
		public void onDisconnected();
	}
	public interface OnExceptionListener extends IBbbListener {
		public void onException(Throwable throwable);
	}
	public interface OnKickUserListener extends IBbbListener {
		public void onKickUser(IParticipant p);
		public void onKickMyself();
	}
	public interface OnParticipantJoinedListener extends IBbbListener {
		public void onParticipantJoined(IParticipant p);
	}
	public interface OnParticipantLeftListener extends IBbbListener {
		public void onParticipantLeft(IParticipant p);
	}
	public interface OnParticipantStatusChangeListener extends IBbbListener {
		public void onChangePresenter(IParticipant p);
		public void onChangeHasStream(IParticipant p);
		public void onChangeRaiseHand(IParticipant p);
	}
	public interface OnListenerJoinedListener extends IBbbListener {
		public void onListenerJoined(IListener p);
	}
	public interface OnListenerLeftListener extends IBbbListener {
		public void onListenerLeft(IListener p);
	}
	public interface OnListenerStatusChangeListener extends IBbbListener {
		public void onChangeIsMuted(IListener p);
		public void onChangeIsTalking(IListener p);
	}
	public interface OnAudioListener extends IBbbListener {
		public void onAudio(Audio audio);
	}
	
	private Set<OnPublicChatMessageListener> publicChatMessageListeners = new LinkedHashSet<OnPublicChatMessageListener>();
	private Set<OnPrivateChatMessageListener> privateChatMessageListeners = new LinkedHashSet<OnPrivateChatMessageListener>();
	private Set<OnConnectedListener> connectedListeners = new LinkedHashSet<OnConnectedListener>();
	private Set<OnDisconnectedListener> disconnectedListeners = new LinkedHashSet<OnDisconnectedListener>();
	private Set<OnExceptionListener> exceptionListeners = new LinkedHashSet<OnExceptionListener>();
	private Set<OnKickUserListener> kickUserListeners = new LinkedHashSet<OnKickUserListener>();
	private Set<OnParticipantJoinedListener> participantJoinedListeners = new LinkedHashSet<OnParticipantJoinedListener>();
	private Set<OnParticipantLeftListener> participantLeftListeners = new LinkedHashSet<OnParticipantLeftListener>();
	private Set<OnParticipantStatusChangeListener> participantStatusChangeListeners = new LinkedHashSet<OnParticipantStatusChangeListener>();
	private Set<OnListenerJoinedListener> listenerJoinedListeners = new LinkedHashSet<OnListenerJoinedListener>();
	private Set<OnListenerLeftListener> listenerLeftListeners = new LinkedHashSet<OnListenerLeftListener>();
	private Set<OnListenerStatusChangeListener> listenerStatusChangeListeners = new LinkedHashSet<OnListenerStatusChangeListener>();
	private Set<OnAudioListener> audioListeners = new LinkedHashSet<OnAudioListener>();
	
	public boolean addPublicChatMessageListener(OnPublicChatMessageListener listener) { return publicChatMessageListeners.add(listener); }
	public boolean removePublicChatMessageListener(OnPublicChatMessageListener listener) { return publicChatMessageListeners.remove(listener); }
	public Set<OnPublicChatMessageListener> getPublicChatMessageListeners() { return publicChatMessageListeners; }

	public boolean addPrivateChatMessageListener(OnPrivateChatMessageListener listener) { return privateChatMessageListeners.add(listener); }
	public boolean removePrivateChatMessageListener(OnPrivateChatMessageListener listener) { return privateChatMessageListeners.remove(listener); }
	public Set<OnPrivateChatMessageListener> getPrivateChatMessageListener() { return privateChatMessageListeners; }
	
	public boolean addConnectedListener(OnConnectedListener listener) { return connectedListeners.add(listener); }
	public boolean removeConnectedListener(OnConnectedListener listener) { return connectedListeners.remove(listener); }
	public Set<OnConnectedListener> getConnectedListeners() { return connectedListeners; }
	
	public boolean addDisconnectedListener(OnDisconnectedListener listener) { return disconnectedListeners.add(listener); }
	public boolean removeDisconnectedListener(OnDisconnectedListener listener) { return disconnectedListeners.remove(listener); }
	public Set<OnDisconnectedListener> getDisconnectedListeners() { return disconnectedListeners; }
	
	public boolean addExceptionListener(OnExceptionListener listener) { return exceptionListeners.add(listener); }
	public boolean removeExceptionListener(OnExceptionListener listener) { return exceptionListeners.remove(listener); }
	public Set<OnExceptionListener> getExceptionListeners() { return exceptionListeners; }
	
	public boolean addKickUserListener(OnKickUserListener listener) { return kickUserListeners.add(listener); }
	public boolean removeKickUserListener(OnKickUserListener listener) { return kickUserListeners.remove(listener); }
	public Set<OnKickUserListener> getKickUserListeners() { return kickUserListeners; }
	
	public boolean addParticipantJoinedListener(OnParticipantJoinedListener listener) { return participantJoinedListeners.add(listener); }
	public boolean removeParticipantJoinedListener(OnParticipantJoinedListener listener) { return participantJoinedListeners.remove(listener); }
	public Set<OnParticipantJoinedListener> getParticipantJoinedListeners() { return participantJoinedListeners; }
	
	public boolean addParticipantLeftListener(OnParticipantLeftListener listener) { return participantLeftListeners.add(listener); }
	public boolean removeParticipantLeftListener(OnParticipantLeftListener listener) { return participantLeftListeners.remove(listener); }
	public Set<OnParticipantLeftListener> getParticipantLeftListeners() { return participantLeftListeners; }
	
	public boolean addParticipantStatusChangeListener(OnParticipantStatusChangeListener listener) { return participantStatusChangeListeners.add(listener); }
	public boolean removeParticipantStatusChangeListener(OnParticipantStatusChangeListener listener) { return participantStatusChangeListeners.remove(listener); }
	public Set<OnParticipantStatusChangeListener> getParticipantStatusChangeListeners() { return participantStatusChangeListeners; }
	
	public boolean addListenerJoinedListener(OnListenerJoinedListener listener) { return listenerJoinedListeners.add(listener); }
	public boolean removeListenerJoinedListener(OnListenerJoinedListener listener) { return listenerJoinedListeners.remove(listener); }
	public Set<OnListenerJoinedListener> getListenerJoinedListeners() { return listenerJoinedListeners; }
	
	public boolean addListenerLeftListener(OnListenerLeftListener listener) { return listenerLeftListeners.add(listener); }
	public boolean removeListenerLeftListener(OnListenerLeftListener listener) { return listenerLeftListeners.remove(listener); }
	public Set<OnListenerLeftListener> getListenerLeftListeners() { return listenerLeftListeners; }
	
	public boolean addListenerStatusChangeListener(OnListenerStatusChangeListener listener) { return listenerStatusChangeListeners.add(listener); }
	public boolean removeListenerStatusChangeListener(OnListenerStatusChangeListener listener) { return listenerStatusChangeListeners.remove(listener); }
	public Set<OnListenerStatusChangeListener> getListenerStatusChangeListeners() { return listenerStatusChangeListeners; }

	public boolean addAudioListener(OnAudioListener listener) { return audioListeners.add(listener); }
	public boolean removeAudioListener(OnAudioListener listener) { return audioListeners.remove(listener); }
	public Set<OnAudioListener> getAudioListeners() { return audioListeners; }

}
