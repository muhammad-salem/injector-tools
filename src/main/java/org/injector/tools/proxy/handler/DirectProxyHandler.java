package org.injector.tools.proxy.handler;

import lombok.extern.slf4j.Slf4j;
import org.injector.tools.config.HostProxyConfig;

import java.io.IOException;
import java.nio.channels.SocketChannel;

@Slf4j
public class DirectProxyHandler extends ProxyHandler {

    public DirectProxyHandler(SocketChannel clientSocket, HostProxyConfig proxyConfig, ChannelSelector channelSelector) {
        super(clientSocket, proxyConfig, channelSelector);
    }

    protected void connectToProxyServer() {
        try {
            log.info("creates a proxy socket");
            connectToProxyServer(payload.getHost(), payload.getPortInt());
        } catch (IOException e) {
            log.info("error", "Can't connect to " + payload.getHost() + ":" + payload.getPortInt() + "\n".concat(e.getMessage()));
        }
    }


    @Override
    void handelProxyResponse() {
    }

}
