package org.injector.tools.ssh.proxyhandler;

import com.jcraft.jsch.JSchException;
import org.injector.tools.log.Logger;
import org.injector.tools.speed.NetworkMonitorSpeed;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class SNIInjectProxy extends ProxySocket {

    private final String sniHost;

    public SNIInjectProxy(String sniHost, NetworkMonitorSpeed monitorSpeed) {
        super("", 0, monitorSpeed);
        this.sniHost = sniHost;
    }

    @Override
    public Socket openSocketConnection(String hostname, int port, int timeout) throws IOException, JSchException {
        var address = InetAddress.getByName(hostname);
        Logger.debug(getClass(),"Resolve Host name: [%s] with IP [%s]", hostname, address.getHostAddress());
        var factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        var socket = (SSLSocket) factory.createSocket(address.getHostAddress(), port);
        var serverName = new SNIHostName(this.sniHost);
        var params = socket.getSSLParameters();
        params.setServerNames(List.of(serverName));
        socket.setSSLParameters(params);
        Logger.debug(getClass(),"Use SNI Host Name: %s", (Object) this.sniHost);
        return socket;
    }

}
