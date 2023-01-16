import common.Util;
import lombok.extern.slf4j.Slf4j;
import notion.api.NotionApiController;
import notion.dto.NotionPage;
import tistory.api.TistoryApiController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Main {

    public static TistoryApiController tistoryApiController;
    public static NotionApiController notionApiController;

    public static void main(String[] args) {
        log.info("Notistory 시작");

        Util.initConfig();

        tistoryApiController = new TistoryApiController();
        notionApiController = new NotionApiController();

        List<NotionPage> articles = notionApiController.getArticlesForNotistory();
        Map<Boolean, List<NotionPage>> partitionedMap = articles
                .stream()
                .collect(Collectors.partitioningBy(NotionPage::isRegisterWork));

        List<NotionPage> registerList = partitionedMap.get(true);
        List<NotionPage> updateList = partitionedMap.get(false);

        for (NotionPage notionPage : registerList) {
            tistoryApiController.writeNewArticle(notionPage);
            notionApiController.changeDatabaseState(notionPage);
        }

        for (NotionPage notionPage : updateList) {
            tistoryApiController.updateArticle(notionPage);
            notionApiController.changeDatabaseState(notionPage);
        }

        log.info("Notistory 종료");
    }
}
