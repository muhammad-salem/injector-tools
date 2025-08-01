package org.injector.tools.lunch;

import org.injector.tools.config.*;
import org.injector.tools.config.type.SSHProxyType;
import org.injector.tools.log.Logger;
import org.injector.tools.proxy.LocalProxy;
import org.injector.tools.ssh.jsch.JschSSHClient;
import org.injector.tools.ssh.trilead.SSHForwardClient;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class InjectionTools {

    private final Config config;
    private final ExecutorService executor;

    private SSHForwardClient ssh;
    private LocalProxy localProxy;
    private JschSSHClient jschSSHClient;

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
        var maxRetry = new AtomicInteger(config.getMaxRetries());
        Supplier<Boolean> keepRetry =  (maxRetry.get() <= 0)
                ? () -> Boolean.TRUE
                : () -> maxRetry.get() > 0;
        executor.submit(() -> {
            Logger.debug(getClass(), "ssh max retry is %s".formatted(maxRetry.get()));
            while (keepRetry.get()) {
                try {
                    jschSSHClient.connectHost();
                } catch (Exception e) {
                    Logger.debug(getClass(),"connection failed");
                }
                if (maxRetry.addAndGet(-1) == 0) {
                    Logger.debug(getClass(), "stop application (try count: %s)".formatted(maxRetry.get()));
                    try {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.exit(1);
                }
                Logger.debug(getClass(), "try to connect... (try count: %s)".formatted(maxRetry.get()));
            }
        });
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
