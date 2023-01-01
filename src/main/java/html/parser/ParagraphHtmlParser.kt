package html.parser

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

class ParagraphHtmlParser : HtmlParser {

    override fun parse(block: MutableMap<String, Any>): Element {

        val paragraph = block["paragraph"] as HashMap<String, Any>
        val richTexts = paragraph["rich_text"] as List<HashMap<String, Any>>

        if(richTexts.isEmpty()) return Element("br")

        var p = Element("p")

        for (richText in richTexts) {
            val textNodes = createTextNodes(richText)
            p.appendChildren(textNodes)
        }

        return p
    }

    fun createTextNodes(textBlock: HashMap<String, Any>): MutableList<Node> {
        var plainText = textBlock["plain_text"] as String
        val textNodes = linebreakPlaintext(plainText)

        var innerTag = createInnerTag(textBlock)
        innerTag?.appendChildren(textNodes)

        return innerTag?.let { mutableListOf(it) } ?: textNodes
    }

    private fun linebreakPlaintext(plainText: String): MutableList<Node> {
        var textNodeList = mutableListOf<Node>()
        val splitTexts = plainText.split("\n")

        for ((index, text) in splitTexts.withIndex()) {
            textNodeList.add(TextNode(text))
            if(index == splitTexts.lastIndex) continue
            textNodeList.add(Element("br"))
        }

        return textNodeList
    }

    private fun createInnerTag(richText: HashMap<String, Any>) : Element? {
        var innerTag = createAnchorTagIfLinkType(richText["href"] as String?)

        val (isAnnotatedElement, annotatedAttribute) = isAnnotatedElement(richText)

        if(isAnnotatedElement) {
            innerTag = innerTag ?: Element("span")
            setAnnotatedAttribute(innerTag, annotatedAttribute)
        }

        return innerTag
    }

    private fun setAnnotatedAttribute(
        innerTag: Element,
        annotatedAttribute: List<Pair<String, Any>>
    ) {

        for ((key, value) in annotatedAttribute) {
            when(key) {
                "color" -> innerTag.addClass(buildString { append("highlight-").append(value) })
                else -> innerTag.addClass(key)
            }
        }
    }

    private fun createAnchorTagIfLinkType(
        href: String?
    ): Element? = if(href != null) {
        val a = Element("a")
        a.attr("href", href)
        a
    } else null


    private fun isAnnotatedElement(richText : HashMap<String, Any>) : Pair<Boolean, List<Pair<String, Any>>> {
        val annotations : HashMap<String, Any> = richText["annotations"] as HashMap<String, Any>

        val annotatedAttributeList = mutableListOf<Pair<String, Any>>()
        for (key in annotations.keys) {
            if((key == "color" && annotations[key] != "default") || annotations[key] == true) {
                annotatedAttributeList += Pair(key, annotations[key]!!)
            }
        }

        return Pair(annotatedAttributeList.isNotEmpty(), annotatedAttributeList)
    }

}