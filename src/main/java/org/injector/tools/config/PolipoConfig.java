package org.injector.tools.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;

@Setter
@Getter
public class PolipoConfig {

    protected boolean usePolipo = false;
    private int proxyPort = 8123;
    private String proxyAddress = "0.0.0.0";
    private String socksProxyType = "socks5";
    private String socksParentProxy = "127.0.0.1:1080";
    private String dnsNameServer = "8.8.8.8";

    private String allowedPorts = "1-65535";
    private String tunnelAllowedPorts = "1-65535";

    transient private String diskCacheRoot = "";// R.PolipoCache; // null -> no cache
    transient private String polipoConfigFile = ""; // R.PolipoConfigFile;

    public void createPolipoConfigProp() {
        createPolipoConfigProp(getPolipoConfigFile());
    }

    public void createPolipoConfigProp(String fileName) {
        try {
            var prop = updateProperties();
            var out = new PrintStream(fileName);
            out.println("#HTTP1o1 Injector - Polipo Config");
            var names = prop.stringPropertyNames();
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
        var prop = new Properties();
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                Object object = field.get(this);
                if (object instanceof String temp) {
                    prop.setProperty(field.getName(), temp);
                } else if (object instanceof Integer temp) {
                    prop.setProperty(field.getName(), temp.toString());
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.fillInStackTrace();
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
        String data = "proxyPort = " + proxyPort + "\n";
        data += "proxyAddress = \"" + proxyAddress + "\"\n";
        data += "socksProxyType = " + socksProxyType + "\n";
        data += "socksParentProxy = \"" + socksParentProxy + "\"\n";
        data += "dnsNameServer = \"" + dnsNameServer + "\"\n";
        data += "allowedPorts = " + allowedPorts + "\n";
        data += "tunnelAllowedPorts = " + tunnelAllowedPorts + "\n";
        data += "diskCacheRoot = " + diskCacheRoot + "\n";
        data += "cacheIsShared=true\n";
        return data;
    }


    /**
     * indicate if config file is created or not
     */

    public void createPolipoConfigFile() throws IOException {
        var data = getProperties();
        writeConfig(getPolipoConfigFile(), data);
    }

    public void createPolipoConfigFile(String filename) throws IOException {
        String data = getConfigData();
        writeConfig(filename, data);
    }

    public void writeConfig(String filename, String data) throws IOException {
        FileUtils.write(new File(filename), data, "utf-8");
    }

}
