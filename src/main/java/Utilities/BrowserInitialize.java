package Utilities;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;

import java.net.URL;

public class BrowserInitialize {
        public static WebDriver initBrowser(String platform, String browser,String os,String version) {
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

                    DesiredCapabilities caps = new DesiredCapabilities();

                    switch (browser.toLowerCase()) {
                        case "chrome":
                            caps.setCapability("browserName", "chrome");
                            caps.setCapability("version", "latest");
                            caps.setCapability("build", ConfigReader.get("AutomationTitleName"));
                            caps.setCapability("name", ConfigReader.get("AutomationName"));
                            caps.setCapability("w3c", true);
                            caps.setCapability("plugin", "java-testNG");
                            break;

                        case "firefox":
                            caps.setCapability("browserName", "Firefox");
                            break;

                        case "edge":
                            caps.setCapability("browserName", "Edge");
                            break;

                        default:
                            throw new IllegalArgumentException("Unsupported browser for cloud: " + browser);
                    }

                    caps.setCapability("os", os);
                    caps.setCapability("osVersion", version);

                    // Sample BrowserStack credentials
                    String username = ConfigReader.get("LT_USERNAME");
                    String accessKey = ConfigReader.get("LT_ACCESS_KEY");

                    driver =new RemoteWebDriver( new URL("https://"+username+":"+accessKey+"@hub.lambdatest.com/wd/hub"),caps);
                    //driver = new RemoteWebDriver(remoteUrl, caps);https://rameshnn007:LT_o8l2e8EXBuAoLjmlHJS9y1emf2Bv96mygSCFEjmlS9TCOMi@hub.lambdatest.com/wd/hub
                }

            } catch (Exception e) {
                Assert.fail(e.toString());
            }
            return driver;
        }

    }

