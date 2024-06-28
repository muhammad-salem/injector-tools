package org.injector.tools.event;

import org.injector.tools.event.listeners.*;

import java.util.ArrayList;
import java.util.List;

public interface EventHandler {

    List<InitListener> initListenerList = new ArrayList<>();
    List<ErrorListener> errorListenerList = new ArrayList<>();
    List<StartListener> startListenerList = new ArrayList<>();
    List<StopListener> stopListenerList = new ArrayList<>();
    List<CompleteListener> completeListenerList = new ArrayList<>();
    List<SuccessListener> successListenerList = new ArrayList<>();

    default void addStateListener(StateListener listener) {
        addCompleteListener(listener);
        addErrorListener(listener);
        addStartListener(listener);
        addStopListener(listener);
        addSuccessListener(listener);
        addInitListener(listener);
    }

    default void addCompleteListener(CompleteListener listener) {
        completeListenerList.add(listener);
    }

    default void addErrorListener(ErrorListener listener) {
        errorListenerList.add(listener);
    }

    default void addStartListener(StartListener listener) {
        startListenerList.add(listener);
    }

    default void addStopListener(StopListener listener) {
        stopListenerList.add(listener);
    }

    default void addSuccessListener(SuccessListener listener) {
        successListenerList.add(listener);
    }

    default void addInitListener(InitListener listener) {
        initListenerList.add(listener);
    }

    default void fireCompleteListener() {
        completeListenerList.forEach(CompleteListener::runComplete);
    }

    default void fireInitListener() {
        initListenerList.forEach(InitListener::runInit);
    }

    default void fireErrorListener() {
        errorListenerList.forEach(ErrorListener::runError);
    }

    default void fireStartListener() {
        startListenerList.forEach(StartListener::runStart);
    }

    default void fireStopListener() {
        stopListenerList.forEach(StopListener::runStop);
    }

    default void fireSuccessListener() {
        successListenerList.forEach(SuccessListener::runSuccess);
    }

    default void onCompleteListener() {
        completeListenerList.forEach(CompleteListener::onComplete);
    }

    default void onInitListener() {
        initListenerList.forEach(InitListener::onInit);
    }

    default void onErrorListener() {
        errorListenerList.forEach(ErrorListener::onError);
    }

    default void onStartListener() {
        startListenerList.forEach(StartListener::onStart);
    }

    default void onStopListener() {
        stopListenerList.forEach(StopListener::onStop);
    }

    default void onSuccessListener() {
        successListenerList.forEach(SuccessListener::onSuccess);
    }

    default void clearListeners() {
        clearCompleteListener();
        clearInitListener();
        clearStartListener();
        clearStopListener();
        clearSuccessListener();
        clearErrorListener();
    }

    default void clearCompleteListener() {
        completeListenerList.clear();
    }

    default void clearInitListener() {
        initListenerList.clear();
    }

    default void clearErrorListener() {
        errorListenerList.clear();
    }

    default void clearStartListener() {
        startListenerList.clear();
    }

    default void clearStopListener() {
        stopListenerList.clear();
    }

    default void clearSuccessListener() {
        successListenerList.clear();
    }

    default boolean removeStateListener(org.injector.tools.event.listeners.StateListener listener) {
        return removeCompleteListener(listener)
                & removeInitListener(listener)
                & removeErrorListener(listener)
                & removeStartListener(listener)
                & removeStopListener(listener)
                & removeSuccessListener(listener);
    }

    default boolean removeCompleteListener(CompleteListener listener) {
        return completeListenerList.remove(listener);
    }

    default boolean removeInitListener(InitListener listener) {
        return initListenerList.remove(listener);
    }

    default boolean removeErrorListener(ErrorListener listener) {
        return errorListenerList.remove(listener);
    }

    default boolean removeStartListener(StartListener listener) {
        return startListenerList.remove(listener);
    }

    default boolean removeStopListener(StopListener listener) {
        return stopListenerList.remove(listener);
    }

    default boolean removeSuccessListener(SuccessListener listener) {
        return successListenerList.remove(listener);
    }

}
