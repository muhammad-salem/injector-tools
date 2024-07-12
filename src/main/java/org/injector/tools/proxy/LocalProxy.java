package org.injector.tools.proxy;

import lombok.Getter;
import lombok.Setter;
import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.config.LocalProxyConfig;
import org.injector.tools.event.EventRunnableHandler;
import org.injector.tools.log.Logger;
import org.injector.tools.proxy.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * create Local Proxy with no configuration
 * you should call setLocalProxyConfig, setProxyConfig, and initLocalProxy
 * the local proxy will do no check for its type in the localProxyConfig
 * it will run, so you need to check it before create any new {@link LocalProxy}
 */

public class LocalProxy implements EventRunnableHandler {

    private ServerSocketChannel localServer = null;
    private ChannelSelector channelSelector;
    @Setter
    @Getter
    private LocalProxyConfig localProxyConfig;
    @Setter
    @Getter
    private HostProxyConfig hostProxyConfig;
    /**
     * create Local Proxy with no configuration
     * you should call setLocalProxyConfig, setProxyConfig, and initLocalProxy
     * the local proxy will do no check for its type in the localProxyConfig
     * it will run, so you need to check it before create any new {@link LocalProxy}
     */
    public LocalProxy() {
    }

    public LocalProxy(LocalProxyConfig localProxyConfig) {
        this(localProxyConfig, localProxyConfig.getHostProxyConfig());
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

    public void initSelectorService() {
        try {
            channelSelector = new ChannelSelector();
        } catch (Exception e) {
            Logger.debug(e.getClass(), "Message ", e.getMessage());
        }
    }

    public void initLocalProxy() {
        try {
            localServer = ServerSocketChannel.open();
            InetSocketAddress address = new InetSocketAddress(localProxyConfig.getLocalProxyPort());
            localServer.bind(address);
            Logger.debug(getClass(), "local proxy start listen on port (" + address.getPort() + ")");
        } catch (IOException e) {
            try {
                localServer = ServerSocketChannel.open();
                InetSocketAddress address = new InetSocketAddress(0);
                localServer.bind(address);
                localProxyConfig.setLocalProxyPort(address.getPort());

//				System.err.println("Local Server Port is automatically allocated to " + this.localPort );
                Logger.debug(getClass(), "Local Server Port is automatically allocated to " + localProxyConfig.getLocalProxyPort());

            } catch (IOException e1) {
                Logger.debug(getClass(), "Can't initApp Local Server");
                Logger.debug(e.getClass(), "Message ", e.getMessage());
                return;
            }

        }

        fireInitListener();
    }


    public void registerLocalServerToSelector() {
        try {
            localServer.configureBlocking(false);
            Logger.debug(getClass(), "Configure to Non-Blocking");
            localServer.register(channelSelector.getSelector(), localServer.validOps(), this);
            Logger.debug(getClass(), "Local Server had been registered To Selector Channel");
        } catch (IOException e) {
            Logger.debug(getClass(), "fail to configure Block local server");
            Logger.debug(e.getClass(), "Message ", e.getMessage());
        }

    }


    public void run() {
        if (localServer == null) initLocalProxy();

        fireStartListener();
        try {
            while (true) {
                SocketChannel client = localServer.accept();
                Logger.debug(getClass(), "", client.getRemoteAddress());
                handle(client);
//				new TunnelProxyHandler(localServer.accept());
            }
        } catch (IOException e) {
            Logger.debug(getClass(), "local proxy server  error");
            e.fillInStackTrace();

            fireErrorListener();
            fireStopListener();
        }
        Logger.debug(getClass(), "Proxy had stop!");
        fireCompleteListener();
    }


    public void checkProxyServer() {
        if (!localProxyConfig.isAllowToRun() || hostProxyConfig.isDirect()){
            return;
        }
        var checker = new HostChecker();
        checker.init(hostProxyConfig.getProxyHost(), hostProxyConfig.getProxyPort(), 4500);
        checker.checkHost();
    }


    public void handle(SocketChannel client) {
        ProxyHandler handler = switch (hostProxyConfig.getProxyType()) {
            case HTTP, HTTPS -> {
                Logger.debug(getClass(), "use TunnelProxyHandler");
                yield new TunnelProxyHandler(client, hostProxyConfig, channelSelector);

//				Logger.debug(getClass() , "use AdvancedSplitHandler");
//				handler = new AdvancedSplitHandler(client,proxyConfig);

//				Logger.debug(getClass() , "use SplitCleanerHandler");
//				handler = new SplitCleanerHandler(client,proxyConfig);
            }
            case SOCKS -> {
                Logger.debug(getClass(), "use Http2Socks5Handler");
                yield new Http2Socks5Handler(client, hostProxyConfig, channelSelector);
            }
			/*case STOP:
				Logger.debug(getClass() , "no Handler is defined in configuration file but 'STOP' is, ignore and close handling client request");
				try {
                    client.close();
                }catch (IOException e){
                    Logger.debug(getClass() , "error while closing client socket");
                }
				return;*/
            case SNI_HOST_NAME -> {
                Logger.debug(getClass(), "use SniHostNameProxyHandler");
                yield new SniHostNameProxyHandler(client, hostProxyConfig, channelSelector);
            }
            /*case TRANSPARENT:*/
            case DIRECT_CLOSE -> {
                Logger.debug(getClass(), "use DirectCloseHandler");
                yield new DirectCloseHandler(client, hostProxyConfig, channelSelector);
            }
            case PROXY_CLOSE -> {
                Logger.debug(getClass(), "use ProxyCloseHandler");
                yield new ProxyCloseHandler(client, hostProxyConfig, channelSelector);
            }
            default -> {
                Logger.debug(getClass(), "use DirectProxyHandler");
                yield new DirectProxyHandler(client, hostProxyConfig, channelSelector);
            }
        };

//		handler.addErrorListener(stateEvent::fireErrorListener);

        handler.startHandler();
//		service.execute(handler::startHandler);

    }


    public void setConfig(LocalProxyConfig localProxyConfig, HostProxyConfig hostProxyConfig) {
        this.localProxyConfig = localProxyConfig;
        this.hostProxyConfig = hostProxyConfig;
    }


}
