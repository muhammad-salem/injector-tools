package org.injector.tools.lunch;

import lombok.extern.slf4j.Slf4j;
import org.injector.tools.config.Config;
import org.injector.tools.config.LocalProxyConfig;
import org.injector.tools.config.SSHConfig;
import org.injector.tools.config.type.SSHProxyType;
import org.injector.tools.proxy.LocalProxy;
import org.injector.tools.ssh.jsch.JschSSHClient;
import org.injector.tools.ssh.trilead.SSHForwardClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Slf4j
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
            log.info("Local Proxy is allowed to start");
            localProxy = new LocalProxy(config.getLocalProxyConfig());

//			localProxy.initSelectorService();
//			localProxy.initLocalProxy();
//			localProxy.checkProxyServer();
//			localProxy.registerLocalServerToSelector();
//			localProxy.start();
//			executor.submit(localProxy);
        } else {
            log.info("Local Proxy is not allowed to start");
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
            log.info("Local Proxy is not allowed to start");
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
        Supplier<Boolean> keepRetry = (maxRetry.get() <= 0)
                ? () -> Boolean.TRUE
                : () -> maxRetry.get() > 0;
        executor.submit(() -> {
            log.info("ssh max retry is {}".formatted(maxRetry.get()));
            while (keepRetry.get()) {
                try {
                    jschSSHClient.connectHost();
                } catch (Exception e) {
                    log.info("connection failed");
                }
                if (maxRetry.addAndGet(-1) == 0) {
                    log.info("stop application (try count: {})".formatted(maxRetry.get()));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.exit(1);
                }
                log.info("try to connect... (try count: {})".formatted(maxRetry.get()));
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
