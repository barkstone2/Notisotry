package notion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import notion.WorkType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class NotionPage {

    private String parentId;
    private String pageId;
    private String title;
    private List<String> tag;
    private String category;
    private String releaseState;
    @JsonFormat
    private LocalDateTime releaseDate;
    private boolean allowComment = true;
    private String content;
    private WorkType workType;
    private String articleId;
    private boolean succeed = false;

    public NotionPage(String title, List<String> tag, String pageId, String category, String releaseState, LocalDateTime releaseDate, boolean allowComment, String parentId, String articleId, WorkType workType) {
        this.title = title;
        this.tag = tag;
        this.pageId = pageId;
        this.category = category;
        this.releaseState = releaseState;
        this.releaseDate = releaseDate;
        this.allowComment = allowComment;
        this.parentId = parentId;
        this.articleId = articleId;
        this.workType = workType;
    }

    public String getParentId() {
        return parentId;
    }

    public String getTitle() {
        return title;
    }

    public String getTag() {
        if(tag == null || tag.isEmpty()) return "";
        return String.join(",", tag);
    }

    public String getPageId() {
        return pageId;
    }

    public String getCategory() {
        return category == null ? "" : category;
    }

    public String getReleaseState() {
        return releaseState == null || releaseState.equals("비공개") ? "0" : "3";
    }

    public String getTimestamp() {
        if(releaseDate == null || releaseDate.isBefore(LocalDateTime.now())) return null;
        return String.valueOf(Timestamp.valueOf(releaseDate).getTime());
    }

    public String isAllowComment() {
        return allowComment ? "1" : "0";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public void workSucceed() {this.succeed = true;}
    public boolean isSucceed() {return succeed;}
    public boolean isRegisterWork() {
        return workType != null && workType.equals(WorkType.REGISTER);
    }

}
