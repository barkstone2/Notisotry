package html.parser

import org.jsoup.nodes.Node

interface HtmlParser {
    fun parse(block: Map<String, Any>): Node?
}