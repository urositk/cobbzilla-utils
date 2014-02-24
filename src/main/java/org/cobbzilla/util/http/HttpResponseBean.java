package org.cobbzilla.util.http;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
public class HttpResponseBean {

    @Getter @Setter private int status;
    @Getter @Setter private Multimap<String, String> headers;
    @Getter @Setter private InputStream entity;
    @Getter @Setter private long contentLength;
    @Getter @Setter private String contentType;

    public void addHeader(String name, String value) {
        if (headers == null) headers = LinkedListMultimap.create();
        headers.put(name, value);
    }

    public void close () {
        if (entity != null) {
            try {
                entity.close();
            } catch (Exception e) {
                log.warn("error closing stream: "+e, e);
            }
        }
    }
}
