package org.injector.tools.event.listeners;

public interface ErrorListener {
    default void runError() {
        new Thread(this::onError).start();
    }

    void onError();
}








