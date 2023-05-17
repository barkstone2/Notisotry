package common

import org.jsoup.nodes.Element

fun Element.addStyleAttribute(attr: String): Element {
    val attributes = this.attributes()
    var styleStr = attributes.firstOrNull { a -> a.key == "style" }?.value ?: ""
    styleStr += "$attr;"

    return this.attr("style", styleStr)
}
