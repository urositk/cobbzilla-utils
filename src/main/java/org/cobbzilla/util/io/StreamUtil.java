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
        return stream2file(in, file);
    }

    public static File stream2file(InputStream in, File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            IOUtils.copy(in, out);
        }
        return file;
    }

    public static ByteArrayInputStream toStream(String s) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(s.getBytes(StringUtil.UTF8));
    }

    public static String toString(InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        return out.toString();
    }

    public static InputStream loadResourceAsStream(String path) throws IOException {
        return loadResourceAsStream(path, StreamUtil.class);
    }

    public static InputStream loadResourceAsStream(String path, Class clazz) throws IOException {
        InputStream in = clazz.getClassLoader().getResourceAsStream(path);
        if (in == null) throw new IOException("Resource not found: " + path);
        return in;
    }

    public static File loadResourceAsFile (String path) throws IOException {
        return loadResourceAsFile(path, StreamUtil.class);
    }

    public static File loadResourceAsFile (String path, Class clazz) throws IOException {
        final File tmp = File.createTempFile("resource", ".tmp");
        @Cleanup final FileOutputStream out = new FileOutputStream(tmp);
        IOUtils.copy(loadResourceAsStream(path, clazz), out);
        return tmp;
    }

    public static String loadResourceAsStringOrDie(String path) {
        try {
            return loadResourceAsString(path, StreamUtil.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("cannot load resource: "+path+": "+e, e);
        }
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

    /**
     * Copy the first n bytes from input to output
     * @return the number of bytes actually copied (might be less than n if EOF was reached)
     */
    public static long copyNbytes(InputStream input, OutputStream output, long n) throws IOException {
        byte[] buffer = new byte[(n > DEFAULT_BUFFER_SIZE) ? DEFAULT_BUFFER_SIZE : (int) n];
        long copied = 0;
        int read = 0;
        while (copied < n && -1 != (read = input.read(buffer, 0, (int) (n - copied > buffer.length ? buffer.length : n - copied)))) {
            output.write(buffer, 0, read);
            copied += read;
        }
        return copied;
    }

}
