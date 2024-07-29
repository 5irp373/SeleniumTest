import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class TestImplementation {

    private WebDriver driver;

    @BeforeClass
    public static void setupWebdriverChromeDriver() {
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/src/main/resources/chromedriver.exe");
    }

    @Before
    public void setup() {
        driver = new ChromeDriver();
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }


    @Test
    public void assertSauceDemoTitle() {
        // Simple test of accessing main login page and if there is title
        driver.get("https://www.saucedemo.com/");
        assertThat(driver.getTitle(), containsString("Swag Labs"));
    }

    //general login method
    public void login(String inputName, String inputPassword) {
        driver.get("https://www.saucedemo.com/");

        WebElement userName = driver.findElement(By.id("user-name"));
        WebElement userPassword = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        userName.sendKeys(inputName);
        userPassword.sendKeys(inputPassword);
        loginButton.click();
    }

    @Test
    public void assertLogin() {

        login("standard_user", "secret_sauce");

        assertThat(driver.getCurrentUrl(), containsString("https://www.saucedemo.com/inventory.html"));
        assertThat(driver.findElement(By.className("inventory_list")).isDisplayed(), is(true));

        assertThat(driver.findElement(By.cssSelector("div[class=\"header_secondary_container\"] span[class=\"title\"]")).getText(), containsString("Products"));


    }

    @Test
    public void assertCannotLogin() {

        login("locked_out_user", "secret_sauce");
        assertThat(driver.findElement(By.xpath("//*[@id=\"login_button_container\"]/div/form/div[3]/h3")).getText(), containsString("Epic sadface: Sorry, this user has been locked out."));


//       System.out.println("Number of inventory_list elements: " + driver.findElements(By.className("inventory_list")).size());
        assertThat(driver.findElements(By.className("inventory_list")).size(), is(0));

//        System.out.println("Number of login_container elements: " + driver.findElements(By.className("form_group")).size());
        assertThat(driver.findElements(By.className("form_group")).size(), is(2));

    }

    @Test
    public void assertProductsShown() {
        //First attempt of assertProductShown. The idea here is to extract text from item name (label?) and alt text of images of same product.
        //Im creating temporary ArrayLists which Im filling with that date. Then I simply compare both lists if they equal.
        login("standard_user", "secret_sauce");

//        System.out.println("Number of products on page: " + driver.findElements(By.className("inventory_item")).size());
        assertThat(driver.findElements(By.className("inventory_item")).size(), is(6));


        //getting names (strings) from the Product labels and adding them to temporary arrayList
        List<WebElement> listOfProducts = driver.findElements(By.className("inventory_item_name"));
        List<String> productItemNames = new ArrayList<>();
        for (WebElement eachProduct : listOfProducts) {
            productItemNames.add(eachProduct.getText());
        }

        //getting names (strings) from the images to the temporary ArrayList
        List<WebElement> listOfIMGs = driver.findElements(By.cssSelector("[class=\"inventory_item_img\"] img"));
        List<String> namesFromImages = new ArrayList<>();
        for (WebElement eachIMG : listOfIMGs) {
            namesFromImages.add(eachIMG.getDomAttribute("alt"));
        }

//        System.out.println(productItemNames);
//        System.out.println(namesFromImages);

        //comparing the two lists (this will most probably break if the items are out of order?)
        assertTrue(productItemNames.equals(namesFromImages));

    }

    @Test
    public void assertProductsShownDifferentApproach() {
        //Second attempt of assertProductShown test. The idea here is to make a list of inventory_items and then with for loop going throug them
        //Assumption here is that each Inventory_item on page has 2 parts. The title and the image text (in same div). Im comparing these one by one using loop.
        login("standard_user", "secret_sauce");

//        System.out.println("Number of products on page: " + driver.findElements(By.className("inventory_item")).size());
        assertThat(driver.findElements(By.className("inventory_item")).size(), is(6));

        List<WebElement> inventoryList = driver.findElements(By.className("inventory_item"));

        for (WebElement itemFromList : inventoryList) {
            WebElement titleOfProduct = itemFromList.findElement(By.className("inventory_item_name"));
            WebElement imageNameOfProduct = itemFromList.findElement(By.cssSelector("[class=\"inventory_item_img\"] img"));

            String titleOfProductText = titleOfProduct.getText();
            String imageNameOfProductText = imageNameOfProduct.getDomAttribute("alt");

//            System.out.println("Title from product: " + titleOfProductText);
//            System.out.println("Title from image: " + imageNameOfProductText);

            assertTrue(titleOfProductText.equals(imageNameOfProductText));

        }


    }

    @Test
    public void completeOrder() {
        //login as standard user, add item to cart. Order the item. Check if the order was placed
        login("standard_user", "secret_sauce");

        driver.findElement(By.id("add-to-cart-sauce-labs-bolt-t-shirt")).click();
        driver.findElement(By.className("shopping_cart_link")).click();
        assertThat(driver.getCurrentUrl(), containsString("https://www.saucedemo.com/cart.html"));
        assertThat(driver.findElement(By.className("inventory_item_name")).getText(), containsString("Sauce Labs Bolt T-Shirt"));

        driver.findElement(By.id("checkout")).click();
        assertThat(driver.findElement(By.cssSelector("span[class=\"title\"]")).getText(), containsString("Checkout: Your Information"));
        assertThat(driver.getCurrentUrl(), containsString("https://www.saucedemo.com/checkout-step-one.html"));
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("37412");
        driver.findElement(By.id("continue")).click();

        assertThat(driver.findElement(By.cssSelector("span[class=\"title\"]")).getText(), containsString("Checkout: Overview"));
        assertThat(driver.getCurrentUrl(), containsString("https://www.saucedemo.com/checkout-step-two.html"));


        driver.findElement(By.id("finish")).click();

        assertThat(driver.findElement(By.cssSelector("span[class=\"title\"]")).getText(), containsString("Checkout: Complete!"));
        assertThat(driver.getCurrentUrl(), containsString("https://www.saucedemo.com/checkout-complete.html"));
        assertThat(driver.findElement(By.cssSelector("h2[class=\"complete-header\"]")).getText(), containsString("Thank you for your order!"));

        WebElement image = driver.findElement(By.cssSelector("img[class=\"pony_express\"]"));
        String imageSource = image.getAttribute("src");
        System.out.println(imageSource);
        assertFalse(imageSource.isEmpty());

    }
}