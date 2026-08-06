package org.injector.tools.proxy.handler;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SecureProxyTunnel {

    public static void main(String[] args) {
        // Proxy configuration
        String proxyHost = "77.111.245.12";
        int proxyPort = 443;
        String proxyUser = "AA8AB86A9242CE303DC16DE3FC37954A438CEDC9";
        String proxyPass = "eyJhbGciOiJFQ0RILUVTK0EyNTZLVyIsImN0eSI6IkpXVCIsImVuYyI6IkEyNTZHQ00iLCJlcGsiOnsia3R5IjoiRUMiLCJjcnYiOiJQLTI1NiIsIngiOiJ1WER3R1hZY2RPU1haMGdkNFFmV05Ndmt6ZWZUbW1GMkNJS0pZZVFNMlVVIiwieSI6IjlYWld0VFVUNEtTM3NBQktVSmhldEl4YUMyS2hrNHlNWmlyTnlkQldGSVEifX0.3OD9UMDXmfX9kn2QFggSVv3Qf_MnlWJzKAiD2KZHww7FNP-Vg4elVg.-NYZIh4wWkSCkf2a.RI8Mp3DxBrLJkNTIyjOHI1JgAoCNK_Yvd_abVcTpEfW5_BujDaFp7f0Dn4GVwd4u89atLoWq653q4_JG0DMjO12jJ_MGZoSZ1GBX8FNN1jI1P1sY2DxKJYdmQ5gX_-leyeli8GJS2erNjUO74CaVuifT2RypM-2tumKZnZGAAMpi9gGKlfZgBG9f3aa2kB4X-FyOnwbL4bgwlU-tTLYogBbhL1JWmALLEVXCSPpeVvo2SQYpshP8SGmFBdburo2uHRzK6tP0sHHpwoHK-dkssRga0jpYSWSK2-L96Yl42DLase0DvmaX2CxbL3cUAGflJ02fwW6sDe_l7l9D6qsz-rEUD9Dz3XvaqZJ4oPLOy5T3921_cdXeG3rqjXXOepi5VCbWZFdTIBonTOqGIqhQPx__MGx9ZD5TjYV3auoZzOZpzwcsepLIZxQF6envf0R67UMAoWFudZHO.L9wT9K9MG3wZF-oneXr-Bw";

        // Target configuration
        String targetHost = "www.google.com";
        int targetPort = 443;

        try {
            // 1. Connect to the TLS/SSL Proxy
            System.out.println("Connecting to secure proxy at " + proxyHost + ":" + proxyPort);
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket proxySocket = (SSLSocket) sslSocketFactory.createSocket(proxyHost, proxyPort);
            proxySocket.startHandshake();

            // 2. Prepare HTTP CONNECT request with Basic Auth
            String auth = proxyUser + ":" + proxyPass;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            OutputStream out = proxySocket.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

            writer.print("CONNECT " + targetHost + ":" + targetPort + " HTTP/1.1\r\n");
            writer.print("Host: " + targetHost + ":" + targetPort + "\r\n");
            writer.print("Proxy-Authorization: Basic " + encodedAuth + "\r\n");

            writer.print("SE-Client-Version: Stable 114.0.5282.21\r\n");
            writer.print("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 OPR/114.0.0.0\r\n");
            writer.print("SE-Operating-System: Windows\r\n");
            writer.print("Proxy-Connection: keep-alive\r\n");
            writer.print("Connection: keep-alive\r\n");

            writer.print("\r\n"); // Empty line signals end of headers
            writer.flush();

            // 3. Read the Proxy Response
            InputStream in = proxySocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String statusLine = reader.readLine();
            System.out.println("Proxy Response: " + statusLine);

            if (statusLine == null || !statusLine.contains("200")) {
                throw new IOException("Failed to establish tunnel through proxy: " + statusLine);
            }

            // Consume remaining proxy headers until the blank line
            String header;
            while ((header = reader.readLine()) != null && !header.isEmpty()) {
                // Optionally log or process proxy headers here
                System.out.println("Proxy header: " + header);
            }

            // 4. Layer a new SSLSocket over the established tunnel for end-to-end TLS
            System.out.println("Tunnel established. Starting TLS handshake with " + targetHost);
            SSLSocket targetSocket = (SSLSocket) sslSocketFactory.createSocket(
                    proxySocket, // Layer over the existing proxy socket
                    targetHost,
                    targetPort,
                    true // Auto-close the underlying proxy socket when this socket closes
            );
            targetSocket.startHandshake();
            System.out.println("TLS Handshake with " + targetHost + " successful!");

            // 5. Send an HTTP request to the final target (google.com)
            OutputStream targetOut = targetSocket.getOutputStream();
            PrintWriter targetWriter = new PrintWriter(new OutputStreamWriter(targetOut, StandardCharsets.UTF_8));
            targetWriter.print("GET / HTTP/1.1\r\n");
            targetWriter.print("Host: " + targetHost + "\r\n");
            targetWriter.print("Connection: close\r\n");
            targetWriter.print("\r\n");
            targetWriter.flush();

            // 6. Read and print the target server's response
            InputStream targetIn = targetSocket.getInputStream();
            BufferedReader targetReader = new BufferedReader(new InputStreamReader(targetIn, StandardCharsets.UTF_8));
            String line;
            while ((line = targetReader.readLine()) != null) {
                System.out.println(line);
            }

            // Clean up resources
            targetSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
