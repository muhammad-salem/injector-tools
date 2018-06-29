package org.injector.tools.config;

import org.injector.tools.config.type.LocalProxyType;

public class LocalProxyConfig {

    public static String LOCALHOST  = "localhost";
    public static String LOCALIP    = "127.0.0.1";
    public static String LOCALIPAll = "0.0.0.0";


    private LocalProxyType localProxyType = LocalProxyType.TRANSPARENT;
    private int localProxyPort = 8989;
    
    private HostProxyConfig hostProxyConfig = new HostProxyConfig();


    public LocalProxyConfig() {
    	this(0, LocalProxyType.STOP ,new HostProxyConfig());
    }
    
    public LocalProxyConfig(int localProxyPort) {
    	this(localProxyPort, LocalProxyType.TRANSPARENT ,new HostProxyConfig());
    }
    public LocalProxyConfig(int localProxyPort, HostProxyConfig proxyConfig) {
        this(localProxyPort, LocalProxyType.TRANSPARENT, proxyConfig);
    }

    public LocalProxyConfig(int localProxyPort, LocalProxyType localProxyType, HostProxyConfig hostProxyConfig) {
        this.localProxyPort = localProxyPort;
        this.localProxyType = localProxyType;
        this.setHostProxyConfig(hostProxyConfig);
    }


    public int getLocalProxyPort() {
        return localProxyPort;
    }
    public void setLocalProxyPort(int localProxyPort) {
        this.localProxyPort = localProxyPort;
    }
    public LocalProxyType getLocalProxyType() { return localProxyType; }
    public void setLocalProxyType(LocalProxyType localProxyType) { this.localProxyType = localProxyType; }
    /**
     * 
     * @return true if only localProxyType not equal to STOP
     */
    public boolean isAllowToRun(){ return localProxyType != LocalProxyType.STOP; }
	public HostProxyConfig getHostProxyConfig() {
		return hostProxyConfig;
	}
	public void setHostProxyConfig(HostProxyConfig hostProxyConfig) {
		this.hostProxyConfig = hostProxyConfig;
	}

    
    
    
}
