package html.parser

import common.Util
import org.jsoup.nodes.Element

class TodoHtmlParser : HtmlParser, ParentNode() {

    override fun parse(block: Map<String, Any>, isListChild: Boolean): Element? {
        val todoInfo = block["to_do"] as Map<String, Any>

        val isChecked = todoInfo["checked"] as Boolean
        val richTexts = todoInfo["rich_text"] as List<Map<String, Any>>

        val todoList = Element("ul")
            .addClass("to-do-list")

        val todoItem = Element("li")

        val checkBox = Element("div")
            .addClass("checkbox")
            .addClass(buildString {
                append("checkbox-")
                    .append(if (isChecked) "on" else "off")
            })

        val todoText = Element("span")
            .addClass(buildString {
                append("to-do-children-")
                    .append(if (isChecked) "checked" else "unchecked")
            })

        for (richText in richTexts) {
            val textNodes = Util.paragraphHtmlParser.createTextNodes(richText, true)
            todoText.appendChildren(textNodes)
        }

        todoItem
            .appendChild(checkBox)
            .appendChild(todoText)

        todoList.appendChild(todoItem)

        appendChildIfExist(block, todoList)

        return todoList
    }
}