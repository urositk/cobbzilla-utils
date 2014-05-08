package org.cobbzilla.util.dns;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class DnsServerConfiguration {

    @Getter @Setter private String dnsClass;
    @Getter @Setter private Map<String, String> properties;

    @Getter(lazy=true) private final DnsServer server = initDnsServer();

    public DnsServer initDnsServer () {
        try {
            final DnsServer server = (DnsServer) Class.forName(dnsClass).newInstance();
            server.initialize(properties);
            return server;
        } catch (Exception e) {
            throw new IllegalStateException("initDnsServer: error creating "+dnsClass+" with properties="+properties+": "+e, e);
        }
    }
}
