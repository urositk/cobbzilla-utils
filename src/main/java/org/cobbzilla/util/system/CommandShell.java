package org.cobbzilla.util.system;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CommandShell {

    protected static final String EXPORT_PREFIX = "export ";

    public static Map<String, String> loadShellExports (String userFile) throws IOException {
        File file = new File(System.getProperty("user.home") + File.separator + userFile);
        if (!file.exists()) {
            throw new IllegalArgumentException("file does not exist: "+file.getAbsolutePath());
        }
        return loadShellExports(file);
    }

    public static Map<String, String> loadShellExports (File f) throws IOException {
        final Map<String, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
            String line, key, value;
            int eqPos;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) continue;
                if (line.startsWith(EXPORT_PREFIX)) {
                    line = line.substring(EXPORT_PREFIX.length()).trim();
                    eqPos = line.indexOf('=');
                    if (eqPos != -1) {
                        key = line.substring(0, eqPos).trim();
                        value = line.substring(eqPos+1).trim();
                        map.put(key, value);
                    }
                }
            }
        }
        return map;
    }

}
