package org.cobbzilla.util.http;

public interface HttpResponseHandler {

    void success (HttpRequestBean request, HttpResponseBean response);
    void failure (HttpRequestBean request, HttpResponseBean response);

}
