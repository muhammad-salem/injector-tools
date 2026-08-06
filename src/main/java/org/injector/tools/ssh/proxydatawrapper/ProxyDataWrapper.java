/**
 *
 */
package org.injector.tools.ssh.proxydatawrapper;

import com.trilead.ssh2.ProxyData;
import lombok.Getter;
import lombok.Setter;
import org.injector.tools.speed.TerminalNetworkMonitor;
import org.injector.tools.speed.net.MonitorSocketWrapper;

import java.io.IOException;
import java.net.Socket;


/**
 * @author salem
 *
 */
public abstract class ProxyDataWrapper implements ProxyData {


    protected final String proxyHost;
    protected final int proxyPort;
    protected final String[] requestHeaderLines;


    @Setter
    private TerminalNetworkMonitor networkMonitorSpeed;

    @Getter
    @Setter
    private MonitorSocketWrapper socketWrapper;


    public ProxyDataWrapper(String proxyHost, int proxyPort) {
        this(proxyHost, proxyPort, null, new TerminalNetworkMonitor());
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
     * @param requestHeaderLines An array with additional request header lines (without end-of-line markers)
     *                           that have to be sent to the server. May be <code>null</code>.
     */

    public ProxyDataWrapper(String proxyHost, int proxyPort, String[] requestHeaderLines) {
        this(proxyHost, proxyPort, requestHeaderLines, new TerminalNetworkMonitor());

    }

    public ProxyDataWrapper(String proxyHost, int proxyPort, TerminalNetworkMonitor nm) {
        this(proxyHost, proxyPort, null, nm);
    }

    public ProxyDataWrapper(String proxyHost, int proxyPort, String[] requestHeaderLines, TerminalNetworkMonitor nm) {
        if (proxyHost == null)
            throw new IllegalArgumentException("proxyHost must be non-null");

        if (proxyPort < 0)
            throw new IllegalArgumentException("proxyPort must be non-negative");

        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.requestHeaderLines = requestHeaderLines;
        this.networkMonitorSpeed = nm;
    }


    @Override
    public Socket openConnection(String hostname, int port, int connectTimeout) throws IOException {
        Socket socket = openSocketConnection(hostname, port, connectTimeout);
        return monitorSocket(socket);
    }


    public abstract Socket openSocketConnection(String hostname, int port, int connectTimeout) throws IOException;


    private MonitorSocketWrapper monitorSocket(Socket socket) {
        return socketWrapper = new MonitorSocketWrapper(socket, networkMonitorSpeed);
    }

}
