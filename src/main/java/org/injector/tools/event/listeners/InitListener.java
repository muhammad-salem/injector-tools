package org.injector.tools.event.listeners;
public interface InitListener{
    default void runInit(){
        new Thread(this::onInit).start();
    }
    void onInit();

}
