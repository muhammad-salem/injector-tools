package org.injector.tools.log.impl;

import org.slf4j.LoggerFactory;

public class LogFile extends LogOut{

    String fileName;
    public LogFile(String fileName){
        this.fileName = fileName;
    }

    @Override
    public synchronized void initDebugger() {
        configProperty();
        setLOGFILEKEY(fileName);
        logger = LoggerFactory.getLogger("");
        clearProperty();
    }

}
