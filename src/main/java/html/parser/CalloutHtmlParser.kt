package html.parser

import common.Util
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

class CalloutHtmlParser : HtmlParser, ParentNode() {

    /**
     * 콜아웃 icon의 경우 emoji만 지원
     * 아이콘이나 이미지로 처리된 경우 기본 이모지인 전구 이모지로 대체됨 - 💡
     */
    override fun parse(block: Map<String, Any>): Node? {
        val calloutInfo = block["callout"] as Map<String, Any>

        val icon = calloutInfo["icon"] as Map<String, Any>
        val iconEmoji = icon["emoji"] as? String ?: "\uD83D\uDCA1"

        val richTexts = calloutInfo["rich_text"] as List<Map<String, Any>>

        val calloutWrap = Element("figure")
            .addClass("callout")

        val calloutIcon = Element("div")

        val iconTag = Element("span")
            .addClass("icon")
            .append(iconEmoji)

        calloutIcon.appendChild(iconTag)

        val calloutContent = Element("div")

        for (richText in richTexts) {
            val textNodes = Util.paragraphHtmlParser.createTextNodes(richText)
            calloutContent.appendChildren(textNodes)
        }

        appendChildIfExist(block, calloutContent)

        calloutWrap.appendChild(calloutIcon)
        calloutWrap.appendChild(calloutContent)

        return calloutWrap
    }
}