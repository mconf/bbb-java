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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.mconf.bbb.BigBlueButtonClient.OnConnectedListener;
import org.mconf.bbb.BigBlueButtonClient.OnDisconnectedListener;
import org.mconf.bbb.api.ApplicationService;
import org.mconf.bbb.api.JoinedMeeting;
import org.red5.server.so.SharedObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.RtmpDecoder;
import com.flazr.rtmp.RtmpEncoder;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.ClientHandshakeHandler;
import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.CommandAmf0;
import com.flazr.rtmp.message.Control;

/*
 * - what happens when a client join a session
 * getMyUserId
 * participantsSO
 * participants.getParticipants
 * meetMeUsersSO
 * voice.getMeetMeUsers
 * voice.isRoomMuted
 * presentationSO
 * presentation.getPresentationInfo
 * presentation.assignPresenter
 * breakoutSO
 * drawSO
 * deskSO
 * deskshare.checkIfStreamIsPublishing
 * chat.getChatMessages
 * 
 * - web client module division
 * breakout  
 * chat  
 * deskshare  
 * example  
 * listeners  
 * phone  
 * present  
 * videoconf  
 * viewers  
 * whiteboard
 */

public class MainRtmpConnection extends RtmpConnection {

    private static final Logger log = LoggerFactory.getLogger(MainRtmpConnection.class);
	private boolean connected = false;
    
	public MainRtmpConnection(ClientOptions options, BigBlueButtonClient context) {
		super(options, context);
	}
	
	@Override
	protected ChannelPipelineFactory pipelineFactory() {
		return new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
		        final ChannelPipeline pipeline = Channels.pipeline();
		        pipeline.addLast("handshaker", new ClientHandshakeHandler(options));
		        pipeline.addLast("decoder", new RtmpDecoder());
		        pipeline.addLast("encoder", new RtmpEncoder());
		        pipeline.addLast("handler", MainRtmpConnection.this);
		        return pipeline;
			}
		};
	}

	@Override
	protected void onConnectedUnsuccessfully() {
		for (OnConnectedListener listener : context.getConnectedListeners())
			listener.onConnectedUnsuccessfully();
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		/*
		 * https://github.com/bigbluebutton/bigbluebutton/blob/master/bigbluebutton-client/src/org/bigbluebutton/main/model/users/NetConnectionDelegate.as#L102
		 * _netConnection.connect(?);
		 */		
			
		JoinedMeeting meeting = context.getJoinService().getJoinedMeeting();
		options.setArgs((Object[]) null);
		
		List<Object> args = new ArrayList<Object>();
		args.add(meeting.getFullname());
		args.add(meeting.getRole());
		if (!context.getJoinService().getApplicationService().getVersion().equals(ApplicationService.VERSION_0_81) &&
			!context.getJoinService().getApplicationService().getVersion().equals(ApplicationService.VERSION_0_9))
			args.add(meeting.getConference());
		if (context.getJoinService().getApplicationService().getVersion().equals(ApplicationService.VERSION_0_7))
			args.add(meeting.getMode());
		args.add(meeting.getRoom());
		args.add(meeting.getVoicebridge());
		args.add(meeting.doRecord());
		args.add(meeting.getExternUserID());
		args.add(meeting.getInternalUserID());
		if (meeting.isGuestDefined()) {
			args.add(meeting.isGuest());
		}
		if (context.getJoinService().getApplicationService().getVersion().equals(ApplicationService.VERSION_0_81) ||
			context.getJoinService().getApplicationService().getVersion().equals(ApplicationService.VERSION_0_9)) {
			args.add(context.getJoinService().getLockOnStart());
			args.add(context.getJoinService().getMuteOnStart());
			args.add(context.getJoinService().getLockSettings());
		}
		options.setArgs(args.toArray());
		
		writeCommandExpectingResult(e.getChannel(), Command.connect(options));
	}
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		super.channelDisconnected(ctx, e);
		log.debug("Rtmp Channel Disconnected");
		
		connected = false;
		for (OnDisconnectedListener l : context.getDisconnectedListeners())
			l.onDisconnected();
	}

    @SuppressWarnings("unchecked")
	public String connectGetCode(Command command) {
    	return ((Map<String, Object>) command.getArg(0)).get("code").toString();
    }

	private void setMyUserId(Channel channel) {
		JoinedMeeting meeting = context.getJoinService().getJoinedMeeting();
		context.setMyUserId(meeting.getInternalUserID());
		connected = true;
		for (OnConnectedListener l : context.getConnectedListeners())
			l.onConnectedSuccessfully();

		context.createUsersModule(this, channel);
	}

    public void doGetMyUserId(Channel channel) {
    	Command command = new CommandAmf0("getMyUserId", null);
    	writeCommandExpectingResult(channel, command);
    }
    
    public boolean onGetMyUserId(String resultFor, Command command) {
    	if (resultFor.equals("getMyUserId")) {
	    	context.setMyUserId((String) command.getArg(0));

			connected = true;
			for (OnConnectedListener l : context.getConnectedListeners())
				l.onConnectedSuccessfully();
			
	    	return true;
    	} else
    		return false;
    }

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent me) {
        final Channel channel = me.getChannel();
        final RtmpMessage message = (RtmpMessage) me.getMessage();
        switch(message.getHeader().getMessageType()) {
        	case CONTROL:
                Control control = (Control) message;
                switch(control.getType()) {
                    case PING_REQUEST:
                        final int time = control.getTime();
                        Control pong = Control.pingResponse(time);
                        channel.write(pong);
                        break;
                }
        		break;
        	
	        case COMMAND_AMF0:
	        case COMMAND_AMF3:
	            Command command = (Command) message;                
	            String name = command.getName();
	            log.debug("server command: {}", name);
	            if(name.equals("_result")) {
	                String resultFor = transactionToCommandMap.get(command.getTransactionId());
	                if (resultFor == null) {
	                	log.warn("result for method without tracked transaction");
	                	break;
	                }
	                log.info("result for method call: {}", resultFor);
					if(resultFor.equals("connect")) {
						String code = connectGetCode(command);
						if (code.equals("NetConnection.Connect.Success")) {
							// TODO: Here we have to change and get this internalID directly from the bot
							if (context.getJoinService().getApplicationService().getVersion().equals(ApplicationService.VERSION_0_81) ||
								context.getJoinService().getApplicationService().getVersion().equals(ApplicationService.VERSION_0_9)) {
								setMyUserId(channel);
								} else {
									doGetMyUserId(channel);
								}
							} else {
	                		log.error("method connect result in {}, quitting", code);
	                		log.debug("connect response: {}", command.toString());
	                		channel.close();
	                	}
	                	return;
					} else if (!context.getJoinService().getApplicationService().getVersion().equals(ApplicationService.VERSION_0_81) &&
								!context.getJoinService().getApplicationService().getVersion().equals(ApplicationService.VERSION_0_9)) {
						if (onGetMyUserId(resultFor, command)) {
							context.createUsersModule(this, channel);
							break;
						}
					}
	                context.onCommand(resultFor, command);
                	break;
	            } else if (name.equals("_error")) {
	                Map<String, Object> args = (Map<String, Object>) command.getArg(0);
	                log.error(args.get("code").toString() + ": " + args.get("description").toString());
	            } else if (name.equals("onMessageFromServer")) {
	                context.onMessageFromServer(command);
	                break;
	            }
	            break;
	            
	        case SHARED_OBJECT_AMF0:
	        case SHARED_OBJECT_AMF3:
	        	onSharedObject(channel, (SharedObjectMessage) message);
	        	break;
    		default:
    			log.info("ignoring rtmp message: {}", message);
	        	break;
        }
	}
	
	public boolean isConnected() {
		return connected;
	}
}
