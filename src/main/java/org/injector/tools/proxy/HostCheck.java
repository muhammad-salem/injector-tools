package org.injector.tools.proxy;

public interface HostCheck {

    void check();

    void setResult(boolean isAlive);

    String getHost();

    void setHost(String host);

    int getPort();

    void setPort(int port);

    int getTimeOut();

    void setTimeOut(int millsecond);

    boolean isAlive();

    default void checkHost() {
        Thread checkThread = new Thread(this::check, "HostCheckerThread");
        checkThread.start();
        try {
            checkThread.join(getTimeOut() + 50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
