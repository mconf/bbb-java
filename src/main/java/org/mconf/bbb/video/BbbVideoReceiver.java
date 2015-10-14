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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mconf.bbb.BigBlueButtonClient;
import org.mconf.bbb.users.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.Video;
import com.flazr.util.Utils;

public class BbbVideoReceiver {

	protected class VideoConnection extends VideoReceiverConnection {

		public VideoConnection(ClientOptions options,
				BigBlueButtonClient context) {
			super(options, context);
		}

		@Override
		protected void onVideo(Video video) {
			BbbVideoReceiver.this.onVideo(video);
		}
		
	}
	
	private static final Logger log = LoggerFactory.getLogger(BbbVideoReceiver.class);

	private String userId;
	private String streamName;
	private VideoConnection videoConnection;
	private Boolean firstPacket = true;
	
	public BbbVideoReceiver(String userId, BigBlueButtonClient context) {
		this.userId = userId;

		ClientOptions opt = new ClientOptions();
		opt.setClientVersionToUse(Utils.fromHex("00000000"));
		opt.setHost(context.getJoinService().getApplicationService().getServerUrl());
		opt.setAppName("video/" + context.getJoinService().getJoinedMeeting().getConference());
		
		streamName = null;
		for (Participant p : context.getParticipants()) {
			if (p.getUserId().equals(userId)) {
				if (p.hasStream()) {
					streamName = p.getStatus().getStreamName();
				}
				break;
			}
		}
		
		if (streamName == null) {
			log.error("The userId = {} has no stream", userId);
			return;
		}
		
		opt.setWriterToSave(null);
		opt.setStreamName(streamName);

		videoConnection = new VideoConnection(opt, context);
	}
	
	protected void onVideo(Video video) {
		log.debug("received video package: {}", video.getHeader().getTime());
		if (firstPacket) {
			firstPacket = false;
			log.info("Receiving video {} from user {}", streamName, userId);
		}
	}
	
	public void start() {
		if (videoConnection != null)
			videoConnection.connect();
	}
	
	public void stop() {
		if (videoConnection != null)
			videoConnection.disconnect();
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getStreamName() {
		return streamName;
	}
	
	public float getAspectRatio() {
		return getAspectRatio(userId, streamName);
	}
	
	public static float getAspectRatio(String userId, String streamName) {
		if (streamName != null && streamName.contains(userId)) {
			
			/*
			 * 	0.7 -> 120x1601
			 * 	0.8 -> 120x1601-131292666
			 * 	0.81 -> 120x160-1-131292666
			 */
			
			/*
			 * 0.81 stream name format
			 */
			Pattern streamNamePattern = Pattern.compile("(\\d+)[x](\\d+)[-]\\d+[-]\\d+");
			Matcher matcher = streamNamePattern.matcher(streamName);
			if( matcher.matches() ) {
				String widthStr = matcher.group(1);
				String heightStr = matcher.group(2);
				int width = Integer.parseInt(widthStr);
				int height = Integer.parseInt(heightStr);
				return width / (float) height;
			}
			
			/*
			 * 0.7 or 0.8 stream name format
			 */
			streamNamePattern = Pattern.compile("(\\d+)[x](\\d+)([-]\\d+)?");
			matcher = streamNamePattern.matcher(streamName);
			if( matcher.matches() ) {				
				String widthStr = matcher.group(1);
				String heightAndId = matcher.group(2);
				String heightStr = heightAndId.substring(0, heightAndId.lastIndexOf(userId));
				int width = Integer.parseInt(widthStr);
				int height = Integer.parseInt(heightStr);				
				return width / (float) height;
			}			
		}		
		return -1;
	}
}
