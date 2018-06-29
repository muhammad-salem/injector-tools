package org.injector.tools.config;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;

public class PolipoConfig {

	protected boolean usePolipo = false;
	private int proxyPort = 8123;
	private String proxyAddress = "0.0.0.0";
	private String socksProxyType = "socks5";
	private String socksParentProxy = "127.0.0.1:1080";
	private String dnsNameServer = "8.8.8.8";
	
	private String allowedPorts = "1-65535";
	private String tunnelAllowedPorts = "1-65535";

	transient private String diskCacheRoot ="";// R.PolipoCache; // null -> no cache
    transient private String polipoConfigFile = ""; // R.PolipoConfigFile;

	public int getProxyPort() {
		return proxyPort;
	}

	public String getProxyAddress() {
		return proxyAddress;
	}

	public String getSocksProxyType() {
		return socksProxyType;
	}

	public String getSocksParentProxy() {
		return socksParentProxy;
	}

	public String getDnsNameServer() {
		return dnsNameServer;
	}

	public String getDiskCacheRoot() {
		return diskCacheRoot;
	}

	public String getAllowedPorts() {
		return allowedPorts;
	}

	public String getTunnelAllowedPorts() {
		return tunnelAllowedPorts;
	}

	public boolean isUsePolipo() {
		return usePolipo;
	}

	public void setUsePolipo(boolean usePolipo) {
		this.usePolipo = usePolipo;
	}
	
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public void setProxyAddress(String proxyAddress) {
		this.proxyAddress = proxyAddress;
	}

	public void setSocksProxyType(String socksProxyType) {
		this.socksProxyType = socksProxyType;
	}

	public void setSocksParentProxy(String socksParentProxy) {
		this.socksParentProxy = socksParentProxy;
	}

	public void setDnsNameServer(String dnsNameServer) {
		this.dnsNameServer = dnsNameServer;
	}

	public void setDiskCacheRoot(String diskCacheRoot) {
		this.diskCacheRoot = diskCacheRoot;
	}

	public void setAllowedPorts(String allowedPorts) {
		this.allowedPorts = allowedPorts;
	}

	public void setTunnelAllowedPorts(String tunnelAllowedPorts) {
		this.tunnelAllowedPorts = tunnelAllowedPorts;
	}
	
	
	/**********************************************************/
	/****************** polipo setting ************************/
	/**********************************************************/

	public void createPolipoConfigProp() {
		createPolipoConfigProp(getPolipoConfigFile());
	}
	
	public void createPolipoConfigProp(String fileName) {
		try {
			Properties prop = updateProperties();
			PrintStream out = new PrintStream(fileName);
			out.println("#HTTP1o1 Injector - Polipo Config");
			Set<String> names = prop.stringPropertyNames();

			for (String name : names) {
				out.print(name);
                out.print(" = ");
				out.println(prop.getProperty(name));
			}
			out.flush();
			out.close();
		} catch (Exception e) {
			System.out.println("error writing to polipo's config file");
		}
	}
	
	
	public Properties updateProperties() {
		Properties prop = new Properties();
		Field[] fields = getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				Object object = field.get(this);
				if(object instanceof String) {
					String temp = (String) object;
//					temp = temp.replaceAll(":", "\\:");
//					System.out.println(temp);
					prop.setProperty(field.getName(), temp);
				}else if (object instanceof Integer) {
					Integer temp = (Integer) object;
					prop.setProperty(field.getName(), temp.toString());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				
				e.printStackTrace();
			}
			
			
		}
		prop.remove("polipoConfigFile");
		return prop;
	}

    public String getProperties() {
		String data = "#HTTP1o1 Injector - Polipo Config";
		Properties prop = updateProperties();
		Set<String> names = prop.stringPropertyNames();
		for (String name : names) {
            data += '\n' + name + " = " + prop.getProperty(name);
		}
		return data;
	}
	
	public String getConfigData() {
		String data = "proxyPort = "+ proxyPort + "\n";
		data += "proxyAddress = \""+ proxyAddress + "\"\n";
		data += "socksProxyType = "+ socksProxyType +"\n";
		data += "socksParentProxy = \""+ socksParentProxy + "\"\n";
		data += "dnsNameServer = \""+ dnsNameServer + "\"\n";
		data += "allowedPorts = "+ allowedPorts +"\n";
		data += "tunnelAllowedPorts = "+ tunnelAllowedPorts +"\n";
        data += "diskCacheRoot = "+ diskCacheRoot + "\n";
		data += "cacheIsShared=true\n";



		return data;
	}


	/**
	 * indicate if config file is created or not
	 * @throws IOException 
	 */
	
	public void createPolipoConfigFile() throws IOException {
		String data = getProperties();
//        PrintStream out = new PrintStream(getPolipoConfigFile());
//        out.write(data.getBytes());
//        out.flush();
//        out.close();

		writeConfig(getPolipoConfigFile(), data);

//		createPolipoConfigProp(getPolipoConfigFile()+".prop");
		
//		createPolipoConfigProp();
	}
	public void createPolipoConfigFile(String filename) throws IOException {
		String data = getConfigData();
		writeConfig(filename, data);
	}

	public void writeConfig(String filename, String data) throws IOException {
		FileUtils.write(new File(filename), data, "utf-8");
	}

	public String getPolipoConfigFile() {
		return polipoConfigFile;
	}

	public void setPolipoConfigFile(String polipoConfigFile) {
		this.polipoConfigFile = polipoConfigFile;
	}
}
