package org.cobbzilla.util.io;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {

    public static final File DEFAULT_TEMPDIR = new File(System.getProperty("java.io.tmpdir"));

    public static File createTempDir(String prefix) throws IOException {
        return createTempDir(DEFAULT_TEMPDIR, prefix);
    }

    public static File createTempDir(File parentDir, String prefix) throws IOException {
        Path parent = FileSystems.getDefault().getPath(parentDir.getAbsolutePath());
        return new File(Files.createTempDirectory(parent, prefix).toAbsolutePath().toString());
    }

    public static void writeResourceToFile(String resourcePath, File outFile, Class clazz) throws IOException {
        if (!outFile.getParentFile().exists() || !outFile.getParentFile().canWrite() || (outFile.exists() && !outFile.canWrite())) {
            throw new IllegalArgumentException("outFile is not writeable: "+outFile.getAbsolutePath());
        }
        try (InputStream in = clazz.getClassLoader().getResourceAsStream(resourcePath);
             OutputStream out = new FileOutputStream(outFile)) {
            if (in == null) throw new IllegalArgumentException("null data at resourcePath: "+resourcePath);
            IOUtils.copy(in, out);
        }
    }

}
