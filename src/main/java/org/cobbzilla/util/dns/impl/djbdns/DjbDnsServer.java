package org.cobbzilla.util.dns.impl.djbdns;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.dns.DnsServer;
import org.cobbzilla.util.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Designed to be used in conjunction with the djbdns_updater.sh script (see resources)
 *
 * Writes temp files to a directory that is watched by the djbdns_updater.sh script.
 * The djbdns_updater.sh script runs as root and coordinates changes to the djbdns data file.
 */
@Slf4j
public class DjbDnsServer implements DnsServer {

    private static final String PROP_CHANGES_DIR = "changesDir";

    @Getter @Setter private File changesDir;

    @Override
    public void initialize(Map<String, String> properties) {
        final String path = properties.get(PROP_CHANGES_DIR);
        if (path == null) throw new IllegalArgumentException("No "+PROP_CHANGES_DIR+" property defined");

        this.changesDir = new File(path);
        if (!changesDir.exists() || !changesDir.canRead() || !changesDir.canWrite()) {
            throw new IllegalArgumentException("Cannot read/write root file: "+changesDir.getAbsolutePath());
        }
    }

    @Override
    public synchronized void writeA(String hostname, String ip, int ttl) throws IOException {
        final String data = new StringBuilder().append("+").append(hostname).append(":").append(ip).append(":").append(ttl).toString();
        writeChange(data);
    }

    @Override
    public void writeMX(String mailDomain, String mxHostname, int rank, int ttl) throws IOException {
        final String data = new StringBuilder().append("@").append(mailDomain).append(".::").append(mxHostname).append(":").append(ttl).toString();
        writeChange(data);
    }

    @Override
    public void writeNS(String fqdn, String ip, int ttl) throws IOException {
        final String data = new StringBuilder().append("&").append(fqdn).append(".:").append(ip).append(":a:").append(ttl).toString();
        writeChange(data);
    }

    private void writeChange(String data) throws IOException {
        FileUtil.toFile(File.createTempFile("djbdns", "tmp", changesDir).getAbsolutePath(), data);
    }

}
