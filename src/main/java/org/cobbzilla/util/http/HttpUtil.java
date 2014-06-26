package org.cobbzilla.util.http;

import com.google.common.collect.Multimap;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

    public static HttpResponseBean getResponse(HttpRequestBean requestBean) throws IOException {

        final HttpResponseBean bean = new HttpResponseBean();
        @Cleanup final CloseableHttpClient client = HttpClients.createDefault();

        final HttpUriRequest request = initHttpRequest(requestBean);

        final Multimap<String, String> headers = requestBean.getHeaders();
        for (String headerName  : headers.keySet()) {
            for (String headerValue : headers.get(headerName)) {
                request.setHeader(headerName, headerValue);
            }
        }

        final HttpResponse response = client.execute(request);

        for (Header header : response.getAllHeaders()) {
            bean.addHeader(header.getName(), header.getValue());
        }

        bean.setStatus(response.getStatusLine().getStatusCode());
        bean.setContentLength(response.getEntity().getContentLength());
        final Header contentType = response.getEntity().getContentType();
        if (contentType != null) {
            bean.setContentType(contentType.getValue());
        }
        bean.setEntity(response.getEntity().getContent());

        return bean;
    }

    public static HttpResponseBean getResponse(String urlString) throws IOException {

        final HttpResponseBean bean = new HttpResponseBean();
        @Cleanup final CloseableHttpClient client = HttpClients.createDefault();
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

    public static HttpUriRequest initHttpRequest(HttpRequestBean<String> requestBean) {
        try {
            final HttpUriRequest request;
            switch (requestBean.getMethod()) {
                case HttpMethods.GET:
                    request = new HttpGet(requestBean.getUri());
                    break;

                case HttpMethods.POST:
                    request = new HttpPost(requestBean.getUri());
                    if (requestBean.hasData()) ((HttpPost) request).setEntity(new StringEntity(requestBean.getData()));
                    break;

                case HttpMethods.PUT:
                    request = new HttpPut(requestBean.getUri());
                    if (requestBean.hasData()) ((HttpPut) request).setEntity(new StringEntity(requestBean.getData()));
                    break;

                case HttpMethods.DELETE:
                    request = new HttpDelete(requestBean.getUri());
                    break;

                default:
                    throw new IllegalStateException("Invalid request method: "+requestBean.getMethod());
            }
            return request;

        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("initHttpRequest: " + e, e);
        }
    }
}
