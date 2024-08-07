package org.injector.tools.proxy.handler;

import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.log.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * stay in blocking mode
 *
 * @author salem
 */
public class DirectCloseHandler extends DirectProxyHandler {

    private int readDataCounter = 1;

    public DirectCloseHandler(SocketChannel clientSocket, HostProxyConfig proxyConfig,
                              ChannelSelector channelSelector) {
        super(clientSocket, proxyConfig, channelSelector);
        readClientRequestLine();
        skipReadRequestLine = true;
    }

    @Override
    public void startHandler() {
        fireStartListener();
        connectToProxyServer();
        writePayloadToRemoteHost();

        registerTransferDataFromClientToProxy();
        Future<?> future = getService().submit(this::registerTransferDataFromProxyToClient);
        checkStopThreadPool(future);
    }


    @Override
    protected void registerTransferDataFromProxyToClient() {
        Logger.debug(getClass(), "Start transfer Data From Proxy To Client");

        ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
        try {
            //setChannelsBlockMode(false);
            while (true) {

                if (remote.read(buffer) > 0) {
                    readDataCounter++;
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        client.write(buffer);
                    }
                    buffer.clear();
                } else {
                    TimeUnit.MILLISECONDS.sleep(200);
                }
            }

        } catch (IOException e) {
            Logger.debug(e.getClass(), "read from proxy", e.getMessage());
            debugSocketsChannel(e);
            fireErrorListener();
        } catch (InterruptedException e) {
            Logger.debug(getClass(), e.getClass().getSimpleName(), e.getMessage());
        }
    }

    public void checkStopThreadPool(Future<?> future) {

        try {
            TimeUnit.MILLISECONDS.sleep(300);
            int prefValue = 0;
            while (readDataCounter > prefValue || readDataCounter == 1) {
                prefValue = readDataCounter;
                TimeUnit.MILLISECONDS.sleep(500);
            }


            future.cancel(true);

            closeConnection();
        } catch (InterruptedException e) {
            Logger.debug(getClass(), e.getClass().getSimpleName(), e.getMessage());
        }

    }

}
