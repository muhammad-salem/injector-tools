package org.injector.tools.ssh.jsch.forwarding;

import com.jcraft.jsch.ChannelDirectTCPIP;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.connectbot.simplesocks.Socks5Server;
import org.injector.tools.log.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Socks5PortForwarding extends Thread {
    private final Session session;
    private final ServerSocket ss;

    public Socks5PortForwarding(Session session, int local_port)
            throws IOException {
        this.session = session;

        setName("Socks5PortForwarding");

        ss = new ServerSocket(local_port);
//		System.out.println(ss.getLocalPort());
    }

    public Socks5PortForwarding(Session session, InetSocketAddress localAddress)
            throws IOException {
        this.session = session;

        ss = new ServerSocket();
        ss.bind(localAddress);
    }

    @Override
    public void run() {
        while (session.isConnected()) {
            final Socket sock;
            try {
                sock = ss.accept();
            } catch (IOException e) {
                stopWorking();
                return;
            }

            Socks5Runnable runnable = new Socks5Runnable(sock);
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            t.start();


        }
    }

    public void stopWorking() {
        try {
            /* This will lead to an IOException in the ss.accept() call */
            ss.close();
        } catch (IOException ignore) {
        }
    }

    class Socks5Runnable implements Runnable {
        private static final int idleTimeout = 180000; //3 minutes

        private final Socket sock;
        private InputStream in;
        private OutputStream out;

        public Socks5Runnable(Socket sock) {
            this.sock = sock;

            setName("Socks5Runnable");
        }

        public void run() {
            try {
                startSession();
            } catch (IOException ioe) {
                try {
                    sock.close();
                } catch (IOException ignore) {
                }
            }
        }

        private void startSession() throws IOException {
            sock.setSoTimeout(idleTimeout);

            in = sock.getInputStream();
            out = sock.getOutputStream();
            Socks5Server server = new Socks5Server(in, out);
            try {
                if (!server.acceptAuthentication() || !server.readRequest()) {
                    Logger.debug(this.getClass(), "Could not start SOCKS session");
                    return;
                }
            } catch (IOException ioe) {
                server.sendReply(Socks5Server.ResponseCode.GENERAL_FAILURE);
                return;
            }

            if (server.getCommand() == Socks5Server.Command.CONNECT) {
                onConnect(server);
            } else {
                server.sendReply(Socks5Server.ResponseCode.COMMAND_NOT_SUPPORTED);
            }
        }

        private void onConnect(Socks5Server server) throws IOException {
            final ChannelDirectTCPIP channelDirectTCPIP;

            String destHost = server.getHostName();
            if (destHost == null) {
                destHost = server.getAddress().getHostAddress();
            }

            try {
                /*
                 * This may fail, e.g., if the remote port is closed (in
                 * optimistic terms: not open yet)
                 */
                channelDirectTCPIP = (ChannelDirectTCPIP) session.openChannel("direct-tcpip");
                channelDirectTCPIP.setHost(destHost);
                channelDirectTCPIP.setPort(server.getPort());

//				cn = cm.openDirectTCPIPChannel(destHost, server.getPort(),
//						"127.0.0.1", 0);

            } catch (Exception e) {
                /*
                 * Try to send a notification back to the client and then close the socket.
                 */
                try {
                    server.sendReply(Socks5Server.ResponseCode.GENERAL_FAILURE);
                } catch (IOException ignore) {
                }

                try {
                    sock.close();
                } catch (IOException ignore) {
                }

                return;
            }

            server.sendReply(Socks5Server.ResponseCode.SUCCESS);

            channelDirectTCPIP.setInputStream(in);
            channelDirectTCPIP.setOutputStream(out);
            try {
                channelDirectTCPIP.connect();
            } catch (JSchException e) {
                e.fillInStackTrace();
            }


        }
    }
}