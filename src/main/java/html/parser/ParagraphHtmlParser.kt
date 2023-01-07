package html.parser

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

class ParagraphHtmlParser : HtmlParser, ParentNode() {

    private val colorMap = mapOf(
        Pair("red", "color: rgba(212, 76, 71, 1);"),
        Pair("pink", "color: rgba(193, 76, 138, 1);"),
        Pair("purple", "color: rgba(144, 101, 176, 1);"),
        Pair("blue", "color: rgba(51, 126, 169, 1);"),
        Pair("green", "color: rgba(68, 131, 97, 1);"),
        Pair("yellow", "color: rgba(203, 145, 47, 1);"),
        Pair("orange", "color: rgba(217, 115, 13, 1);"),
        Pair("brown", "color: rgba(159, 107, 83, 1);"),
        Pair("gray", "color: rgba(120, 119, 116, 1);"),
    )

    private val tagNameMap = mapOf(
        Pair("bold", "b"),
        Pair("italic", "i"),
        Pair("strikethrough", "s"),
        Pair("underline", "u"),
    )

    override fun parse(block: Map<String, Any>, isListChild: Boolean): Node {

        val paragraph = block["paragraph"] as Map<String, Any>
        val richTexts = paragraph["rich_text"] as List<Map<String, Any>>

        var p = Element(if(isListChild) "lchild" else "p")
            .addClass("notistory")
            .attr("style",
                buildString {
                    append("margin-top: 0.5em !important; ")
                    append("margin-bottom: 0.5em !important; ")
                }
            )

        for (richText in richTexts) {
            val textNodes = createTextNodes(richText)
            p.appendChildren(textNodes)
        }

        if(richTexts.isEmpty() || isListChild) {
            p.appendChild(Element("br"))
        }

        p.attr("data-ke-size", "size16")

        val hasChildren = block["has_children"] as Boolean
        var parent = p;

        if(hasChildren) {
            var childrenDiv = Element("div")
                .addClass("indented")
                .attr("style", "padding-left: 1.5em;")

            appendChildIfExist(block, childrenDiv, isListChild)

            parent = Element("indented")
            parent
                .appendChild(p)
                .appendChild(childrenDiv)
        } else {
            appendChildIfExist(block, p, isListChild)
        }

        return parent
    }

    fun createTextNodes(textBlock: Map<String, Any>, ignoreAnnotations: Boolean = false): MutableList<Node> {
        var plainText = textBlock["plain_text"] as String
        val textNodes = linebreakPlaintext(plainText)

        var innerTag = createInnerTag(textBlock, textNodes, ignoreAnnotations)

        return innerTag?.let { mutableListOf(it) } ?: textNodes
    }

    private fun linebreakPlaintext(plainText: String): MutableList<Node> {
        var textNodeList = mutableListOf<Node>()
        val splitTexts = plainText.split("\n")

        for ((index, text) in splitTexts.withIndex()) {
            textNodeList.add(TextNode(text))
            if(index == splitTexts.lastIndex) continue
            textNodeList.add(TextNode("\n"))
            textNodeList.add(Element("br"))
        }

        return textNodeList
    }

    private fun createInnerTag(richText: Map<String, Any>, textNodes : List<Node>, ignoreAnnotations: Boolean) : Element? {
        var innerTag = createAnchorTagIfLinkType(richText["href"] as String?)

        val (isAnnotatedElement, annotatedAttribute) = isAnnotatedElement(richText)

        if(isAnnotatedElement && !ignoreAnnotations) {
            innerTag = innerTag ?: Element("span")
            innerTag.appendChildren(textNodes)
            innerTag = setAnnotatedAttribute(innerTag, annotatedAttribute)
        } else {
            innerTag?.appendChildren(textNodes)
        }

        return innerTag
    }

    private fun setAnnotatedAttribute(
        innerTag: Element,
        annotatedAttribute: List<Pair<String, Any>>
    ) : Element {

        var tempInnerTag = innerTag

        for ((key, value) in annotatedAttribute) {
            when(key) {
                "color" -> {
                    tempInnerTag = Element("span")
                        .addClass(buildString { append("highlight-").append(value) })
                        .attr("style", getRgb(value as String))
                        .appendChild(innerTag)
                }
                "code" -> {
                    tempInnerTag = Element("span")
                        .addClass("code")
                        .attr("style",
                            buildString {
                                append("background: rgba(135, 131, 120, 0.15); ")
                                append("color: #eb5757; ")
                                append("padding: 0.2em 0.4em; ")
                                append("border-radius: 3px; ")
                                append("font-size: 85%;")
                            }
                        )
                        .appendChild(tempInnerTag)
                }
                else -> {
                    tempInnerTag = Element(tagNameMap[key])
                        .appendChild(tempInnerTag)
                }
            }
        }

        return tempInnerTag.addClass("notistory")
    }

    private fun getRgb(value: String) : String {
        val isBackgroundColor = value.contains("background")
        var key = if(isBackgroundColor) {
            value.substring(0, value.indexOf("_"))
        } else {
            value
        }

        val rgb = colorMap[key]

        return buildString { append(if(isBackgroundColor) "background-" else "").append(rgb) }
    }

    private fun createAnchorTagIfLinkType(
        href: String?
    ): Element? = if(href != null) {
        Element("a")
            .addClass("notistory")
            .attr("href", href)
    } else null


    private fun isAnnotatedElement(richText : Map<String, Any>) : Pair<Boolean, List<Pair<String, Any>>> {
        val annotations = richText["annotations"] as Map<String, Any>

        val annotatedAttributeList = mutableListOf<Pair<String, Any>>()
        for (key in annotations.keys) {
            if((key == "color" && annotations[key] != "default") || annotations[key] == true) {
                annotatedAttributeList += Pair(key, annotations[key]!!)
            }
        }

        return Pair(annotatedAttributeList.isNotEmpty(), annotatedAttributeList)
    }

}