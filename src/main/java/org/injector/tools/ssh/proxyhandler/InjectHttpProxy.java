package org.injector.tools.ssh.proxyhandler;

import java.io.IOException;
import java.net.Socket;

import org.injector.tools.log.Logger;
import org.injector.tools.speed.NetworkMonitorSpeed;

public class InjectHttpProxy extends DirectInjectProxy {

	public InjectHttpProxy(String proxyHost, int proxyPort, String paylod) {
		super(proxyHost, proxyPort, paylod);
		// TODO Auto-generated constructor stub
	}

	public InjectHttpProxy(String proxyHost, int proxyPort, String paylod, NetworkMonitorSpeed monitorSpeed) {
		super(proxyHost, proxyPort, paylod, monitorSpeed);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Socket openConnection(String hostname, int port, int timeout) throws IOException {
		
		//setup proxywrapper socket
		Socket proxy = new Socket(hostname, port);
		proxy.setSoTimeout(timeout);
		Logger.debug(getClass(), "proxywrapper socket state", (proxy.isClosed()?"[closed]":"[connected]"));

		//setup payload
		Logger.debug(getClass(), "start connect to %s:%s ", hostname, port);
		payload.setRequest("CONNECT "+hostname+":"+port+" HTTP/1.0\r\n\r\n");
		Logger.debug(getClass(), "CONNECT "+hostname+":"+port+" HTTP/1.0\r\n\r\n");
		
//		proxywrapper.getOutputStream().write(temp.getBytes());
//		proxywrapper.getOutputStream().flush();
		
		String requestLinePayload = payload.getRawPayload();
		Logger.debug(getClass(), "Payload formate.");
		Logger.debug(getClass(), requestLinePayload);
		Logger.debug(getClass(), "Start Write Payload Host.");
		writePayloadToProxy(proxy.getOutputStream(), requestLinePayload);
		
		// stat read response 
		Logger.debug(getClass(), "wating read response ..... ");
		byte[] b = new byte[1*1024];			
		int i = proxy.getInputStream().read(b);
		if(i <= 0) return null;
		Logger.debug(getClass(), new String(b, 0, i));
		
		Logger.debug(getClass(), "additonal response ..... ");
		i = proxy.getInputStream().read(b);
		if(i <= 0) {
			Logger.debug(getClass(), "Error read data -- Direct Inject Method");
			return null;
		}
		Logger.debug(getClass(), new String(b, 0, i));
		Logger.debug(getClass(), "end");
		
		return proxy;
	}
	

}
