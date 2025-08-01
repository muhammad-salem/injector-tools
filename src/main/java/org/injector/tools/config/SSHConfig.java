package org.injector.tools.config;

import lombok.Getter;
import lombok.Setter;
import org.injector.tools.config.type.SSHProxyType;

@Setter
@Getter
public class SSHConfig {

    private String host = "";
    private Integer port = 443;
    private String user = "";
    private String password = "";

    private Integer localSocksPort = 1080;
    private Integer localHttpPort = 8123;

    private Boolean useCompression = true;
    private Boolean skipCheckHostKey = false;
    private int timeout = 4500;
    private int maxRetries = 1;

    private SSHProxyType sshProxyType = SSHProxyType.HTTP_PROXY;
    private String proxyHost = "127.0.0.1";
    private Integer proxyPort = 8989;

    private String sniHostName = "example.com";

    private String payload = "[raw][crlf][crlf]";

    public SSHConfig() {
    }

    public SSHConfig(String sshHost,
                     Integer sshPort,
                     String sshUser,
                     String sshPassword,
                     String proxyHost,
                     Integer proxyPort,
                     Integer localSocksPort,
                     SSHProxyType sshProxyType,
                     Boolean useCompression,
                     String sniHostName) {
        this.host = sshHost;
        this.port = sshPort;
        this.user = sshUser;
        this.password = sshPassword;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.localSocksPort = localSocksPort;
        this.sshProxyType = sshProxyType;
        this.useCompression = useCompression;
        this.sniHostName = sniHostName;
    }

}
