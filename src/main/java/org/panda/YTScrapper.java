package org.panda;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

public class YTScrapper {
    private static int maxSoundTitle = 15;

    public static void setMaxSoundTitle(int maxSoundTitle) {
        YTScrapper.maxSoundTitle = maxSoundTitle;
    }
    public static void scrape(String searchKeyword) {
        System.setProperty("webdriver.chrome.driver", System.getenv("CHROME_DRIVER")); // Update path if needed

        // Set Chrome options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // Run without UI
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");  // Prevent memory issues

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        // Create JavascriptExecutor object
        JavascriptExecutor js = (JavascriptExecutor) driver;
        driver.get("https://www.youtube.com/results?search_query="+searchKeyword);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<WebElement> videoWrappers = new LinkedList<>();
        wait.until(webDriver -> {
            boolean flag = false;
            List<WebElement> elements = webDriver.findElements(By.cssSelector("ytd-video-renderer"));
            js.executeScript("arguments[0].scrollIntoView(true);", elements.get(elements.size()-1));
            if(elements.size() >= maxSoundTitle) {
                flag = true;
            }
            videoWrappers.clear();
            videoWrappers.addAll(elements);
            return flag;
        });

        videoWrappers.forEach(videoWrapper -> {
            js.executeScript("arguments[0].scrollIntoView(true);", videoWrapper.findElement(By.cssSelector("yt-image img")));
            String thumbnailLink = videoWrapper.findElement(By.cssSelector("yt-image img")).getDomAttribute("src");
            WebElement titleWrapper = videoWrapper.findElement(By.cssSelector("#title-wrapper yt-formatted-string"));
            String detailLink = "https://www.youtube.com"+videoWrapper.findElement(By.cssSelector("ytd-thumbnail a")).getDomAttribute("href")
                    .replaceAll("&.*", "");
            System.out.println(titleWrapper.getText());
            System.out.println("Thumbnail: "+thumbnailLink);
            System.out.println("Detail Link: "+detailLink);
        });
        System.out.println("Total size: "+videoWrappers.size());

        //Important part do not delete
//        List<WebElement> videoWrappers = driver.findElements(By.cssSelector("ytd-video-renderer"));
//        videoWrappers.forEach(videoWrapper -> {
//            String thumbnailLink = videoWrapper.findElement(By.cssSelector("yt-image img")).getDomAttribute("src");
//            WebElement titleWrapper = videoWrapper.findElement(By.cssSelector("#title-wrapper yt-formatted-string"));
//            System.out.println(titleWrapper.getText());
//            System.out.println("Thumbnail: "+thumbnailLink);
//        });
        driver.quit();
        System.out.println("Success");
    }
}
