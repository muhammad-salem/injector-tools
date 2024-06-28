package org.injector.tools.event.listeners;

public interface StartListener {
    default void runStart() {
        new Thread(this::onStart).start();
    }

    void onStart();
}