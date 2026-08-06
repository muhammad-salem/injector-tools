package org.injector.tools.ssh.proxydatawrapper;

import lombok.extern.slf4j.Slf4j;
import org.injector.tools.payload.Payload;
import org.injector.tools.speed.TerminalNetworkMonitor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * still in development
 * can be used to direct inject or remote inject
 * <p>
 * this class used for direct injection for
 * <br>	- SSH connect message
 * <br> - SSH-2.0- command itself if you want
 *
 * @author salem
 */
@Slf4j
public class DirectInjectDataWrapper extends ProxyDataWrapper {

    Socket proxy;
    Payload payload;

    public DirectInjectDataWrapper(String proxyHost, int proxyPort) {
        super(proxyHost, proxyPort, null, null);
    }

    public DirectInjectDataWrapper(String proxyHost, int proxyPort, TerminalNetworkMonitor nm) {
        super(proxyHost, proxyPort, null, nm);
    }


    public DirectInjectDataWrapper(String proxyHost, int proxyPort, String pyld, TerminalNetworkMonitor nm) {
        super(proxyHost, proxyPort, null, nm);
        payload = new Payload(pyld);
    }

    public DirectInjectDataWrapper(String proxyHost, int proxyPort, String[] requestHeaderLines,
                                   TerminalNetworkMonitor nm) {
        super(proxyHost, proxyPort, requestHeaderLines, nm);
    }

    public Socket getProxySocket() {
        return proxy;
    }

    public void setProxySocket(Socket proxy) {
        this.proxy = proxy;
    }

    private void writePayloadToProxy(OutputStream proxyOutput, String requestLinePayload) {
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


    @Override
    public Socket openSocketConnection(String hostname, int port, int connectTimeout) throws IOException {

        //setup proxy wrapper socket
        proxy = new Socket(hostname, port);
        proxy.setSoTimeout(connectTimeout);
        log.info("proxy socket state: [{}]", proxy.isClosed() ? "closed" : "connected");

        //setup payload
        log.info("start connect to {}:{}", hostname, port);
        payload.setRequest("CONNECT " + hostname + ":" + port + " HTTP/1.0\r\n\r\n");
        log.info("CONNECT {}:{} HTTP/1.0\r\n\r\n", hostname, port);

//		proxywrapper.getOutputStream().write(temp.getBytes());
//		proxywrapper.getOutputStream().flush();

        String requestLinePayload = payload.getRawPayload();
        log.info("Payload format: {}", requestLinePayload);
        log.info("Start Write Payload Host.");
        writePayloadToProxy(proxy.getOutputStream(), requestLinePayload);

        // stat read response
//		log.info( "waiting read response ..... ");
//		byte[] b = new byte[1*1024];			
//		int i = proxy.getInputStream().read(b);
//		if(i <= 0) return null;
//		log.info( new String(b, 0, i));
//		
//		log.info( "additional response ..... ");
//		i = proxy.getInputStream().read(b);
//		if(i <= 0) {
//			log.info( "Error read data -- Direct Inject Method");
//			return null;
//		}
//		log.info( new String(b, 0, i));
//		log.info( "end");

        return proxy;
    }


}
