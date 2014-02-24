package org.cobbzilla.util.http;

import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cobbzilla.util.string.StringUtil;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
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

    public static InputStream get (String urlString) throws IOException {
        final URL url = new URL(urlString);
        final URLConnection urlConnection = url.openConnection();
        return urlConnection.getInputStream();
    }

    public static File url2file (String url) throws IOException {
        return url2file(url, (File) null);
    }
    public static File url2file (String url, String file) throws IOException {
        return url2file(url, file == null ? null : new File(file));
    }
    public static File url2file (String url, File file) throws IOException {
        if (file == null) file = File.createTempFile("url2file-", ".tmp");
        @Cleanup final InputStream in = get(url);
        @Cleanup final OutputStream out = new FileOutputStream(file);
        IOUtils.copy(in, out);
        return file;
    }

    public static String url2string (String url) throws IOException {
        @Cleanup final InputStream in = get(url);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        return out.toString();
    }

    public static HttpResponseBean getResponse(String urlString) throws IOException {

        final HttpResponseBean bean = new HttpResponseBean();
        final HttpClient client = new DefaultHttpClient();
        final HttpResponse response = client.execute(new HttpGet(urlString));

        for (Header header : response.getAllHeaders()) {
            bean.addHeader(header.getName(), header.getValue());
        }

        bean.setStatus(response.getStatusLine().getStatusCode());
        bean.setContentLength(response.getEntity().getContentLength());
        bean.setContentType(response.getEntity().getContentType().getValue());
        bean.setEntity(response.getEntity().getContent());

        return bean;
    }
}
