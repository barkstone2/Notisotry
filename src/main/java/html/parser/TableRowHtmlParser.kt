package html.parser

import common.Util
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

class TableRowHtmlParser : HtmlParser {

    override fun parse(block: Map<String, Any>): Node {
        val tableRow = block["table_row"] as Map<String, Any>
        val cells = tableRow["cells"] as List<List<Map<String, Any>>>

        val tr = Element("tr")

        for (cell in cells) {
            val td = Element("td")
            for (cellText in cell) {
                val textNodes = Util.paragraphHtmlParser.createTextNodes(cellText)
                td.appendChildren(textNodes)
            }
            if(!td.hasText()) td.html("&nbsp;")
            tr.appendChild(td)
        }

        return tr
    }
}