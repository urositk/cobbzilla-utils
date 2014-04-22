package org.cobbzilla.util.dns;

import java.io.IOException;
import java.util.Map;

public interface DnsServer {

    public void initialize (Map<String, String> properties);

    public void writeA (String hostname, String ip, int ttl) throws IOException;

}
