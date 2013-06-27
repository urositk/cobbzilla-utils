package org.cobbzilla.util.string;

import org.apache.commons.lang.LocaleUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class StringUtil {

    public static final String UTF8 = "UTF-8";
    public static final String EMPTY = "";
    public static final String DEFAULT_LOCALE = "en_US";

    public static String prefix(String s, int count) {
        return s == null ? null : s.length() > count ? s.substring(0, count) : s;
    }

    public static String packagePath(Class clazz) {
        return clazz.getPackage().getName().replace(".","/");
    }

    public static boolean empty(String s) { return s == null || s.length() == 0; }

    public static String lastPathElement(String url) { return url.substring(url.lastIndexOf("/")+1); }

    public static Integer safeParseInt(String s) {
        if (StringUtil.empty(s)) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String fullDateTime(String localeString, long time) {
        Locale locale = LocaleUtils.toLocale(localeString);
        return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale).format(new Date(time));
    }

}
