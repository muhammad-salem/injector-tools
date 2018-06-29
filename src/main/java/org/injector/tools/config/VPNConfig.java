package org.injector.tools.config;

public class VPNConfig {

	private boolean useVPN = false;
	private String vpnConfigFile = "";
	private String vpnUser = "vpn";
	private String vpnPass = "vpn";

	private String proxyHost = "127.0.0.1";
	private int proxyPort = 8123;
	
	public boolean isUseVPN() {
		return useVPN;
	}
	public String getVpnConfigFile() {
		return vpnConfigFile;
	}
	public String getVpnUser() {
		return vpnUser;
	}
	public String getVpnPass() {
		return vpnPass;
	}
	public void setUseVPN(boolean useVPN) {
		this.useVPN = useVPN;
	}
	public void setVpnConfigFile(String vpnConfigFile) {
		this.vpnConfigFile = vpnConfigFile;
	}
	public void setVpnUser(String vpnUser) {
		this.vpnUser = vpnUser;
	}
	public void setVpnPass(String vpnPass) {
		this.vpnPass = vpnPass;
	}
	public int getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}
	public String getProxyHost() {
		return proxyHost;
	}
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}


}
