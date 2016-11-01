package org.cobbzilla.util.http;

import com.google.common.collect.Multimap;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.cobbzilla.util.string.StringUtil;
import org.cobbzilla.util.system.CommandResult;
import org.cobbzilla.util.system.CommandShell;
import org.cobbzilla.util.system.Sleep;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.http.URIUtil.getFileExt;
import static org.cobbzilla.util.system.Sleep.sleep;

@Slf4j
public class HttpUtil {

    public static final String DEFAULT_CERT_NAME = "ssl-https";

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

    public static HttpResponseBean upload (String url,
                                           File file,
                                           Map<String, String> headers) throws IOException {
        @Cleanup final CloseableHttpClient client = HttpClients.createDefault();
        final HttpPost method = new HttpPost(url);
        final FileBody fileBody = new FileBody(file);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create().addPart("file", fileBody);
        method.setEntity(builder.build());

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                method.addHeader(new BasicHeader(header.getKey(), header.getValue()));
            }
        }

        @Cleanup final CloseableHttpResponse response = client.execute(method);

        final HttpResponseBean responseBean = new HttpResponseBean()
                .setEntityBytes(EntityUtils.toByteArray(response.getEntity()))
                .setHttpHeaders(response.getAllHeaders())
                .setStatus(response.getStatusLine().getStatusCode());
        return responseBean;
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
        if (file == null) file = File.createTempFile("url2file-", getFileExt((url)));
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
                sleep(sleep, "waiting to possibly retry after IOException: "+lastException);
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
        final HttpClientBuilder clientBuilder = requestBean.initClientBuilder(HttpClients.custom());
        @Cleanup final CloseableHttpClient client = clientBuilder.build();
        return getResponse(requestBean, client);
    }

    public static HttpResponseBean getResponse(HttpRequestBean requestBean, HttpClient client) throws IOException {

        final HttpResponseBean bean = new HttpResponseBean();

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

    public static boolean isOk(String url) { return isOk(url, URIUtil.getHost(url)); }

    public static boolean isOk(String url, String host) {
        final CommandLine command = new CommandLine("curl")
                .addArgument("--insecure") // since we are requested via the IP address, the cert will not match
                .addArgument("--header").addArgument("Host: " + host) // pass FQDN via Host header
                .addArgument("--silent")
                .addArgument("--location")                              // follow redirects
                .addArgument("--write-out").addArgument("%{http_code}") // just print status code
                .addArgument("--output").addArgument("/dev/null")       // and ignore data
                .addArgument(url);
        try {
            final CommandResult result = CommandShell.exec(command);
            final String statusCode = result.getStdout();
            return result.isZeroExitStatus() && statusCode != null && statusCode.trim().startsWith("2");

        } catch (IOException e) {
            log.warn("isOk: Error fetching " + url + " with Host header=" + host + ": " + e);
            return false;
        }
    }

    public static boolean isOk(String url, String host, int maxTries, long sleepUnit) {
        long sleep = sleepUnit;
        for (int i = 0; i < maxTries; i++) {
            if (i > 0) {
                Sleep.sleep(sleep);
                sleep *= 2;
            }
            if (isOk(url, host)) return true;
        }
        return false;
    }
}
