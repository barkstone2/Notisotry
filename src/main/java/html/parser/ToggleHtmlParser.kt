package html.parser

import common.Util
import org.jsoup.nodes.Element

class ToggleHtmlParser : HtmlParser, ParentNode() {

    override fun parse(block: Map<String, Any>): Element? {
        val toggleInfo = block["toggle"] as Map<String, Any>
        val richTexts = toggleInfo["rich_text"] as List<Map<String, Any>>

        val toggle = Element("div")

        for (richText in richTexts) {
            val textNodes = Util.paragraphHtmlParser.createTextNodes(richText)
            toggle.appendChildren(textNodes)
        }

        toggle
            .attr("data-ke-type", "moreLess")
            .attr("data-text-more", "더보기")
            .attr("data-text-less", "닫기")

        appendChildIfExist(block, toggle)

        return toggle
    }
}