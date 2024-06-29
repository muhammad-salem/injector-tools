package org.injector.tools.config.utils;

import lombok.Setter;
import org.injector.tools.config.*;
import org.injector.tools.utils.Utils;

import java.io.FileNotFoundException;

@Setter
public class ManageConfig {

    public static ManageConfig getManager() {
        return manager != null ? manager : new ManageConfig();
    }
    private static ManageConfig manager = getManager();

    private Config config;

    public ManageConfig() {
    }

    public ManageConfig(Config config) {
        this.config = config;
    }


    public static void setManager(ManageConfig manage) {
        ManageConfig.manager = manage;
    }

    public static Config getAppConfig() {
        return getManager().getConfig();
    }

    public static void setAppConfig(Config config) {
        getManager().setConfig(config);
    }

    public static LocalProxyConfig getLocalProxyConfig() {
        return getAppConfig().getLocalProxyConfig();
    }

    public static HostProxyConfig getProxyConfig() {
        return getAppConfig().getHostProxyConfig();
    }

    public static SSHConfig getSshConfig() {
        return getAppConfig().getSshConfig();
    }

    public static PolipoConfig getPolipoConfig() {
        return getAppConfig().getPolipoConfig();
    }

    public static void updateLocalProxyConfig(LocalProxyConfig localProxyConfig) {
        getAppConfig().setLocalProxyConfig(localProxyConfig);
    }

    public static void updateProxyConfig(HostProxyConfig proxyConfig) {
        getAppConfig().setHostProxyConfig(proxyConfig);
    }

    public static void updateSshConfig(SSHConfig sshConfig) {
        getAppConfig().setSshConfig(sshConfig);
    }

    public static void updatePolipoConfig(PolipoConfig polipoConfig) {
        getAppConfig().setPolipoConfig(polipoConfig);
    }

    public static void readConfig(String filename) {
        Config config;
        try {
            config = Utils.fromJson(Config.class, filename);
        } catch (FileNotFoundException e) {
            ManageConfig.getAppConfig();
            return;
        }
        ManageConfig.setAppConfig(config);
    }

    public static void setPolipoDir(String polipoConfigFile, String polipoCache) {
        getPolipoConfig().setPolipoConfigFile(polipoConfigFile);
        getPolipoConfig().setDiskCacheRoot(polipoCache);
    }

    public static void writeConfig(String filename) {
        Utils.toJsonFile(filename, ManageConfig.getAppConfig());
    }

    public static String toFormatConfig() {
        return FormatConfig.ConfigTotext(getAppConfig());
    }

    public static String formatLimitConfig() {
        return FormatConfig.ConfigTotextLimit(getAppConfig());
    }

    public Config getConfig() {
        return config != null ? config : new Config();
    }


}
