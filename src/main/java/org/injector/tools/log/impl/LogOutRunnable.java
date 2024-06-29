package org.injector.tools.log.impl;

import org.injector.tools.log.Debugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terminal.ansi.Ansi;

import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class LogOutRunnable implements Debugger, Runnable {

    protected Ansi ansi = new Ansi() {
    };
    protected ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    protected String splitter = "::";
    Logger logger;
    String messageFormat = "[{}]\t{}";
    String titleHeadFormat = "[{}]\t{} {";
    String titleBodyFormat = "[{}]\t\t{}";
    String titleFooterFormat = "[{}]\t }";
    Formatter formatter = new Formatter();
    boolean run = true;

    public void setColorFormat(boolean useAnsiColour) {
        if (useAnsiColour) {
            messageFormat = "[" + ansi.red("{}") + "]\t" + ansi.blueLight("{}");
            titleHeadFormat = "[" + ansi.red("{}") + "]\t" + ansi.blueLight("{}") + ansi.green("  {");
            titleBodyFormat = "[" + ansi.red("{}") + "]\t\t" + ansi.green("{}"); // "[{}]\t\t{}";
            titleFooterFormat = "[" + ansi.red("{}") + "]\t " + ansi.green("}");
        } else {
            messageFormat = "[{}]\t{}";
            titleHeadFormat = "[{}]\t{} {";
            titleBodyFormat = "[{}]\t\t{}";
            titleFooterFormat = "[{}]\t }";
        }
    }

    @Override
    public void initDebugger() {
        configProperty();
        lockFileKey(LogFileKey.out);
        logger = LoggerFactory.getLogger("");
        clearProperty();

    }

    @Override
    public void debug(String message) {
        queue.offer(message);
    }

    @Override
    public void debug(String className, String message, Object... args) {
        formatter.format(Locale.getDefault(Locale.Category.FORMAT), message, args);
        message = formatter.toString();
        className = getStringMiddle(className);
        debug(className, message);
    }

    @Override
    public void debug(String className, String message) {
        queue.offer(className + splitter + message);
    }

    @Override
    public void debug(String className, String title, String message) {
        queue.offer(className + splitter + title + splitter + message);
    }

    String getStringMiddle(String classname) {
        return getStringMiddle(classname, 23);
    }

    public void stop() {
        run = false;
    }

    @Override
    public void run() {
        while (run) {
            String logs = queue.poll();
            if (logs != null) {
                String[] messages = logs.split(splitter);
                switch (messages.length) {
                    case 1:
                        debugLog(messages[0]);
                        break;
                    case 2:
                        debugLog(messages[0], messages[1]);
                        break;
                    case 3:
                        debugLog(messages[0], messages[1], messages[2]);
                        break;
                    default:
                        break;
                }

            }
            if (queue.isEmpty()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (Exception e) {
                }
            }
        }
    }


    protected void debugLog(String message) {
        logger.info(message);
    }

    protected void debugLog(String classname, String message) {
        classname = getStringMiddle(classname);
        String[] lines = message.split("\n");
        for (String line : lines) {
            logger.info(messageFormat, classname, line);
        }
    }


    protected void debugLog(String classname, String title, String message) {
        classname = getStringMiddle(classname);
        logger.info(titleHeadFormat, classname, title);
        String[] lines = message.split("\n");
        for (String line : lines)
            logger.info(titleBodyFormat, classname, line);
        logger.info(titleFooterFormat, classname);
    }
}
