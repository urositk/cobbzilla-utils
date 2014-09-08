package org.cobbzilla.util.dns;

public interface DnsManager {

    public void writeA(String fqdn, String ip, int ttl) throws Exception;

    public void writeCNAME(String fqdn, String name, int ttl) throws Exception;

    public void writeMX(String fqdn, String mxHostname, int rank, int ttl) throws Exception;

    public void publish() throws Exception;

    public void removeAll(String domain);

}
