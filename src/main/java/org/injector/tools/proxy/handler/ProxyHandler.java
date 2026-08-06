package org.injector.tools.proxy.handler;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.event.EventRunnableHandler;
import org.injector.tools.payload.Payload;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Setter
@Getter
public abstract class ProxyHandler implements EventRunnableHandler {

    protected SocketChannel client = null;
    protected SocketChannel remote = null;
    protected Payload payload;
    protected String requestLine;
    protected boolean skipReadRequestLine = false;
    protected HostProxyConfig proxyConfig;
    private ChannelSelector channelSelector;

    public ProxyHandler() {}

    public ProxyHandler(SocketChannel client, HostProxyConfig proxyConfig, ChannelSelector channelSelector) {
        this();
        this.client = client;
        this.proxyConfig = proxyConfig;
        this.channelSelector = channelSelector;
        this.payload = new Payload(proxyConfig.getPayload());
    }

    public Selector getSelector() {
        return channelSelector.getSelector();
    }

    public ExecutorService getService() {
        return channelSelector.getService();
    }

    public void startHandler() {
        fireStartListener();
        defaultLifeCycleCaller();
    }

    private void defaultLifeCycleCaller() {

        // read client request line and setup payload placeholder
        if (!skipReadRequestLine)
            readClientRequestLine();

        // setup proxy/server mode
        // and setup in/out stream
        connectToProxyServer();

        /* in case of all things goes well, connect server with client direct */
        // addErrorListener(this::closeConnection);
        // addSuccessListener(this::transferDataFromClientToProxy);
        // addSuccessListener(this::transferDataFromProxyToClient);

        readResponseFromProxy();
        writePayloadToRemoteHost();

        // transferDataFromClientToProxy();
        // transferDataFromProxyToClient();

        registerChannelToSelector();
    }

    protected void readResponseFromProxy() {
        getService().execute(() -> {
            handelProxyResponse();
            fireSuccessListener();
        });
    }

    /**
     * start to read response from proxy wrapper server and <b>analysis</b> that
     * response. <br>
     * then write the edited response back to client <br>
     * <br>
     * can ignore write that response at all to client
     */
    abstract void handelProxyResponse();


    protected void setChannelsBlockMode(boolean block) throws IOException {
        client.configureBlocking(block);
        remote.configureBlocking(block);
    }

    protected void registerChannelToSelector() {
        try {
            setChannelsBlockMode(false);
            client.register(getSelector(), SelectionKey.OP_READ, remote);
            remote.register(getSelector(), SelectionKey.OP_READ, client);
        } catch (Exception e) {
            log.error("registerChannelToSelector", e);
        }
    }

    protected void registerTransferDataFromClientToProxy() {
        try {
            client.configureBlocking(false);
            client.register(getSelector(), SelectionKey.OP_READ, remote);
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    protected void registerTransferDataFromProxyToClient() {
        try {
            remote.configureBlocking(false);
            remote.register(getSelector(), SelectionKey.OP_READ, client);
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    protected void connectToProxyServer() {
        try {
            connectToProxyServer(proxyConfig.getProxyHost(), proxyConfig.getProxyPort());
        } catch (IOException e) {
            log.error("Can't connect to {}:{}{}", proxyConfig.getProxyHost(), proxyConfig.getProxyPort(), "\n".concat(e.getMessage()));
        }
    }

    protected void connectToProxyServer(String host, int port) throws IOException {
        var remoteAddress = new InetSocketAddress(host, port);
        remoteConnect(remoteAddress);
    }

    /**
     *
     * @param remoteAddress
     * @throws IOException
     */
    protected void remoteConnect(InetSocketAddress remoteAddress) throws IOException {
        remote = SocketChannel.open();
        remote.connect(remoteAddress);
        remote.finishConnect();
        log.info("connect to host: {}", remoteAddress.toString());
    }

    protected void readClientRequestLine() {
        ByteBuffer buffer = ByteBuffer.allocate(20 * 1024);
        int bytes_read = 0;
        try {
            bytes_read = client.read(buffer);
            buffer.flip();
            requestLine = new String(buffer.array(), 0, bytes_read);

            if (payload == null)
                payload = new Payload(proxyConfig.getPayload());
            payload.setRequest(requestLine);
            log.info("Client Request Line: {}", requestLine);
            log.info("Request Payload Mode ==> {}", payload.getRawPayload());
        } catch (IOException e) {
            log.error("Can't read request line.", e);
        }
    }

    protected void writePayloadToRemoteHost() {
        String raw = payload.getRawPayload();
        ArrayList<Integer> index = getSplitIndex(raw);
        try {

            if (index == null) {
                remote.write(ByteBuffer.wrap(raw.getBytes()));
                log.info("write payload raw to Proxy: {}", raw);
            } else {
                ByteBuffer buffer;
                for (int i = 0; i < index.size(); i += 2) {
                    buffer = ByteBuffer.wrap(raw.substring(index.get(i), index.get(i + 1)).getBytes());
                    buffer.flip();
                    remote.write(buffer);
                    log.info("write payload raw#{}: {}",
                            i / 2,
                            raw.substring(index.get(i), index.get(i + 1))
                    );
                }
            }
        } catch (IOException e) {
            log.error("Error", e);
        }

    }


    public void sleepTime(int timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
            log.info("slept for : {}", timeout);
        } catch (InterruptedException e) {
            log.error("Message: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public void sendNormalRequest() throws IOException {
        log.info( "try to send normal request");
        log.info( requestLine);
        remote.write(ByteBuffer.wrap(requestLine.getBytes()));
    }

    void closeConnection() {
        try {
            remote.close();
            client.close();
            log.info( "remote is {}", (remote.isConnected() ? "alive" : "closed"));
            log.info( "client is {}", (client.isBlocking() ? "alive" : "closed"));
        } catch (IOException e) {
            log.info( "error - close i/o sockets");
            log.error("IOException", e);
        }
    }

    /**
     * @param requestPayload payload request to find split in it
     * @return null if no split found or index of data to write [0,46] [54, 80]
     */
    public ArrayList<Integer> getSplitIndex(String requestPayload) {
        return Payload.getSplitIndexes(requestPayload);
    }

    public void debugSocketsChannel(Exception e) {
        log.info( "remote is " + (remote.isConnected() ? "alive" : "closed"));
        log.info( "client is " + (client.isBlocking() ? "alive" : "closed"));
        log.error("Message", e.getMessage());
    }

    public String readClientRequest(ReadableByteChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(20 * 1024);
        try {
            while (client.read(buffer) != -1) {
                // ignore -- keep read
            }
        } catch (IOException e) {
            log.info( "error", "Can't read request line.\n".concat(e.getMessage()));
        }
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];

        // Copy data from buffer into the array
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
//        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    public ByteBuffer readClientRequestBytes(SocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(20 * 1024);
        try {
            client.read(buffer);
        } catch (IOException e) {
            log.info( "error", "Can't read request line.\n".concat(e.getMessage()));
        }
        buffer.flip();
        return ByteBuffer.wrap(buffer.array(), 0, buffer.limit());
    }


    protected void clearMemory() {
        try {
            client.close();
            remote.close();
        } catch (IOException e) {
            log.error("Error closing client and remote channel", e.getMessage());
        }

        payload = null;
        requestLine = null;

        skipReadRequestLine = false;

        proxyConfig = null;

    }

    @Override
    protected void finalize() throws Throwable {
        clearMemory();
        clearListeners();
        super.finalize();
    }
}
