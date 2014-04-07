package org.cobbzilla.util.time;

import org.cobbzilla.util.string.StringUtil;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static final long DAY    = TimeUnit.DAYS.toMillis(1);
    public static final long HOUR   = TimeUnit.HOURS.toMillis(1);
    public static final long MINUTE = TimeUnit.MINUTES.toMillis(1);
    public static final long SECOND = TimeUnit.SECONDS.toMillis(1);

    public static Long parse(String time, DateTimeFormatter formatter) {
        return StringUtil.empty(time) ? null : formatter.parseDateTime(time).getMillis();
    }

    public static String format(Long time, DateTimeFormatter formatter) {
        return time == null ? null : new DateTime(time).toString(formatter);
    }

    public static String formatDurationFrom(long start) {

        long duration = System.currentTimeMillis() - start;
        long days = 0, hours = 0, mins = 0, secs = 0, millis = 0;

        if (duration > DAY) {
            days = duration/DAY;
            duration -= days * DAY;
        }
        if (duration > HOUR) {
            hours = duration/HOUR;
            duration -= hours * HOUR;
        }
        if (duration > MINUTE) {
            mins = duration/MINUTE;
            duration -= mins * MINUTE;
        }
        if (duration > SECOND) {
            secs = duration/SECOND;
            millis = duration - secs * SECOND;
        }

        return String.format("%1$01dd %2$02d:%3$02d:%4$02d.%5$04d", days, hours, mins, secs, millis);
    }
}
