package org.injector.tools.config;

import lombok.Getter;
import lombok.Setter;
import org.injector.tools.config.type.LocalProxyType;

@Setter
@Getter
public class LocalProxyConfig {

    public static String LOCALHOST = "localhost";
    public static String LOCAL_IP = "127.0.0.1";
    public static String LOCAL_IP_All = "0.0.0.0";


    private final LocalProxyType localProxyType;
    private int localProxyPort;

    private HostProxyConfig hostProxyConfig = new HostProxyConfig();


    public LocalProxyConfig() {
        this(0, LocalProxyType.STOP, new HostProxyConfig());
    }

    public LocalProxyConfig(int localProxyPort) {
        this(localProxyPort, LocalProxyType.STOP, new HostProxyConfig());
    }

    public LocalProxyConfig(int localProxyPort, HostProxyConfig proxyConfig) {
        this(localProxyPort, LocalProxyType.TRANSPARENT, proxyConfig);
    }

    public LocalProxyConfig(int localProxyPort, LocalProxyType localProxyType, HostProxyConfig hostProxyConfig) {
        this.localProxyPort = localProxyPort;
        this.localProxyType = localProxyType;
        this.setHostProxyConfig(hostProxyConfig);
    }


    /**
     * @return true if only localProxyType not equal to STOP
     */
    public boolean isAllowToRun() {
        return localProxyType != LocalProxyType.STOP;
    }

}
