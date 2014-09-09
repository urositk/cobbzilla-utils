package org.cobbzilla.util.dns;

import lombok.*;
import lombok.experimental.Accessors;

import static org.cobbzilla.util.string.StringUtil.empty;

@Accessors(chain=true) @ToString @NoArgsConstructor @AllArgsConstructor
public class DnsRecordBase {

    @Getter @Setter private String fqdn;
    public boolean hasFqdn() { return !empty(fqdn); }

    @Getter @Setter private DnsType type;
    public boolean hasType () { return type != null; }

    @Getter @Setter private String value;
    public boolean hasValue () { return !empty(value); }

}
