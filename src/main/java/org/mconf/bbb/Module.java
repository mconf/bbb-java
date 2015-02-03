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

import org.jboss.netty.channel.Channel;

import com.flazr.rtmp.message.Command;

import java.util.Map;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONException;

public abstract class Module {
	protected final MainRtmpConnection handler;
	protected final Channel channel;
	protected final String version;
	
	public Module(final MainRtmpConnection handler, final Channel channel) {
		this.handler = handler;
		this.channel = channel;
		version = handler.getContext().getJoinService().getApplicationService().getVersion();
	}
	
	abstract public boolean onCommand(String resultFor, Command command);

	protected JSONObject getMessage(Object msg) {
		JSONObject jobj = null;
		try {
			Map<String, Object> map = (HashMap<String, Object>) msg;
			jobj = new JSONObject((String) map.get("msg"));
		} catch (JSONException je) {
			System.out.println(je.toString());
		}
		return jobj;
	}

	protected Object getFromMessage(JSONObject msg, String attribute) {
		Object obj = null;
		try {
			obj = (Object) msg.get(attribute);
		} catch (JSONException je) {
			System.out.println(je.toString());
		}
		return obj;
	}
}
