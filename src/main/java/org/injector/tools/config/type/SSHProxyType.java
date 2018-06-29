package org.injector.tools.config.type;

import org.injector.tools.config.SSHConfig;
import org.injector.tools.speed.NetworkMonitorSpeed;
import org.injector.tools.ssh.proxyhandler.DirectInjectProxy;
import org.injector.tools.ssh.proxyhandler.DirectProxy;
import org.injector.tools.ssh.proxyhandler.HTTPProxy;
import org.injector.tools.ssh.proxyhandler.InjectHttpProxy;
import org.injector.tools.ssh.proxyhandler.ProxySocket;
import org.injector.tools.ssh.proxyhandler.Socks5Proxy;

public enum SSHProxyType {
	Auto,
	NOProxy,
	Direct,
	HTTPProxy,
	Socks5Proxy,
	DirectInject,
	InjectProxyHttp,
	STOP,
	Other;
	
	public ProxySocket getProxy(SSHConfig config, NetworkMonitorSpeed monitorSpeed) {
//		monitorSpeed = null;
		switch (this) {
			default:
			case NOProxy:
				// same as normal connection - use no proxy aka Direct connection.
			case Direct:
				return new DirectProxy(config.getProxyHost(), config.getProxyPort(), monitorSpeed);
			case Auto:
				// same as use local proxy for {now}
				// Suppose to {implement an extra code} 
				// to check configuration then chose the right way to connect , or tray them all till it.
			case HTTPProxy:
				// use proxy before connect to ssh host , defined with local proxy
				return new HTTPProxy(config.getProxyHost(), config.getProxyPort(), monitorSpeed);

			case DirectInject:
				// used as direct inject to ssh host
				return new DirectInjectProxy(config.getHost(), config.getPort(), config.getPayload(), monitorSpeed);
				
			case InjectProxyHttp:
				// used to inject to proxy host
				// chose between the proxy payload or direct payload option.
				return new InjectHttpProxy(config.getProxyHost(), config.getProxyPort(), config.getPayload(), monitorSpeed);
				
			case Socks5Proxy:
				// not implemented class yet // do nothing
				return new Socks5Proxy(config.getProxyHost(), config.getProxyPort(), monitorSpeed);
				
			case Other:
				// um handled case.
				return null;
		}
	}
	
	
}