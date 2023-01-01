package html.parser

import org.jsoup.nodes.Element

open class ParentNode {
    protected fun appendChildIfExist(block: MutableMap<String, Any>, parentNode: Element) {
        val hasChildren = block["has_children"] as Boolean
        if (hasChildren) {
            Main.notionApiController.appendChildNodesToParent(block["id"] as String, parentNode)
        }
    }
}