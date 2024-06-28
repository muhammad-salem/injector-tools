package org.injector.tools.log.impl;

import org.injector.tools.log.Debugger;

public class Log2Null implements Debugger {

    @Override
    public void initDebugger() {
        System.setOut(null);
    }

    @Override
    public void debug(String message) {
    }

    @Override
    public void debug(String className, String message, Object... args) {
    }

    @Override
    public void debug(String className, String message) {
    }

    @Override
    public void debug(String className, String title, String message) {
    }

}
