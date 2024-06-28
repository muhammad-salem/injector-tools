package org.injector.tools.ssh.jsch;


import com.jcraft.jsch.UserInfo;
import org.injector.tools.log.Logger;

public class SSHUserInfo implements UserInfo {

    String passwd;

    public SSHUserInfo(String passwd) {
        this.passwd = passwd;
    }

    @Override
    public String getPassphrase() {
        return null;
    }

    @Override
    public String getPassword() {
        return passwd;
    }

    @Override
    public boolean promptPassword(String message) {
        Logger.debug(getClass(), "prompt password", message);
        return true;
    }

    @Override
    public boolean promptPassphrase(String message) {
        Logger.debug(getClass(), "prompt passphrase", message);
        return true;
    }

    @Override
    public boolean promptYesNo(String message) {
        Logger.debug(getClass(), "prompt {Yes-No}", message);
        return true;
    }

    @Override
    public void showMessage(String message) {
        Logger.debug(getClass(), "server message", message);
    }


}
