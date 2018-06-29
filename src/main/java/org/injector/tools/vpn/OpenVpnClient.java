package org.injector.tools.vpn;

import java.io.File;
import java.io.IOException;

import org.injector.tools.config.VPNConfig;
import org.injector.tools.event.EventHandler;
import org.openvpn.vpn.VPNClient.ConfigError;
import org.openvpn.vpn.VPNClient.CredsUnspecifiedError;

/**
 * 
 * Created by salem on 1/12/17.
 * @author salem
 */
public class OpenVpnClient implements EventHandler{

	private VPNConfig vpnConfig;
	private VPN vpnClient;
	public VPN getVpnClient() {
		return vpnClient;
	}

	public void setVpnClient(VPN vpnClient) {
		this.vpnClient = vpnClient;
	}

	public Thread getMainThread() {
		return mainThread;
	}

	public void setMainThread(Thread mainThread) {
		this.mainThread = mainThread;
	}

	Thread mainThread;
	/**
	 * use direct connection
	 * cant call fireSuccessListener, due to one thread implementation in openvpn client
	 * @param vpnConfig
	 */
	
	public OpenVpnClient(VPNConfig vpnConfig) {
		fireInitListener();
		this.setVpnConfig(vpnConfig);
		try {
			vpnClient = new VPN(new File(vpnConfig.getVpnConfigFile()),
			        vpnConfig.getVpnUser(),
					vpnConfig.getVpnPass(),
			        vpnConfig.getProxyHost(), vpnConfig.getProxyPort()+"");
		} catch (ConfigError | CredsUnspecifiedError | IOException e) {
			fireErrorListener();
			e.printStackTrace();
		}
		
	}
	
//    public void startService(VPNConfig Config){
//    	vpnConfig = Config;
//
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if(vpnConfig.isUseVPN())
//        startService("127.0.0.1", polipoConfig.getProxyPort()+"");
//    }


    public void initService(){
        mainThread = new Thread(new Runnable() 
        {
            @Override
            public void run() {

                // execute client session
                vpnClient.connect();

                // show stats before exit
                vpnClient.show_stats();
                fireCompleteListener();
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                vpnClient.stop();
                fireStopListener();
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                }
            }
        });

        mainThread.start();
        fireStartListener();
    }

	public VPNConfig getVpnConfig() {
		return vpnConfig;
	}

	public void setVpnConfig(VPNConfig vpnConfig) {
		this.vpnConfig = vpnConfig;
	}
}
