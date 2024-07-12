package org.injector.tools.proxy;

import lombok.Getter;
import lombok.Setter;
import org.injector.tools.log.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Getter
@Setter
public class HostChecker implements HostCheck {

    protected String host;
    protected int port;
    protected int timeOut = 4500;
    boolean result;

    @Override
    public void check() {
        Logger.debug(getClass(), "check proxy server ...  ..   .");
        try {
            Socket proxy = new Socket();
//			Socket proxys = new Socket(proxyConfig.getProxyHost(), proxyConfig.getProxyPort());
//			proxy.setSoTimeout(4500);
//			proxy.setTcpNoDelay(true);
            proxy.connect(new InetSocketAddress(getHost(), getPort()), timeOut);
            proxy.isConnected();
            if (!proxy.isClosed())
                Logger.debug(getClass(), "Host: " + getHost() + ":" + getPort() + " is alive");
            proxy.close();
            setResult(true);
        } catch (IOException e) {

            Logger.debug(getClass(), "Can't connect to (" + getHost() + ":" + getPort() + ")");
            Logger.debug(getClass(), e.getMessage());
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
