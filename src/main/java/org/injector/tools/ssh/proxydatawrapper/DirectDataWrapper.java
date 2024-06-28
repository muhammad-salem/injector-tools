/**
 *
 */
package org.injector.tools.ssh.proxydatawrapper;

import org.injector.tools.speed.TerminalNetworkMonitor;

import java.io.IOException;
import java.net.Socket;

/**
 * @author salem
 *
 */
public class DirectDataWrapper extends ProxyDataWrapper {

    /**
     * Same as calling {@link #HTTPProxyData(String, int, String, String) HTTPProxyData(proxyHost, proxyPort, <code>null</code>, <code>null</code>)}
     *
     * @param proxyHost Proxy hostname.
     * @param proxyPort Proxy port.
     */
    public DirectDataWrapper(String proxyHost, int proxyPort) {
        super(proxyHost, proxyPort, null, null);
    }

    /**
     * Same as calling {@link #HTTPProxyData(String, int, String, String) HTTPProxyData(proxyHost, proxyPort, <code>null</code>, <code>null</code>)}
     *
     * @param proxyHost proxywrapper host name.
     * @param proxyPort proxywrapper port.
     * @param monitorSpeed network speed monitor
     */
    public DirectDataWrapper(String proxyHost, int proxyPort, TerminalNetworkMonitor monitorSpeed) {
        super(proxyHost, proxyPort, null, monitorSpeed);
    }

    @Override
    public Socket openSoccketConnection(String hostname, int port, int connectTimeout) throws IOException {
        return new Socket(hostname, port);
    }


}
