package org.cobbzilla.util.dns;

public interface DnsManager {

    public void writeA(String hostname, String ip, int ttl) throws Exception;

    public void writeCNAME(String hostname, String name, int ttl) throws Exception;

    public void writeMX(String mailDomain, String mxHostname, int rank, int ttl) throws Exception;

    public void removeAll(String domain);

}
