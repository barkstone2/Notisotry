package html.parser

import common.Util
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

class CalloutHtmlParser : HtmlParser, ParentNode() {

    /**
     * 콜아웃 icon의 경우 emoji만 지원
     * 아이콘이나 이미지로 처리된 경우 기본 이모지인 전구 이모지로 대체됨 - 💡
     */
    override fun parse(block: Map<String, Any>, isListChild: Boolean): Node? {
        val calloutInfo = block["callout"] as Map<String, Any>

        val icon = calloutInfo["icon"] as Map<String, Any>
        val iconEmoji = icon["emoji"] as? String ?: "\uD83D\uDCA1"

        val richTexts = calloutInfo["rich_text"] as List<Map<String, Any>>

        val calloutWrap = Element("figure")
            .addClass("callout")
            .addClass("notistory")
            .attr("style",
                "background: rgba(135, 131, 120, 0.15); " +
                        "padding: 1rem; display: " +
                        "flex; white-space: " +
                        "pre-wrap; " +
                        "border-radius: 3px;")

        val calloutIcon = Element("div")
            .addClass("notistory")

        val iconTag = Element("span")
            .addClass("icon")
            .addClass("notistory")
            .append(iconEmoji)

        calloutIcon.appendChild(iconTag)

        val calloutContent = Element("div")
            .addClass("notistory")

        for (richText in richTexts) {
            val textNodes = Util.paragraphHtmlParser.createTextNodes(richText)
            calloutContent.appendChildren(textNodes)
        }

        appendChildIfExist(block, calloutContent, isListChild)

        calloutWrap.appendChild(calloutIcon)
        calloutWrap.appendChild(calloutContent)

        return calloutWrap
    }
}