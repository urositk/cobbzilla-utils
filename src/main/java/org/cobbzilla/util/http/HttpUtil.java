package org.cobbzilla.util.http;

import org.cobbzilla.util.string.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpUtil {

    public static Map<String, String> queryParams(URL url) throws UnsupportedEncodingException {
        return queryParams(url, StringUtil.UTF8);
    }

    // from: http://stackoverflow.com/a/13592567
    public static Map<String, String> queryParams(URL url, String encoding) throws UnsupportedEncodingException {
        final Map<String, String> query_pairs = new LinkedHashMap<>();
        final String query = url.getQuery();
        final String[] pairs = query.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), encoding), URLDecoder.decode(pair.substring(idx + 1), encoding));
        }
        return query_pairs;
    }
}
