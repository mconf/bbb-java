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

package org.mconf.bbb.users;

import java.util.Map;

import org.mconf.bbb.api.ApplicationService;

import org.json.JSONObject;
import org.json.JSONException;

public class Status {

	private boolean raiseHand;
	private boolean hasStream;
	private boolean presenter;
	private String streamName;
	private String mood;

	public Status(Map<String, Object> param, String appServerVersion) {
		decode(param, appServerVersion);
	}

	public Status(JSONObject jobj) {
		try {
			raiseHand = jobj.getBoolean("raiseHand");
			hasStream = jobj.getBoolean("hasStream");
			presenter = jobj.getBoolean("presenter");
			streamName = jobj.getString("webcamStream");
			// TODO: fix this to work with mconf 090
			mood = "";
//			mood = jobj.getString("mood");
		} catch (JSONException je) {
			System.out.println(je.toString());
		}
	}
	
	public Status() {
	}

	/*
	 * example:
	 * {raiseHand=false, hasStream=false, presenter=true}
	 */
	public void decode(Map<String, Object> param, String appServerVersion) {
		if (param.containsKey("raiseHand")) {
			raiseHand = (Boolean) param.get("raiseHand");
		}
		if (param.containsKey("mood")) {
			mood = (String) param.get("mood");
		}
		setHasStream(param.get("hasStream"));
		if (appServerVersion.equals(ApplicationService.VERSION_0_7))
			streamName = hasStream? (String) param.get("streamName") : "";
		presenter = (Boolean) param.get("presenter");
	}
	
	public String getMood() {
		return mood;
	}
	
	public void setMood(String value) {
		mood = value;
	}

	public boolean isRaiseHand() {
		return raiseHand;
	}

	public void setRaiseHand(boolean raiseHand) {
		this.raiseHand = raiseHand;
	}

	public boolean doesHaveStream() {
		return hasStream;
	}

	public void setHasStream(boolean hasStream) {
		this.hasStream = hasStream;
	}

	public void setHasStream(Object value) {
		if (value.getClass() == Boolean.class)
			hasStream = (Boolean) value;
		else {
			String[] params = ((String) value).split(",");
			hasStream = Boolean.valueOf(params[0]);
			for (int i = 1; i < params.length; ++i) {
				String[] tuple = params[i].split("=");
				if (tuple.length < 2)
					continue;
				if (tuple[0].equals("stream"))
					streamName = tuple[1];
			}
		}
	}

	public String getStreamName() {
		return streamName;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	public boolean isPresenter() {
		return presenter;
	}

	public void setPresenter(boolean presenter) {
		this.presenter = presenter;
	}

	@Override
	public String toString() {
		return "Status [hasStream=" + hasStream + ", presenter=" + presenter
				+ ", raiseHand=" + raiseHand + ", streamName=" + streamName
				+ "]";
	}
	
	@Override
	public Status clone() {
		Status clone = new Status();
		clone.hasStream = this.hasStream;
		clone.presenter = this.presenter;
		clone.raiseHand = this.raiseHand;
		clone.streamName = this.streamName;
		return clone;
	}

}
