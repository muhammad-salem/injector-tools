package org.injector.tools.ssh.trilead;

import com.trilead.ssh2.*;
import lombok.Getter;
import lombok.Setter;
import org.injector.tools.config.SSHConfig;
import org.injector.tools.config.type.SSHProxyType;
import org.injector.tools.event.EventHandler;
import org.injector.tools.log.Logger;
import org.injector.tools.speed.TerminalNetworkMonitor;
import org.injector.tools.ssh.proxyhandler.DirectProxy;
import org.injector.tools.ssh.proxyhandler.ProxySocket;
import org.terminal.ansi.Ansi;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.SecureRandom;

public class SSHForwardClient implements EventHandler {


    /*******************************************************/

    protected SSHConfig sshConfig;

    protected ProxySocket proxyData;
    protected TerminalNetworkMonitor monitorSpeed;

    /**
     * -- GETTER --
     *
     *
     * -- SETTER --
     *
     @return the connection
      * @param connection the connection to set
     */
    @Setter
    @Getter
    protected Connection connection;
    /**
     * -- SETTER --
     *
     *
     * -- GETTER --
     *
     @param portForwarder the portForwarder to set
      * @return the portForwarder
     */
    @Getter
    @Setter
    protected DynamicPortForwarder dynamicPortForwarder;
    Thread thread;

    public SSHForwardClient(SSHConfig sshConfig) {
        this(sshConfig, new TerminalNetworkMonitor());
    }


    public SSHForwardClient(SSHConfig sshConfig, TerminalNetworkMonitor monitorSpeed) {
        this.sshConfig = sshConfig;
        this.monitorSpeed = monitorSpeed;
        initSSHClient();
    }

    public SSHForwardClient(
            String remoteHost,
            int remotePort,
            String remoteUser,
            String remotePassword,
            String proxyHost,
            int proxyPort,
            int localSocksPort,
            boolean useCompression,
            ProxySocket proxyData,
            SSHProxyType howToConnect,
            String sniHost,
            TerminalNetworkMonitor monitorSpeed) {
        this.proxyData = proxyData;
        this.monitorSpeed = monitorSpeed;
        sshConfig = new SSHConfig(
                remoteHost, remotePort,
                remoteUser, remotePassword,
                proxyHost, proxyPort,
                localSocksPort, howToConnect,
                useCompression, sniHost);
        initSSHClient();
    }

    public static String[] getAvailableCiphers() {
        return Connection.getAvailableCiphers();
    }

//	private void initProxyData() {
//		if(proxyData == null) {
//			switch (sshConfig.getHowToConnect()) {
//
//				case NOProxy:
//					// same as normal connection - use no proxy aka Direct connection.
//				case Direct:
//					//use direct connection to host to inject
//					proxyData = new DirectDataWrapper(sshConfig.getSshHost(), sshConfig.getSshPort(), monitorSpeed);
//					break;
//				case Auto:
//					// same as use local proxy for {now}
//					// Suppose to {implement an extra code} 
//					// to check configuration then chose the right way to connect , or tray them all till it.
//				case HTTPProxy:
//					// use proxy before connect to ssh host , defined with local proxy
//					proxyData = new HTTPProxyDataWrapper(sshConfig.getProxyHost(), sshConfig.getProxyPort(), monitorSpeed);
//					break;
//				case DirectInject:
//					// used as direct inject to ssh host
//					proxyData = new DirectInjectDataWrapper(sshConfig.getSshHost(), sshConfig.getSshPort(), sshConfig.getDirectPayload(), monitorSpeed);
//					break;
//				case InjectProxyHttp:
//					// used to inject to proxy host
//					// chose between the proxy payload or direct payload option.
//					proxyData = new InjectHttpProxyDataWrapper(sshConfig.getProxyHost(), sshConfig.getProxyPort(), sshConfig.getDirectPayload(), monitorSpeed);
//					break;
//				case Socks5Proxy:
//					// not implemented class yet // do nothing
//					proxyData = new Socks5ProxyDataWrapper(sshConfig.getProxyHost(), sshConfig.getProxyPort(), monitorSpeed);
//					break;
//				case Other:
//					// um handled case.
//					break;
//				default:
//					// this case as it should be proxy wrapper data had been set before
//					proxyData = getProxyDataWrapper();
//			}
//		}
//		
//	}

    public static String[] getAvailableMACs() {
        return Connection.getAvailableMACs();
    }

    public static String[] getAvailableServerHostKeyAlgorithms() {
        return Connection.getAvailableServerHostKeyAlgorithms();
    }

    /*****************************************************/


    public void reStart() {
        initSSHClient();
        System.gc();
        start();
    }

    public void initSSHClient() {

        connection = new Connection(sshConfig.getHost(), sshConfig.getPort());
        if (monitorSpeed == null) monitorSpeed = new TerminalNetworkMonitor();

//		initProxyData();
        proxyData = sshConfig.getSshProxyType().getProxy(sshConfig, monitorSpeed);
        connection.setProxyData(proxyData);


//		connection.addConnectionMonitor(new ConnectionMonitor() {
//			@Override
//			public void connectionLost(Throwable reason) {
//				connectionMonitorLost(reason);
//			}
//		});


        connection.addConnectionMonitor(this::connectionMonitorLost);

        connection.enableDebugging(true, (level, className, message) -> Logger.debug(className, message));


        /**** add internal listener ****/

        //


        //  restart thread
//        addStopListener(this::closeDynamicPortForwarder);
//		  addErrorListener(connection::close);

    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this::run, "ssh");
            thread.start();
        } else if (thread.isAlive()) {
            try {
                thread.join(250);
                thread = new Thread(this::reStart, "ssh");
                thread.start();
            } catch (InterruptedException e) {
                Logger.debug(getClass(), e.getMessage());
                e.fillInStackTrace();
            }
        } else thread.start();
    }

    public void stop() {
        if (thread == null) {
        } else if (thread.isAlive()) {
            try {
                thread.join(250);
            } catch (InterruptedException e) {
                Logger.debug(getClass(), e.getMessage());
                e.fillInStackTrace();
            }
        }
    }

    public void run() {

        fireStartListener();

        try {
            if (sshConfig.getUseCompression()) {
                Logger.debug(getClass(), "Enable Compression.");
            }
            connection.setCompression(sshConfig.getUseCompression());
            Logger.debug(getClass(), "Start connect.");

            /*ServerHostKeyVerifier v = new ServerHostKeyVerifier() {
                @Override
                public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm, byte[] serverHostKey) throws Exception {
                    return false;
                }
            } ;*/

            ConnectionInfo info = connection.connect(); //null, 0, sshConfig.getKexTimeout());

            Logger.debug(getClass(), "Connected to host " + connection.getHostname());
            Logger.debug(getClass(), "Key Exchange Counter:\t" + info.keyExchangeCounter);
            Logger.debug(getClass(), "Algorithm:\t" + info.serverToClientMACAlgorithm
                    + "\t " + info.clientToServerCryptoAlgorithm);
            Logger.debug(getClass(), "keyExchangeAlgorithm:\t" + info.keyExchangeAlgorithm);

            boolean passOK = connection.authenticateWithPassword(sshConfig.getUser(), sshConfig.getPassword());
            if (passOK) {
                Logger.debug(getClass(), "Finish authenticate With Password.");
            } else {
                Logger.debug(getClass(), "Connection is " + Ansi.Red + " not authenticated." + Ansi.Reset);
                fireErrorListener();
                fireCompleteListener();
                return;
            }
            Logger.debug(getClass(), "Start Dynamic forward ...  ..   .");

            dynamicPortForwarder = connection.createDynamicPortForwarder(sshConfig.getLocalSocksPort());
            Logger.debug(getClass(), "Connected ...  ..   .");
            Logger.debug(getClass(), "Start SOCKS5 Server at port " + sshConfig.getLocalSocksPort());
//			connection.enableDebugging(false, null);


            if (passOK) {
                fireSuccessListener();
                fireCompleteListener();
            }


        } catch (IOException e) {
            Logger.debug(getClass(), e.getMessage());
            e.fillInStackTrace();

            fireErrorListener();
//            fireStopListener();
        }

    }

    private void connectionMonitorLost(Throwable reason) {
        Logger.debug(getClass(), reason.getMessage());

        if (dynamicPortForwarder != null) {
            try {
                dynamicPortForwarder.close();
                Logger.debug(getClass(), "Close Dynamic Port Forwarder");
            } catch (Exception e) {
                Logger.debug(getClass(), e.getMessage());
            }
        }
        //fireStopListener();
        //fireCompleteListener();
//		try {
//		    connection.close();
//			dynamicPortForwarder.close();
//		} catch (IOException e) {
//			Logger.debug(getClass(), e.getMessage());
//		}


    }

    /**
     * @return the proxyData if {@code null} will use direct connection to ssh remote host
     */
    public ProxySocket getProxySocket() {
        if (proxyData != null)
            return proxyData;
        return new DirectProxy(sshConfig.getHost(), sshConfig.getPort(), monitorSpeed);
    }


//	public void addStateListener(StateListener listener) {
//		stateEvent.addStateListener(listener);
//	}
//
//	public void addCompleteListener(CompleteListener listener) {
//		stateEvent.addCompleteListener(listener);
//	}
//
//	public void addErrorListener(ErrorListener listener) {
//		stateEvent.addErrorListener(listener);
//	}
//
//	public void addStartListener(StartListener listener) {
//		stateEvent.addStartListener(listener);
//	}
//
//	public void addStopListener(StopListener listener) {
//		stateEvent.addStopListener(listener);
//	}
//
//	public void addSuccessListener(SuccessListener listener) {
//		stateEvent.addSuccessListener(listener);
//	}
//
//	private void fireCompleteListener() {
//		stateEvent.fireCompleteListener();
//	}
//
//	private void fireErrorListener() {
//		stateEvent.fireErrorListener();
//	}
//
//	private void fireStartListener() {
//		stateEvent.fireStartListener();
//	}
//
//	private void fireStopListener() { stateEvent.fireStopListener(); }
//
//	private void fireSuccessListener() {
//		stateEvent.fireSuccessListener();
//	}

    /**
     * @param proxy the ProxyData/ProxySocket to set
     */
    public void setProxySocket(ProxySocket proxy) {
        this.proxyData = proxy;
    }

    /**
     * @return the monitorSpeed
     */
    public TerminalNetworkMonitor getNetworkMonitorSpeed() {
        return monitorSpeed;
    }

    /**
     * @param monitorSpeed the monitorSpeed to set
     */
    public void setNetworkMonitorSpeed(TerminalNetworkMonitor monitorSpeed) {
        this.monitorSpeed = monitorSpeed;
    }

    public boolean authenticateWithKeyboardInteractive(String user, InteractiveCallback cb) throws IOException {
        return connection.authenticateWithKeyboardInteractive(user, cb);
    }

    public boolean authenticateWithKeyboardInteractive(String user, String[] submethods, InteractiveCallback cb) throws IOException {
        return connection.authenticateWithKeyboardInteractive(user, submethods, cb);
    }

    public boolean authenticateWithPassword(String user, String password) throws IOException {
        return connection.authenticateWithPassword(user, password);
    }

    public boolean authenticateWithNone(String user) throws IOException {
        return connection.authenticateWithNone(user);
    }

    public boolean authenticateWithPublicKey(String user, char[] pemPrivateKey, String password) throws IOException {
        return connection.authenticateWithPublicKey(user, pemPrivateKey, password);
    }

    public boolean authenticateWithPublicKey(String user, KeyPair pair) throws IOException {
        return connection.authenticateWithPublicKey(user, pair);
    }

    public boolean authenticateWithPublicKey(String user, File pemFile, String password) throws IOException {
        return connection.authenticateWithPublicKey(user, pemFile, password);
    }

    public void addConnectionMonitor(ConnectionMonitor cmon) {
        connection.addConnectionMonitor(cmon);
    }

    public void setCompression(boolean enabled) throws IOException {
        connection.setCompression(enabled);
    }


    public void close() {
        connection.close();
    }

    public void closeDynamicPortForwarder() {
        if (dynamicPortForwarder != null) {
            dynamicPortForwarder.close();
        }
    }

    public ConnectionInfo connect() throws IOException {
        return connection.connect();
    }

    public ConnectionInfo connect(ServerHostKeyVerifier verifier) throws IOException {
        return connection.connect(verifier);
    }

    public ConnectionInfo connect(ServerHostKeyVerifier verifier, int connectTimeout, int kexTimeout) throws IOException {
        return connection.connect(verifier, connectTimeout, kexTimeout);
    }

    public LocalPortForwarder createLocalPortForwarder(int local_port, String host_to_connect, int port_to_connect) throws IOException {
        return connection.createLocalPortForwarder(local_port, host_to_connect, port_to_connect);
    }

    public LocalPortForwarder createLocalPortForwarder(InetSocketAddress addr, String host_to_connect, int port_to_connect) throws IOException {
        return connection.createLocalPortForwarder(addr, host_to_connect, port_to_connect);
    }

    public LocalStreamForwarder createLocalStreamForwarder(String host_to_connect, int port_to_connect) throws IOException {
        return connection.createLocalStreamForwarder(host_to_connect, port_to_connect);
    }

    public DynamicPortForwarder createDynamicPortForwarder(int local_port) throws IOException {
        return connection.createDynamicPortForwarder(local_port);
    }

    public DynamicPortForwarder createDynamicPortForwarder(InetSocketAddress addr) throws IOException {
        return connection.createDynamicPortForwarder(addr);
    }

    public SCPClient createSCPClient() throws IOException {
        return connection.createSCPClient();
    }

    public void forceKeyExchange() throws IOException {
        connection.forceKeyExchange();
    }

    public String getHostname() {
        return connection.getHostname();
    }

    public int getPort() {
        return connection.getPort();
    }

    public ConnectionInfo getConnectionInfo() throws IOException {
        return connection.getConnectionInfo();
    }

    public String[] getRemainingAuthMethods(String user) throws IOException {
        return connection.getRemainingAuthMethods(user);
    }

    public boolean isAuthenticationComplete() {
        return connection.isAuthenticationComplete();
    }

    public boolean isAuthenticationPartialSuccess() {
        return connection.isAuthenticationPartialSuccess();
    }

    public boolean isAuthMethodAvailable(String user, String method) throws IOException {
        return connection.isAuthMethodAvailable(user, method);
    }

    public Session openSession() throws IOException {
        return connection.openSession();
    }

    public void sendIgnorePacket() throws IOException {
        connection.sendIgnorePacket();
    }

    public void sendIgnorePacket(byte[] data) throws IOException {
        connection.sendIgnorePacket(data);
    }

    public void setClient2ServerCiphers(String[] ciphers) {
        connection.setClient2ServerCiphers(ciphers);
    }

    public void setClient2ServerMACs(String[] macs) {
        connection.setClient2ServerMACs(macs);
    }

    public void setDHGexParameters(DHGexParameters dgp) {
        connection.setDHGexParameters(dgp);
    }

    public void setServer2ClientCiphers(String[] ciphers) {
        connection.setServer2ClientCiphers(ciphers);
    }

    public void setServer2ClientMACs(String[] macs) {
        connection.setServer2ClientMACs(macs);
    }

    public void setServerHostKeyAlgorithms(String[] algos) {
        connection.setServerHostKeyAlgorithms(algos);
    }

    public void setProxyData(ProxyData proxyData) {
        connection.setProxyData(proxyData);
    }

    public void requestRemotePortForwarding(String bindAddress, int bindPort, String targetAddress, int targetPort) throws IOException {
        connection.requestRemotePortForwarding(bindAddress, bindPort, targetAddress, targetPort);
    }

    public void cancelRemotePortForwarding(int bindPort) throws IOException {
        connection.cancelRemotePortForwarding(bindPort);
    }

    public void setSecureRandom(SecureRandom rnd) {
        connection.setSecureRandom(rnd);
    }

    public void enableDebugging(boolean enable, DebugLogger logger) {
        connection.enableDebugging(enable, logger);
    }

    public void ping() throws IOException {
        connection.ping();
    }


}
