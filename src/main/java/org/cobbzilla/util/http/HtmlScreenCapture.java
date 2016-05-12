package org.cobbzilla.util.http;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

import static org.cobbzilla.util.io.FileUtil.abs;

@Slf4j
public class HtmlScreenCapture {

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
        log.info("capture: captured URL "+url+" -> "+abs(file));
    }

}
