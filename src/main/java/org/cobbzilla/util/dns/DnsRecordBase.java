package org.cobbzilla.util.dns;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;

import static org.apache.commons.lang3.StringUtils.chop;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Accessors(chain=true) @ToString @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class DnsRecordBase {

    @Getter @Setter private String fqdn;
    public boolean hasFqdn() { return !empty(fqdn); }
    @JsonIgnore public String getNormalFqdn() { return empty(fqdn) ? fqdn : fqdn.endsWith(".") ? chop(fqdn) : fqdn; }

    @Getter @Setter private DnsType type;
    public boolean hasType () { return type != null; }

    @Getter @Setter private String value;
    public boolean hasValue () { return !empty(value); }

    @JsonIgnore
    public DnsRecordMatch getMatcher() {
        return (DnsRecordMatch) new DnsRecordMatch().setFqdn(fqdn).setType(type).setValue(value);
    }

    public boolean match(DnsRecordMatch match) {
        // strip trailing dot if there is one
        final String val = !hasValue() || !value.endsWith(".") ? value : chop(value);

        if (match.hasSubdomain() && !getNormalFqdn().endsWith(match.getSubdomain())) return false;
        if (match.hasType() && getType() != match.getType()) return false;
        if (match.hasFqdn() && !getNormalFqdn().equalsIgnoreCase(match.getFqdn())) return false;
        if (match.hasValue() && hasValue() && !val.equalsIgnoreCase(match.getValue())) return false;
        return true;
    }
}
