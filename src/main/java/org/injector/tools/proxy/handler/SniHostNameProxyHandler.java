package org.injector.tools.proxy.handler;

import lombok.extern.slf4j.Slf4j;
import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.ssl.SSLUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class SniHostNameProxyHandler extends ProxyHandler {

    protected SSLSocket sslSocket;

    public SniHostNameProxyHandler(
            SocketChannel clientSocket,
            HostProxyConfig proxyConfig,
            ChannelSelector channelSelector) {
        super(clientSocket, proxyConfig, channelSelector);
    }

    @Override
    public void startHandler() {

        // read client request line and setup payload placeholder
        readClientRequestLine();

        // setup proxy/server mode
        // and setup in/out stream
        connectToProxyServer();

//        writePayloadToRemoteHost();

        registerChannelToSelector();
        handelProxyResponse();
    }

    // direct connect to
    @Override
    protected void connectToProxyServer() {
        try {
            log.info("creates a proxy socket");
            var address = InetAddress.getByName(payload.getHost());
            log.info("Resolve Host name: [{}] with IP [{}]", payload.getHost(), address.getHostAddress());
            this.remoteConnect(new InetSocketAddress(address.getHostAddress(), payload.getPortInt()));
        } catch (IOException e) {
            log.error("Can't connect to {}:{}{}", payload.getHost(), payload.getPortInt(), "\n".concat(e.getMessage()));
        }
    }

    @Override
    protected void remoteConnect(InetSocketAddress remoteAddress) throws IOException {
        super.remoteConnect(remoteAddress);
        var socket = this.remote.socket();
//        var factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        var factory = SSLUtils.getSSLSocketFactory();

        this.sslSocket = (SSLSocket) factory.createSocket(socket, payload.getHost(), payload.getPortInt(), false);
        var serverName = new SNIHostName(this.proxyConfig.getSniHostName());
        var params = this.sslSocket.getSSLParameters();
        params.setServerNames(List.of(serverName));
        this.sslSocket.setSSLParameters(params);
        log.info("Use SNI Host Name: {}", (Object) this.proxyConfig.getSniHostName());
    }

    @Override
    void handelProxyResponse() {
        // do nothing
        try {
            this.client.write(ByteBuffer.wrap("HTTP/1.0 200 connected\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1)));
//            this.remote.write(ByteBuffer.wrap(payload.getRawPayload().getBytes(StandardCharsets.UTF_8)));
//            this.sslSocket.getOutputStream().write("SSH".getBytes(StandardCharsets.UTF_8));
//            this.sslSocket.getOutputStream().flush();
        } catch (Exception e) {
            log.info( e.getClass().getSimpleName() + " message", e.getMessage());
        }
    }

    @Override
    protected void writePayloadToRemoteHost() {
//        try {
//            this.remote.finishConnect();
////            sslSocket.startHandshake();
//        } catch (IOException e) {
//            log.info( e.getClass().getSimpleName() + " message", e.getMessage());
//        }

//        var raw = payload.getRawPayload();
//        try {
//            this.sslSocket.getOutputStream().write(ByteBuffer.wrap(raw.getBytes()).array());
//            log.info( "write payload to host to host", payload.getRawPayload());
//        } catch (IOException e) {
//            log.info( e.getClass().getSimpleName() + " message", e.getMessage());
//        }
    }

}
