package org.cobbzilla.util.http;

import lombok.Getter;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

import static org.cobbzilla.util.io.FileUtil.abs;

public class PhantomUtil {

    @Getter private final String phantomjs;

    public PhantomUtil(String phantomjs) { this.phantomjs = phantomjs; }

    private PhantomJSDriver defaultDriver(String phantomjs) {
        final DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setJavascriptEnabled(true);
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjs);
        return new PhantomJSDriver(capabilities);
    }

    public PhantomJSHandle execJs(String script) {
        final PhantomJSDriver driver = defaultDriver(phantomjs);
        final PhantomJSHandle handle = new PhantomJSHandle(driver);
        driver.setErrorHandler(handle);
        driver.executePhantomJS(script);
        return handle;
    }

    public static final String LOAD_AND_EXEC = "var page = require('webpage').create();\n" +
            "page.open('@@URL@@', function() {\n" +
            "  page.evaluateJavaScript('@@JS@@');\n" +
            "});\n";

    public void loadPage (File file) { loadPageAndExec(file, "console.log('successfully loaded "+abs(file)+"')"); }
    public void loadPage(String url) { loadPageAndExec(url, "console.log('successfully loaded "+url+"')"); }

    public void loadPageAndExec(File file, String script) { loadPageAndExec("file://"+abs(file), script); }

    public void loadPageAndExec(String url, String script) {
        execJs(LOAD_AND_EXEC.replace("@@URL@@", url).replace("@@JS@@", script));
    }
}
