package org.injector.tools.proxy.handler.nio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class HttpToHttpsProxyServer {

    private static final int LOCAL_PORT = 8989;
    private static final String UPSTREAM_HOST = "77.111.246.24";
    private static final int UPSTREAM_PORT = 443;
    private static final String PROXY_USER = "8BF16D3D82F6E0368030C053B91ACB4CABBFE57E";
    private static final String PROXY_PASS = "eyJhbGciOiJFQ0RILUVTK0EyNTZLVyIsImN0eSI6IkpXVCIsImVuYyI6IkEyNTZHQ00iLCJlcGsiOnsia3R5IjoiRUMiLCJjcnYiOiJQLTI1NiIsIngiOiI3RUszaWl6Q3MtbW9DanJEZC1GSkJCVUlLSktJbl9tblV4REh0X0dIeWVZIiwieSI6IjZDaWpNalRuMWw2M1JRZWlDQUk4ZnlNd0h0VFZRTS1hbk1fNXdNdkdGMGcifX0.EyLGrFwUtZn5w2eXxoRaO9NykDJeIrW4yBqdRLiwPxq3_l3EZyHJpQ.V151C48EfbAYLzKH.aTBBVaS9wrHy7sMB44qjDXfVHYymOYNlYSKxzhbzzGfRswIjqRUbxpcno3HMbqQMv9gZXK1AZE2lI3rR5-4oOXeYlVjoVAKQwCORRxE8rCR8duStQEjhaE1C1cF83MVnmqUgNQHNBIyMQZ7g6uVOx1IT2XIKsVWG3BTdFEbNw3ppMEqY_XPbBicqeRk3_-fdy9Nkfz3vkVb4IbvyPtxbX9T1y1XssfwSKjGAM4laDgedpEjf35HoOVInXR8cnvqPVgqVZgjq_5VtEIWu1cUv7b_owzHrh6PhtPDeqnYseP0NrEiGLtJvjeEBbQIi7FfAmgr84qlhDuEaaxckDyhJQBXoZP5FNYxSsZ9TVxNcSZc7isz3ydGV1UnuYMBRkHtc6F8znEzZV6do5pzTAW4-AOa1-_lPjhsJY7ROIF4oBGH5w3W6DdUxVf_zb1w72ezNopM_D959pyHh4ag.I4azn7hhkiP-0SBqi93x0Q";

    public static void main(String[] args) {
        log.info("Starting local HTTP proxy on port {}", LOCAL_PORT);

        try (ServerSocket serverSocket = new ServerSocket(LOCAL_PORT)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                log.debug("Accepted connection from client: {}", clientSocket.getRemoteSocketAddress());

                // Submit to a new Virtual Thread for each client connection
                Thread.startVirtualThread(new ProxyHandler(clientSocket));
            }
        } catch (Exception e) {
            log.error("Fatal error in server socket loop", e);
        }
    }

    @RequiredArgsConstructor
    private static class ProxyHandler implements Runnable {
        private final Socket clientSocket;

        @Override
        public void run() {
            String authHeader = "Basic " + Base64.getEncoder()
                    .encodeToString((PROXY_USER + ":" + PROXY_PASS).getBytes(StandardCharsets.UTF_8));

            try (Socket localSocket = clientSocket;
                 InputStream clientIn = localSocket.getInputStream();
                 OutputStream clientOut = localSocket.getOutputStream()) {

                // 1. Read the initial target request/handshake from local client
                byte[] buffer = new byte[8192];
                int bytesRead = clientIn.read(buffer);
                if (bytesRead == -1) return;

                String initialRequest = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                
                // 2. Establish TLS connection to the upstream secure proxy
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                try (SSLSocket upstreamSocket = (SSLSocket) factory.createSocket(UPSTREAM_HOST, UPSTREAM_PORT)) {
                    upstreamSocket.startHandshake();
                    
                    InputStream upstreamIn = upstreamSocket.getInputStream();
                    OutputStream upstreamOut = upstreamSocket.getOutputStream();

                    // 3. Inject Proxy-Authorization headers if it's a CONNECT tunnel or standard HTTP request
                    String modifiedRequest = injectProxyAuth(initialRequest, authHeader);

                    // 4. Forward the initial modified request to upstream
                    upstreamOut.write(modifiedRequest.getBytes(StandardCharsets.UTF_8));
                    upstreamOut.flush();

                    // 5. Spawn two virtual threads to bridge bidirectional raw data stream
                    Thread toUpstream = Thread.startVirtualThread(() -> bridgeData(clientIn, upstreamOut));
                    Thread toClient = Thread.startVirtualThread(() -> bridgeData(upstreamIn, clientOut));

                    // Wait for both payload-transfer threads to finish
                    toUpstream.join();
                    toClient.join();
                }
            } catch (Exception e) {
                log.error("Error handling proxy routing details: {}", e.getMessage());
            } finally {
                log.debug("Connection closed for client");
            }
        }

        private String injectProxyAuth(String request, String authHeader) {
            StringBuilder newHeaders = new StringBuilder();
            String[] lines = request.split("\r\n");

            for (String line : lines) {
                String lower = line.toLowerCase();
                if (!lower.startsWith("proxy-authorization:")) {
                    newHeaders.append(line).append("\r\n");
                }
            }

            newHeaders.append("Proxy-Authorization: ");
            newHeaders.append(authHeader);
            newHeaders.append("\r\n\r\n"); // End header block

            return newHeaders.toString();
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
}
