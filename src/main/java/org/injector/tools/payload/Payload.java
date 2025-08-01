package org.injector.tools.payload;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class Payload implements Closeable {

    SplitType splitType = SplitType.NON;
    private String format;
    private String request;
    private TreeMap<PlaceHolder, String> requestMap = new TreeMap<>();
    private ArrayList<String> splitPayload = new ArrayList<String>();

    public Payload(String format) {
        setFormat(format);
    }

    public Payload(String format, String request) {
        setFormat(format);
        setRequest(request);
        initRequestMap();
    }

    public static ArrayList<Integer> getSplitIndexes(String requestPayload) {
        if (!requestPayload.contains("split]") || !requestPayload.contains("[split")) {
            return null;
        }
        ArrayList<Integer> index = new ArrayList<Integer>();
        int x, y, i;
        index.add(0);
        for (int j = 0; j < requestPayload.length(); j++) {
            i = requestPayload.indexOf("split", j);
            if (i == -1) {
                index.add(requestPayload.length());
                break;
            }
            x = requestPayload.lastIndexOf('[', i);
            y = requestPayload.indexOf(']', i) + 1;

            index.add(x);
            index.add(y);
            j = y;
        }
        return index;
    }

    /**
     * the response can be on any forms <br/>
     * * request <--> [split_type] <--> request
     * <br/>
     * * request <--> [split_type] <--> request <--> [split_type]
     * <br/>
     * * split_type <--> request <--> [split_type]
     * <br/>
     * * [split_type] <--> request <--> [split_type] <--> request
     *
     * @param payloadRequest payload to analysis
     * @return list of string as separated request
     */
    public static ArrayList<String> splitPayload(final String payloadRequest) {
        ArrayList<String> list = new ArrayList<String>();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < payloadRequest.length(); i++) {
            if (payloadRequest.charAt(i) == '[') {
                list.add(temp.toString());
                temp = new StringBuilder("[");
            } else if (payloadRequest.charAt(i) == ']') {
                temp.append(']');
                list.add(temp.toString());
                temp = new StringBuilder();
            } else {
                temp.append(payloadRequest.charAt(i));
            }
        }
        if (!temp.isEmpty()) {
            list.add(temp.toString());
        }
        return list;
    }

    public static List<String> splitPayloadRequest(String requestPayload) {
        if (!requestPayload.contains("split]") || !requestPayload.contains("[split")) {
            return new ArrayList<String>() {
                private static final long serialVersionUID = 1L;

                {
                    add(requestPayload);
                }
            };
        }
        ArrayList<String> list = new ArrayList<String>();
        int x, y, i;

        for (int j = 0; j < requestPayload.length(); j++) {
            i = requestPayload.indexOf("split", j);
            if (i == -1) {
                list.add(requestPayload.substring(j, requestPayload.length() - 1));
                break;
            }
            x = requestPayload.lastIndexOf('[', i);
            y = requestPayload.indexOf(']', i) + 1;

            list.add(requestPayload.substring(x, y));
            j = y;
        }
        return list;
    }

    @Override
    public void close() throws IOException {
        requestMap.clear();
        requestMap = null;
        splitPayload.clear();
        splitPayload = null;
        format = null;
        request = null;
        splitType = null;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
        requestMap.clear();
        initRequestMap();
    }

    public void setDomain(String domain) {
        requestMap.put(PlaceHolder.domain, domain);
    }

    /*
     * same as $getRawPayload()
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getRawPayload();
    }

    public String toRawPayload(String replace) {

        replace = replace.replace(PlaceHolder.crlf.toString(), "\r\n");
        replace = replacePlaceholder(replace, PlaceHolder.raw);
        replace = replacePlaceholder(replace, PlaceHolder.rawAll);
        replace = replacePlaceholder(replace, PlaceHolder.real_raw);
        replace = replacePlaceholder(replace, PlaceHolder.url);

        replace = replacePlaceholder(replace, PlaceHolder.method);
        replace = replacePlaceholder(replace, PlaceHolder.host_port);
        replace = replacePlaceholder(replace, PlaceHolder.host);
        replace = replacePlaceholder(replace, PlaceHolder.port);
        replace = replacePlaceholder(replace, PlaceHolder.protocol);
        replace = replacePlaceholder(replace, PlaceHolder.headers);
        replace = replacePlaceholder(replace, PlaceHolder.ua);
        replace = replacePlaceholder(replace, PlaceHolder.referer);
        replace = replacePlaceholder(replace, PlaceHolder.cache_control);
        replace = replacePlaceholder(replace, PlaceHolder.header_host);
        replace = replacePlaceholder(replace, PlaceHolder.netData);
        replace = replacePlaceholder(replace, PlaceHolder.connection);
        replace = replacePlaceholder(replace, PlaceHolder.keep_alive);


        replace = replacePlaceholder(replace, PlaceHolder.x_online_host);
        replace = replacePlaceholder(replace, PlaceHolder.x_forward_host);
        replace = replacePlaceholder(replace, PlaceHolder.x_forwarded_for);
        replace = replacePlaceholder(replace, PlaceHolder.cookie);

        //replace = replacePlaceholder(replace, PlaceHolder.instant_split);


        return replace;
    }

    public String getRawPayload() {

        String replace = format;

        replace = replace.replace("\\n", "\n");
        replace = replace.replace("\\r", "\r");

        replace = replace.replace(PlaceHolder.crlf.toString(), "\r\n");
        replace = replace.replace(PlaceHolder.cr.toString(), "\r");
        replace = replace.replace(PlaceHolder.lf.toString(), "\n");


        replace = replacePlaceholder(replace, PlaceHolder.raw);
        replace = replacePlaceholder(replace, PlaceHolder.rawAll);
        replace = replacePlaceholder(replace, PlaceHolder.url);

        replace = replacePlaceholder(replace, PlaceHolder.method);
        replace = replacePlaceholder(replace, PlaceHolder.host_port);
        replace = replacePlaceholder(replace, PlaceHolder.host);
        replace = replacePlaceholder(replace, PlaceHolder.port);
        replace = replacePlaceholder(replace, PlaceHolder.protocol);
        replace = replacePlaceholder(replace, PlaceHolder.headers);
        replace = replacePlaceholder(replace, PlaceHolder.ua);
        replace = replacePlaceholder(replace, PlaceHolder.referer);
        replace = replacePlaceholder(replace, PlaceHolder.cache_control);
        replace = replacePlaceholder(replace, PlaceHolder.header_host);
        replace = replacePlaceholder(replace, PlaceHolder.netData);
        replace = replacePlaceholder(replace, PlaceHolder.connection);
        replace = replacePlaceholder(replace, PlaceHolder.keep_alive);


        replace = replacePlaceholder(replace, PlaceHolder.x_online_host);
        replace = replacePlaceholder(replace, PlaceHolder.x_forward_host);
        replace = replacePlaceholder(replace, PlaceHolder.x_forwarded_for);
        replace = replacePlaceholder(replace, PlaceHolder.cookie);

        //replace = replacePlaceholder(replace, PlaceHolder.instant_split);


        return replace;
    }

    public ArrayList<String> getSplitwPayload() {
        return splitPayload;
    }

//	void setHostPort() {
//
//
//		String host = getFromRequestMab(PlaceHolder.host);
//		if(host.startsWith("http") && host.contains("://")){
//			int start = host.indexOf("//")+2;
//			int end = host.indexOf("/", start);
//			end = end < 0 ? host.length() : end;
//			requestMap.put(PlaceHolder.host_port, host.substring(start, end)+":"+getFromRequestMab(PlaceHolder.port));
//		}
//		else	requestMap.put(PlaceHolder.host_port, getFromRequestMab(PlaceHolder.host)+":"+getFromRequestMab(PlaceHolder.port));
//	}

    String replacePlaceholder(String str, PlaceHolder holder) {
        String t = getFromRequestMab(holder);
        if (t == null) return str;
        if (!str.contains(holder.toString())) return str;
        return str.replace(holder.toString(), t);
    }

    public void initRequestMap() {
//		System.out.println(request);
//		requestMap.clear();
        setRawAll();
        setRAW();


        String[] lines = request.split("\r\n");
        setNetDate(lines[0]);
        setRequesNetData(getFromRequestMab(PlaceHolder.netData));
        setHeaders(getFromRequestMab(PlaceHolder.raw));

        setHeaderOf(PlaceHolder.ua);
        fixUA();

        setHeaderOf(PlaceHolder.header_host);
        setHeaderOf(PlaceHolder.referer);
        setHeaderOf(PlaceHolder.x_online_host);
        setHeaderOf(PlaceHolder.x_forward_host);
        setHeaderOf(PlaceHolder.x_forwarded_for);
        setHeaderOf(PlaceHolder.connection);
        setHeaderOf(PlaceHolder.cookie);
        setHeaderOf(PlaceHolder.cache_control);

        requestMap.put(PlaceHolder.instant_split, "");

    }

    private void fixUA() {
        if (requestMap.containsKey(PlaceHolder.ua)) {
            String ua = requestMap.get(PlaceHolder.ua);
            ua = ua.replaceAll("User-Agent: ", "");
            requestMap.put(PlaceHolder.ua, ua);
        }
    }

    void setHeaderOf(PlaceHolder holder) {
        String headers = getFromRequestMab(PlaceHolder.headers);
        String holderRaw = holder.getRawString();
        if (headers.contains(holderRaw)) {
            int start = headers.indexOf(holderRaw);
            int end = headers.indexOf("\r\n", start);
            end = end < 0 ? headers.length() : end;
            requestMap.put(holder, headers.substring(start, end));
        }
    }

    void setRequesNetData(String netData) {
        requestMap.put(PlaceHolder.method, netData.split(" ")[0]);
        requestMap.put(PlaceHolder.url, netData.split(" ")[1]);
//		requestMap.put(PlaceHolder.host_port, netData.split(" ")[1]);

        requestMap.put(PlaceHolder.protocol, netData.split(" ")[2]);
        requestMap.put(PlaceHolder.major, requestMap.get(PlaceHolder.protocol).charAt(5) + "");
        requestMap.put(PlaceHolder.minor, requestMap.get(PlaceHolder.protocol).charAt(7) + "");

        String url = getUrl();
        int startIndex = url.indexOf("//");
        startIndex = startIndex == -1 ? 0 : startIndex + 2;
        int endIndex = url.indexOf('/', startIndex);
        endIndex = endIndex == -1 ? url.length() : endIndex;

        String host_port = url.substring(startIndex, endIndex);
        if (host_port.contains(":")) {
            int colindex = host_port.indexOf(':');
            requestMap.put(PlaceHolder.host, host_port.substring(0, colindex));
            requestMap.put(PlaceHolder.port, host_port.substring(colindex + 1));
        } else {
            requestMap.put(PlaceHolder.host, host_port);
            requestMap.put(PlaceHolder.port, (url.startsWith("https") ? "443" : "80"));
        }
        requestMap.put(PlaceHolder.host_port, getHost() + ':' + getPort());

//		final String url = requestMap.get(PlaceHolder.url);
//		if(url.contains("http://")){
//			String host_port = url.substring(7);
//			if(host_port.contains(":")) {
//				requestMap.put(PlaceHolder.host, host_port.substring(0, host_port.indexOf(':')));
//				requestMap.put(PlaceHolder.port, host_port.substring(host_port.indexOf(':')+1, host_port.length()-1));
//			}else {
//				requestMap.put(PlaceHolder.host, url);
//
//				int index = netData.split(" ")[1].indexOf('/', 7);
//				if(index != -1) {
//					requestMap.put(PlaceHolder.host, url.substring(7,index));
//				}
//
//				requestMap.put(PlaceHolder.port, "80");
//			}
//
//
//		}else if(url.contains("https://")){
//			String host_port = netData.split(" ")[1].substring(6);
//			if(host_port.contains(":")) {
//				requestMap.put(PlaceHolder.host, host_port.substring(0, host_port.indexOf(':')));
//				requestMap.put(PlaceHolder.port, host_port.substring(host_port.indexOf(':')+1, host_port.length()-1));
//			}else {
////				requestMap.put(PlaceHolder.host, netData.split(" ")[1]);
//
//				int index = url.indexOf('/', 8);
//				requestMap.put(PlaceHolder.host, url.substring(8,index));
//
//				requestMap.put(PlaceHolder.port, "443");
//			}
//
//		}else if(netData.split(" ")[1].contains(":")){
//			String[] hstprt = url.split(":");
//			requestMap.put(PlaceHolder.host, hstprt[0]);
//			requestMap.put(PlaceHolder.port, hstprt[1]);
//		}
//		setHostPort();
    }

    void setNetDate(String netData) {
        requestMap.put(PlaceHolder.netData, netData);
    }

    void setRAW() {
        //requestMap.put(PlaceHolder.raw, request.split("\r\n\r\n")[0]);
        int end = request.indexOf("\r\n\r\n");
        if (end == -1) end = request.length();
        requestMap.put(PlaceHolder.raw, request.substring(0, end));

    }

    void setRawAll() {
        requestMap.put(PlaceHolder.rawAll, request);
        requestMap.put(PlaceHolder.real_raw, request);
    }

    private String getFromRequestMab(PlaceHolder holder) {
        return requestMap.get(holder);
    }

    public void printMab() {
        System.out.println("============================ Map Request ============================");
        for (PlaceHolder ph : requestMap.keySet()) {
            System.out.println(
                    ph + " : " +
                            requestMap.get(ph)
                                    .replace(PlaceHolder.crlf.getRawString(),
                                            PlaceHolder.crlf.toString()));
        }

        System.out.println("============================================================================");
    }

    public String getPlaceHolder(PlaceHolder holder) {
        return requestMap.get(holder);
    }

    public SplitType geSplitType() {
        initSplitIfFound();
        return splitType;
    }

    public ArrayList<Integer> getSplitIndexes() {
        return getSplitIndexes(getRawPayload());
    }

    public List<String> getSplitPayloadRequest() {
        return splitPayloadRequest(getRawPayload());
    }

    private void initSplitIfFound() {

        if (format.contains("split")) {
            int x = 0, y = 0, i = 0;
            i = format.indexOf("split");
            x = format.lastIndexOf('[', i);
            y = format.indexOf(']', i) + 1;

            splitPayload.add(format.substring(0, x));
            splitPayload.add(format.substring(y));

            String sub = format.substring(x, y);
//
//			System.out.println(sub);
//
//			String[] splt = formate.split(sub, 2);
//			for (String str : splt) {
//				splitRaw.add(str);
//			}
			/*
			 * 	# [instant_split] = request 1 -> request 2
				# [split] = request 1 -> delay -> request 2
				# [delay_split] = request 1 -> more delay -> request 2
				# [repeat_split] = request 1 + request 1 -> request 2
				# [reverse_split] = request 1 + request 2 -> request 2
				# [split-x] = request 1 + request 2 -> delay -> request 2
				# [x-split] = request 1 + request 2 -> request 2
			 */
            switch (sub) {
                case "[split]":
                    splitType = SplitType.Split;
                    break;
                case "[instant_split]":
                    splitType = SplitType.Instant_Split;
                    break;
                case "[delay_split]":
                    splitType = SplitType.Delay_Split;
                    break;
                case "[repeat_split]":
                    splitType = SplitType.Repeat_Split;
                    break;
                case "[reverse_split]":
                    splitType = SplitType.Reverse_Split;
                    break;
                case "[split-x]":
                    splitType = SplitType.Split_X;
                    break;
                case "[x-split]":
                    splitType = SplitType.X_Split;
                default:
                    break;
            }

        } else
            splitType = SplitType.NON;

    }

    /**
     * @return the url
     */
    public String getUrl() {
        return getFromRequestMab(PlaceHolder.url);
    }


    /////////////////////////////////////

    /**
     * @return the host
     */
    public String getHost() {
        return getFromRequestMab(PlaceHolder.host);
    }

    /**
     * @return the port
     */
    public String getPort() {
        return getFromRequestMab(PlaceHolder.port);
    }

//	public String getHostAbsoulte() {
//		String temp = getFromRequestMab(PlaceHolder.header_host);
//		temp = temp.substring(temp.indexOf(':')+2);
//		return temp;
//	}

    public int getPortInt() {
        try {
            return Integer.parseInt(getPort());
        } catch (NumberFormatException e) {
            return 80;
        }
    }

    /**
     * @return the host_port
     */
    public String getHost_port() {
        return getFromRequestMab(PlaceHolder.host_port);
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return getFromRequestMab(PlaceHolder.method);
    }

    /**
     * @return the ssh
     */
    public String getSsh() {
        return getFromRequestMab(PlaceHolder.ssh);
    }

    /**
     * @return the raw
     */
    public String getRaw() {
        return getFromRequestMab(PlaceHolder.raw);
    }

    /**
     * @return the rawAll
     */
    public String getRawAll() {
        return getFromRequestMab(PlaceHolder.rawAll);
    }

    /**
     * @return the protocol
     */
    public String getProtocol() {
        return getFromRequestMab(PlaceHolder.protocol);
    }

    /**
     * @return the major
     */
    public String getMajor() {
        return getFromRequestMab(PlaceHolder.major);
    }

    /**
     * @return the minor
     */
    public String getMinor() {
        return getFromRequestMab(PlaceHolder.minor);
    }

    /**
     * @return the headers
     */
    public String getHeaders() {
        return getFromRequestMab(PlaceHolder.headers);
    }

    void setHeaders(String raw) {
        int start = raw.indexOf("\r\n") + 2;
//		int end = raw.lastIndexOf("*\r\n");		// can't get it
        requestMap.put(PlaceHolder.headers, raw.substring(start));
    }

    /**
     * @return the body
     */
    public String getBody() {
        return getFromRequestMab(PlaceHolder.body);
    }

    /**
     * @return the ua
     */
    public String getUa() {
        return getFromRequestMab(PlaceHolder.ua);
    }

    /**
     * @return the header_host
     */
    public String getHeader_host() {
        return getFromRequestMab(PlaceHolder.header_host);
    }

    /**
     * @return the referer
     */
    public String getReferer() {
        return getFromRequestMab(PlaceHolder.referer);
    }

    /**
     * @return the netData
     */
    public String getNetData() {
        return getFromRequestMab(PlaceHolder.netData);
    }

    /**
     * @return the auth
     */
    public String getAuth() {
        return getFromRequestMab(PlaceHolder.auth);
    }

    /**
     * @return the connection
     */
    public String getConnection() {
        return getFromRequestMab(PlaceHolder.connection);
    }

    /**
     * @return the cookie
     */
    public String getCookie() {
        return getFromRequestMab(PlaceHolder.cookie);
    }

    /**
     * @return the keep_alive
     */
    public String getKeep_alive() {
        return getFromRequestMab(PlaceHolder.keep_alive);
    }

    /**
     * @return the cache_control
     */
    public String getCache_control() {
        return getFromRequestMab(PlaceHolder.cache_control);
    }

    /**
     * @return the rotate
     */
    public String getRotate() {
        return getFromRequestMab(PlaceHolder.rotate);
    }

    /**
     * @return the x_online_host
     */
    public String getX_online_host() {
        return getFromRequestMab(PlaceHolder.x_online_host);
    }

    /**
     * @return the x_forward_host
     */
    public String getX_forward_host() {
        return getFromRequestMab(PlaceHolder.x_forward_host);
    }

    /**
     * @return the x_forwarded_for
     */
    public String getX_forwarded_for() {
        return getFromRequestMab(PlaceHolder.x_forwarded_for);
    }


}

