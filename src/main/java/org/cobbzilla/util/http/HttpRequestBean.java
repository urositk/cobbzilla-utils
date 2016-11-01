package org.cobbzilla.util.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cobbzilla.util.string.StringUtil;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.http.HttpMethods.*;

@NoArgsConstructor @ToString(of={"method", "uri"}) @Accessors(chain=true)
public class HttpRequestBean<T> {

    @Getter @Setter private String method = GET;
    @Getter @Setter private String uri;

    @Getter @Setter private T data;
    public boolean hasData () { return data != null; }

    @Getter @Setter private Multimap<String, String> headers = ArrayListMultimap.create();
    public HttpRequestBean<T> withHeader (String name, String value) { setHeader(name, value); return this; }
    public HttpRequestBean<T> setHeader (String name, String value) {
        headers.put(name, value);
        return this;
    }
    public boolean hasHeaders () { return !empty(headers); }

    public HttpRequestBean (String uri) { this(GET, uri, null); }

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

    public HttpRequestBean<T> setAuth(HttpAuthType authType, String name, String password) {
        setAuthType(authType);
        setAuthUsername(name);
        setAuthPassword(password);
        return this;
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

    public static HttpRequestBean<String> get   (String path)              { return new HttpRequestBean<>(GET, path); }
    public static HttpRequestBean<String> put   (String path, String json) { return new HttpRequestBean<>(PUT, path, json); }
    public static HttpRequestBean<String> post  (String path, String json) { return new HttpRequestBean<>(POST, path, json); }
    public static HttpRequestBean<String> delete(String path)              { return new HttpRequestBean<>(DELETE, path); }

    public String cURL () {
        // todo: add support for HTTP auth fields: authType/username/password
        final StringBuilder b = new StringBuilder("curl '"+getUri()).append("'");
        for (Map.Entry<String, Collection<String>> headerSet : getHeaders().asMap().entrySet()) {
            final String name = headerSet.getKey();
            for (String value : headerSet.getValue()) {
                b.append(" -H '").append(name).append(": ").append(value).append("'");
            }
        }
        if (getMethod().equals(PUT) || getMethod().equals(POST)) {
            b.append(" --data-binary '").append(getData()).append("'");
        }
        return b.toString();
    }


    public HttpClientBuilder initClientBuilder(HttpClientBuilder clientBuilder) {
        if (!hasAuth()) return clientBuilder;
        final HttpClientContext localContext = HttpClientContext.create();
        final BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(getHost(), getPort()),
                new UsernamePasswordCredentials(getAuthUsername(), getAuthPassword()));

        final AuthCache authCache = new BasicAuthCache();
        final AuthScheme authScheme = getAuthType().newScheme();
        authCache.put(getHttpHost(), authScheme);

        localContext.setAuthCache(authCache);
        clientBuilder.setDefaultCredentialsProvider(credsProvider);
        return clientBuilder;
    }

}
