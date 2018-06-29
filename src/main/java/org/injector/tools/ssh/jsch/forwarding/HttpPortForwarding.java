package org.injector.tools.ssh.jsch.forwarding;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.jcraft.jsch.ChannelDirectTCPIP;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class HttpPortForwarding extends Thread {
	private Session session;
	private ServerSocket ss;

	class HttpRunnable implements Runnable {
		private static final int idleTimeout	= 180000; //3 minutes

		private Socket sock;
		private InputStream in;
		private OutputStream out;

		public HttpRunnable(Socket sock) {
			this.sock = sock;

			setName("HttpRunnable");
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
			
			
			HttpInputStream httpins = new HttpInputStream(sock.getInputStream());
			if(httpins.requestBytes.length == -1) {
				// write back to client internal error HTTP/1.1	500 (Internal Server Error)
				closeClientConnection();
				return;
			}
			
//			String req = new String(httpins.requestBytes, "utf-8");
//			Logger.debug(this.getClass(), req);
//			Logger.debug(this.getClass(), "req -> "+httpins.getHost()+':'+httpins.getPort());
			
			try {
				final ChannelDirectTCPIP channel = (ChannelDirectTCPIP) session.openChannel("direct-tcpip");
				channel.setHost(httpins.getHost());
				channel.setPort(httpins.getPortInt());
				
				
				
				
				if(httpins.getMethod().equals("CONNECT")) {
					out.write("HTTP/1.0 200 Connection established\r\n\r\n".getBytes("utf-8"));
					out.flush();
					
				}else {
					in = httpins;
				}
				
				
				channel.setInputStream(in);
				channel.setOutputStream(out);
				channel.connect();
				
				
				
			} catch (JSchException e) {
				// write back to client internal error HTTP/1.1 500 (Internal Server Error)
				closeClientConnection();
				return;
			}
			
			
			

		}
		
		private void closeClientConnection() throws UnsupportedEncodingException, IOException {
			// write back to client internal error HTTP/1.1	500 (Internal Server Error)
			out.write("HTTP/1.0 500 (Internal Server Error)\r\n\r\n".getBytes("utf-8"));
			out.flush();
			return;
		}
		
	}

	public HttpPortForwarding(Session session, int local_port)
			throws IOException {
		this.session = session;

		setName("HttpPortForwarding");

		ss = new ServerSocket(local_port);
//		System.out.println(ss.getLocalPort());
	}

	public HttpPortForwarding(Session session, InetSocketAddress localAddress)
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

			HttpRunnable runnable = new HttpRunnable(sock);
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
}
