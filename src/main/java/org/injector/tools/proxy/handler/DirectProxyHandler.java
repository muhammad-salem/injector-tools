package org.injector.tools.proxy.handler;

import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.log.Logger;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class DirectProxyHandler extends ProxyHandler {

    public DirectProxyHandler(SocketChannel clientSocket, HostProxyConfig proxyConfig, ChannelSelector channelSelector) {
        super(clientSocket, proxyConfig, channelSelector);
    }

    protected void connectToProxyServer() {
        try {
            Logger.debug(getClass(), "creates a proxy socket");
            connectToProxyServer(payload.getHost(), payload.getPortInt());
        } catch (IOException e) {
            Logger.debug(getClass(), "error", "Can't connect to " + payload.getHost() + ":" + payload.getPortInt() + "\n".concat(e.getMessage()));
        }

    }


    @Override
    void handelProxyResponse() {
    }

}
