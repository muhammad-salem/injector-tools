package org.injector.tools.config.utils;

import java.io.FileNotFoundException;

import org.injector.tools.config.*;
import org.injector.tools.utils.Utils;

public class ManagConfig {
	
	private static ManagConfig manager = getManager();
	public static ManagConfig getManager() {return manager != null ? manager : new ManagConfig();}
	public static Config getAppConfig() { return getManager().getConfig(); }
	public static void setManager(ManagConfig manag) { ManagConfig.manager = manag;}
	public static void setAppConfig(Config config) {	getManager().setConfig(config);}
	
	public static LocalProxyConfig getLocalProxyConfig() { return getAppConfig().getLocalProxyConfig(); }
	public static HostProxyConfig getProxyConfig() { return getAppConfig().getHostProxyConfig(); }
	public static SSHConfig getSshConfig() { return getAppConfig().getSshConfig(); }
	public static PolipoConfig getPolipoConfig() { return getAppConfig().getPolipoConfig(); }
//	public static VPNConfig getVpnConfig() { return getAppConfig().getVpnConfig(); }
	
	public static void updateLocalProxyConfig(LocalProxyConfig localProxyConfig) { getAppConfig().setLocalProxyConfig(localProxyConfig); }
	public static void updateProxyConfig(HostProxyConfig proxyConfig) { getAppConfig().setHostProxyConfig(proxyConfig); }
	public static void updateSshConfig(SSHConfig sshConfig) { getAppConfig().setSshConfig(sshConfig); }
	public static void updatePolipoConfig(PolipoConfig polipoConfig) { getAppConfig().setPolipoConfig(polipoConfig); }
//	public static void updatetVpnConfig(VPNConfig vpnConfig) {getAppConfig().setVpnConfig(vpnConfig);}
	
	
	private Config config ;
    public ManagConfig() {}
    public ManagConfig(Config config) {this.config = config;}
	public Config getConfig() {	return config != null ? config : new Config();}
	public void setConfig(Config config) {	this.config = config;}
	

	public static void readConfig(String filename) {
		Config config;
		try {
			config = Utils.fromJson( Config.class, filename);
		} catch (FileNotFoundException e) {
			ManagConfig.getAppConfig();
			return;
		}
		ManagConfig.setAppConfig(config);
	}

	public static void setPolipoDir(String polipoConfigFile, String polipoCache){
        getPolipoConfig().setPolipoConfigFile(polipoConfigFile);
        getPolipoConfig().setDiskCacheRoot(polipoCache);
    }

	public static void writeConfig(String filename) {
		Utils.toJsonFile(filename, ManagConfig.getAppConfig());
	}
	
	public static String toFormatConfig() {
		return FormatConfig.ConfigTotext(getAppConfig());
	}
	
	public static String formatLimitConfig() {
		return FormatConfig.ConfigTotextLimit(getAppConfig());
	}

	
	
	
}
