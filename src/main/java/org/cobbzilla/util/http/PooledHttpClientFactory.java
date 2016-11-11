package org.cobbzilla.util.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.cobbzilla.util.reflect.ObjectFactory;

import java.util.Map;

@AllArgsConstructor
public class PooledHttpClientFactory implements ObjectFactory<HttpClient> {

    @Getter private String host;
    @Getter private int maxConnections;

    @Override public HttpClient create() {
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxConnections);
        cm.setMaxPerRoute(new HttpRoute(new HttpHost(host)), maxConnections);
        return HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

    @Override public HttpClient create(Map<String, Object> ctx) { return create(); }

}
