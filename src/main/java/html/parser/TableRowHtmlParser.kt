package html.parser

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

class TableRowHtmlParser : HtmlParser {

    override fun parse(block: MutableMap<String, Any>): Node {
        val tableRow = block["table_row"] as HashMap<String, Any>
        val cells = tableRow["cells"] as List<List<HashMap<String, Any>>>

        val tr = Element("tr")
        val paragraphHtmlParser = ParagraphHtmlParser()

        for (cell in cells) {
            val td = Element("td")
            for (cellText in cell) {
                val textNodes = paragraphHtmlParser.createTextNodes(cellText)
                td.appendChildren(textNodes)
            }
            if(!td.hasText()) td.html("&nbsp;")
            tr.appendChild(td)
        }

        return tr
    }
}