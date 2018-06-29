package org.injector.tools.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.config.LocalProxyConfig;
import org.injector.tools.event.EventRunnableHandler;
import org.injector.tools.log.Logger;
import org.injector.tools.proxy.handler.ChannelSelector;
import org.injector.tools.proxy.handler.DirectCloseHandler;
import org.injector.tools.proxy.handler.DirectProxyHandler;
import org.injector.tools.proxy.handler.Http2Socks5Handler;
import org.injector.tools.proxy.handler.ProxyCloseHandler;
import org.injector.tools.proxy.handler.ProxyHandler;
import org.injector.tools.proxy.handler.TunnelProxyHandler;

/**
 * create Local Proxy with no configuration
 * you should call setLocalProxyConfig, setProxyConfig, and initLocalProxy
 * the local proxy will do no check for its type in the localProxyConfig
 * it will run, so you need to check it before create any new {@link LocalProxy}
 */

public class LocalProxy implements EventRunnableHandler {
	
	private ChannelSelector channelSelector;
	public void  initSelectorService(){
		try {
			channelSelector = new ChannelSelector();
		} catch (Exception e) {
			Logger.debug(e.getClass(), "Message ", e.getMessage());
		}
	}
	
	
	private LocalProxyConfig localProxyConfig;
	private HostProxyConfig hostProxyConfig;
	ServerSocketChannel localServer = null;

	/**
	 * create Local Proxy with no configuration
	 * you should call setLocalProxyConfig, setProxyConfig, and initLocalProxy
	 * the local proxy will do no check for its type in the localProxyConfig
	 * it will run, so you need to check it before create any new {@link LocalProxy}
	 */
	public LocalProxy() {}
	
	public LocalProxy(LocalProxyConfig localProxyConfig) {
		this(localProxyConfig,localProxyConfig.getHostProxyConfig());
	}
	public LocalProxy(LocalProxyConfig localProxyConfig, HostProxyConfig proxyConfig) {
		setLocalProxyConfig(localProxyConfig);
		setHostProxyConfig(proxyConfig);
		
		initSelectorService();
		
		initLocalProxy();
		checkProxyServer();
//		channelSelector.getService().execute(this::run);
//		run();
		registerLocalServerToSelector();
		channelSelector.startSelector();
	}


	public void initLocalProxy() {
		try {
			localServer = ServerSocketChannel.open();
			InetSocketAddress address = new InetSocketAddress(localProxyConfig.getLocalProxyPort());
			localServer.bind(address);
			Logger.debug(getClass(), "local proxy start lisiten to port (" + address.getPort() + ")");
		} catch (IOException e) {
			try {
				localServer = ServerSocketChannel.open();
				InetSocketAddress address = new InetSocketAddress(0);
				localServer.bind(address);
				localProxyConfig.setLocalProxyPort( address.getPort() );
				
//				System.err.println("Local Server Port is automatically allocated to " + this.localPort );
				Logger.debug(getClass(), "Local Server Port is automatically allocated to " + localProxyConfig.getLocalProxyPort());

			} catch (IOException e1) {
				Logger.debug(getClass(), "Can't initApp Local Server" );
				Logger.debug(e.getClass(), "Message ", e.getMessage());
				return;
			}
			
		}
		
		fireInitListener();
	}

	
	public void registerLocalServerToSelector() {
		try {
			localServer.configureBlocking(false);
			Logger.debug(getClass(), "Configure to Non-Blocking" );
			localServer.register(channelSelector.getSelector(), localServer.validOps(), this);
			Logger.debug(getClass(), "Local Server had been registered To Selector Channel" );
		} catch (IOException e) {
			Logger.debug(getClass(), "fail to configure Block local server" );
			Logger.debug(e.getClass(), "Message ", e.getMessage());
			return;
		}

	}

	
	public void run() {
		if(localServer == null) initLocalProxy();
		
		fireStartListener();
		try {
			while(true) {
				SocketChannel client = localServer.accept();
				Logger.debug(getClass(), "", client.getRemoteAddress());
				handle(client);
//				new TunnelProxyHandler(localServer.accept());
			}
		} catch (IOException e) {
			Logger.debug(getClass(), "local proxy server  error" );
			e.printStackTrace();
			
			fireErrorListener();
			fireStopListener();
		}
		Logger.debug(getClass(), "Proxy had stop!" );
		fireCompleteListener();
	}
		
	
	public void checkProxyServer() {
		if(!localProxyConfig.isAllowToRun()) {
			return;
		}
		if(hostProxyConfig.isDirect())
			return;
		
		HostChecker checker = new HostChecker();
		checker.init(hostProxyConfig.getProxyHost(), hostProxyConfig.getProxyPort(), 4500);
		checker.checkHost();
		
	}
	
	
	public void handle(SocketChannel client ) {
		ProxyHandler handler = null; 
		switch (hostProxyConfig.getProxyType()) {
			
			case HTTP:
			case HTTPS:
				Logger.debug(getClass() , "use handler : TunnelProxyHandler");
				handler = new TunnelProxyHandler(client,hostProxyConfig, channelSelector);

//				Logger.debug(getClass() , "use handler : AdvancedSplitHandler", channelSelector);
//				handler = new AdvancedSplitHandler(client,proxyConfig);
				
//				Logger.debug(getClass() , "use handler : SplitCleanerHandler", channelSelector);
//				handler = new SplitCleanerHandler(client,proxyConfig);
				break;
			case SOCKS4:
			case SOCKS5:
				Logger.debug(getClass() , "select Http2Socks5Handler as Handler");
				handler = new Http2Socks5Handler(client, hostProxyConfig, channelSelector);
				break;
			/*case STOP:
				Logger.debug(getClass() , "no Handler is defined in configuration file but 'STOP' is, ignore and close handling client request");
				try {
                    client.close();
                }catch (IOException e){
                    Logger.debug(getClass() , "error while closing client socket");
                }
				return;
			case TRANSPARENT:*/
			case DIRECT_CLOSE:	
				Logger.debug(getClass() , "select DirectCloseHandler as Handler");
				handler = new DirectCloseHandler(client, hostProxyConfig, channelSelector);
				break;
			case PROXY_CLOSE:
				Logger.debug(getClass() , "select ProxyCloseHandler as Handler");
				handler = new ProxyCloseHandler(client, hostProxyConfig, channelSelector);
				break;
			case DIRECT:
			default:	
				Logger.debug(getClass() , "select DirectProxyHandler as Handler");
				handler = new DirectProxyHandler(client, hostProxyConfig, channelSelector);
		}

//		handler.addErrorListener(stateEvent::fireErrorListener);
		
		handler.startHandler();
//		service.execute(handler::startHandler);
		
	}


    public void setConfig(LocalProxyConfig localProxyConfig, HostProxyConfig hostProxyConfig) {
        this.localProxyConfig = localProxyConfig;
        this.hostProxyConfig = hostProxyConfig;
    }

    public LocalProxyConfig getLocalProxyConfig() {
        return localProxyConfig;
    }
    public void setLocalProxyConfig(LocalProxyConfig localProxyConfig) {
        this.localProxyConfig = localProxyConfig;
    }
    public HostProxyConfig getHostProxyConfig() {
        return hostProxyConfig;
    }
    public void setHostProxyConfig(HostProxyConfig hostProxyConfig) {
        this.hostProxyConfig = hostProxyConfig;
    }


}
