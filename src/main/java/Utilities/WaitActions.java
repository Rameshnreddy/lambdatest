package Utilities;

import org.openqa.selenium.By;

public class WaitActions extends UserActions{

public boolean elementDisplayed(By locator){
   return findElement(locator).isDisplayed();
}
}
