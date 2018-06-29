package org.injector.tools.ssh.proxyhandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.injector.tools.log.Logger;
import org.injector.tools.speed.NetworkMonitorSpeed;

import com.jcraft.jsch.JSchException;
import com.trilead.ssh2.HTTPProxyException;
import com.trilead.ssh2.crypto.Base64;

public class HTTPProxy extends ProxySocket{

	protected String proxyUser;
	protected String proxyPass;
	protected String[] headers;
	
	public HTTPProxy(String proxyHost, int proxyPort) {
		this(proxyHost, proxyPort, null, null, null);
	}

	public HTTPProxy(String proxyHost, int proxyPort, NetworkMonitorSpeed monitorSpeed) {
		this(proxyHost, proxyPort, null, null, monitorSpeed);
	}
	
	public HTTPProxy(String proxyHost, int proxyPort, String proxyUser, String proxyPass,
			NetworkMonitorSpeed monitorSpeed) {
		this(proxyHost, proxyPort, proxyUser, proxyPass, monitorSpeed,null);
	}
	
	public HTTPProxy(String proxyHost, int proxyPort, String proxyUser, String proxyPass,
			NetworkMonitorSpeed monitorSpeed, String[] headers) {
		super(proxyHost, proxyPort, monitorSpeed);
		this.proxyUser = proxyUser;
		this.proxyPass = proxyPass;
		this.headers = headers;
	}
	
	public String getProxyUser() {
		return proxyUser;
	}

	public String getProxyPass() {
		return proxyPass;
	}
	
	public void setHeaders(String[] headers) {
		this.headers = headers;
	}
	public String[] getHeaders() {
		return headers;
	}
	
	@Override
	public Socket openSoccketConnection(String hostname, int port, int connectTimeout) throws IOException, JSchException{
		Socket sock = new Socket();

		InetAddress addr = InetAddress.getByName(proxyHost);
		sock.connect(new InetSocketAddress(addr, proxyPort), connectTimeout);
//		sock.setSoTimeout(connectTimeout);

			/* OK, now tell the proxy the host we actually want to connect to */

		StringBuffer sb = new StringBuffer();

		sb.append("CONNECT ");
		sb.append(hostname);
		sb.append(':');
		sb.append(port);
		sb.append(" HTTP/1.0\r\n");


		if ((proxyUser != null) && (proxyPass != null))
		{
			String credentials = proxyUser + ":" + proxyPass;
			char[] encoded = Base64.encode(credentials.getBytes("ISO-8859-1"));
			sb.append("Proxy-Authorization: Basic ");
			sb.append(encoded);
			sb.append("\r\n");
		}

		if (headers != null)
		{
			for (int i = 0; i < headers.length; i++)
			{
				if (headers[i] != null)
				{
					sb.append(headers[i]);
					sb.append("\r\n");
				}
			}
		}

		sb.append("\r\n");
        Logger.debug(getClass(), sb.toString());


		OutputStream out = sock.getOutputStream();

		out.write(sb.toString().getBytes("ISO-8859-1"));
		out.flush();

			/* Now parse the HTTP response */

		byte[] buffer = new byte[1024];
		InputStream in = sock.getInputStream();

		int len = readLineRN(in, buffer);

		String httpReponse = new String(buffer, 0, len, "ISO-8859-1");

		if (httpReponse.startsWith("HTTP/") == false)
			throw new IOException("The proxywrapper did not send back a valid HTTP response.");

			/* "HTTP/1.X XYZ X" => 14 characters minimum */

		if ((httpReponse.length() < 14) || (httpReponse.charAt(8) != ' ') || (httpReponse.charAt(12) != ' '))
			throw new IOException("The proxywrapper did not send back a valid HTTP response.");

		int errorCode = 0;

		try
		{
			errorCode = Integer.parseInt(httpReponse.substring(9, 12));
		}
		catch (NumberFormatException ignore)
		{
			throw new IOException("The proxywrapper did not send back a valid HTTP response.");
		}

		if ((errorCode < 0) || (errorCode > 999))
			throw new IOException("The proxywrapper did not send back a valid HTTP response.");

		if (errorCode != 200)
		{
			throw new HTTPProxyException(httpReponse.substring(13), errorCode);
		}

			/* OK, read until empty line */

		while (true)
		{
			len = readLineRN(in, buffer);
			if (len == 0)
				break;
		}
		return sock;
	}

	
	


}
