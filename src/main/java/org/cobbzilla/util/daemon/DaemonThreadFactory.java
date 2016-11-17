package org.cobbzilla.util.daemon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory implements ThreadFactory {

    public static final DaemonThreadFactory instance = new DaemonThreadFactory();

    @Override public Thread newThread(Runnable r) {
        final Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }

    public static ExecutorService pool (int count) { return Executors.newFixedThreadPool(count, instance); }

}
