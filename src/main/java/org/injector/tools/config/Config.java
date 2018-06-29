package org.injector.tools.config;

import java.util.Arrays;

import org.injector.tools.config.type.HostProxyType;
import org.injector.tools.config.type.LocalProxyType;
import org.injector.tools.config.type.SSHProxyType;
import org.injector.tools.utils.Utils;

public class Config {

	private LocalProxyConfig localProxyConfig = new LocalProxyConfig(8989);
	// private ProxyConfig proxyConfig = new ProxyConfig();
	private SSHConfig sshConfig = new SSHConfig();
	private PolipoConfig polipoConfig = new PolipoConfig();
	private VPNConfig vpnConfig = new VPNConfig();

	protected boolean isDebuggable = false;

//	protected boolean usePolipo = false;
	protected boolean useIPtables = false;

	final String _LocalProxyType = Arrays.toString(LocalProxyType.values());
	final String _HostProxyType = Arrays.toString(HostProxyType.values());
	final String _SSHProxyType = Arrays.toString(SSHProxyType.values());

	private final String _version = "0.2.488-dev";

	public static void CreateJsonTemplet() {
		CreateJsonTemplet("templet.json");
	}

	public static void CreateJsonTemplet(String fileName) {
		fileName = fileName.replace("~", System.getProperty("user.home"));
		Config templet = new Config();
		Utils.toJsonFile(fileName, templet);
	}

	public LocalProxyConfig getLocalProxyConfig() {
		return localProxyConfig;
	}

	public HostProxyConfig getHostProxyConfig() {
		return getLocalProxyConfig().getHostProxyConfig();
	}

	public SSHConfig getSshConfig() {
		return sshConfig;
	}

	public PolipoConfig getPolipoConfig() {
		return polipoConfig;
	}

	public void setLocalProxyConfig(LocalProxyConfig localProxyConfig) {
		this.localProxyConfig = localProxyConfig;
	}

	public void setHostProxyConfig(HostProxyConfig hostProxyConfig) {
		getLocalProxyConfig().setHostProxyConfig(hostProxyConfig);
	}

	public void setSshConfig(SSHConfig sshConfig) {
		this.sshConfig = sshConfig;
	}

	public void setPolipoConfig(PolipoConfig polipoConfig) {
		this.polipoConfig = polipoConfig;
	}

	public boolean isUseIPtables() {
		return useIPtables;
	}

	public void setUseIPtables(boolean useIPtables) {
		this.useIPtables = useIPtables;
	}

//	public boolean isUsePolipo() {
//		return usePolipo;
//	}
//
//	public void setUsePolipo(boolean usePolipo) {
//		this.usePolipo = usePolipo;
//	}

	public VPNConfig getVpnConfig() {
		return vpnConfig;
	}

	public void setVpnConfig(VPNConfig vpnConfig) {
		this.vpnConfig = vpnConfig;
	}

	/**
	 * get Config in json string formate <br/>
	 * same as <b>toJson >/b>
	 * 
	 * @see java.lang.Object#toString()
	 * @return String json
	 * @author salem
	 */
	@Override
	public String toString() {
		return toJson();
	}

	/**
	 * get json string
	 * 
	 * @return String : json formate
	 * @author salem
	 */
	public String toJson() {
		return Utils.toJson(this);
	}

	

	public boolean isDebuggable() {
		return isDebuggable;
	}

	public void setDebuggable(boolean isDebuggable) {
		this.isDebuggable = isDebuggable;
	}

	public String getVersion() {
		return _version;
	}

}
