package notion;

import java.util.Arrays;

public enum NotionBlockType {

    PARAGRAPH("paragraph"),
    HEADING_1("heading_1"),
    HEADING_2("heading_2"),
    HEADING_3("heading_3"),
    BULLETED_LIST_ITEM("bulleted_list_item"),
    NUMBERED_LIST_ITEM("numbered_list_item"),
    TO_DO("to_do"),
    TOGGLE("toggle"),
    EMBED("embed"),
    IMAGE("image"),
    VIDEO("video"),
    CALLOUT("callout"),
    QUOTE("quote"),
    DIVIDER("divider"),
    TABLE_OF_CONTENTS("table_of_contents"),
    COLUMN("column"),
    COLUMN_LIST("column_list"),
    TABLE("table"),
    TABLE_ROW("table_row"),
    CODE("code"),
    UNSUPPORTED("unsupported"),
    ;
//            ,"file"
//            ,"pdf"
//            ,"bookmark"
//            ,"equation"
//            ,"link_preview"
//            ,"synced_block"
//            ,"template"
//            ,"link_to_page"


    public String type;
    private static final NotionBlockType[] VALUES = NotionBlockType.values();

    public static NotionBlockType getByType(String type) {
        return Arrays
                .stream(VALUES)
                .filter(v -> v.type.equals(type))
                .findFirst()
                .orElse(null);
    }

    NotionBlockType(String type) {
        this.type = type;
    }

}
