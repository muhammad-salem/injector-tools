package org.injector.tools.ssh.jsch;

import java.io.IOException;
import java.net.UnknownHostException;

import org.injector.tools.config.SSHConfig;
import org.injector.tools.config.type.SSHProxyType;
import org.injector.tools.event.EventHandler;
import org.injector.tools.log.Logger;
import org.injector.tools.speed.NetworkMonitorSpeed;
import org.injector.tools.speed.TerminalNetworkMonitor;
import org.injector.tools.ssh.jsch.forwarding.HttpPortForwarding;
import org.injector.tools.ssh.jsch.forwarding.Socks5PortForwarding;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class JschSSHClient implements EventHandler{

	protected SSHConfig config;

	protected NetworkMonitorSpeed monitorSpeed;
	
	Session session;
	
	public JschSSHClient(SSHConfig config) {
		this(config, new TerminalNetworkMonitor());
	}
	public JschSSHClient(SSHConfig config, NetworkMonitorSpeed monitorSpeed) {
		this.config = config;
		this.monitorSpeed = monitorSpeed;
		//addSuccessListener(this::keepConnectionAlive);
	}
	
	
	
	public void setConfig(SSHConfig config) {
		this.config = config;
	}
	public void setMonitorSpeed(NetworkMonitorSpeed monitorSpeed) {
		this.monitorSpeed = monitorSpeed;
	}
	public SSHConfig getConfig() {
		return config;
	}
	public NetworkMonitorSpeed getMonitorSpeed() {
		return monitorSpeed;
	}
	
	Thread thread;
    public void start(){
	    if (thread == null){
	        thread = new Thread(this::connectHost, "jsch");
	        thread.start();
        }else if (thread.isAlive()){
	        try {
                thread.join(250);
                thread = new Thread(this::connectHost, "jsch");
                thread.start();
            }catch (InterruptedException e){
	            Logger.debug(getClass(), e.getMessage());
	            e.printStackTrace();
            }
        }else thread.start();
    }

	public void connectHost() {

		
		if(config.getSSHProxyType() == SSHProxyType.STOP) {
			Logger.debug(getClass(), "SSH Client not allow to run");
			return;
		}
		
		try {
			
			JSch.setLogger(new com.jcraft.jsch.Logger() {

				public void log(int level, String message) {
					Logger.debug(JschSSHClient.class, message);
				}

				public boolean isEnabled(int level) {
					return config.isDebuggable();
				}
			});

			JSch jsch = new JSch();
			Logger.debug(getClass(), "JSch VERSION " + JSch.VERSION);
			session = jsch.getSession(config.getUser(), config.getHost(), config.getPort());

			session.setProxy(config.getSSHProxyType().getProxy(config, monitorSpeed));
//			session.setProxy(HowToConnect.Direct.getProxy(config, monitorSpeed));
			
			if(config.isUseCompression()) {
				session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
				session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
				session.setConfig("compression_level", "9");
			}
			

			UserInfo ui = new SSHUserInfo(config.getPassword());
			session.setUserInfo(ui);
			
//			session.setTimeout(config.getKexTimeout());
//			session.setConfig("StrictHostKeyChecking", "no");
			
			
//			session.setServerAliveInterval(config.getTimeout());
			session.connect(/*config.getTimeout()*/);
			
			

			int open_sock5_port = config.getLocalSocksPort(), open_http_port = config.getLocalHttpPort();
			if(open_sock5_port > -1) {
				Socks5PortForwarding thread = new Socks5PortForwarding(session, open_sock5_port);
				thread.start();
				Logger.debug(getClass(), "start proxy thread : [ socks5://127.0.0.1:" + open_sock5_port + "/ ]");
			}
			
			if(open_http_port > -1) {
				HttpPortForwarding httpPortForwarding = new HttpPortForwarding(session, open_http_port);
				httpPortForwarding.start();
				Logger.debug(getClass(), "start proxy thread : [ http://127.0.0.1:" + open_http_port + "/ ]");
			}
			
			
			while (session.isConnected()) {
				try {
					session.sendKeepAliveMsg();
					Thread.sleep(2000);
				}  catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			//keepConnectionAlive();
			
			//monitorSpeed.start();
			
		} catch (JSchException | UnknownHostException e) {
			Logger.debug(getClass(), e.getClass().getTypeName(), e.getMessage());
			Logger.debug(getClass(), e.getClass().getSimpleName(), e.getCause().getMessage());
			e.printStackTrace();
		} catch (NullPointerException  | IOException  e) {
			Logger.debug(getClass(), e.getClass().getTypeName(), e.getMessage());
			Logger.debug(getClass(), e.getClass().getSimpleName(), e.getCause().getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			//e.printStackTrace();
			//Logger.debug(getClass(), "Exception1", e.getMessage());
			Logger.debug(getClass(), e.getClass().getSimpleName(), e.toString());
			Logger.debug(getClass(), "Exception2", e.getCause().getMessage());
			//e.printStackTrace();
		}
		
		fireSuccessListener();
		
	}
	
	void keepConnectionAlive() {
		while (session.isConnected()) {
			try {
				session.sendKeepAliveMsg();
				Thread.sleep(2000);
			}  catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
