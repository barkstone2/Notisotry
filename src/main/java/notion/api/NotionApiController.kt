package notion.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import common.Util
import java.io.File

class NotionApiController {
    private var databaseId: String? = null
    private var databaseOptions: HashMap<String, Any> = HashMap()
    private val objectMapper = jacksonObjectMapper()

    init {
        initDatabaseId()
    }

    private fun initDatabaseId() {
        databaseId = Util.getNotionConfigProperty("database_id")
        databaseOptions = objectMapper.readValue(
            File("./src/main/resources/notion_database_filter_query.json"),
        )
    }


}