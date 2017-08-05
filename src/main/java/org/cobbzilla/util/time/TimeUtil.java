package org.cobbzilla.util.time;

import org.cobbzilla.util.string.StringUtil;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.concurrent.TimeUnit;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.daemon.ZillaRuntime.hexnow;
import static org.cobbzilla.util.daemon.ZillaRuntime.now;

public class TimeUtil {

    public static final long DAY    = TimeUnit.DAYS.toMillis(1);
    public static final long HOUR   = TimeUnit.HOURS.toMillis(1);
    public static final long MINUTE = TimeUnit.MINUTES.toMillis(1);
    public static final long SECOND = TimeUnit.SECONDS.toMillis(1);

    public static final DateTimeFormatter DATE_FORMAT_MMDDYYYY = DateTimeFormat.forPattern("MM/dd/yyyy");
    public static final DateTimeFormatter DATE_FORMAT_MMMM_D_YYYY = DateTimeFormat.forPattern("MMMM d, yyyy");
    public static final DateTimeFormatter DATE_FORMAT_YYYY_MM_DD = DateTimeFormat.forPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_FORMAT_YYYYMMDD = DateTimeFormat.forPattern("yyyyMMdd");
    public static final DateTimeFormatter DATE_FORMAT_MMM_DD_YYYY = DateTimeFormat.forPattern("MMM dd, yyyy");
    public static final DateTimeFormatter DATE_FORMAT_YYYY_MM_DD_HH_mm_ss = DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss");
    public static final DateTimeFormatter DATE_FORMAT_YYYYMMDDHHMMSS = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    // For now only m (months) and d (days) are supported
    // Both have to be present at the same time in that same order, but the value for each can be 0 to exclude that one - e.g. 0m15d.
    public static final PeriodFormatter PERIOD_FORMATTER = new PeriodFormatterBuilder()
            .appendMonths().appendSuffix("m").appendDays().appendSuffix("d").toFormatter();

    public static Long parse(String time, DateTimeFormatter formatter) {
        return empty(time) ? null : formatter.parseDateTime(time).getMillis();
    }

    public static String format(Long time, DateTimeFormatter formatter) {
        return time == null ? null : new DateTime(time).toString(formatter);
    }

    public static String formatDurationFrom(long start) {
        long duration = now() - start;
        return formatDuration(duration);
    }

    public static String formatDuration(long duration) {
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
        }
        millis = duration - secs * SECOND;

        if (days > 0) return String.format("%1$01dd %2$02d:%3$02d:%4$02d.%5$04d", days, hours, mins, secs, millis);
        return String.format("%1$02d:%2$02d:%3$02d.%4$04d", hours, mins, secs, millis);
    }

    public static long parseDuration(String duration) {
        if (empty(duration)) return 0;
        final long val = Long.parseLong(duration.length() > 1 ? StringUtil.chopSuffix(duration) : duration);
        switch (duration.charAt(duration.length()-1)) {
            case 's': return TimeUnit.SECONDS.toMillis(val);
            case 'm': return TimeUnit.MINUTES.toMillis(val);
            case 'h': return TimeUnit.HOURS.toMillis(val);
            case 'd': return TimeUnit.DAYS.toMillis(val);
            default: return val;
        }
    }

    public static long addYear (long time) {
        return new DateTime(time).withFieldAdded(DurationFieldType.years(), 1).getMillis();
    }

    public static long add365days (long time) {
        return new DateTime(time).withFieldAdded(DurationFieldType.days(), 365).getMillis();
    }

    public static String timestamp() { return timestamp(ClockProvider.ZILLA); }

    public static String timestamp(ClockProvider clock) {
        final long now = clock.now();
        return DATE_FORMAT_YYYY_MM_DD.print(now)+"-"+hexnow(now);
    }
}
