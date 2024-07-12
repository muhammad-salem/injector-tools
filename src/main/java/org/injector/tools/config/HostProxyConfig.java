package org.injector.tools.config;


import lombok.Getter;
import lombok.Setter;
import org.injector.tools.config.type.HostProxyType;

@Setter
@Getter
public class HostProxyConfig {

    private HostProxyType proxyType = HostProxyType.HTTP;

    private String proxyHost = "";
    private int proxyPort = -1;

    private String sniHostName = "";

    private String proxyUser = "";
    private String proxyPassword = "";

    private String payload = "[raw][crlf][crlf]";


    public HostProxyConfig() {
    }

    public HostProxyConfig(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public HostProxyConfig(String proxyHost, int proxyPort, String proxyUser, String proxyPassword, HostProxyType proxyType, String sniHostName) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;
        this.proxyType = proxyType;
        this.sniHostName = sniHostName;
    }

    @Override
    public String toString() {
        return "Proxy[" + proxyUser + ":" + proxyPassword + "@" + proxyHost + ":" + proxyPort + "]";
    }

    public boolean isDirect() {
        return switch (proxyType) {
            case DIRECT, DIRECT_CLOSE, SNI_HOST_NAME ->  true;
            default -> false;
        };
    }

}
