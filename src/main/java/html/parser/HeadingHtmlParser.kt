package html.parser

import common.Util
import org.jsoup.nodes.Element

class HeadingHtmlParser : HtmlParser, ParentNode() {

    private val headingTagMap = mapOf(
        Pair("heading_1", "h2"),
        Pair("heading_2", "h3"),
        Pair("heading_3", "h4"),
    )
    private val headingSizeMap = mapOf(
        Pair("h2", "size26"),
        Pair("h3", "size23"),
        Pair("h4", "size20"),
    )

    override fun parse(block: Map<String, Any>, isListChild: Boolean): Element? {
        val headingType = block["type"] as String
        val tagName = headingTagMap[headingType]
        val headingInfo = block[headingType] as Map<String, Any>

        val richTexts = headingInfo["rich_text"] as List<Map<String, Any>>

        var headingTag = Element(tagName)
        headingTag.attr("data-ke-size", headingSizeMap[tagName])

        for (richText in richTexts) {
            val textNodes = Util.paragraphHtmlParser.createTextNodes(richText)
            headingTag.appendChildren(textNodes)
        }

        val isToggleable = headingInfo["is_toggleable"] as Boolean
        var toggleTag : Element? = null
        if(isToggleable) {
            toggleTag = Element("div")
                .addClass("notistory")
                .attr("data-ke-type", "moreLess")
                .attr("data-text-more", "더보기")
                .attr("data-text-less", "닫기")

            val toggleButton = Element("a")
                .addClass("btn-toggle-moreless")
                .addClass("notistory")
                .text("더보기")

            val toggleContent = Element("div")
                .addClass("moreless-content")

            toggleContent.appendChild(headingTag)

            appendChildIfExist(block, toggleContent)

            toggleTag
                .appendChild(toggleButton)
                .appendChild(toggleContent)
        }

        return toggleTag ?: headingTag
    }
}