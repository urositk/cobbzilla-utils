package org.cobbzilla.util.dns;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Accessors(chain=true) @NoArgsConstructor @ToString(callSuper=true)
public class DnsRecord extends DnsRecordBase {

    public static final int DEFAULT_TTL = (int) TimeUnit.HOURS.toMillis(1);

    @Getter @Setter private int ttl = DEFAULT_TTL;
    @Getter @Setter private Map<String, String> options;

    public DnsRecord setOption(String optName, String value) {
        options.put(optName, value);
        return this;
    }

    public String getOption(String optName) { return options.get(optName); }

    public int getIntOption(String optName, int defaultValue) {
        try {
            return Integer.parseInt(options.get(optName));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }
}
