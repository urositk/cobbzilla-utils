package org.cobbzilla.util.string;

public class StringUtil {

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
}
