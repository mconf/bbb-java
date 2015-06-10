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

package org.mconf.bbb.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;
import org.mconf.bbb.BigBlueButtonClient.OnPrivateChatMessageListener;
import org.mconf.bbb.BigBlueButtonClient.OnPublicChatMessageListener;
import org.mconf.bbb.MainRtmpConnection;
import org.mconf.bbb.Module;
import org.mconf.bbb.api.ApplicationService;
import org.mconf.bbb.users.IParticipant;
import org.mconf.bbb.users.UsersModule;
import org.red5.server.api.IAttributeStore;
import org.red5.server.api.so.IClientSharedObject;
import org.red5.server.api.so.ISharedObjectBase;
import org.red5.server.api.so.ISharedObjectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.CommandAmf0;

public class ChatModule extends Module implements ISharedObjectListener {
	private static final Logger log = LoggerFactory.getLogger(ChatModule.class);

	private final IClientSharedObject publicChatSO, privateChatSO;

	private List<ChatMessage> publicChatMessages = Collections.synchronizedList(new ArrayList<ChatMessage>());
	private Map<String, List<ChatMessage>> privateChatMessages = new ConcurrentHashMap<String, List<ChatMessage>>();
	
	public final static int MESSAGE_ENCODING_UNKNOWN = -1;
	public final static int MESSAGE_ENCODING_STRING = 0;
	public final static int MESSAGE_ENCODING_TYPED_OBJECT = 1;
	public static int MESSAGE_ENCODING = MESSAGE_ENCODING_UNKNOWN;

	public ChatModule(MainRtmpConnection handler, Channel channel) {
		super(handler, channel);
		
		if (version.equals(ApplicationService.VERSION_0_7))
			MESSAGE_ENCODING = MESSAGE_ENCODING_STRING;
		else
			MESSAGE_ENCODING = MESSAGE_ENCODING_TYPED_OBJECT;
		
		if (version.equals(ApplicationService.VERSION_0_9) || version.equals(ApplicationService.VERSION_0_81)) {
			publicChatSO = null;
			privateChatSO = null;
			publicChatMessages.clear();
			privateChatMessages.clear();
//			doGetChatMessages();
		} else {
			publicChatSO = handler.getSharedObject("chatSO", false);
			publicChatSO.addSharedObjectListener(this);
			publicChatSO.connect(channel);
			
			privateChatSO = handler.getSharedObject(handler.getContext().getMyUserId(), false);
			privateChatSO.addSharedObjectListener(this);
			privateChatSO.connect(channel);
		}
	}

	@Override
	public void onSharedObjectClear(ISharedObjectBase so) {
		log.debug("onSharedObjectClear");
		if (so.equals(publicChatSO)) {
			publicChatMessages.clear();
			doGetChatMessages();
			return;
		}
		if (so.equals(privateChatSO)) {
			privateChatMessages.clear();
		}
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
	public void onSharedObjectSend(ISharedObjectBase so, 
			String method, List<?> params) {
		log.debug("onSharedObjectSend");
		
		if (so.equals(publicChatSO)) {
			if (method.equals("newChatMessage") && params != null) {
				// example: [oi|Felipe|0|14:35|en|97]
				onPublicChatMessage(new ChatMessage(params.get(0)));
				return;
			}
		}
		if (so.equals(privateChatSO)) {
			if (method.equals("messageReceived") && params != null) {
				// example: [97, oi|Felipe|0|14:35|en|97]
				String userId = UsersModule.getUserIdFromObject(params.get(0));
				onPrivateChatMessage(new ChatMessage(params.get(1)), handler.getContext().getUsersModule().getParticipants().get(userId));
				return;
			}
		}
		
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
	 * {@link} https://github.com/bigbluebutton/bigbluebutton/blob/master/bigbluebutton-client/src/org/bigbluebutton/modules/chat/services/PublicChatSharedObjectService.as#L128
	 */
	public void doGetChatMessages() {
		String commandName;
		if (version.equals(ApplicationService.VERSION_0_81) ||
				version.equals(ApplicationService.VERSION_0_9)) {
			commandName = "chat.sendPublicChatHistory";
		} else {
			commandName = "chat.getChatMessages";
		}
		Command cmd = new CommandAmf0(commandName, null);
		handler.writeCommandExpectingResult(channel, cmd);
	}
	
	/**
	 * example:
	 * [ARRAY [oi|Felipe|0|21:37|en|61, alo|Felipe|0|21:48|en|61, testando|Felipe|0|21:48|en|61]]
	 * @param resultFor
	 * @param command
	 * @return
	 */
	/*
	 */
	public boolean onGetChatMessages(String resultFor, Command command) {
		if (resultFor.equals("chat.getChatMessages")) {
			publicChatMessages.clear();
			
			List<Object> messages = (List<Object>) Arrays.asList((Object[]) command.getArg(0));
			for (Object message : messages)
				publicChatMessages.add(new ChatMessage(message));
			for (OnPublicChatMessageListener listener : handler.getContext().getPublicChatMessageListeners())
				listener.onPublicChatMessage(publicChatMessages, handler.getContext().getUsersModule().getParticipants());
			return true;
		}
		return false;
	}

	/**
	 * {@link} https://github.com/bigbluebutton/bigbluebutton/blob/master/bigbluebutton-client/src/org/bigbluebutton/modules/chat/services/PublicChatSharedObjectService.as#L89
	 * @param message
	 */
	public void sendPublicChatMessage(String message) {
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setMessage(message);
		chatMessage.setUsername(handler.getContext().getJoinService().getJoinedMeeting().getFullname());
		chatMessage.setUserId(handler.getContext().getMyUserId());

    	Command command = new CommandAmf0("chat.sendMessage", null, chatMessage.encode());
    	handler.writeCommandExpectingResult(channel, command);
	}
	
	/**
	 * {@link} https://github.com/bigbluebutton/bigbluebutton/blob/master/bigbluebutton-client/src/org/bigbluebutton/modules/chat/services/PrivateChatSharedObjectService.as#L103
	 * @param message
	 * @param userid
	 */
	public void sendPrivateChatMessage(String message, String userid) {
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setMessage(message);
		chatMessage.setUsername(handler.getContext().getJoinService().getJoinedMeeting().getFullname());
		chatMessage.setUserId(handler.getContext().getMyUserId());
		
		// \TODO the userId is being passed as Double, but should be passed as String on BigBlueButton 0.81
    	Command command = new CommandAmf0("chat.privateMessage", null, chatMessage.encode(), Double.valueOf(handler.getContext().getMyUserId()), Double.valueOf(userid));
    	handler.writeCommandExpectingResult(channel, command);

    	// the message sent should be received like on public chat
    	onPrivateChatMessage(chatMessage, handler.getContext().getUsersModule().getParticipants().get(userid));
	}
	
	public void onPublicChatMessage(ChatMessage chatMessage) {
		IParticipant source = handler.getContext().getUsersModule().getParticipants().get(chatMessage.getUserId());
		onPublicChatMessage(chatMessage, source);
	}
	
	public void onPublicChatMessage(ChatMessage chatMessage, IParticipant source) {
		for (OnPublicChatMessageListener listener : handler.getContext().getPublicChatMessageListeners())
			listener.onPublicChatMessage(chatMessage, source);
		log.info("handling public chat message: {}", chatMessage);
		publicChatMessages.add(chatMessage);
	}

	public void onPrivateChatMessage(ChatMessage chatMessage, IParticipant source) {
		for (OnPrivateChatMessageListener listener : handler.getContext().getPrivateChatMessageListener())
			listener.onPrivateChatMessage(chatMessage, source);
		synchronized (privateChatMessages) {
			if (!privateChatMessages.containsKey(source.getUserId()))
				privateChatMessages.put(source.getUserId(), new ArrayList<ChatMessage>());
			privateChatMessages.get(source.getUserId()).add(chatMessage);
		}
		log.info("handling private chat message from {}: {}", source, chatMessage);
	}
	
	public List<ChatMessage> getPublicChatMessage() {
		return publicChatMessages;
	}
	
	public Map<String, List<ChatMessage>> getPrivateChatMessage() {
		return privateChatMessages;
	}
	
	private void onMessageReceived(Object obj) {
		ChatMessageVO msg = new ChatMessageVO();
		msg.fromMap((Map<String, Object>) obj);
		
		log.error("Chat message received\n{}", msg.toString());
		
		//\TODO do something with the received message
	}

	public boolean onMessageFromServer(Command command) {
		switch (version) {
			case ApplicationService.VERSION_0_9:
			case ApplicationService.VERSION_0_81:
				return handle0Dot9MessageFromServer(command);
			default:
				return handleMessageFromServer(command);
		}
	}

	private boolean handleMessageFromServer(Command command) {
		String type = (String) command.getArg(0);
		if (type.equals("ChatReceivePublicMessageCommand") || type.equals("ChatReceivePrivateMessageCommand")) {
			onMessageReceived(command.getArg(1));
			return true;
		} else if (type.equals("ChatRequestMessageHistoryReply")) {
			Map<String, Object> args = (Map<String, Object>) command.getArg(1);
			int count = Double.valueOf(args.get("count").toString()).intValue();
			List<Object> messages = (List<Object>) Arrays.asList((Object[]) args.get("messages"));
			for (int i = 0; i < count; ++i) {
				onMessageReceived(messages.get(i));
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean handle0Dot9MessageFromServer(Command command) {
		String msgName = (String) command.getArg(0);
		switch (msgName) {
			case "ChatRequestMessageHistoryReply":
				handleChatRequestMessageHistoryReply((Map<String, Object>) command.getArg(1));
				return true;
			case "ChatReceivePublicMessageCommand":
				handleChatReceivePublicMessageCommand((Map<String, Object>) command.getArg(1));
				return true;
			case "ChatReceivePrivateMessageCommand":
				handleChatReceivePrivateMessageCommand((Map<String, Object>) command.getArg(1));
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean onCommand(String resultFor, Command command) {
		if (onGetChatMessages(resultFor, command)) {
			return true;
		} else {
			return false;
		}
	}

	private void handleChatRequestMessageHistoryReply(Map<String, Object> msg) {

	}

	private void handleChatReceivePublicMessageCommand(Map<String, Object> msg) {

	}

	private void handleChatReceivePrivateMessageCommand(Map<String, Object> msg) {

	}
}