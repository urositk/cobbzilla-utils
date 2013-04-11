package org.cobbzilla.util.time;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.string.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
public class ImprovedTimezone {

    private int id;
    private String gmtOffset;
    private String displayName;
    private TimeZone timezone;
    private String displayNameWithOffset;

    private static List<ImprovedTimezone> TIMEZONES = null;
    private static Map<Integer, ImprovedTimezone> TIMEZONES_BY_ID = new HashMap<>();
    private static Map<String, ImprovedTimezone> TIMEZONES_BY_GMT = new HashMap<>();
    private static Map<String, ImprovedTimezone> TIMEZONES_BY_JNAME = new HashMap<>();
    private static final String TZ_FILE = StringUtil.packagePath(ImprovedTimezone.class) +"/timezones.txt";

    private static TimeZone SYSTEM_TIMEZONE;
    static {
        try {
            init();
        } catch (IOException e) {
            String msg = "Error initializing ImprovedTimezone from timezones.txt: "+e;
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
        TimeZone sysTimezone = TimeZone.getDefault();
        ImprovedTimezone tz = TIMEZONES_BY_JNAME.get(sysTimezone.getDisplayName());
        if(null == tz) {
            for(String displayName: TIMEZONES_BY_JNAME.keySet()) {
                ImprovedTimezone tz1 = TIMEZONES_BY_JNAME.get(displayName);
                String dn = displayName.replace("GMT-0","GMT-");
                dn = dn.replace("GMT+0", "GMT+");
                if(tz1.getGmtOffset().equals(dn)) {
                    tz = tz1;
                    break;
                }
            }
        }
        if(null == tz) {
            throw(new ExceptionInInitializerError("System Timezone could not be located in timezones.txt"));
        }

        SYSTEM_TIMEZONE = tz.getTimeZone();
        log.info("System Time Zone set to " + SYSTEM_TIMEZONE.getDisplayName());
    }

    private ImprovedTimezone (int id,
                              String gmtOffset,
                              TimeZone timezone,
                              String displayName) {
        this.id = id;
        this.gmtOffset = gmtOffset;
        this.timezone = timezone;
        this.displayName = displayName;
        this.displayNameWithOffset = "("+gmtOffset+") "+displayName;
    }

    public int getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getGmtOffset() { return gmtOffset; }
    @JsonIgnore public TimeZone getTimeZone() { return timezone; }
    public String getDisplayNameWithOffset () { return displayNameWithOffset; }
    public long getLocalTime (long systemTime) {
        // convert time to GMT
        long gmtTime = systemTime - SYSTEM_TIMEZONE.getRawOffset();
        // now that we're in GMT, convert to local
        long localTime = gmtTime + getTimeZone().getRawOffset();

        return localTime;
    }

    public String toString () {
        return "[ImprovedTimezone id="+id+" offset="+gmtOffset
                +" name="+displayName+" zone="+timezone.getDisplayName() +"]";
    }

    public static List<ImprovedTimezone> getTimeZones () {
        return TIMEZONES;
    }

    public static ImprovedTimezone getTimeZoneById (int id) {
        ImprovedTimezone tz = TIMEZONES_BY_ID.get(id);
        if (tz == null) {
            throw new IllegalArgumentException("Invalid timezone id: "+id);
        }
        return tz;
    }

    public static ImprovedTimezone getTimeZoneByJavaDisplayName (String name) {
        ImprovedTimezone tz = TIMEZONES_BY_JNAME.get(name);
        if (tz == null) {
            throw new IllegalArgumentException("Invalid timezone name: "+name);
        }
        return tz;
    }

    public static ImprovedTimezone getTimeZoneByGmtOffset(String value) {
        return TIMEZONES_BY_GMT.get(value);
    }

    /**
     * Initialize timezones from a file on classpath.
     * The first line of the file is a header that is ignored.
     */
    private static void init () throws IOException {

        TIMEZONES = new ArrayList<>();
        try (InputStream in = ImprovedTimezone.class.getClassLoader().getResourceAsStream(TZ_FILE)) {
            if (in == null) {
                throw new IOException("Error loading timezone file from classpath: "+TZ_FILE);
            }
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
                String line = r.readLine();
                while (line != null) {
                    line = r.readLine();
                    if (line == null) break;
                    ImprovedTimezone improvedTimezone = initZone(line);
                    TIMEZONES.add(improvedTimezone);
                    TIMEZONES_BY_ID.put(improvedTimezone.getId(), improvedTimezone);
                    TIMEZONES_BY_JNAME.put(improvedTimezone.getTimeZone().getDisplayName(), improvedTimezone);
                    TIMEZONES_BY_GMT.put(improvedTimezone.getGmtOffset(), improvedTimezone);
                }
            }
        }
    }
    private static ImprovedTimezone initZone (String line) {
        StringTokenizer st = new StringTokenizer(line);
        int id = Integer.parseInt(st.nextToken());
        String gmtOffset = st.nextToken();
        String timezoneName = st.nextToken();
        TimeZone tz = TimeZone.getTimeZone(timezoneName);
        if (!gmtOffset.equals("GMT") && isGMT(tz)) {
            String msg = "Error looking up timezone: "+timezoneName+": got GMT, expected "+gmtOffset;
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        StringBuffer displayName = new StringBuffer();
        while (st.hasMoreTokens()) {
            displayName.append(st.nextToken()).append(' ');
        }
        return new ImprovedTimezone(id, gmtOffset, tz, displayName.toString().trim());
    }

    private static boolean isGMT(TimeZone tz) {
        return tz.getRawOffset() == 0;
    }

}
