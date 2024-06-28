package org.injector.tools.proxy.handler;

import org.injector.tools.log.Logger;
import org.injector.tools.proxy.LocalProxy;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChannelSelector implements Closeable {

    private ExecutorService service;
    private Selector selector;

    public ChannelSelector() throws IOException {
        this.service = Executors.newCachedThreadPool();
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

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public ExecutorService getService() {
        return service;
    }

    public void setService(ExecutorService service) {
        this.service = service;
    }


    public void startSelector() {
        try {
            service.execute(this::startSelectorProcess);
        } catch (Exception e) {
            Logger.debug(e.getClass(), "Message ", e.getMessage());
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
                if (selector.select() > 0) {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    do {
                        SelectionKey key = iterator.next();

                        if (key.isValid() && key.isReadable()) {
                            readKey(key);
                        } else if (key.isValid() && key.isAcceptable()) {
                            acceptKey(key);
                        } else if (key.isValid() && key.isWritable()) {
                            writeKey(key);
                        } else if (key.isValid() && key.isConnectable()) {
                            connectKey(key);
                        }

                        iterator.remove();
                    } while (iterator.hasNext());

                } else {
                    TimeUnit.MILLISECONDS.sleep(100);
                }
            } catch (IOException e) {
                Logger.debug(e.getClass(), "Message ", e.getMessage());
            } catch (InterruptedException e) {
                Logger.debug(e.getClass(), "Message ", e.getMessage());
            }


        }
    }

    protected void connectKey(SelectionKey key) {
        Logger.debug(getClass(), "connectKey");
    }

    protected void writeKey(SelectionKey key) {
        Logger.debug(getClass(), "writeKey");
    }

    protected void acceptKey(SelectionKey key) {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            SocketChannel client = server.accept();
            Logger.debug(getClass(), "accept connection", client.getRemoteAddress().toString());
            LocalProxy localProxy = (LocalProxy) key.attachment();
            service.execute(() -> {
                localProxy.handle(client);
            });
        } catch (IOException e) {
            Logger.debug(e.getClass(), "Message ", e.getMessage());
            Logger.debug(getClass(), "local proxy server  error");
        }
    }

    protected void readKey(SelectionKey key) {
        SocketChannel input = (SocketChannel) key.channel();
        SocketChannel output = (SocketChannel) key.attachment();
        ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);
        try {
            while (input.read(buffer) > 0) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    output.write(buffer);
                }
            }
        } catch (IOException e) {
            Logger.debug(getClass(), "read/write opr", e.getMessage());
        }


    }

    @Override
    public void close() throws IOException {
        closeSelector();
        service.shutdown();
    }


}
