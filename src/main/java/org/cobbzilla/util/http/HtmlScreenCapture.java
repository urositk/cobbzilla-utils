package org.cobbzilla.util.http;

import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.io.StreamUtil;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.now;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.string.StringUtil.getPackagePath;
import static org.cobbzilla.util.system.Sleep.sleep;
import static org.cobbzilla.util.time.TimeUtil.formatDuration;

@Slf4j
public class HtmlScreenCapture extends PhantomUtil {

    private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(15);

    public HtmlScreenCapture (String phantomJsPath) { super(phantomJsPath); }
    public HtmlScreenCapture (PhantomJSDriver driver) { super(driver); }

    public static final String SCRIPT = StreamUtil.loadResourceAsStringOrDie(getPackagePath(HtmlScreenCapture.class)+"/html_screen_capture.js");

    public synchronized void capture (String url, File file) { capture(url, file, TIMEOUT); }

    public synchronized void capture (String url, File file, long timeout) {
        execJs(SCRIPT.replace("@@URL@@", url).replace("@@FILE@@", abs(file)));
        long start = now();
        while (file.length() == 0 && now() - start < timeout) sleep(100);
        if (file.length() == 0 && now() - start >= timeout) {
            die("capture: after "+formatDuration(timeout)+" file was never written to: "+abs(file));
        }
    }

    public void capture (File in, File out) {
        try {
            capture(in.toURI().toString(), out);
        } catch (Exception e) {
            die("capture("+abs(in)+"): "+e, e);
        }
    }

}
