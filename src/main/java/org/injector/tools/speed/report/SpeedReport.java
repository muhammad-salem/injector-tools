package org.injector.tools.speed.report;

import java.text.DecimalFormat;

public interface SpeedReport extends SpeedTotal, CalculateSpeed{

    default String getTotalReceiveMB(){return toUnitLength(getTotalReceive());}
    default String getTotalSendMB(){return toUnitLength(getTotalSend());}
    default String getTotalMB(){return toUnitLength(getTotal());}
    default String getReceiveTCPMB() {return toUnitLength(getReceiveTCP());}
    default String getReceiveUDPMB(){return toUnitLength(getReceiveUDP());}
    default String getSendTCPMB(){return toUnitLength(getSendTCP());}
    default String getSendUDPMB(){return toUnitLength(getSendUDP());}


    default String getSpeedTCPReceiveMB() { return toUnitLength(speedOfTCPReceive()); }
    default String getSpeedTCPSendMB() {
        return toUnitLength(speedOfTCPSend());
    }
    default String getSpeedUDPReceiveMB() {
        return toUnitLength(speedOfUDPReceive());
    }
    default String getSpeedUDPSendMB() {
        return toUnitLength(speedOfUDPSend());
    }



    //******************Utils*******************//

    default String toUnitLength(long length) {
//        String size = new String();
        DecimalFormat decFormat = new DecimalFormat("0.0");

        float b = length;
        float k = b/1024;
        float m = k/1024;
        float g = m/1024;
        float t = g/1024;

        if (t >= 1) {
            return  decFormat.format(t) +" TB";
        } else if (g >= 1) {
            return  decFormat.format(g) +" GB";
        } else if (m >= 1) {
            return  decFormat.format(m) +" MB";
        } else if (k >= 1) {
            return  decFormat.format(k) +" KB";
        }

        return  decFormat.format(b) +" Bytes";
    }
}
