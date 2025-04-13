package org.lambdatestHackathon;

import Utilities.UserActions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

public class AlertsPage extends UserActions {

    By jsAlert = By.cssSelector("button[onclick=\"jsAlert()\"]");
    By jsConfirm = By.cssSelector("button[onclick=\"jsConfirm()\"]");
    By jsPrompt = By.cssSelector("button[onclick=\"jsPrompt()\"]");
    By results = By.id("result");

    public String validateAlerts(String textToEnter){
        handleAlerts(true,textToEnter);
       return findElement(results).getText();

    }
}
