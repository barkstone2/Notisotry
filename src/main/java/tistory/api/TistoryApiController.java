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

    private Map<String, String> categoryMap = new HashMap<>();

    private String authCode;
    private String accessToken;

    public TistoryApiController() {
        authorizeTistory();
        getAccessToken();
        initCategoryMap();
    }

    public void writeNewArticle(NotionPage notionPage) {
        log.info("게시글 등록 로직 시작");

        Map<String, String> param = Map.of(
                "access_token", accessToken,
                "output", "json",
                "blogName", Util.getTistoryConfigProperty("blogName"),
                "title", notionPage.getTitle(),
                "content", notionPage.getContent(),
                "visibility", notionPage.getReleaseState(),
                "category", getCategoryIdByCategoryLabel(notionPage.getCategory()),
                "published", notionPage.getReleaseDate(),
                "tag", notionPage.getTag(),
                "acceptComment", notionPage.isAllowComment()
        );

        String url = ARTICLE_WRITE_URL;

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

            response = Util.objectMapper.readValue(connection.getInputStream(), Map.class);
            connection.disconnect();

            response = (Map<String, Object>) response.get("tistory");

            if (!response.get("status").equals("200")) {
                throw new IllegalStateException("");
            }

            notionPage.uploadSuccess();

            log.info("게시글 등록 성공");
        } catch (Exception e) {
            connection.disconnect();
            log.info("게시글 등록 실패");
            System.exit(0);
        }

    }

    public String getCategoryIdByCategoryLabel(String categoryLabel) {
        if(categoryMap == null) throw new IllegalStateException("need to reload category");

        return categoryMap.get(categoryLabel) == null ? "0" : categoryMap.get(categoryLabel);
    }

    public String uploadImageFileAndGetReplacer(@NotNull String imageUrl) {
        log.info("---> 티스토리 이미지 첨부 로직 시작");

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

            /** 파일 데이터를 넣는 부분**/
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

                log.info("---> 티스토리 이미지 업로드 성공");
                return response.get("replacer").toString();
            } else {
                log.info("---> 티스토리 이미지 업로드 실패");
            }

            connection.disconnect();
        } catch (Exception e) {
            connection.disconnect();
            log.info("---> 티스토리 이미지 업로드 실패");
        }

        return "";
    }

    private void authorizeTistory() {

        log.info("티스토리 인증 절차 시작");

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
                throw new IllegalStateException("티스토리 인증 실패");
            }

            authCode = redirectUrl.substring(redirectUrl.indexOf("=") + 1, redirectUrl.indexOf("&"));

            log.info("티스토리 인증 완료");
        } catch (Exception e) {
            log.info("티스토리 인증 실패");
            System.exit(0);
        } finally {
            driver.quit();
        }

    }

    private void getAccessToken() {
        log.info("티스토리 access_token 획득 시작");

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
            System.exit(0);
        }

    }

    private void initCategoryMap() {
        log.info("티스토리 카테고리 조회 시작");

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
            log.info("티스토리 카테고리 조회 실패");
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
