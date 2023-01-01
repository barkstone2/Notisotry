package html.parser

import Main
import org.jsoup.nodes.DataNode
import org.jsoup.nodes.Node

class ImageHtmlParser : HtmlParser {

    override fun parse(block: Map<String, Any>) : Node? {
        val image = block["image"] as HashMap<String, Any>

        val imageType = image["type"] as String
        val imageInfo = image[imageType] as HashMap<String, Any>
        val imageUrl = imageInfo["url"] as String

        if(imageUrl.isNullOrEmpty()) return null

        // replacer만 받아와서 content에 넣으면 자동으로 이미지 태그 생성해줌
        // caption 정보는 처리 불가능
        // replacer를 반환받지 못하는 경우 이미지를 본문에 추가하지 않음
        val imageReplacer : String = Main.tistoryApiController.uploadImageFileAndGetReplacer(imageUrl);

        return DataNode(imageReplacer)
    }

}

