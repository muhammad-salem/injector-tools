package org.injector.tools.proxy.handler;


import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.log.Logger;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

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


            Logger.debug(getClass(), "Connect to Socks Proxy.... ");

            remoteConnect(address);
//			
//			proxySocket = new Socket(proxy);
//			proxySocket.connect(address);

            Logger.debug(getClass(), "Connected to Socks Proxy", proxyConfig.getProxyHost());
        } catch (Exception e) {
            Logger.debug(getClass(), "error Can't connect to " + proxyConfig, e.getMessage());
            e.fillInStackTrace();
        }

    }


}
