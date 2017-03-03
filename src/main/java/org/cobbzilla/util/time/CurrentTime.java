package org.cobbzilla.util.time;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.daemon.ZillaRuntime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class CurrentTime {

    @Getter @Setter private long now;
    @Getter @Setter private String timestamp;
    @Getter @Setter private String yyyyMMdd;
    @Getter @Setter private String yyyyMMddHHmmss;

    public CurrentTime(DateTimeZone tz) {
        now = ZillaRuntime.now();
        final DateTime time = new DateTime(now, tz);
        yyyyMMdd = TimeUtil.DATE_FORMAT_YYYY_MM_DD.print(time);
        yyyyMMddHHmmss = TimeUtil.DATE_FORMAT_YYYY_MM_DD_HH_mm_ss.print(time);
    }

}
