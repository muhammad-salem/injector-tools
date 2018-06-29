package org.injector.tools.log.impl;

import java.io.FileNotFoundException;

import org.slf4j.LoggerFactory;

public class LogErr extends LogOutRunable{

    public void initDebugger(String filename){
        try {
            System.setErr(new LogPrintStream(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        initDebugger();
    }

    @Override
    public void initDebugger() {
        configProperty();
        setLOGFILEKEY(LogFileKey.err);
        logger = LoggerFactory.getLogger(""/*ansi.Magenta("HTTP1o1")*/);
        clearProperty();        
        setColourFormate(true);
    }
}
