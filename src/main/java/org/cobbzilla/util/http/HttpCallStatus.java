package org.cobbzilla.util.http;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum HttpCallStatus {

    initialized, requested, received_response, success, failure, error, timeout;

    @JsonCreator public static HttpCallStatus fromString (String val) { return valueOf(val.toLowerCase()); }

}
