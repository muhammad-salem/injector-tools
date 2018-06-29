package org.injector.tools.vpn;

import org.injector.tools.log.Logger;
import org.openvpn.ovpn3.Event;
import org.openvpn.ovpn3.LogInfo;
import org.openvpn.ovpn3.Status;

public interface InitVPN {

    default void log(LogInfo loginfo) {
        String text = loginfo.getText();
//       System.out.format("LOG: %s", text);
        Logger.debug(VPN.class, "VPN LOG: ", text);
    }

    default void done(Status status) {
        Logger.debug(VPN.class, "DONE Status: err=%b msg='%s'%n", status.getError(), status.getMessage());
    }

    default void event(Event event) {
        boolean error = event.getError();
        String name = event.getName();
        String info = event.getInfo();
        Logger.debug(VPN.class, "EVENT:\t\t err=%b name=%s info='%s'", error, name, info);
    }

    void show_stats();
}
