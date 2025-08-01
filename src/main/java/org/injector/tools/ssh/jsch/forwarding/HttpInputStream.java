package org.injector.tools.ssh.jsch.forwarding;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class HttpInputStream extends InputStream {

    public byte[] requestBytes;
    public int length;
    InputStream in;
    String requestData;
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    private String host, port;
    private String headstr = null;
    private int readint;

    public HttpInputStream(InputStream in) {
        super();
        this.in = in;
        requestBytes = new byte[8 * 1024];
        readClientRequest();
    }

    private void readClientRequest() {
        try {
            length = in.read(requestBytes);
            if (length == -1) return;
            requestBytes = Arrays.copyOfRange(requestBytes, 0, length);
            Scanner scanner = new Scanner(new String(requestBytes, StandardCharsets.UTF_8));
            requestData = scanner.nextLine();
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

        } catch (IOException ignored) {}
        checkHostPort();
    }

    // h t t p s : / / h o s t : p o r t /
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

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public int getPortInt() {
        try {
            return Integer.parseInt(getPort());
        } catch (NumberFormatException e) {
            // 80 as no https throw this exception  -- still can be any other port
            return 80;
        }
    }

    public String getMethod() {
        return requestData.split(" ")[0];
    }

    public String getUrl() {
        return requestData.split(" ")[1];
    }

    public String getProtocol() {
        return requestData.split(" ")[2];
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

    public String getHeadersStr() {
        try {
            headstr = "";
            headers.forEach((t, u) -> {
                u.forEach((s1) -> {
                    headstr += t + ": " + s1;
                });
            });
            return headstr;
        } finally {
            headstr = null;
        }
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public int read() throws IOException {
        if (readint < requestBytes.length) {
//			System.out.print((char)reqByte[count - length] );
            return requestBytes[readint++];
        } else if (readint == requestBytes.length) {
            readint++;
            return -1;
        }
        return in.read();
    }


}
