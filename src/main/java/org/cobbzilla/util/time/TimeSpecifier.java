package org.cobbzilla.util.time;

import org.joda.time.DateTime;

import static org.cobbzilla.util.daemon.ZillaRuntime.now;

public interface TimeSpecifier {

    long get(long t);

    static TimeSpecifier nowSpecifier() { return t -> now(); }
    static TimeSpecifier todaySpecifier() { return t -> new DateTime(t, DefaultTimezone.getZone()).withTimeAtStartOfDay().getMillis(); }

}
