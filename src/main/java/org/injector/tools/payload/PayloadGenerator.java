package org.injector.tools.payload;

public interface PayloadGenerator {

	/*
	 * works with Dropbear server
	 */
	final String Ignore 		= "";
	final String Default 		= "[real_raw][crlf][crlf]";

	final String Normal		= "CONNECT [host_port] [protocol][crlf]Host: [domain][crlf][crlf]";
	final String FrontInject	= "GET [domain] HTTP/1.1[crlf]Host: [domain][crlf][crlf]CONNECT [host_port] [protocol][crlf][crlf]";
	final String BackInject		= "CONNECT [host_port] [protocol][crlf][crlf]GET [domain] [protocol][crlf]Host: [domain][crlf][crlf]";
	final String FrontQuery		= "CONNECT [domain]@[host_port] [protocol][crlf]GET [domain] [protocol][crlf]Host: [domain][crlf][crlf]";
	final String BackQuery		= "CONNECT [host_port]@[domain] [protocol][crlf]GET [domain] [protocol][crlf]Host: [domain][crlf][crlf]";
	
	final String Direct32 		= "[real_raw][crlf]fffffffffffffffffffffffffffff32[crlf]0000000000000000000000000000032[crlf]bbbbbbbbbbbbbbbbbb21[crlf]a[crlf][crlf]";
	
	void add_x_online_host();
	void add_x_forward_host();
	void add_x_forwarded_for();
	
	void add_connection();
	void add_user_agent();
	void add_referer();
	void add_dual_connect();
	void add_raw();
	
	void add_split();
	void add_delay_split();
	void add_instant_split();
	
	
	
	
}

