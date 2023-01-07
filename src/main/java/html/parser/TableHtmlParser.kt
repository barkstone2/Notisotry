package html.parser

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

open class TableHtmlParser : HtmlParser, ParentNode() {

    override fun parse(block: Map<String, Any>, isListChild: Boolean): Node {
        val table = Element("table")
            .addClass("notistory")
            .attr("data-ke-align", "alignLeft")
            .attr("border", "1")
            .attr("style", "border-collapse: collapse; width: 100%;")

        val tbody = Element("tbody")
            .addClass("notistory")

        appendChildIfExist(block, tbody)
        table.appendChild(tbody)

        return table
    }

}