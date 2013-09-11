package org.cobbzilla.util.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @AllArgsConstructor
public class HttpRequestBean<T> {

    @Getter @Setter private String method;
    @Getter @Setter private String uri;
    @Getter @Setter private T data;

    public HttpRequestBean (String method, String uri) {
        this.method = method;
        this.uri = uri;
    }

}
