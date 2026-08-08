package org.injector.tools.proxy.handler;

import org.injector.tools.proxy.handler.nio.NioSslClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface ReadWriteOperation {

     Logger log = LoggerFactory.getLogger(ReadWriteOperation.class);

    void syncData();

    static ReadWriteOperation create(SocketChannel input, SocketChannel output) {
        return () -> {
            ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
            try {
                int len = -2;
                while ((len = input.read(buffer)) > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        output.write(buffer);
                    }
                    buffer.clear();
                }
                if (len == -1) {
                    input.close();
                    output.close();
                }
            } catch (Exception e) {
                log.error("Exception", e);
//                try {input.close();output.close();} catch (IOException ignored) {}
//                throw new RuntimeException(e);
            }
        };
    }

    static ReadWriteOperation create(SocketChannel input, NioSslClient output) {
        return () -> {
            ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
            try {
                int len;
                while ((len = input.read(buffer)) > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        output.write(buffer);
                    }
                    buffer.clear();
                }
                if (len == -1) {
                    input.close();
                    output.close();
                }
            } catch (Exception e) {
                log.error("Exception", e);
//                try {input.close();output.close();} catch (IOException ignored) {}
//                throw new RuntimeException(e);
            }
        };
    }

    static ReadWriteOperation create(NioSslClient input, SocketChannel output) {
        return () -> {
            try {
                input.read(buffer -> {
                    try {
                        while (buffer.hasRemaining()) {
                            output.write(buffer);
                        }
                    } catch (Exception e) {
                        log.error("Exception", e);
//                        try {input.close();output.close();} catch (IOException ignored) {}
//                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                log.error("Exception", e);
//                try {input.close();output.close();} catch (IOException ignored) {}
//                throw new RuntimeException(e);
            }
        };
    }

}
