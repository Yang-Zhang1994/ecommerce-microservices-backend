package com.grainmart.qa.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    protected void open(String url) {
        driver.get(url);
    }

    protected void waitForText(String text) {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(normalize-space(.),'" + text + "')]")));
    }

    protected void clickButton(String label) {
        driver.findElement(By.xpath("//button[normalize-space()='" + label + "']")).click();
    }
}
