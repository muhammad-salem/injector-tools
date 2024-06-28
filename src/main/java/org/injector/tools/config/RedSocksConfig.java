package org.injector.tools.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedSocksConfig {

    // dir start type host port auth user pass

    private String directory;
    private RedSocksState state;
    private RedSocksType proxyType;
    private String proxyHost;
    private Integer proxyPort;
    private Boolean useAuth;
    private String proxyUser;
    private String proxyPass;
    public RedSocksConfig() {
    }
    public RedSocksConfig(String directory, RedSocksState state, RedSocksType proxyType, String proxyHost, int proxyPort) {
        this(directory, state, proxyType, proxyHost, proxyPort, false, null, null);
    }


    public RedSocksConfig(String directory, RedSocksState state, RedSocksType proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPass) {
        this(directory, state, proxyType, proxyHost, proxyPort, true, proxyUser, proxyPass);
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

    public enum RedSocksState {
        START, STOP
    }

    public enum RedSocksType {
        HTTP, SOCKS
    }

}
