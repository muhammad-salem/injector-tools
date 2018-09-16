package org.injector.tools.log.impl;

import java.util.Formatter;
import java.util.Locale;

import org.injector.tools.log.Debugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terminal.Ansi;

public class LogOut implements Debugger {

    Logger logger;
    protected Ansi ansi = new Ansi();
    
    String messageFormate = "[{}]\t{}";
    String titleHeadFormate = "[{}]\t{} {";
    String titleBodyFormate = "[{}]\t\t{}";
    String titleFotterFormate = "[{}]\t\t}";
    
    public void setColourFormate(boolean useansicolour) {
    	if(useansicolour) {
    		messageFormate = "["+ansi.red("{}")+"]\t"+ansi.blueLight("{}");
    		titleHeadFormate = "[" + ansi.red("{}") + "]\t" + ansi.blueLight("{}") + ansi.green("  {");
    		titleBodyFormate =  "["+ansi.red("{}")+"]\t\t"+ansi.green("{}"); // "[{}]\t\t{}";
    		titleFotterFormate = "[" + ansi.red("{}") + "]\t\t" + ansi.grayLight("}");
    	}else {
    		messageFormate = "[{}]\t{}";
    		titleHeadFormate = "[{}]\t{} {";
    		titleBodyFormate = "[{}]\t\t{}";
    		titleFotterFormate = "[{}]\t\t}";
		}
    }
    
    @Override
    public void initDebugger() {
        configProperty();
        setLOGFILEKEY(LogFileKey.out);
        logger = LoggerFactory.getLogger("");
        clearProperty();
    }

    @Override
    public void debug(String message) {
        logger.info(message);
    }

    Formatter formatter = new Formatter();
    @Override
    public void debug(String classname, String message, Object... args) {
        formatter.format(Locale.getDefault(Locale.Category.FORMAT),message,  args);
        message = formatter.toString();
        classname = getStringMiddle(classname);
        debug(classname, message);
    }

    @Override
    public void debug(String classname, String message) {
//        message = message.replace("\r","");
        classname = getStringMiddle(classname);
        String[] lines = message.split("\n");
        for(String line : lines){
            logger.info(messageFormate, classname, line);
        }

    }

    @Override
    public void debug(String classname, String title, String message) {
//        debug(classname, title);
//        debug(classname, message);
        classname = getStringMiddle(classname);
        logger.info(titleHeadFormate, classname, title);
        String[] lines = message.split("\n");
        for(String line : lines)
                 logger.info(titleBodyFormate, classname, line);
        logger.info(titleFotterFormate, classname);
    }
    

    String getStringMiddle(String classname){
        return getStringMiddle(classname, 23);
    }
}
