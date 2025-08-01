package org.injector.tools.proxy.handler;

import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.log.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class SplitCleanerHandler extends TunnelProxyHandler {


    boolean has200Ok = false;

    public SplitCleanerHandler(SocketChannel clientSocket, HostProxyConfig proxyConfig, ChannelSelector channelSelector) {
        super(clientSocket, proxyConfig, channelSelector);
    }

    @Override
    protected void writePayloadToRemoteHost() {
        ArrayList<String> reqs = payload.getSplitwPayload();

        try {
            for (int i = 0; i < reqs.size(); i++) {
                String data = reqs.get(i);
                if (data.startsWith("[")) {
                    // split case her
                    // read response her

                    super.handelProxyResponse();

                } else {
                    Logger.debug(getClass(), "write raw payload part#" + i, reqs.get(i));
                    remote.write(ByteBuffer.wrap(reqs.get(i).getBytes()));
                }

//
//				// try to handle the clean input stream of the proxy
//				//cleanProxyInputStream();
//				Thread cleanThread = new Thread(this::cleanProxyInputStream);
//				cleanThread.start();
//				try {
//					cleanThread.join(200);
//				} catch (InterruptedException ignored) {}
//
//				if(has200Ok) {
//
////					Logger.debug(getClass(), "fire Success Listener");
//
//					Logger.debug(getClass(), "stop writing the rest of payload ");
//					Logger.debug(getClass(), "send 200 Connected to Client ");
//					clientOutput.write("HTTP/1.1 200 Connected\r\n\r\n".getBytes());
////					proxyOutput.write(0);
////					proxyOutput.write("SSH-2.0-".getBytes());
////					TimeUnit.SECONDS.sleep(1);
////					fireSuccessListener();
//			        transferDataFromProxyToClient();
//					transferDataFromClientToProxy();
//
//
//				}

            }
            super.handelProxyResponse();
            fireSuccessListener();
        } catch (Exception ignored) {}

    }

    public void cleanProxyInputStream() {
        Logger.debug(getClass(), "clean proxy input stream ");
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytes_read = 0;
        try {
            while ((bytes_read = remote.read(buffer)) != -1) {
                buffer.clear();
                if (bytes_read == 0) {
                    Logger.debug(getClass(), "input stream of proxy had been cleand");
                    break;
                } else {
                    String res = new String(buffer.array(), 0, bytes_read);
                    Logger.debug(getClass(), "proxy input stream output", res);
                    if (res.contains("200 Connect")) {
                        has200Ok = true;
                    }
                }


            }
        } catch (IOException ignored) {}
    }

    protected void readResponseFromProxy() {
        Logger.debug(getClass(), "readResponseFromProxy()", "do nothing");
    }

//	protected void transferDataFromClientToProxy() {
//		Logger.debug(getClass(), "Start transferDataFromClientToProxy");
//		new Thread(new Runnable() {
//
//			private void transferData() {
//				byte[] temp = new byte[8 * 1024];
//				int bytes_read = 0;
//				try {
//					while ((bytes_read = clientInput.read(temp)) != -1) {
//						String data = new String(temp, 0, bytes_read);
//						Logger.debug(getClass(), "debug client request", data);
////						if(data.startsWith("SSH-2.0-")) {continue;}
//						proxyOutput.write(temp, 0, bytes_read);
//						proxyOutput.flush();
//					}
//				} catch (IOException e) {
//					Logger.debug(getClass(), "read from client", e.getMessage());
//                    debugSockets(e);
//                    fireErrorListener();
//				}
//				
//			}
//			
//			@Override
//			public void run() {
//				transferData();
//			}}).start();
//	}
//	
//	@Override
//	void handelProxyResponse() {
//		Logger.debug( getClass(), "handelProxyResponse()", "do nothing");
//	}
}
