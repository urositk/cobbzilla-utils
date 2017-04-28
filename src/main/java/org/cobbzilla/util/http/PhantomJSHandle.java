package org.cobbzilla.util.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.ErrorHandler;

import java.io.Closeable;

@AllArgsConstructor
public class PhantomJSHandle extends ErrorHandler implements Closeable {

    @Getter final PhantomJSDriver driver;

    @Override public void close() {
        if (driver != null) {
            driver.close();
            driver.quit();
        }
    }

}