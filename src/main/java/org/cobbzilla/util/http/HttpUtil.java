package org.cobbzilla.util.http;

import com.google.common.collect.Multimap;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.InputStreamEntity;
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

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.system.Sleep.sleep;

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

    public static final int DEFAULT_RETRIES = 3;

    public static File url2file (String url) throws IOException {
        return url2file(url, null, DEFAULT_RETRIES);
    }
    public static File url2file (String url, String file) throws IOException {
        return url2file(url, file == null ? null : new File(file), DEFAULT_RETRIES);
    }
    public static File url2file (String url, File file) throws IOException {
        return url2file(url, file, DEFAULT_RETRIES);
    }
    public static File url2file (String url, File file, int retries) throws IOException {
        if (file == null) file = File.createTempFile("url2file-", ".tmp");
        IOException lastException = null;
        long sleep = 100;
        for (int i=0; i<retries; i++) {
            try {
                @Cleanup final InputStream in = get(url);
                @Cleanup final OutputStream out = new FileOutputStream(file);
                IOUtils.copy(in, out);
                lastException = null;
                break;
            } catch (IOException e) {
                lastException = e;
                sleep(sleep, lastException);
                sleep *= 5;
            }
        }
        if (lastException != null) throw lastException;
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
        @Cleanup final InputStream content = response.getEntity().getContent();
        bean.setEntity(content);

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
        if (response.getEntity() != null) {
            final Header contentType = response.getEntity().getContentType();
            if (contentType != null) bean.setContentType(contentType.getValue());

            bean.setContentLength(response.getEntity().getContentLength());
            bean.setEntity(response.getEntity().getContent());
        }

        return bean;
    }

    public static HttpUriRequest initHttpRequest(HttpRequestBean requestBean) {
        try {
            final HttpUriRequest request;
            switch (requestBean.getMethod()) {
                case HttpMethods.GET:
                    request = new HttpGet(requestBean.getUri());
                    break;

                case HttpMethods.POST:
                    request = new HttpPost(requestBean.getUri());
                    break;

                case HttpMethods.PUT:
                    request = new HttpPut(requestBean.getUri());
                    break;

                case HttpMethods.DELETE:
                    request = new HttpDelete(requestBean.getUri());
                    break;

                default:
                    return die("Invalid request method: " + requestBean.getMethod());
            }

            if (requestBean.hasData() && request instanceof HttpEntityEnclosingRequestBase) {
                setData(requestBean.getData(), (HttpEntityEnclosingRequestBase) request);
            }

            return request;

        } catch (UnsupportedEncodingException e) {
            return die("initHttpRequest: " + e, e);
        }
    }

    private static void setData(Object data, HttpEntityEnclosingRequestBase request) throws UnsupportedEncodingException {
        if (data == null) return;
        if (data instanceof String) {
            request.setEntity(new StringEntity((String) data));
        } else if (data instanceof InputStream) {
            request.setEntity(new InputStreamEntity((InputStream) data));
        } else {
            throw new IllegalArgumentException("Unsupported request entity type: "+data.getClass().getName());
        }
    }

    public static String getContentType(HttpResponse response) {
        final Header contentTypeHeader = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
        return (contentTypeHeader == null) ? null : contentTypeHeader.getValue();
    }
}
