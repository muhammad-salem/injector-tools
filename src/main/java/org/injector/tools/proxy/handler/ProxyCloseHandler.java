package org.injector.tools.proxy.handler;

import lombok.extern.slf4j.Slf4j;
import org.injector.tools.config.HostProxyConfig;

import java.io.IOException;
import java.nio.channels.SocketChannel;

@Slf4j
public class ProxyCloseHandler extends DirectCloseHandler {

    public ProxyCloseHandler(SocketChannel clientSocket, HostProxyConfig proxyConfig, ChannelSelector channelSelector) {
        super(clientSocket, proxyConfig, channelSelector);
    }

    protected void connectToProxyServer() {
        try {
            connectToProxyServer(proxyConfig.getProxyHost(), proxyConfig.getProxyPort());
            log.info("creates a proxy socket");
        } catch (IOException e) {
            log.info("error", "Can't connect to " + proxyConfig.getProxyHost() + ":" + proxyConfig.getProxyPort() + "\n".concat(e.getMessage()));
        }

    }

}
