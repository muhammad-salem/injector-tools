package org.injector.tools.proxy.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.log.Logger;

public class HandlerByResponseLine extends ProxyHandler{

	public HandlerByResponseLine(SocketChannel clientSocket, HostProxyConfig proxyConfig, ChannelSelector channelSelector) {
		super(clientSocket, proxyConfig, channelSelector);
	}

	@Override
	void handelProxyResponse() {
		Logger.debug(getClass(), "---> Proxy request sent, awaiting response....... .. .");
//		byte[] temp = new byte[8 * 1024];
		ByteBuffer buffer = ByteBuffer.allocate(8*1024);
		int bytes_read = 0;
		try {
//			if(count >= 1) {
//				proxyOutput.write("hi\r\n\r\n".getBytes());
//				proxyOutput.flush();
//				sendNormalRequest();
//				writePayloadToProxy();
//			}
			bytes_read = remote.read(buffer);
			if(bytes_read == -1) {
				Logger.debug( getClass().getSimpleName(), "---> end of proxywrapper response");
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		buffer.flip();
		String respons = new String(buffer.array(), 0, bytes_read, Charset.forName("ISO-8859-1"));
		Logger.debug(getClass(), "---> read response data", respons);
		
//		ResponsLine l = new ResponsLine();
//		l.setResponse(str);
//		Logger.debug("Respose ", l.toString());
		
		if(respons.contains("200 Connect")){
			int start = respons.indexOf("200 Connect")-9;
			respons = respons.substring(start);
			try {
				buffer.position(start);
				client.write(buffer);
				Logger.debug(getClass(), "---> response send 200 OK to client", respons);
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		HTTP/1.1 200 OK\r\n\r\n
		else if(respons.endsWith(" 200 OK\r\n\r\n")){
			try {
				
				client.write(buffer);
				Logger.debug(getClass(), "---> response send",respons);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		else if(respons.contains("\r\n\r\nHTTP")){
			int start = respons.indexOf("\r\n\r\nHTTP")+2;
			respons = respons.substring(start);
			try {
				buffer.position(start);
				client.write(buffer);
				Logger.debug(getClass(), "---> response send", respons);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}else if(respons.contains("onnection: close\r\n")){
			try {
				client.write(buffer);
				Logger.debug(getClass(), "response send -- close client socket",respons);
				client.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
//		else if(str.contains("HTTP/1.1 302 Found")){
//			try {
//				sendNormalRequest();
//				writeResponseToClient();
//				return;
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			
//		}
		else if(respons.contains("301 Found") || respons.contains("302 Found") || respons.contains("307 Temporary Redirect")){
//		else if(l.getCode()/ 100 == 3){
			try {
				sendNormalRequest();
				handelProxyResponse();
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		else {
//			++count;
			handelProxyResponse();
		}
		
	}

}
