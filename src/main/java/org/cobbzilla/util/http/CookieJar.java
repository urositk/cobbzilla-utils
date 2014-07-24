package org.cobbzilla.util.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cobbzilla.util.collection.CaseInsensitiveStringKeyMap;

public class CookieJar extends CaseInsensitiveStringKeyMap<HttpCookieBean> {

    public void add (HttpCookieBean cookie) {
        if (cookie.expired()) {
            remove(cookie.getName());
        } else {
            put(cookie.getName(), cookie);
        }
    }

    @JsonIgnore
    public String getRequestValue() {
        final StringBuilder sb = new StringBuilder();
        for (String name : keySet()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(name).append("=").append(get(name).getValue());
        }
        return sb.toString();
    }

}
