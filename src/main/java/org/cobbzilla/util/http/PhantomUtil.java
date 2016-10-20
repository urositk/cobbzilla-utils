package org.cobbzilla.util.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

import static org.cobbzilla.util.io.FileUtil.abs;

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

    public static final String LOAD_AND_EXEC = "var page = require('webpage').create();\n" +
            "page.open('@@URL@@', function() {\n" +
            "  page.evaluateJavaScript('@@JS@@');\n" +
            "});\n";

    public void loadPage (File file) { loadPageAndExec(file, "console.log('successfully loaded "+abs(file)+"')"); }
    public void loadPage(String url) { loadPageAndExec(url, "console.log('successfully loaded "+url+"')"); }

    public void loadPageAndExec(File file, String script) {
        loadPageAndExec("file://"+abs(file), script);
    }

    public void loadPageAndExec(String url, String script) {
        execJs(LOAD_AND_EXEC.replace("@@URL@@", url).replace("@@JS@@", script));
    }
}
