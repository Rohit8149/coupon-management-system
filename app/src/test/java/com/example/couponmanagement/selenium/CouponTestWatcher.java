package com.example.couponmanagement.selenium;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CouponTestWatcher implements TestWatcher {

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String testMethod = context.getTestMethod().map(Method::getName).orElse("unknownTest");
        System.err.println("[CouponTestWatcher] TEST FAILED: " + testMethod + " -> " + cause.getMessage());

        context.getTestInstance().ifPresent(instance -> {
            if (instance instanceof HasWebDriver) {
                WebDriver driver = ((HasWebDriver) instance).getWebDriver();
                if (driver != null) {
                    try {
                        if (driver instanceof TakesScreenshot) {
                            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                            File projectFailureDir = new File("../screenshots/failures");
                            if (!projectFailureDir.exists()) {
                                projectFailureDir.mkdirs();
                            }

                            File targetFile = new File(projectFailureDir, testMethod + "_" + timestamp + ".png");
                            Files.copy(screenshot.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            System.err.println("[CouponTestWatcher] Saved failure screenshot to: " + targetFile.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        System.err.println("[CouponTestWatcher] Failed to write screenshot: " + e.getMessage());
                    } finally {
                        try {
                            driver.quit();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        String testMethod = context.getTestMethod().map(Method::getName).orElse("unknownTest");
        System.out.println("[CouponTestWatcher] TEST PASSED: " + testMethod);

        context.getTestInstance().ifPresent(instance -> {
            if (instance instanceof HasWebDriver) {
                WebDriver driver = ((HasWebDriver) instance).getWebDriver();
                if (driver != null) {
                    try {
                        driver.quit();
                    } catch (Exception ignored) {}
                }
            }
        });
    }
}
