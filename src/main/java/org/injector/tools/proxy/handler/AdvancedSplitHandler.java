package org.injector.tools.proxy.handler;

import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.log.Logger;
import org.injector.tools.payload.PlaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class AdvancedSplitHandler extends TunnelProxyHandler {


    boolean has200Ok = false;

    public AdvancedSplitHandler(SocketChannel clientSocket, HostProxyConfig proxyConfig, ChannelSelector channelSelector) {
        super(clientSocket, proxyConfig, channelSelector);
    }

    @Override
    protected void writePayloadToRemoteHost() {
        ArrayList<Integer> index = payload.getSplitIndexes();
        String requestLinePayload = payload.getRawPayload();
        try {
            if (index == null) {
                remote.write(ByteBuffer.wrap(requestLinePayload.getBytes()));
                Logger.debug(getClass(), "write payload raw to Proxy", requestLinePayload);
            } else {
                boolean readmore = true;
                for (int i = 0; i < index.size(); i += 2) {
                    remote.write(ByteBuffer.wrap(requestLinePayload.substring(index.get(i), index.get(i + 1)).getBytes()));
                    Logger.debug(getClass(), "write payload raw#" + i / 2, requestLinePayload.substring(index.get(i), index.get(i + 1)));

                    if (readmore) {
                        // try to handle the clean input stream of the proxy
                        //cleanProxyInputStream();
                        Thread cleanThread = new Thread(this::cleanProxyInputStream);
                        cleanThread.start();
                        try {
                            cleanThread.join(200);
                        } catch (InterruptedException e) {
                            e.fillInStackTrace();
                        }

                        if (has200Ok) {

                            Logger.debug(getClass(), "fire Success Listener");
                            fireSuccessListener();

//							clientOutput.write("HTTP/1.1 200 Connected\r\n\r\n".getBytes());
//							proxyOutput.write(0);

                            Logger.debug(getClass(), "stop writing the rest of payload ");
                            return;
                        }
                    }
                    if ((i + 2) >= index.size()) {
                        readmore = false;
                    }
                    Logger.debug(getClass(), "(i+2) >= index.size()", "is :" + Boolean.valueOf((i + 2) >= index.size()));

                }

            }
        } catch (IOException e) {
            e.fillInStackTrace();
        }

    }

    private void cleanProxyInputStream() {
        Logger.debug(getClass(), "clean proxy input stream ");
        has200Ok = handelProxyStateResponse();
    }

    boolean handelProxyStateResponse() {
        Logger.debug(getClass(), "proxy request had been send, ( waiting for response)");
        byte[] temp = new byte[8 * 1024];
        ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
        int bytes_read = 0;
        try {
            bytes_read = remote.read(buffer);
            buffer.flip();
            if (bytes_read == -1) {
                Logger.debug(getClass(), "---> end of proxywrapper response there is no more data because the end of the stream has been reached");
                return false;
            }
        } catch (IOException e) {
            e.fillInStackTrace();
        }

        String response = new String(temp, 0, bytes_read, StandardCharsets.ISO_8859_1);
        Logger.debug(getClass(), "start analysis for proxy response ", response);

//		ResponsLine l = new ResponsLine();
//		l.setResponse(str);
//		Logger.debug("Respose ", l.toString());

        if (response.contains("200 Connect")) {
            int start = response.indexOf("200 Connect") - 9;
            int length = bytes_read - start;
//			if(str.substring(start).contains("\r\n\r\nHTTP/")){
//				length =  str.indexOf("\r\n\r\nHTTP", start)+4;
            // write HTTP/1.x 200 connected\r\n\r\n
//				Logger.debug(getClass().getSimpleName(), "start = %d, length = %d, end = %d, bytes_read = %d;\n", start, length, start+length, bytes_read);
//				writeBytes(clientOutput, temp, start, length-start);

            //serch for the next response part
//				start = str.indexOf("\r\n\r\n", end)+4;
//				end = str.indexOf("\r\n\r\n", start)+4;
//				Logger.debug(getClass().getSimpleName(), "start = %d, length = %d, end = %d, bytes_read = %d;\n", start, length, start+length, bytes_read);
//				writeBytes(clientOutput, temp, end, bytes_read);
//				return;
//			}else {

            Logger.debug(getClass(), "start = %d, length = %d, end = %d, bytes_read = %d;\n", start, length, start + length, bytes_read);
//				System.out.printf("start = %d, length = %d, end = %d, bytes_read = %d;\n", start, length, start+length, bytes_read);

            //the normal state
            // write HTTP/1.x 200 connected\r\n\r\n
            buffer.position(start);
            try {
                client.write(buffer);
                Logger.debug(getClass(), getPayload().getPlaceHolder(PlaceHolder.host), response.substring(start));
            } catch (IOException e) {
                Logger.debug(e.getClass(), "error write response to client");
            }
            return true;
//			}

        }
//		HTTP/1.1 200 OK\r\n\r\n
        else if (response.endsWith(" 200 OK\r\n\r\n")) {
            try {
                client.write(buffer);
                Logger.debug(getClass(), "write response to client", response);
            } catch (IOException e) {
                e.fillInStackTrace();
            }
            return true;
        } else if (response.contains("\r\n\r\nHTTP")) {
            int start = response.indexOf("\r\n\r\nHTTP") + 4;
            response = response.substring(start);
            try {
                buffer.position(start);
                client.write(buffer);
                Logger.debug(getClass(), "write response to client", response);
            } catch (IOException e) {
                e.fillInStackTrace();
            }
            return true;
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

        /* extra try from this app to perform connection */
        else if (response.contains("301 Found") || response.contains("302 Found") || response.contains("307 Temporary Redirect")) {
//		else if(l.getCode()/ 100 == 3){
            try {
                Logger.debug(getClass(), "response state code 3xx", "try send normal request");
                sendNormalRequest();
                return handelProxyStateResponse();
            } catch (Exception e) {
                e.fillInStackTrace();
            }

        } else if (response.contains("onnection: close\r\n")) {
            try {
                client.write(buffer);
                Logger.debug(getClass(), "write response to client -&- close client socket", response);
                client.close();
            } catch (Exception e) {
                e.fillInStackTrace();
            }
            return false;
        }
        // SSH-2.0
        else if (response.startsWith("SSH-2.0")) {
            try {
                client.write(ByteBuffer.wrap("HTTP/1.0 200 connected\r\n\r\n".getBytes(StandardCharsets.UTF_8)));
                client.write(buffer);
            } catch (Exception e) {
                e.fillInStackTrace();
            }
            return true;
        } else {
//			handelProxyResponse();
        }

        return false;
    }


//	private void cleanProxyInputStream() {
//		Logger.debug(getClass(), "clean proxy input stream ");
//		byte[] temp = new byte[8 * 1024];
//		int bytes_read = 0;
//		try {
//			while( (bytes_read = proxyInput.read(temp)) != -1){
//				if(bytes_read == 0) {
//					Logger.debug( getClass(), "input stream of proxy had been cleand");
//					break;
//				}else {
//					String res = new String(temp, 0, bytes_read);
//					Logger.debug( getClass(), "proxy input stream output", res);
//					if(res.contains("200 Connect")) {
//						has200Ok = true;
//						
////						int index = res.indexOf("HTTP/1.1 200 Connected");
////						clientOutput.write(temp, index, bytes_read-index);
////						Logger.debug( getClass(), "write response to client ", new String(temp, index, bytes_read-index));
//						//proxyOutput.write(0);
//					}
//				}
//					
//				
//			}
//		} catch (IOException e) {
//			e.fillInStackTrace();
//		}
//	}

    protected void readResponseFromProxy() {
        Logger.debug(getClass(), "readResponseFromProxy()", "do nothing");
    }

    @Override
    void handelProxyResponse() {
        Logger.debug(getClass(), "handelProxyResponse()", "do nothing");
    }

}
