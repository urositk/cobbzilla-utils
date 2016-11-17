package org.cobbzilla.util.daemon;

import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory implements ThreadFactory {

    public static final DaemonThreadFactory instance = new DaemonThreadFactory();

    @Override public Thread newThread(Runnable r) {
        final Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }

}
