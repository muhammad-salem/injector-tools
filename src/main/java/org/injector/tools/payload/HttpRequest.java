package org.injector.tools.payload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class HttpRequest {

    public byte[] requestBytes;
    public int length;
    String requestData;
    RequestLine requestLine;
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    private String headerString = null;
    public HttpRequest() {
        requestBytes = new byte[8 * 1024];
    }

    public HttpRequest(InputStream in) {
        this();
        readClientRequest(in);
    }

    public HttpRequest(String request) {
        this();
        readClientRequest(request.getBytes());
    }

    public void readClientRequest(InputStream in) {
        try {
            length = in.read(requestBytes);
            if (length == -1) return;
            requestBytes = Arrays.copyOfRange(requestBytes, 0, length);
            readClientRequest(requestBytes);
        } catch (Exception e) {

        }
    }

    private void readClientRequest(byte[] bytes) {
        Scanner scanner = new Scanner(new String(bytes, StandardCharsets.UTF_8));
        requestData = scanner.nextLine();
        requestLine = new RequestLine(requestData);
        System.out.println(requestData);
        String temp;
        while (scanner.hasNext()) {
            temp = scanner.nextLine();
            if (temp.equals("")) continue;
            String[] head = temp.split(": ");
            if (!headers.containsKey(head[0])) {

                ArrayList<String> list = new ArrayList<>();
                if (head.length == 2) {
                    //  Opened hole is her have to remove to be secure, only allow valid header not null
                    list.add(head[1]);
                } else if (head.length == 1) {
                    list.add("");
                } else {
                    //ignore case - more than regex
                    // throw not valid Exception
                }
                headers.put(head[0], list);
            } else {
                List<String> list = headers.get(head[0]);
                if (head.length == 2) {
                    list.add(head[1]);
                } else if (head.length == 1) {
                    //  Opened hole is her have to remove to be secure, only allow valid header not null
                    list.add("");
                } else {
                    //ignore case - more than regex
                    // throw not valid Exception
                }
                headers.put(head[0], list);
            }


        }
        scanner.close();


    }

    public String getHeadersStr() {
        try {
            headerString = "";
            headers.forEach((t, u) -> {
                u.forEach((s1) -> {
                    headerString += t + ": " + s1;
                });
            });
            return headerString;
        } finally {
            headerString = null;
        }
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public RequestLine getRequestLine() {
        return requestLine;
    }

    public String getRequest() {
        return requestLine.getRequest();
    }

    public String getHost() {
        return requestLine.getHost();
    }

    public String getPort() {
        return requestLine.getPort();
    }

    public int getPortInt() {
        return requestLine.getPortInt();
    }

    public String getMethod() {
        return requestLine.getMethod();
    }

    public String getUrl() {
        return requestLine.getUrl();
    }

    public String getProtocol() {
        return requestLine.getProtocol();
    }

    public String getMajor() {
        return requestLine.getMajor();
    }

    public String getMinor() {
        return requestLine.getMinor();
    }


    public InputStream createInputStreamFromRequest(InputStream in) {
        InputStream request_in = new InputStream() {
            int count = 0;

            @Override
            public int read() throws IOException {
                if (count > length) {
                    return in.read();
                }
                if (count < length) {
                    return requestBytes[count++];
                } else if (count == length) {
                    count++;
                    return -1;
                }
                return in.read();
            }
        };
        return request_in;
    }

}
