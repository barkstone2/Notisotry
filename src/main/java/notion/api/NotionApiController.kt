package notion.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import common.Util
import html.parser.*
import notion.NotionBlockType
import notion.dto.NotionPage
import org.asynchttpclient.DefaultAsyncHttpClient
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors
import javax.net.ssl.HttpsURLConnection
import kotlin.system.exitProcess

private const val BASE_URL = "https://api.notion.com/v1"

private const val DATABASE_URL_PREFIX = "databases"
private const val DATABASE_URL_SUFFIX = "query"

private const val BLOCK_URL_PREFIX = "blocks"
private const val BLOCK_URL_SUFFIX = "children"

private const val PAGE_URL_PREFIX = "pages"

private const val NOTION_VERSION_HEADER_KEY = "Notion-Version"
private const val NOTION_VERSION_HEADER_VALUE = "2022-06-28"

private const val AUTHORIZATION_HEADER_KEY = "Authorization"

class NotionApiController {
    private val log = LoggerFactory.getLogger(javaClass)
    private var databaseId: String? = null
    private var databaseOptions: HashMap<String, Any> = HashMap()
    private val objectMapper = jacksonObjectMapper()

    init {
        initDatabaseId()
    }

    private val htmlParserMapper = mapOf(
        Pair(NotionBlockType.IMAGE, ImageHtmlParser()),
        Pair(NotionBlockType.BULLETED_LIST_ITEM, ListHtmlParser()),
        Pair(NotionBlockType.NUMBERED_LIST_ITEM, ListHtmlParser()),
        Pair(NotionBlockType.CODE, CodeHtmlParser()),
        Pair(NotionBlockType.HEADING_1, HeadingHtmlParser()),
        Pair(NotionBlockType.HEADING_2, HeadingHtmlParser()),
        Pair(NotionBlockType.HEADING_3, HeadingHtmlParser()),
        Pair(NotionBlockType.PARAGRAPH, ParagraphHtmlParser()),
        Pair(NotionBlockType.TO_DO, TodoHtmlParser()),
        Pair(NotionBlockType.TOGGLE, ToggleHtmlParser()),
        Pair(NotionBlockType.TABLE, TableHtmlParser()),
        Pair(NotionBlockType.TABLE_ROW, TableRowHtmlParser()),
        Pair(NotionBlockType.QUOTE, QuoteHtmlParser()),
        Pair(NotionBlockType.CALLOUT, CalloutHtmlParser()),
        Pair(NotionBlockType.DIVIDER, DividerHtmlParser()),
    )

    private fun initDatabaseId() {
        databaseId = Util.getNotionConfigProperty("database_id")
        databaseOptions = objectMapper.readValue(
            "{\n" +
                "  \"filter\": {\n" +
                "    \"property\": \"done\",\n" +
                "    \"checkbox\": {\n" +
                "      \"equals\": false\n" +
                "    }\n" +
                "  },\n" +
                "  \"sorts\": [\n" +
                "    {\n" +
                "      \"property\": \"release-date\",\n" +
                "      \"direction\": \"ascending\"\n" +
                "    }\n" +
                "  ]\n" +
                "}"
        )
    }

    fun getNewArticlesForRegister() : List<NotionPage>? {
        log.info("노션 데이터베이스 목록 로딩 시작")

        val notionPageInfos = mutableListOf<NotionPage>()

        try {

            val urlString =
                Util.createUrlByPrefixAndSuffix(BASE_URL, DATABASE_URL_PREFIX, DATABASE_URL_SUFFIX, databaseId)

            val url = URL(urlString)
            val connection = url.openConnection() as HttpsURLConnection
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty(AUTHORIZATION_HEADER_KEY, Util.getNotionConfigProperty("authorization"))
            connection.setRequestProperty(NOTION_VERSION_HEADER_KEY, NOTION_VERSION_HEADER_VALUE)
            connection.setRequestProperty("Content-Type", "application/json")

            val jsonParam = Util.objectMapper.writeValueAsString(databaseOptions)
            connection.outputStream.use { os ->
                val requestData = jsonParam.toByteArray(charset("utf-8"))
                os.write(requestData)
            }
            connection.connect()

            val response = objectMapper.readValue<Map<String, Any>>(connection.inputStream)
            val results =
                objectMapper.convertValue(response["results"], object : TypeReference<List<Map<String, Any>>>() {})

            for (resultMap in results) {
                notionPageInfos += createNotionPageInfo(resultMap)
            }

        } catch (e : Exception) {
            log.info("노션 데이터베이스 목록 로딩 실패")
            exitProcess(0)
        }

        return notionPageInfos
    }

    private fun createNotionPageInfo(resultMap: Map<String, Any>): NotionPage {
        log.info("-> 노션 데이터베이스 페이지 정보 생성 시작")
        val startTime = System.currentTimeMillis()
        var notionPage: NotionPage
        var titleString = ""

        try {

            val parentId = resultMap["id"] as String
            val properties = objectMapper.convertValue(resultMap["properties"], object : TypeReference<Map<String, Any>>() {})

            val tag = objectMapper.convertValue(properties["tag"], object : TypeReference<Map<String, Any>>() {})
            val tags = objectMapper.convertValue(
                tag["multi_select"],
                object : TypeReference<List<Map<String, String>>?>() {})
            val tagNameList = tags
                ?.stream()
                ?.map { t: Map<String, String> -> t["name"] }
                ?.collect(Collectors.toList())

            val pageLink =
                objectMapper.convertValue(properties["page-link"], object : TypeReference<Map<String, String>>() {})
            val pageId =
                with(pageLink["url"]) {
                    var temp = this ?: throw IllegalStateException("게시글 링크가 누락됐습니다.")
                    temp = if (temp.contains("-")) {
                        temp.substring(temp.lastIndexOf("-") + 1)
                    } else {
                        temp.substring(temp.lastIndexOf("/") + 1)
                    }
                    temp
                }

            val category =
                objectMapper.convertValue(properties["category"], object : TypeReference<Map<String, Any>>() {})
            val selectedCategory =
                objectMapper.convertValue(category["select"], object : TypeReference<Map<String, String>?>() {})
            val categoryName = selectedCategory?.get("name")

            val releaseState =
                objectMapper.convertValue(properties["release-state"], object : TypeReference<Map<String, Any>>() {})
            val status =
                objectMapper.convertValue(releaseState["status"], object : TypeReference<Map<String, String>>() {})
            val releaseStateString = status["name"]

            val allowComment =
                objectMapper.convertValue(
                    properties["allow-comment"],
                    object : TypeReference<Map<String, String>>() {})
            val isAllowComment = objectMapper.convertValue(allowComment["checkbox"], Boolean::class.java)

            val titleWrap =
                objectMapper.convertValue(properties["title"], object : TypeReference<Map<String, Any>>() {})
            val title =
                objectMapper.convertValue(
                    titleWrap["title"], object : TypeReference<MutableList<Map<String, Any>>>() {})
            titleString = objectMapper.convertValue(
                if (title?.size == 0) "default-title" else title[0]?.get("plain_text") ?: "default-title",
                String::class.java
            )

            val releaseDate =
                objectMapper.convertValue(properties["release-date"], object : TypeReference<Map<String, Any>>() {})
            val date =
                objectMapper.convertValue(releaseDate["date"], object : TypeReference<Map<String, Any>?>() {})
            val releaseDateText = objectMapper.convertValue(date?.get("start"), String::class.java)
            val releaseDateValue =
                if (releaseDateText != null)
                    LocalDateTime.parse(releaseDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
                else LocalDateTime.now()

            notionPage = NotionPage(
                titleString, tagNameList, pageId, categoryName,
                releaseStateString, releaseDateValue, isAllowComment, parentId
            )
            notionPage.content = getPageContent(notionPage)

        } catch (e : Exception) {
            log.info("-> 노션 데이터베이스 페이지 정보 생성 실패")
            exitProcess(0)
        }

        val endTime = System.currentTimeMillis()

        log.info("-> 노션 데이터베이스 페이지 정보 생성 성공\n처리 페이지 제목 : {}\n소요 시간 : {} second", titleString, (endTime - startTime) / 1000)

        return notionPage
    }

    private fun getPageContent(page: NotionPage) : String {
        log.info("--> \"{}\" 페이지 로딩 시작", page.title)

        var urlString = Util.createUrlByPrefixAndSuffix(BASE_URL, BLOCK_URL_PREFIX, BLOCK_URL_SUFFIX, page.pageId)

        val pageContent = Document("")

        try {
            pageContent.outputSettings().prettyPrint(false)
            getPageContentRecursive(urlString, null, pageContent)

        } catch (e : Exception) {
            log.info("--> \"{}\" 페이지 로딩 실패", page.title)
            exitProcess(0)
        }

        log.info("--> \"{}\" 페이지 로딩 성공", page.title)

        return pageContent.html()
    }

    private fun getPageContentRecursive(
        baseUrl: String, parameterMap: Map<String, String>?, pageContent: Element,
        isListChild: Boolean = false
    ) {
        val urlString = Util.addParametersToUrl(baseUrl, parameterMap)
        val url = URL(urlString)
        val connection = url.openConnection() as HttpsURLConnection
        connection.setRequestProperty(AUTHORIZATION_HEADER_KEY, Util.getNotionConfigProperty("authorization"))
        connection.setRequestProperty(NOTION_VERSION_HEADER_KEY, NOTION_VERSION_HEADER_VALUE)

        connection.connect()

        val response = objectMapper.readValue<Map<String, Any>>(connection.inputStream)
        val hasMore = response["has_more"] as Boolean
        val results = objectMapper.convertValue(response["results"], object:TypeReference<List<Map<String, Any>>>(){})

        var listNumber = 0
        for (result in results) {
            val type = NotionBlockType.getByType(result["type"] as String)
            val htmlParser = htmlParserMapper[type] ?: continue
            val parseResult = htmlParser.parse(result, isListChild) ?: continue
            if(type.equals(NotionBlockType.NUMBERED_LIST_ITEM)) {
                parseResult.attr("start", (++listNumber).toString())
            } else {
                listNumber = if(Util.isEmptyParagraph(result)) listNumber else 0
            }
            pageContent.appendChild(parseResult)
        }

        if(hasMore) {
            getPageContentRecursive(baseUrl, mapOf("start_cursor" to response["next_cursor"] as String), pageContent)
        }
    }

    fun appendChildNodesToParent(
        parentId: String,
        parentNode: Element,
        isListChild: Boolean = false
    ) {
        var urlString = Util.createUrlByPrefixAndSuffix(BASE_URL, BLOCK_URL_PREFIX, BLOCK_URL_SUFFIX, parentId)
        getPageContentRecursive(urlString, null, parentNode, isListChild)
    }

    fun changeArticleState(notionPage: NotionPage) {
        if(!notionPage.isDone) return

        log.info("노션 데이터베이스 완료 처리 시작")

        val urlString = Util.createUrlByPrefixAndSuffix(BASE_URL, PAGE_URL_PREFIX, "", notionPage.parentId)

        val client = DefaultAsyncHttpClient()
        client
            .prepare("PATCH", urlString)
            .setHeader(AUTHORIZATION_HEADER_KEY, Util.getNotionConfigProperty("authorization"))
            .setHeader(NOTION_VERSION_HEADER_KEY, NOTION_VERSION_HEADER_VALUE)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"properties\" : {\"done\" : {\"checkbox\" : true}}}")
            .execute()
            .toCompletableFuture()
            .thenAccept { r ->
                log.info(
                    if (r.statusCode == 200) "노션 데이터베이스 완료 처리 성공"
                    else "노션 데이터베이스 완료 처리 실패 : $r.statusCode"
                )
            }
            .join()

        client.close()
    }

}