package notion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class NotionPage {

    private String pageId;
    private String title;
    private List<String> tag;
    private String category;
    private String releaseState;
    @JsonFormat
    private LocalDateTime releaseDate;
    private boolean allowComment;
    private String content;
    private boolean done;

    public NotionPage(String title, List<String> tag, String pageId, String category, String releaseState, LocalDateTime releaseDate, boolean allowComment) {
        this.title = title;
        this.tag = tag;
        this.pageId = pageId;
        this.category = category;
        this.releaseState = releaseState;
        this.releaseDate = releaseDate;
        this.allowComment = allowComment;
    }

    public NotionPage(String title, List<String> tag, String pageId, String category, String releaseState, LocalDateTime releaseDate, boolean allowComment, String content, boolean done) {
        this.title = title;
        this.tag = tag;
        this.pageId = pageId;
        this.category = category;
        this.releaseState = releaseState;
        this.releaseDate = releaseDate;
        this.allowComment = allowComment;
        this.content = content;
        this.done = done;
    }

    public NotionPage() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getReleaseState() {
        return releaseState;
    }

    public void setReleaseState(String releaseState) {
        this.releaseState = releaseState;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isAllowComment() {
        return allowComment;
    }

    public void setAllowComment(boolean allowComment) {
        this.allowComment = allowComment;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

}
