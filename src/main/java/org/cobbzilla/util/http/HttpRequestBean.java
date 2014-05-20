package org.cobbzilla.util.http;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.*;
import org.cobbzilla.util.string.StringUtil;

import java.net.URI;

@NoArgsConstructor @ToString(of={"method", "uri"})
public class HttpRequestBean<T> {

    @Getter @Setter private String method = HttpMethods.GET;
    @Getter @Setter private String uri;
    @Getter @Setter private T data;
    @Getter @Setter private Multimap<String, String> headers = ArrayListMultimap.create();

    public HttpRequestBean (String uri) { this(HttpMethods.GET, uri, null); }

    public HttpRequestBean (String method, String uri) { this(method, uri, null); }

    public HttpRequestBean (String method, String uri, T data) {
        this.method = method;
        this.uri = uri;
        this.data = data;
    }

    public HttpRequestBean (String method, String uri, T data, Multimap<String, String> headers) {
        this(method, uri, data);
        this.headers = headers;
    }

    public void setHeader (String name, String value) {
        headers.put(name, value);
    }

    public boolean hasData () { return data != null; }

    @Getter(lazy=true, value=AccessLevel.PRIVATE) private final URI _uri = initURI();

    private URI initURI() { return StringUtil.uriOrDie(uri); }

    public String getHost () { return get_uri().getHost(); }
    public int getPort () { return get_uri().getPort(); }
    public String getPath () { return get_uri().getPath(); }

}
