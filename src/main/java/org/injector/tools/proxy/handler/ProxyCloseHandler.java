package org.injector.tools.proxy.handler;

import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.log.Logger;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ProxyCloseHandler extends DirectCloseHandler {

    public ProxyCloseHandler(SocketChannel clientSocket, HostProxyConfig proxyConfig, ChannelSelector channelSelector) {
        super(clientSocket, proxyConfig, channelSelector);
    }

    protected void connectToProxyServer() {
        try {
            connectToProxyServer(proxyConfig.getProxyHost(), proxyConfig.getProxyPort());
            Logger.debug(getClass(), "creates a proxy socket");
        } catch (IOException e) {
            Logger.debug(getClass(), "error", "Can't connect to " + proxyConfig.getProxyHost() + ":" + proxyConfig.getProxyPort() + "\n".concat(e.getMessage()));
        }

    }

}
