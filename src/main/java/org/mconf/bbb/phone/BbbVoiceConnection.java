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

package org.mconf.bbb.phone;

import org.mconf.bbb.BigBlueButtonClient;
import org.mconf.bbb.phone.VoiceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.RtmpReader;
import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.Audio;
import com.flazr.util.Utils;

public class BbbVoiceConnection extends VoiceConnection {
	//private FlvWriter writer; // to record the received audio to a flv file
	private static final Logger log = LoggerFactory.getLogger(BbbVoiceConnection.class);

	public BbbVoiceConnection(BigBlueButtonClient context, RtmpReader reader) {
		super(null, context);
		//writer = new FlvWriter("received.flv");

		options = new ClientOptions();
		options.setClientVersionToUse(Utils.fromHex("00000000"));
		options.setHost(context.getJoinService().getServerUrl().toLowerCase().replace("http://", ""));
		options.setAppName("sip");
		options.publishLive();
		if (reader != null) {
			options.setReaderToPublish(reader);
		}
	}

	public void setLoop(boolean loop) {
		options.setLoop(loop ? Integer.MAX_VALUE : 0);
	}

	public boolean start() {
		return connect();
	}

	public void stop() {
		disconnect();
	}
	
	@Override
	protected void onAudio(Audio audio) {
		log.debug("received audio package: {}", audio.getHeader().getTime());
	}
}
