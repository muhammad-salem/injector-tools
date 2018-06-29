package org.injector.tools.config;


import org.injector.tools.config.type.HostProxyType;

public class HostProxyConfig {

	private HostProxyType proxyType = HostProxyType.HTTP;
	
    private String proxyHost = "";
    private int proxyPort = -1;

    private String proxyUser = "";
    private String proxyPassword = "";
    
    private String payload = "[raw][crlf][crlf]";


    public HostProxyConfig() { }
    public HostProxyConfig(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public HostProxyConfig(String proxyHost, int proxyPort, String proxyUser, String proxyPassword, HostProxyType proxyType) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;
        this.proxyType = proxyType;
    }
    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public HostProxyType getProxyType() {
        return proxyType;
    }

    public void setProxyType(HostProxyType proxyType) {
        this.proxyType = proxyType;
    }





    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }
	public String getPayload() {
		return payload;
	}
	public void setPayload(String payload) {
		this.payload = payload;
	}
	
	@Override
	public String toString() {
		return "Proxy[" + proxyUser + ":" + proxyPassword + "@" + proxyHost + ":" + proxyPort + "]";
	}
	public boolean isDirect() {
		return proxyType == HostProxyType.DIRECT || proxyType == HostProxyType.DIRECT_CLOSE;
	}



}
