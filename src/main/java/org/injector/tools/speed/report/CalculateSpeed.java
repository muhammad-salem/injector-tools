package org.injector.tools.speed.report;

public interface CalculateSpeed {

    void demondSpeedNow();

    long speedOfTCPSend();

    long speedOfTCPReceive();

    long speedOfUDPSend();

    long speedOfUDPReceive();
}
