package org.panda;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class YTScrapper {
    private static int maxSoundTitle = 15;

    public static void setMaxSoundTitle(int maxSoundTitle) {
        YTScrapper.maxSoundTitle = maxSoundTitle;
    }
    public static List<Map<String, String>> scrape(String searchKeyword) {
        List<Map<String, String>> videoLinkList = new LinkedList<>();
        System.setProperty("webdriver.chrome.driver", System.getenv("CHROME_DRIVER")); // Update path if needed

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // Run without UI
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");  // Prevent memory issues

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
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
            Map<String, String> detailMap = new LinkedHashMap<>();
            detailMap.put("title", titleWrapper.getText());
            detailMap.put("thumbnailLink", thumbnailLink);
            detailMap.put("detailLink", detailLink);
            videoLinkList.add(detailMap);
        });
        System.out.println("Total size: "+videoWrappers.size());
        driver.quit();
        System.out.println("Success");
        return videoLinkList;
    }

    public static String getVideoStreamUrl(String videoUrl) {
        return executeYtDlpCommand(new String[]{"yt-dlp", "-g", "--no-warnings", videoUrl});
    }
    public static String getAudioStreamUrl(String videoUrl) {
        return executeYtDlpCommand(new String[]{"yt-dlp", "-g", "-f", "bestaudio", "--no-warnings", videoUrl});
    }

    private static String executeYtDlpCommand(String[] command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return output.toString().trim();
            } else {
                System.err.println("Failed to extract video stream URL. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
