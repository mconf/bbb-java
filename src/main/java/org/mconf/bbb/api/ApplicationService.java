package org.mconf.bbb.api;

public class ApplicationService {
	public final static String VERSION_0_7 = "0.7";
	public final static String VERSION_0_8 = "0.8";
	private String serverUrl;
	private String serverVersion;

	public ApplicationService(String serverUrl) {
		// the application server is different from the API server
		this.serverUrl = cleanAddress(serverUrl);
		this.serverVersion = JoinServiceProxy.getBigBlueButtonVersion(serverUrl);
	}

	public ApplicationService(String serverUrl, String version) {
		// the application server and the API server are the same
		this.serverUrl = cleanAddress(serverUrl);
		this.serverVersion = version;
	}

	public String getServerUrl() {
		return serverUrl;
	}
	
	public String getVersion() {
		return serverVersion;
	}
	
	private String cleanAddress(String addr) {
		if (addr.matches("http://.*"))
			addr = addr.substring(7);
		if (addr.matches("https://.*"))
			addr = addr.substring(8);
		if (addr.matches(".*:\\d*"))
			addr = addr.substring(0, addr.lastIndexOf(":"));
		return addr;
	}
}
