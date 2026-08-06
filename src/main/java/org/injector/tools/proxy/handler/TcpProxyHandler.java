package org.injector.tools.proxy.handler;

import lombok.extern.slf4j.Slf4j;
import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.ssl.SSLUtils;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public abstract class TcpProxyHandler extends ProxyHandler {

    protected static final long POLL_SLEEP_MS = 100; // Balanced latency and CPU usage
    protected static final int SSL_BUFFER_SIZE = 80 * 1024;

    protected SSLSocket sslSocket;

    public TcpProxyHandler(
            SocketChannel client,
            HostProxyConfig proxyConfig,
            ChannelSelector channelSelector) {
        super(client, proxyConfig, channelSelector);
    }

    @Override
    public void startHandler() {

        // read client request line and setup payload placeholder
        readClientRequestLine();

        var skipProxyAuthorization = proxyConfig.getProxyUser() == null
                || proxyConfig.getProxyUser().isBlank()
                || proxyConfig.getProxyPassword() == null
                || proxyConfig.getProxyPassword().isBlank();
        if (!skipProxyAuthorization) {
            this.payload.setProxyAuthorization(proxyConfig.getProxyUser(), proxyConfig.getProxyPassword());
        }

        // setup proxy/server mode
        // and setup in/out stream
        connectToProxyServer();

        writePayloadToRemoteHost();

        registerChannelToSelector();
    }

    // direct connect to
    @Override
    protected void connectToProxyServer() {
        var tlsProxyHost = this.proxyConfig.getProxyHost();
        var tlsProxyPort = this.proxyConfig.getProxyPort();
        try {
            log.info( "creates a proxy socket");
            var address = InetAddress.getByName(tlsProxyHost);
            log.info("Resolve TLS Proxy Host name: [{}] with IP [{}]/{}", tlsProxyHost, address.getHostAddress(), tlsProxyPort);
            this.remoteConnect(new InetSocketAddress(address.getHostAddress(), tlsProxyPort));
        } catch (IOException e) {
            log.info("Can't connect to {}:{}", tlsProxyHost, tlsProxyPort, e);
        }
    }

    @Override
    protected void remoteConnect(InetSocketAddress remoteAddress) throws IOException {
        super.remoteConnect(remoteAddress);
        var socket = this.remote.socket();
        var factory = SSLUtils.getSSLSocketFactory();
//        this.sslSocket = (SSLSocket) factory.createSocket(socket, payload.getHost(), payload.getPortInt(), false);
        this.sslSocket = (SSLSocket) factory.createSocket(socket, this.proxyConfig.getSniHostName(), payload.getPortInt(), false);

        if (this.proxyConfig.getSniHostName() != null && !this.proxyConfig.getSniHostName().isBlank()) {
            var serverName = new SNIHostName(this.proxyConfig.getSniHostName());
            var params = this.sslSocket.getSSLParameters();
            params.setServerNames(List.of(serverName));
            this.sslSocket.setSSLParameters(params);
            log.info("Use SNI Host Name: {}", this.proxyConfig.getSniHostName());
        }
    }

    @Override
    void handelProxyResponse() {
        // do nothing
        try {
            this.client.write(ByteBuffer.wrap("HTTP/1.1 200 connected\r\n\r\n".getBytes(StandardCharsets.UTF_8)));
            log.info( "send 200 connected to client");
        } catch (Exception e) {
            log.error( "error message caused by " + e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void writePayloadToRemoteHost() {
        try {
//            this.remote.finishConnect();
            this.sslSocket.startHandshake();
        } catch (IOException e) {
            log.info("Error in Handshake", e);
        }

        var raw = payload.getRawPayload();
        try {
            this.sslSocket.getOutputStream().write(ByteBuffer.wrap(raw.getBytes()).array());
            this.sslSocket.getOutputStream().flush();
            log.info("write payload to proxy: {}", raw);
        } catch (IOException e) {
            log.error("error writePayloadToRemoteHost", e);
        }
    }

}
