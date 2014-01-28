package org.cobbzilla.util.system;

import java.io.IOException;
import java.net.ServerSocket;

public class PortPicker {
    public static int pick () throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }
}
