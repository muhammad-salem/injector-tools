package org.injector.tools.event.listeners;

public interface CompleteListener {
    default void runComplete(){
        new Thread(this::onComplete).start();
    }
    void onComplete();
}

