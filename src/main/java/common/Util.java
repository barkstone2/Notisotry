package common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Util {

    public static ObjectMapper objectMapper = new ObjectMapper();
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

}
