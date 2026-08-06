package org.injector.tools.ssh.jsch;


import com.jcraft.jsch.UserInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        log.info("prompt password: {}", message);
        return true;
    }

    @Override
    public boolean promptPassphrase(String message) {
        log.info("prompt passphrase: {}", message);
        return true;
    }

    @Override
    public boolean promptYesNo(String message) {
        log.info("prompt {Yes-No}: {}", message);
        return true;
    }

    @Override
    public void showMessage(String message) {
        log.info("server message: {}", message);
    }


}
