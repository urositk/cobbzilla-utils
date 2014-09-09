package org.cobbzilla.util.dns;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.cobbzilla.util.string.StringUtil.empty;

@Accessors(chain=true) @NoArgsConstructor
public class DnsRecordMatch extends DnsRecordBase {

    @Getter @Setter private String subdomain;

    public DnsRecordMatch(DnsRecordBase record) {
        super(record.getFqdn(), record.getType(), record.getValue());
    }

    public boolean hasSubdomain() { return !empty(subdomain); }

}
