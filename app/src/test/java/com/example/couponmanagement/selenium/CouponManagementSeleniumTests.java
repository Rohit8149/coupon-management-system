package com.example.couponmanagement.selenium;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(CouponTestWatcher.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CouponManagementSeleniumTests implements HasWebDriver {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @Override
    public WebDriver getWebDriver() {
        return this.driver;
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,850");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
    }


    @Test
    @Order(1)
    @DisplayName("Journey 1: Admin navigates to /create, fills form, asserts coupon is created and appears in /coupons")
    void testJourney1_CreateCouponAndVerifyInDirectory() {
        driver.get(baseUrl + "/coupons/create");

        // Verify page header
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(), 'Create Promotional Coupon')]")));
        assertNotNull(header, "Create coupon page header should be visible");

        // Fill form fields
        String testCode = "SELENIUM" + (int)(Math.random() * 900 + 100);
        driver.findElement(By.id("code")).sendKeys(testCode);

        Select discountTypeSelect = new Select(driver.findElement(By.id("discountType")));
        discountTypeSelect.selectByValue("PERCENTAGE");

        driver.findElement(By.id("discountValue")).clear();
        driver.findElement(By.id("discountValue")).sendKeys("25.0");

        driver.findElement(By.id("minOrderAmount")).clear();
        driver.findElement(By.id("minOrderAmount")).sendKeys("75.0");

        driver.findElement(By.id("usageLimit")).clear();
        driver.findElement(By.id("usageLimit")).sendKeys("300");

        // Use JavascriptExecutor to set HTML5 date input safely
        WebElement expiryDateInput = driver.findElement(By.id("expiryDate"));
        String futureDate = LocalDate.now().plusMonths(6).toString();
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", expiryDateInput, futureDate);

        driver.findElement(By.id("description")).sendKeys("Selenium automated campaign promotion voucher");

        // Submit form
        driver.findElement(By.id("btnSubmitCoupon")).click();

        // Assert redirected to /coupons
        wait.until(ExpectedConditions.urlContains("/coupons"));
        assertTrue(driver.getCurrentUrl().contains("/coupons"), "Should redirect to coupons directory");

        // Assert success alert
        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("alertSuccess")));
        assertTrue(successAlert.getText().contains(testCode), "Success banner should confirm created coupon code");

        // Assert new coupon code appears in table
        List<WebElement> rows = driver.findElements(By.xpath("//span[contains(@id, 'coupon-code-')]"));
        boolean foundCode = rows.stream().anyMatch(e -> e.getText().trim().equalsIgnoreCase(testCode));
        assertTrue(foundCode, "Newly created coupon " + testCode + " must be present in the directory table");
    }

    @Test
    @Order(2)
    @DisplayName("Journey 2: Validation gate: Submitting duplicate code triggers error message")
    void testJourney2_ValidationGate_DuplicateCode() {
        driver.get(baseUrl + "/coupons/create");

        // Attempt to create a coupon with already-seeded code 'WELCOME20'
        driver.findElement(By.id("code")).sendKeys("WELCOME20");

        Select discountTypeSelect = new Select(driver.findElement(By.id("discountType")));
        discountTypeSelect.selectByValue("PERCENTAGE");

        driver.findElement(By.id("discountValue")).clear();
        driver.findElement(By.id("discountValue")).sendKeys("15.0");

        driver.findElement(By.id("minOrderAmount")).clear();
        driver.findElement(By.id("minOrderAmount")).sendKeys("50.0");

        driver.findElement(By.id("usageLimit")).clear();
        driver.findElement(By.id("usageLimit")).sendKeys("100");

        WebElement expiryDateInput = driver.findElement(By.id("expiryDate"));
        String futureDate = LocalDate.now().plusMonths(3).toString();
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", expiryDateInput, futureDate);

        driver.findElement(By.id("description")).sendKeys("Attempt to submit duplicate WELCOME20 promo code");

        driver.findElement(By.id("btnSubmitCoupon")).click();

        // Assert error banner appears
        WebElement errorAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("alertError")));
        assertNotNull(errorAlert, "Error alert banner must be displayed on duplicate code submission");
        assertTrue(errorAlert.getText().toLowerCase().contains("already exists"), 
                "Error alert should explain that coupon code already exists");
    }

    @Test
    @Order(3)
    @DisplayName("Journey 3: Search & filter: User enters search term and asserts table filters correctly")
    void testJourney3_SearchAndFilterDirectory() {
        driver.get(baseUrl + "/coupons");

        // 1. Search by specific code
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchInput")));
        searchInput.clear();
        searchInput.sendKeys("WELCOME20");

        driver.findElement(By.id("btnSearchSubmit")).click();

        // Assert only WELCOME20 is visible in the filtered results
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("couponTable")));
        List<WebElement> codeElements = driver.findElements(By.xpath("//span[contains(@id, 'coupon-code-')]"));
        assertFalse(codeElements.isEmpty(), "Search result should contain at least 1 match");
        for (WebElement elem : codeElements) {
            assertEquals("WELCOME20", elem.getText().trim(), "All filtered items should match search query WELCOME20");
        }

        // 2. Filter by status 'ACTIVE'
        driver.get(baseUrl + "/coupons");
        Select statusFilter = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("statusFilter"))));
        statusFilter.selectByValue("ACTIVE");

        driver.findElement(By.id("btnSearchSubmit")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("couponTable")));
        List<WebElement> statusBadges = driver.findElements(By.xpath("//span[contains(@id, 'coupon-status-')]"));
        assertFalse(statusBadges.isEmpty(), "Active filter should yield coupons");
        for (WebElement badge : statusBadges) {
            assertEquals("ACTIVE", badge.getText().trim(), "All items returned by ACTIVE filter must have ACTIVE status");
        }
    }

    @Test
    @Order(4)
    @DisplayName("Journey 4: Status action: User clicks 'Activate', 'Redeem', or 'Expire' and verifies status tag")
    void testJourney4_StatusLifecycleTransitions() {
        driver.get(baseUrl + "/coupons");

        // Find FESTIVE10 which is seeded as DRAFT (id=3 in seed data)
        WebElement activateBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnActivate-3")));
        assertNotNull(activateBtn, "Activate button should be present for DRAFT coupon");
        activateBtn.click();

        // After activation, verify status badge transitions to ACTIVE
        wait.until(ExpectedConditions.urlContains("/coupons"));
        WebElement statusBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("coupon-status-3")));
        assertEquals("ACTIVE", statusBadge.getText().trim(), "Coupon status badge must transition from DRAFT to ACTIVE");

        // Now verify Redeem action is available for ACTIVE coupon
        WebElement redeemBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnRedeem-3")));
        assertNotNull(redeemBtn, "Redeem button should be present for ACTIVE coupon");
        redeemBtn.click();

        // Verify success alert or updated redemption count
        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("alertSuccess")));
        assertTrue(successAlert.getText().toLowerCase().contains("redeemed") || successAlert.getText().toLowerCase().contains("success"),
                "Redemption success message should appear");

        // Now test Expire transition
        WebElement expireBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnExpire-3")));
        expireBtn.click();

        wait.until(ExpectedConditions.urlContains("/coupons"));
        WebElement expiredStatusBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("coupon-status-3")));
        assertEquals("EXPIRED", expiredStatusBadge.getText().trim(), "Coupon status badge must transition to EXPIRED");
    }
}
