package org.cobbzilla.util.io;

import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.cobbzilla.util.string.StringUtil;

import java.io.*;

public class StreamUtil {

    public static final String SUFFIX = ".tmp";
    public static final String PREFIX = "stream2file";

    public static File stream2file (InputStream in) throws IOException {
        return stream2file(in, false);
    }

    public static File stream2temp (InputStream in) throws IOException {
        return stream2file(in, true);
    }

    public static File stream2file (InputStream in, boolean temp) throws IOException {
        final File file = File.createTempFile(PREFIX, SUFFIX);
        if (temp) file.deleteOnExit();
        try (OutputStream out = new FileOutputStream(file)) {
            IOUtils.copy(in, out);
        }
        return file;
    }

    public static InputStream loadResourceAsStream(String path) throws IOException {
        return loadResourceAsStream(path, StreamUtil.class);
    }

    public static InputStream loadResourceAsStream(String path, Class clazz) throws IOException {
        InputStream in = clazz.getClassLoader().getResourceAsStream(path);
        if (in == null) throw new IOException("Resource not found: " + path);
        return in;
    }

    public static String loadResourceAsString(String path) throws IOException {
        return loadResourceAsString(path, StreamUtil.class);
    }

    public static String loadResourceAsString(String path, Class clazz) throws IOException {
        @Cleanup final InputStream in = loadResourceAsStream(path, clazz);
        @Cleanup final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        return out.toString(StringUtil.UTF8);
    }

}
