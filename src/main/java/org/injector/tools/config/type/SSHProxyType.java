package org.injector.tools.config.type;

import org.injector.tools.config.SSHConfig;
import org.injector.tools.speed.NetworkMonitorSpeed;
import org.injector.tools.ssh.proxyhandler.*;

public enum SSHProxyType {
    STOP,
    DIRECT,
    DIRECT_PROXY,
    HTTP_PROXY,
    SOCKS_PROXY,
    DIRECT_INJECT,
    INJECT_PROXY_HTTP,
    SNI_INJECTION;

    public ProxySocket getProxy(SSHConfig config, NetworkMonitorSpeed monitorSpeed) {
//		monitorSpeed = null;
        return switch (this) {
            // same as normal connection - use no proxy aka Direct connection.
            case DIRECT_PROXY -> new DirectProxy(config.getProxyHost(), config.getProxyPort(), monitorSpeed);
            // same as use local proxy for {now}
            // Suppose to {implement an extra code}
            // to check configuration then chose the right way to connect , or tray them all till it.
            case HTTP_PROXY ->
                // use proxy before connect to ssh host , defined with local proxy
                    new HttpProxy(config.getProxyHost(), config.getProxyPort(), monitorSpeed);
            case DIRECT_INJECT ->
                // used as direct inject to ssh host
                    new DirectInjectProxy(config.getHost(), config.getPort(), config.getPayload(), monitorSpeed);
            case INJECT_PROXY_HTTP ->
                // used to inject to proxy host
                // chose between the proxy payload or direct payload option.
                    new InjectHttpProxy(config.getProxyHost(), config.getProxyPort(), config.getPayload(), monitorSpeed);
            case SNI_INJECTION ->
                // used to manipulate SNI
                    new SniHostNameInjectionProxy(config.getSniHostName(), monitorSpeed);
            case SOCKS_PROXY ->
                // not implemented class yet // do nothing
                    new Socks5Proxy(config.getProxyHost(), config.getProxyPort(), monitorSpeed);

            // no proxy
            default -> null;
        };
    }

}