package org.injector.tools.proxy.handler;


import lombok.extern.slf4j.Slf4j;
import org.injector.tools.config.HostProxyConfig;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

@Slf4j
public class Http2Socks5Handler extends TunnelProxyHandler {

    /**
     * create Http2Socks5Handler for <b>clientSocket</b> with <b>proxyConfig </b>
     * with its payload data
     *
     * @param clientSocket
     * @param proxyConfig
     */
    public Http2Socks5Handler(SocketChannel clientSocket, HostProxyConfig proxyConfig, ChannelSelector channelSelector) {
        super(clientSocket, proxyConfig, channelSelector);
        initRequestLine();
    }

    private void initRequestLine() {
        readClientRequestLine();
        skipReadRequestLine = true;

    }

    @Override
    protected void connectToProxyServer() {
        try {

//			Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyConfig.getProxyHost(), proxyConfig.getProxyPort()));
            InetSocketAddress address = new InetSocketAddress(payload.getHost(), payload.getPortInt());


            log.info("Connect to Socks Proxy.... ");

            remoteConnect(address);
//			
//			proxySocket = new Socket(proxy);
//			proxySocket.connect(address);

            log.info("Connected to Socks Proxy", proxyConfig.getProxyHost());
        } catch (Exception e) {
            log.info("error Can't connect to " + proxyConfig, e.getMessage());
        }

    }


}
