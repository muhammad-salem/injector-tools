package org.injector.tools.proxy.handler;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectorProxyTunnel {

    private static final int PORT = 8989;
    private static final String PROXY_HOST = "77.111.247.79";
    private static final int PROXY_PORT = 443;
    private static final String PROXY_USER = "00FA2EB3D708FDDB74CC6310923182A63097416C";
    private static final String PROXY_PASS = "eyJhbGciOiJFQ0RILUVTK0EyNTZLVyIsImN0eSI6IkpXVCIsImVuYyI6IkEyNTZHQ00iLCJlcGsiOnsia3R5IjoiRUMiLCJjcnYiOiJQLTI1NiIsIngiOiJnSzJvdW1XcGpQVU53YVdWcmZxal91SmNmMUV2U19JWHJBR21rTnVLcmtVIiwieSI6InB5XzMtUEhDRTRwTHNQMDFOelMwSlhvMUNOOEM5LXphbVZzZi1lYmd5TE0ifX0.ZPU-CyLa_wfaMugNZtYFLcUGbcSvD4HorptdQeRYTuPFvjjLZlIr6g.3Q1kk3MQ7JUpyvaH.p1WunKdjkOjxkuG2AZKnQd9vDILQx2B0DMxDdyvmhrLdrd1dS1MsvVaDH6Q5H0GXjDEjvTbds0r1QpUNmPvKbOs8-x_IcwzLflnhdG7bOHpVb4mIUcWb_pvGlRmrqGcTX19uSQhcBgWqkcnHHtqTTQLnwuSh9Rukoh5OQ3fmsngz27Q6vqUHg_OjPokRnyLyNp929L2MU9LbfMy9wma98dlHQi36TFCqqlU1RADULmeSriByUD9HLp6Bz0DhMwqVMFEADD9gIXT0r0Y8_F1uiFBUrOyFjZTp5XM2WdXEjVG_e5WldegZc7GWY4-WzkJDYZ6bwbft3-K6QDh07wbe6164pkhWMfbqk-sxqQQNeJ3ogT8dbbALAq3T2qJdPOl8DFiVwKGXjxg3UjDmKSxbBfJ7paF3FPBjU8H7Xm7mtAMcnAU1mGYEWA_9Qb4ifQVJnftjlmo4z6AmAaQ.HTxnoWgdb3Tb-fUxyibheg";
    
    // Thread pool to handle the blocking TLS tunneling work
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverChannel = ServerSocketChannel.open()) {

            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Selector-driven HTTP Proxy running on port " + PORT + "...");

            while (true) {
                selector.select(); // Blocks until a channel is ready
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        handleAccept(serverChannel, selector);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleAccept(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        // Allocate a buffer to store this specific client's HTTP CONNECT request headers
        clientChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(4096));
        System.out.println("Accepted temporary connection from: " + clientChannel.getRemoteAddress());
    }

    private static void handleRead(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        try {
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                closeQuietly(clientChannel);
                return;
            }

            buffer.flip();
            String requestData = StandardCharsets.UTF_8.decode(buffer).toString();
            buffer.compact(); // Keep any overflow data

            // Check if we have received the complete HTTP header delimiter (\r\n\r\n)
            if (requestData.contains("\r\n\r\n")) {
                // Cancel the selector registration so the Selector thread stops tracking it
                key.cancel();
                
                // Switch channel back to blocking mode so it can work with SSLSocket
                clientChannel.configureBlocking(true);

                // Handoff the actual TLS tunneling to the thread pool
                threadPool.submit(() -> processTunnel(clientChannel, requestData));
            }
        } catch (Exception e) {
            System.err.println("Error reading client request: " + e.getMessage());
            closeQuietly(clientChannel);
        }
    }

    private static void processTunnel(SocketChannel clientChannel, String rawHeaders) {
        SSLSocket proxySocket = null;
//        SSLSocket targetSocket = null;
        try {
            // Extract the first line (e.g., "CONNECT ://google.com HTTP/1.1")
            String firstLine = rawHeaders.split("\r\n")[0];
            if (!firstLine.startsWith("CONNECT")) {
                sendHttpError(clientChannel, 400, "Bad Request");
                return;
            }

            String[] parts = firstLine.split(" ");
            String[] hostPort = parts[1].split(":");
            String targetHost = hostPort[0];
            int targetPort = Integer.parseInt(hostPort[1]);

            System.out.println("Selector dispatched tunnel to target: " + targetHost + ":" + targetPort);

            // Connect and handshake with upstream proxy
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            proxySocket = (SSLSocket) sslSocketFactory.createSocket(PROXY_HOST, PROXY_PORT);
            proxySocket.startHandshake();

            // Upstream proxy authentication
            String auth = PROXY_USER + ":" + PROXY_PASS;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            OutputStream proxyOut = proxySocket.getOutputStream();
            PrintWriter proxyWriter = new PrintWriter(new OutputStreamWriter(proxyOut, StandardCharsets.UTF_8));

            proxyWriter.print("CONNECT " + targetHost + ":" + targetPort + " HTTP/1.1\r\n");
            proxyWriter.print("Host: " + targetHost + ":" + targetPort + "\r\n");
            proxyWriter.print("Proxy-Authorization: Basic " + encodedAuth + "\r\n");

            proxyWriter.print("SE-Client-Version: Stable 114.0.5282.21\r\n");
            proxyWriter.print("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 OPR/114.0.0.0\r\n");
            proxyWriter.print("SE-Operating-System: Windows\r\n");
            proxyWriter.print("Proxy-Connection: keep-alive\r\n");
            proxyWriter.print("Connection: keep-alive\r\n");

            proxyWriter.print("\r\n");
            proxyWriter.flush();

            // Safely read response header without over-consuming
            InputStream proxyIn = proxySocket.getInputStream();
            String statusLine = readLine(proxyIn);
            if (statusLine == null || !statusLine.contains("200")) {
                sendHttpError(clientChannel, 502, "Bad Gateway");
                return;
            }

            // Consume remaining upstream headers
            while (true) {
                String header = readLine(proxyIn);
                if (header == null || header.isEmpty()) break;
            }

            // Wrap target connection into a second TLS layer
//            targetSocket = (SSLSocket) sslSocketFactory.createSocket(proxySocket, targetHost, targetPort, true);
//            targetSocket.startHandshake();

            // Tell original client they are connected
            sendHttpSuccess(clientChannel);

            // Pipe data back and forth
            InputStream clientIn = clientChannel.socket().getInputStream();
            OutputStream clientOut = clientChannel.socket().getOutputStream();
            InputStream targetIn = proxySocket.getInputStream();
            OutputStream targetOut = proxySocket.getOutputStream();

            Thread t1 = new Thread(() -> pipeData(clientIn, targetOut));
            Thread t2 = new Thread(() -> pipeData(targetIn, clientOut));
            t1.start();
            t2.start();
            t1.join();
            t2.join();

        } catch (Exception e) {
            System.err.println("Tunnel error: " + e.getMessage());
        } finally {
            closeQuietly(clientChannel);
            closeQuietly(proxySocket);
//            closeQuietly(targetSocket);
        }
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\r') continue;
            if (c == '\n') break;
            bos.write(c);
        }
        if (bos.size() == 0 && c == -1) return null;
        return bos.toString(StandardCharsets.UTF_8.name());
    }

    private static void pipeData(InputStream src, OutputStream dest) {
        byte[] buffer = new byte[8192];
        int bytesRead;
        try {
            while ((bytesRead = src.read(buffer)) != -1) {
                dest.write(buffer, 0, bytesRead);
                dest.flush();
            }
        } catch (IOException ignored) {}
    }

    private static void sendHttpSuccess(SocketChannel channel) throws IOException {
        String res = "HTTP/1.1 200 Connection Established\r\n\r\n";
        channel.write(ByteBuffer.wrap(res.getBytes(StandardCharsets.UTF_8)));
    }

    private static void sendHttpError(SocketChannel channel, int status, String message) throws IOException {
        String res = "HTTP/1.1 " + status + " " + message + "\r\nConnection: close\r\n\r\n";
        channel.write(ByteBuffer.wrap(res.getBytes(StandardCharsets.UTF_8)));
    }

    private static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try { resource.close(); } catch (Exception ignored) {}
        }
    }
}
