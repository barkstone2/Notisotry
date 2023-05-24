package html.parser

import common.addStyleAttribute
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

        val p = Element(if(isListChild) "lchild" else "p")
            .addClass("notistory")
            .addStyleAttribute(
                buildString {
                    append("margin-top: 0.5em !important; ")
                    append("margin-bottom: 0.5em !important; ")
                }
            )

        appendTextNodesToParent(richTexts, p)

        if(richTexts.isEmpty() || isListChild) {
            p.appendChild(Element("br"))
        }

        p.attr("data-ke-size", "size16")

        val hasChildren = block["has_children"] as Boolean
        var parent = p;

        if(hasChildren) {
            val childrenDiv = Element("div")
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

    fun appendTextNodesToParent(richTexts: List<Map<String, Any>>, parent: Element, ignoreAnnotations: Boolean = false, isCodeBlock: Boolean = false) {
        var anchorList = mutableListOf<Node>()

        var processHref: String? = null
        for (richText in richTexts) {

            val href = richText["href"] as String?
            val textNodes = createTextNodes(richText, ignoreAnnotations, isCodeBlock)

            // 현재 노드가 링크가 아닌 경우 부모 태그에 삽입 후 continue
            if(href == null) {
                // 처리 중이던 링크가 있는 경우 해당 앵커 노드 먼저 처리
                if(processHref != null) {
                    appendAnchorToParent(processHref, parent, anchorList)

                    processHref = null
                    anchorList = mutableListOf()
                }

                parent.appendChildren(textNodes)
                continue
            }

            // 현재 노드가 링크고 처리중이던 링크가 없는 경우 초기값 세팅 후 continue
            if(processHref == null) {
                anchorList.addAll(textNodes)
                processHref = href
                continue
            }

            // 현재 링크와 처리중인 링크가 같은 경우 리스트에 값만 추가
            if(href == processHref) {
                anchorList.addAll(textNodes)
                continue
            }

            // 처리중이던 링크와 다른 새로운 링크를 처리하는 경우
            appendAnchorToParent(processHref, parent, anchorList)

            processHref = href
            anchorList = mutableListOf()
            anchorList.addAll(textNodes)
        }

        if(processHref != null) {
            appendAnchorToParent(processHref, parent, anchorList)
        }
    }

    fun createTextNodes(textBlock: Map<String, Any>, ignoreAnnotations: Boolean = false, isCodeBlock: Boolean = false): MutableList<Node> {
        val plainText = textBlock["plain_text"] as String
        val textNodes = linebreakPlaintext(plainText, isCodeBlock)

        val innerTag = createInnerTag(textBlock, textNodes, ignoreAnnotations)

        return mutableListOf(innerTag)
    }

    private fun linebreakPlaintext(plainText: String, isCodeBlock: Boolean = false): MutableList<Node> {
        val textNodeList = mutableListOf<Node>()
        val splitTexts = plainText.split("\n")

        for ((index, text) in splitTexts.withIndex()) {
            textNodeList.add(TextNode(text))
            if(index == splitTexts.lastIndex) continue
            textNodeList.add(TextNode("\n"))
            if(!isCodeBlock) textNodeList.add(Element("br"))
        }

        return textNodeList
    }

    private fun createInnerTag(richText: Map<String, Any>, textNodes : List<Node>, ignoreAnnotations: Boolean) : Element {
        var innerTag = Element("span")

        val (isAnnotatedElement, annotatedAttribute) = isAnnotatedElement(richText)

        if(isAnnotatedElement && !ignoreAnnotations) {
            innerTag.appendChildren(textNodes)
            innerTag = setAnnotatedAttribute(innerTag, annotatedAttribute)
        } else {
            innerTag.appendChildren(textNodes)
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
                        .addStyleAttribute(getRgb(value as String))
                        .appendChild(tempInnerTag)
                }
                "code" -> {
                    tempInnerTag = Element("span")
                        .addClass("code")
                        .addStyleAttribute(
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
            .attr("target", "_blank")
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

    private fun appendAnchorToParent(processHref: String, parent: Element, anchorList: MutableList<Node>) {
        val anchor = Element("a")
            .addClass("notistory")
            .attr("href", processHref)
            .attr("target", "_blank")

        anchor.appendChildren(anchorList)
        parent.appendChild(anchor)
    }

}