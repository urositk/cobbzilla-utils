package org.cobbzilla.util.dns;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Accessors(chain=true) @ToString(callSuper=true)
public class DnsRecord extends DnsRecordBase {

    public static final int DEFAULT_TTL = (int) TimeUnit.HOURS.toMillis(1);

    @Getter @Setter private int ttl = DEFAULT_TTL;
    @Getter @Setter private Map<String, String> options;

    public DnsRecord setOption(String optName, String value) {
        if (options == null) options = new HashMap<>();
        options.put(optName, value);
        return this;
    }

    public String getOption(String optName) { return options == null ? null : options.get(optName); }

    public int getIntOption(String optName, int defaultValue) {
        try {
            return Integer.parseInt(options.get(optName));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }
}
