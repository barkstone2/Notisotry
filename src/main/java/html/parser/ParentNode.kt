package html.parser

import Main
import org.jsoup.nodes.Element

open class ParentNode {
    protected fun appendChildIfExist(
        block: Map<String, Any>,
        parentNode: Element,
        isListChild: Boolean = false
    ) {
        val hasChildren = block["has_children"] as Boolean
        if (hasChildren) {
            Main.notionApiController.appendChildNodesToParent(
                block["id"] as String,
                parentNode,
                isListChild
            )
        }
    }
}