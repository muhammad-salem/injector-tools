package org.injector.tools.proxy.handler;

import lombok.extern.slf4j.Slf4j;
import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.proxy.handler.nio.SslChannelHandler;
import org.injector.tools.ssl.SSLUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Slf4j
public class SecureChannelProxyHandler extends TcpProxyHandler {

    private SslChannelHandler sslChannelHandler;

    public SecureChannelProxyHandler(
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


        registerChannelToSelector();
    }

    @Override
    protected void remoteConnect(InetSocketAddress remoteAddress) throws IOException {
        remote = SocketChannel.open();
        remote.connect(remoteAddress);
        remote.finishConnect();
        log.info("connect to host: {}", remoteAddress);

        this.sslChannelHandler = new SslChannelHandler(
                this.remote,
                SSLUtils.getSSLContext(),
                this.proxyConfig.getProxyHost(),
                this.proxyConfig.getProxyPort(),
                this.proxyConfig.getSniHostName()
        );
        log.info("Initiating TLS handshake...");
        if (this.sslChannelHandler.doHandshake()) {
            log.info("Handshake successful! Secure connection established: {}", remoteAddress);
        } else {
            log.info("Handshake error! Secure connection establish failed: {}", remoteAddress);
            throw new RuntimeException("TLS handshake failed");
        }
    }

    @Override
    protected void registerChannelToSelector() {
        try {
            client.configureBlocking(false);
            remote.configureBlocking(false);

            client.register(getSelector(), SelectionKey.OP_READ, ReadWriteOperation.create(client, sslChannelHandler));
            remote.register(getSelector(), SelectionKey.OP_READ, ReadWriteOperation.create(sslChannelHandler, client));

            log.info( "success registerChannelToSelector");
        } catch (Exception e) {
            log.error( "failed registerChannelToSelector");
        }
    }

    @Override
    protected void writePayloadToRemoteHost() {
        var request = payload.getRawPayload();
        try {
            this.sslChannelHandler.write(ByteBuffer.wrap(request.getBytes()));
            log.info("write payload to proxy: {}", request);
        } catch (IOException e) {
            log.error("error writePayloadToRemoteHost", e);
        }
    }

}
