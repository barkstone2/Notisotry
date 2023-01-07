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
            .addClass("notistory")
            .attr("style", buildString {
                append("padding-inline-start: 1.7em; ")
                append("margin: 0; ")
                append("margin-block-start: 0.6em; ")
                append("margin-block-end: 0.6em; ")
                append("text-indent: -1.7em; ")
                append("list-style: none; ")
                }
            )

        val todoItem = Element("li")
            .addClass("notistory")
            .attr("style",
                buildString {
                append("list-style: none; ")
                append("padding-left: 0.1em; ")
            }
            )

        val checkBox = Element("div")
            .addClass("checkbox")
            .addClass("notistory")
            .addClass(buildString {
                append("checkbox-")
                    .append(if (isChecked) "on" else "off")
            })
            .attr("style",
                buildString {
                    append("display: inline-flex; ")
                    append("vertical-align: text-bottom; ")
                    append("width: 16px; ")
                    append("height: 16px; ")
                    append("background-size: 16px; ")
                    append("margin-left: 2px; ")
                    append("margin-right: 5px; ")
                }
            )

        val todoText = Element("span")
            .addClass("notistory")
            .addClass(buildString {
                append("to-do-children-")
                    .append(if (isChecked) "checked" else "unchecked")
            })

        if(isChecked)
            todoText
                .attr("style", buildString {
                    append("text-decoration: line-through; ")
                    append("opacity: 0.375;")
                })

        for (richText in richTexts) {
            val textNodes = Util.paragraphHtmlParser.createTextNodes(richText, true)
            todoText.appendChildren(textNodes)
        }

        todoItem
            .appendChild(checkBox)
            .appendChild(todoText)

        todoList.appendChild(todoItem)

        appendChildIfExist(block, todoList, isListChild)

        return todoList
    }
}