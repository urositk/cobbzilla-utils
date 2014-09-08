package org.cobbzilla.util.dns;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.string.StringUtil;

@Accessors(chain=true)
public class DnsRecordMatch extends DnsRecordBase {

    @Getter @Setter private String subdomain;
    public boolean hasSubdomain() { return !StringUtil.empty(subdomain); }

}
