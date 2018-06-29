package org.injector.tools.proxy;

public interface HostCheck {
	
	void check();
	
	void setHost(String host);
	void setPort(int port);
	void setTimeOut(int millsecond);
	void setResult(boolean isAlive);
	
	String getHost();
	int getPort();
	int  getTimeOut();
	boolean isAlive();
	
	default void checkHost() {
		Thread checkThread = new Thread(this::check, "HostCheckerThread");
		checkThread.start();
		try {
			checkThread.join( getTimeOut() + 50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
