package org.injector.tools.proxy;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.config.LocalProxyConfig;
import org.injector.tools.event.EventRunnableHandler;
import org.injector.tools.proxy.handler.ChannelSelector;
import org.injector.tools.proxy.handler.DirectCloseHandler;
import org.injector.tools.proxy.handler.DirectProxyHandler;
import org.injector.tools.proxy.handler.Http2Socks5Handler;
import org.injector.tools.proxy.handler.ProxyCloseHandler;
import org.injector.tools.proxy.handler.ProxyHandler;
import org.injector.tools.proxy.handler.SecureProxyHandler;
import org.injector.tools.proxy.handler.TunnelProxyHandler;

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

@Slf4j
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
//        channelSelector.startSelectorProcess();
    }

    public void initSelectorService() {
        try {
            channelSelector = new ChannelSelector(this);
        } catch (Exception e) {
            log.error("Error Message {}", e.getMessage());
        }
    }

    public void initLocalProxy() {
        try {
            localServer = ServerSocketChannel.open();
            InetSocketAddress address = new InetSocketAddress(localProxyConfig.getLocalProxyPort());
            localServer.bind(address);
            log.info("local proxy start listen on port ({})", address.getPort());
        } catch (IOException e) {
            try {
                localServer = ServerSocketChannel.open();
                InetSocketAddress address = new InetSocketAddress(0);
                localServer.bind(address);
                localProxyConfig.setLocalProxyPort(address.getPort());

//				System.err.println("Local Server Port is automatically allocated to " + this.localPort );
                log.info("Local Server Port is automatically allocated to {}", localProxyConfig.getLocalProxyPort());

            } catch (IOException e1) {
                log.info("Can't initApp Local Server");
                log.error("Error Message", e);
                return;
            }
        }

        fireInitListener();
    }

    public void registerLocalServerToSelector() {
        try {
            localServer.configureBlocking(false);
            log.info("Configure to Non-Blocking");
            localServer.register(channelSelector.getSelector(), localServer.validOps());
            log.info("Local Server had been registered To Selector Channel");
        } catch (IOException e) {
            log.info("fail to configure Block local server");
            log.error("Error Message: {}", e.getMessage());
        }
    }


    public void checkProxyServer() {
        if (!localProxyConfig.isAllowToRun() || hostProxyConfig.isDirect()) {
            return;
        }
        var checker = new HostChecker();
        checker.init(hostProxyConfig.getProxyHost(), hostProxyConfig.getProxyPort(), 4500);
        checker.checkHost();
    }


    public void handle(SocketChannel client) {
        ProxyHandler handler = switch (hostProxyConfig.getProxyType()) {
            case HTTP, HTTPS -> {
                log.info("use TunnelProxyHandler");
                yield new TunnelProxyHandler(client, hostProxyConfig, channelSelector);

//				Logger.debug(getClass() , "use AdvancedSplitHandler");
//				handler = new AdvancedSplitHandler(client,proxyConfig);

//				Logger.debug(getClass() , "use SplitCleanerHandler");
//				handler = new SplitCleanerHandler(client,proxyConfig);
            }
            case SOCKS -> {
                log.info("use Http2Socks5Handler");
                yield new Http2Socks5Handler(client, hostProxyConfig, channelSelector);
            }
            case SNI_HOST_NAME -> {
                log.info("use SNI_HOST_NAME: SecureProxyHandler");
                yield new SecureProxyHandler(client, hostProxyConfig, channelSelector);
            }
            /*case TRANSPARENT:*/
            case DIRECT_CLOSE -> {
                log.info("use DirectCloseHandler");
                yield new DirectCloseHandler(client, hostProxyConfig, channelSelector);
            }
            case PROXY_CLOSE -> {
                log.info("use ProxyCloseHandler");
                yield new ProxyCloseHandler(client, hostProxyConfig, channelSelector);
            }
            default -> {
                log.info("use DirectProxyHandler");
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
