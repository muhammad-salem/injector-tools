package org.injector.tools.log.impl;

import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

public class LogErr extends LogOutRunable {

    public void initDebugger(String filename) {
        try {
            System.setErr(new LogPrintStream(filename));
        } catch (FileNotFoundException e) {
            e.fillInStackTrace();
        }
        initDebugger();
    }

    @Override
    public void initDebugger() {
        configProperty();
        lockFileKey(LogFileKey.err);
        logger = LoggerFactory.getLogger(""/*ansi.Magenta("HTTP1o1")*/);
        clearProperty();
        setColourFormate(true);
    }
}
