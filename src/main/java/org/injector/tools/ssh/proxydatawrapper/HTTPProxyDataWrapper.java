/**
 *
 */
package org.injector.tools.ssh.proxydatawrapper;

import com.trilead.ssh2.HTTPProxyException;
import com.trilead.ssh2.crypto.Base64;
import com.trilead.ssh2.transport.ClientServerHello;
import lombok.extern.slf4j.Slf4j;
import org.injector.tools.speed.TerminalNetworkMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author salem
 *
 */
@Slf4j
public class HTTPProxyDataWrapper extends ProxyDataWrapper {
    private final String proxyUser;
    private final String proxyPass;

    /**
     * Same as calling {@link #HTTPProxyData(String, int, String, String) HTTPProxyData(proxyHost, proxyPort, <code>null</code>, <code>null</code>)}
     *
     * @param proxyHost Proxy hostname.
     * @param proxyPort Proxy port.
     */
    public HTTPProxyDataWrapper(String proxyHost, int proxyPort) {
        this(proxyHost, proxyPort, null, null, null, null);

    }

    /**
     * Same as calling {@link #HTTPProxyData(String, int, String, String, String[]) HTTPProxyData(proxyHost, proxyPort, <code>null</code>, <code>null</code>, <code>null</code>)}
     *
     * @param proxyHost Proxy hostname.
     * @param proxyPort Proxy port.
     * @param proxyUser Username for basic authentication (<code>null</code> if no authentication is needed).
     * @param proxyPass Password for basic authentication (<code>null</code> if no authentication is needed).
     */
    public HTTPProxyDataWrapper(String proxyHost, int proxyPort, String proxyUser, String proxyPass) {
        this(proxyHost, proxyPort, proxyUser, proxyPass, null, null);
    }

    /**
     * Connection data for an HTTP proxy wrapper. It is possible to specify a username and password
     * if the proxy wrapper requires basic authentication. Also, additional request header lines can
     * be specified (e.g., "User-Agent: CERN-LineMode/2.15 libwww/2.17b3").
     * <p>
     * Please note: if you want to use basic authentication, then both <code>proxyUser</code>
     * and <code>proxyPass</code> must be non-null.
     * <p>
     * Here is an example:
     * <p>
     * <code>
     * new HTTPProxyData("192.168.1.1", "3128", "proxyuser", "secret", new String[] {"User-Agent: TrileadBasedClient/1.0", "X-My-Proxy-Option: something"});
     * </code>
     *
     * @param proxyHost          Proxy hostname.
     * @param proxyPort          Proxy port.
     * @param proxyUser          Username for basic authentication (<code>null</code> if no authentication is needed).
     * @param proxyPass          Password for basic authentication (<code>null</code> if no authentication is needed).
     * @param requestHeaderLines An array with additional request header lines (without end-of-line markers)
     *                           that have to be sent to the server. May be <code>null</code>.
     */

    public HTTPProxyDataWrapper(String proxyHost, int proxyPort, String proxyUser, String proxyPass,
                                String[] requestHeaderLines) {
        this(proxyHost, proxyPort, proxyUser, proxyPass, requestHeaderLines, null);

    }

    public HTTPProxyDataWrapper(String proxyHost, int proxyPort, TerminalNetworkMonitor nm) {
        this(proxyHost, proxyPort, null, null, null, nm);
    }

    public HTTPProxyDataWrapper(String proxyHost, int proxyPort, String proxyUser, String proxyPass, TerminalNetworkMonitor nm) {
        this(proxyHost, proxyPort, proxyUser, proxyPass, null, nm);
    }

    public HTTPProxyDataWrapper(String proxyHost, int proxyPort, String proxyUser, String proxyPass,
                                String[] requestHeaderLines, TerminalNetworkMonitor nm) {
        super(proxyHost, proxyPort, requestHeaderLines, nm);
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;
    }


    public String getProxyUser() {
        return proxyUser;
    }

    public String getProxyPass() {
        return proxyPass;
    }

    @Override
    public Socket openSocketConnection(String hostname, int port, int connectTimeout) throws IOException {
        Socket sock = new Socket();

        InetAddress addr = InetAddress.getByName(proxyHost);
        sock.connect(new InetSocketAddress(addr, proxyPort), connectTimeout);
//		sock.setSoTimeout(connectTimeout);

        /* OK, now tell the proxy the host we actually want to connect to */

        StringBuilder sb = new StringBuilder();

        sb.append("CONNECT ");
        sb.append(hostname);
        sb.append(':');
        sb.append(port);
        sb.append(" HTTP/1.0\r\n");


        if ((proxyUser != null) && (proxyPass != null)) {
            String credentials = proxyUser + ":" + proxyPass;
            char[] encoded = Base64.encode(credentials.getBytes(StandardCharsets.ISO_8859_1));
            sb.append("Proxy-Authorization: Basic ");
            sb.append(encoded);
            sb.append("\r\n");
        }

        if (requestHeaderLines != null) {
            for (String requestHeaderLine : requestHeaderLines) {
                if (requestHeaderLine != null) {
                    sb.append(requestHeaderLine);
                    sb.append("\r\n");
                }
            }
        }

        sb.append("\r\n");
        log.info(sb.toString());


        OutputStream out = sock.getOutputStream();

        out.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
        out.flush();

        /* Now parse the HTTP response */

        byte[] buffer = new byte[1024];
        InputStream in = sock.getInputStream();

        int len = ClientServerHello.readLineRN(in, buffer);

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
            len = ClientServerHello.readLineRN(in, buffer);
        } while (len != 0);
        return sock;
    }

}
