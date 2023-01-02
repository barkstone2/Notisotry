package html.parser

import common.Util
import org.jsoup.nodes.Element

class CodeHtmlParser : HtmlParser {

    override fun parse(block: Map<String, Any>): Element? {

        val codeInfo = block["code"] as Map<String, Any>
        val language = codeInfo["language"] as String
        val richTexts = codeInfo["rich_text"] as List<Map<String, Any>>

        val codeWrap = Element("pre")
        with(codeWrap) {
            addClass(language)
            addClass("hljs")
            attr("data-ke-language", language)
            attr("data-ke-type", "codeblock")
        }

        for (richText in richTexts) {
            val textNodes = Util.paragraphHtmlParser.createTextNodes(richText, true)
            codeWrap.appendChildren(textNodes)
        }

        return codeWrap
    }
}