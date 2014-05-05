package org.cobbzilla.util.http;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

@NoArgsConstructor @AllArgsConstructor
public class HttpRequestBean<T> {

    @Getter @Setter private String method;
    @Getter @Setter private String uri;
    @Getter @Setter private T data;
    @Getter @Setter private Multimap<String, String> headers = ArrayListMultimap.create();

    public HttpRequestBean (String method, String uri, T data) {
        this(method, uri, data, null);
    }

    public boolean hasData () { return data != null; }

    @Getter(lazy=true, value=AccessLevel.PRIVATE) private final URI _uri = initURI();

    private URI initURI() {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("initURI: "+e, e);
        }
    }

    public HttpRequestBean (String method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public String getHost () { return get_uri().getHost(); }
    public int getPort () { return get_uri().getPort(); }
    public String getPath() { return get_uri().getPath(); }

}
