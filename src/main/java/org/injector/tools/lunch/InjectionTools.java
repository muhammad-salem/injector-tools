package org.injector.tools.lunch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.injector.tools.config.Config;
import org.injector.tools.config.LocalProxyConfig;
import org.injector.tools.config.PolipoConfig;
import org.injector.tools.config.RedSocksConfig;
import org.injector.tools.config.SSHConfig;
import org.injector.tools.config.VPNConfig;
import org.injector.tools.config.type.SSHProxyType;
import org.injector.tools.iptables.RedSocks;
import org.injector.tools.log.Logger;
import org.injector.tools.polipo.Polipo;
import org.injector.tools.proxy.LocalProxy;
import org.injector.tools.ssh.jsch.JschSSHClient;
import org.injector.tools.ssh.trilead.SSHForwardClient;
import org.injector.tools.utils.R;
import org.injector.tools.vpn.OpenVpnClient;

public class InjectionTools {
	Config config;
	
	
	LocalProxy localProxy;
	SSHForwardClient ssh;
	JschSSHClient jschSSHClient;
	Polipo polipo;
	OpenVpnClient vpnClient;
	
	ExecutorService executor;
	
	public InjectionTools(Config config) {
		this.config = config;
		executor = Executors.newFixedThreadPool(6);
	}
	
	public void StartLocalProxyService() {
		if(config.getLocalProxyConfig().isAllowToRun()) {
			Logger.debug(getClass(), "Local Proxy is allowed to start");
			localProxy = new LocalProxy(config.getLocalProxyConfig());
			
//			localProxy.initSelectorService();
//			localProxy.initLocalProxy();
//			localProxy.checkProxyServer();
//			localProxy.registerLocalServerToSelector();
//			localProxy.start();
//			executor.submit(localProxy);
		}else {
			Logger.debug(getClass(), "Local Proxy is not allowed to start");
		}
		
	}
	
	public void StartLocalProxyService(LocalProxyConfig localProxyConfig) {
		
		
		if(config.getLocalProxyConfig().isAllowToRun()) {
			localProxy = new LocalProxy(localProxyConfig, config.getHostProxyConfig());
			localProxy.checkProxyServer();
//			localProxy.setDaemon(true);
//			localProxy.start();
//			executor.submit(localProxy);
		}else {
			Logger.debug(getClass(), "Local Proxy is not allowed to start");
		}
	}

	public void StartJschSSHService() { StartJschSSHService(config.getSshConfig()); }

    private void StartJschSSHService(SSHConfig config) {
    	if(config.getSSHProxyType() == SSHProxyType.STOP) return;
    	
		jschSSHClient = new JschSSHClient(config);
//		jschSSHClient.addSuccessListener(jschSSHClient.getMonitorSpeed()::start);
//		jschSSHClient.start();
//		executor.submit(jschSSHClient.getMonitorSpeed()::start);
		executor.submit(jschSSHClient::connectHost);
		jschSSHClient.addSuccessListener(()-> executor.submit(jschSSHClient.getMonitorSpeed()::start));
		
	}

	public void StartSSHService() { StartSSHService(config.getSshConfig()); }
	public void StartSSHService(SSHConfig sshConfig) {
		ssh = new SSHForwardClient(sshConfig);
//		ssh.setDaemon(true);



        if (config.isUseIPtables()){
            ssh.addSuccessListener(this::StartRedSocksService);
            ssh.addStopListener(this::destroyRedSocksService);
        }

		ssh.addSuccessListener(ssh.getNetworkMonitorSpeed()::start);
//		ssh.addErrorListener(ssh.getNetworkMonitorSpeed()::stop);

		ssh.addErrorListener(ssh::clearListeners);

//		ssh.addStopListener(ssh::reStart);
		
		ssh.start();
	}
	
	

	
	public void StartPolipoService() {
		StartPolipoService(config.getPolipoConfig());
	}
	public void StartPolipoService(PolipoConfig polipoConfig) {
        if (!polipoConfig.isUsePolipo()) return;
		polipoConfig.setDiskCacheRoot(R.PolipoCache);
		polipoConfig.setPolipoConfigFile(R.PolipoConfigFile);
        if (polipo != null) return;
		polipo = new Polipo(polipoConfig);
		polipo.start();

        if (polipo.isAlive() )Logger.debug(polipo.getClass(), "Run Polipo, HTTP Proxy Server (127.0.0.1:"+polipoConfig.getProxyPort()+")");
		Runtime.getRuntime().addShutdownHook(new Thread(this::destroyPolipoService));
	}
	
	public void destroyPolipoService() {
		if(polipo==null)return;
		if (polipo.isAlive()){
            polipo.destroy();
            System.out.println("\nPolipo has stoped!");
        }
        Logger.debug(polipo.getClass(), "Polipo is stoped!.\n");
	}



    RedSocks redSocks;
    public void StartRedSocksService() {
        if (!config.isUseIPtables()){
            return;
        }
        RedSocksConfig redSocksConfig = new RedSocksConfig();
        redSocksConfig.setDirectory(R.ConfigPath+R.separator+"redsocks");
        redSocksConfig.setState(RedSocksConfig.RedSocksState.start);
        redSocksConfig.setProxyType(RedSocksConfig.RedSocksType.socks5);
        redSocksConfig.setProxyHost("127.0.0.1");
        redSocksConfig.setProxyPort(config.getSshConfig().getLocalSocksPort());
        redSocksConfig.setUseAuth(false);

        redSocks = new RedSocks(redSocksConfig);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                destroyRedSocksService();
            }
        }));


    }

    public void destroyRedSocksService() {
        if (!config.isUseIPtables()){
            return;
        }
        if (redSocks != null)
            if (redSocks.getConfig() != null)
                if (redSocks.getConfig().getState() == RedSocksConfig.RedSocksState.start)
                    redSocks.stop();
    }
    
    public void StartVPNService() {StartVPNService(config.getVpnConfig());}
    public void StartVPNService(VPNConfig vpnConfig) {
    	if (!vpnConfig.isUseVPN()) {
    		Logger.debug(vpnClient.getClass(), "Not Allowed to start vpn Service");
			return;
		}
		vpnClient = new OpenVpnClient(vpnConfig);
		vpnClient.initService();
	}
    
    public void destroyVPNService() {
    	try {
			vpnClient.getMainThread().join(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
}
