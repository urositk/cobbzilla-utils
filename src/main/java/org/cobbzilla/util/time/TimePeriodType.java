package org.cobbzilla.util.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static org.cobbzilla.util.daemon.ZillaRuntime.now;
import static org.cobbzilla.util.time.TimeSpecifier.*;
import static org.cobbzilla.util.time.TimeUtil.*;

@Slf4j @AllArgsConstructor
public enum TimePeriodType {

    today            (todaySpecifier(),            tomorrowSpecifier()),
    tomorrow         (tomorrowSpecifier(),         futureDaySpecifier(2)),
    week_to_date     (t -> startOfWeekMillis(),    nowSpecifier()),
    month_to_date    (t -> startOfMonthMillis(),   nowSpecifier()),
    quarter_to_date  (t -> startOfQuarterMillis(), nowSpecifier()),
    year_to_date     (t -> startOfYearMillis(),    nowSpecifier()),
    yesterday        (yesterdaySpecifier(),        todaySpecifier()),
    previous_week    (t -> lastWeekMillis(),       t -> startOfWeekMillis()),
    previous_month   (t -> lastWeekMillis(),       t -> startOfMonthMillis()),
    previous_quarter (t -> lastQuarterMillis(),    t -> startOfQuarterMillis()),
    previous_year    (t -> lastYearMillis(),       t -> startOfYearMillis());

    private TimeSpecifier startSpecifier;
    private TimeSpecifier endSpecifier;

    @JsonCreator public static TimePeriodType fromString (String val) {
        try {
            return valueOf(val.toLowerCase());
        } catch (IllegalArgumentException e) {
            log.warn("fromString("+val+"): invalid value, use one of: "+Arrays.toString(values()));
            throw e;
        }
    }

    public long start() { return startSpecifier.get(now()); }
    public long end  () { return endSpecifier.get(now()); }

}
