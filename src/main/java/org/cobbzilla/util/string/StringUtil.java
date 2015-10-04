package org.cobbzilla.util.string;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.LocaleUtils;
import org.cobbzilla.util.security.MD5Util;
import org.cobbzilla.util.time.ImprovedTimezone;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class StringUtil {

    public static final String UTF8 = "UTF-8";
    public static final Charset UTF8cs = Charset.forName(UTF8);

    public static final String EMPTY = "";
    public static final String[] EMPTY_ARRAY = {};
    public static final String DEFAULT_LOCALE = "en_US";
    public static final String BYTES_PATTERN = "(\\d+)(\\s+)?([MgGgTtPpEe][Bb])";

    public static final Transformer XFORM_TO_STRING = new Transformer() {
        @Override public Object transform(Object o) { return String.valueOf(o); }
    };

    public static List<String> toStringCollection (Collection c) {
        return new ArrayList<>(CollectionUtils.collect(c, XFORM_TO_STRING));
    }

    public static String prefix(String s, int count) {
        return s == null ? null : s.length() > count ? s.substring(0, count) : s;
    }

    public static String packagePath(Class clazz) {
        return clazz.getPackage().getName().replace(".","/");
    }

    public static List<String> split (String s, String delim) {
        final StringTokenizer st = new StringTokenizer(s, delim);
        final List<String> results = new ArrayList<>();
        while (st.hasMoreTokens()) {
            results.add(st.nextToken());
        }
        return results;
    }

    public static String replaceLast(String s, String find, String replace) {
        if (empty(s)) return s;
        int lastIndex = s.lastIndexOf(find);
        if (lastIndex < 0) return s;
        return s.substring(0, lastIndex) + s.substring(lastIndex).replaceFirst(find, replace);
    }

    public static String lastPathElement(String url) { return url.substring(url.lastIndexOf("/")+1); }

    public static String safeFunctionName (String s) { return s.replaceAll("\\W", ""); }

    public static String removeWhitespace (String s) { return s.replaceAll("\\s", ""); }

    public static Integer safeParseInt(String s) {
        if (empty(s)) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String shortDateTime(String localeString, Integer timezone, long time) {
        return formatDateTime("SS", localeString, timezone, time);
    }

    public static String mediumDateTime(String localeString, Integer timezone, long time) {
        return formatDateTime("MM", localeString, timezone, time);
    }

    public static String fullDateTime(String localeString, Integer timezone, long time) {
        return formatDateTime("FF", localeString, timezone, time);
    }

    public static String formatDateTime(String style, String localeString, Integer timezone, long time) {
        final Locale locale = LocaleUtils.toLocale(localeString);
        final ImprovedTimezone tz = ImprovedTimezone.getTimeZoneById(timezone);
        return DateTimeFormat.forPattern(DateTimeFormat.patternForStyle(style, locale))
                .withZone(DateTimeZone.forTimeZone(tz.getTimezone())).print(time);
    }

    public static String trimQuotes (String s) {
        if (s == null) return s;
        while (s.startsWith("\"") || s.startsWith("\'")) s = s.substring(1);
        while (s.endsWith("\"") || s.endsWith("\'")) s = s.substring(0, s.length()-1);
        return s;
    }

    public static String getPackagePath(Class clazz) {
        return clazz.getPackage().getName().replace('.', '/');
    }

    public static String urlEncode (String s) {
        try {
            return URLEncoder.encode(s, UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("urlEncode: "+e, e);
        }
    }

    public static URI uriOrDie (String s) {
        try {
            return new URI(s);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("bad uri: "+e, e);
        }
    }

    public static String urlParameterize(Map<String, String> params) {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(urlEncode(entry.getKey()))
                    .append('=')
                    .append(urlEncode(entry.getValue()));
        }
        return sb.toString();
    }

    public static String toString (Collection c) { return toString(c, ","); }

    public static String toString (Collection c, String sep) {
        StringBuilder builder = new StringBuilder();
        for (Object o : c) {
            if (builder.length() > 0) builder.append(sep);
            builder.append(o);
        }
        return builder.toString();
    }

    public static Set<String> toSet (String s, String sep) {
        return new HashSet<>(Arrays.asList(s.split(sep)));
    }

    public static String tohex(byte[] data) {
        return tohex(data, 0, data.length);
    }

    public static String tohex(byte[] data, int start, int len) {
        StringBuilder b = new StringBuilder();
        int stop = start+len;
        for (int i=start; i<stop; i++) {
            b.append(getHexValue(data[i]));
        }
        return b.toString();
    }

    /**
     * Get the hexadecimal string representation for a byte.
     * The leading 0x is not included.
     *
     * @param b the byte to process
     * @return a String representing the hexadecimal value of the byte
     */
    public static String getHexValue(byte b) {
        int i = (int) b;
        return MD5Util.HEX_DIGITS[((i >> 4) + 16) % 16] + MD5Util.HEX_DIGITS[(i + 128) % 16];
    }

    public static String uncapitalize(String s) {
        return empty(s) ? s : s.length() == 1 ? s.toLowerCase() : s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    public static boolean exceptionContainsMessage(Throwable e, String s) {
        return e != null && (
                (e.getMessage() != null && e.getMessage().contains(s))
             || (e.getCause() != null && exceptionContainsMessage(e.getCause(), s))
        );
    }

    public static String ellipsis(String s, int len) {
        if (s.length() <= len) return s;
        return s.substring(0, len) + "...";
    }

    public static boolean containsIgnoreCase(Collection<String> values, String value) {
        for (String v : values) if (v != null && v.equalsIgnoreCase(value)) return true;
        return false;
    }
}
