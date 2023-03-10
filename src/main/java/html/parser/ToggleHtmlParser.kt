package html.parser

import common.Util
import org.jsoup.nodes.Element

class ToggleHtmlParser : HtmlParser, ParentNode() {

    override fun parse(block: Map<String, Any>, isListChild: Boolean): Element? {
        val toggleInfo = block["toggle"] as Map<String, Any>
        val richTexts = toggleInfo["rich_text"] as List<Map<String, Any>>

        val toggle = Element("div")
            .addClass("notistory")
            .attr("data-ke-type", "moreLess")
            .attr("data-text-more", "더보기")
            .attr("data-text-less", "닫기")

        val toggleButton = Element("a")
            .addClass("notistory")
            .addClass("btn-toggle-moreless")
            .text("더보기")

        val toggleContent = Element("div")
            .addClass("notistory")
            .addClass("moreless-content")

        for (richText in richTexts) {
            val textNodes = Util.paragraphHtmlParser.createTextNodes(richText)
            toggleContent.appendChildren(textNodes)
        }

        appendChildIfExist(block, toggleContent, isListChild)

        toggle
            .appendChild(toggleButton)
            .appendChild(toggleContent)

        return toggle
    }
}