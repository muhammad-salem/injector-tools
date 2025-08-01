package org.injector.tools.ssh.jsch;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import lombok.Getter;
import lombok.Setter;
import org.injector.tools.config.SSHConfig;
import org.injector.tools.config.type.SSHProxyType;
import org.injector.tools.event.EventHandler;
import org.injector.tools.log.Logger;
import org.injector.tools.speed.NetworkMonitorSpeed;
import org.injector.tools.speed.TerminalNetworkMonitor;
import org.injector.tools.ssh.jsch.forwarding.HttpPortForwarding;
import org.injector.tools.ssh.jsch.forwarding.Socks5PortForwarding;

import java.io.IOException;

public class JschSSHClient implements EventHandler {

    @Setter
    @Getter
    protected SSHConfig config;

    @Setter
    @Getter
    protected NetworkMonitorSpeed monitorSpeed;


    public JschSSHClient(SSHConfig config) {
        this(config, new TerminalNetworkMonitor());
    }


    public JschSSHClient(SSHConfig config, NetworkMonitorSpeed monitorSpeed) {
        this.config = config;
        this.monitorSpeed = monitorSpeed;
        //addSuccessListener(this::keepConnectionAlive);
    }

    public void connectHost() {
        if (SSHProxyType.STOP.equals(config.getSshProxyType())) {
            Logger.debug(getClass(), "SSH Client not allow to run");
            return;
        }
        try {
            JSch.setLogger(new com.jcraft.jsch.Logger() {
                public void log(int level, String message) {
                    Logger.debug(JschSSHClient.class, message);
                }
                public boolean isEnabled(int level) {
                    return true;
                }
            });

            JSch jsch = new JSch();
            Logger.debug(getClass(), "JSch VERSION " + JSch.VERSION);
            Session session = jsch.getSession(config.getUser(), config.getHost(), config.getPort());

            session.setProxy(config.getSshProxyType().getProxy(config, monitorSpeed));
//			session.setProxy(HowToConnect.Direct.getProxy(config, monitorSpeed));

            if (config.getUseCompression()) {
                session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
                session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
                session.setConfig("compression_level", "9");
            }
            if (config.getSkipCheckHostKey()){
                session.setConfig("StrictHostKeyChecking", "no");
            }

            UserInfo ui = new SSHUserInfo(config.getPassword());
            session.setUserInfo(ui);

//			session.setTimeout(config.getKexTimeout());
//			session.setConfig("StrictHostKeyChecking", "no");

//			session.setServerAliveInterval(config.getTimeout());
            session.connect(/*config.getTimeout()*/);


            int open_sock5_port = config.getLocalSocksPort(), open_http_port = config.getLocalHttpPort();
            if (open_sock5_port > -1) {
                Socks5PortForwarding thread = new Socks5PortForwarding(session, open_sock5_port);
                thread.start();
                Logger.debug(getClass(), "start proxy thread : [ socks5://127.0.0.1:" + open_sock5_port + "/ ]");
            }

            if (open_http_port > -1) {
                HttpPortForwarding httpPortForwarding = new HttpPortForwarding(session, open_http_port);
                httpPortForwarding.start();
                Logger.debug(getClass(), "start proxy thread : [ http://127.0.0.1:" + open_http_port + "/ ]");
            }

            while (session.isConnected()) {
                session.sendKeepAliveMsg();
                Thread.sleep(2000);
            }
        } catch (JSchException | NullPointerException | IOException e) {
            Logger.debug(getClass(), e.getClass().getTypeName(), e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            if (e instanceof InterruptedException){
                Thread.currentThread().interrupt();
            }
            Logger.debug(getClass(), e.getClass().getSimpleName(), e.toString());
            Logger.debug(getClass(), "Exception2", e.getCause().getMessage());
            throw new RuntimeException(e);
        }

        fireSuccessListener();
    }

}
