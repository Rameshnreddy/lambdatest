package org.lambdatestHackathon;

import Constants.DataConstants;
import Utilities.ConfigReader;
import Utilities.DriverFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Base64;

public class HackathonTest extends DriverFactory implements DataConstants {
@DataProvider(name = "loginScenario",parallel = true)
public Object[][] getLoginData() {
    return new Object[][]{
            {ConfigReader.get("validUsername"),new String(Base64.getDecoder().decode(ConfigReader.get("validPassword")))},
            {"testInvalidUsername","InvalidPassword"},

    };
}
    @Test(dataProvider = "loginScenario")
    public void loginFunctionality(String username,String password){
        launchURL(ConfigReader.get(loginURL));
        LoginPage loginPage = new LoginPage();
        loginPage.loginUsingCredentials(username,password);
        Assert.assertTrue(new HomePage().isPageLoaded(),"Failed to login as Entered credentials are invalid");
    }

    @Test
    public void validateJsAlerts(){
        launchURL(ConfigReader.get(alertURL));
        AlertsPage alertsPage = new AlertsPage();
        alertsPage.clickElement(alertsPage.jsAlert,"Js Alert");
        Assert.assertEquals( "You successfully clicked an alert",alertsPage.validateAlerts(""));
        alertsPage.clickElement(alertsPage.jsConfirm,"Js Alert");
        Assert.assertEquals(  "You clicked: Ok",alertsPage.validateAlerts(""));
        alertsPage.clickElement(alertsPage.jsPrompt,"Js Alert");
        Assert.assertEquals(  "You entered: test",alertsPage.validateAlerts("test"));
    }
}