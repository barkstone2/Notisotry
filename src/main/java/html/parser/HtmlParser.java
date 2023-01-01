package html.parser;

import org.jsoup.nodes.Node;

import java.util.Map;

public interface HtmlParser {

    Node parse(Map<String, Object> block);

}
