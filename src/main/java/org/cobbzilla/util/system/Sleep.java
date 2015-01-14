package org.cobbzilla.util.system;

public class Sleep {

    public static void sleep (long millis) {
        sleep(millis, "no reason for sleep given", null);
    }

    public static void sleep (long millis, String reason) {
        sleep(millis, reason, null);
    }

    public static void sleep (long millis, Exception cause) {
        sleep(millis, null, cause);
    }

    public static void sleep (long millis, String reason, Exception cause) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new IllegalStateException("sleep interrupted ("+reason+")", cause);
        }
    }
}
