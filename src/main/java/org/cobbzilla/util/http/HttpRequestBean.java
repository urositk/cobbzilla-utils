package org.cobbzilla.util.http;

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
    @Getter @Setter private Map<String, String> headers = Collections.EMPTY_MAP;
    @Getter @Setter private boolean isRedirect = false;

    public HttpRequestBean (String method, String uri, T data) {
        this(method, uri, data, Collections.EMPTY_MAP, false);
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

    public HttpRequestBean (String method, String uri, boolean redirect) {
        this.method = method;
        this.uri = uri;
        this.isRedirect = redirect;
    }

    public String getHost () { return get_uri().getHost(); }
    public int getPort () { return get_uri().getPort(); }
    public String getPath() { return get_uri().getPath(); }

}
