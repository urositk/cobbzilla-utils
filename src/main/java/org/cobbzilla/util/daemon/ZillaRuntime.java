package org.cobbzilla.util.daemon;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import static org.cobbzilla.util.io.FileUtil.list;
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

    public static <T> T notSupported() { return notSupported("not supported"); }
    public static <T> T notSupported(String message) { throw new UnsupportedOperationException(message); }

    public static boolean empty(String s) { return s == null || s.length() == 0; }

    public static boolean empty(Object o) {
        if (o == null) return true;
        if (o instanceof Collection) return ((Collection)o).isEmpty();
        if (o instanceof Map) return ((Map)o).isEmpty();
        if (o instanceof File) {
            final File f = (File) o;
            return !f.exists() || f.length() == 0 || (f.isDirectory() && list(f).length == 0);
        }
        if (o.getClass().isArray()) {
            if (o.getClass().getComponentType().isPrimitive()) {
                switch (o.getClass().getComponentType().getName()) {
                    case "boolean": return ((boolean[]) o).length == 0;
                    case "byte": return ((byte[]) o).length == 0;
                    case "short": return ((short[]) o).length == 0;
                    case "char": return ((char[]) o).length == 0;
                    case "int": return ((int[]) o).length == 0;
                    case "long": return ((long[]) o).length == 0;
                    case "float": return ((float[]) o).length == 0;
                    case "double": return ((double[]) o).length == 0;
                    default: return o.toString().length() == 0;
                }
            } else {
                return ((Object[]) o).length == 0;
            }
        }
        return o.toString().length() == 0;
    }

    public static Boolean safeBoolean(String val) { return empty(val) ? null : Boolean.valueOf(val); }
    public static Integer safeInt(String val) { return empty(val) ? null : Integer.valueOf(val); }
    public static Long safeLong(String val) { return empty(val) ? null : Long.valueOf(val); }
}
