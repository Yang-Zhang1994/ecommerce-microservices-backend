package com.grainmart.qa.selenium;

import com.grainmart.qa.selenium.pages.CartPage;
import com.grainmart.qa.selenium.pages.HomePage;
import com.grainmart.qa.selenium.pages.LoginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Same checkout-path assertions as gulimall-mall/e2e/checkout-path.smoke.spec.ts
 * (home → cart → empty login validation). Default target: UI-contract fixture.
 *
 * <pre>
 *   E2E_BASE_URL=http://127.0.0.1:4173 mvn -f tools/selenium-smoke/pom.xml test
 * </pre>
 */
class CheckoutPathSmokeTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeAll
    static void setupDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void openBrowser() {
        baseUrl = System.getenv().getOrDefault("E2E_BASE_URL", "http://127.0.0.1:4173");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--disable-gpu", "--window-size=1280,800");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void quit() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void homeCartEmptyLoginShowsValidation() {
        boolean fixture = !Boolean.parseBoolean(System.getenv().getOrDefault("E2E_LIVE", "false"));

        new HomePage(driver, wait).openAt(baseUrl, fixture);
        new CartPage(driver, wait).openAt(baseUrl, fixture);
        LoginPage login = new LoginPage(driver, wait)
                .openAt(baseUrl, fixture)
                .submitEmptyCredentials();

        assertTrue(
                login.showsEmptyCredentialsError(),
                "empty login should show validation error");
    }
}
