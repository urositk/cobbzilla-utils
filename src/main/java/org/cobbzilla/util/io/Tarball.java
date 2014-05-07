package org.cobbzilla.util.io;

import com.google.common.io.Files;
import lombok.Cleanup;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;

public class Tarball {

    /**
     * @param tarball the tarball to unroll
     * @return a File representing the temp directory where the tarball was unrolled
     */
    public static File unroll (File tarball) throws IOException, ArchiveException {
        File tempDirectory = Files.createTempDir();
        return unroll(tarball, tempDirectory);
    }

    public static File unroll(File tarball, File dir) throws IOException, ArchiveException {

        @Cleanup final TarArchiveInputStream tarIn
                = (TarArchiveInputStream) new ArchiveStreamFactory()
                .createArchiveInputStream("tar", new GzipCompressorInputStream(new FileInputStream(tarball)));

        TarArchiveEntry entry;
        while ((entry = tarIn.getNextTarEntry()) != null) {
            String name = entry.getName();
            if (name.startsWith("./")) name = name.substring(2);
            if (name.startsWith("/")) name = name.substring(1); // "root"-based files just go into current dir
            if (name.endsWith("/")) {
                final String subdirName = name.substring(0, name.length() - 1);
                final File subdir = new File(dir, subdirName);
                if (!subdir.mkdirs()) {
                    throw new IllegalStateException("Error creating directory: "+subdir.getAbsolutePath());
                }
                continue;
            }
            try (OutputStream out = new FileOutputStream(new File(dir, name))) {
                if (StreamUtil.copyNbytes(tarIn, out, entry.getSize()) != entry.getSize()) {
                    throw new IllegalStateException("Expected to copy "+entry.getSize()+ " bytes for "+entry.getName()+" in tarball "+tarball.getAbsolutePath());
                }
            }
        }

        return dir;
    }

}
