package org.injector.tools.ssh.proxyhandler;

import com.jcraft.jsch.JSchException;
import org.injector.tools.log.Logger;
import org.injector.tools.speed.NetworkMonitorSpeed;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;

public class SniHostNameInjectionProxy extends ProxySocket {

    private final String sniHost;

    public SniHostNameInjectionProxy(String sniHost, NetworkMonitorSpeed monitorSpeed) {
        super("", 0, monitorSpeed);
        this.sniHost = sniHost;
    }

    @Override
    public Socket openSocketConnection(String hostname, int port, int timeout) throws IOException, JSchException {
        var address = InetAddress.getByName(hostname);
        Logger.debug(getClass(),"Resolve Host name: [%s] with IP [%s]", hostname, address.toString());
//        var factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        var factory = this.getSSLSocketFactory();
        var socket = (SSLSocket) factory.createSocket(address.getHostAddress(), port);
        var serverName = new SNIHostName(this.sniHost);
        var params = socket.getSSLParameters();
        params.setServerNames(List.of(serverName));
        socket.setSSLParameters(params);
        Logger.debug(getClass(),"Use SNI Host Name: %s", (Object) this.sniHost);
        return socket;
    }

    private SSLSocketFactory getSSLSocketFactory() {
        var trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
        try {
            var sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            return  (SSLSocketFactory) SSLSocketFactory.getDefault();
        }
    }

}
