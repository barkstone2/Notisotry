package html.parser

import org.jsoup.nodes.Element

class ToggleHtmlParser : HtmlParser, ParentNode() {

    override fun parse(block: Map<String, Any>, isListChild: Boolean): Element? {
        val toggleInfo = block["toggle"] as Map<String, Any>
        val richTexts = toggleInfo["rich_text"] as List<Map<String, Any>>

        var toggleTitle = ""

        for (richText in richTexts) {
            val plainText = richText["plain_text"] as String
            toggleTitle += plainText.replace("\n", " ")
        }

        val toggle = Element("div")
            .addClass("notistory")
            .attr("data-ke-type", "moreLess")
            .attr("data-text-more", toggleTitle)
            .attr("data-text-less", "닫기")

        val toggleButton = Element("a")
            .addClass("notistory")
            .addClass("btn-toggle-moreless")
            .text("더보기")

        val toggleContent = Element("div")
            .addClass("notistory")
            .addClass("moreless-content")

        appendChildIfExist(block, toggleContent, isListChild)

        toggle
            .appendChild(toggleButton)
            .appendChild(toggleContent)

        return toggle
    }
}