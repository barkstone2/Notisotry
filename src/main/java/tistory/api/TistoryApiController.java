package tistory.api;

import com.fasterxml.jackson.core.type.TypeReference;
import common.Util;
import lombok.extern.slf4j.Slf4j;
import notion.dto.NotionPage;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import tistory.TistoryCategory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Slf4j
public class TistoryApiController {

    private static final String AUTHORIZE_URL = "https://www.tistory.com/oauth/authorize";
    private static final String ACCESS_TOKEN_URL = "https://www.tistory.com/oauth/access_token";
    private static final String GET_CATEGORY_URL = "https://www.tistory.com/apis/category/list";
    private static final String IMAGE_UPLOAD_URL = "https://www.tistory.com/apis/post/attach";
    private static final String ARTICLE_WRITE_URL = "https://www.tistory.com/apis/post/write";
    private static final String ARTICLE_UPDATE_URL = "https://www.tistory.com/apis/post/modify";

    private Map<String, String> categoryMap = new HashMap<>();

    private String authCode;
    private String accessToken;

    public TistoryApiController() {
        String configAccessToken = Util.getTistoryConfigProperty("access_token");
        if(configAccessToken != null) accessToken = configAccessToken;
        else {
            authorizeTistory();
            getAccessToken();
        }

        initCategoryMap();
    }

    private void postArticle(NotionPage notionPage, Map<String, String> param, String url) {
        Map<String, Object> response;
        HttpsURLConnection connection = null;

        try {

            connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            OutputStream os = connection.getOutputStream();
            String jsonParam = Util.objectMapper.writeValueAsString(param);

            byte request_data[] = jsonParam.getBytes("utf-8");
            os.write(request_data);

            os.flush();
            os.close();

            connection.connect();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) throw new IllegalStateException("");

            response = Util.objectMapper.readValue(connection.getInputStream(), Map.class);

            connection.disconnect();

            response = (Map<String, Object>) response.get("tistory");

            notionPage.setArticleId(String.valueOf(response.get("postId")));
            notionPage.workSucceed();

            log.info("????????? ?????? ??????");
        } catch (Exception e) {
            connection.disconnect();
            log.info("????????? ?????? ??????");
            System.exit(0);
        }
    }

    public void updateArticle(NotionPage notionPage) {
        log.info("????????? ?????? ?????? ??????");

        Map<String, String> param = createRequestParam(notionPage);
        param.put("postId", notionPage.getArticleId());

        postArticle(notionPage, param, ARTICLE_UPDATE_URL);
    }

    public void writeNewArticle(NotionPage notionPage) {
        log.info("????????? ?????? ?????? ??????");

        postArticle(notionPage, createRequestParam(notionPage), ARTICLE_WRITE_URL);
    }

    private Map<String, String> createRequestParam(NotionPage notionPage) {
        Map<String, String> param = new HashMap<>(Map.of(
                "access_token", accessToken,
                "output", "json",
                "blogName", Util.getTistoryConfigProperty("blogName"),
                "title", notionPage.getTitle(),
                "content", notionPage.getContent(),
                "visibility", notionPage.getReleaseState(),
                "category", getCategoryIdByCategoryLabel(notionPage.getCategory()),
                "tag", notionPage.getTag(),
                "acceptComment", notionPage.isAllowComment()
        ));
        param.put("published", notionPage.getTimestamp());
        return param;
    }

    public String getCategoryIdByCategoryLabel(String categoryLabel) {
        if(categoryMap == null) throw new IllegalStateException("need to reload category");

        return categoryMap.get(categoryLabel) == null ? "0" : categoryMap.get(categoryLabel);
    }

    public String uploadImageFileAndGetReplacer(@NotNull String imageUrl) {
        log.info("---> ???????????? ????????? ?????? ?????? ??????");

        String url = Util.addParametersToUrl(IMAGE_UPLOAD_URL, Map.of(
                "access_token", accessToken,
                "blogName", Util.getTistoryConfigProperty("blogName"),
                "output", "json"
        ));

        Map<String, Object> response;
        HttpsURLConnection connection = null;

        try {

            String boundary = "^-----^";
            String LINE_FEED = "\r\n";
            String charset = "UTF-8";
            connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream outputStream = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);

            /** ?????? ???????????? ?????? ??????**/
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"" + UUID.randomUUID() + "\"").append(LINE_FEED);
            writer.append("Content-Type: " + guessImageType(imageUrl)).append(LINE_FEED);
            writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();

            BufferedInputStream bufferedInputStream = new BufferedInputStream(new URL(imageUrl).openStream());
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            bufferedInputStream.close();
            writer.append(LINE_FEED);
            writer.flush();

            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                response = Util.objectMapper.readValue(connection.getInputStream(), HashMap.class);
                response = (Map<String, Object>) response.get("tistory");

                if (!response.get("status").equals("200")) return "";

                log.info("---> ???????????? ????????? ????????? ??????");
                return response.get("replacer").toString();
            } else {
                log.info("---> ???????????? ????????? ????????? ??????");
            }

            connection.disconnect();
        } catch (Exception e) {
            connection.disconnect();
            log.info("---> ???????????? ????????? ????????? ??????");
        }

        return "";
    }

    private void authorizeTistory() {

        log.info("???????????? ?????? ?????? ??????");

        String url = Util.addParametersToUrl(AUTHORIZE_URL,
                Map.of(
                        "client_id", Util.getTistoryConfigProperty("client_id"),
                        "redirect_uri", Util.getTistoryConfigProperty("redirect_url"),
                        "response_type", "code")
        );

        ChromeDriver driver = Util.getChromeDriver();

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
                throw new IllegalStateException("???????????? ?????? ??????");
            }

            authCode = redirectUrl.substring(redirectUrl.indexOf("=") + 1, redirectUrl.indexOf("&"));

            log.info("???????????? ?????? ??????");
        } catch (Exception e) {
            log.info("???????????? ?????? ??????");
            System.exit(0);
        } finally {
            driver.quit();
        }

    }

    private void getAccessToken() {
        log.info("???????????? access_token ?????? ??????");

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

            connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.connect();

            if (connection.getResponseCode() != 200) {
                throw new IllegalStateException("???????????? access_token ?????? ??????");
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
                Util.updateConfig(accessToken);
            }

            connection.disconnect();
        } catch (Exception e) {
            connection.disconnect();
            log.info("???????????? access_token ?????? ??????");
            System.exit(0);
        }

    }

    private void initCategoryMap() {
        log.info("???????????? ???????????? ?????? ??????");

        String url = Util.addParametersToUrl(GET_CATEGORY_URL, Map.of(
                "access_token", accessToken,
                "output", "json",
                "blogName", Util.getTistoryConfigProperty("blogName")
        ));

        Map<String, Object> response = null;
        HttpsURLConnection connection = null;

        try {
            connection = (HttpsURLConnection) new URL(url).openConnection();

            connection.connect();

            response = Util.objectMapper.readValue(connection.getInputStream(), HashMap.class);
            connection.disconnect();

            response = (HashMap<String, Object>) response.get("tistory");

            if (!response.get("status").equals("200")) {
                throw new IllegalStateException("");
            }

            response = (Map<String, Object>) response.get("item");

        } catch (Exception e) {
            connection.disconnect();
            log.info("???????????? ???????????? ?????? ??????");
            System.exit(0);
        }

        List<TistoryCategory> categoryList = Util.objectMapper
                .convertValue(response.get("categories"), new TypeReference<ArrayList<TistoryCategory>>(){});

        categoryList
                .stream()
                .forEach(category -> categoryMap.put(category.getLabel(), category.getId()));
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

    private String guessImageType(String imageUrl) {
        if(imageUrl.contains(".png")) return "image/png";
        else if(imageUrl.contains(".webp")) return "image/webp";
        else if(imageUrl.contains(".gif")) return "image/gif";
        else if(imageUrl.contains(".jpeg") || imageUrl.contains(".jpg")) return "image/jpeg";
        return "application/octet-stream";
    }


}