package org.cobbzilla.util.dns;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cobbzilla.util.string.StringUtil;

@Accessors(chain=true) @ToString
public class DnsRecordBase {

    @Getter @Setter private String fqdn;
    public boolean hasFqdn() { return !StringUtil.empty(fqdn); }

    @Getter @Setter private DnsType type;
    public boolean hasType () { return type != null; }

    @Getter @Setter private String value;
    public boolean hasValue () { return !StringUtil.empty(value); }

}
