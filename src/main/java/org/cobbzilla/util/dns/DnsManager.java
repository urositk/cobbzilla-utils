package org.cobbzilla.util.dns;

public interface DnsManager {

    public void writeA(String hostname, String ip, int ttl);

    public void writeCNAME(String hostname, String name, int ttl);

    public void writeMX(String mailDomain, String mxHostname, int rank, int ttl);

    public void removeAll(String domain);

}
