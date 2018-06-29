package org.injector.tools.vpn;

import java.io.File;
import java.io.IOException;

import org.injector.tools.log.Logger;
import org.openvpn.ovpn3.OpenVPNClient;
import org.openvpn.vpn.VPNClient;

public class VPN extends VPNClient implements InitVPN {

	public VPN() {	}
	public VPN(String config_text, String username, String password, String proxyHost, String proxyPort)
			throws ConfigError, CredsUnspecifiedError {
		// defineConfigWithProxy(proxyHost, proxyPort);
		super(config_text, username, password, proxyHost, proxyPort);
	}

	public VPN(String config_text, String username, String password, String proxyHost, String proxyPort,
			String proxyUsername, String proxyPassword) throws ConfigError, CredsUnspecifiedError {

		// defineConfigWithProxy(proxyHost, proxyPort, proxyUsername,proxyPassword);
		super(config_text, username, password, proxyHost, proxyPort, proxyUsername, proxyPassword);
	}

	public VPN(String config_text, String username, String password) throws ConfigError, CredsUnspecifiedError {
		super(config_text, username, password);
	}

	public VPN(File config_file, String username, String password, String proxyHost, String proxyPort)
			throws ConfigError, CredsUnspecifiedError, IOException {

		// defineConfigWithProxy(proxyHost, proxyPort);
		super(config_file, username, password, proxyHost, proxyPort);
	}

	public VPN(File config_file, String username, String password, String proxyHost, String proxyPort,
			String proxyUsername, String proxyPassword) throws ConfigError, CredsUnspecifiedError, IOException {

		// defineConfigWithProxy(proxyHost, proxyPort, proxyUsername,proxyPassword);
		super(config_file, username, password, proxyHost, proxyPort, proxyUsername, proxyPassword);
	}

	public VPN(File config_file, String username, String password)
			throws ConfigError, CredsUnspecifiedError, IOException {
		super(config_file, username, password);
	}

	

	@Override
	public void show_stats() {
		int n = OpenVPNClient.stats_n();
		for (int i = 0; i < n; ++i) {
			String name = OpenVPNClient.stats_name(i);
			long value = this.getClient_thread().stats_value(i);
			if (value > 0)
				Logger.debug(VPN.class, "STAT:\t\t %s=%s", name, value);
		}
	}

}
