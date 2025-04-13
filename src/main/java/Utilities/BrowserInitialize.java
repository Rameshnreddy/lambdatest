package Utilities;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;

public class BrowserInitialize {
        public static WebDriver initBrowser(String platform, String browser, String os, String version) {
            RemoteWebDriver driver = null;

            try {
                if (platform.equalsIgnoreCase("local")) {
                    switch (browser.toLowerCase()) {
                        case "chrome":
                             driver = new ChromeDriver();
                            break;

                        case "firefox":
                            driver = new FirefoxDriver();
                            driver.manage().window().maximize();
                            break;

                        case "edge":
                            driver = new EdgeDriver();
                            driver.manage().window().maximize();
                            break;

                        default:
                            throw new IllegalArgumentException("Browser not supported: " + browser);
                    }
                }

                else if (platform.equalsIgnoreCase("cloud")) {

                    HashMap<String, Object> ltOptions = new HashMap<String, Object>();
                    ltOptions.put("username", ConfigReader.get("LT_USERNAME"));
                    ltOptions.put("accessKey", ConfigReader.get("LT_ACCESS_KEY"));
                    ltOptions.put("project", ConfigReader.get("AutomationName"));
                    ltOptions.put("w3c", true);
                    ltOptions.put("plugin", "java-testNG");
                    ltOptions.put("name", version);
                    ltOptions.put("build", ConfigReader.get("AutomationName"));
                    switch (browser.toLowerCase()) {
                        case "chrome":
                            ChromeOptions browserOptions = new ChromeOptions();
                            browserOptions.setPlatformName(os);
                            browserOptions.setBrowserVersion("latest");
                            browserOptions.setCapability("LT:Options", ltOptions);
                            driver = new RemoteWebDriver(new URL("https://hub.lambdatest.com/wd/hub"),browserOptions);

                            break;

                        case "firefox":
                            FirefoxOptions fbrowserOptions = new FirefoxOptions();
                            fbrowserOptions.setCapability("browserName", "Firefox");
                            fbrowserOptions.setPlatformName("Windows 11");
                            fbrowserOptions.setBrowserVersion("latest");
                            driver = new RemoteWebDriver(new URL("https://hub.lambdatest.com/wd/hub"),fbrowserOptions);

                            break;

                        case "edge":
                            EdgeOptions ebrowserOptions = new EdgeOptions();
                            ebrowserOptions.setCapability("browserName", "Edge");
                            ebrowserOptions.setPlatformName("Windows 11");
                            ebrowserOptions.setBrowserVersion("latest");
                            driver = new RemoteWebDriver(new URL("https://hub.lambdatest.com/wd/hub"),ebrowserOptions);

                            break;

                        default:
                            throw new IllegalArgumentException("Unsupported browser for cloud: " + browser);
                    }   }

            } catch (Exception e) {
                Assert.fail(e.toString());
            }
            return driver;
        }

    }

