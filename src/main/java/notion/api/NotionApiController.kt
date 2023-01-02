package notion.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import common.Util
import html.parser.*
import notion.NotionBlockType
import org.jsoup.nodes.Element
import java.io.File

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


}