package com.grainmart.qa.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class LoginPage extends BasePage {

    private static final String EMPTY_CREDENTIALS_ERROR =
            "Please enter username and password";

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public LoginPage openAt(String baseUrl, boolean fixture) {
        open(baseUrl + (fixture ? "/login.html" : "/login"));
        waitForText("Sign in");
        return this;
    }

    public LoginPage submitEmptyCredentials() {
        clickButton("Sign in");
        waitForText(EMPTY_CREDENTIALS_ERROR);
        return this;
    }

    public boolean showsEmptyCredentialsError() {
        return driver.getPageSource().contains(EMPTY_CREDENTIALS_ERROR);
    }
}
