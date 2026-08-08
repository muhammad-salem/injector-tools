package org.injector.tools.ssh.proxyhandler;

import lombok.extern.slf4j.Slf4j;
import org.injector.tools.payload.Payload;
import org.injector.tools.speed.NetworkMonitorSpeed;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

@Slf4j
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
        log.info("proxy socket state : {}", proxy.isClosed() ? "[closed]" : "[connected]");

        //setup payload
        log.info("start connect to {}:{}", hostname, port);
        payload.setRequest("CONNECT " + hostname + ":" + port + " HTTP/1.0\r\n\r\n");
        log.info("CONNECT {}:{} HTTP/1.0\r\n\r\n", hostname, port);

//				proxywrapper.getOutputStream().write(temp.getBytes());
//				proxywrapper.getOutputStream().flush();

        String requestLinePayload = payload.getRawPayload();
        log.info("Payload format: {}", requestLinePayload);
        log.info("Start Write Payload Host.");
        writePayloadToProxy(proxy.getOutputStream(), requestLinePayload);

        // stat read response
//				log.info("wating read response ..... ");
//				byte[] b = new byte[1*1024];			
//				int i = proxy.getInputStream().read(b);
//				if(i <= 0) return null;
//				log.info(new String(b, 0, i));
//				
//				log.info("additonal response ..... ");
//				i = proxy.getInputStream().read(b);
//				if(i <= 0) {
//					log.info("Error read data -- Direct Inject Method");
//					return null;
//				}
//				log.info(new String(b, 0, i));
//				log.info("end");

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
                    log.info(requestLinePayload.substring(index.get(i), index.get(i + 1)));
                }

            }
        } catch (IOException ignored) {
        }

    }

}
