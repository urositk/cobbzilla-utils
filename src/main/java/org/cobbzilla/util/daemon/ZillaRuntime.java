package org.cobbzilla.util.daemon;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        long start = now();
        while (thread.isAlive() && now() - start < timeout) {
            sleep(100, "terminate: waiting for thread to die: "+thread);
        }
        if (thread.isAlive()) {
            log.warn("terminate: thread did not die voluntarily, killing it: "+thread);
            thread.stop();
        }
    }

    public static <T> T die(String message)              { throw new IllegalStateException(message); }
    public static <T> T die(String message, Exception e) { throw new IllegalStateException(message, e); }
    public static <T> T die(Exception e)                 { throw new IllegalStateException("(no message)", e); }

    public static <T> T notSupported()               { return notSupported("not supported"); }
    public static <T> T notSupported(String message) { throw new UnsupportedOperationException(message); }

    public static boolean empty(String s) { return s == null || s.length() == 0; }

    public static boolean empty(Object o) {
        if (o == null) return true;
        if (o instanceof Collection) return ((Collection)o).isEmpty();
        if (o instanceof Map) return ((Map)o).isEmpty();
        if (o instanceof Iterable) return !((Iterable)o).iterator().hasNext();
        if (o instanceof File) {
            final File f = (File) o;
            return !f.exists() || f.length() == 0 || (f.isDirectory() && list(f).length == 0);
        }
        if (o.getClass().isArray()) {
            if (o.getClass().getComponentType().isPrimitive()) {
                switch (o.getClass().getComponentType().getName()) {
                    case "boolean": return ((boolean[]) o).length == 0;
                    case "byte":    return ((byte[]) o).length == 0;
                    case "short":   return ((short[]) o).length == 0;
                    case "char":    return ((char[]) o).length == 0;
                    case "int":     return ((int[]) o).length == 0;
                    case "long":    return ((long[]) o).length == 0;
                    case "float":   return ((float[]) o).length == 0;
                    case "double":  return ((double[]) o).length == 0;
                    default: return o.toString().length() == 0;
                }
            } else {
                return ((Object[]) o).length == 0;
            }
        }
        return o.toString().length() == 0;
    }

    public static Boolean safeBoolean(String val, Boolean ifNull) { return empty(val) ? ifNull : Boolean.valueOf(val); }
    public static Boolean safeBoolean(String val) { return safeBoolean(val, null); }

    public static Integer safeInt(String val, Integer ifNull) { return empty(val) ? ifNull : Integer.valueOf(val); }
    public static Integer safeInt(String val) { return safeInt(val, null); }

    public static Long safeLong(String val, Long ifNull) { return empty(val) ? ifNull : Long.valueOf(val); }
    public static Long safeLong(String val) { return safeLong(val, null); }

    public static BigInteger bigint(long val) { return new BigInteger(String.valueOf(val)); }
    public static BigInteger bigint(int val) { return new BigInteger(String.valueOf(val)); }
    public static BigInteger bigint(byte val) { return new BigInteger(String.valueOf(val)); }

    public static BigDecimal big(String val) { return new BigDecimal(val); }
    public static BigDecimal big(double val) { return new BigDecimal(val); }
    public static BigDecimal big(float val) { return new BigDecimal(val); }
    public static BigDecimal big(long val) { return new BigDecimal(val); }
    public static BigDecimal big(int val) { return new BigDecimal(val); }
    public static BigDecimal big(byte val) { return new BigDecimal(String.valueOf(val)); }

    public static int percent(int value, double pct) { return percent(value, pct, RoundingMode.HALF_EVEN); }

    public static int percent(int value, double pct, RoundingMode rounding) {
        return big(value).multiply(big(pct)).setScale(0, rounding).intValue();
    }

    public static int percent(BigDecimal value, BigDecimal pct) {
        return percent(value.intValue(), pct.multiply(big(0.01)).doubleValue(), RoundingMode.HALF_EVEN);
    }

    public static String uuid() { return UUID.randomUUID().toString(); }

    public static long now() { return System.currentTimeMillis(); }

    public static <T> T pickRandom(T[] things) { return things[RandomUtils.nextInt(0, things.length)]; }
    public static <T> T pickRandom(List<T> things) { return things.get(RandomUtils.nextInt(0, things.size())); }

    public static BufferedReader stdin() { return new BufferedReader(new InputStreamReader(System.in)); }
    public static BufferedWriter stdout() { return new BufferedWriter(new OutputStreamWriter(System.out)); }

}
