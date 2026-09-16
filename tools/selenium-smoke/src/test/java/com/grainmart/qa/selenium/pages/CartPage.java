package com.grainmart.qa.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class CartPage extends BasePage {

    public CartPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public CartPage openAt(String baseUrl, boolean fixture) {
        open(baseUrl + (fixture ? "/cart.html" : "/cart/list"));
        waitForText("My Cart");
        return this;
    }
}
