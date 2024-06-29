package org.injector.tools.ssh.proxyhandler;

import com.jcraft.jsch.JSchException;
import org.injector.tools.speed.NetworkMonitorSpeed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Socks5Proxy extends HttpProxy {

    public Socks5Proxy(String proxyHost, int proxyPort) {
        super(proxyHost, proxyPort);
    }

    public Socks5Proxy(String proxyHost, int proxyPort, NetworkMonitorSpeed monitorSpeed) {
        super(proxyHost, proxyPort, monitorSpeed);
    }

    public Socks5Proxy(String proxyHost, int proxyPort, String proxyUser, String proxyPass,
                       NetworkMonitorSpeed monitorSpeed) {
        super(proxyHost, proxyPort, proxyUser, proxyPass, monitorSpeed, null);
    }

    public Socks5Proxy(String proxyHost, int proxyPort, String proxyUser, String proxyPass,
                       NetworkMonitorSpeed monitorSpeed, String[] headers) {
        super(proxyHost, proxyPort, proxyUser, proxyPass, monitorSpeed, headers);
    }


    @Override
    public Socket openSocketConnection(String hostname, int port, int timeout) throws IOException, JSchException {
        Socket socket5;
        try {
            socket5 = new Socket(proxyHost, proxyPort);

            InputStream in = socket5.getInputStream();
            OutputStream out = socket5.getOutputStream();

            if (timeout > 0) {
                socket5.setSoTimeout(timeout);
            }
            socket5.setTcpNoDelay(true);

            byte[] buf = new byte[1024];
            int index = 0;

		/*
		                   +----+----------+----------+
		                   |VER | METHODS | METHODS  |
		                   +----+----------+----------+
		                   | 1  |    1     | 1 to 255 |
		                   +----+----------+----------+

		   The VER field is set to X'05' for this version of the protocol.  The
		   METHODS field contains the number of method identifier octets that
		   appear in the METHODS field.

		   The values currently defined for METHOD are:

		          o  X'00' NO AUTHENTICATION REQUIRED
		          o  X'01' GSSAPI
		          o  X'02' USERNAME/PASSWORD
		          o  X'03' to X'7F' IANA ASSIGNED
		          o  X'80' to X'FE' RESERVED FOR PRIVATE METHODS
		          o  X'FF' NO ACCEPTABLE METHODS
		*/

            buf[index++] = 5;

            buf[index++] = 2;
            buf[index++] = 0;           // NO AUTHENTICATION REQUIRED
            buf[index++] = 2;           // USERNAME/PASSWORD

            out.write(buf, 0, index);

		/*
		    The server selects from one of the methods given in METHODS, and
		    sends a METHOD selection message:

		                         +----+--------+
		                         |VER | METHOD |
		                         +----+--------+
		                         | 1  |   1    |
		                         +----+--------+
		*/
            //in.read(buf, 0, 2);
            fill(in, buf, 2);

            boolean check = false;
            switch ((buf[1]) & 0xff) {
                case 0:                // NO AUTHENTICATION REQUIRED
                    check = true;
                    break;
                case 2:                // USERNAME/PASSWORD
                    if (proxyUser == null || proxyPass == null) break;

		/*
		   Once the SOCKS V5 server has started, and the client has selected the
		   Username/Password Authentication protocol, the Username/Password
		   subnegotiation begins.  This begins with the client producing a
		   Username/Password request:

		           +----+------+----------+------+----------+
		           |VER | ULEN |  UNAME   | PLEN |  PASSWD  |
		           +----+------+----------+------+----------+
		           | 1  |  1   | 1 to 255 |  1   | 1 to 255 |
		           +----+------+----------+------+----------+

		   The VER field contains the current version of the subnegotiation,
		   which is X'01'. The ULEN field contains the length of the UNAME field
		   that follows. The UNAME field contains the username as known to the
		   source operating system. The PLEN field contains the length of the
		   PASSWD field that follows. The PASSWD field contains the password
		   association with the given UNAME.
		*/
                    index = 0;
                    buf[index++] = 1;
                    buf[index++] = (byte) (proxyUser.length());
                    System.arraycopy(str2byte(proxyUser), 0, buf, index, proxyUser.length());
                    index += proxyUser.length();
                    buf[index++] = (byte) (proxyPass.length());
                    System.arraycopy(str2byte(proxyPass), 0, buf, index, proxyPass.length());
                    index += proxyPass.length();

                    out.write(buf, 0, index);

		/*
		   The server verifies the supplied UNAME and PASSWD, and sends the
		   following response:

		                        +----+--------+
		                        |VER | STATUS |
		                        +----+--------+
		                        | 1  |   1    |
		                        +----+--------+

		   A STATUS field of X'00' indicates success. If the server returns a
		   `failure' (STATUS value other than X'00') status, it MUST close the
		   connection.
		*/
                    //in.read(buf, 0, 2);
                    fill(in, buf, 2);
                    if (buf[1] == 0)
                        check = true;
                    break;
                default:
            }

            if (!check) {
                try {
                    socket5.close();
                } catch (Exception eee) {
                }
                throw new JSchException("fail in SOCKS5 proxy");
            }

		/*
		      The SOCKS request is formed as follows:

		        +----+-----+-------+------+----------+----------+
		        |VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
		        +----+-----+-------+------+----------+----------+
		        | 1  |  1  | X'00' |  1   | Variable |    2     |
		        +----+-----+-------+------+----------+----------+

		      Where:

		      o  VER    protocol version: X'05'
		      o  CMD
		         o  CONNECT X'01'
		         o  BIND X'02'
		         o  UDP ASSOCIATE X'03'
		      o  RSV    RESERVED
		         o  ATYP   address type of following address
		         o  IP V4 address: X'01'
		         o  DOMAINNAME: X'03'
		         o  IP V6 address: X'04'
		      o  DST.ADDR       desired destination address
		      o  DST.PORT desired destination port in network octet
		         order
		*/

            index = 0;
            buf[index++] = 5;
            buf[index++] = 1;       // CONNECT
            buf[index++] = 0;

            byte[] hostb = str2byte(hostname);
            int len = hostb.length;
            buf[index++] = 3;      // DOMAINNAME
            buf[index++] = (byte) (len);
            System.arraycopy(hostb, 0, buf, index, len);
            index += len;
            buf[index++] = (byte) (port >>> 8);
            buf[index++] = (byte) (port & 0xff);

            out.write(buf, 0, index);

		/*
		   The SOCKS request information is sent by the client as soon as it has
		   established a connection to the SOCKS server, and completed the
		   authentication negotiations.  The server evaluates the request, and
		   returns a reply formed as follows:

		        +----+-----+-------+------+----------+----------+
		        |VER | REP |  RSV  | ATYP | BND.ADDR | BND.PORT |
		        +----+-----+-------+------+----------+----------+
		        | 1  |  1  | X'00' |  1   | Variable |    2     |
		        +----+-----+-------+------+----------+----------+

		   Where:

		   o  VER    protocol version: X'05'
		   o  REP    Reply field:
		      o  X'00' succeeded
		      o  X'01' general SOCKS server failure
		      o  X'02' connection not allowed by ruleset
		      o  X'03' Network unreachable
		      o  X'04' Host unreachable
		      o  X'05' Connection refused
		      o  X'06' TTL expired
		      o  X'07' Command not supported
		      o  X'08' Address type not supported
		      o  X'09' to X'FF' unassigned
		    o  RSV    RESERVED
		    o  ATYP   address type of following address
		      o  IP V4 address: X'01'
		      o  DOMAINNAME: X'03'
		      o  IP V6 address: X'04'
		    o  BND.ADDR       server bound address
		    o  BND.PORT       server bound port in network octet order
		*/

            //in.read(buf, 0, 4);
            fill(in, buf, 4);

            if (buf[1] != 0) {
                try {
                    socket5.close();
                } catch (Exception eee) {
                }
                throw new JSchException("ProxySOCKS5: server returns " + buf[1]);
            }

            switch (buf[3] & 0xff) {
                case 1:
                    //in.read(buf, 0, 6);
                    fill(in, buf, 6);
                    break;
                case 3:
                    //in.read(buf, 0, 1);
                    fill(in, buf, 1);
                    //in.read(buf, 0, buf[0]+2);
                    fill(in, buf, (buf[0] & 0xff) + 2);
                    break;
                case 4:
                    //in.read(buf, 0, 18);
                    fill(in, buf, 18);
                    break;
                default:
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            try {
                if (socket != null) socket.close();
            } catch (Exception eee) {
            }
            String message = "ProxySOCKS5: " + e;
            if (e instanceof Throwable)
                throw new JSchException(message, e);
            throw new JSchException(message);
        }
        return socket5;
    }


    protected byte[] str2byte(String str, String encoding) {
        if (str == null)
            return null;
        try {
            return str.getBytes(encoding);
        } catch (java.io.UnsupportedEncodingException e) {
            return str.getBytes();
        }
    }

    protected byte[] str2byte(String str) {
        return str2byte(str, "UTF-8");
    }

    protected void fill(InputStream in, byte[] buf, int len) throws JSchException, IOException {
        int s = 0;
        while (s < len) {
            int i = in.read(buf, s, len - s);
            if (i <= 0) {
                throw new JSchException("ProxySOCKS5: stream is closed");
            }
            s += i;
        }
    }


}
