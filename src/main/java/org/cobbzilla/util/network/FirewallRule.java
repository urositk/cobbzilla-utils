package org.cobbzilla.util.network;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public class FirewallRule {

    @Getter @Setter Integer port;
    @Getter @Setter TransportProtocol protocol = TransportProtocol.tcp;
    @Getter @Setter NetworkInterfaceType iface = NetworkInterfaceType.world;
    @Getter @Setter FirewallChain chain = FirewallChain.INPUT;
    @Getter @Setter FirewallAction action = FirewallAction.ACCEPT;

}
