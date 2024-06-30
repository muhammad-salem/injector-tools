package org.injector.tools.config;

import lombok.Getter;
import lombok.Setter;
import org.injector.tools.config.type.SSHProxyType;

@Setter
@Getter
public class SSHConfig {

    private String host = "";
    private int port = 443;
    private String user = "";
    private String password = "";

    private int localSocksPort = 1080;
    private int localHttpPort = 8123;

    private boolean useCompression = true;
    private int timeout = 4500;

    private SSHProxyType sshProxyType = SSHProxyType.HTTP_PROXY;
    private String proxyHost = "127.0.0.1";
    private int proxyPort = 8989;

    private String sniHost = "example.com";

    private String payload = "[raw][crlf][crlf]";

    public SSHConfig() {
    }

    public SSHConfig(String sshHost,
                     int sshPort,
                     String sshUser,
                     String sshPassword,
                     String proxyHost,
                     int proxyPort,
                     int localSocksPort,
                     SSHProxyType sshProxyType,
                     boolean useCompression,
                     String sniHost) {
        this.host = sshHost;
        this.port = sshPort;
        this.user = sshUser;
        this.password = sshPassword;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.localSocksPort = localSocksPort;
        this.sshProxyType = sshProxyType;
        this.useCompression = useCompression;
        this.sniHost = sniHost;
    }

}
