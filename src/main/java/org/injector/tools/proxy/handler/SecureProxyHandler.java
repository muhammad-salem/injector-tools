package org.injector.tools.proxy.handler;

import lombok.extern.slf4j.Slf4j;
import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.proxy.handler.nio.NioSslClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Slf4j
public class SecureProxyHandler extends TcpProxyHandler {

    private NioSslClient nioSslClient;

    public SecureProxyHandler(
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

    @Override
    protected void remoteConnect(InetSocketAddress remoteAddress) throws IOException {
        this.nioSslClient = new NioSslClient(
                remoteAddress.getHostName(),
                remoteAddress.getPort(),
                this.proxyConfig.getSniHostName()
        );
        log.info("Initiating TLS handshake...");
        try {
            if (this.nioSslClient.connect()) {
                log.info("Handshake successful! Secure connection established: {}", remoteAddress);
            } else {
                log.info("Handshake error! Secure connection establish failed: {}", remoteAddress);
                throw new RuntimeException("TLS handshake failed");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.remote = this.nioSslClient.getSocketChannel();
    }

    @Override
    protected void writePayloadToRemoteHost() {
        var request = payload.getRawPayload();
        try {
            this.nioSslClient.write(request);
            log.info("write payload to proxy: {}", request);

//            ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
//            this.nioSslClient.read(buffer::put);
//            buffer.flip();
//            String response = new String(buffer.array(), 0 , buffer.limit());
//            log.info("read proxy response: {}", response);
//
//            if (response.contains(" 200 ")) {
//                String res = "HTTP/1.1 200 Connection Established\r\n\r\n";
//                this.client.write(ByteBuffer.wrap(res.getBytes(StandardCharsets.UTF_8)));
//                log.info("write response to client: {}", res);
//            }
        } catch (Exception e) {
            log.error("error writePayloadToRemoteHost", e);
        }
    }

    @Override
    protected void registerChannelToSelector() {
        try {
            client.configureBlocking(false);
            remote.configureBlocking(false);

            client.register(getWorkerSelector(), SelectionKey.OP_READ, ReadWriteOperation.create(client, nioSslClient));
            remote.register(getWorkerSelector(), SelectionKey.OP_READ, ReadWriteOperation.create(nioSslClient, client));

            log.info("success registerChannelToSelector");
        } catch (Exception e) {
            log.error("failed registerChannelToSelector");
        }
    }

}
