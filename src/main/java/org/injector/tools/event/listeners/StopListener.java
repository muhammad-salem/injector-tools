package org.injector.tools.event.listeners;

public interface StopListener {
    default void runStop() {
        new Thread(this::onStop).start();
    }

    void onStop();
}