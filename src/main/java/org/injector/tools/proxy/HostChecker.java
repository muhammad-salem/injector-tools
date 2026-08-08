package org.injector.tools.proxy;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
@Getter
@Setter
public class HostChecker implements HostCheck {

    protected String host;
    protected int port;
    protected int timeOut = 4500;
    boolean result;

    @Override
    public void check() {
        log.info("check proxy server ...  ..   .");
        try {
            Socket proxy = new Socket();
//			Socket proxys = new Socket(proxyConfig.getProxyHost(), proxyConfig.getProxyPort());
//			proxy.setSoTimeout(4500);
//			proxy.setTcpNoDelay(true);
            proxy.connect(new InetSocketAddress(getHost(), getPort()), timeOut);
            proxy.isConnected();
            if (!proxy.isClosed())
                log.info("Host: {}:{} is alive", getHost(), getPort());
            proxy.close();
            setResult(true);
        } catch (IOException e) {

            log.info("Can't connect to ({}:{})", getHost(), getPort());
            log.info(e.getMessage());
            setResult(false);
        } catch (Exception e) {
            setResult(false);
        }
    }

    @Override
    public boolean isAlive() {
        return result;
    }

    @Override
    public void setResult(boolean isAlive) {
        this.result = isAlive;
    }

    public void init(String host, int port, int timeout) {
        setHost(host);
        setPort(port);
        setTimeOut(timeout);
    }

}
