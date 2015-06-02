package org.cobbzilla.util.io;

import com.google.common.io.Files;
import lombok.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import static org.cobbzilla.util.io.FileUtil.abs;

/**
 * A directory that implements Closeable. Use lombok @Cleanup to nuke it when it goes out of scope.
 */
@Slf4j
public class TempDir extends File implements Closeable {

    private interface TempDirOverrides { boolean delete(); }

    @Delegate(excludes=TempDirOverrides.class)
    private File file;

    public TempDir () {
        super(abs(Files.createTempDir()));
        file = new File(super.getPath());
    }

    @Override public void close() throws IOException {
        if (!delete()) log.warn("close: error deleting TempDir: "+abs(file));
    }

    /**
     * Override to call 'delete', delete the entire directory.
     * @return true if the delete was successful.
     */
    @Override public boolean delete() { return FileUtils.deleteQuietly(file); }

}
