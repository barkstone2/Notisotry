import common.Util;
import lombok.extern.slf4j.Slf4j;
import notion.api.NotionApiController;
import notion.dto.NotionPage;
import tistory.api.TistoryApiController;

import java.util.List;

@Slf4j
public class Main {

    public static TistoryApiController tistoryApiController;
    public static NotionApiController notionApiController;

    public static void main(String[] args) {

        log.info("Notistory 시작");

        Util.initConfig();

        tistoryApiController = new TistoryApiController();
        notionApiController = new NotionApiController();

        List<NotionPage> result = notionApiController.getNewArticlesForRegister();

        for (NotionPage notionPage : result) {
            tistoryApiController.writeNewArticle(notionPage);
            notionApiController.changeArticleState(notionPage);
        }

        log.info("Notistory 종료");
    }
}
