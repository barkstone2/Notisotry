package html.parser

import common.Util
import org.jsoup.nodes.Element

class ListHtmlParser : HtmlParser, ParentNode() {

    private val listTagMap = mapOf(
        Pair("bulleted_list_item", "ul"),
        Pair("numbered_list_item", "ol"),
    )
    private val tistoryListTypeMap = mapOf(
        Pair("ul", "disc"),
        Pair("ol", "decimal")
    )

    override fun parse(block: Map<String, Any>, isListChild: Boolean): Element? {
        val listType = block["type"] as String
        val tagName = listTagMap[listType]
        val listInfo = block[listType] as Map<String, Any>

        val richTexts = listInfo["rich_text"] as List<Map<String, Any>>

        var listWrapTag = Element(tagName)
            .addClass("notistory")
            .attr("style",
                buildString {
                    append("list-style-type: ")
                        .append(tistoryListTypeMap[tagName])
                        .append(";")
                })


        var listItemTag = Element("li")
            .addClass("notistory")

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