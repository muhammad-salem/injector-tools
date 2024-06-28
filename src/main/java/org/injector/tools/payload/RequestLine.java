package org.injector.tools.payload;

import lombok.Getter;

@Getter
public class RequestLine {
    String request;
    private String host, port;

    public RequestLine(String httRequestLine) {
        this.request = httRequestLine;
        checkHostPort();
    }

    public void setRequest(String request) {
        this.request = request;
        checkHostPort();
    }

    // h t t p s : / / h o s t : p o r t /
    // 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0

    // h t t p : / / h o s t : p o r t /
    // 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0
    private void checkHostPort() {
        String url = getUrl();
        int startIndex = url.indexOf("//");
        startIndex = startIndex == -1 ? 0 : startIndex + 2;
        int endIndex = url.indexOf('/', startIndex);
        endIndex = endIndex == -1 ? url.length() : endIndex;

        String host_port = url.substring(startIndex, endIndex);
        if (host_port.contains(":")) {
            int colindex = host_port.indexOf(':');
            host = host_port.substring(0, colindex);
            port = host_port.substring(colindex + 1);
        } else {
            host = host_port;
            port = (url.startsWith("https") ? "443" : "80");
        }


    }

    public int getPortInt() {
        try {
            return Integer.parseInt(getPort());
        } catch (NumberFormatException e) {
            e.fillInStackTrace();
            // 80 as no https throw this exception  -- still can be any other port
            return 80;
        }
    }

    public String getMethod() {
        return request.split(" ")[0];
    }

    public String getUrl() {
        return request.split(" ")[1];
    }

    public String getProtocol() {
        return request.split(" ")[2];
    }

    /**
     * @return the major
     */
    public String getMajor() {
        return getProtocol().charAt(5) + "";
    }

    /**
     * @return the minor
     */
    public String getMinor() {
        return getProtocol().charAt(7) + "";
    }

}
