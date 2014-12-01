package org.cobbzilla.util.io;

import com.google.common.io.Files;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;

import java.io.*;

@Slf4j
public class Tarball {

    /**
     * @param tarball the tarball to unroll. Can be .tar.gz or .tar.bz2
     * @return a File representing the temp directory where the tarball was unrolled
     */
    public static File unroll (File tarball) throws Exception {
        File tempDirectory = Files.createTempDir();
        try {
            return unroll(tarball, tempDirectory);
        } catch (Exception e) {
            FileUtils.deleteDirectory(tempDirectory);
            throw e;
        }
    }

    public static File unroll(File tarball, File dir) throws IOException, ArchiveException {

        final String path = tarball.getAbsolutePath();
        final FileInputStream fileIn = new FileInputStream(tarball);
        final CompressorInputStream zipIn;

        if (path.endsWith(".gz") || path.endsWith(".tgz")) {
            zipIn = new GzipCompressorInputStream(fileIn);

        } else if (path.endsWith(".bz2")) {
            zipIn = new BZip2CompressorInputStream(fileIn);

        } else {
            log.warn("tarball (" + path + ") was not .tar.gz, .tgz, or .tar.bz2, assuming .tar.gz");
            zipIn = new GzipCompressorInputStream(fileIn);
        }

        @Cleanup final TarArchiveInputStream tarIn
                = (TarArchiveInputStream) new ArchiveStreamFactory()
                .createArchiveInputStream("tar", zipIn);

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

            // when "./" gets squashed to "", we skip the entry
            if (name.trim().length() == 0) continue;

            try (OutputStream out = new FileOutputStream(new File(dir, name))) {
                if (StreamUtil.copyNbytes(tarIn, out, entry.getSize()) != entry.getSize()) {
                    throw new IllegalStateException("Expected to copy "+entry.getSize()+ " bytes for "+entry.getName()+" in tarball "+ path);
                }
            }
        }

        return dir;
    }

}
