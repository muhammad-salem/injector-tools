package org.injector.tools.config.utils;

import org.injector.tools.config.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class FormatConfig {

    public static String ConfigToText(Config config) {
        FormatConfig formatConfig = new FormatConfig();
        String temp = formatConfig.getStringMiddle(" Config ", 67, '=') + "\n";
        temp += formatConfig.getLine("Debug", config.getDebuggable() + "");

        temp += formatConfig.toString(config.getLocalProxyConfig());
        temp += formatConfig.toString(config.getHostProxyConfig());
        temp += formatConfig.toString(config.getSshConfig());

        temp += "**\n" + config.getVersion() + " 2017-2018 © \n";
        temp += formatConfig.getStringMiddle("========", 67, '=') + "\n";

        return temp;
    }

    public static String ConfigTotextLimit(Config config) {
        FormatConfig formatConfig = new FormatConfig();
        String temp = formatConfig.getStringMiddle(" Config ", 67, '=') + "\n";

        Map<String, String> map = new LinkedHashMap<>();
        map.put("Debug", config.getDebuggable() + "");
        temp += formatConfig.getLine(map);
        map.clear();
        temp += formatConfig.toStringLimit(config.getLocalProxyConfig());
        temp += formatConfig.toStringLimit(config.getHostProxyConfig());
        temp += formatConfig.toStringLimit(config.getSshConfig());

        temp += "**\n**" + formatConfig.getStringMiddle("        ", 39, ' ');
        temp += "   " + config.getVersion() + " 2017-2018 © \n";

        temp += formatConfig.getStringMiddle("========", 67, '=') + "\n";

        return temp;
    }

    public String getStringMiddle(String str, int count, char ch) {
        if (str.length() > count)
            return str;
        int append = (count - str.length()) / 2;
        boolean reminder = ((count - str.length()) % 2) == 1;
        if (reminder) {
            str = ch + str;
        }
        var padding = String.valueOf(ch).repeat(append);
        return padding + str + padding;
    }

    public String getStringMidlle67(String str) {
        return getStringMiddle(str, 67, '-');
    }

    public String getStringRight(String str, int count, char ch) {
        if (str.length() > count)
            return null;
        for (int i = str.length(); i < count; i++) {
            str = ch + str;
        }
        return str;
    }

    public String getStringRight(String str, int count) {
        return getStringRight(str, count, ' ');
    }

    String getWidthRight(String string) {
        return getStringRight(string, 30);
    }

    public String getLine(String name, String value) {
        return "**" + getWidthRight(name) + " : " + value + "\n";
    }

    public String getLine(Map<String, String> map) {
        String temp = "**";

        for (String key : map.keySet()) {
            temp += '\t' + key + " : " + map.get(key) + '\t';
        }
//		System.out.println(temp);
        return temp + "\n";
    }

    public String getLineWidth_80(String name, final String value) {
        String temp = "**\t" + name + " : " + value + '\n';
        return temp;
    }

    public String toString(LocalProxyConfig localProxyConfig) {
        if (localProxyConfig == null) {
            return "";
        }
        String temp = "";
        temp += getStringMidlle67(" Local Proxy ") + "\n";
        temp += getLine("Type", localProxyConfig.getLocalProxyType().toString());
        temp += getLine("Port", localProxyConfig.getLocalProxyPort() + "");
        return temp;
    }

    public String toStringLimit(LocalProxyConfig localProxyConfig) {
        if (localProxyConfig == null) {
            return "";
        }
        String temp = getStringMidlle67(" Local Proxy ") + "\n";

        Map<String, String> map = new LinkedHashMap<>();
        map.put("Type", localProxyConfig.getLocalProxyType().toString());
        map.put("Port", localProxyConfig.getLocalProxyPort() + "");
        return temp + getLine(map);
    }

    public String toString(HostProxyConfig proxyConfig) {
        if (proxyConfig == null)
            return "";
        String temp = "";
        temp += getStringMidlle67(" Proxy Server ") + "\n";
        temp += getLine("Type", proxyConfig.getProxyType().toString());
        temp += getLine("Host", proxyConfig.getProxyHost());
        temp += getLine("Port", proxyConfig.getProxyPort() + "");
        temp += getLine("User", proxyConfig.getProxyUser());
        temp += getLine("Password", proxyConfig.getProxyPassword());
        temp += getLine("Payload", proxyConfig.getPayload());

        return temp;
    }

    public String toString(SSHConfig sshConfig) {
        if (sshConfig == null)
            return "";

        String temp = "";
        temp += getStringMidlle67(" SSH Config ") + "\n";

        temp += getLine("Host", sshConfig.getHost());
        temp += getLine("Port", sshConfig.getPort() + "");
        temp += getLine("User", sshConfig.getUser());
        temp += getLine("Password", sshConfig.getPassword());
        temp += getLine("Local Socks Port", sshConfig.getLocalSocksPort() + "");
        temp += getLine("Local HTTP Port", sshConfig.getLocalHttpPort() + "");
        temp += getLine("Kex Time out", String.valueOf(sshConfig.getTimeout()));
        temp += getLine("Use Compression", String.valueOf(sshConfig.isUseCompression()));
        temp += getLine("Debug Connection", String.valueOf(sshConfig.isDebuggable()));
        temp += getLine("SSH Proxy Type", sshConfig.getSshProxyType().toString());
        temp += getLine("Proxy Host", sshConfig.getProxyHost());
        temp += getLine("Proxy Port", sshConfig.getProxyPort() + "");
        temp += getLine("Direct Payload", sshConfig.getPayload());

        return temp;
    }

    public String toStringLimit(HostProxyConfig proxyConfig) {
        if (proxyConfig == null)
            return "";
        String temp = getStringMidlle67(" Proxy Server ") + "\n";

        Map<String, String> map = new LinkedHashMap<>();
        map.put("Type", proxyConfig.getProxyType().toString());
        temp += getLine(map);
        map.clear();

        map.put("Host", proxyConfig.getProxyHost());
        map.put("Port", proxyConfig.getProxyPort() + "");
        temp += getLine(map);
        map.clear();

        map.put("User", proxyConfig.getProxyUser());
        map.put("Password", proxyConfig.getProxyPassword());
        temp += getLine(map);
        map.clear();

        temp += getLineWidth_80("Payload", proxyConfig.getPayload());

        return temp;
    }

    public String toStringLimit(SSHConfig sshConfig) {
        if (sshConfig == null)
            return "";

        String temp = getStringMidlle67(" SSH Config ") + "\n";

        Map<String, String> map = new LinkedHashMap<>();
        map.put("Host", sshConfig.getHost());
        map.put("Port", sshConfig.getPort() + "");
        temp += getLine(map);
        map.clear();
        map.put("User", sshConfig.getUser());
        map.put("Password", sshConfig.getPassword());
        temp += getLine(map);
        map.clear();
        map.put("Local Socks Port", sshConfig.getLocalSocksPort() + "");
        map.put("Local HTTP Port", sshConfig.getLocalHttpPort() + "");
        temp += getLine(map);
        map.clear();

        map.put("Use Compression", String.valueOf(sshConfig.isUseCompression()));
        map.put("Debug Connection", String.valueOf(sshConfig.isDebuggable()));
        temp += getLine(map);
        map.clear();

        map.put("SSH Proxy Type", sshConfig.getSshProxyType().toString());
        map.put("Kex Time out", String.valueOf(sshConfig.getTimeout()));
        temp += getLine(map);
        map.clear();

        map.put("Proxy Host", sshConfig.getProxyHost());
        map.put("Proxy Port", sshConfig.getProxyPort() + "");
        temp += getLine(map);
        map.clear();
        temp += getLineWidth_80("Direct Payload", sshConfig.getPayload());
        return temp;
    }

}
