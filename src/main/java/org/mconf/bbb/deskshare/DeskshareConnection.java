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

package org.mconf.bbb.deskshare;

import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.mconf.bbb.BigBlueButtonClient;
import org.mconf.bbb.RtmpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.RtmpDecoder;
import com.flazr.rtmp.RtmpEncoder;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.ClientHandshakeHandler;
import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.Control;
import com.flazr.rtmp.message.MessageType;

public class DeskshareConnection extends RtmpConnection {

	private static final Logger log = LoggerFactory.getLogger(DeskshareConnection.class);
	private boolean firstPacket;

	public DeskshareConnection(ClientOptions options, BigBlueButtonClient context) {
		super(options, context);
		closeChannelWhenStreamStopped = false;
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
				pipeline.addLast("handler", DeskshareConnection.this);
				return pipeline;
			}
		};
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		options.setArgs(context.getJoinService().getJoinedMeeting().getConference());
		Command connect = Command.connect(options);
		writeCommandExpectingResult(e.getChannel(), connect);
	}

	public String connectGetCode(Command command) {
		return ((Map<String, Object>) command.getArg(0)).get("code").toString();
	}

	@Override
	protected void onCommandResult(Channel channel, Command command,
			String resultFor) {
		log.debug("result for method call: {}", resultFor);
		if (resultFor.equals("connect")) {
			String code = connectGetCode(command);
			if (code.equals("NetConnection.Connect.Success")) {
				context.createDeskshareModule(this, channel);
			} else {
				log.error("method connect result in {}, quitting", code);
				log.debug("connect response: {}", command.toString());
				channel.close();
			}
        } else if(resultFor.equals("createStream")) {
            streamId = ((Double) command.getArg(0)).intValue();
            log.debug("playStreamId to use: {}", streamId);
            writer = options.getWriterToSave();
            ClientOptions newOptions = new ClientOptions();
            newOptions.setStreamName(options.getStreamName());
            firstPacket = true;
            channel.write(Command.play(streamId, newOptions));
            channel.write(Control.setBuffer(streamId, 0));
		} else {
			context.getDeskshareModule().onCommand(resultFor, command);
		}
	}
	
	@Override
	protected void onCommandCustom(Channel channel, Command command, String name) {
		if (name.equals("onMessageFromServer")) {
			context.onMessageFromServer(command, version);
		}
	}

	public void createStream(Channel channel) {
		writeCommandExpectingResult(channel, Command.createStream());
	}
	
	@Override
	protected void onMultimedia(Channel channel, RtmpMessage message) {
		super.onMultimedia(channel, message);
		if (message.getHeader().getMessageType() == MessageType.VIDEO) {
			log.debug("received deskshare package: {}", message.getHeader().getTime());
			if (firstPacket) {
				firstPacket = false;
				log.info("Receiving deskshare stream");
				context.getDeskshareModule().sendStartedViewing(channel);
			}
		}
	}

	public void destroyStream(Channel channel) {
		writeCommandExpectingResult(channel, Command.closeStream(streamId));
	}
}
