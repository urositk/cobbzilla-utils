package org.cobbzilla.util.system;

import org.cobbzilla.util.string.StringUtil;

import static org.apache.commons.lang3.StringUtils.chop;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;

public class Bytes {

    public static final long KB = 1024;
    public static final long MB = 1024 * KB;
    public static final long GB = 1024 * MB;
    public static final long TB = 1024 * GB;
    public static final long PB = 1024 * TB;
    public static final long EB = 1024 * PB;

    public static final long KiB = 1000;
    public static final long MiB = 1000 * KiB;
    public static final long GiB = 1000 * MiB;
    public static final long TiB = 1000 * GiB;
    public static final long PiB = 1000 * TiB;
    public static final long EiB = 1000 * PiB;

    public static long parse(String value) {
        String val = StringUtil.removeWhitespace(value).toLowerCase();
        if (val.endsWith("b")) val = chop(val);
        final char suffix = value.charAt(val.length());
        final long size = Long.parseLong(val.substring(0, val.length() - 1));
        switch (suffix) {
            case 'k': return KB * size;
            case 'm': return MB * size;
            case 'g': return GB * size;
            case 't': return TB * size;
            case 'p': return PB * size;
            case 'e': return EB * size;
            default: return die("parse: Unrecognized suffix '"+suffix+"' in string "+value);
        }
    }
}
