package org.cobbzilla.util.network;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum FirewallAction {

    ACCEPT, CLASSIFY, CLUSTERIP, CONNMARK, CONNSECMARK, DNAT,
    DROP, DSCP, ECN, LOG, MARK, MASQUERADE, MIRROR, NETMAP, NFQUEUE,
    NOTRACK, QUEUE, REDIRECT, REJECT, RETURN, SAME, SECMARK, SNAT,
    TCPMSS, TOS, TTL, ULOG, CUSTOM;

    @JsonCreator public FirewallAction create (String v) { return valueOf(v.toUpperCase()); }


}
