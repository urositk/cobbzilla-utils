package org.cobbzilla.util.http;

import java.net.URI;
import java.net.URISyntaxException;

public class URIUtil {

    public static String getHost(String uri) {
        try {
            return new URI(uri).getHost();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid host in URI: "+ uri);
        }
    }

    /**
     * getTLD("foo.bar.baz") == "baz"
     * @param uri A URI that includes a host part
     * @return the top-level domain
     */
    public static String getTLD(String uri) {
        final String parts[] = getHost(uri).split("\\.");
        if (parts.length > 0) return parts[parts.length-1];
        throw new IllegalArgumentException("Invalid host in URI: "+uri);
    }

    /**
     * getRegisteredDomain("foo.bar.baz") == "bar.baz"
     * @param uri A URI that includes a host part
     * @return the "registered" domain, which includes the TLD and one level up.
     */
    public static String getRegisteredDomain(String uri) {
        final String host = getHost(uri);
        final String parts[] = host.split("\\.");
        switch (parts.length) {
            case 0: throw new IllegalArgumentException("Invalid host: "+host);
            case 1: return host;
            default: return parts[parts.length-2] + "." + parts[parts.length-1];
        }
    }
}
