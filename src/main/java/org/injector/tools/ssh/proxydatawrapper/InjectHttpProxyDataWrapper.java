package org.injector.tools.ssh.proxydatawrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import org.injector.tools.log.Logger;
import org.injector.tools.payload.Payload;
import org.injector.tools.speed.TerminalNetworkMonitor;

/**
 * still in development
 * can be use to direct inject or remote inject
 * 
 * this class used for direct injection for 
 * <br>	- SSH connect message 
 * <br> - SSH-2.0- command itself if you want
 * @author salem
 *
 */
public class InjectHttpProxyDataWrapper extends ProxyDataWrapper  {

	Socket proxy;
	Payload payload ;
	public InjectHttpProxyDataWrapper(String proxyHost, int proxyPort) {
		super(proxyHost, proxyPort, null, null);
	}
	public InjectHttpProxyDataWrapper(String proxyHost, int proxyPort, TerminalNetworkMonitor nm) {
		super(proxyHost, proxyPort, null, nm);
	}
	
	public InjectHttpProxyDataWrapper(String proxyHost, int proxyPort, String pyld, TerminalNetworkMonitor nm) {
		super(proxyHost, proxyPort, null, nm);
		 payload = new Payload(pyld);
	}
	
	public InjectHttpProxyDataWrapper(String proxyHost, int proxyPort, String[] requestHeaderLines,
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
		ArrayList<Integer> index = Payload.getSplitIndexs(requestLinePayload);
		try {
			if(index == null){
				proxyOutput.write(requestLinePayload.getBytes());
				proxyOutput.flush();
			}else{
				for (int i = 0; i < index.size(); i+=2) {
					proxyOutput.write(requestLinePayload.substring( index.get(i), index.get(i+1)).getBytes());
					proxyOutput.flush();
					Logger.debug(getClass().getSimpleName(), requestLinePayload.substring( index.get(i), index.get(i+1)));
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	

	@Override
	public Socket openSoccketConnection(String hostname, int port, int connectTimeout) throws IOException {
		
		//setup proxywrapper socket
		proxy = new Socket(hostname, port);
		proxy.setSoTimeout(connectTimeout);
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
