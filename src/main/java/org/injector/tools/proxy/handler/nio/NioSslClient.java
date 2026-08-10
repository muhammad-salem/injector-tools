package org.injector.tools.proxy.handler.nio;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.injector.tools.ssl.SSLUtils;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

/**
 * An SSL/TLS client that connects to a server using its IP address and port.
 * <p/>
 * After initialization of a {@link NioSslClient} object, {@link NioSslClient#connect()} should be called,
 * in order to establish connection with the server.
 * <p/>
 * When the connection between the client and the object is established, {@link NioSslClient} provides
 * a public write and read method, in order to communicate with its peer.
 *
 * @author <a href="mailto:alex.a.karnezis@gmail.com">Alex Karnezis</a>
 */
@Slf4j
public class NioSslClient extends NioSslPeer implements Closeable {

    /**
     * The remote address of the server this client is configured to connect to.
     */
    private String remoteAddress;

    /**
     * The port of the server this client is configured to connect to.
     */
    private int port;

    /**
     * the fake sni host name
     */
    private String sniHostName;

    /**
     * The engine that will be used to encrypt/decrypt data between this client and the server.
     */
    private SSLEngine engine;

    /**
     * The socket channel that will be used as the transport link between this client and the server.
     */
    @Getter
    private SocketChannel socketChannel;


    /**
     * Initiates the engine to run as a client using peer information, and allocates space for the
     * buffers that will be used by the engine.
     *
     * @param remoteAddress The IP address of the peer.
     * @param port          The peer's port that will be used.
     */
    public NioSslClient(String remoteAddress, int port, String sniHostName) {
        this.remoteAddress = remoteAddress;
        this.port = port;
        this.sniHostName = sniHostName;

        SSLContext context = SSLUtils.getSSLContext();
        this.engine = context.createSSLEngine(remoteAddress, port);
        this.engine.setUseClientMode(true);

        if (sniHostName != null && !sniHostName.isBlank()) {
            SSLParameters sslParams = this.engine.getSSLParameters();
            var serverName = new SNIHostName(sniHostName);
            sslParams.setServerNames(List.of(serverName));
            log.info("Use SNI Host Name: {}", sniHostName);
            this.engine.setSSLParameters(sslParams);
        }

        SSLSession session = engine.getSession();
        myAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        myNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        peerAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());
    }

    /**
     * Opens a socket channel to communicate with the configured server and tries to complete the handshake protocol.
     *
     * @return True if client established a connection with the server, false otherwise.
     * @throws Exception
     */
    public boolean connect() throws Exception {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(remoteAddress, port));
        while (!socketChannel.finishConnect()) {
            // can do something here...
        }
        engine.beginHandshake();
        return doHandshake(socketChannel, engine);
    }

    /**
     * Public method to send a message to the server.
     *
     * @param message - message to be sent to the server.
     * @throws IOException if an I/O error occurs to the socket channel.
     */
    public void write(String message) throws IOException {
        write(socketChannel, engine, message);
    }

    public void write(ByteBuffer buffer) throws IOException {
        write(socketChannel, engine, buffer);
    }

    /**
     * Implements the write method that sends a message to the server the client is connected to,
     * but should not be called by the user, since socket channel and engine are inner class' variables.
     * {@link NioSslClient#write(String)} should be called instead.
     *
     * @param message - message to be sent to the server.
     * @param engine  - the engine used for encryption/decryption of the data exchanged between the two peers.
     * @throws IOException if an I/O error occurs to the socket channel.
     */
    @Override

    protected void write(SocketChannel socketChannel, SSLEngine engine, String message) throws IOException {
        this.write(socketChannel, engine, ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
    }

    protected void write(SocketChannel socketChannel, SSLEngine engine, ByteBuffer buffer) throws IOException {

        log.debug("About to write to the server...");

        myAppData.clear();
        myAppData.put(buffer);
        myAppData.flip();
        while (myAppData.hasRemaining()) {
            // The loop has a meaning for (outgoing) messages larger than 16KB.
            // Every wrap call will remove 16KB from the original message and send it to the remote peer.
            myNetData.clear();
            SSLEngineResult result = engine.wrap(myAppData, myNetData);
            switch (result.getStatus()) {
                case OK:
                    myNetData.flip();
                    while (myNetData.hasRemaining()) {
                        socketChannel.write(myNetData);
                    }
                    log.debug("Message sent to the server");
                    break;
                case BUFFER_OVERFLOW:
                    myNetData = enlargePacketBuffer(engine, myNetData);
                    break;
                case BUFFER_UNDERFLOW:
                    throw new SSLException("Buffer underflow occurred after a wrap. I don't think we should ever get here.");
                case CLOSED:
                    closeConnection(socketChannel, engine);
                    return;
                default:
                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
            }
        }

    }

    /**
     * Public method to try to read from the server.
     *
     * @throws Exception
     */
    public void read(Consumer<ByteBuffer> bufferConsumer) throws Exception {
        this.read(engine, socketChannel, bufferConsumer);
    }

    @Override
    protected void read(SSLEngine engine, SocketChannel readFromChannel, Consumer<ByteBuffer> bufferConsumer) throws Exception {
        log.debug("About to read from the server...");

        peerNetData.clear();
        int waitToReadMillis = 50;
        boolean exitReadLoop = false;
        while (!exitReadLoop) {
            peerNetData.clear();
            var pos = peerNetData.position();
            int bytesRead = socketChannel.read(peerNetData);
            if (bytesRead > 0) {
                peerNetData.flip();
                pos = peerNetData.position();
                while (peerNetData.hasRemaining()) {
                    peerAppData.clear();
                    SSLEngineResult result = engine.unwrap(peerNetData, peerAppData);
                    switch (result.getStatus()) {
                        case OK:
                            peerAppData.flip();
                            bufferConsumer.accept(peerAppData);
//                            log.info("1.Server response: {}", new String(peerAppData.array(), 0, peerAppData.limit()));
                            exitReadLoop = true;
                            break;
                        case BUFFER_OVERFLOW:
//                            peerAppData = handleBufferUnderflow(engine, peerAppData);
                            peerAppData = enlargeApplicationBuffer(engine, peerAppData);
                            break;
                        case BUFFER_UNDERFLOW:
                            if (peerNetData.capacity() - peerNetData.position() > 0) {
                                peerNetData.compact();
                                Thread.sleep(waitToReadMillis);
                                bytesRead = socketChannel.read(peerNetData);
                                peerNetData.flip();
                            } else {
                                peerNetData = handleBufferUnderflow(engine, peerNetData);
                            }
                            break;
                        case CLOSED:
                            closeConnection(socketChannel, engine);
                            return;
                    }
                }
            } else if (bytesRead < 0) {
                handleEndOfStream(socketChannel, engine);
                return;
            }
            Thread.sleep(waitToReadMillis);
        }
    }

    /**
     * Public method to try to read from the server.
     *
     * @throws Exception
     */
    public void read() throws Exception {
        read(socketChannel, engine);
    }

    /**
     * Will wait for response from the remote peer, until it actually gets something.
     * Uses {@link SocketChannel#read(ByteBuffer)}, which is non-blocking, and if
     * it gets nothing from the peer, waits for {@code waitToReadMillis} and tries again.
     * <p/>
     * Just like {@link NioSslClient#read(SocketChannel, SSLEngine)} it uses inner class' socket channel
     * and engine and should not be used by the client. {@link NioSslClient#read()} should be called instead.
     *
     * @param engine - the engine used for encryption/decryption of the data exchanged between the two peers.
     * @throws Exception
     */
    @Override
    protected void read(SocketChannel socketChannel, SSLEngine engine) throws Exception {
        log.debug("About to read from the server...");

        peerNetData.clear();
        int waitToReadMillis = 50;
        boolean exitReadLoop = false;
        while (!exitReadLoop) {
            int bytesRead = socketChannel.read(peerNetData);
            if (bytesRead > 0) {
                peerNetData.flip();
                while (peerNetData.hasRemaining()) {
                    if (!exitReadLoop) {
                        peerAppData.clear();
                    }
                    SSLEngineResult result = engine.unwrap(peerNetData, peerAppData);
                    switch (result.getStatus()) {
                        case OK:
//                            peerAppData.flip();
                            log.info("2.Server response: {}", new String(peerAppData.array(), 0 , peerAppData.position()));
                            exitReadLoop = true;
                            break;
                        case BUFFER_OVERFLOW:
                            peerAppData = enlargeApplicationBuffer(engine, peerAppData);
                            break;
                        case BUFFER_UNDERFLOW:
                            if (peerNetData.capacity() - peerNetData.position() > 0) {
                                peerNetData.compact();
                                Thread.sleep(waitToReadMillis);
                                bytesRead = socketChannel.read(peerNetData);
                                peerNetData.flip();
                            } else {
                                peerNetData = handleBufferUnderflow(engine, peerNetData);
                            }
                            break;
                        case CLOSED:
                            closeConnection(socketChannel, engine);
                            return;
                    }
                }
            } else if (bytesRead < 0) {
                handleEndOfStream(socketChannel, engine);
                return;
            }
            Thread.sleep(waitToReadMillis);
        }
        peerAppData.flip();
    }

    /**
     * Should be called when the client wants to explicitly close the connection to the server.
     *
     * @throws IOException if an I/O error occurs to the socket channel.
     */
    public void close() throws IOException {
        log.debug("About to close connection with the server...");
        closeConnection(socketChannel, engine);
        executor.shutdown();
        log.debug("Goodbye!");
    }

}