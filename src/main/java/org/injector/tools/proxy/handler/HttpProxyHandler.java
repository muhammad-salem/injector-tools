package org.injector.tools.proxy.handler;

import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.log.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HttpProxyHandler extends ProxyHandler {

    public HttpProxyHandler(SocketChannel clientSocket, HostProxyConfig proxyConfig, ChannelSelector channelSelector) {
        super(clientSocket, proxyConfig, channelSelector);
    }


//	public HttpProxyHandler(Socket client, String requestLine) {
//		super(client, requestLine);
//	}


    @Override
    void handelProxyResponse() {
        Logger.debug(getClass(), "---> Proxy request sent, awaiting response....... .. .");
//		byte[] temp = new byte[8 * 1024];
        ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
        int bytes_read = 0;
        try {
//			if(count >= 1) {
//				proxyOutput.write("hi\r\n\r\n".getBytes());
//				proxyOutput.flush();
//				sendNormalRequest();
//				writePayloadToProxy();
//			}
            bytes_read = remote.read(buffer);
            if (bytes_read == -1) {
                Logger.debug(getClass(), "---> end of proxywrapper response");
                return;
            }
        } catch (IOException e) {
            e.fillInStackTrace();
        }

        String response = new String(buffer.array(), 0, bytes_read, StandardCharsets.ISO_8859_1);
        Logger.debug(getClass(), "---> read response data", response);

//		ResponsLine l = new ResponsLine();
//		l.setResponse(str);
//		Logger.debug("Respose ", l.toString());

        buffer.flip();
        if (response.contains("200 Connect")) {
            int start = response.indexOf("200 Connect") - 9;
            response = response.substring(start);
            try {

                client.write(buffer);
//				LogSlf4j(str);
                Logger.debug(getClass(), "---> response send 200 OK to client", response);
            } catch (IOException e) {
                e.fillInStackTrace();
            }
        }
//		HTTP/1.1 200 OK\r\n\r\n
        else if (response.endsWith(" 200 OK\r\n\r\n")) {
            try {
                client.write(buffer);
//				LogSlf4j(str);
                Logger.debug(getClass(), "---> response send", response);
            } catch (IOException e) {
                e.fillInStackTrace();
            }
        }
        // the HTTP
        else if (response.contains("\r\n\r\nHTTP")) {
            int start = response.indexOf("\r\n\r\nHTTP") + 2;
            response = response.substring(start);
            try {
                buffer.position(start);
                client.write(buffer);
                Logger.debug(getClass(), "---> response send", response);
            } catch (IOException e) {
                e.fillInStackTrace();
            }
        } else if (response.contains("onnection: close\r\n")) {
            try {
                client.write(buffer);
                Logger.debug(getClass(), "response send -- close client socket", response);
                client.close();
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }
//		else if(str.contains("HTTP/1.1 302 Found")){
//			try {
//				sendNormalRequest();
//				writeResponseToClient();
//				return;
//			} catch (Exception e) {
//				e.fillInStackTrace();
//			}
//			
//		}
//		else if(str.contains("301 Found") || str.contains("302 Found") || str.contains("307 Temporary Redirect")){
////		else if(l.getCode()/ 100 == 3){
//			try {
//				sendNormalRequest();
//				handelProxyResponse();
//				return;
//			} catch (Exception e) {
//				e.fillInStackTrace();
//			}	
//		}
//		else {
////			++count;
////			handelProxyResponse();
//		}

    }


}
