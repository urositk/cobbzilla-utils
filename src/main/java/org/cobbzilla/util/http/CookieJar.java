package org.cobbzilla.util.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;
import org.cobbzilla.util.collection.CaseInsensitiveStringKeyMap;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class CookieJar extends CaseInsensitiveStringKeyMap<HttpCookieBean> {

    public CookieJar(List<HttpCookieBean> cookies) {
        for (HttpCookieBean cookie : cookies) add(cookie);
    }

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

    public List<HttpCookieBean> getCookiesList () { return new ArrayList<>(values()); }


}
