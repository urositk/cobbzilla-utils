package org.cobbzilla.util.http;


public interface HttpEventErrorHandler {

    boolean isSuccessful(HttpRequestBean request, HttpResponseBean response);
    void  handleError(HttpRequestBean request, HttpResponseBean response);
}
