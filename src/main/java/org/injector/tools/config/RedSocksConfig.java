package org.injector.tools.config;

public class RedSocksConfig {

    // dir start type host port auth user pass

    public enum RedSocksState{
        start,stop
    }

    public enum RedSocksType{
        http,socks5,socks4
    }

    private String directory;
    private RedSocksState state;
    private RedSocksType proxyType;
    private String proxyHost;
    private int proxyPort;
    private boolean useAuth;
    private String proxyUser;
    private String proxyPass;


    public RedSocksConfig(){}
    public RedSocksConfig(String directory, RedSocksState state, RedSocksType proxyType, String proxyHost, int proxyPort) {
        this(directory,state,proxyType,proxyHost,proxyPort, false, null, null);
    }
    public RedSocksConfig(String directory, RedSocksState state, RedSocksType proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPass) {
        this(directory,state,proxyType,proxyHost,proxyPort,true, proxyUser, proxyPass);
    }
    public RedSocksConfig(String directory, RedSocksState state, RedSocksType proxyType,
                          String proxyHost, int proxyPort, boolean useAuth, String proxyUser, String proxyPass) {
        this.directory = directory;
        this.state = state;
        this.proxyType = proxyType;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.useAuth = useAuth;
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public RedSocksState getState() {
        return state;
    }

    public void setState(RedSocksState state) {
        this.state = state;
    }

    public RedSocksType getProxyType() {
        return proxyType;
    }

    public void setProxyType(RedSocksType proxyType) {
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

    public boolean isUseAuth() {
        return useAuth;
    }

    public void setUseAuth(boolean useAuth) {
        this.useAuth = useAuth;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPass() {
        return proxyPass;
    }

    public void setProxyPass(String proxyPass) {
        this.proxyPass = proxyPass;
    }


}
