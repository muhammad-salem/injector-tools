package org.injector.tools.log.impl;

import org.injector.tools.log.Debugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terminal.Ansi;

import java.util.Formatter;
import java.util.Locale;

public class LogOut implements Debugger {

    protected Ansi ansi = new Ansi();
    Logger logger;
    String messageFormat = "[{}]\t{}";
    String titleHeadFormat = "[{}]\t{} {";
    String titleBodyFormat = "[{}]\t\t{}";
    String titleFooterFormat = "[{}]\t\t}";


    public void setColorFormat(boolean useAnsiColour) {
        if (useAnsiColour) {
            messageFormat = "[" + ansi.red("{}") + "]\t" + ansi.blueLight("{}");
            titleHeadFormat = "[" + ansi.red("{}") + "]\t" + ansi.blueLight("{}") + ansi.green("  {");
            titleBodyFormat = "[" + ansi.red("{}") + "]\t\t" + ansi.green("{}"); // "[{}]\t\t{}";
            titleFooterFormat = "[" + ansi.red("{}") + "]\t\t" + ansi.grayLight("}");
        } else {
            messageFormat = "[{}]\t{}";
            titleHeadFormat = "[{}]\t{} {";
            titleBodyFormat = "[{}]\t\t{}";
            titleFooterFormat = "[{}]\t\t}";
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
        logger.info(message);
    }

    @Override
    public void debug(String className, String message, Object... args) {
        var formatter = new Formatter();
        formatter.format(Locale.getDefault(Locale.Category.FORMAT), message, args);
        message = formatter.toString();
        formatter.close();
        className = getStringMiddle(className);
        debug(className, message);
    }

    @Override
    public void debug(String className, String message) {
        className = getStringMiddle(className);
        String[] lines = message.split("\n");
        for (String line : lines) {
            logger.info(messageFormat, className, line);
        }
    }

    @Override
    public void debug(String className, String title, String message) {
        className = getStringMiddle(className);
        logger.info(titleHeadFormat, className, title);
        String[] lines = message.split("\n");
        for (String line : lines) {
            logger.info(titleBodyFormat, className, line);
        }
        logger.info(titleFooterFormat, className);
    }


    String getStringMiddle(String className) {
        return getStringMiddle(className, 23);
    }
}
