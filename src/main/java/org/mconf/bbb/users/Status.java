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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mconf.bbb.api.ApplicationService;

public class Status {
	
	private boolean raiseHand;
	private boolean hasStream;
	private boolean presenter;	
	private List<String> streamNameList;
	
	public Status(Map<String, Object> param, String appServerVersion) {
		streamNameList = new ArrayList<String>();
		decode(param, appServerVersion);
	}
	
	public Status() {
		streamNameList = new ArrayList<String>();
	}

	/*
	 * example:
	 * {raiseHand=false, hasStream=false, presenter=true}
	 */
	public void decode(Map<String, Object> param, String appServerVersion) {
		raiseHand = (Boolean) param.get("raiseHand");
		setHasStream(param.get("hasStream"));
		if (appServerVersion.equals(ApplicationService.VERSION_0_7)) {
			String name = hasStream? (String) param.get("streamName") : "";
			setStreamName(name);
		}
		presenter = (Boolean) param.get("presenter");
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
	
	public boolean doesHaveStream(String streamName) {
		return streamNameList.contains(streamName) ? true : false;
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
				if (tuple[0].equals("stream")) {
					setStreamName(tuple[1]);
				}
			}
		}
	}
	
	public String getStreamName() {
		/*
		 * returns all the stream names (SN) in the format SN|SN|...|SN
		 * 
		 * example: 160x12042-12642868|160x12042-12742666
		 * 
		 */
		
		String streamName = "";
		for(int i = 0; i < streamNameList.size(); i++) {
			
			if(i != 0)
				streamName+= "|";
			
			streamName += streamNameList.get(i);
		}
		return streamName;
	}
	
	public String getStreamName(int index) {
		/*
		 * returns the stream name at position 'index' in the streamNameList
		 * 
		 * example: 160x12042-12742666
		 */
		
		return streamNameList.get(index);
	}

	public void setStreamName(String streamName) {
		/*
		 * streamName is a series of one or more stream names separated by a '|'
		 * 
		 * example: 160x12042-12642868|160x12042-12742666
		 * 
		 */
		
		String [] names = streamName.split("\\|");
		
		if(!streamNameList.isEmpty())
			streamNameList.clear();
		
		for(String name:names)
			streamNameList.add(name);
	}

	public boolean isPresenter() {
		return presenter;
	}

	public void setPresenter(boolean presenter) {
		this.presenter = presenter;
	}
	
	public int getNumberOfStreams() {
		return streamNameList.size();
	}

	@Override
	public String toString() {
		return "Status [hasStream=" + hasStream + ", presenter=" + presenter
				+ ", raiseHand=" + raiseHand + ", streamName=" + getStreamName()
				+ "]";
	}
	
	@Override
	public Status clone() {
		Status clone = new Status();
		clone.hasStream = this.hasStream;
		clone.presenter = this.presenter;
		clone.raiseHand = this.raiseHand;
		
		for(String name:this.streamNameList)
			clone.streamNameList.add(name);
		
		return clone;
	}
}
