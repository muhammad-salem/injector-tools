package org.injector.tools.log;

import org.injector.tools.log.impl.LogFileKey;
import org.slf4j.impl.SimpleLogger;

public interface Debugger {

    void initDebugger();

    default void debug(Class<?> className, String message, Object... args) {
        debug(className.getSimpleName(), message, args);
    }

    default void debug(Class<?> className, String message) {
        debug(className.getSimpleName(), message);
    }

    default void debug(Class<?> className, String title, String message) {
        debug(className.getSimpleName(), title, message);
    }

    void debug(String message);

    void debug(String className, String message, Object... args);

    void debug(String className, String message);

    void debug(String className, String title, String message);

    default void clearProperty() {
//        System.clearProperty(SimpleLogger.LOG_FILE_KEY);
//        System.clearProperty(SimpleLogger.SHOW_DATE_TIME_KEY);
//        System.clearProperty(SimpleLogger.DATE_TIME_FORMAT_KEY);
//        System.clearProperty(SimpleLogger.SHOW_LOG_NAME_KEY);
    }

    default void configProperty() {
        System.setProperty(SimpleLogger.SHOW_DATE_TIME_KEY, "true");
        System.setProperty(SimpleLogger.DATE_TIME_FORMAT_KEY, "h:mm:ss:S a");
        System.setProperty(SimpleLogger.SHOW_LOG_NAME_KEY, "false");
        System.setProperty(SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
//		System.setProperty(SimpleLogger.SHOW_THREAD_NAME_KEY, "true");
        System.setProperty(SimpleLogger.CACHE_OUTPUT_STREAM_STRING_KEY, "true");
        System.setProperty(SimpleLogger.LEVEL_IN_BRACKETS_KEY, "true");
        System.setProperty(SimpleLogger.WARN_LEVEL_STRING_KEY, "WARNING");
        System.setProperty(SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
    }

    default void lockFileKey(LogFileKey key) {
        System.setProperty(SimpleLogger.LOG_FILE_KEY, key.type);
    }

    default void lockFileKey(String filename) {
        System.setProperty(SimpleLogger.LOG_FILE_KEY, filename);
    }

    default String getStringMiddle(String str, int count) {
        if (str.length() > count) return str;
        int append = (count - str.length()) / 2;
        boolean reminder = ((count - str.length()) % 2) == 1;
        if (reminder) str = ' ' + str;
        var padding = String.valueOf(' ').repeat(append);
        return padding + str + padding;
    }

}
