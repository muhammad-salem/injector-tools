package org.injector.tools.speed;


public class NetworkMonitorSpeed extends SpeedMonitor implements Runnable {

    protected long timer = 0;
    protected boolean showSpeed = true;

    public void start() {
//        Thread t = new Thread(this);
        showSpeed = true;
        timer = 0;
//        t.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (showSpeed)
                    System.out.println("\n\n");
            }
        }));
        run();
    }

    public void stop() {
        showSpeed = false;
    }

    public long getTimer() {
        return timer;
    }

    @Override
    public void run() {

    }


}
