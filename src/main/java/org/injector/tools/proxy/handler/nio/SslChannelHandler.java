package org.injector.tools.proxy.handler.nio;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A complete, self-contained handler for managing TLS/SSL handshakes,
 * data wrapping (encryption), and data unwrapping (decryption) over a SocketChannel.
 */
@Slf4j
public class SslChannelHandler implements Closeable {

    private final SocketChannel socketChannel;
    private final SSLEngine sslEngine;
    private final ExecutorService taskExecutor;

    private ByteBuffer myAppData;
    private ByteBuffer myNetData;
    private ByteBuffer peerAppData;
    private ByteBuffer peerNetData;

    /**
     * Initializes the SSL Engine handler with host and port for verification and session caching.
     *
     * @param socketChannel The underlying network channel.
     * @param sslContext    The pre-configured SSLContext containing keys/trust anchors.
     * @param host          The remote peer hostname (used for SNI and certificate verification).
     * @param port          The remote peer port (used for session caching).
     */
    public SslChannelHandler(SocketChannel socketChannel, SSLContext sslContext, String host, int port, String sniHostName) {
        this.socketChannel = socketChannel;

        // Passing host and port enables SNI extension and SSL session resumption caching
        this.sslEngine = sslContext.createSSLEngine(host, port);
        this.sslEngine.setUseClientMode(true);

        // Enforce strict endpoint identification (hostname validation) for clients
        SSLParameters sslParams = sslEngine.getSSLParameters();
        sslParams.setEndpointIdentificationAlgorithm("HTTPS");
        if (sniHostName != null && !sniHostName.isBlank()) {
            var serverName = new SNIHostName(sniHostName);
            sslParams.setServerNames(List.of(serverName));
            log.info( "Use SNI Host Name: {}", sniHostName);
        }
        sslEngine.setSSLParameters(sslParams);

        // Heavy crypto tasks (like certificate validation) run here to avoid blocking I/O
        this.taskExecutor = Executors.newSingleThreadExecutor();

        SSLSession session = sslEngine.getSession();
        this.myAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        this.peerAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        this.myNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        this.peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());
    }

    /**
     * Executes the TLS Handshake state machine synchronously.
     * This loop blocks until the handshake is either FINISHED or NOT_HANDSHAKING.
     *
     * @return true if handshake succeeded, false if connection closed.
     * @throws IOException If a network or TLS protocol error occurs.
     */
    public boolean doHandshake() throws IOException {
        sslEngine.beginHandshake();
        SSLEngineResult.HandshakeStatus status = sslEngine.getHandshakeStatus();

        while (status != SSLEngineResult.HandshakeStatus.FINISHED &&
                status != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {

            switch (status) {
                case NEED_WRAP:
                    myNetData.clear();
                    SSLEngineResult wrapResult = sslEngine.wrap(myAppData, myNetData);
                    status = wrapResult.getHandshakeStatus();

                    if (wrapResult.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                        myNetData = handleBufferOverflow(myNetData, sslEngine.getSession().getPacketBufferSize());
                        break;
                    }

                    myNetData.flip();
                    while (myNetData.hasRemaining()) {
                        socketChannel.write(myNetData);
                    }
                    break;

                case NEED_UNWRAP:
                    if (socketChannel.read(peerNetData) < 0) {
                        if (sslEngine.isInboundDone() && sslEngine.isOutboundDone()) {
                            return false;
                        }
                        try {
                            sslEngine.closeInbound();
                        } catch (SSLException e) {
                            // Suppress inbound closure anomalies during dropouts
                        }
                        sslEngine.closeOutbound();
                        return false;
                    }

                    peerNetData.flip();
                    SSLEngineResult unwrapResult = sslEngine.unwrap(peerNetData, peerAppData);
                    peerNetData.compact();
                    status = unwrapResult.getHandshakeStatus();

                    if (unwrapResult.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                        // Incomplete TLS record received; jump back to read loop for more data
                        break;
                    } else if (unwrapResult.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                        peerAppData = handleBufferOverflow(peerAppData, sslEngine.getSession().getApplicationBufferSize());
                        break;
                    }
                    break;

                case NEED_TASK:
                    Runnable task;
                    while ((task = sslEngine.getDelegatedTask()) != null) {
                        taskExecutor.execute(task);
                    }
                    status = sslEngine.getHandshakeStatus();
                    break;

                default:
                    throw new IllegalStateException("Unexpected handshake status state: " + status);
            }
        }
        return true;
    }

    /**
     * Encrypts and transmits application cleartext data.
     *
     * @param plainText Buffer containing cleartext data ready to be read.
     * @throws IOException If encryption or channel writing fails.
     */
    public void write(ByteBuffer plainText) throws IOException {
        myNetData.clear();

        while (plainText.hasRemaining()) {
            SSLEngineResult result = sslEngine.wrap(plainText, myNetData);

            if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                myNetData = handleBufferOverflow(myNetData, sslEngine.getSession().getPacketBufferSize());
                continue;
            } else if (result.getStatus() != SSLEngineResult.Status.OK) {
                throw new SSLException("Encryption engine failed: " + result.getStatus());
            }

            myNetData.flip();
            while (myNetData.hasRemaining()) {
                socketChannel.write(myNetData);
            }
            myNetData.clear();
        }
    }

    /**
     * Reads data from the wire, decrypts it, and returns the cleartext buffer.
     *
     * @return A ready-to-be-read ByteBuffer containing cleartext, or null if connection ends or data is incomplete.
     * @throws IOException If channel reading or decryption fails.
     */
    public ByteBuffer read() throws IOException {
        int bytesRead = socketChannel.read(peerNetData);
        if (bytesRead == -1) {
            return null; // Remote peer triggered EOF
        }

        peerNetData.flip();
        peerAppData.clear();

        while (peerNetData.hasRemaining()) {
            SSLEngineResult result = sslEngine.unwrap(peerNetData, peerAppData);

            if (result.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                peerNetData.compact();
                return null;
            } else if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                peerAppData = handleBufferOverflow(peerAppData, sslEngine.getSession().getApplicationBufferSize());
                continue;
            } else if (result.getStatus() != SSLEngineResult.Status.OK) {
                throw new SSLException("Decryption engine failed: " + result.getStatus());
            }
        }

        peerNetData.compact();
        peerAppData.flip();
        return peerAppData;
    }

    /**
     * Dynamically resizes a byte buffer if the SSLEngine requests more space.
     */
    private ByteBuffer handleBufferOverflow(ByteBuffer buffer, int dynamicProposedSize) {
        int targetCapacity = Math.max(buffer.capacity() * 2, dynamicProposedSize);
        ByteBuffer newBuffer = ByteBuffer.allocate(targetCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    /**
     * Gracefully tears down the TLS engine and signals the peer before closure.
     */
    public void close() throws IOException {
        sslEngine.closeOutbound();
        try {
            doHandshake(); // Flush TLS closure alert down the channel
        } catch (IOException e) {
            // Ignore pipeline flushes during active socket termination
        }
        taskExecutor.shutdown();
        socketChannel.close();
    }

}
