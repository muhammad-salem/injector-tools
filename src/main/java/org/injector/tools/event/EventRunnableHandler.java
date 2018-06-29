package org.injector.tools.event;

import java.util.ArrayList;
import java.util.List;

import org.injector.tools.event.listeners.StateListener;

public interface EventRunnableHandler {
    List<Runnable> initListenerList = new ArrayList<>();
    List<Runnable> errorListenerList = new ArrayList<>();
    List<Runnable> startListenerList = new ArrayList<>();
    List<Runnable> stopListenerList = new ArrayList<>();
    List<Runnable> completeListenerList = new ArrayList<>();
    List<Runnable> successListenerList = new ArrayList<>();


    default void addStateListener(StateListener listener){
        addCompleteListener(listener::onComplete);
        addErrorListener(listener::onError);
        addStartListener(listener::onStart);
        addStopListener(listener::onStop);
        addSuccessListener(listener::onSuccess);
        addInitListener(listener::onInit);
    }


    default void addCompleteListener(Runnable listener){ completeListenerList.add(listener); }
    default void addErrorListener(Runnable listener){ errorListenerList.add(listener); }
    default void addStartListener(Runnable listener){ startListenerList.add(listener); }
    default void addStopListener(Runnable listener){ stopListenerList.add(listener); }
    default void addSuccessListener(Runnable listener){ successListenerList.add(listener); }
    default void addInitListener(Runnable listener){initListenerList.add(listener); }

    
    default void fireCompleteListener(){completeListenerList.forEach(Runnable::run);}
    default void fireInitListener(){initListenerList.forEach(Runnable::run);}
    default void fireErrorListener(){errorListenerList.forEach(Runnable::run);}
    default void fireStartListener(){ startListenerList.forEach(Runnable::run); }
    default void fireStopListener(){ stopListenerList.forEach(Runnable::run); }
    default void fireSuccessListener(){ successListenerList.forEach(Runnable::run); }
    
    
    
    
   


    default void clearListeners() {
        clearCompleteListener();
        clearInitListener();
        clearStartListener();
        clearStopListener();
        clearSuccessListener();
        clearErrorListener();
    }
    default void clearCompleteListener() { completeListenerList.clear(); }
    default void clearInitListener() { initListenerList.clear(); }
    default void clearErrorListener() { errorListenerList.clear(); }
    default void clearStartListener() { startListenerList.clear(); }
    default void clearStopListener() { stopListenerList.clear(); }
    default void clearSuccessListener() { successListenerList.clear(); }

    default boolean removeStateListener(org.injector.tools.event.listeners.StateListener listener) {
        return removeCompleteListener(listener::onComplete)
                & removeInitListener(listener::onInit)
                & removeErrorListener(listener::onError)
                & removeStartListener(listener::onStart)
                & removeStopListener(listener::onStop)
                & removeSuccessListener(listener::onSuccess);
    }

    default boolean removeCompleteListener(Runnable listener) { return completeListenerList.remove(listener); }
    default boolean removeInitListener(Runnable listener) { return initListenerList.remove(listener); }
    default boolean removeErrorListener(Runnable listener) { return errorListenerList.remove(listener); }
    default boolean removeStartListener(Runnable listener) { return startListenerList.remove(listener); }
    default boolean removeStopListener(Runnable listener) { return stopListenerList.remove(listener); }
    default boolean removeSuccessListener(Runnable listener) { return successListenerList.remove(listener); }

}
