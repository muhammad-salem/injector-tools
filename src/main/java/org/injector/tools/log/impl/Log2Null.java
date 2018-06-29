package org.injector.tools.log.impl;

import org.injector.tools.log.Debugger;

public class Log2Null implements Debugger {

	@Override	public void initDebugger() { System.setOut(null);	}
	@Override	public void debug(String message) {	}
	@Override	public void debug(String classname, String message, Object... args) {	}
	@Override	public void debug(String classname, String message) {	}
	@Override	public void debug(String classname, String title, String message) {		}

}
