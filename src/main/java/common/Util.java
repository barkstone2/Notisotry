package common;

import com.fasterxml.jackson.databind.ObjectMapper;
import html.parser.ParagraphHtmlParser;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Util {

    public static ObjectMapper objectMapper = new ObjectMapper();
    public static ParagraphHtmlParser paragraphHtmlParser = new ParagraphHtmlParser();
    private static Map<String, Map<String, String>> config;

    public static void initConfig() {

        String filePath = "./src/main/resources/config.json";
        File configFile = new File(filePath);

        if(configFile.exists()) {
            try {
                config = objectMapper.readValue(new FileInputStream(configFile), HashMap.class);
            } catch (Exception e) {
                log.info("fail to load config file. check your config file structure");
            }
        } else {
            throw new IllegalStateException("config file does not exist");
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

}
