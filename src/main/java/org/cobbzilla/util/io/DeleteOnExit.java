package org.cobbzilla.util.io;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
public class DeleteOnExit implements Runnable {

    private Logger log;
    private File path;

    @Override
    public void run() {
        if (!path.exists()) return;
        if (path.isDirectory()) {
            try {
                FileUtils.deleteDirectory(path);
            } catch (IOException e) {
                log.warn("FileUtil.deleteOnExit: error deleting path="+path+": "+e, e);
            }
        } else {
            if (!path.delete()) {
                log.warn("FileUtil.deleteOnExit: error deleting path="+path);
            }
        }
    }

    public static void schedule(Logger log, File path) {
        Runtime.getRuntime().addShutdownHook(new Thread(new DeleteOnExit(log, path)));
    }
}
