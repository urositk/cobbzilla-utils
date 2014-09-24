package org.cobbzilla.util.http;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.reflect.ReflectionUtil;
import org.cobbzilla.util.string.StringUtil;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.StringTokenizer;

import static org.cobbzilla.util.string.StringUtil.empty;

@NoArgsConstructor @Accessors(chain=true) @Slf4j
public class HttpCookieBean {

    public static final DateTimeFormatter EXPIRES_PATTERN = DateTimeFormat.forPattern("E, dd-MMM-yyyy HH:mm:ss z");

    @Getter @Setter private String name;
    @Getter @Setter private String value;
    @Getter @Setter private String domain;

    public HttpCookieBean(String name, String value, String domain) {
        this.name = name;
        this.value = value;
        this.domain = domain;
    }

    public HttpCookieBean (HttpCookieBean other) {
        ReflectionUtil.copy(this, other);
    }

    public boolean hasDomain () { return !StringUtil.empty(domain); }

    @Getter @Setter private String path;
    @Getter @Setter private String expires;
    @Getter @Setter private Long maxAge;
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
                    case "max-age": cookie.maxAge = Long.valueOf(parts[1]); break;
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
        if (maxAge != null) sb.append("; Max-Age=").append(maxAge);
        if (!empty(path)) sb.append("; Path=").append(path);
        if (!empty(domain)) sb.append("; Domain=").append(domain);
        if (httpOnly) sb.append("; HttpOnly");
        if (secure) sb.append("; Secure");
        return sb.toString();
    }

    public String toRequestHeader () { return name + "=" + value; }

    public boolean expired () {
        return (maxAge != null && maxAge <= 0)
                || (expires != null && EXPIRES_PATTERN.parseDateTime(expires).toDateTime().isBeforeNow());
    }
}
