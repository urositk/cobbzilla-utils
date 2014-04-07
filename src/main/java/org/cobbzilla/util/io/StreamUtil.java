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

    public static ByteArrayInputStream toStream(String publicKey) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(publicKey.getBytes(StringUtil.UTF8));
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

    public static Reader loadResourceAsReader(String resourcePath, Class clazz) throws IOException {
        return new InputStreamReader(loadResourceAsStream(resourcePath, clazz));
    }

    public static final int DEFAULT_BUFFER_SIZE = 32 * 1024;

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copyLarge(input, output, DEFAULT_BUFFER_SIZE);
    }

    public static long copyLarge(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
