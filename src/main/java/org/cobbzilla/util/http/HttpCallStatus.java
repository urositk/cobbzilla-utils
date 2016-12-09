package org.cobbzilla.util.http;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum HttpCallStatus {

    requested, success, error, timeout;

    @JsonCreator public static HttpCallStatus fromString (String val) { return valueOf(val.toLowerCase()); }

}
