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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import org.jboss.netty.channel.Channel;
import org.mconf.bbb.deskshare.DeskshareConnection;
import org.mconf.bbb.Module;
import org.mconf.bbb.api.ApplicationService;
import org.red5.server.api.IAttributeStore;
import org.red5.server.api.so.IClientSharedObject;
import org.red5.server.api.so.ISharedObjectBase;
import org.red5.server.api.so.ISharedObjectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.CommandAmf0;

public class DeskshareModule implements ISharedObjectListener {
	private static final Logger log = LoggerFactory.getLogger(DeskshareModule.class);

	private final DeskshareConnection handler;
	private final Channel channel;
	private final IClientSharedObject deskSO;
	private final String conference;

	private int width;
	private int height;
	private Boolean publishing;

	public DeskshareModule(DeskshareConnection handler, Channel channel) {
		this.handler = handler;
		this.channel = channel;
		this.conference = this.handler.getContext().getJoinService().getJoinedMeeting().getConference();

		deskSO = this.handler.getSharedObject(this.conference + "-deskSO", false);
		deskSO.addSharedObjectListener(this);
		deskSO.connect(this.channel);
	}

	@Override
	public void onSharedObjectClear(ISharedObjectBase so) {
		log.debug("onSharedObjectClear");
		checkIfStreamIsPublishing();
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
	public void onSharedObjectSend(ISharedObjectBase so, String method, List<?> params) {
		log.debug("onSharedObjectSend");

		if (so.equals(deskSO)) {}
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so, String key, Object value) {
		log.debug("onSharedObjectUpdate 1");
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so, IAttributeStore values) {
		log.debug("onSharedObjectUpdate 2");
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so, Map<String, Object> values) {
		log.debug("onSharedObjectUpdate 3");
	}

	public boolean onCommand(String resultFor, Command command) {
		return false;
	}

	public boolean onMessageFromServer(Command command) {
		return false;
	}

	private void checkIfStreamIsPublishing() {
		Command cmd = new CommandAmf0("deskshare.checkIfStreamIsPublishing", null);
		this.handler.writeCommandExpectingResult(this.channel, cmd);
	}
}