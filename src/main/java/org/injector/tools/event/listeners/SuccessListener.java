package org.injector.tools.event.listeners;

public interface SuccessListener {
    default void runSuccess(){
        new Thread(this::onSuccess).start();
    }
    void onSuccess();
}
