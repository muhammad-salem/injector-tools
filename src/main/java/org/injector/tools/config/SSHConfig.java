package org.injector.tools.config;

import org.injector.tools.config.type.SSHProxyType;

public class SSHConfig {

	

	private String host = "";
	private int port = 443;
	private String user = "";
    private String password = "";

    private int localSocksPort = 1080;
    private int localHttpPort = 8123;
    
    private boolean useCompression = true;
    private boolean isDebuggable = true;
    private int timeout = 4500;
    
    private SSHProxyType sshProxyType = SSHProxyType.HTTPProxy;
    private String proxyHost = "127.0.0.1";
	private int proxyPort = 8989;
	
    private String payload = "[raw][crlf][crlf]";

    public SSHConfig() { }
    public SSHConfig(String sshHost,
                     int sshPort,
                     String sshUser,
                     String sshPassword,
                     String proxyHost,
                     int proxyPort,
                     int localSocksPort,
                     SSHProxyType sshProxyType,
                     boolean isDebuggable,
                     boolean useCompression) {
        this.host = sshHost;
        this.port = sshPort;
        this.user = sshUser;
        this.password = sshPassword;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.localSocksPort = localSocksPort;
        this.sshProxyType = sshProxyType;
        this.isDebuggable = isDebuggable;
        this.useCompression = useCompression;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setLocalSocksPort(int localSocksPort) {
        this.localSocksPort = localSocksPort;
    }

	public void setLocalHttpPort(int localHttpPort) {
		this.localHttpPort = localHttpPort;
	}
	public void setSSHProxyType(SSHProxyType sshProxyType) {
        this.sshProxyType = sshProxyType;
    }

    public void setDebuggable(boolean debuggable) {
        this.isDebuggable = debuggable;
    }


    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public int getLocalSocksPort() {
        return localSocksPort;
    }

    public int getLocalHttpPort() {
		return localHttpPort;
	}
    
    public SSHProxyType getSSHProxyType() {
        return sshProxyType;
    }

    public boolean isDebuggable() {
        return isDebuggable;
    }
    public boolean isUseCompression() {
        return useCompression;
    }

    public void setUseCompression(boolean useCompression) {
        this.useCompression = useCompression;
    }
	public String getPayload() {
		return payload;
	}
	public void setPayload(String payload) {
		this.payload = payload;
	}

    public void setTimeout(int timeout) { this.timeout = timeout; }
    public int getTimeout() { return timeout; }

}
