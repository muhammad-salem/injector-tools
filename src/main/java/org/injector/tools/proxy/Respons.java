package org.injector.tools.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Respons {


    private String response;
    private String protocol;
    private int code;
    private String state;

    public static int readLineRN(InputStream is, byte[] buffer) throws IOException {
        int pos = 0;
        boolean need10 = false;
        int len = 0;
        while (true) {
            int c = is.read();
            if (c == -1)
                throw new IOException("Premature connection close");

            buffer[pos++] = (byte) c;

            if (c == 13) {
                need10 = true;
                len++;
                continue;
            }

            if (c == 10) {
                len++;
                break;
            }

            if (need10)
                throw new IOException("Malformed line sent by the server, the line does not end correctly.");

            len++;
            if (pos >= buffer.length)
                throw new IOException("The server sent a too long line.");
        }
        if (pos >= buffer.length)
            throw new IOException("The server sent a too long line.");
        return len;
    }

    /**
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String respone) {
        this.response = respone.split("\r\n")[0];
        protocol = response.substring(0, 8);
        try {
            code = Integer.valueOf(response.substring(9, 12));
        } catch (NumberFormatException e) {
//			e.fillInStackTrace();
        }
        state = response.substring(13);
    }

    public int readResponse(InputStream is, byte[] buffer) throws IOException {
        int len = readLineRN(is, buffer);
        response = new String(buffer, 0, len, StandardCharsets.ISO_8859_1);
        protocol = response.substring(0, 8);
        try {
            code = Integer.valueOf(response.substring(9, 12));
        } catch (NumberFormatException e) {
//			e.fillInStackTrace();
        }
        state = response.substring(13);
        return len;
    }

    public String readBody(InputStream is) throws IOException {
        byte[] buffer = new byte[1024];
        String lines = "";
        int bytes_read = 0;
        while (true) {
            bytes_read = readLineRN(is, buffer);
            if (bytes_read == 0)
                break;
            lines += new String(buffer, 0, bytes_read) + "\r\n";
        }
        return lines;
    }

    @Override
    public String toString() {

        return "Protocol: " + protocol
                + "\t Code: " + code
                + "\t State: " + state;
    }

}
