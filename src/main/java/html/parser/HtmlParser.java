package html.parser;

import org.jsoup.nodes.Element;

import java.util.Map;

public interface HtmlParser {

    Element parse(Map<String, Object> block);

}
