package com.home.learning.poc.springlib.interactions;

public class ActionComponent {

    public void click(String locator) {
        System.out.println("The locator from Framework " + locator);
    }

    public void clickAndDisappear(String locator, String disappearingLocator){
        //Read the locator value from the DB using hte received input.
        //Change string to By (Selenium Class)
        //Find locator using findElement(Selenium Method)
        //Click on the Method,
        //Wait for the validationLocator element to arrive.
        //Report the validation locator not found or present
        System.out.println("The locator from Framework " + locator);
    }

    public void type(String locator, String data){
        System.out.println("The locator from Framework " + locator + " data: "+ data);
    }

    public String findElement(String string){
        return string;
    }
}
