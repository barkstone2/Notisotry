package notion.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import common.Util
import html.parser.*
import notion.NotionBlockType
import org.jsoup.nodes.Element
import java.io.File
import java.net.URL
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


}