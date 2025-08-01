package org.injector.tools.ssh.proxyhandler;

import org.injector.tools.log.Logger;
import org.injector.tools.payload.Payload;
import org.injector.tools.speed.NetworkMonitorSpeed;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class DirectInjectProxy extends ProxySocket {

    protected Payload payload;

    public DirectInjectProxy(String proxyHost, int proxyPort, String paylod) {
        this(proxyHost, proxyPort, paylod, null);
    }

    public DirectInjectProxy(String proxyHost, int proxyPort, String paylod, NetworkMonitorSpeed monitorSpeed) {
        super(proxyHost, proxyPort, monitorSpeed);
        this.payload = new Payload(paylod);
    }

    @Override
    public Socket openSocketConnection(String hostname, int port, int timeout) throws IOException {
        //setup proxywrapper socket
        Socket proxy = new Socket(hostname, port);
        proxy.setSoTimeout(timeout);
        Logger.debug(this.getClass(), "proxy socket state : " + (proxy.isClosed() ? "[closed]" : "[connected]"));

        //setup payload
        Logger.debug(this.getClass(), "start connect to " + hostname + ":" + port);
        payload.setRequest("CONNECT " + hostname + ":" + port + " HTTP/1.0\r\n\r\n");
        Logger.debug(this.getClass(), "CONNECT " + hostname + ":" + port + " HTTP/1.0\r\n\r\n");

//				proxywrapper.getOutputStream().write(temp.getBytes());
//				proxywrapper.getOutputStream().flush();

        String requestLinePayload = payload.getRawPayload();
        Logger.debug(this.getClass(), "Payload format", requestLinePayload);
        Logger.debug(this.getClass(), "Start Write Payload Host.");
        writePayloadToProxy(proxy.getOutputStream(), requestLinePayload);

        // stat read response
//				Logger.debug(getClass(), "wating read response ..... ");
//				byte[] b = new byte[1*1024];			
//				int i = proxy.getInputStream().read(b);
//				if(i <= 0) return null;
//				Logger.debug(getClass(), new String(b, 0, i));
//				
//				Logger.debug(getClass(), "additonal response ..... ");
//				i = proxy.getInputStream().read(b);
//				if(i <= 0) {
//					Logger.debug(getClass(), "Error read data -- Direct Inject Method");
//					return null;
//				}
//				Logger.debug(getClass(), new String(b, 0, i));
//				Logger.debug(getClass(), "end");

        return proxy;
    }

    protected void writePayloadToProxy(OutputStream proxyOutput, String requestLinePayload) {
        ArrayList<Integer> index = Payload.getSplitIndexes(requestLinePayload);
        try {
            if (index == null) {
                proxyOutput.write(requestLinePayload.getBytes());
                proxyOutput.flush();
            } else {
                for (int i = 0; i < index.size(); i += 2) {
                    proxyOutput.write(requestLinePayload.substring(index.get(i), index.get(i + 1)).getBytes());
                    proxyOutput.flush();
                    Logger.debug(getClass().getSimpleName(), requestLinePayload.substring(index.get(i), index.get(i + 1)));
                }

            }
        } catch (IOException ignored) {}

    }

}
