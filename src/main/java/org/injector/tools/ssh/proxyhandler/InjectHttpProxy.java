package org.injector.tools.ssh.proxyhandler;

import lombok.extern.slf4j.Slf4j;
import org.injector.tools.speed.NetworkMonitorSpeed;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class InjectHttpProxy extends DirectInjectProxy {

    public InjectHttpProxy(String proxyHost, int proxyPort, String payload) {
        super(proxyHost, proxyPort, payload);
    }

    public InjectHttpProxy(String proxyHost, int proxyPort, String payload, NetworkMonitorSpeed monitorSpeed) {
        super(proxyHost, proxyPort, payload, monitorSpeed);
    }

    @Override
    public Socket openConnection(String hostname, int port, int timeout) throws IOException {

        //setup proxy wrapper socket
        Socket proxy = new Socket(hostname, port);
        proxy.setSoTimeout(timeout);
        log.info("proxy wrapper socket state {}", (proxy.isClosed() ? "[closed]" : "[connected]"));

        //setup payload
        log.info("start connect to {}:{}", hostname, port);
        payload.setRequest("CONNECT " + hostname + ":" + port + " HTTP/1.0\r\n\r\n");
        log.info("CONNECT {}:{} HTTP/1.0\r\n\r\n", hostname, port);

//		proxy wrapper.getOutputStream().write(temp.getBytes());
//		proxy wrapper.getOutputStream().flush();

        String requestLinePayload = payload.getRawPayload();
        log.info("Payload format.");
        log.info(requestLinePayload);
        log.info("Start Write Payload Host.");
        writePayloadToProxy(proxy.getOutputStream(), requestLinePayload);

        // stat read response
        log.info("waiting read response ..... ");
        byte[] b = new byte[1024];
        int i = proxy.getInputStream().read(b);
        if (i <= 0) return null;
        log.info( new String(b, 0, i));

        log.info("additional response ..... ");
        i = proxy.getInputStream().read(b);
        if (i <= 0) {
            log.info("Error read data -- Direct Inject Method");
            return null;
        }
        log.info(new String(b, 0, i));
        log.info("end");

        return proxy;
    }


}
