package common;

import com.fasterxml.jackson.databind.ObjectMapper;
import html.parser.ParagraphHtmlParser;
import lombok.extern.slf4j.Slf4j;
import notion.NotionBlockType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Slf4j
public class Util {

    public static ObjectMapper objectMapper = new ObjectMapper();
    public static ParagraphHtmlParser paragraphHtmlParser = new ParagraphHtmlParser();
    private static Map<String, Map<String, String>> config;

    public static void initConfig() {

        log.info("설정 파일 로딩 중...");

        String filePath = "./config.json";
        File configFile = new File(filePath);

        if(configFile.exists()) {
            try {
                config = objectMapper.readValue(new FileInputStream(configFile), HashMap.class);
                log.info("설정 파일 로딩 완료");
            } catch (Exception e) {
                log.info("설정 파일 로딩 실패, 설정 파일을 확인해주세요.");
                System.exit(0);
            }
        } else {
            log.info("설정 파일이 존재하지 않습니다.");
            System.exit(0);
        }
    }

    private static String getConfigProperty(String directory, String key) {
        if (config == null) {
            throw new IllegalStateException("need config file");
        }

        return config.get(directory).get(key);
    }

    public static String getNotionConfigProperty(String key) {
        return getConfigProperty("notion", key);
    }

    public static String getTistoryConfigProperty(String key) {
        return getConfigProperty("tistory", key);
    }

    public static ChromeDriver getChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.setLogLevel(ChromeDriverLogLevel.OFF);

        ChromeDriver driver = new ChromeDriver(options);
        driver.setLogLevel(Level.OFF);

        return driver;
    }

    public static String createUrlByPrefixAndSuffix(String baseUrl, String prefix, String suffix, String resourceId) {
        StringBuilder builder = new StringBuilder(baseUrl);
        builder.append("/");
        builder.append(prefix);
        builder.append("/");
        builder.append(resourceId);
        builder.append("/");
        builder.append(suffix);

        return builder.toString();
    }

    public static String addParametersToUrl(String baseUrl, @Nullable Map<String, String> queryStringMap) {
        if(queryStringMap == null || queryStringMap.isEmpty()) return baseUrl;

        StringBuilder builder = new StringBuilder(baseUrl);
        builder.append("?");

        queryStringMap
                .keySet()
                .stream()
                .forEach(k -> {
                    builder.append(k);
                    builder.append("=");
                    builder.append(queryStringMap.get(k));
                    builder.append("&");
                });

        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    public static Boolean isEmptyParagraph(Map<String, Object> block) {
        NotionBlockType type = NotionBlockType.getByType((String) block.get("type"));
        if(!type.equals(NotionBlockType.PARAGRAPH)) return false;

        Map<String, Object> paragraph = (Map<String, Object>) block.get("paragraph");
        List<Map<String, Object>> richTexts = (List<Map<String, Object>>) paragraph.get("rich_text");

        return richTexts.isEmpty();
    }
}
