package org.cobbzilla.util.dns;

import java.util.List;

public interface DnsManager {

    public List<DnsRecord> list(DnsRecordMatch match) throws Exception;

    public boolean write(DnsRecord record) throws Exception;

    public void publish() throws Exception;

    public int remove(DnsRecordMatch match) throws Exception;

}
