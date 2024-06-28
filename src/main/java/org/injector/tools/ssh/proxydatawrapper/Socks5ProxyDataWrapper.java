/**
 *
 */
package org.injector.tools.ssh.proxydatawrapper;

import org.injector.tools.speed.TerminalNetworkMonitor;

import java.io.IOException;
import java.net.Socket;

/**
 *
 * still in development progress
 * @author salem
 *
 */
public class Socks5ProxyDataWrapper extends ProxyDataWrapper {
    private final String proxyUser;
    private final String proxyPass;

    /**
     * Same as calling {@link #HTTPProxyData(String, int, String, String) HTTPProxyData(proxyHost, proxyPort, <code>null</code>, <code>null</code>)}
     *
     * @param proxyHost Proxy hostname.
     * @param proxyPort Proxy port.
     */
    public Socks5ProxyDataWrapper(String proxyHost, int proxyPort) {
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
    public Socks5ProxyDataWrapper(String proxyHost, int proxyPort, String proxyUser, String proxyPass) {
        this(proxyHost, proxyPort, proxyUser, proxyPass, null, null);
    }

    /**
     * Connection data for a HTTP proxywrapper. It is possible to specify a username and password
     * if the proxywrapper requires basic authentication. Also, additional request header lines can
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
     * @param proxyHost Proxy hostname.
     * @param proxyPort Proxy port.
     * @param proxyUser Username for basic authentication (<code>null</code> if no authentication is needed).
     * @param proxyPass Password for basic authentication (<code>null</code> if no authentication is needed).
     * @param requestHeaderLines An array with additional request header lines (without end-of-line markers)
     *        that have to be sent to the server. May be <code>null</code>.
     */

    public Socks5ProxyDataWrapper(String proxyHost, int proxyPort, String proxyUser, String proxyPass,
                                  String[] requestHeaderLines) {
        this(proxyHost, proxyPort, proxyUser, proxyPass, requestHeaderLines, null);

    }

    public Socks5ProxyDataWrapper(String proxyHost, int proxyPort, TerminalNetworkMonitor nm) {
        this(proxyHost, proxyPort, null, null, null, nm);
    }

    public Socks5ProxyDataWrapper(String proxyHost, int proxyPort, String proxyUser, String proxyPass, TerminalNetworkMonitor nm) {
        this(proxyHost, proxyPort, proxyUser, proxyPass, null, nm);
    }

    public Socks5ProxyDataWrapper(String proxyHost, int proxyPort, String proxyUser, String proxyPass,
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
    public Socket openSoccketConnection(String hostname, int port, int connectTimeout) throws IOException {
        return null;
    }

}
