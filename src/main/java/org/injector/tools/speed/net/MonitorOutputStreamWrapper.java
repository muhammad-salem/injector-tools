package org.injector.tools.speed.net;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Youchao Feng
 * @version 1.0
 * @date Sep 21, 2015 2:34 PM
 */
public class MonitorOutputStreamWrapper extends OutputStream {

    private OutputStream originalOutputStream;

    private List<OutputStreamMonitor> monitors;

    public MonitorOutputStreamWrapper(OutputStream outputStream, OutputStreamMonitor... monitors) {
        this.originalOutputStream = outputStream;
        this.monitors = new ArrayList<>(monitors.length);
        Collections.addAll(this.monitors, monitors);
    }

    public MonitorOutputStreamWrapper(OutputStream outputStream, List<OutputStreamMonitor> monitors) {
        this.originalOutputStream = outputStream;
        this.monitors = monitors;
    }

    public static OutputStream wrap(OutputStream outputStream, OutputStreamMonitor... monitors) {
        return new MonitorOutputStreamWrapper(outputStream, monitors);
    }

    public static OutputStream wrap(OutputStream outputStream, List<OutputStreamMonitor> monitors) {
        return new MonitorOutputStreamWrapper(outputStream, monitors);
    }

    public MonitorOutputStreamWrapper addMonitor(OutputStreamMonitor monitor) {
        if (monitors == null) {
            monitors = new ArrayList<>(1);
        }
        monitors.add(monitor);
        return this;
    }

    public MonitorOutputStreamWrapper removeMonitor(OutputStreamMonitor monitor) {
        if (monitors != null) {
            monitors.remove(monitor);
        }
        return this;
    }

    public OutputStream getOriginalOutputStream() {
        return originalOutputStream;
    }

    public void setOriginalOutputStream(OutputStream originalOutputStream) {
        this.originalOutputStream = originalOutputStream;
    }

    @Override
    public void write(int b) throws IOException {
        originalOutputStream.write(b);
//    byte[] bytes = {(byte) b};
//    informMonitor(bytes);
        informMonitor(1);
    }

    public List<OutputStreamMonitor> getMonitors() {
        return monitors;
    }

    public void setMonitors(List<OutputStreamMonitor> monitors) {
        this.monitors = monitors;
    }

    @Override
    public void close() throws IOException {
        originalOutputStream.close();
    }

    @Override
    public void flush() throws IOException {
        originalOutputStream.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        originalOutputStream.write(b, off, len);
//    informMonitor(b, off, len);
        informMonitor(len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        originalOutputStream.write(b);
//    informMonitor(b);
        informMonitor(b.length);

    }

    private void informMonitor(int len) {
        if (monitors != null) {
            for (OutputStreamMonitor monitor : monitors) {
                monitor.onWrite(len);
            }
        }
    }

//  private void informMonitor(byte[] bytes) {
//    if (monitors != null) {
//      for (OutputStreamMonitor monitor : monitors) {
//        monitor.onWrite(bytes);
//      }
//    }
//  }
//
//  private void informMonitor(byte[] bytes, int off, int length) {
//    if (monitors != null) {
//    	byte[] b = Arrays.copyOfRange(bytes, off, off + length);
//    	for (OutputStreamMonitor monitor : monitors) {
//    		monitor.onWrite(b);
//    	}
//    }
//  }
}
