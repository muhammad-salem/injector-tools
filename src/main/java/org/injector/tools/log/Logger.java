package org.injector.tools.log;

import org.injector.tools.log.impl.Log2Null;

public class Logger {

//	public static boolean DEBUG = false;
	private static Debugger logger;
	public static Debugger getLogger() {return logger; }
	public static void setLogger(Debugger logger) { Logger.logger = logger; }
	public static void setLogger(boolean DEBUG,Debugger logger) {
//	    Logger.DEBUG = DEBUG;
	    if(DEBUG) {
	    	Logger.logger = logger;
	    }
	    else {
	    	Logger.logger = new Log2Null();
	    }
	    
	}

	public static void setLogger(boolean DEBUG, Class<Debugger> logger) {
//		Logger.DEBUG = DEBUG;
		try {
			 if(DEBUG) {
				 Logger.logger = (Debugger) logger.getConstructors()[0].newInstance(new Object[0]);
			    }
			    else {
			    	Logger.logger = new Log2Null();
			    }
		    
		}catch (Exception e){
		    e.printStackTrace();
		}
	}
//	public static void setDEBUG(boolean DEBUG) { Logger.DEBUG = DEBUG; }
//	public static boolean isDEBUG() { return DEBUG; }

	public static void debug(Class<?> classname, String message, Object ... args) {
//	    if (DEBUG){
	    	logger.debug(classname.getSimpleName(), message, args);
//		}
	}
	public static void debug(Class<?> classname, String message) {
//		if (DEBUG){
	    	logger.debug(classname.getSimpleName(), message);
//		}

	}
	public static void debug(String title, String message) {
//		if (DEBUG){
		    logger.debug(title, message);
//		}

	}
	public static void debug(Class<?> classname,String title, String message) {
//		if (DEBUG){
		    logger.debug(classname.getSimpleName(), title, message);
//		}

	}
	public static void debug(String classname,String title, String message) {
//		if (DEBUG){
		    logger.debug(classname, title, message);
//		}
	}

	public static void debug( String message){
//		if (DEBUG){
		    logger.debug(message);
//		}
	}

	public static void error(Class<?> classname, String message) {
//		if (DEBUG){
		    debug(classname.getSimpleName(), message);
//		}

	}
	public static void error(String title, String message) {
//		if (DEBUG){
		    logger.debug(title, message);
//		}

	}
	
	
}
