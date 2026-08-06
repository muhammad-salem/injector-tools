package org.injector.tools.proxy.handler;

import lombok.extern.slf4j.Slf4j;
import org.injector.tools.config.HostProxyConfig;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;


@Slf4j
public class SecureProxyHandler extends TcpProxyHandler {

    public SecureProxyHandler(
            SocketChannel client,
            HostProxyConfig proxyConfig,
            ChannelSelector channelSelector) {
        super(client, proxyConfig, channelSelector);
    }

    @Override
    protected void registerChannelToSelector() {
        try {
            client.configureBlocking(true);
            var localSocket = client.socket();
            var upstreamSocket = this.sslSocket;

            InputStream clientIn = localSocket.getInputStream();
            OutputStream clientOut = localSocket.getOutputStream();

            InputStream upstreamIn = upstreamSocket.getInputStream();
            OutputStream upstreamOut = upstreamSocket.getOutputStream();

            // 5. Spawn two virtual threads to bridge bidirectional raw data stream
            Thread toUpstream = Thread.startVirtualThread(() -> bridgeData(clientIn, upstreamOut));
            Thread toClient = Thread.startVirtualThread(() -> bridgeData(upstreamIn, clientOut));

            // Wait for both payload-transfer threads to finish
            toUpstream.join();
            toClient.join();
            log.info( "registerChannelToSelector call done");
        } catch (Exception e) {
            log.error( "failed registerChannelToSelector: ", e);
        }
    }

    private void bridgeData(InputStream input, OutputStream output) {
        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
                output.flush();
            }
        } catch (Exception e) {
            // Pipe broken or socket closed expectedly during disconnect
            log.trace("Stream bridge closure notice: {}", e.getMessage());
        }
    }

}
