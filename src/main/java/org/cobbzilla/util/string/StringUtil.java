package org.cobbzilla.util.string;

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

public class StringUtil {

    public static final String UTF8 = "UTF-8";
    public static final Charset UTF8cs = Charset.forName(UTF8);

    public static final String EMPTY = "";
    public static final String[] EMPTY_ARRAY = {};
    public static final String DEFAULT_LOCALE = "en_US";
    public static final String BYTES_PATTERN = "(\\d+)(\\s+)?([MgGgTtPpEe][Bb])";

    public static String prefix(String s, int count) {
        return s == null ? null : s.length() > count ? s.substring(0, count) : s;
    }

    public static String packagePath(Class clazz) {
        return clazz.getPackage().getName().replace(".","/");
    }

    public static boolean empty(String s) { return s == null || s.length() == 0; }

    public static List<String> split (String s, String delim) {
        final StringTokenizer st = new StringTokenizer(s, delim);
        final List<String> results = new ArrayList<>();
        while (st.hasMoreTokens()) {
            results.add(st.nextToken());
        }
        return results;
    }

    public static String lastPathElement(String url) { return url.substring(url.lastIndexOf("/")+1); }

    public static String safeHostname (String s) {
        return s.replaceAll("\\W", "");
    }

    public static Integer safeParseInt(String s) {
        if (StringUtil.empty(s)) return null;
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
}
