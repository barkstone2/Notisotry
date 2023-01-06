package html.parser

import common.Util
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

class QuoteHtmlParser : HtmlParser {

    override fun parse(block: Map<String, Any>, isListChild: Boolean): Node? {
        val quoteInfo = block["quote"] as Map<String, Any>
        val richTexts = quoteInfo["rich_text"] as List<Map<String, Any>>

        val blockquote = Element("blockquote")
        blockquote.attr("data-ke-style", "style2")

        for (richText in richTexts) {
            val textNodes = Util.paragraphHtmlParser.createTextNodes(richText)
            blockquote.appendChildren(textNodes)
        }

        return blockquote
    }
}