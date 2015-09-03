package org.cobbzilla.util.network;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum FirewallChain {

    INPUT, OUTPUT, FORWARD;

    @JsonCreator public static FirewallChain create (String v) { return valueOf(v.toUpperCase()); }

}
