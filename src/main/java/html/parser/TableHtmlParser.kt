package html.parser

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

open class TableHtmlParser : HtmlParser, ParentNode() {

    override fun parse(block: MutableMap<String, Any>): Node {
        val table = Element("table")
        val tbody = Element("tbody")
        appendChildIfExist(block, tbody)
        table.appendChild(tbody)

        return table
    }

}