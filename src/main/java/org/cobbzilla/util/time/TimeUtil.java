package org.cobbzilla.util.time;

import org.cobbzilla.util.string.StringUtil;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeUtil {

    private static final Logger LOG = LoggerFactory.getLogger(TimeUtil.class);

    public static Long parse(String time, DateTimeFormatter formatter) {
        return StringUtil.empty(time) ? null : formatter.parseDateTime(time).getMillis();
    }

    public static String format(Long time, DateTimeFormatter formatter) {
        return time == null ? null : new DateTime(time).toString(formatter);
    }
}
