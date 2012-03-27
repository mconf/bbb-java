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

package org.mconf.bbb.video;

import org.mconf.bbb.BigBlueButtonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.RtmpReader;
import com.flazr.rtmp.client.ClientOptions;
import com.flazr.util.Utils;


public class BbbVideoPublisher {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BbbVideoPublisher.class);

	public VideoPublisherConnection videoConnection;
	private String streamName;
	private BigBlueButtonClient context;
	private ClientOptions opt;
	
	public BbbVideoPublisher(BigBlueButtonClient context, RtmpReader reader, String streamName) {
		this.context = context;
		this.streamName = streamName;

		opt = new ClientOptions();
		opt.setClientVersionToUse(Utils.fromHex("00000000"));
		opt.setHost(context.getJoinService().getApplicationService().getServerUrl());
		opt.setAppName("video/" + context.getJoinService().getJoinedMeeting().getConference());
		opt.publishLive();
		opt.setStreamName(streamName);
		opt.setReaderToPublish(reader);
	}
	
	public void setLoop(boolean loop) {
		opt.setLoop(loop? Integer.MAX_VALUE: 0);
	}
	
	public void start() {
		context.getUsersModule().addStream(streamName);
		videoConnection = new VideoPublisherConnection(opt, context);
		videoConnection.connect();
	}
	
	public void stop() {
		context.getUsersModule().removeStream(streamName);
		videoConnection.disconnect();
	}
}
