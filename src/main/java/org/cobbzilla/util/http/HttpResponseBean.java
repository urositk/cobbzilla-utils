package org.cobbzilla.util.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.cobbzilla.util.json.JsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static org.apache.http.HttpHeaders.CONTENT_LENGTH;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Slf4j @Accessors(chain=true) @ToString(of={"status", "headers"})
public class HttpResponseBean {

    @Getter @Setter private int status;
    @Getter @Setter private Multimap<String, String> headers;
    @Getter private byte[] entity;
    @Getter @Setter private long contentLength;
    @Getter @Setter private String contentType;

    @JsonIgnore public boolean isOk() { return (status % 100) == 2; }

    public void addHeader(String name, String value) {
        if (headers == null) headers = LinkedListMultimap.create();
        if (name.equalsIgnoreCase(CONTENT_TYPE)) setContentType(value);
        else if (name.equalsIgnoreCase(CONTENT_LENGTH)) setContentLength(Long.valueOf(value));
        headers.put(name, value);
    }

    public HttpResponseBean setEntityBytes(byte[] bytes) { this.entity = bytes; return this; }

    public void setEntity (InputStream entity) {
        try {
            this.entity = entity == null ? null : IOUtils.toByteArray(entity);
        } catch (IOException e) {
            die("setEntity: error reading stream: " + e, e);
        }
    }

    public boolean hasEntity () { return !empty(entity); }

    public String getEntityString () {
        return entity == null ? null : new String(entity);
    }

    public <T> T getEntity (Class<T> clazz) {
        return entity == null ? null : JsonUtil.fromJsonOrDie(getEntityString(), clazz);
    }

    public Collection<String> getHeaderValues (String name) { return headers.get(name); }

    public String getFirstHeaderValue (String name) {
        final Collection<String> values = headers.get(name);
        return values == null || values.isEmpty() ? null : values.iterator().next();
    }

    public HttpResponseBean setHttpHeaders(Header[] headers) {
        for (Header header : headers) {
            addHeader(header.getName(), header.getValue());;
        }
        return this;
    }

}
