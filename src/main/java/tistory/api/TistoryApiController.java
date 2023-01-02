package tistory.api;

import common.Util;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class TistoryApiController {

    private static final String AUTHORIZE_URL = "https://www.tistory.com/oauth/authorize";
    private static final String ACCESS_TOKEN_URL = "https://www.tistory.com/oauth/access_token";
    private static final String GET_CATEGORY_URL = "https://www.tistory.com/apis/category/list";
    private static final String IMAGE_UPLOAD_URL = "https://www.tistory.com/apis/post/attach";

    private Map<String, Optional<String>> categoryMap = new HashMap<>();

    private String authCode;
    private String accessToken;

    private void authorizeTistory() {

        String url = Util.addParametersToUrl(AUTHORIZE_URL,
                Map.of(
                        "client_id", Util.getTistoryConfigProperty("client_id"),
                        "redirect_uri", Util.getTistoryConfigProperty("redirect_url"),
                        "response_type", "code")
        );

        ChromeDriver driver = Util.getChromeDriver();

        log.info("티스토리 인증 시작");

        try {

            driver.get(url);

            String id = Util.getTistoryConfigProperty("id");
            String pw = Util.getTistoryConfigProperty("pw");

            driver.findElement(By.xpath("//a[@class=\"btn_login link_kakao_id\"]")).click();

            busyWaitForSelenium(driver, By.id("input-loginKey"));

            driver.findElement(By.id("input-loginKey")).sendKeys(id);
            driver.findElement(By.id("input-password")).sendKeys(pw);
            driver.findElement(By.xpath("//button[@type=\"submit\"]")).click();
            Thread.sleep(2000);

            busyWaitForSelenium(driver, By.xpath("//button[@class=\"confirm\"]"));

            driver.findElement(By.xpath("//button[@class=\"confirm\"]")).click();
            Thread.sleep(2000);

            String redirectUrl = driver.getCurrentUrl();
            while (!redirectUrl.contains("https://www.tistory.com/?")) {
                Thread.sleep(1000);
                redirectUrl = driver.getCurrentUrl();
            }

            if (redirectUrl.contains("error")) {
                throw new IllegalStateException("티스토리 인증 실패");
            }

            authCode = redirectUrl.substring(redirectUrl.indexOf("=") + 1, redirectUrl.indexOf("&"));

            log.info("티스토리 인증 완료");
        } catch (Exception e) {
            log.info("티스토리 인증 실패");
        } finally {
            driver.quit();
        }

    }

    private void getAccessToken() {
        String url = Util.addParametersToUrl(ACCESS_TOKEN_URL,
                Map.of(
                        "client_id", Util.getTistoryConfigProperty("client_id"),
                        "client_secret", Util.getTistoryConfigProperty("secret_key"),
                        "redirect_uri", Util.getTistoryConfigProperty("redirect_url"),
                        "code", authCode,
                        "grant_type", "authorization_code")
        );

        HttpsURLConnection connection = null;
        try {
            log.info("티스토리 access_token 획득 시작");

            connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.connect();

            if (connection.getResponseCode() != 200) {
                throw new IllegalStateException("티스토리 access_token 획득 실패");
            }

            try (InputStream in = connection.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                byte[] buf = new byte[1024 * 8];
                int length = 0;
                while ((length = in.read(buf)) != -1) {
                    out.write(buf, 0, length);
                }
                accessToken = new String(out.toByteArray(), "UTF-8");
                accessToken = accessToken.substring(accessToken.indexOf("=") + 1);
            }

            connection.disconnect();
        } catch (Exception e) {
            connection.disconnect();
            log.info("티스토리 access_token 획득 실패");
        }

    }


    private void busyWaitForSelenium(ChromeDriver driver, By locator) throws InterruptedException {
        while (true) {
            try {
                Thread.sleep(1000);
                driver.findElement(locator);
                break;
            } catch (NoSuchElementException e) {
            }
        }
    }

}
