package org.injector.tools.ssh.proxyhandler;

import com.jcraft.jsch.JSchException;
import com.trilead.ssh2.HTTPProxyException;
import lombok.Getter;
import org.injector.tools.log.Logger;
import org.injector.tools.speed.NetworkMonitorSpeed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Getter
public class HttpProxy extends ProxySocket {

    protected String proxyUser;
    protected String proxyPass;
    protected String[] headers;

    public HttpProxy(String proxyHost, int proxyPort) {
        this(proxyHost, proxyPort, null, null, null);
    }

    public HttpProxy(String proxyHost, int proxyPort, NetworkMonitorSpeed monitorSpeed) {
        this(proxyHost, proxyPort, null, null, monitorSpeed);
    }

    public HttpProxy(String proxyHost, int proxyPort, String proxyUser, String proxyPass,
                     NetworkMonitorSpeed monitorSpeed) {
        this(proxyHost, proxyPort, proxyUser, proxyPass, monitorSpeed, null);
    }

    public HttpProxy(String proxyHost, int proxyPort, String proxyUser, String proxyPass,
                     NetworkMonitorSpeed monitorSpeed, String[] headers) {
        super(proxyHost, proxyPort, monitorSpeed);
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;
        this.headers = headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    @Override
    public Socket openSocketConnection(String hostname, int port, int connectTimeout) throws IOException, JSchException {
        Socket sock = new Socket();

        var address = InetAddress.getByName(proxyHost);
        sock.connect(new InetSocketAddress(address, proxyPort), connectTimeout);
//		sock.setSoTimeout(connectTimeout);

        /* OK, now tell the proxy the host we actually want to connect to */

        StringBuffer sb = new StringBuffer();

        sb.append("CONNECT ");
        sb.append(hostname);
        sb.append(':');
        sb.append(port);
        sb.append(" HTTP/1.0\r\n");


        if ((proxyUser != null) && (proxyPass != null)) {
            String credentials = proxyUser + ":" + proxyPass;
            byte[] encoded = Base64.getEncoder().encode(credentials.getBytes(StandardCharsets.ISO_8859_1));
            sb.append("Proxy-Authorization: Basic ");
            sb.append(Arrays.toString(encoded));
            sb.append("\r\n");
        }

        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                if (headers[i] != null) {
                    sb.append(headers[i]);
                    sb.append("\r\n");
                }
            }
        }

        sb.append("\r\n");
        Logger.debug(getClass(), sb.toString());


        OutputStream out = sock.getOutputStream();

        out.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
        out.flush();

        /* Now parse the HTTP response */

        byte[] buffer = new byte[1024];
        InputStream in = sock.getInputStream();

        int len = readLineRN(in, buffer);

        String httpResponse = new String(buffer, 0, len, StandardCharsets.ISO_8859_1);

        if (!httpResponse.startsWith("HTTP/"))
            throw new IOException("The proxy wrapper did not send back a valid HTTP response.");

        /* "HTTP/1.X XYZ X" => 14 characters minimum */

        if ((httpResponse.length() < 14) || (httpResponse.charAt(8) != ' ') || (httpResponse.charAt(12) != ' '))
            throw new IOException("The proxy wrapper did not send back a valid HTTP response.");

        int errorCode = 0;

        try {
            errorCode = Integer.parseInt(httpResponse.substring(9, 12));
        } catch (NumberFormatException ignore) {
            throw new IOException("The proxy wrapper did not send back a valid HTTP response.");
        }

        if ((errorCode < 0) || (errorCode > 999))
            throw new IOException("The proxy wrapper did not send back a valid HTTP response.");

        if (errorCode != 200) {
            throw new HTTPProxyException(httpResponse.substring(13), errorCode);
        }

        /* OK, read until empty line */

        do {
            len = readLineRN(in, buffer);
        } while (len != 0);
        return sock;
    }


}
