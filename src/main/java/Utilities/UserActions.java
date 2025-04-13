package Utilities;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;

public class UserActions extends DriverFactory {

    public void enterText(By element, String valueToBeEntered, String elementName) {
        getTest().info("Trying to enter in "+elementName+" with value as "+valueToBeEntered);
        try {
            findElement(element).sendKeys(valueToBeEntered);
        } catch (Exception e) {
            getTest().info("Failed to enter text in Element: \n"+e);
            throw new RuntimeException(e);
        }
    }
    public WebElement findElement(By locator){

        WebElement element =null;
        try {
        element = getDriver().findElement(locator);
        getTest().info("Found an element");
        }
        catch (Exception e){
            getTest().info("Failed to fetch Element: \n"+e);
            throw new RuntimeException(e);
        }
        return element;
    }
    public List<WebElement> findElements(By locator){
        List<WebElement> elements =null;
        try {
            elements = getDriver().findElements(locator);
        }
        catch (Exception e){
            getTest().info("Failed to fetch Elements: \n"+e);
            throw new RuntimeException(e);
        }
        return elements;
    }
    public void clickElement(By locator, String locatorName){
        getTest().info("clicking on an element "+locatorName);
       try {
           getTest().info("clicking on element " + locatorName);
           findElement(locator).click();
       }
       catch (Exception e){
           clickUsingJS(locator);
       }
    }
    public void clickUsingJS(By locator){
        try {
            WebElement JSelement = DriverFactory.getDriver().findElement(locator);
            JavascriptExecutor executor = (JavascriptExecutor) DriverFactory.getDriver();
            executor.executeScript("arguments[0].click();", new Object[]{JSelement});
        }
        catch (Exception e){
            getTest().fail("failed to click on element");
        throw new RuntimeException(e);
        }
    }

    public void handleAlerts(boolean accept, String textToEnter){
        getTest().info("trying to perform actions on alert");
        Alert asd = getDriver().switchTo().alert();
        if(!textToEnter.equals("")){
            asd.sendKeys(textToEnter);
            getTest().info("Successfully enter the text "+textToEnter+" the alert");
        }
        if(accept ){
          asd.accept();
          getTest().info("Successfully accepted the alert");
        }
        else {
            asd.dismiss();
            getTest().info("Successfully dismissed the alert");
        }

    }
}
