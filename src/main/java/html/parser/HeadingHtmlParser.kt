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
    private val headingStyleMap = mapOf(
            Pair("h2", "font-size: 1.875rem; margin-top: 1.875rem;"),
            Pair("h3", "font-size: 1.5rem; margin-top: 1.5rem;"),
            Pair("h4", "font-size: 1.25rem; margin-top: 1.25rem;"),
    )

    override fun parse(block: Map<String, Any>, isListChild: Boolean): Element? {
        val headingType = block["type"] as String
        val tagName = headingTagMap[headingType]
        val headingInfo = block[headingType] as Map<String, Any>

        val richTexts = headingInfo["rich_text"] as List<Map<String, Any>>

        var headingTag = Element(if(isListChild) "div" else tagName)
            .addClass("notistory")
            .addClass(tagName)
            .attr("data-ke-size", headingSizeMap[tagName])
            .attr("style",
                buildString {
                    append("margin-bottom: 0; ")
                    append("line-height: 1.2; ")
                    append(headingStyleMap[tagName])
                }
            )

        Util.paragraphHtmlParser.appendTextNodesToParent(richTexts, headingTag)

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

            appendChildIfExist(block, toggleContent, isListChild)

            toggleTag
                .appendChild(toggleButton)
                .appendChild(toggleContent)
        }

        return toggleTag ?: headingTag
    }
}