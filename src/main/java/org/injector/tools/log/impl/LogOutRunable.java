package org.injector.tools.log.impl;

import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.injector.tools.log.Debugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terminal.ansi.Ansi;

public class LogOutRunable implements Debugger, Runnable{

    Logger logger;
    protected Ansi ansi = new Ansi() {};
    protected ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    protected String spliter = "::";
    
    String messageFormate = "[{}]\t{}";
    String titleHeadFormate = "[{}]\t{} {";
    String titleBodyFormate = "[{}]\t\t{}";
    String titleFotterFormate = "[{}]\t }";
    
    public void setColourFormate(boolean useansicolour) {
    	if(useansicolour) {
    		messageFormate = "["+ansi.red("{}")+"]\t"+ansi.blueLight("{}");
    		titleHeadFormate = "[" + ansi.red("{}") + "]\t" + ansi.blueLight("{}") + ansi.green("  {");
    		titleBodyFormate =  "["+ansi.red("{}")+"]\t\t"+ansi.green("{}"); // "[{}]\t\t{}";
    		titleFotterFormate = "[" + ansi.red("{}") + "]\t " + ansi.green("}");
    	}else {
    		messageFormate = "[{}]\t{}";
    		titleHeadFormate = "[{}]\t{} {";
    		titleBodyFormate = "[{}]\t\t{}";
    		titleFotterFormate = "[{}]\t }";
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
        queue.offer(message);
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
        queue.offer(classname + spliter + message);
    }

    @Override
    public void debug(String classname, String title, String message) {
        queue.offer(classname + spliter + title + spliter + message);
    }
    

    String getStringMiddle(String classname){
        return getStringMiddle(classname, 23);
    }

    boolean run = true;
    public void stop(){ run = false;}
	@Override
	public void run() {
		while (run) {
			String logs = queue.poll();
			if(logs != null) {
				String[] messages = logs.split(spliter);
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
				try {TimeUnit.MILLISECONDS.sleep(10);} catch (Exception e) {}
			}
		}
	}
	
	
	protected void debugLog(String message) {
        logger.info(message);
    }
	
    protected void debugLog(String classname, String message) {
    	classname = getStringMiddle(classname);
        String[] lines = message.split("\n");
        for(String line : lines){
            logger.info(messageFormate, classname, line);
        }
    }

    
    protected void debugLog(String classname, String title, String message) {
    	classname = getStringMiddle(classname);
        logger.info(titleHeadFormate, classname, title);
        String[] lines = message.split("\n");
        for(String line : lines)
                 logger.info(titleBodyFormate, classname, line);
        logger.info(titleFotterFormate, classname);
    }
}
