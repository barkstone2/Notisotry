package html.parser

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

class DividerHtmlParser : HtmlParser {

    override fun parse(block: Map<String, Any>): Node? {
        return Element("hr")
            .attr("contenteditable", "false")
            .attr("data-ke-type", "horizontalRule")
            .attr("data-ke-style", "style5")
    }
}