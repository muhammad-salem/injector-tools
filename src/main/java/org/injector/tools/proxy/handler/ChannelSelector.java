package org.injector.tools.proxy.handler;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.injector.tools.proxy.LocalProxy;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Setter
@Getter
public class ChannelSelector implements Closeable {

    private ExecutorService service;
    private Selector selector;

    public ChannelSelector() throws IOException {
        this.service = Executors.newVirtualThreadPerTaskExecutor();
        this.selector = Selector.open();
    }

    public ChannelSelector(ExecutorService service) throws IOException {
        this.service = service;
        this.selector = Selector.open();
    }

    public ChannelSelector(ExecutorService service, Selector selector) {
        this.selector = selector;
        this.service = service;
    }


    public void startSelector() {
        try {
            Thread thread = new Thread(this::startSelectorProcess, "SelectorProcess");
            thread.setDaemon(true);
            thread.start();
//            service.execute(this::startSelectorProcess);
        } catch (Exception e) {
            log.error("Error start daemon selector thread", e);
        }
    }

    public void resetSelector() throws IOException {
        if (selector != null) closeSelector();
        selector = Selector.open();
    }


    public void closeSelector() throws IOException {
        if (selector != null) selector.close();
    }

    public void startSelectorProcess() {
        while (true) {
            try {
                // block thread
                selector.select(100);
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    log.info("select key, isValid: {}", key.isValid());
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        acceptKey(key);
                    } else if (key.isReadable()) {
                        readKey(key);
                    } else if (key.isWritable()) {
                        writeKey(key);
                    } else if (key.isConnectable()) {
                        connectKey(key);
                    }
                }
            } catch (Exception e) {
                log.error("Error Message", e);
            }
        }
    }

    protected void connectKey(SelectionKey key) {
        log.info("connectKey");
        try {
            var channel = (SocketChannel) key.channel();
            channel.finishConnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void acceptKey(SelectionKey key) {
        try (ServerSocketChannel server = (ServerSocketChannel) key.channel()) {
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            LocalProxy localProxy = (LocalProxy) key.attachment();
//            client.register(selector, SelectionKey.OP_READ, localProxy);
            localProxy.handle(client);
//            service.submit(() -> localProxy.handle(client));
            log.info("accept connection {}", client.getRemoteAddress().toString());
        } catch (IOException e) {
            log.error("local proxy server error", e);
        }
    }

    protected void readKey(SelectionKey key) {
        if (key.attachment() instanceof SocketChannel) {
            readSocketChannelKey(key);
        } else if(key.attachment() instanceof ReadWriteOperation) {
            handleReadWriteOperation(key);;
        } else if (key.attachment() instanceof LocalProxy) {
            readLocalProxyClient(key);
        }
    }

    protected void handleReadWriteOperation(SelectionKey key) {
        log.info("call syncData");
        try {
            ReadWriteOperation operation = (ReadWriteOperation) key.attachment();
            operation.syncData();
        } catch (Exception e) {
            log.error("handleReadWriteOperation", e);
        }
    }

    protected void readLocalProxyClient(SelectionKey key) {
        service.execute(() -> {
            ( (LocalProxy)key.attachment() ).handle((SocketChannel) key.channel());
        });
    }

    protected void readSocketChannelKey(SelectionKey key) {
        SocketChannel input = (SocketChannel) key.channel();
        SocketChannel output = (SocketChannel) key.attachment();
        readChannel(input, output);
    }

    protected void readChannel(ReadableByteChannel input , WritableByteChannel output) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(200 * 1024);
            while (input.read(buffer) > 0) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    output.write(buffer);
                }
                buffer.clear();
            }
        } catch (IOException e) {
            log.error("error read/write operation, " + e.getMessage());
        }
    }

    protected void writeKey(SelectionKey key) {
        if (key.attachment() instanceof SocketChannel) {
            writeSocketChannelKey(key);
        }
    }

    protected void writeSocketChannelKey(SelectionKey key) {
        SocketChannel input = (SocketChannel) key.channel();
        SocketChannel output = (SocketChannel) key.attachment();
        readChannel(input, output);
    }

    @Override
    public void close() throws IOException {
        closeSelector();
        service.shutdown();
    }

}
