package org.cobbzilla.util.http;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.now;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.system.Sleep.sleep;

@Slf4j
public class HtmlScreenCapture {

    private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(5);

    private final PhantomJSDriver phantomJSDriver;

    public HtmlScreenCapture (String phantomjs) {
        final DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setJavascriptEnabled(true);
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjs);
        phantomJSDriver = new PhantomJSDriver(capabilities);
    }

    public static final String SCRIPT = "var page = require('webpage').create();\n" +
            "page.open('@@URL@@', function() {\n" +
            "  page.render('@@FILE@@');\n" +
            "});\n";

    public void capture (String url, File file) {
        phantomJSDriver.executePhantomJS(SCRIPT.replace("@@URL@@", url).replace("@@FILE@@", abs(file)));
        long start = now();
        while (file.length() == 0 && now() - start < TIMEOUT) sleep(50);
        if (file.length() == 0 && now() - start >= TIMEOUT) die("capture: file was never written to: "+abs(file));
    }

    public void capture (File in, File out) {
        try {
            capture(in.toURI().toString(), out);
        } catch (Exception e) {
            die("capture("+abs(in)+"): "+e, e);
        }
    }

}
