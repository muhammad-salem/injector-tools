package org.injector.tools.proxy.handler;

import lombok.extern.slf4j.Slf4j;
import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.payload.PlaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TunnelProxyHandler extends ProxyHandler {

    public TunnelProxyHandler(SocketChannel clientSocket, HostProxyConfig proxyConfig, ChannelSelector channelSelector) {
        super(clientSocket, proxyConfig, channelSelector);
    }

    @Override
    void handelProxyResponse() {
        log.info("proxy request had been send, ( waiting for response)");
        //byte[] temp = new byte[8 * 1024];
        ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
        int bytes_read = 0;
        try {
            bytes_read = remote.read(buffer);
            if (bytes_read == -1) {
                log.info("---> end of proxy wrapper response there is no more data because the end of the stream has been reached");
                return;
            } else if (bytes_read == 0) {
                bytes_read = remote.read(buffer);
            }
        } catch (IOException ignored) {
        }
        buffer.flip();
        String response = new String(buffer.array(), 0, bytes_read, StandardCharsets.ISO_8859_1);
        log.info("read proxy response: {}", response);

//		ResponsLine l = new ResponsLine();
//		l.setResponse(str);
//		Logger.debug("Response ", l.toString());

        if (response.contains("200 Connect")) {
            int start = response.indexOf("200 Connect") - 9;
            int length = bytes_read - start;
//			if(str.substring(start).contains("\r\n\r\nHTTP/")){
//				length =  str.indexOf("\r\n\r\nHTTP", start)+4;
            // write HTTP/1.x 200 connected\r\n\r\n
//				Logger.debug(getClass().getSimpleName(), "start = %d, length = %d, end = %d, bytes_read = %d;\n", start, length, start+length, bytes_read);
//				writeBytes(clientOutput, temp, start, length-start);

            //search for the next response part
//				start = str.indexOf("\r\n\r\n", end)+4;
//				end = str.indexOf("\r\n\r\n", start)+4;
//				Logger.debug(getClass().getSimpleName(), "start = %d, length = %d, end = %d, bytes_read = %d;\n", start, length, start+length, bytes_read);
//				writeBytes(clientOutput, temp, end, bytes_read);
//				return;
//			}else {

            log.info("start = {}, length = {}, end = {}, bytes_read = {}", start, length, start + length, bytes_read);

            //the normal state
            // write HTTP/1.x 200 connected\r\n\r\n

            try {
                buffer.position(start);
                client.write(buffer);
                log.info("write response to client: {}", response);
            } catch (IOException ignored) {
            }
            log.info(getPayload().getPlaceHolder(PlaceHolder.host), response.substring(start));
            //			}

        }
//		HTTP/1.1 200 OK\r\n\r\n
        else if (response.endsWith(" 200 OK\r\n\r\n")) {
            try {
                client.write(buffer);
                log.info("write response to client: {}", response);
            } catch (IOException ignored) {
            }
        } else if (response.contains("\r\n\r\nHTTP")) {
            int start = response.indexOf("\r\n\r\nHTTP") + 4;
            response = response.substring(start);
            try {
                buffer.position(start);
                client.write(buffer);
                log.info("write response to client: {}", response);
            } catch (IOException ignored) {
            }
        }
//		else if(str.contains("HTTP/1.1 302 Found")){
//			try {
//				sendNormalRequest();
//				writeResponseToClient();
//				return;
//			} catch (Exception ignored) {}
//			
//		}

        /* extra try from this app to perform connection */
        else if (response.contains("301 Found") || response.contains("302 Found") || response.contains("307 Temporary Redirect")) {
//		else if(l.getCode()/ 100 == 3){
            try {
                log.info("response state code 3xx: try send normal request");
                sendNormalRequest();
                handelProxyResponse();
            } catch (Exception ignored) {
            }

        } else if (response.contains("onnection: close\r\n")) {
            try {
                client.write(buffer);
                log.info("write response to client -&- close client socket: {}", response);
                client.close();
            } catch (Exception ignored) {
            }
        }
        // SSH-2.0
        else if (response.startsWith("SSH-2.0")) {
            try {
                client.write(ByteBuffer.wrap("HTTP/1.0 200 connected\r\n\r\n".getBytes(StandardCharsets.UTF_8)));
                client.write(buffer);
            } catch (Exception ignored) {
            }
        } else {
//			handelProxyResponse();
        }

    }


}
