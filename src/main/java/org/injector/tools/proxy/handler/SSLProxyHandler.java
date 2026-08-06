package org.injector.tools.proxy.handler;

import lombok.extern.slf4j.Slf4j;
import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.ssl.SSLUtils;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class SSLProxyHandler extends ProxyHandler {

    protected static final long POLL_SLEEP_MS = 100; // Balanced latency and CPU usage
    protected static final int SSL_BUFFER_SIZE = 80 * 1024;

    private SSLEngine engine;
    private ByteBuffer myAppData;
    private ByteBuffer myNetData;
    private ByteBuffer peerAppData;
    private ByteBuffer peerNetData;


    public SSLProxyHandler(
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
            log.info("creates a proxy socket");
            var address = InetAddress.getByName(tlsProxyHost);
            log.info("Resolve TLS Proxy Host name: [{}] with IP [{}]", tlsProxyHost, address.getHostAddress());
            this.remoteConnect(new InetSocketAddress(address.getHostAddress(), tlsProxyPort));
        } catch (IOException e) {
            log.error("Can't connect to {}:{}{}", tlsProxyHost, tlsProxyPort, "\n".concat(e.getMessage()));
        }
    }

    @Override
    protected void remoteConnect(InetSocketAddress remoteAddress) throws IOException {
        super.remoteConnect(remoteAddress);
//        var socket = this.remote.socket();
        SSLContext sslContext = SSLUtils.getSSLContext();
        this.engine = sslContext.createSSLEngine(this.proxyConfig.getProxyHost(), this.proxyConfig.getProxyPort());
        if (this.proxyConfig.getSniHostName() != null && !this.proxyConfig.getSniHostName().isBlank()) {
            var serverName = new SNIHostName(this.proxyConfig.getSniHostName());
            var params = this.engine.getSSLParameters();
            params.setServerNames(List.of(serverName));
            this.engine.setSSLParameters(params);
            log.info( "Use SNI Host Name: {}", this.proxyConfig.getSniHostName());
        }
        this.engine.setUseClientMode(true);
        SSLSession session = engine.getSession();

        myAppData = ByteBuffer.allocate(session.getApplicationBufferSize()*4);
        myNetData = ByteBuffer.allocate(session.getPacketBufferSize()*4);
        peerAppData = ByteBuffer.allocate(session.getApplicationBufferSize()*4);
        peerNetData = ByteBuffer.allocate(session.getPacketBufferSize()*4);
    }

    @Override
    void handelProxyResponse() {
        // do nothing
        try {
            this.client.write(ByteBuffer.wrap("HTTP/1.1 200 connected\r\n\r\n".getBytes(StandardCharsets.UTF_8)));
            log.info( "send 200 connected to client");
        } catch (Exception e) {
            log.error("error message caused by {}", e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void writePayloadToRemoteHost() {
        try {
            this.doHandshake(remote, engine, myNetData, peerNetData);
        } catch (Exception e) {
            log.error("Exception", e);
        }
        var raw = payload.getRawPayload();
        myAppData.put(raw.getBytes());

        try {
            while (myAppData.hasRemaining()) {
                // Generate SSL/TLS encoded data (handshake or application data)
                SSLEngineResult res = engine.wrap(myAppData, myNetData);
                // Process status of call
                if (res.getStatus() == SSLEngineResult.Status.OK) {
                    myAppData.compact();
                    // Send SSL/TLS encoded data to peer
                    while(myNetData.hasRemaining()) {
                        int num = remote.write(myNetData);
                        if (num == -1) {
                            // handle closed channel
                        } else if (num == 0) {
                            // no bytes written; try again later
                        }
                    }
                }
                // Handle other status: BUFFER_OVERFLOW, CLOSED...
            }
            log.info("write payload to proxy: {}", raw);
        } catch (IOException e) {
            log.error("IOException",  e);
        }
    }

    private void doHandshake(SocketChannel socketChannel, SSLEngine engine,
                     ByteBuffer myNetData, ByteBuffer peerNetData) throws Exception {
        // Create byte buffers to use for holding application data
        int appBufferSize = engine.getSession().getApplicationBufferSize();
        ByteBuffer myAppData = ByteBuffer.allocate(appBufferSize);
        ByteBuffer peerAppData = ByteBuffer.allocate(appBufferSize);
        // Begin handshake
        engine.beginHandshake();
        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        // Process handshaking message
        while (hs != SSLEngineResult.HandshakeStatus.FINISHED &&
                hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (hs) {
                case NEED_UNWRAP:
                    // Receive handshaking data from peer
                    if (socketChannel.read(peerNetData) < 0) {
                        // Handle closed channel
                    }
                    // Process incoming handshaking data
                    peerNetData.flip();
                    SSLEngineResult res = engine.unwrap(peerNetData, peerAppData);
                    peerNetData.compact();
                    hs = res.getHandshakeStatus();
                    // Check status
                    switch (res.getStatus()) {
                        case OK :
                            // Handle OK status
                            break;
                        // Handle other status: BUFFER_UNDERFLOW, BUFFER_OVERFLOW, CLOSED...
                    }
                    break;
                case NEED_WRAP :
                    // Empty the local network packet buffer.
                    myNetData.clear();
                    // Generate handshaking data
                    res = engine.wrap(myAppData, myNetData);
                    hs = res.getHandshakeStatus();
                    // Check status
                    switch (res.getStatus()) {
                        case OK :
                            myNetData.flip();
                            // Send the handshaking data to peer
                            while (myNetData.hasRemaining()) {
                                if (socketChannel.write(myNetData) < 0) {
                                    // Handle closed channel
                                }
                            }
                            break;
                        // Handle other status: BUFFER_OVERFLOW, BUFFER_UNDERFLOW, CLOSED...
                    }
                    break;
                case NEED_TASK :
                    // Handle blocking tasks
                    Runnable task;
                    while ((task=engine.getDelegatedTask()) != null) {
                        this.getChannelSelector().getService().submit(task);
                    }
                    break;
                // Handle other status: // FINISHED or NOT_HANDSHAKING...
            }
        }
        // Processes after handshaking...
    }

    @Override
    protected void registerChannelToSelector() {
        try {
            setChannelsBlockMode(false);
            client.register(getSelector(), SelectionKey.OP_READ, ReadWriteOperation.create(remote, client));
            remote.register(getSelector(), SelectionKey.OP_READ, ReadWriteOperation.create(client, remote));
        } catch (Exception e) {
            log.error("registerChannelToSelector", e);
        }
    }

}
