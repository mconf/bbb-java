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

import org.mconf.bbb.BigBlueButtonClient;
import org.mconf.bbb.deskshare.DeskshareConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.client.ClientOptions;
import com.flazr.util.Utils;

public class BbbDeskshareConnection extends DeskshareConnection {
	private static final Logger log = LoggerFactory.getLogger(BbbDeskshareConnection.class);

	public BbbDeskshareConnection(BigBlueButtonClient context) {
		super(null, context);

		options = new ClientOptions();
		options.setClientVersionToUse(Utils.fromHex("00000000"));
		options.setHost(context.getJoinService().getApplicationService().getServerUrl());
		options.setStreamName(context.getJoinService().getJoinedMeeting().getConference());
		options.setAppName("deskShare");
	}

	public boolean start() {
		log.warn("Connecting BbbDeskshareConnection");
		return connect();
	}

	public void stop() {
		disconnect();
	}
}
