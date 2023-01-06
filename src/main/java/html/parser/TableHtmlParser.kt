package html.parser

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

open class TableHtmlParser : HtmlParser, ParentNode() {

    override fun parse(block: Map<String, Any>, isListChild: Boolean): Node {
        val table = Element("table")
        table.attr("data-ke-align", "alignLeft")

        val tbody = Element("tbody")
        appendChildIfExist(block, tbody)
        table.appendChild(tbody)

        return table
    }

}