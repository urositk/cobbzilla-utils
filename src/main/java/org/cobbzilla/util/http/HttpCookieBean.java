package org.cobbzilla.util.http;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.string.StringUtil;

import java.util.StringTokenizer;

import static org.cobbzilla.util.string.StringUtil.empty;

@Slf4j
public class HttpCookieBean {

    @Getter @Setter private String name;
    @Getter @Setter private String value;
    @Getter @Setter private String domain;
    public boolean hasDomain () { return !StringUtil.empty(domain); }

    @Getter @Setter private String path;
    @Getter @Setter private String expires;
    @Getter @Setter private String maxAge;
    @Getter @Setter private boolean secure;
    @Getter @Setter private boolean httpOnly;

    public static HttpCookieBean parse (String setCookie) {
        final HttpCookieBean cookie = new HttpCookieBean();
        final StringTokenizer st = new StringTokenizer(setCookie, ";");
        while (st.hasMoreTokens()) {
            final String token = st.nextToken().trim();
            if (cookie.name == null) {
                // first element is the name=value
                final String[] parts = token.split("=");
                cookie.name = parts[0];
                cookie.value = parts[1];

            } else if (token.contains("=")) {
                final String[] parts = token.split("=");
                switch (parts[0].toLowerCase()) {
                    case "path":    cookie.path = parts[1]; break;
                    case "domain":  cookie.domain = parts[1]; break;
                    case "expires": cookie.expires = parts[1]; break;
                    case "max-age": cookie.maxAge = parts[1]; break;
                    default: log.warn("Unrecognized cookie attribute: "+parts[0]);
                }
            } else {
                switch (token.toLowerCase()) {
                    case "httponly": cookie.httpOnly = true; break;
                    case "secure": cookie.secure = true; break;
                    default: log.warn("Unrecognized cookie attribute: "+token);
                }
            }
        }
        return cookie;
    }

    public String toHeaderValue () {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value);
        if (!empty(expires)) sb.append("; Expires=").append(expires);
        if (!empty(maxAge)) sb.append("; Max-Age=").append(maxAge);
        if (!empty(path)) sb.append("; Path=").append(path);
        if (!empty(domain)) sb.append("; Domain=").append(domain);
        if (httpOnly) sb.append("; HttpOnly");
        if (secure) sb.append("; Secure");
        return sb.toString();
    }

}
