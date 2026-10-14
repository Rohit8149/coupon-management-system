package com.example.couponmanagement.selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Controlled demonstration of the automated failure screenshot hook.
 * This test intentionally introduces an assertion mismatch to trigger
 * CouponTestWatcher and verify that a timestamped PNG is persisted to screenshots/failures/.
 */
@org.junit.jupiter.api.Disabled("Demonstration test for failure screenshot capture; run explicitly when validating failure listener")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(CouponTestWatcher.class)
public class CouponFailureDemonstrationTest implements HasWebDriver {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @Override
    public WebDriver getWebDriver() {
        return this.driver;
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,800");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
    }

    @Test
    @DisplayName("Demonstration: Deliberate assertion failure to verify automated screenshot capture hook")
    void testDemonstrateFailureScreenshotCapture() {
        driver.get("http://localhost:" + port + "/coupons");

        // Locate code of first coupon (WELCOME20) and assert an impossible value to trigger failure watcher
        WebElement firstCode = driver.findElement(By.id("coupon-code-1"));
        assertEquals("INTENTIONAL_MISMATCH_FOR_SCREENSHOT_DEMO", firstCode.getText().trim(),
                "Deliberate assertion mismatch to trigger the automated TestWatcher screenshot listener");
    }
}
