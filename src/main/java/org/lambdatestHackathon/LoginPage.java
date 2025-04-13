package org.lambdatestHackathon;
import Utilities.UserActions;
import org.openqa.selenium.By;
import org.testng.Assert;

/**
 * Hello world!
 *
 */
public class LoginPage extends UserActions
{
    By usernameTextField = By.id("username");
    By passwordTextField = By.id("password");
    By loginPageContent = By.id("content");
    By loginButton = By.cssSelector("button[type=submit]");
public boolean isPageLoaded(){
    return findElement(loginPageContent).isDisplayed();
}
public void loginUsingCredentials(String username, String password){
        enterText(usernameTextField, username, "Username text field");
        enterText(passwordTextField, password, "password text field");
        clickElement(loginButton, "Login Button");
}
}
