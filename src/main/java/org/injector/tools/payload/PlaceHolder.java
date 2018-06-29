package org.injector.tools.payload;

public enum PlaceHolder{
		url("[url]"),
		host("[host]"),
		port("[port]"),
		host_port("[host_port]"),
		
		cr("[cr]"),
		lf("[lf]"),
		_10("#10"),
		_13("#13"),
		crlf("[crlf]"),
		lfcr("[lfcr]"),
		crlfcrlf("[crlf][crlf]"),
		
		method("[method]"),
		ssh("[ssh]"),
		raw("[raw]"),
		rawAll("[rawAll]"),
		real_raw("[real_raw]"),
		
		protocol("[protocol]"),
		major("major"),
		minor("minor"),
		headers("[headers]"),
		body("[body]"),
		ua("[ua]"),
		header_host("[header_host]"),
		referer("[referer]"),
		netData("[netData]"),
		auth("[auth]"),
		connection("[connection]"),
		cookie("[cookie]"),
		keep_alive("[keep_alive]"),
		cache_control("[cache_control]"),
		rotate("[rotate=*]"),
		
		split("[split]"),
		delay_split("[delay_split]"),
		instant_split("[instant_split]"),
		
		x_online_host("[xonlinehost]"),
		x_forward_host("[xfrhost]"),
		x_forwarded_for("[xfrwdfor]"),
		
		domain("[domain]");

	
	String name;
	private PlaceHolder(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public String getRawString() {
		switch (this) {
		case header_host:
			return "Host:";
		
		case cr:
			return "\r";
		case lf:
			return "\n";
		case _13:
			return "\r";
		case _10:
			return "\n";	
		case crlf:
			return "\r\n";
		case lfcr:
			return "\n\r";
		case crlfcrlf:
			return "\r\n\r\n";
		
		case protocol:
			return "HTTP/";
		case ua:
			return "User-Agent:";
		case referer:
			return "Referer:";
		case x_online_host:
			return "X-Online-Host:";
		case x_forward_host:
			return "X-Forward-Host:";
		case x_forwarded_for:
			return "X-Forwarded-For:";
		case connection:
			return "Connection:";
		case cookie:
			return "Cookie:";
		case keep_alive:
			return "Connection: keep-alive";
		case cache_control:
			return "Cache-Control:";
		case instant_split:
			return "";
			
		default:
			return "";
		}
	}
	
	
}