package Utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;

import static Utilities.BrowserInitialize.initBrowser;

public class DriverFactory {
    private static final ThreadLocal<WebDriver> threadLocalDriver = new ThreadLocal<>();
    private static final ThreadLocal<ExtentTest> threadLocalTest = new ThreadLocal<>();
    private static ExtentReports extent;
    private static ExtentHtmlReporter htmlReporter;
    private FixWithAIPrompt fixWithAIHelper;
    public static WebDriver getDriver() {
        return threadLocalDriver.get();
    }
    public static ExtentTest getTest() {
        return threadLocalTest.get();
    }

    @BeforeSuite
    public synchronized void setUp() {
        String reportPath = System.getProperty("user.dir") + "/test-output/ExtentReport.html";
        
        // Ensure ExtentReports is created only once
        if (extent == null) {
            htmlReporter = new ExtentHtmlReporter(reportPath);
            htmlReporter.config().setDocumentTitle(ConfigReader.get("AutomationTitleName"));
            htmlReporter.config().setReportName(ConfigReader.get("AutomationName"));
            
            extent = new ExtentReports();
            extent.attachReporter(htmlReporter);
        }
        try {
            FixWithAIPrompt.cleanupAIFixFolder();
            System.out.println("AI Prompt folder cleaned up successfully before test suite.");
        } catch (Exception e) {
            System.err.println("Error cleaning up AI Prompt folder: " + e.getMessage());
        }
    }

    @BeforeMethod
    public void setUpMethod(Method method) {
        // Create a new ExtentTest for each thread
        ExtentTest test = extent.createTest(method.getName());
        threadLocalTest.set(test);
        String platform = System.getProperty("platform","cloud");
        String browser = System.getProperty("browser","chrome");
        String os = System.getProperty("os", "Windows");

        threadLocalDriver.set(initBrowser(platform,browser,os,method.getName()));
        fixWithAIHelper = new FixWithAIPrompt();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        WebDriver driver = threadLocalDriver.get();
        ExtentTest test = threadLocalTest.get();

        try {
            if (result.getStatus() == ITestResult.FAILURE) {
                fixWithAIHelper.onTestFailure(result);
                test.fail("Test Failed.");
            } else if (result.isSuccess()) {
                test.pass("Test Passed.");
            }
        } finally {
            // Always ensure driver is closed and thread-local variables are removed
            if (driver != null) {
                String status = result.isSuccess() ? "passed" : "failed";
                //String reason = result.getThrowable() != null ? result.getThrowable().getMessage() : "Test passed successfully";

                ((JavascriptExecutor) driver).executeScript("lambda-status=" + status);
                driver.quit();
            }
            threadLocalDriver.remove();
            threadLocalTest.remove();
        }
    }

    @AfterSuite
    public synchronized void flushReport() {
        if (extent != null) {
            extent.flush();
        }
    }

    public void launchURL(String url){
        getDriver().get(url);
        threadLocalTest.get().info("Opening browser and navigating to the URL");
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }
}