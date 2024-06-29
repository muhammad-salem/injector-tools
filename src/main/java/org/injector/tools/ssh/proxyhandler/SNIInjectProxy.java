package org.injector.tools.ssh.proxyhandler;

import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import org.injector.tools.log.Logger;
import org.injector.tools.speed.NetworkMonitorSpeed;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

@Slf4j
public class SNIInjectProxy extends ProxySocket {

    private final String serverNameIndication;

    public SNIInjectProxy(String serverNameIndication, NetworkMonitorSpeed monitorSpeed) {
        super("", 0, monitorSpeed);
        this.serverNameIndication = serverNameIndication;
    }

    @Override
    public Socket openSocketConnection(String hostname, int port, int timeout) throws IOException, JSchException {
        var address = InetAddress.getByName(hostname);
        log.info("ip addresses for host name: [{}] is [{}]", hostname, address.getHostAddress());
        var factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        var socket = (SSLSocket) factory.createSocket(address.getHostAddress(), port);
        var serverName = new SNIHostName(this.serverNameIndication);
        var params = socket.getSSLParameters();
        params.setServerNames(List.of(serverName));
        socket.setSSLParameters(params);
        return socket;
    }

}
