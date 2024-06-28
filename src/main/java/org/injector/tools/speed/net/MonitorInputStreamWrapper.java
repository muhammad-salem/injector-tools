package org.injector.tools.speed.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Youchao Feng
 * @version 1.0
 * @date Sep 21, 2015 2:31 PM
 */
public class MonitorInputStreamWrapper extends InputStream {

    private final InputStream originalInputStream;
    private List<InputStreamMonitor> monitors;

    public MonitorInputStreamWrapper(InputStream inputStream) {
        this.originalInputStream = inputStream;
    }

    public MonitorInputStreamWrapper(InputStream inputStream, InputStreamMonitor... monitors) {
        this.originalInputStream = inputStream;
        this.monitors = new ArrayList<>(monitors.length);
        Collections.addAll(this.monitors, monitors);
    }

    public MonitorInputStreamWrapper(InputStream inputStream, List<InputStreamMonitor> monitors) {
        this.originalInputStream = inputStream;
        this.monitors = monitors;
    }

    public static InputStream wrap(InputStream inputStream, InputStreamMonitor... monitors) {
        return new MonitorInputStreamWrapper(inputStream, monitors);
    }

    public static InputStream wrap(InputStream inputStream, List<InputStreamMonitor> monitors) {
        return new MonitorInputStreamWrapper(inputStream, monitors);
    }

    public InputStream getOriginalInputStream() {
        return originalInputStream;
    }

    public MonitorInputStreamWrapper addMonitor(InputStreamMonitor monitor) {
        if (monitors == null) {
            monitors = new ArrayList<>(1);
        }
        monitors.add(monitor);
        return this;
    }

    public MonitorInputStreamWrapper removeMonitor(InputStreamMonitor monitor) {
        if (monitors != null) {
            monitors.remove(monitor);
        }
        return this;
    }

    @Override
    public int read() throws IOException {
        int b = originalInputStream.read();
//    byte[] array = {(byte) b};
        informMonitor(1);
        return b;
    }

    public List<InputStreamMonitor> getMonitors() {
        return monitors;
    }

    public void setMonitors(List<InputStreamMonitor> monitors) {
        this.monitors = monitors;
    }

    @Override
    public void close() throws IOException {
        originalInputStream.close();
    }

    @Override
    public int available() throws IOException {
        return originalInputStream.available();
    }

    @Override
    public long skip(long n) throws IOException {
        return originalInputStream.skip(n);
    }

    @Override
    public synchronized void reset() throws IOException {
        originalInputStream.reset();
    }

    @Override
    public synchronized void mark(int readLimit) {
        originalInputStream.mark(readLimit);
    }

    @Override
    public boolean markSupported() {
        return originalInputStream.markSupported();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int length = originalInputStream.read(b);
        if (length > 0) {
            informMonitor(length);
        }
        return length;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int length = originalInputStream.read(b, off, len);
        if (length > 0) {
            informMonitor(length);
        }
        return length;
    }


    private void informMonitor(int len) {
        if (monitors != null) {
            for (InputStreamMonitor monitor : monitors) {
                monitor.onRead(len);
            }
        }
    }
//  private void informMonitor(byte[] bytes) {
//    if (monitors != null) {
//      for (InputStreamMonitor monitor : monitors) {
//        monitor.onRead(bytes);
//      }
//    }
//  }

//  private void informMonitor(byte[] bytes, int off, int len) {
//    if (monitors != null) {
//    	byte[] b = Arrays.copyOfRange(bytes, off, off + len);
//    	for (InputStreamMonitor monitor : monitors) {  
//    		monitor.onRead(b);
//    	}
//    }
//  }
}
