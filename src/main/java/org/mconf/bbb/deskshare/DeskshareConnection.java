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

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.mconf.bbb.BigBlueButtonClient;
import org.mconf.bbb.RtmpConnection;
import org.red5.server.so.SharedObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.RtmpDecoder;
import com.flazr.rtmp.RtmpEncoder;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.ClientHandshakeHandler;
import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.Command;

public class DeskshareConnection extends RtmpConnection {

	private static final Logger log = LoggerFactory.getLogger(DeskshareConnection.class);

	public DeskshareConnection(ClientOptions options, BigBlueButtonClient context) {
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
				pipeline.addLast("handler", DeskshareConnection.this);
				return pipeline;
			}
		};
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		options.setArgs(context.getJoinService().getJoinedMeeting().getConference());
		Command connect = Command.connect(options);
		log.warn(connect.toString());
		writeCommandExpectingResult(e.getChannel(), connect);
	}

	public String connectGetCode(Command command) {
		return ((Map<String, Object>) command.getArg(0)).get("code").toString();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent me) {
		final Channel channel = me.getChannel();
		final RtmpMessage message = (RtmpMessage) me.getMessage();
		switch(message.getHeader().getMessageType()) {
			case CONTROL:
				break;

			case COMMAND_AMF0:
			case COMMAND_AMF3:
				Command command = (Command) message;                
				String name = command.getName();
				log.debug("server command: {}", name);
				switch (name) {
					case "_result":
						handleCommandResult(command, channel);
						break;
					case "_error":
						handleCommandError(command);
						break;
					case "onMessageFromServer":
						handleCommandMessageFromServer(command);
						break;
					default:
						log.error("unknown command: {}", name);
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

	private void handleCommandResult(Command cmd, Channel channel) {
		String resultFor = transactionToCommandMap.get(cmd.getTransactionId());

		if (resultFor != null) {
			log.info("result for method call: {}", resultFor);
			if (resultFor.equals("connect")) {
				String code = connectGetCode(cmd);
				if (code.equals("NetConnection.Connect.Success")) {
					context.createDeskshareModule(this, channel);
				} else {
					log.error("method connect result in {}, quitting", code);
					log.debug("connect response: {}", cmd.toString());
					channel.close();
				}
			} else context.onCommand(resultFor, cmd);
		} else log.warn("result for method without tracked transaction");
	}

	private void handleCommandError(Command cmd) {
		Map<String, Object> args = (Map<String, Object>) cmd.getArg(0);
		log.error(args.get("code").toString() + ": " + args.get("description").toString());
	}

	private void handleCommandMessageFromServer(Command cmd) {
		context.onMessageFromServer(cmd, version);
	}
}
