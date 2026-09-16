package com.grainmart.qa.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class HomePage extends BasePage {

    public HomePage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public HomePage openAt(String baseUrl, boolean fixture) {
        open(fixture ? baseUrl + "/index.html" : trailingSlash(baseUrl));
        waitForText("GrainMart");
        return this;
    }

    private static String trailingSlash(String url) {
        return url.endsWith("/") ? url : url + "/";
    }
}
