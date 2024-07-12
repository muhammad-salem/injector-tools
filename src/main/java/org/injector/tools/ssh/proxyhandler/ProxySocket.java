package org.injector.tools.ssh.proxyhandler;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.SocketFactory;
import com.trilead.ssh2.ProxyData;
import lombok.Setter;
import org.injector.tools.log.Logger;
import org.injector.tools.speed.NetworkMonitorSpeed;
import org.injector.tools.speed.net.MonitorSocketWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;


@Setter
public abstract class ProxySocket implements Proxy, ProxyData {
    protected String proxyHost;
    protected int proxyPort;
    protected InputStream in;
    protected OutputStream out;

    protected NetworkMonitorSpeed monitorSpeed;
    protected MonitorSocketWrapper socket;

    public ProxySocket(String proxyHost, int proxyPort) {
        this(proxyHost, proxyPort, null);
    }

    public ProxySocket(String proxyHost, int proxyPort, NetworkMonitorSpeed monitorSpeed) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.monitorSpeed = Objects.requireNonNullElseGet(monitorSpeed, NetworkMonitorSpeed::new);
    }

    @Override
    public Socket openConnection(String hostname, int port, int timeout) throws IOException {
        connect(null, hostname, port, timeout);
        return socket;
    }

    public abstract Socket openSocketConnection(String hostname, int port, int timeout) throws IOException, JSchException;

    @Override
    public void connect(SocketFactory socket_factory, String host, int port, int timeout) {
        try {
//			if (socket_factory == null) {
            Logger.debug(getClass(), "create proxy socket");
            var directSocket = openSocketConnection(host, port, timeout);
            this.socket = new MonitorSocketWrapper(directSocket, monitorSpeed);
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
//			} else {
//				Logger.debug(getClass(), "create proxy socket using 'socket_factory'");
//				var directSocket = socket_factory.createSocket(host, port);
//				this.socket = new MonitorSocketWrapper(directSocket, monitorSpeed);
//				this.in = socket_factory.getInputStream(socket);
//		        this.out = socket_factory.getOutputStream(socket);
//			}

        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    @Override
    public Socket getSocket() {
        return socket;
    }

    @Override
    public InputStream getInputStream() {
        return in;
    }

    @Override
    public OutputStream getOutputStream() {
        return out;
    }

    @Override
    public void close() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (Exception e) {
        }
        in = null;
        out = null;
        socket = null;
    }

    public final int readLineRN(InputStream is, byte[] buffer) throws IOException {
        int pos = 0;
        boolean need10 = false;
        int len = 0;
        while (true) {
            int c = is.read();
            if (c == -1)
                throw new IOException("Premature connection close");

            buffer[pos++] = (byte) c;

            if (c == 13) {
                need10 = true;
                continue;
            }

            if (c == 10)
                break;

            if (need10)
                throw new IOException("Malformed line sent by the server, the line does not end correctly.");

            len++;
            if (pos >= buffer.length)
                throw new IOException("The server sent a too long line.");
        }

        return len;
    }
}
