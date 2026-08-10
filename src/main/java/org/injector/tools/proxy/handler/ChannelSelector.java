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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Setter
@Getter
public class ChannelSelector implements Closeable {

    private final LocalProxy localProxy;
    private final Selector selector = Selector.open();
    private final Selector workerSelector = Selector.open();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Queue<SocketChannel> registerQueue = new ConcurrentLinkedQueue<>();

    public ChannelSelector(LocalProxy localProxy) throws IOException {
        this.localProxy = localProxy;
    }

    public void startSelector() {
        try {
            this.executor.submit(this::startConnectSelector);
            this.executor.submit(this::startReadWriteSelector);
        } catch (Exception e) {
            log.error("Error start daemon selector thread", e);
        }
    }

    private void startConnectSelector() {
        while (true) {
            try {
                // block thread
                selector.select(50);
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
                    } else if (key.isConnectable()) {
                        connectKey(key);
                    }
                }
            } catch (Exception e) {
                log.error("Error Message", e);
            }
        }
    }

    private void startReadWriteSelector() {
        while (true) {
            try {
                // 1. Process pending registrations before blocking on select()
                processRegistrations();
                // 2. Block until I/O events are ready
                workerSelector.select(50);
                Iterator<SelectionKey> iterator = workerSelector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    log.info("select key, isValid: {}", key.isValid());
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isReadable()) {
                        readKey(key);
                    } else if (key.isWritable()) {
                        writeKey(key);
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
            registerChannel(client);
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
        executor.execute(() -> {
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
            log.error("error read/write operation", e);
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

    public void registerChannel(SocketChannel channel) {
        registerQueue.add(channel);
        workerSelector.wakeup(); // Wake up select() to process the queue immediately
    }

    private void processRegistrations() {
        SocketChannel channel;
        while ((channel = registerQueue.poll()) != null) {
            // Register the channel to THIS worker's selector for reading
            this.localProxy.handle(channel);
        }
    }

    @Override
    public void close() throws IOException {
        selector.close();
        workerSelector.close();
        executor.shutdown();
    }

}
