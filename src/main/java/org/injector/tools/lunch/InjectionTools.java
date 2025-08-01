package org.injector.tools.lunch;

import org.injector.tools.config.*;
import org.injector.tools.config.type.SSHProxyType;
import org.injector.tools.log.Logger;
import org.injector.tools.proxy.LocalProxy;
import org.injector.tools.ssh.jsch.JschSSHClient;
import org.injector.tools.ssh.trilead.SSHForwardClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InjectionTools {

	Config config;

    LocalProxy localProxy;
    SSHForwardClient ssh;
    JschSSHClient jschSSHClient;

    ExecutorService executor;

    public InjectionTools(Config config) {
        this.config = config;
        executor = Executors.newFixedThreadPool(6);
    }

    public void startLocalProxyService() {
        if (config.getLocalProxyConfig().isAllowToRun()) {
            Logger.debug(getClass(), "Local Proxy is allowed to start");
            localProxy = new LocalProxy(config.getLocalProxyConfig());

//			localProxy.initSelectorService();
//			localProxy.initLocalProxy();
//			localProxy.checkProxyServer();
//			localProxy.registerLocalServerToSelector();
//			localProxy.start();
//			executor.submit(localProxy);
        } else {
            Logger.debug(getClass(), "Local Proxy is not allowed to start");
        }

    }

    public void startLocalProxyService(LocalProxyConfig localProxyConfig) {


        if (config.getLocalProxyConfig().isAllowToRun()) {
            localProxy = new LocalProxy(localProxyConfig, config.getHostProxyConfig());
            localProxy.checkProxyServer();
//			localProxy.setDaemon(true);
//			localProxy.start();
//			executor.submit(localProxy);
        } else {
            Logger.debug(getClass(), "Local Proxy is not allowed to start");
        }
    }

    public void startJschSSHService() {
        startJschSSHService(config.getSshConfig());
    }

    private void startJschSSHService(SSHConfig config) {
        if (SSHProxyType.STOP.equals(config.getSshProxyType())) {
			return;
		}

        jschSSHClient = new JschSSHClient(config);
//		jschSSHClient.addSuccessListener(jschSSHClient.getMonitorSpeed()::start);
//		jschSSHClient.start();
        executor.submit(jschSSHClient.getMonitorSpeed()::start);
        executor.submit(jschSSHClient::connectHost);
//		jschSSHClient.addSuccessListener(()-> executor.submit(jschSSHClient.getMonitorSpeed()::start));
    }

    public void StartSSHService() {
        StartSSHService(config.getSshConfig());
    }

    public void StartSSHService(SSHConfig sshConfig) {
        ssh = new SSHForwardClient(sshConfig);
//		ssh.setDaemon(true);

        ssh.addSuccessListener(ssh.getNetworkMonitorSpeed()::start);
//		ssh.addErrorListener(ssh.getNetworkMonitorSpeed()::stop);

        ssh.addErrorListener(ssh::clearListeners);
//		ssh.addStopListener(ssh::reStart);

        ssh.start();
    }

}
