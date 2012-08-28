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
import org.sipdroid.codecs.Speex;
import org.sipdroid.net.RtpPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.flazr.rtmp.RtmpReader;
import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.Audio;
import com.flazr.util.Utils;

public class BbbVoiceConnection extends VoiceConnection {
	//private FlvWriter writer; // to record the received audio to a flv file
	private static final Logger log = LoggerFactory.getLogger(BbbVoiceConnection.class);
	Speex speex;
	int minBufferSize;
	AudioTrack track;
	
	final int BUFFER_SIZE = 1024;
	

	public BbbVoiceConnection(BigBlueButtonClient context, RtmpReader reader) {
		super(null, context);
		//writer = new FlvWriter("received.flv");

		options = new ClientOptions();
		options.setClientVersionToUse(Utils.fromHex("00000000"));
		options.setHost(context.getJoinService().getApplicationService().getServerUrl());
		options.setAppName("sip");
		options.publishLive();
		if (reader != null) {
			options.setReaderToPublish(reader);
		}
				
		setCodec();
	}

	public void setLoop(boolean loop) {
		options.setLoop(loop ? Integer.MAX_VALUE : 0);
	}

	public boolean start() {
		return connect();
	}

	public void stop() {
		track.stop();
		track.release();
		speex.close();
		disconnect();
	}
	
	@Override
	protected void onAudio(Audio audio) {
		//log.debug("received audio package: {}", audio.getHeader().getTime());
		
		
		byte[] audioEncoded = audio.getData();
		log.debug("tamanho array encoded = {}",audioEncoded.length);
		
		
		short[] audioDecoded = new short[1024];		
		int sizeDecoded = speex.decode(audioEncoded, audioDecoded, audioEncoded.length);
		log.debug("tamanho array decoded = {}",sizeDecoded);
		
		track.write(audioDecoded, 0, sizeDecoded );
	
	}
	
	private void setCodec()
	{
		speex = new Speex();
		speex.init();
		
		minBufferSize = AudioTrack.getMinBufferSize(speex.samp_rate(), 
			             							AudioFormat.CHANNEL_CONFIGURATION_MONO, 
			             							AudioFormat.ENCODING_PCM_16BIT);
		//int mu = speex.samp_rate()/8000;
		
		//if (minBufferSize < 2*2*BUFFER_SIZE*3*mu)
				//minBufferSize = 2*2*BUFFER_SIZE*3*mu;
		
		
		track = new AudioTrack(AudioManager.STREAM_MUSIC, 
	  			   speex.samp_rate(), 
	  			   AudioFormat.CHANNEL_CONFIGURATION_MONO, 
	  			   AudioFormat.ENCODING_PCM_16BIT,
	  			   minBufferSize, 
	  			   AudioTrack.MODE_STREAM);		
		
		track.play();	
	}	
	
}