package org.injector.tools.log.impl;

public enum LogFileKey{
		err("System.err"),
		out("System.out");
		public String type;
		LogFileKey(String type){
			this.type = type;
		}
}