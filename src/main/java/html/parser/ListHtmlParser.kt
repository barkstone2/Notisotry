package html.parser

import common.Util
import org.jsoup.nodes.Element

class ListHtmlParser : HtmlParser, ParentNode() {

    private val listTagMap = mapOf(
        Pair("bulleted_list_item", "ul"),
        Pair("numbered_list_item", "ol"),
    )
    private val listStyleMap = mapOf(
        Pair("ul", buildString {
            append("padding-inline-start: 1.7em; ")
            append("list-style-type: disc; ")
            append("margin: 0; ")
            append("margin-block-start: 0.6em; ")
            append("margin-block-end: 0.6em; ")
        }),
        Pair("ol", buildString {
            append("padding-inline-start: 1.6em; ")
            append("list-style-type: decimal; ")
            append("margin: 0; ")
            append("margin-block-start: 0.6em; ")
            append("margin-block-end: 0.6em; ")
        }),
    )
    private val listItemStyleMap = mapOf(
        Pair("ul", buildString {
            append("list-style-type: disc; ")
            append("padding-left: 0.1em;")
            append("position: relative; ")
            append("font-size: inherit; ")
            append("line-height: 1.6; ")
            append("color: inherit; ")
            append("text-indent: inherit; ")
        }),
        Pair("ol", buildString {
            append("list-style-type: decimal; ")
            append("padding-left: 0.2em;")
            append("position: relative; ")
            append("font-size: inherit; ")
            append("line-height: 1.6; ")
            append("color: inherit; ")
            append("text-indent: inherit; ")
        })
    )

    override fun parse(block: Map<String, Any>, isListChild: Boolean): Element? {
        val listType = block["type"] as String
        val tagName = listTagMap[listType]
        val listInfo = block[listType] as Map<String, Any>

        val richTexts = listInfo["rich_text"] as List<Map<String, Any>>

        var listWrapTag = Element(tagName)
            .addClass("notistory")
            .attr("style", listStyleMap[tagName])

        var listItemTag = Element("li")
            .addClass("notistory")
            .attr("style", listItemStyleMap[tagName])

        for (richText in richTexts) {
            val textNodes = Util.paragraphHtmlParser.createTextNodes(richText)
            listItemTag.appendChildren(textNodes)
        }
        listItemTag.appendChild(Element("br"))

        appendChildIfExist(block, listItemTag, true)

        listWrapTag.appendChild(listItemTag)

        return listWrapTag
    }
}