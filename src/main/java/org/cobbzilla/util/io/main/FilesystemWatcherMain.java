package org.cobbzilla.util.io.main;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cobbzilla.util.io.DamperedCompositeBufferedFilesystemWatcher;
import org.cobbzilla.util.main.BaseMain;
import org.cobbzilla.util.system.CommandShell;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.file.WatchEvent;
import java.util.List;

@Slf4j
public class FilesystemWatcherMain extends BaseMain<FilesystemWatcherMainOptions> {

    public static final DateTimeFormatter DFORMAT = DateTimeFormat.forPattern("yyyy-MMM-dd HH:mm:ss");

    public static void main (String[] args) { main(FilesystemWatcherMain.class, args); }

    protected void run() throws Exception {

        final FilesystemWatcherMainOptions options = getOptions();

        final DamperedCompositeBufferedFilesystemWatcher watcher = new DamperedCompositeBufferedFilesystemWatcher
                (options.getTimeout(), options.getMaxEvents(), options.getWatchPaths(), options.getDamperMillis()) {
            @Override public void uber_fire(List<WatchEvent<?>> events) {
                try {
                    if (options.hasCommand()) {
                        CommandShell.exec(new CommandLine(options.getCommand()));
                    } else {
                        final String msg = status() + " uber_fire ("+events.size()+" events) at " + DFORMAT.print(System.currentTimeMillis());
                        log.info(msg);
                        System.out.println(msg);
                    }
                } catch (Exception e) {
                    final String msg = status() + " Error running command (" + options.getCommand() + "): "
                            + e + "\n" + ExceptionUtils.getStackTrace(e);
                    log.error(msg, e);
                    System.err.println(msg);
                }
            }
        };

        synchronized (watcher) {
            watcher.wait();
        }
    }

}
