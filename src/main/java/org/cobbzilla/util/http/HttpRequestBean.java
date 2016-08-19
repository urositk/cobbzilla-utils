package org.cobbzilla.util.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.cobbzilla.util.string.StringUtil;

import java.net.URI;
import java.util.Collection;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@NoArgsConstructor @ToString(of={"method", "uri"}) @Accessors(chain=true)
public class HttpRequestBean<T> {

    @Getter @Setter private String method = HttpMethods.GET;
    @Getter @Setter private String uri;

    @Getter @Setter private T data;
    public boolean hasData () { return data != null; }

    @Getter @Setter private Multimap<String, String> headers = ArrayListMultimap.create();
    public HttpRequestBean<T> withHeader (String name, String value) { setHeader(name, value); return this; }
    public void setHeader (String name, String value) {
        headers.put(name, value);
    }
    public boolean hasHeaders () { return !empty(headers); }

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

    @Getter(lazy=true, value=AccessLevel.PRIVATE) private final URI _uri = initURI();

    private URI initURI() { return StringUtil.uriOrDie(uri); }

    public String getHost () { return get_uri().getHost(); }
    public int getPort () { return get_uri().getPort(); }
    public String getPath () { return get_uri().getPath(); }

    @JsonIgnore @Getter(lazy=true) private final HttpHost httpHost = initHttpHost();
    private HttpHost initHttpHost() { return new HttpHost(getHost(), getPort(), get_uri().getScheme()); }

    @Getter @Setter private HttpAuthType authType;
    @Getter @Setter private String authUsername;
    @Getter @Setter private String authPassword;

    public boolean hasAuth () { return authType != null; }

    public void setAuth(HttpAuthType authType, String name, String password) {
        setAuthType(authType);
        setAuthUsername(name);
        setAuthPassword(password);
    }

    public ContentType getContentType() {
        if (!hasHeaders()) return null;
        final String value = getFirstHeaderValue(HttpHeaders.CONTENT_TYPE);
        if (empty(value)) return null;
        return ContentType.parse(value);
    }

    private String getFirstHeaderValue(String name) {
        if (!hasHeaders()) return null;
        final Collection<String> values = headers.get(name);
        if (empty(values)) return null;
        return values.iterator().next();
    }

    public static HttpRequestBean<String> get   (String path)              { return new HttpRequestBean<>(HttpMethods.GET, path); }
    public static HttpRequestBean<String> put   (String path, String json) { return new HttpRequestBean<>(HttpMethods.PUT, path, json); }
    public static HttpRequestBean<String> post  (String path, String json) { return new HttpRequestBean<>(HttpMethods.POST, path, json); }
    public static HttpRequestBean<String> delete(String path)              { return new HttpRequestBean<>(HttpMethods.DELETE, path); }

}
