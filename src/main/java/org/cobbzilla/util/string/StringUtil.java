package org.cobbzilla.util.string;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtil {

    public static String prefix(String s, int count) {
        return s == null ? null : s.length() > count ? s.substring(0, count) : s;
    }

    public static String packagePath(Class clazz) {
        return clazz.getPackage().getName().replace(".","/");
    }

    public static boolean empty(String s) { return s == null || s.length() == 0; }
}
