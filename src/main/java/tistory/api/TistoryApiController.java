package tistory.api;

import lombok.extern.slf4j.Slf4j;

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

}
