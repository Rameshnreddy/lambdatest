package org.lambdatestHackathon;

import Utilities.UserActions;
import Utilities.WaitActions;
import org.openqa.selenium.By;

public class HomePage extends UserActions {
    By SecureHeader = By.xpath("//h2[normalize-space()=\"Secure Area\"]");

    public boolean isPageLoaded(){
        return new WaitActions().elementDisplayed(SecureHeader);
    }

}
