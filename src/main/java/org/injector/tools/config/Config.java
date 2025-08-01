package org.injector.tools.config;

import lombok.Getter;
import lombok.Setter;
import org.injector.tools.config.type.HostProxyType;
import org.injector.tools.config.type.LocalProxyType;
import org.injector.tools.config.type.SSHProxyType;
import org.injector.tools.utils.Utils;

import java.util.Arrays;

@Setter
@Getter
public class Config {
    private SSHConfig sshConfig = new SSHConfig();
    private LocalProxyConfig localProxyConfig = new LocalProxyConfig(8989, LocalProxyType.STOP, new HostProxyConfig());

    private Boolean enableLogs = true;
    private final String _SSHProxyType = Arrays.toString(SSHProxyType.values());
    private final String _LocalProxyType = Arrays.toString(LocalProxyType.values());
    private final String _HostProxyType = Arrays.toString(HostProxyType.values());
    private final String _version = "0.5.1";

    public static void CreateJsonTemplate() {
        CreateJsonTemplate("template.json");
    }

    public static void CreateJsonTemplate(String fileName) {
        fileName = fileName.replace("~", System.getProperty("user.home"));
        var template = new Config();
        Utils.toJsonFile(fileName, template);
    }

    public HostProxyConfig getHostProxyConfig() {
        return getLocalProxyConfig().getHostProxyConfig();
    }

    public void setHostProxyConfig(HostProxyConfig hostProxyConfig) {
        getLocalProxyConfig().setHostProxyConfig(hostProxyConfig);
    }

    /**
     * get Config in json string format <br/>
     * same as <b>toJson >/b>
     *
     * @return String json
     * @author salem
     * @see java.lang.Object#toString()
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

    public String getVersion() {
        return _version;
    }

}
