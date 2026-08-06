package org.injector.tools.ssh.proxyhandler;

import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import org.injector.tools.speed.NetworkMonitorSpeed;
import org.injector.tools.ssl.SSLUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

@Slf4j
public class SniHostNameInjectionProxy extends ProxySocket {

    protected final String sniHost;

    public SniHostNameInjectionProxy(String sniHost, NetworkMonitorSpeed monitorSpeed) {
        super("", 0, monitorSpeed);
        this.sniHost = sniHost;
    }

    public SniHostNameInjectionProxy(String proxyHost, int proxyPort, String sniHost, NetworkMonitorSpeed monitorSpeed) {
        super(proxyHost, proxyPort, monitorSpeed);
        this.sniHost = sniHost;
    }

    @Override
    public Socket openSocketConnection(String hostname, int port, int timeout) throws IOException, JSchException {
        var address = InetAddress.getByName(hostname);
        log.info("Resolve Host name: [{}] with IP [{}]", hostname, address.toString());
//        var factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        var factory = SSLUtils.getSSLSocketFactory();
        var socket = (SSLSocket) factory.createSocket(address.getHostAddress(), port);
        var serverName = new SNIHostName(this.sniHost);
        var params = socket.getSSLParameters();
        params.setServerNames(List.of(serverName));
        socket.setSSLParameters(params);
        log.info("Use SNI Host Name: {}", (Object) this.sniHost);
        return socket;
    }

}
