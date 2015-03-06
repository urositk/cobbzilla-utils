package org.cobbzilla.util.daemon;

import lombok.extern.slf4j.Slf4j;

import static org.cobbzilla.util.system.Sleep.sleep;

/**
 * the Zilla doesn't mess around.
 */
@Slf4j
public class ZillaRuntime {

    public static void terminate(Thread thread, long timeout) {
        if (thread == null || !thread.isAlive()) return;
        thread.interrupt();
        long start = System.currentTimeMillis();
        while (thread.isAlive() && System.currentTimeMillis() - start < timeout) {
            sleep(100, "terminate: waiting for thread to die: "+thread);
        }
        if (thread.isAlive()) {
            log.warn("terminate: thread did not die voluntarily, killing it: "+thread);
            thread.stop();
        }
    }

    public static <T> T die (String message) { throw new IllegalStateException(message); }

    public static <T> T die (String message, Exception e) { throw new IllegalStateException(message, e); }

    public static <T> T die (Exception e) { throw new IllegalStateException("(no message)", e); }

}
