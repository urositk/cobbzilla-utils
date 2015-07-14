package org.cobbzilla.util.system;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

public class NetworkUtil {

    public static String getLocalhostIpv4 () {
        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface i = interfaces.nextElement();
                if (i.getName().startsWith("lo")) {
                    final Enumeration<InetAddress> addrs = i.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        final String addr = addrs.nextElement().toString();
                        if (addr.startsWith("/127")) return addr.substring(1);
                    }
                }
            }
            return die("getLocalhostIpv4: no local 127.x.x.x address found");

        } catch (Exception e) {
            return die("getLocalhostIpv4: "+e, e);
        }
    }
}
