package org.cobbzilla.util.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

@AllArgsConstructor
public class PhantomUtil {

    @Getter private final PhantomJSDriver driver;

    public PhantomUtil(String phantomjs) { driver = defaultDriver(phantomjs); }

    public PhantomJSDriver defaultDriver(String phantomjs) {
        PhantomJSDriver phantomJSDriver;
        final DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setJavascriptEnabled(true);
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjs);
        phantomJSDriver = new PhantomJSDriver(capabilities);
        return phantomJSDriver;
    }

    public void execJs(String script) { getDriver().executePhantomJS(script); }

    public static final String SCRIPT = "var page = require('webpage').create();\n" +
            "page.open('@@URL@@', function() {\n" +
            "  page.evaluateJavaScript('@@JS@@');\n" +
            "});\n";

    public void loadPageAndExec(String url, String script) {
        execJs(SCRIPT.replace("@@URL@@", url).replace("@@JS@@", script));
    }
}
