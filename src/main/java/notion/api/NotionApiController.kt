package notion.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import common.Util
import html.parser.*
import notion.NotionBlockType
import notion.dto.NotionPage
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors
import javax.net.ssl.HttpsURLConnection

private const val BASE_URL = "https://api.notion.com/v1"

private const val DATABASE_URL_PREFIX = "databases"
private const val DATABASE_URL_SUFFIX = "query"

private const val BLOCK_URL_PREFIX = "blocks"
private const val BLOCK_URL_SUFFIX = "children"

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
            File("./src/main/resources/notion_database_filter_query.json"),
        )
    }

    fun getNewArticlesForRegister() : List<NotionPage>? {
        val urlString = Util.createUrlByPrefixAndSuffix(BASE_URL, DATABASE_URL_PREFIX, DATABASE_URL_SUFFIX, databaseId)

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
        val results = objectMapper.convertValue(response["results"], object:TypeReference<List<Map<String, Any>>>(){})
        val notionPageInfos = mutableListOf<NotionPage>()

        for (resultMap in results) {
            notionPageInfos += createNotionPageInfo(resultMap)
        }

        return notionPageInfos
    }

    private fun createNotionPageInfo(resultMap: Map<String, Any>): NotionPage {
        val parentId = resultMap["id"] as String
        val properties = objectMapper.convertValue(resultMap["properties"], object : TypeReference<Map<String, Any>>() {})

        val startTime = System.currentTimeMillis()
        log.info("create the notion page info start")
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
        val titleString = objectMapper.convertValue(
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

        val notionPage = NotionPage(
            titleString, tagNameList, pageId, categoryName,
            releaseStateString, releaseDateValue, isAllowComment, parentId
        )
        notionPage.content = getPageContent(notionPage)

        val endTime = System.currentTimeMillis()
        log.info("created complete page info - title : {}", titleString)
        log.info("elapsed time is : {} second", (endTime - startTime) / 1000)

        return notionPage
    }

    private fun getPageContent(page: NotionPage) : String {
        var urlString = Util.createUrlByPrefixAndSuffix(BASE_URL, BLOCK_URL_PREFIX, BLOCK_URL_SUFFIX, page.pageId)

        val pageContent = Document("")
        pageContent.outputSettings().prettyPrint(false)

        val article = Element("article")
        article.id("notionArticle")

        getPageContentRecursive(urlString, null, article)

        pageContent.appendChild(article)

        return pageContent.html()
    }

    private fun getPageContentRecursive(baseUrl: String, parameterMap: Map<String, String>?, pageContent: Element) {
        val urlString = Util.addParametersToUrl(baseUrl, parameterMap)
        val url = URL(urlString)
        val connection = url.openConnection() as HttpsURLConnection
        connection.setRequestProperty(AUTHORIZATION_HEADER_KEY, Util.getNotionConfigProperty("authorization"))
        connection.setRequestProperty(NOTION_VERSION_HEADER_KEY, NOTION_VERSION_HEADER_VALUE)

        connection.connect()

        val response = objectMapper.readValue<Map<String, Any>>(connection.inputStream)
        val hasMore = response["has_more"] as Boolean
        val results = objectMapper.convertValue(response["results"], object:TypeReference<List<Map<String, Any>>>(){})

        for (result in results) {
            val htmlParser = htmlParserMapper[NotionBlockType.getByType(result["type"] as String)] ?: continue
            val parseResult = htmlParser.parse(result) ?: continue
            pageContent.appendChild(parseResult)
        }

        if(hasMore) {
            getPageContentRecursive(baseUrl, mapOf("start_cursor" to response["next_cursor"] as String), pageContent)
        }
    }

    fun appendChildNodesToParent(parentId: String, parentNode: Element) {
        var urlString = Util.createUrlByPrefixAndSuffix(BASE_URL, BLOCK_URL_PREFIX, BLOCK_URL_SUFFIX, parentId)
        getPageContentRecursive(urlString, null, parentNode)
    }

}